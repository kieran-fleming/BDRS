package au.com.gaiaresources.bdrs.controller.admin.users;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.gaiaresources.bdrs.controller.AbstractController;

@Controller
public class UserController extends AbstractController {
    @RequestMapping(value = "/admin/users/list.htm", method = RequestMethod.GET)
    public String render() {
        return "adminUserList";
    }
}
