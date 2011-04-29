package au.com.gaiaresources.bdrs.controller;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.AbstractUrlBasedView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.security.Role;

public class HomePageControllerTest extends AbstractControllerTest {
    
    @Test
    public void testRenderIE9() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (Windows; U; MSIE 9.0; WIndows NT 9.0; en-US))");
        testDesktop(request);
    }

    @Test
    public void testRenderIE8() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; Media Center PC 4.0; SLCC1; .NET CLR 3.0.04320)");
        testDesktop(request);
    }
    
    @Test
    public void testRenderIE7() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (Windows; U; MSIE 7.0; Windows NT 6.0; en-US)");
        testDesktop(request);
    }
    
    @Test
    public void testRenderFirefox() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.8.1.19) Gecko/20081202 Firefox (Debian-2.0.0.19-0etch1)");
        testDesktop(request);
    }

    @Test
    public void testRenderChrome() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (X11; U; Linux x86_64; en-US) AppleWebKit/540.0 (KHTML, like Gecko) Ubuntu/10.10 Chrome/9.1.0.0 Safari/540.0");
        testDesktop(request);
    }
    
    @Test
	public void testRenderIPad() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (iPad; U; CPU OS 3_2 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B334b Safari/531.21.10");
	    testMobile(request);
    }
    
    @Test
    public void testRenderIPhone() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 3_0 like Mac OS X; en-us) AppleWebKit/420.1 (KHTML, like Gecko) Version/3.0 Mobile/1A542a Safari/419.3");
	    testMobile(request);
    }
    
	@Test
	public void testRenderAndroid() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (Linux; U; Android 2.1-update1; fr-fr; desire_A8181 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
        testMobile(request);
    }
	
	private void testDesktop(MockHttpServletRequest request) throws Exception {
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "home");
        
        login("admin", "password", new String[] { Role.ADMIN });
        mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "home");
        
        request.addParameter("signin", "true");
        mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "signin");
	}
	
	private void testMobile(MockHttpServletRequest request) throws Exception {
	    ModelAndView mv = handle(request, response);
	    ModelAndViewAssert.assertViewName(mv, "loginmobile");
	    
        login("admin", "password", new String[] { Role.ADMIN });
        mv = handle(request, response);
        
        Assert.assertTrue("View is not redirect view", mv.getView() instanceof RedirectView);
        Assert.assertEquals("Redirect url is not correct.", "/bdrs/mobile/home.htm", ((AbstractUrlBasedView) mv.getView()).getUrl());
    }
}