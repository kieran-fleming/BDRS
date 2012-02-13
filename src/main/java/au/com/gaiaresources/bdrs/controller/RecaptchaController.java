package au.com.gaiaresources.bdrs.controller;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.security.RecaptchaService;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class RecaptchaController extends AbstractController {
    @Autowired
    private RecaptchaService recaptcha;
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping(value = "/recaptcha.htm", method = RequestMethod.GET)
    public String render() {
        return "recaptcha";
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping(value = "/recaptcha.htm", method = RequestMethod.POST)
    public ModelAndView validate(@RequestParam("recaptcha_challenge_field") String challenge,
                                 @RequestParam("recaptcha_response_field") String response) 
    {
        boolean result = recaptcha.validate(getRequestContext().getSessionID(), getRequestContext().getRemoteAddress(), 
                                            challenge, response);
        return new ModelAndView("recaptcha", "success", Boolean.valueOf(result));
    }
}
