package au.com.gaiaresources.bdrs.controller.user;

import javax.annotation.security.RolesAllowed;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

@Controller
@RequestMapping("/admin/user/autocomplete/*.htm")
public class UserAutoCompleteController extends AbstractController {
    @Autowired
    private UserDAO userDAO;
    
    /**
     * Search by e-mail address.
     * @param value {@link String}
     * @return {@link ModelAndView}
     */
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = "byEmail.htm")//, method = RequestMethod.POST)
    public ModelAndView autocompleteByEmail(@RequestParam("value") String value) {
        return new ModelAndView("userAutoComplete", "users", userDAO.getUsersByEmailAddress(value));
    }
}
