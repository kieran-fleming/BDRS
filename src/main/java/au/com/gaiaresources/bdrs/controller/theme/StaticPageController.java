package au.com.gaiaresources.bdrs.controller.theme;

import java.io.File;
import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.service.template.TemplateService;

@Controller
public class StaticPageController extends AbstractController {
    
    @Autowired
    TemplateService templateService;
    @Autowired
    ThemeDAO themeDAO;
    
    Logger log = Logger.getLogger(getClass());
    
    public final static String STATIC_PUBLIC_URL = "/bdrs/public/static/";
    public final static String STATIC_PUBLIC_URL_CATCHALL = STATIC_PUBLIC_URL + "**"; 
    public final static String STATIC_PUBLIC_FILE_DIR = "static/public/";
    
    public final static String STATIC_ADMIN_URL = "bdrs/admin/static/";
    public final static String STATIC_ADMIN_URL_CATCHALL = STATIC_ADMIN_URL + "**";
    public final static String STATIC_ADMIN_FILE_DIR = "static/admin/";
    
    
    
    /*
     * Controller for managing static content uploaded in themes.
     * 
     * 1. Create your theme.
     * 2. Make a directory in the base dir of the theme called... 
     *    - static_public
     * 3. Add a .vm file to this dir. e.g. PAGE_NAME.vm
     * 4. Upload your theme as normal
     * 5. Now browse to the url mapping: /bdrs/public/static/PAGE_NAME.htm   
     * 6. The controller will do some magic and insert the page content into the
     *    themed template for the portal!
     *    
     *    If you're feeling lucky you can do a similar thing for static pages with 
     *    different role requirements, user, admin, root etc!
     */
    
    @RequestMapping(value = STATIC_PUBLIC_URL_CATCHALL, method = RequestMethod.GET)
    public ModelAndView displayPublicStaticPage(HttpServletRequest request,
                                HttpServletResponse response) throws Exception {
        return displayStaticPage(request, response, STATIC_PUBLIC_URL, STATIC_PUBLIC_FILE_DIR);
    }
    
    private ModelAndView displayStaticPage(HttpServletRequest request, HttpServletResponse response,
            String staticUrlPrefix, String fileDir) throws Exception {
        try {
            String contextPath = request.getContextPath();
            String requestUri = request.getRequestURI();
            String filename = requestUri.substring(contextPath.length() + staticUrlPrefix.length(), requestUri.length() - 3) + "vm";

            Theme theme = themeDAO.getActiveTheme(getRequestContext().getPortal());
            
            String path = new File(Theme.THEME_DIR_PROCESSED,
                                   fileDir + filename).getPath();

            StringWriter writer = new StringWriter();

            // We may need to replace some attributes.... add to this map if that's the case
            Map<String, Object> attributeMap = new HashMap<String, Object>();
            
            templateService.mergeTemplate(theme, path, attributeMap, writer);
            ModelAndView mv = new ModelAndView("static_public");
            
            mv.addObject("renderedPage", writer.toString());
            
            return mv;    
        } catch (Exception e) {
            log.debug("Context path: " + request.getContextPath());
            log.debug("Request URI: " + request.getRequestURI());
            log.debug("Request URL: " + request.getRequestURL().toString());
            throw e;
        }
    }
    
    @RequestMapping(value = STATIC_ADMIN_URL_CATCHALL, method = RequestMethod.GET) 
    public ModelAndView displayAdminStaticPage(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        return displayStaticPage(request, response, STATIC_ADMIN_URL, STATIC_ADMIN_FILE_DIR);
    }
}
