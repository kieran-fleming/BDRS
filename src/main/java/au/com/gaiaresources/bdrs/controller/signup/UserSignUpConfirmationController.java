package au.com.gaiaresources.bdrs.controller.signup;

import javax.annotation.security.RolesAllowed;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallbackWithSuccessOrFail;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Controller
public class UserSignUpConfirmationController extends AbstractController {
    @Autowired
    private RegistrationService registrationService;
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping(value = "/confirmregistration.htm")
    public String confirmRegistration(@RequestParam(value = "key", required = false) final String registrationKey) {
        if (StringUtils.notEmpty(registrationKey)) {
            Boolean result = doInTransaction(new TransactionCallbackWithSuccessOrFail() {
                public Boolean doInTransaction(TransactionStatus status) {
                    registrationService.completeRegistration(registrationKey);
                    return true;
                }
            });
            if (result) {
                return "redirect:/registrationconfirmed.htm";
            } else {
                return "redirect:/registrationconfirmationfailed.htm";
            }
        } else {
            return getRedirectHome();
        }
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping("/registrationconfirmed.htm")
    public String registrationComfirmed() {
        return "registrationConfirmed";
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping("/registrationconfirmationfailed.htm")
    public String registrationComfirmationFailed() {
        return "registrationConfirmationFailed";
    }
}
