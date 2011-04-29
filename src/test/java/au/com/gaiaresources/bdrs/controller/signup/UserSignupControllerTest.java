package au.com.gaiaresources.bdrs.controller.signup;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.BindingResult;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.user.User;

public class UserSignupControllerTest extends AbstractControllerTest {

    @Test
    public void testSignup() throws Exception
    {
        request.setMethod("POST");
        request.setRequestURI("/usersignup.htm");
        UserSignUpController controller = (UserSignUpController)getController(request);
        
        UserSignUpForm u = new UserSignUpForm();
        u.setEmailAddress("jimmy@emailprovider.com");
        u.setUserName("jimmy");
        u.setFirstName("Jim");
        u.setLastName("Jolly");
        request.setAttribute("user", u);
      
        BindingResult bresult = new BeanPropertyBindingResult(u, "user");
        controller.save(u, bresult);
        
        User newuser = userDAO.getUser("jimmy");
        Assert.assertEquals("jimmy", newuser.getName());
        Assert.assertEquals("jimmy@emailprovider.com", newuser.getEmailAddress());
    }
}
