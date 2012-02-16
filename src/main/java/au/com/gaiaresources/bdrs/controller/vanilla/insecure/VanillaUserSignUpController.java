package au.com.gaiaresources.bdrs.controller.vanilla.insecure;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.user.UserMetaData;
import au.com.gaiaresources.bdrs.service.user.UserMetaDataService;
import au.com.gaiaresources.bdrs.service.user.UserMetaData.UserMetaDataType;
import au.com.gaiaresources.bdrs.servlet.RecaptchaProtected;

@Controller
@RecaptchaProtected
public class VanillaUserSignUpController extends AbstractController {
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private UserMetaDataService umdService;
    @Autowired
    private MetadataDAO metadataDAO;

    /**
     * On a GET, render the form, in this case the user signup for for BA
     * 
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "/vanilla/usersignup.htm", method = RequestMethod.GET)
    public ModelAndView renderForm(HttpServletRequest request,
            HttpServletRequest response) throws Exception {
        ModelAndView mav = new ModelAndView("usersignup", "user",
                new UserSignUpForm());
        List<UserMetaData> metaList = umdService.getMetadataMap(request);
        for (UserMetaData umd : metaList) {
            umd.setValue("");
        }
        mav.addObject("metaList", metaList);
        return mav;
    }

    /**
     * On POST, save the user, send the emails, etc.
     * 
     * @param u
     * @return
     * @throws Exception
     */
    @SuppressWarnings({ "unchecked" })
    @RequestMapping(value = "/vanilla/usersignup.htm", method = RequestMethod.POST)
    public String save(
            HttpServletRequest request,
            HttpServletRequest response,
            @RequestParam(value = "username", required = true) String username,
            @RequestParam(value = "email", required = true) String email,
            @RequestParam(value = "firstName", required = true) String firstName,
            @RequestParam(value = "lastName", required = true) String lastName,
            @RequestParam(value = "password", required = true) String password)
            throws Exception {

        String redirect = hasErrors(email, firstName, lastName, username);
        if (redirect != null) {
            return redirect;
        }
        String contextPath = ContentService.getRequestURL(request);
        
        User saveResult = registrationService.signUp(username, email, firstName, lastName, password, contextPath, "ROLE_USER");

        List<UserMetaData> metaList = umdService.getMetadataMap(request);

        Map<String, String[]> params = request.getParameterMap();
        for (UserMetaData umd : metaList) {
            String[] p = params.get(umd.getKey());
            String metaValue = p != null ? p[0] : null;
            String metaKey = umd.getKey();

            if (umd.getType() == UserMetaDataType.Boolean) {
                metaValue = UserMetaData.TRUE.equals(metaValue) ? UserMetaData.TRUE
                        : UserMetaData.FALSE;
            }

            if (metaValue != null) {
                if (saveResult.metadataExists(metaKey)) {
                    Metadata md = saveResult.getMetadataObj(metaKey);
                    md.setValue(metaValue);
                    metadataDAO.update(md);
                } else {
                    Metadata md = new Metadata();
                    md.setKey(metaKey);
                    md.setValue(metaValue);
                    metadataDAO.save(md);
                    saveResult.addMetaDataObj(md);
                }
            }
        }
        userDAO.updateUser(saveResult);
        if (saveResult != null) {
            if (registrationService.isAdminApprovalRequired()) {
                getRequestContext().addMessage(new Message("user.signup.waiting")); 
            } else {
                getRequestContext().addMessage(new Message("user.signup.success"));
            }
        } else {
            getRequestContext().addMessage(new Message("user.signup.failed"));
        }
        return getRedirectHome();
    }

    /**
     * Convenience method for validating form and feeding errors in to the
     * binding result
     * @param username 
     * 
     * @param u
     *            the UserSignUpForm to validate
     * @param result
     *            the binding result to pass errors to
     * @return true if there ARE binding errors, false if there aren't.
     */
    private String hasErrors(String email, String firstName, String lastName, String username) {
        String redirect = null;
        if (userDAO.getUserByEmailAddress(email) != null) {
            // The username (email address) is already taken.
            getRequestContext().addMessage(new Message(
                    "UserSignUpForm.emailAddress[unique]"));
            redirect = "redirect:/reminder.htm";
        } else if (userDAO.getUser(username) != null) {
            // The username is already taken.
            getRequestContext().addMessage(new Message(
                    "UserSignUpForm.userName[unique]"));
            redirect = "redirect:/reminder.htm";
        } else {
            if (empty(firstName)) {
                getRequestContext().addMessage(new Message(
                        "UserSignUpForm.firstName[not.blank]"));
                redirect = "redirect:usersignup.htm";
            }
            if (empty(lastName)) {
                getRequestContext().addMessage(new Message(
                        "UserSignUpForm.lastName[not.blank]"));
                redirect = "redirect:usersignup.htm";
            }
            if (empty(email)) {
                getRequestContext().addMessage(new Message(
                        "UserSignUpForm.emailAddress[not.blank]"));
                redirect = "redirect:usersignup.htm";
            }
            if (empty(username)) {
                getRequestContext().addMessage(new Message(
                        "UserSignUpForm.userName[not.blank]"));
                redirect = "redirect:usersignup.htm";
            }
        }
        return redirect;
    }

    /**
     * Returns true if a string is null or empty.
     * 
     * @param s
     *            the string to be tested
     * @return true if it's empty
     */
    private boolean empty(String s) {
        return ((s == null) || s.isEmpty());
    }
}
