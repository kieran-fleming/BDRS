/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.mobile;

import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletOutputStream;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * @author timo
 *
 */
public class BdrsCoreOfflineControllerTest extends AbstractControllerTest {
	 @Test
	    public void testGetManifest() throws Exception {
	        login("admin", "password", new String[] { Role.ADMIN });

	        request.setRequestURI("/bdrs/mobile/mobile.manifest");
	        ModelAndView mv = handle(request, response);
	        ModelAndViewAssert.assertViewName(mv, "bdrsMobileManifest");
	        /*
	         * TODO make sure we build up relevant images list for all surveys
	         * should they have images
	         * ModelAndViewAssert.assertModelAttributeAvailable(mv, "imagesList");
	         * ModelAndViewAssert.assertModelAttributeAvailable(mv, "fileSize");
	         * ModelAndViewAssert.assertModelAttributeAvailable(mv, "customCss");
	         */
	        ModelAndViewAssert.assertModelAttributeAvailable(mv, "manifestVersion");
	        Assert.assertEquals("Content type should be text/cache-manifest", "text/cache-manifest", response.getContentType());
	    }

}
