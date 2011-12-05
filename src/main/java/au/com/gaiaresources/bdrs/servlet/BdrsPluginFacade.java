package au.com.gaiaresources.bdrs.servlet;

import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.template.TemplateService;

/**
 * The name could use improvement, it's a placeholder.
 * 
 * Class to provide access to various BDRS functionality to be used by 3rd party engines -
 * velocity, python and possibly other engines in the future
 * 
 * @author aaron
 *
 */
public class BdrsPluginFacade {

    private ContentService contentService;
    private Portal portal;
    private TemplateService templateService;
    private ManagedFileDAO managedFileDAO;
    private PreferenceDAO prefDAO;
    private Session sesh;
    
    Map<String, Object> contentParams;
    
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * Create a BdrsPluginFacade
     * 
     * @param portal - the current portal
     * @param requestUrl - the URL for the current request
     * @param currentUser - the currently logged in user
     */
    public BdrsPluginFacade(Session session, Portal portal, String requestUrl, User currentUser) {
        
        if (portal == null) {
            throw new IllegalArgumentException("Portal, portal, cannot be null");
        }
        if (requestUrl == null) {
            throw new IllegalArgumentException("String, requestUrl, cannot be null");
        }
        
        if (session == null) {
            throw new IllegalArgumentException("Session cannot be null");
        }
        this.sesh = session;
        // current user can be null when there is no logged in user!
        
        this.portal = portal;
        
        contentService = AppContext.getBean(ContentService.class);
        templateService = AppContext.getBean(TemplateService.class);
        managedFileDAO = AppContext.getBean(ManagedFileDAO.class);
        prefDAO = AppContext.getBean(PreferenceDAO.class);
        
        contentParams = ContentService.getContentParams(portal, requestUrl, currentUser);
    }
    
    /**
     * Get some text content
     * 
     * @param key - the content key to retrieve
     * @return String, the content
     */
    public String getContent(String key) {
        try {
            String content = contentService.getContent(sesh, portal, key);
            return templateService.evaluate(content, contentParams);
        } catch (Exception e) {
            log.error("Could not fetch content, key : " + key, e);
            return "<< BDRS : could not fetch content >>";
        }
    }
    
    /**
     * Get a managed file by uuid
     * 
     * @param uuid - the uuid to search for
     * @return ManagedFile object. null if not found
     */
    public ManagedFile getManagedFile(String uuid) {
        try {
            return managedFileDAO.getManagedFile(uuid);
        } catch (Exception e) {
            log.error("Could not fetch managed file for uuid = " + uuid, e);
            return null;
        }
    }
    
    /**
     * Get a preference value for the current portal by key
     * 
     * @param key - the key for the preference
     * @return String, the preference value
     */
    public String getPreferenceValue(String key) {
        Preference p = prefDAO.getPreferenceByKey(key);
        return p != null ? p.getValue() : "";
    }
    
    /**
     * Get a preference value as a boolean. Preference values are always
     * strings. This will return true if the string value is equal to
     * 'true', case insensitive.
     * 
     * @param key - the key for the preference
     * @return boolean, the preference value
     */
    public boolean getPreferenceBooleanValue(String key) {
        String strValue = getPreferenceValue(key);
        return strValue.toLowerCase().equals("true");
    }
}
