package au.com.gaiaresources.bdrs.servlet.filter;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

public class BdrsAuthenticationFilterTest {

    private BdrsAuthenticationFilter filter;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;
    
    private static final String CONTEXT_PATH = "/BDRS";
    
    @Before
    public void setup() {
        filter = new BdrsAuthenticationFilter();
        request = new MockHttpServletRequest();
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void testNoPortal() {
        request.setRequestURI(CONTEXT_PATH + "/j_spring_security_check");
        Assert.assertTrue("expect requires authentication = true", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testWithPortal() {
        request.setRequestURI(CONTEXT_PATH + "/portal/2/j_spring_security_check");
        Assert.assertTrue("expect requires authentication = true", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testNonAuthenticationUrl() {
        request.setRequestURI(CONTEXT_PATH + "/portal/2/home.htm");
        Assert.assertFalse("expect requires authentication = false", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testNoPortalWithContextPath() {
        request.setRequestURI(CONTEXT_PATH + "/j_spring_security_check");
        request.setContextPath(CONTEXT_PATH);
        Assert.assertTrue("expect requires authentication = true", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testWithPortalWithContextPath() {
        request.setRequestURI(CONTEXT_PATH + "/portal/2/j_spring_security_check");
        request.setContextPath(CONTEXT_PATH);
        Assert.assertTrue("expect requires authentication = true", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testNonAuthenticationUrlWithContextPath() {
        request.setRequestURI(CONTEXT_PATH + "/portal/2/home.htm");
        request.setContextPath(CONTEXT_PATH);
        Assert.assertFalse("expect requires authentication = false", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testChangeAuthenticationUrlWithoutContextPath() {
        request.setRequestURI(CONTEXT_PATH + "/portal/2/login");
        request.setContextPath(CONTEXT_PATH);
        filter.setFilterProcessesUrl("/login");
        Assert.assertTrue("expect requires authentication = true", filter.requiresAuthentication(request, response));
    }
    
    @Test
    public void testChangeAuthenticationUrlWithContextPath() {
        request.setRequestURI(CONTEXT_PATH + "/portal/2/login");
        request.setContextPath(CONTEXT_PATH);
        filter.setFilterProcessesUrl("/login");
        Assert.assertTrue("expect requires authentication = true", filter.requiresAuthentication(request, response));
    }
}
