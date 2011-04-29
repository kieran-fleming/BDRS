package au.com.gaiaresources.bdrs.controller.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallback;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.servlet.RecaptchaProtected;

@Controller
@RecaptchaProtected
public class UserSignUpController extends AbstractController {
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private RegistrationService registrationService;
    @Autowired
    private MetadataDAO metadataDAO;

    /**
     * On a GET, render the form.
     * @return
     */
    @RequestMapping(value = "/usersignup.htm", method = RequestMethod.GET)
    public ModelAndView renderForm() {
        return new ModelAndView("usersignup", "user", new UserSignUpForm());
    }

    /**
     * On POST, save the user.
     * @param u
     * @return
     */
    @RequestMapping(value = "/usersignup.htm", method = RequestMethod.POST)
    public String save(@ModelAttribute("user") final UserSignUpForm u, BindingResult result) {
        if (result.hasErrors()) {
            return "usersignup";

        } else {

            if(userDAO.getUserByEmailAddress(u.getEmailAddress()) != null) {
                // The email address is already taken.
                result.rejectValue("emailAddress", "UserSignUpForm.emailAddress[unique]");
                return "usersignup";

            } else if (userDAO.getUser(u.getUserName()) != null){
                // The username is already taken
                result.rejectValue("userName", "UserSignUpForm.userName[unique]");
                return "usersignup";
            }else{

                User saveResult = doInTransaction(new TransactionCallback<User>() {
                    public User doInTransaction(TransactionStatus status) {
                        return registrationService.signUp(u.getUserName(), u.getEmailAddress(), u.getFirstName(),
                                                          u.getLastName(), u.getPassword());
                    }
                });

                if (saveResult != null) {
                	if (u.getSchoolName() != null) {
	                    Metadata md = new Metadata();
	                    md.setKey(Metadata.SCHOOL_NAME_KEY);
	                    md.setValue(u.getSchoolName());
	                    metadataDAO.save(md);
	
	                    saveResult.getMetadata().add(md);
	                    userDAO.updateUser(saveResult);
                	}
                	if (u.getSchoolSuburb() != null){
                	    Metadata md = new Metadata();
                	    md.setKey(Metadata.SCHOOL_SUBURB_KEY);
                	    md.setValue(u.getSchoolSuburb());
                	    metadataDAO.save(md);

                	    saveResult.getMetadata().add(md);
                	    userDAO.updateUser(saveResult);
                	}
                	if (u.getContactPhoneNumber() != null){
                	    Metadata md = new Metadata();
                        md.setKey(Metadata.TELEPHONE);
                        md.setValue(u.getContactPhoneNumber());
                        metadataDAO.save(md);

                        saveResult.getMetadata().add(md);
                        userDAO.updateUser(saveResult);
                	}
                	if (u.getClimateWatchUserName() != null){
                        Metadata md = new Metadata();
                        md.setKey(Metadata.CLIMATEWATCH_USERNAME);
                        md.setValue(u.getClimateWatchUserName());
                        metadataDAO.save(md);

                        saveResult.getMetadata().add(md);
                        userDAO.updateUser(saveResult);
                    }
                    getRequestContext().addMessage(new Message("user.signup.success"));
                } else {
                    getRequestContext().addMessage(new Message("user.signup.failed"));
                }
            }
        }
        return getRedirectHome();
    }
}
