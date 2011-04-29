package au.com.gaiaresources.bdrs.controller.signup;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.TransactionStatus;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.TransactionCallbackWithSuccessOrFail;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Controller
public class UserSignUpConfirmationController extends AbstractController {
    @Autowired
    private RegistrationService registrationService;
    
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
    
    @RequestMapping("/registrationconfirmed.htm")
    public String registrationComfirmed() {
        return "registrationConfirmed";
    }
    
    @RequestMapping("/registrationconfirmationfailed.htm")
    public String registrationComfirmationFailed() {
        return "registrationConfirmationFailed";
    }
}
