package au.com.gaiaresources.bdrs.controller.admin;

import java.util.Set;
import java.util.TreeSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class UserGroupControllerTest extends AbstractControllerTest {

    @Autowired
    GroupDAO groupDAO;
    
    @Autowired
    UserDAO userDAO;
    
    private static String[][] testnames = { { "aaa", "laaa", Role.ADMIN },
        { "bbb", "lbbb", Role.ADMIN },
        { "ccc", "lccc", Role.ADMIN },
        { "ddd", "lddd", Role.ADMIN } };
    
    Group one;
    Group two;
    Group three;
    Group four;
    Group five;
    
    User aaa;
    User bbb;
    User ccc;
    User ddd;
    
    @Before
    public void setup() throws Exception {
        one = groupDAO.createGroup("one");
        two = groupDAO.createGroup("two");
        three = groupDAO.createGroup("three");
        four = groupDAO.createGroup("four");
        five = groupDAO.createGroup("five");
        
        one.setDescription("one desc");
        two.setDescription("two desc");
        three.setDescription("three desc");
        four.setDescription("four desc");
        five.setDescription("five desc");
        
        one.getGroups().add(two);
        one.getGroups().add(three);
        
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
        
        aaa = userDAO.getUser("aaa");
        bbb = userDAO.getUser("bbb");
        ccc = userDAO.getUser("ccc");
        ddd = userDAO.getUser("ddd");
        
        one.getUsers().add(aaa);
        one.getUsers().add(bbb);
        
        login("admin", "password", new String[] { Role.ADMIN });
    }
    
    private String fakeEmail(String first, String last) {
        return first.toLowerCase() + "@" + last.toLowerCase() + ".com.au";
    }

    @Test
    public void testViewEditForm() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(UserGroupController.EDIT_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        
        ModelAndView mav = this.handle(request, response);
        
        Group g = (Group)mav.getModel().get(UserGroupController.GROUP_TO_EDIT);
        
        Assert.assertEquals(one.getId(), g.getId());
    }
    
    @Test
    public void testSave() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.EDIT_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        request.setParameter(UserGroupController.GROUP_NAME, "new name");
        request.setParameter(UserGroupController.GROUP_DESC, "new desc");
        
        ModelAndView mav = this.handle(request, response);
        
        Group g = (Group)mav.getModel().get(UserGroupController.GROUP_TO_EDIT);
        Assert.assertEquals(one.getId(), g.getId());
        Assert.assertEquals("new name", g.getName());
        Assert.assertEquals("new desc", g.getDescription());
    }
    
    @Test 
    public void testCreate() throws Exception {
        Pattern p = Pattern.compile("groupId=(\\d*)");
        
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.CREATE_URL);
        
        ModelAndView mv = this.handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        String redirectUrl = redirect.getUrl();
        logger.debug(redirectUrl);
        Matcher m = p.matcher(redirectUrl);
        Assert.assertTrue(m.find());
        
        String matchText = m.group(0);
        
        Integer newGroupId = Integer.parseInt(matchText.substring("groupId=".length()));
        Group g = groupDAO.get(newGroupId);
        Assert.assertTrue(g != null);
    }
    
    @Test
    public void testDelete() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.DELETE_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        
        ModelAndView mv = this.handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(UserGroupController.LISTING_URL, redirect.getUrl());
    }
    
    private String createIdList(Integer...integers) {
        StringBuilder sb = new StringBuilder();
        for (Integer i : integers) {
            sb.append(i.toString());
            sb.append(",");
        }
        if (sb.length() > 0) {
            sb.delete(sb.length() - 1, sb.length());
        }
        return sb.toString();
    }
    
    private void assertRedirectOk(ModelAndView mv, Integer expectedGroupId) {
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(UserGroupController.EDIT_URL + "?groupId=" + expectedGroupId.toString(), redirect.getUrl());
    }
    
    @Test
    public void testAddUsers() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.ADD_USERS_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        
        String idList = createIdList(ccc.getId(), ddd.getId());
        request.setParameter(UserGroupController.USER_ID_LIST, idList);
        
        ModelAndView mv = this.handle(request, response);
        assertRedirectOk(mv, one.getId());
        Group g = groupDAO.get(one.getId());
        
        Assert.assertEquals(one.getId(), g.getId());
        Assert.assertEquals(4, g.getUsers().size());
    }
    
    @Test
    public void testRemoveUsers() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.REMOVE_USERS_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        
        String idList = createIdList(aaa.getId(), bbb.getId());
        request.setParameter(UserGroupController.USER_ID_LIST, idList);
        
        ModelAndView mv = this.handle(request, response);
        assertRedirectOk(mv, one.getId());
        Group g = groupDAO.get(one.getId());
        
        Assert.assertEquals(one.getId(), g.getId());
        Assert.assertEquals(0, g.getUsers().size());
    }
    
    @Test
    public void testAddGroups() throws Exception {        
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.ADD_GROUPS_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        
        String idList = createIdList(four.getId(), five.getId());
        request.setParameter(UserGroupController.GROUP_ID_LIST, idList);
        
        ModelAndView mv = this.handle(request, response);
        assertRedirectOk(mv, one.getId());
        Group g = groupDAO.get(one.getId());
        
        Assert.assertEquals(one.getId(), g.getId());
        Assert.assertEquals(4, g.getGroups().size());
    }
    
    @Test
    public void testRemoveGroups() throws Exception {
        request.setMethod("POST");
        request.setRequestURI(UserGroupController.REMOVE_GROUPS_URL);
        request.setParameter(UserGroupController.GROUP_ID, one.getId().toString());
        
        String idList = createIdList(two.getId(), three.getId());
        request.setParameter(UserGroupController.GROUP_ID_LIST, idList);
        
        ModelAndView mv = this.handle(request, response);
        assertRedirectOk(mv, one.getId());
        Group g = groupDAO.get(one.getId());
        
        Assert.assertEquals(one.getId(), g.getId());
        Assert.assertEquals(0, g.getGroups().size());
    }
}
