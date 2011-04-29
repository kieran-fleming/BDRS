package au.com.gaiaresources.bdrs.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

public class TestAbstractController {
    @Test
    public void testRedirectDefaults() {
        assertEquals("redirect:/home.htm", new AbstractControllerImpl().getRedirectHome());
        assertEquals("redirect:/secure/home.htm", new AbstractControllerImpl().getRedirectSecureHome());
        assertEquals("redirect:/admin/home.htm", new AbstractControllerImpl().getRedirectAdminHome());
    }
    
    @Test
    public void testGetControllerMapping() {
        assertEquals("mapping.htm", new AbstractControllerImpl().getControllerRequestMapping(AbstractControllerImpl.class));
    }
    
    @RequestMapping("mapping.htm")
    @Ignore
    private static class AbstractControllerImpl extends AbstractController {
        public AbstractControllerImpl() { }
    }
}
