package au.com.gaiaresources.bdrs.controller.mobile;


import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;

public class MobileApplicationControllerTest extends AbstractControllerTest {
	
	@Before
	public void setup(){
		
	}
	
	@Test
	public void getApplicationTest() throws Exception{
		
        request.setMethod("GET");
        request.setRequestURI(MobileApplicationController.APPLICATION_URL);
        handle(request, response);
        String contentDisposition = (String)response.getHeader("Content-Disposition");
        Assert.assertEquals("attachment;filename=bdrs-mobile.apk", contentDisposition);
       
	}

}
