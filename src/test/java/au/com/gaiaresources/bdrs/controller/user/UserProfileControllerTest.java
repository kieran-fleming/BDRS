package au.com.gaiaresources.bdrs.controller.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.web.servlet.ModelAndView;


import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.user.UserMetaData;
import au.com.gaiaresources.bdrs.service.user.UserMetaData.UserMetaDataType;

public class UserProfileControllerTest extends AbstractControllerTest {

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private MetadataDAO metadataDAO;

    private Integer userId;
    
    @Before
    public void setup() throws Exception {
        // create a test user
        // Create User and the user's Locations
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(au.com.gaiaresources.bdrs.util.StringUtils.generateRandomString(10, 50), emailAddr);

        User u = userDAO.createUser("testuser", "Abigail", "Ambrose", emailAddr, encodedPassword, registrationKey, new String[] { Role.USER });
        userId = u.getId();

        Set<Metadata> newMetatata = new HashSet<Metadata>();
        Map<String, String> newMetadataMap = new HashMap<String, String>();
        newMetadataMap.put(Metadata.STATE, "WA");
        newMetadataMap.put("receiveEmail", UserMetaData.TRUE);
        // really we will consider anything not "TRUE" to be false
        newMetadataMap.put("otherBool", UserMetaData.FALSE);
        Metadata md;
        for (String metaKey : newMetadataMap.keySet()) {
            if (newMetadataMap.get(metaKey) != null) {
                md = new Metadata();
                md.setKey(metaKey);
                md.setValue(newMetadataMap.get(metaKey));
                metadataDAO.save(md);
                newMetatata.add(md);
            }
        }
        u.setMetadata(newMetatata);
        userDAO.updateUser(u);

        login("testuser", "password", new String[] { Role.USER });
    }

    @After
    public void teardown() {
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testUserProfileGet() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/user/profile.htm");

        ModelAndView mv = handle(request, response);

        Assert.assertEquals("Abigail", mv.getModel().get(UserProfileController.FIRST_NAME));
        Assert.assertEquals("Ambrose", mv.getModel().get(UserProfileController.LAST_NAME));
        Assert.assertEquals("abigail.ambrose@example.com", mv.getModel().get(UserProfileController.EMAIL_ADDR));
        Assert.assertEquals("testuser", mv.getModel().get(UserProfileController.USER_NAME));

        Map<String, String> metaMap = getMetaValueMap(mv);
        
        Assert.assertEquals("WA", metaMap.get(Metadata.STATE));
        Assert.assertEquals("", metaMap.get(Metadata.TELEPHONE));

        assertUserMetaDataList(mv);
        
        List<UserMetaData> list = getMetaObject(mv);

        Assert.assertEquals(UserMetaData.TRUE, getUserMetaData(list, "receiveEmail").getValue());
        Assert.assertFalse(UserMetaData.TRUE.equals(getUserMetaData(list, "otherBool").getValue()));
        

        Assert.assertEquals((int) userId, Integer.parseInt((String) mv.getModel().get(UserProfileController.USER_ID)));
        Assert.assertEquals(UserProfileController.USER_INDICATOR, (String) mv.getModel().get(UserProfileController.EDIT_AS));
    }  

    @SuppressWarnings( { "unchecked" })
    @Test
    public void testUserProfilePost() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/user/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");

        // this should have no effect
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password...
        request.setParameter(UserProfileController.PASSWORD, "aardvark");
        
        // not setting receiveEmail so we expect it to be changed to false...
        
        // expect otherBool to be changed to true... Assert.assertEquals(true, mv.getModel().get(UserProfileController.USER_ACTIVE));
        request.setParameter("otherBool", UserMetaData.TRUE);

        // Check the return contents of the modelandview...
        ModelAndView mv = this.handle(request, response);

        // the MAV object should contain some fields ready for displaying to the user...
        Assert.assertEquals("Billy", mv.getModel().get(UserProfileController.FIRST_NAME));
        Assert.assertEquals("Bob", mv.getModel().get(UserProfileController.LAST_NAME));
        Assert.assertEquals("billy@bob.com.au", mv.getModel().get(UserProfileController.EMAIL_ADDR));
        Assert.assertEquals("testuser", mv.getModel().get(UserProfileController.USER_NAME));

        Map<String, String> metaMap = getMetaValueMap(mv);
        Assert.assertEquals("SA", metaMap.get(Metadata.STATE));
        Assert.assertEquals("123123123", metaMap.get(Metadata.TELEPHONE));

        assertUserMetaDataList(mv);

        // This causes a problem since the user object is never written to the DB, when you evict it
        // and try to retrieve the item you get a null. Retrieving the item without evicting it
        // causes the current instance of that object to be returned, i.e. it hasn't been written
        // to the DB.
        //User userToEvict = userDAO.getUser("usertest");
        //userDAO.getSessionFactory().getCurrentSession().evict(userToEvict);

        User u = userDAO.getUser("testuser");

        Assert.assertNotNull(u);
        Assert.assertEquals("Billy", u.getFirstName());
        Assert.assertEquals("Bob", u.getLastName());
        Assert.assertEquals("billy@bob.com.au", u.getEmailAddress());
        Assert.assertEquals("testuser", u.getName());

        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        Assert.assertEquals(passwordEncoder.encodePassword("aardvark", null), u.getPassword());

        Assert.assertEquals("SA", u.getMetadataValue(Metadata.STATE));
        Assert.assertEquals("123123123", u.getMetadataValue(Metadata.TELEPHONE));

        // returns empty string if key does not exist...
        Assert.assertEquals("", u.getMetadataValue(Metadata.CLIMATEWATCH_USERNAME));
        Assert.assertEquals("", u.getMetadataValue(Metadata.SCHOOL_NAME_KEY));
        Assert.assertEquals("", u.getMetadataValue(Metadata.SCHOOL_SUBURB_KEY));
        Assert.assertEquals("", u.getMetadataValue(Metadata.COUNTRY));
        
        Assert.assertEquals(UserMetaData.FALSE, u.getMetadataValue("receiveEmail"));
        Assert.assertEquals(UserMetaData.TRUE, u.getMetadataValue("otherBool"));
        
        List<UserMetaData> list = getMetaObject(mv);

        // anything but UserMetaData.TRUE is considered false
        Assert.assertFalse(UserMetaData.TRUE.equals(getUserMetaData(list, "receiveEmail").getValue()));
        Assert.assertTrue(UserMetaData.TRUE.equals(getUserMetaData(list, "otherBool").getValue()));

        Assert.assertEquals((int) userId, Integer.parseInt((String) mv.getModel().get(UserProfileController.USER_ID)));
        Assert.assertEquals(UserProfileController.USER_INDICATOR, (String) mv.getModel().get(UserProfileController.EDIT_AS));
    }

    @Test
    public void testBadEmail() throws Exception {
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "billy@bob.com.au";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(au.com.gaiaresources.bdrs.util.StringUtils.generateRandomString(10, 50), emailAddr);
        userDAO.createUser("emailuser", "charles", "carey", emailAddr, encodedPassword, registrationKey, new String[] { "ROLE_USER" });

        request.setMethod("POST");
        request.setRequestURI("/user/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, emailAddr);
        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        ModelAndView mv = this.handle(request, response);

        // the MAV object should contain some fields ready for displaying to the user...
        Assert.assertEquals("Billy", mv.getModel().get(UserProfileController.FIRST_NAME));
        Assert.assertEquals("Bob", mv.getModel().get(UserProfileController.LAST_NAME));
        // email field is now blank.
        Assert.assertEquals("", mv.getModel().get(UserProfileController.EMAIL_ADDR));
        Assert.assertEquals("testuser", mv.getModel().get(UserProfileController.USER_NAME));

        Map<String, String> metaMap = getMetaValueMap(mv);
        Assert.assertEquals("SA", metaMap.get(Metadata.STATE));
        Assert.assertEquals("123123123", metaMap.get(Metadata.TELEPHONE));

        assertUserMetaDataList(mv);

        // Ideally we also want to validate the return message but I don't know how that s tuff works yet

        Assert.assertEquals((int) userId, Integer.parseInt((String) mv.getModel().get(UserProfileController.USER_ID)));
        Assert.assertEquals(UserProfileController.USER_INDICATOR, (String) mv.getModel().get(UserProfileController.EDIT_AS));
    }

    @Test
    public void testInvalidPassword() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/user/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "testemail@testprovider.com.au");
        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        ModelAndView mv = this.handle(request, response);

        // the MAV object should contain some fields ready for displaying to the user...
        Assert.assertEquals("Billy", mv.getModel().get(UserProfileController.FIRST_NAME));
        Assert.assertEquals("Bob", mv.getModel().get(UserProfileController.LAST_NAME));
        // email field is now blank.
        Assert.assertEquals("testemail@testprovider.com.au", mv.getModel().get(UserProfileController.EMAIL_ADDR));
        Assert.assertEquals("testuser", mv.getModel().get(UserProfileController.USER_NAME));

        Map<String, String> metaMap = getMetaValueMap(mv);
        Assert.assertEquals("SA", metaMap.get(Metadata.STATE));
        Assert.assertEquals("123123123", metaMap.get(Metadata.TELEPHONE));

        assertUserMetaDataList(mv);

        // Ideally we also want to validate the return message but I don't know how that stuff works yet
        Assert.assertEquals((int) userId, Integer.parseInt((String) mv.getModel().get(UserProfileController.USER_ID)));
        Assert.assertEquals(UserProfileController.USER_INDICATOR, (String) mv.getModel().get(UserProfileController.EDIT_AS));
    }

    @Test(expected = Exception.class)
    public void testAdminFailureWithNoId() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/admin/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");

        // this should have no effect
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password...
        request.setParameter(UserProfileController.PASSWORD, "aardvark");

        // Check the return contents of the modelandview...
        this.handle(request, response);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAdminPost() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/admin/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");
        request.setParameter(UserProfileController.USER_ACTIVE, "active");

        // this should have no effect
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password...
        request.setParameter(UserProfileController.PASSWORD, "aardvark");

        // use the ID of the test user
        request.setParameter(UserProfileController.USER_ID, userId.toString());
        
        request.setParameter(Role.POWERUSER, "on");
        request.setParameter(Role.USER, "on");

        // Check the return contents of the modelandview...
        ModelAndView mv = this.handle(request, response);

        // the MAV object should contain some fields ready for displaying to the user...
        Assert.assertEquals("Billy", mv.getModel().get(UserProfileController.FIRST_NAME));
        Assert.assertEquals("Bob", mv.getModel().get(UserProfileController.LAST_NAME));
        Assert.assertEquals("billy@bob.com.au", mv.getModel().get(UserProfileController.EMAIL_ADDR));
        Assert.assertEquals("testuser", mv.getModel().get(UserProfileController.USER_NAME));
        Assert.assertEquals(true, mv.getModel().get(UserProfileController.USER_ACTIVE));

        Map<String, String> metaMap = getMetaValueMap(mv);
        Assert.assertEquals("SA", metaMap.get(Metadata.STATE));
        Assert.assertEquals("123123123", metaMap.get(Metadata.TELEPHONE));

        assertUserMetaDataList(mv);

        // This causes a problem since the user object is never written to the DB, when you evict it
        // and try to retrieve the item you get a null. Retrieving the item without evicting it
        // causes the current instance of that object to be returned, i.e. it hasn't been written
        // to the DB.
        //User userToEvict = userDAO.getUser("usertest");
        //userDAO.getSessionFactory().getCurrentSession().evict(userToEvict);

        User u = userDAO.getUser("testuser");

        Assert.assertNotNull(u);
        Assert.assertEquals("Billy", u.getFirstName());
        Assert.assertEquals("Bob", u.getLastName());
        Assert.assertEquals("billy@bob.com.au", u.getEmailAddress());
        Assert.assertEquals("testuser", u.getName());
        Assert.assertEquals(true, u.isActive());

        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        Assert.assertEquals(passwordEncoder.encodePassword("aardvark", null), u.getPassword());

        Assert.assertEquals("SA", u.getMetadataValue(Metadata.STATE));
        Assert.assertEquals("123123123", u.getMetadataValue(Metadata.TELEPHONE));

        // returns empty string if key does not exist...
        Assert.assertEquals("", u.getMetadataValue(Metadata.CLIMATEWATCH_USERNAME));
        Assert.assertEquals("", u.getMetadataValue(Metadata.SCHOOL_NAME_KEY));
        Assert.assertEquals("", u.getMetadataValue(Metadata.SCHOOL_SUBURB_KEY));
        Assert.assertEquals("", u.getMetadataValue(Metadata.COUNTRY));
        
        Assert.assertEquals(true, mv.getModel().get(UserProfileController.USER_ACTIVE));
        Assert.assertEquals((int) userId, Integer.parseInt((String) mv.getModel().get(UserProfileController.USER_ID)));
        Assert.assertEquals(UserProfileController.ADMIN_INDICATOR, (String) mv.getModel().get(UserProfileController.EDIT_AS));
        
        Assert.assertEquals(2, u.getRoles().length);
        Assert.assertEquals(Role.POWERUSER, u.getRoles()[0]);
        Assert.assertEquals(Role.USER, u.getRoles()[1]);
        
        ArrayList<String> allowedRoles = (ArrayList<String>)mv.getModel().get("allowedRoles");

        Assert.assertFalse(allowedRoles.contains(Role.ROOT));
        Assert.assertTrue(allowedRoles.contains(Role.ADMIN));
        Assert.assertTrue(allowedRoles.contains(Role.SUPERVISOR));
        Assert.assertTrue(allowedRoles.contains(Role.POWERUSER));
        Assert.assertTrue(allowedRoles.contains(Role.USER));
        
        ArrayList<String> assignedRoles = (ArrayList<String>)mv.getModel().get("assignedRoles");
        Assert.assertFalse(assignedRoles.contains(Role.ROOT));
        Assert.assertFalse(assignedRoles.contains(Role.ADMIN));
        Assert.assertFalse(assignedRoles.contains(Role.SUPERVISOR));
        Assert.assertTrue(assignedRoles.contains(Role.POWERUSER));
        Assert.assertTrue(assignedRoles.contains(Role.USER));
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testCannotRemoveUserRole() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/admin/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");

        // this should have no effect
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password...
        request.setParameter(UserProfileController.PASSWORD, "aardvark");

        // use the ID of the test user
        request.setParameter(UserProfileController.USER_ID, userId.toString());
        
        request.setParameter(Role.POWERUSER, "on");
        //request.setParameter(Role.USER, "on");

        // Check the return contents of the modelandview...
        ModelAndView mv = this.handle(request, response);
        
        ArrayList<String> assignedRoles = (ArrayList<String>)mv.getModel().get("assignedRoles");
        Assert.assertFalse(assignedRoles.contains(Role.ROOT));
        Assert.assertFalse(assignedRoles.contains(Role.ADMIN));
        Assert.assertFalse(assignedRoles.contains(Role.SUPERVISOR));
        Assert.assertTrue(assignedRoles.contains(Role.POWERUSER));
        Assert.assertTrue(assignedRoles.contains(Role.USER));
    }
    
    @SuppressWarnings("unchecked")
    @Test(expected = Exception.class)
    public void testAdminPostInvalidRole() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/admin/profile.htm");

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");

        // this should have no effect
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password...
        request.setParameter(UserProfileController.PASSWORD, "aardvark");

        // use the ID of the test user
        request.setParameter(UserProfileController.USER_ID, userId.toString());
        
        // Assigning a role higher than the editing user
        request.setParameter(Role.ROOT, "on");

        // Check the return contents of the modelandview...
        this.handle(request, response);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testAdminProfileGet() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/admin/profile.htm");
        request.setParameter(UserProfileController.USER_ID, userId.toString());

        ModelAndView mv = handle(request, response);

        Assert.assertEquals("Abigail", mv.getModel().get(UserProfileController.FIRST_NAME));
        Assert.assertEquals("Ambrose", mv.getModel().get(UserProfileController.LAST_NAME));
        Assert.assertEquals("abigail.ambrose@example.com", mv.getModel().get(UserProfileController.EMAIL_ADDR));
        Assert.assertEquals("testuser", mv.getModel().get(UserProfileController.USER_NAME));

        Map<String, String> metaMap = getMetaValueMap(mv);
        Assert.assertEquals("WA", metaMap.get(Metadata.STATE));
        Assert.assertEquals("", metaMap.get(Metadata.TELEPHONE));

        assertUserMetaDataList(mv);

        Assert.assertEquals((int) userId, Integer.parseInt((String) mv.getModel().get(UserProfileController.USER_ID)));
        Assert.assertEquals(UserProfileController.ADMIN_INDICATOR, (String) mv.getModel().get(UserProfileController.EDIT_AS));
        
        ArrayList<String> allowedRoles = (ArrayList<String>)mv.getModel().get("allowedRoles");

        Assert.assertFalse(allowedRoles.contains(Role.ROOT));
        Assert.assertTrue(allowedRoles.contains(Role.ADMIN));
        Assert.assertTrue(allowedRoles.contains(Role.SUPERVISOR));
        Assert.assertTrue(allowedRoles.contains(Role.POWERUSER));
        Assert.assertTrue(allowedRoles.contains(Role.USER));
        
        ArrayList<String> assignedRoles = (ArrayList<String>)mv.getModel().get("assignedRoles");
        Assert.assertFalse(assignedRoles.contains(Role.ROOT));
        Assert.assertFalse(assignedRoles.contains(Role.ADMIN));
        Assert.assertFalse(assignedRoles.contains(Role.SUPERVISOR));
        Assert.assertFalse(assignedRoles.contains(Role.POWERUSER));
        Assert.assertTrue(assignedRoles.contains(Role.USER));
    }
    
    // we expect an exception to be thrown because an ADMIN is trying to edit the details of the ROOT,
    // note the ROOT is also an ADMIN
    @Test(expected = Exception.class)
    public void testAdminEditHigherRoleUserFailure() throws Exception {
        User u = userDAO.getUser("testuser");
        u.setRoles(new String[] { Role.ROOT, Role.ADMIN });
        userDAO.updateUser(u);
        
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/admin/profile.htm");
        request.setParameter(UserProfileController.USER_ID, userId.toString());

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");
        
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password...
        request.setParameter(UserProfileController.PASSWORD, "aardvark");

        // Assigning a role higher than the editing user
        // request.setParameter(Role.ROOT, "on");

        // Check the return contents of the modelandview...
        this.handle(request, response);
    }
    
 // we expect an exception to be thrown because an ADMIN is trying to edit the details of the ROOT,
    // note the ROOT is also an ADMIN
    @Test
    public void testAdminEditSelfAsAdmin() throws Exception {
        //User u = userDAO.getUser("testuser");
        //u.setRoles(new String[] { Role.ADMIN });
        //userDAO.updateUser(u);
        
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/admin/profile.htm");
        request.setParameter(UserProfileController.USER_ID, userDAO.getUser("admin").getId().toString());

        request.setParameter(UserProfileController.FIRST_NAME, "Billy");
        request.setParameter(UserProfileController.LAST_NAME, "Bob");
        request.setParameter(UserProfileController.EMAIL_ADDR, "billy@bob.com.au");
        
        request.setParameter(UserProfileController.USER_NAME, "usertest");

        request.setParameter(Metadata.TELEPHONE, "123123123");
        request.setParameter(Metadata.STATE, "SA");

        // can set all this stuff but it won't have an effect...
        request.setParameter(Metadata.CLIMATEWATCH_USERNAME, "aaa");
        request.setParameter(Metadata.SCHOOL_NAME_KEY, "bbb");
        request.setParameter(Metadata.SCHOOL_SUBURB_KEY, "ccc");

        // this item doesn't already exist in the metadata
        request.setParameter(Metadata.COUNTRY, "eee");

        // change password... 6 characters should pass
        request.setParameter(UserProfileController.PASSWORD, "aardva");
        
        // Assigning a role higher than the editing user
        // request.setParameter(Role.ROOT, "on");

        // Check the return contents of the modelandview...
        this.handle(request, response);
    }
    

    // this stuff is so i can get away with minimal refactoring of tests
    @SuppressWarnings("unchecked")
    private List<UserMetaData> getMetaObject(ModelAndView mv) {
        return (List<UserMetaData>)mv.getModel().get("metaList");
    }
    
    private UserMetaData getUserMetaData(List<UserMetaData> list, String key) {
        for (UserMetaData umd : list) {
            if (umd.getKey().equals(key)) {
                return umd;
            }
        }
        return null;
    }
    
    private Map<String, String> getMetaValueMap(ModelAndView mv) {
        List<UserMetaData> metaList = getMetaObject(mv);
        Map<String, String> metaMap = new HashMap<String, String>();
        for (UserMetaData umd : metaList) {
            metaMap.put(umd.getKey(), umd.getValue());
        }
        return metaMap;
    }
    
    private Map<String, String> getMetaNameMap(ModelAndView mv) {
        List<UserMetaData> metaList = getMetaObject(mv);
        Map<String, String> metaMap = new HashMap<String, String>();
        for (UserMetaData umd : metaList) {
            metaMap.put(umd.getKey(), umd.getDisplayName());
        }
        return metaMap;
    }
    
    private void assertUserMetaDataList(ModelAndView mv)
    {
        List<UserMetaData> list = getMetaObject(mv);
        
        UserMetaData telephone = getUserMetaData(list, Metadata.TELEPHONE);
        
        Assert.assertEquals("telephone", telephone.getKey());
        Assert.assertEquals("Telephone", telephone.getDisplayName());
        Assert.assertEquals("required", telephone.getValidation());
        Assert.assertEquals(UserMetaDataType.String, telephone.getType());
        
        UserMetaData state = getUserMetaData(list, Metadata.STATE);
        
        Assert.assertEquals("state", state.getKey());
        Assert.assertEquals("State", state.getDisplayName());
        Assert.assertEquals("required, maxlength(30)", state.getValidation());
        Assert.assertEquals(UserMetaDataType.String, state.getType());
        
        UserMetaData email = getUserMetaData(list, "receiveEmail");
        
        Assert.assertEquals("receiveEmail", email.getKey());
        Assert.assertEquals("Receive promotional emails", email.getDisplayName());
        Assert.assertEquals(null, email.getValidation());
        Assert.assertEquals(UserMetaDataType.Boolean, email.getType());
        
        UserMetaData otherBool = getUserMetaData(list, "otherBool");
        
        Assert.assertEquals("otherBool", otherBool.getKey());
        Assert.assertEquals("Some other bool", otherBool.getDisplayName());
        Assert.assertEquals(null, otherBool.getValidation());
        Assert.assertEquals(UserMetaDataType.Boolean, otherBool.getType());
    }
}
