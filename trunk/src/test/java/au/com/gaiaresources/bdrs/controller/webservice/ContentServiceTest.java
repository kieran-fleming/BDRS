package au.com.gaiaresources.bdrs.controller.webservice;

import junit.framework.Assert;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockPageContext;
import org.springframework.mock.web.MockServletContext;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.jsp.tag.GetContentTag;

public class ContentServiceTest extends AbstractControllerTest {

    @Autowired
    private ContentDAO contentDAO;
    @Autowired
    private UserDAO userDAO;
    
    final String velocityIn = "<p>${currentUserFirstName} ${currentUserLastName} ${portalName}</p>";   // tests velocity templating...
    final String velocityOut = "<p>adminfirst adminlast Biological Data Recording System</p>";

    // test " " escape code...
    final String testHtml = "<h2>markItUp! Universal markup editor</h2>"
            + "<p><img src=\"../markitup/preview/picture.png\" alt=\"markItUp! logo\" /></p>"
            + "<p><strong>markItUp!</strong> is a javascript over jQuery plug-in which allow you to turn any textarea in a markup editor.</p>"
            + velocityIn;
    
    final String testKey = "page/testcontent";

    @Before
    public void setup() throws Exception {
        
        User admin = userDAO.getUser("admin");
        admin.setFirstName("adminfirst");
        admin.setLastName("adminlast");
        userDAO.updateUser(admin);
        
        login("admin", "password", new String[] { Role.ADMIN });
        saveContent(testKey, testHtml);
    }

    @Test
    public void testLoad() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/webservice/content/loadContent.htm");
        request.setParameter(ContentService.KEY, testKey);

        ModelAndView mv = handle(request, response);

        Assert.assertEquals("application/json", response.getContentType());
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertTrue(json.containsKey("content"));
        Assert.assertEquals(testHtml, json.getString("content"));
    }

    @Test
    public void testSave() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/webservice/content/saveContent.htm");
        request.setParameter(ContentService.KEY, "anotherKey");
        request.setParameter(ContentService.VALUE, testHtml);

        handle(request, response);

        Assert.assertEquals("application/json", response.getContentType());
        Assert.assertEquals(testHtml, getContent("anotherKey"));
    }
    
    private GetContentTag getContentTag;
    private MockServletContext mockServletContext;
    private MockPageContext mockPageContext;
    private WebApplicationContext mockWebApplicationContext;
    
    @Test
    public void testContentTag() {
        
    }

    private String getContent(String key) {
        Content item = contentDAO.getContent(key);
        if (item == null) {
            return new String("");
        }
        return item.getValue();
    }

    private void saveContent(String key, String value) {
        // the DAO does the 'does exist' checking for us...
        contentDAO.saveContent(key, value);
    }
}
