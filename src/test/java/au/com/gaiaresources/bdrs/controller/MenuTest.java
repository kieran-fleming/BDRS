package au.com.gaiaresources.bdrs.controller;


import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.model.menu.MenuItem;
import au.com.gaiaresources.bdrs.model.menu.MenuDAO;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * Tests for data-driven menus.
 * @author stephanie
 */
public class MenuTest extends AbstractGridControllerTest {
    
    @Autowired
    private MenuDAO menuItemDAO;
    
    @Test
    public void testMenuItemAccess() throws Exception {
        List<MenuItem> menu = menuItemDAO.getUserMenus(getRequestContext().getUser());
        getRequestContext().setMenu(menu);
        
        request.setMethod("GET");
        request.setRequestURI(HomePageController.HOME_URL);
        request.addHeader("user-agent", "Mozilla/5.0 (Linux; U; Android 2.1-update1; fr-fr; desire_A8181 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "home");
        // get the rendered menu from the request
        menu = getRequestContext().getMenu();
        
        login("admin", "password", new String[]{ Role.ADMIN });
        List<MenuItem> adminMenu = menuItemDAO.getUserMenus(getRequestContext().getUser());
        getRequestContext().setMenu(adminMenu);

        request.setMethod("GET");
        request.setRequestURI(HomePageController.HOME_URL);
        request.addHeader("user-agent", "Mozilla/5.0 (Linux; U; Android 2.1-update1; fr-fr; desire_A8181 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        
        mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "home");
        adminMenu = getRequestContext().getMenu();
        
        // the admin and anonymous menus should not be the same
        Assert.assertNotSame("Admin and Anonymous menus are the same!", adminMenu, menu);
    }
    
    @Test
    public void testMenuItemContents() throws Exception {
        List<MenuItem> menu = menuItemDAO.getUserMenus(getRequestContext().getUser());
        getRequestContext().setMenu(menu);
        
        request.setMethod("GET");
        request.setRequestURI(HomePageController.HOME_URL);
        request.addHeader("user-agent", "Mozilla/5.0 (Linux; U; Android 2.1-update1; fr-fr; desire_A8181 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "home");
        // get the rendered menu from the request
        menu = getRequestContext().getMenu();
        
        login("admin", "password", new String[]{ Role.ADMIN });
        List<MenuItem> adminMenu = menuItemDAO.getUserMenus(getRequestContext().getUser());
        getRequestContext().setMenu(adminMenu);

        request.setMethod("GET");
        request.setRequestURI(HomePageController.HOME_URL);
        request.addHeader("user-agent", "Mozilla/5.0 (Linux; U; Android 2.1-update1; fr-fr; desire_A8181 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        
        mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "home");
        adminMenu = getRequestContext().getMenu();
        
        // the admin and anonymous menus should not be the same
        Assert.assertNotSame("Admin and Anonymous menus are the same!", adminMenu, menu);
    }
}
