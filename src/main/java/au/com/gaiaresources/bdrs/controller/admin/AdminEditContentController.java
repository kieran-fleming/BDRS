package au.com.gaiaresources.bdrs.controller.admin;

import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import edu.emory.mathcs.backport.java.util.TreeSet;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.service.content.ContentInitialiserService;

@Controller
public class AdminEditContentController extends AbstractController {
    Logger log = Logger.getLogger(AdminEditContentController.class);
    @Autowired
    private ContentDAO contentDAO;

    private ContentInitialiserService contentService = new ContentInitialiserService();
    
    @RequestMapping(value = "/admin/editContent.htm", method = RequestMethod.GET)
    public ModelAndView renderPage(HttpServletRequest request,
            HttpServletResponse response) {
        ModelAndView mav = new ModelAndView("adminEditContent");
        List<String> keys = contentDAO.getAllKeys();
        // add the default portal initializer keys as well if not present
        Set<String> uniqueKeys = new TreeSet(keys);
        uniqueKeys.addAll(ContentInitialiserService.CONTENT.keySet());
        mav.addObject("keys", uniqueKeys);
        return mav;
    }
    
    // There is no protection when using this URL directly. You will reset
    // all of your content!
    @RequestMapping(value="/admin/resetContentToDefault.htm", method = RequestMethod.GET)
    public String reset(HttpServletRequest request, HttpServletResponse response, 
            @RequestParam(value = "key", required = false) String key) throws Exception {
        Portal currentPortal = getRequestContext().getPortal();
        if (currentPortal == null) {
            // something has gone seriously wrong for this to happen...
            throw new Exception("The portal cannot be null");
        }
        if (key == null) {
            contentService.initContent(contentDAO, currentPortal, ContentInitialiserService.getRequestURL(request));
        } else {
            contentService.initContent(contentDAO, currentPortal, key, ContentInitialiserService.getRequestURL(request));
        }
        return "redirect:/admin/editContent.htm";
    }
}
