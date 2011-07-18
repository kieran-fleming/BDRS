package au.com.gaiaresources.bdrs.controller;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapability;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapabilityDAO;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDevice;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDeviceDAO;
import au.com.gaiaresources.bdrs.security.Role;

public class HomePageControllerTest extends AbstractControllerTest {
	
	@Autowired
	private BDRSWurflDeviceDAO deviceDAO;
	
	@Autowired
	private BDRSWurflCapabilityDAO capabilityDAO;

	
	
	@Before
	public void setup(){
		//Note that the device ids do not match a real life ua string
		BDRSWurflDevice device1 = deviceDAO.createDevice("apple_ipad_ver1_sub321", "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B405 Safari/531.21.10");
		BDRSWurflDevice device2 = deviceDAO.createDevice("apple_iphone_ver4_1", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5");
		BDRSWurflDevice device3 = deviceDAO.createDevice("htc_desire_ver1", "Mozilla/5.0 (Linux; U; Android 2.1-update1; fr-fr; desire_A8181 Build/ERE27) AppleWebKit/530.17 (KHTML, like Gecko) Version/4.0 Mobile Safari/530.17");
		
		BDRSWurflCapability capability = capabilityDAO.create("product_info", "is_wireless_device", "true");
		
		device1.getCapabilities().add(capability);
		device2.getCapabilities().add(capability);
		device3.getCapabilities().add(capability);
		
	}
    
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
        request.addHeader("user-agent", "Mozilla/5.0 (iPad; U; CPU OS 3_2_1 like Mac OS X; en-us) AppleWebKit/531.21.10 (KHTML, like Gecko) Version/4.0.4 Mobile/7B405 Safari/531.21.10");
	    testMobile(request);
    }
    
    @Test
    public void testRenderIPhone() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/home.htm");
        request.addHeader("user-agent", "Mozilla/5.0 (iPhone; U; CPU iPhone OS 4_1 like Mac OS X; en-us) AppleWebKit/532.9 (KHTML, like Gecko) Version/4.0.5");
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
	    Assert.assertTrue("View is not redirect view", mv.getView() instanceof RedirectView);
    }
}