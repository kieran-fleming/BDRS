package au.com.gaiaresources.bdrs.controller.admin;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;

@Controller
public class ApproveUserController extends AbstractController {

    @RequestMapping(value="/bdrs/admin/userManagement/approveUsers.htm", method=RequestMethod.GET)
    public ModelAndView render() {
        return new ModelAndView("approveUsers");
    }
}
