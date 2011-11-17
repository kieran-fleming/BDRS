/**
 * 
 */
package au.com.gaiaresources.bdrs.model.menu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import au.com.gaiaresources.bdrs.model.menu.MenuItem;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.FileUtils;

/**
 * @author stephanie
 *
 */
public class JsonMenuDAOImplTest {
    private static final String MISSING_CONFIG = "missing.config";
    private static final String EMPTY_CONFIG = "empty_file.config";
    private static final String MALFORMED_CONFIG = "malformed.config";
    
    private JsonMenuDAOImpl menuDAO;
    private User user = null;
    
    /**
     * @throws java.lang.Exception
     */
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @AfterClass
    public static void tearDownAfterClass() throws Exception {
    }

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
        menuDAO = new JsonMenuDAOImpl();
    }

    /**
     * @throws java.lang.Exception
     */
    @After
    public void tearDown() throws Exception {
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.model.menu.impl.JsonMenuDAOImpl#JsonMenuDAOImpl()}.
     * @throws IOException 
     * @throws JSONException 
     */
    @Test
    public final void testJsonMenuDAOImpl() throws JSONException, IOException {
        // test that the constructor initializes the menus
        JSONArray menuArray = menuDAO.array;
        Assert.assertTrue("No menu items were initialized in constructor", menuArray.size() > 0);
        
        // read the config file
        InputStream configStream = MenuItem.class.getResourceAsStream(JsonMenuDAOImpl.CONFIG_NAME);
        if (configStream == null) {
            System.out.println("Error finding resource: "+MenuItem.class.getResource(JsonMenuDAOImpl.CONFIG_NAME));
        }
        JSONArray expectedArray = (JSONArray) FileUtils.readJsonStream(configStream);
        Assert.assertEquals("Menu from constructor does not match file", expectedArray, menuArray);
        configStream.close();
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.model.menu.impl.JsonMenuDAOImpl#getUserMenus(au.com.gaiaresources.bdrs.model.user.User)}.
     */
    @Test
    public final void testGetUserMenus() {
        // test anonymous user access
        List<MenuItem> anonMenu = menuDAO.getUserMenus(null);
        Assert.assertNotNull("Anonymous menu should not be null.", anonMenu);
        Assert.assertNotSame("Anonymous menu should not contain all items from the menu config.", 
                             menuDAO.array.size(), anonMenu.size());
        
        // test role menu access
        List<MenuItem> userMenu = null;
        for (String role : Role.getAllRoles()) {
            user = new User();
            user.setRoles(new String[]{role});
            // test that this menu is not the same as the anonymous one or the prior role
            List<MenuItem> tmpMenu = menuDAO.getUserMenus(user);
            Assert.assertNotSame("Anonymous menu and "+role+" menu should not be the same.", anonMenu, tmpMenu);
            Assert.assertNotSame(role+" menu should not be the same as prior role menu.", userMenu, tmpMenu);
            userMenu = tmpMenu;
            Assert.assertNotSame(role+" menu should not contain all items from the menu config.", 
                                 menuDAO.array.size(), userMenu.size());
        }
    }
    
    @Test
    public final void testMissingMenuFile() {
        InputStream configStream = JsonMenuDAOImplTest.class.getResourceAsStream(JsonMenuDAOImplTest.MISSING_CONFIG);
        menuDAO.readMenuFromStream(configStream);
        Assert.assertNull(menuDAO.array);
        Assert.assertTrue(menuDAO.getUserMenus(null).isEmpty());
    }
    
    @Test
    public final void testEmptyMenuFile() {
        InputStream configStream = JsonMenuDAOImplTest.class.getResourceAsStream(JsonMenuDAOImplTest.EMPTY_CONFIG);
        menuDAO.readMenuFromStream(configStream);
        Assert.assertNull(menuDAO.array);
        Assert.assertTrue(menuDAO.getUserMenus(null).isEmpty());
    }

    @Test
    public final void testMalformedMenuFile() {
        InputStream configStream = JsonMenuDAOImplTest.class.getResourceAsStream(JsonMenuDAOImplTest.MALFORMED_CONFIG);
        menuDAO.readMenuFromStream(configStream);
        Assert.assertNull(menuDAO.array);
        Assert.assertTrue(menuDAO.getUserMenus(null).isEmpty());
    }
}
