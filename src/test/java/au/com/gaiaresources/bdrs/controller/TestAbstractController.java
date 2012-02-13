package au.com.gaiaresources.bdrs.controller;

import static org.junit.Assert.assertEquals;

import javax.annotation.security.RolesAllowed;

import org.junit.Ignore;
import org.junit.Test;
import org.springframework.web.bind.annotation.RequestMapping;

import au.com.gaiaresources.bdrs.security.Role;

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
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping("mapping.htm")
    @Ignore
    private static class AbstractControllerImpl extends AbstractController {
        public AbstractControllerImpl() { }
    }
}
