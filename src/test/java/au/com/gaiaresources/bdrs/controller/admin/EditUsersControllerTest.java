package au.com.gaiaresources.bdrs.controller.admin;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class EditUsersControllerTest extends AbstractControllerTest {

    private static String[][] testnames = { { "Zaaa", "Hughes", Role.ADMIN },
            { "Zbbb", "Haugen", Role.ADMIN },
            { "Zccc", "Magnusson", Role.ADMIN },
            { "Zddd", "Reid", Role.ADMIN }, { "Zeee", "Jansson", Role.ADMIN },
            { "Zfff", "Jensen", Role.ADMIN },
            { "Zggg", "Pettersson", Role.ADMIN },
            { "Zhhh", "Smith", Role.ADMIN },
            { "Ziii", "Carlsson", Role.ADMIN },
            { "Zjjj", "Haugen", Role.ADMIN },
            { "Zkkk", "Halvorsen", Role.ADMIN },
            { "Zlll", "Andersen", Role.ADMIN },
            { "Zmmm", "Amomomo", Role.ADMIN } };

    @Before
    public void setup() throws Exception {
        // create a test user
        // Create User and the user's Locations
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), emailAddr);

        for (String[] name : testnames) {
            String first = name[0];
            String last = name[1];
            userDAO.createUser(first.toLowerCase(), first, last, fakeEmail(first, last), encodedPassword, registrationKey, new String[] { name[2] });
        }
        login("admin", "password", new String[] { Role.ADMIN });
    }

    private String fakeEmail(String first, String last) {
        return first.toLowerCase() + "@" + last.toLowerCase() + ".com.au";
    }
    
    @Test
    public void testApproveUserWebService() throws Exception {
        String username = "zaaa";
        
        User u = userDAO.getUser(username);
        Assert.assertNotNull("user should not be null", u);
        Assert.assertFalse("User should not be approved yet", u.isActive());
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI(EditUsersController.APPROVE_USER_WEBSERVICE_URL);
        request.setParameter(EditUsersController.PARAM_USER_PK, u.getId().toString());

        this.handle(request, response);
        
        User u2 = userDAO.getUser(username);
        
        Assert.assertTrue("user should now be active", u2.isActive());
    }
}