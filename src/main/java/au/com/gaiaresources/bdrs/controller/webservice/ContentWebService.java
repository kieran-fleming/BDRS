package au.com.gaiaresources.bdrs.controller.webservice;

import javax.annotation.PostConstruct;
import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentService;

@Controller
public class ContentWebService extends AbstractController {

    @Autowired
    private ContentService contentService;

    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String SUCCESS = "success";

    private Logger log = Logger.getLogger(getClass());
    
    /**
     * web service for saving content
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @RolesAllowed( { Role.ROOT, Role.ADMIN })
    @RequestMapping(value = "/webservice/content/saveContent.htm", method = RequestMethod.POST)
    public void save(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String key = request.getParameter(KEY);
        String value = request.getParameter(VALUE);
        if (!StringUtils.hasLength(key)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No key passed to save content");
            log.warn("No key passed to save content");
            return;
        }
        // we don't check for valid keys. maybe this needs to change?
        saveContent(key, value);
        response.setContentType("application/json");
    }

    /**
     * Web service for retrieving content.
     * 
     * @param request
     * @param response
     * @throws Exception
     */
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR, Role.USER, Role.ANONYMOUS})
    @RequestMapping(value = "/webservice/content/loadContent.htm", method = RequestMethod.GET)
    public void load(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String key = request.getParameter(KEY);
        if (!StringUtils.hasLength(key)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No key passed to load content");
            return;
        }
        String content = getContent(key);
        JSONObject result = new JSONObject();
        result.put("content", content);
        writeJson(request, response, result.toString());
    }
    
    /**
     * helper method for getting content
     * 
     * @param key
     * @param contextPath
     * @return
     * @throws Exception
     */
    private String getContent(String key) throws Exception {
        
        return contentService.getContent(getRequestContext().getHibernate(), getRequestContext().getPortal(), key);
    }

    /**
     * helper method for saving content
     * 
     * @param key
     * @param value
     */
    private void saveContent(String key, String value) {
        
        Portal portal = getRequestContext().getPortal();
        if (portal == null) {
            throw new IllegalStateException("Portal cannot be null");
        }
        contentService.saveContent(getRequestContext().getHibernate(), portal, key, value);
    }
}
