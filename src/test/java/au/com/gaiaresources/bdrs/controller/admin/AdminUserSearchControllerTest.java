package au.com.gaiaresources.bdrs.controller.admin;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class AdminUserSearchControllerTest extends AbstractControllerTest {

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

    @SuppressWarnings("unchecked")
    @Test
    public void testUserQuery() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/admin/userSearch.htm");

        request.setParameter(AdminUserSearchController.SEARCH, "true");

        // 13 users with the first name (and hence user name) starting with z
        request.setParameter(AdminUserSearchController.USER_NAME, "z");
        request.setParameter(AdminUserSearchController.MAX_PER_PAGE, "5");
        request.setParameter(AdminUserSearchController.getPageNumberParamName(), "2");
        request.setParameter(AdminUserSearchController.getSortParamName(), AdminUserSearchController.USER_NAME);
        request.setParameter(AdminUserSearchController.getOrderParamName(), "2");

        ModelAndView mav = this.handle(request, response);

        PagedQueryResult<User> result = (PagedQueryResult<User>) mav.getModel().get(AdminUserSearchController.PAGED_USER_RESULT);
        Assert.assertEquals(testnames.length, result.getCount());
        Assert.assertEquals(5, result.getList().size());
        Assert.assertEquals("Zhhh", result.getList().get(0).getFirstName());
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testRoleBlocking() throws Exception {
        User u = userDAO.getUser("admin");
        u.setRoles(new String[] { Role.SUPERVISOR });
        userDAO.updateUser(u);

        login("admin", "password", new String[] { Role.SUPERVISOR });
        request.setMethod("GET");
        request.setRequestURI("/admin/userSearch.htm");

        request.setParameter(AdminUserSearchController.SEARCH, "true");

        ModelAndView mav = this.handle(request, response);

        // we should only see the single user 'admin' as everyone else is a higher ROLE than us
        PagedQueryResult<User> result = (PagedQueryResult<User>) mav.getModel().get(AdminUserSearchController.PAGED_USER_RESULT);
        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals(1, result.getCount());
        Assert.assertEquals("admin", result.getList().get(0).getName());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testRoleExcluding() throws Exception {
        User u = userDAO.getUser("zhhh");
        u.setRoles(new String[] { Role.ROOT, Role.ADMIN });
        userDAO.updateUser(u);

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/admin/userSearch.htm");

        request.setParameter(AdminUserSearchController.SEARCH, "true");

        // 13 users with the first name (and hence user name) starting with z
        request.setParameter(AdminUserSearchController.USER_NAME, "z");
        request.setParameter(AdminUserSearchController.MAX_PER_PAGE, "5");
        request.setParameter(AdminUserSearchController.getPageNumberParamName(), "2");
        request.setParameter(AdminUserSearchController.getSortParamName(), AdminUserSearchController.USER_NAME);
        request.setParameter(AdminUserSearchController.getOrderParamName(), "2");

        ModelAndView mav = this.handle(request, response);

        PagedQueryResult<User> result = (PagedQueryResult<User>) mav.getModel().get(AdminUserSearchController.PAGED_USER_RESULT);
        Assert.assertEquals(5, result.getList().size());
        Assert.assertEquals(testnames.length - 1, result.getCount());
        Assert.assertEquals("Zggg", result.getList().get(0).getFirstName());
    }
}