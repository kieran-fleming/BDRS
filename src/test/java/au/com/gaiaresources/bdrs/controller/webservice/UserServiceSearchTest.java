package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.ArrayList;
import java.util.List;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.admin.EditUsersController;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class UserServiceSearchTest extends AbstractControllerTest {
    
    private static final int FIRST_NAME_IDX = 0;
    private static final int LAST_NAME_IDX = 1;
    private static final int ROLE_IDX = 2;
    private static final int ACTIVE_IDX = 3;
    
    private Logger log = Logger.getLogger(getClass());
    
    private static String[][] testnames = { { "Zaaa", "Hughes", Role.SUPERVISOR, "false" },
        { "Zbbb", "Haugen", Role.SUPERVISOR, "false" },
        { "Zccc", "Magnusson", Role.SUPERVISOR, "false" },
        { "Zddd", "Reid", Role.SUPERVISOR, "true" }, { "Zeee", "Jansson", Role.SUPERVISOR, "false" },
        { "Zfff", "Jensen", Role.POWERUSER, "true" },
        { "Zggg", "Pettersson", Role.POWERUSER, "true" },
        { "Zhhh", "Smith", Role.POWERUSER, "true" },
        { "Ziii", "Carlsson", Role.USER, "true" },
        { "Zjjj", "Haugen", Role.USER, "true" },
        { "Zkkk", "Halvorsen", Role.USER, "true" },
        { "Zlll", "Andersen", Role.USER, "true" },
        { "Zmmm", "Amomomo", Role.USER, "true" } };
    
    private User admin;
    private User root;
    
    private List<User> userList;

    @Before
    public void setup() throws Exception {
        
        userList = new ArrayList<User>();
        
        userList.add(userDAO.getUser("admin"));
        userList.add(userDAO.getUser("root"));
        
        // create a test user
        // Create User and the user's Locations
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), emailAddr);
    
        for (String[] name : testnames) {
            String first = name[FIRST_NAME_IDX];
            String last = name[LAST_NAME_IDX];
            User u = userDAO.createUser(first.toLowerCase(), first, last, fakeEmail(first, last), encodedPassword, registrationKey, new String[] { name[ROLE_IDX] });            
            
            u.setActive(Boolean.valueOf(name[ACTIVE_IDX]));
            userDAO.updateUser(u);
            userList.add(u);
        }
    }
    
    private String fakeEmail(String first, String last) {
        return first.toLowerCase() + "@" + last.toLowerCase() + ".com.au";
    }

    @Test
    public void testUserQuery() throws Exception {
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        request.setParameter(UserService.PARAM_CONTAINS, "z");
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(5, rowArray.size());
        Assert.assertEquals(testnames.length, json.getLong("records"));
        Assert.assertEquals("Zhhh", ((JSONObject)rowArray.get(0)).getString("firstName"));        
    }
    
    @Test
    public void testContainsQuery() throws Exception {
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        request.setParameter(UserService.PARAM_CONTAINS, "son");
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(4, rowArray.size());
        Assert.assertEquals(4, json.getLong("records"));
        Assert.assertEquals("Ziii", ((JSONObject)rowArray.get(0)).getString("firstName"));        
    }
   
    @SuppressWarnings("unchecked")
    @Test
    public void testEmptySearch() throws Exception {
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        // should have 0 matches
        request.setParameter(UserService.PARAM_CONTAINS, "sdakfjhasdfjklsda");
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(0, rowArray.size());
        Assert.assertEquals(0, json.getLong("records"));
    }
    
    @Test
    public void testSearchByNotActive() throws Exception {
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        request.setParameter(UserService.PARAM_ACTIVE, "false");

        // should return the root and admin users
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals("asserting user count", countUsers(userList, false), json.getLong("records"));
    }
    
    @Test
    public void testSearchByActive() throws Exception {
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        request.setParameter(UserService.PARAM_ACTIVE, "true");

        // should return the root and admin users
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals("asserting user count", countUsers(userList, true), json.getLong("records"));
    }
    
    @Test
    public void testSearchByDontCareActive() throws Exception {
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");
       
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        // should return the root and admin users
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        Assert.assertEquals("asserting user count", countUsers(userList, null), json.getLong("records"));
    }
    
    @Test
    public void testLimitSearchByRolesSupervisor() throws Exception {
        User accessingUser = getUserWithRole(userList, Role.SUPERVISOR);
        login(accessingUser.getName(), "password", new String[] { Role.SUPERVISOR });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");

        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        int expectedUsers = countUsersWithRole(userList, Role.SUPERVISOR) +
                            countUsersWithRole(userList, Role.POWERUSER) +
                            countUsersWithRole(userList, Role.USER);
        
        Assert.assertEquals("comparing user with role counts", expectedUsers, json.getLong("records"));
    }
    
    @Test
    public void testLimitSearchByRolesPowerUser() throws Exception {
        User accessingUser = getUserWithRole(userList, Role.POWERUSER);
        login(accessingUser.getName(), "password", new String[] { Role.POWERUSER });
        
        request.setMethod("GET");
        request.setRequestURI("/webservice/user/searchUsers.htm");

        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "5");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "2");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, EditUsersController.INPUT_ARG_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        int expectedUsers = countUsersWithRole(userList, Role.POWERUSER) +
                            countUsersWithRole(userList, Role.USER);
        
        Assert.assertEquals("comparing user with role counts", expectedUsers, json.getLong("records"));
    }
    
    private int countUsers(List<User> list, Boolean active) {
        int count = 0;
        for (User u : list) {
            Assert.assertNotNull("no null users in list allowed" , u);
            if (active == null) {
                ++count;
            } else if (active && u.isActive()) {
                ++count;
            } else if (!active && !u.isActive()) {
                ++count;
            }
        }
        Assert.assertTrue("count should never be 0", count > 0); 
        return count;
    }
    
    private int countUsersWithRole(List<User> list, String role) {
        int count = 0;
        for (User u : list) {
            Assert.assertNotNull("no null users in list allowed" , u);
            for (String r : u.getRoles()) {
                if (role.equals(r)) {
                    ++count;
                    break;
                }
            }
        }
        return count;
    }
    
    private User getUserWithRole(List<User> list, String role) {
        for (User u : list) {
            Assert.assertNotNull("no null users in list allowed" , u);
            for (String r : u.getRoles()) {
                if (role.equals(r)) {
                    return u;
                }
            }
        }
        return null;
    }
}
