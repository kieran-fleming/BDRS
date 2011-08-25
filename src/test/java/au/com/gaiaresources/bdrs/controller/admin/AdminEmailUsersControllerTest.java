/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.admin;

import static org.junit.Assert.*;

import org.junit.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;

/**
 * @author stephanie
 *
 */
public class AdminEmailUsersControllerTest extends AbstractControllerTest {

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.controller.admin.AdminEmailUsersController#renderPage(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)}.
     * @throws Exception 
     */
    @Test
    public void testRenderPage() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/admin/emailUsers.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "adminEmailUsers");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "keys");
        
    }

    /**
     * Test method for {@link au.com.gaiaresources.bdrs.controller.admin.AdminEmailUsersController#sendMessage(javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse, java.lang.String, java.lang.String, java.lang.String)}.
     * @throws Exception 
     */
    @Test
    public void testSendMessage() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/admin/sendMessage.htm");
        request.setParameter("to", "");
        request.setParameter("subject", "");
        request.setParameter("content", "");
        
        //ModelAndView mv = handle(request, response);
        // TODO: how do we actually test this?
    }

}
