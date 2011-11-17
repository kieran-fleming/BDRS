package au.com.gaiaresources.bdrs.servlet;

import java.util.Map;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.template.TemplateService;

/**
 * The name sucks, it's a placeholder.
 * 
 * Class to provide access to various BDRS functionality to be used by 3rd party engines like
 * velocity, python plugin stuff, ???, profit.
 * 
 * @author aaron
 *
 */
public class BdrsPluginFacade {

    private ContentService contentService;
    private Portal portal;
    private TemplateService templateService;
    Map<String, Object> contentParams;
    
    private Logger log = Logger.getLogger(getClass());
    
    public BdrsPluginFacade(Portal portal, String requestUrl, User currentUser) {
        
        if (portal == null) {
            throw new IllegalArgumentException("Portal, portal, cannot be null");
        }
        if (requestUrl == null) {
            throw new IllegalArgumentException("String, requestUrl, cannot be null");
        }
        
        // current user can be null when there is no logged in user!
        
        this.portal = portal;
        
        contentService = AppContext.getBean(ContentService.class);
        templateService = AppContext.getBean(TemplateService.class);
        
        contentParams = ContentService.getContentParams(portal, requestUrl, currentUser);
    }
    
    public String getContent(String key) {
        try {
            String content = contentService.getContent(portal, key);
            return templateService.evaluate(content, contentParams);
        } catch (Exception e) {
            log.error("Could not fetch content, key : " + key, e);
            return "<< BDRS : could not fetch content >>";
        }
    }
}
