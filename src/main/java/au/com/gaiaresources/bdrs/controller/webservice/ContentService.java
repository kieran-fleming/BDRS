package au.com.gaiaresources.bdrs.controller.webservice;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class ContentService extends AbstractController {

    @Autowired
    private ContentDAO contentDAO;

    public static final String KEY = "key";
    public static final String VALUE = "value";
    public static final String SUCCESS = "success";

    private Logger log = Logger.getLogger(getClass());
    
    @RolesAllowed( { Role.ROOT, Role.ADMIN })
    @RequestMapping(value = "/webservice/content/saveContent.htm", method = RequestMethod.POST)
    public void save(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String key = request.getParameter(KEY);
        String value = request.getParameter(VALUE);
        if (!StringUtils.hasLength(key)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No key passed to save content");
            throw new Exception("No key passed to save content");
        }
        // we don't check for valid keys. maybe this needs to change?
        saveContent(key, value);
        response.setContentType("application/json");
    }

    @RequestMapping(value = "/webservice/content/loadContent.htm", method = RequestMethod.GET)
    public void load(HttpServletRequest request, HttpServletResponse response)
            throws Exception {

        String key = request.getParameter(KEY);
        if (!StringUtils.hasLength(key)) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "No key passed to load content");
            throw new Exception("No key passed to load content");
        }
        String content = getContent(key);
        JSONObject result = new JSONObject();
        result.put("content", content);
        response.getWriter().write(result.toString());
        response.setContentType("application/json");
    }

    private String getContent(String key) throws Exception {
        Content item = contentDAO.getContent(key);
        if (item == null) {
            log.warn("Didn't find content \"" + key + "\" in the DAO, " +
                    "loading default value.");
            // couldn't find the content in the DAO,
            // load the default from the file
            PortalInitialiser pi = new PortalInitialiser();
            Portal currentPortal = getRequestContext().getPortal();
            if (currentPortal == null) {
                // something has gone seriously wrong for this to happen...
                throw new Exception("The portal cannot be null");
            }
            item = pi.initContent(contentDAO, currentPortal, key, null);
            if (item == null) {
                log.warn("Couldn't load default for content \"" + key + "\".");
                return "";
            }
        }
        return item.getValue();
    }

    private void saveContent(String key, String value) {
        // the DAO does the 'does exist' checking for us...
        contentDAO.saveContent(key, value);
    }
}
