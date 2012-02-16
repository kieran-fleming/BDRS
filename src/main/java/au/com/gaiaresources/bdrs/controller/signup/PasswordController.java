package au.com.gaiaresources.bdrs.controller.signup;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
public class PasswordController extends AbstractController {
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private RegistrationService registrationService;
    
    /**
     * Render the password reminder screen.
     * @return {@link ModelAndView}
     */
    @RequestMapping(value = "/reminder.htm", method = RequestMethod.GET)
    public ModelAndView show() {
        return new ModelAndView("password-reminder");
    }
    
    /**
     * Request the password reminder.
     * @param emailAddress The provided e-mail address.
     * @return {@link String} a redirection.
     */
    @RequestMapping(value = "/reminder.htm", method = RequestMethod.POST)
    public String request(@RequestParam("emailaddress") String emailAddress) {
        User user = userDAO.getUserByEmailAddress(emailAddress);
        if (user != null) {
            registrationService.resetPassword(user);
            getRequestContext().addMessage("password.reminder.email.sent");
            return getRedirectHome();
        } else {
            getRequestContext().addMessage("password.reminder.email.not.found");
            return "redirect:/reminder.htm";
        }
    }
}
