package au.com.gaiaresources.bdrs.servlet;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemePage;

public class InterceptorTest extends AbstractControllerTest {

    @Autowired
    HandlerInterceptor interceptor;
    @Autowired
    ThemeDAO themeDAO;
    
    Theme theme;
    ThemePage page;
    
    @Before
    public void setup() {
        theme = new Theme();
        theme.setName("my test theme");
        theme.setThemeFileUUID("dummy uuid");
        theme = themeDAO.save(theme);
        
        page = new ThemePage();
        page.setTitle("test page");
        page.setDescription("test description");
        page.setTheme(theme);
        page.setKey("view name");
        page = themeDAO.save(page);
        
        getRequestContext().setTheme(theme);
    }
    
    @Test
    public void testThemePage() throws Exception {
        ModelAndView mv = new ModelAndView(page.getKey());
        
        // third arg is the handler which isn't used internally.
        // may need to refactor this test if this fact changes.
        interceptor.postHandle(request, response, null, mv);
        
        Assert.assertEquals("check title", page.getTitle(), (String)mv.getModel().get(Interceptor.PARAM_PAGE_TITLE));
        Assert.assertEquals("check description", page.getDescription(), (String)mv.getModel().get(Interceptor.PARAM_PAGE_DESCRIPTION));
    }
}
