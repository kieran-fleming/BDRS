package au.com.gaiaresources.bdrs.controller.admin;

import javax.annotation.security.RolesAllowed;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.security.Role;

@RolesAllowed({ Role.ADMIN, Role.SUPERVISOR })
@Controller
public class ApproveUserController extends AbstractController {

    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value="/bdrs/admin/userManagement/approveUsers.htm", method=RequestMethod.GET)
    public ModelAndView render() {
        return new ModelAndView("approveUsers");
    }
}
