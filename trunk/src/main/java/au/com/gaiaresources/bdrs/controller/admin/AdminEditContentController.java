package au.com.gaiaresources.bdrs.controller.admin;

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;

@Controller
public class AdminEditContentController extends AbstractController {

    @Autowired
    private ContentDAO contentDAO;

    @RequestMapping(value = "/admin/editContent.htm", method = RequestMethod.GET)
    public ModelAndView renderPage(HttpServletRequest request,
            HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("adminEditContent");
        List<String> keys = contentDAO.getAllKeys();
        mav.addObject("keys", keys);
        return mav;
    }
    
    // There is no protection when using this URL directly. You will reset
    // all of your content!
    @RequestMapping(value="/admin/resetContentToDefault.htm", method = RequestMethod.GET)
    public String reset(HttpServletRequest request, HttpServletResponse response) 
        throws Exception {
        PortalInitialiser pi = new PortalInitialiser();
        Portal currentPortal = getRequestContext().getPortal();
        if (currentPortal == null) {
            // something has gone seriously wrong for this to happen...
            throw new Exception("The portal cannot be null");
        }
        pi.initContent(currentPortal);
        return "redirect:/admin/editContent.htm";
    }
}
