package au.com.gaiaresources.bdrs.controller;

import static org.junit.Assert.assertEquals;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

public class TestAbstractController {
    @Test
    public void testRedirectDefaults() {
        assertEquals(AbstractController.REDIRECT_HOME, new AbstractControllerImpl().getRedirectHome());
        assertEquals(AbstractController.REDIRECT_SECURE_HOME, new AbstractControllerImpl().getRedirectSecureHome());
        assertEquals(AbstractController.REDIRECT_ADMIN_HOME, new AbstractControllerImpl().getRedirectAdminHome());
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
