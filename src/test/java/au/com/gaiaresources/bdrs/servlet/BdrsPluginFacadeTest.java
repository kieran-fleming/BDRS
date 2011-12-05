package au.com.gaiaresources.bdrs.servlet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.content.ContentService;


public class BdrsPluginFacadeTest extends AbstractControllerTest {

    @Autowired
    private ContentService contentService;
    
    private static final String TEST_CONTENT_KEY = "public/home";
    
    private User currentUser;
    
    @Before
    public void setup() {
        contentService.saveContent(getRequestContext().getHibernate(), 
                                   this.defaultPortal, TEST_CONTENT_KEY, "${portal.getName()} ${bdrsContextPath} ${bdrsApplicationUrl} ${currentUser.getFirstName()} ${currentUser.getLastName()}");
        
        currentUser = new User();
        currentUser.setFirstName("jimmy");
        currentUser.setLastName("jojo");
    }
    
    @Test
    public void testGetContentTokenReplace() {
        BdrsPluginFacade facade = new BdrsPluginFacade(sesh, defaultPortal, "http://www.mybdrs.com/contextpath/home.htm", currentUser);
        String result = facade.getContent(TEST_CONTENT_KEY);
        Assert.assertEquals("expect test to be replaced correctly", defaultPortal.getName() + " /contextpath" + " http://www.mybdrs.com/contextpath/portal/" + defaultPortal.getId() + " jimmy jojo", result);
    }
    
    @Test
    public void testGetPreference() {
        // get one of the default preferences - see preference.json
        BdrsPluginFacade facade = new BdrsPluginFacade(sesh, defaultPortal, "http://www.mybdrs.com/contextpath/home.htm", currentUser);
        String value = facade.getPreferenceValue("taxon.showScientificName");
        Assert.assertEquals("String does not match expected value", "true", value);
    }
    
    @Test
    public void testGetPreferenceBooleanValue() {
        // get one of the default preferences - see preference.json
        BdrsPluginFacade facade = new BdrsPluginFacade(sesh, defaultPortal, "http://www.mybdrs.com/contextpath/home.htm", currentUser);
        boolean value = facade.getPreferenceBooleanValue("taxon.showScientificName");
        Assert.assertEquals("boolean does not match expected value", true, value);
    }
}
