package au.com.gaiaresources.bdrs.controller.webservice;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.admin.AdminUserSearchController;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

public class UserServiceSearchTest extends AbstractControllerTest {
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
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        request.setParameter(UserService.USER_NAME, "z");
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, AdminUserSearchController.USER_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(5, rowArray.size());
        Assert.assertEquals(testnames.length, json.getLong("records"));
        Assert.assertEquals("Zhhh", ((JSONObject)rowArray.get(0)).getString("firstName"));        
    }
   
    @SuppressWarnings("unchecked")
    @Test
    public void testEmptySearch() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        // should have 0 matches
        request.setParameter(UserService.USER_NAME, "sdakfjhasdfjklsda");
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, UserService.USER_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(0, rowArray.size());
        Assert.assertEquals(0, json.getLong("records"));
    }
}
