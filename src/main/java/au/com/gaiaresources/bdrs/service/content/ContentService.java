/**
 * 
 */
package au.com.gaiaresources.bdrs.service.content;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

/**
 * @author stephanie
 *
 */
@Service
public class ContentService {
    
    private static Logger log = Logger.getLogger(ContentService.class);
    
    private static final String CONTENT_PACKAGE = "/au/com/gaiaresources/bdrs/service/content/";
    
    public static final Map<String, String> CONTENT;
    static {
        Map<String, String> tmp = new HashMap<String, String>();
        tmp.put("public/home", "public_home.vm");
        tmp.put("public/about", "public_about.vm");
        tmp.put("public/contact", "public_contact.vm");
        tmp.put("public/help", "public_help.vm");
        tmp.put("public/privacyStatement", "public_privacyStatement.vm");
        tmp.put("public/termsAndConditions", "termsAndConditions.vm");
        
        tmp.put("root/portalListing", "root_portalListing.vm");
        tmp.put("root/portalEdit", "root_portalEdit.vm");
        tmp.put("root/portalEdit/entryPoints", "root_portalEdit_entryPoints.vm");
        tmp.put("root/portalEdit/patternTester", "root_portalEdit_patternTester.vm");
        tmp.put("root/theme/listing", "root_theme_listing.vm");
        tmp.put("root/theme/edit", "root_theme_edit.vm");
        tmp.put("root/theme/edit/themeElements", "root_theme_edit_themeElements.vm");
        tmp.put("root/theme/edit/advanced", "root_theme_edit_advanced.vm");
        tmp.put("root/theme/edit/advanced/editFile", "root_theme_edit_advanced_editFile.vm");
        
        tmp.put("admin/groupEdit", "admin_groupEdit.vm");
        tmp.put("admin/groupListing", "admin_groupListing.vm");
        tmp.put("admin/thresholdEdit", "admin_thresholdEdit.vm");
        tmp.put("admin/censusMethodEdit", "admin_censusMethodEdit.vm");
        tmp.put("admin/censusMethodListing", "admin_censusMethodListing.vm");
        
        tmp.put("user/home", "user_home.vm");
        tmp.put("user/managedFileListing", "user_managedFileListing.vm");
        tmp.put("user/singleSiteMultiTaxaTable", "user_singleSiteMultiTaxaTable.vm");
        tmp.put("user/recordSightingMapDescription", "user_recordSightingMapDescription.vm");
        tmp.put("user/widgetBuilder", "user_widgetBuilder.vm");
        
        /* In content package resources */
        tmp.put("admin/home", CONTENT_PACKAGE + "admin_home.vm");
        tmp.put("admin/manageUsers", CONTENT_PACKAGE + "admin_manageUsers.vm");
        tmp.put("admin/approveUsers", CONTENT_PACKAGE + "admin_approveUsers.vm");
        tmp.put("admin/groupEdit/members", CONTENT_PACKAGE + "admin_groupEdit_members.vm");
        tmp.put("admin/groupEdit/groups", CONTENT_PACKAGE + "admin_groupEdit_groups.vm");
        tmp.put("admin/emailUsers", CONTENT_PACKAGE + "admin_emailUsers.vm");
        tmp.put("admin/editProjects", CONTENT_PACKAGE + "admin_editProjects.vm");
        tmp.put("admin/editProject", CONTENT_PACKAGE + "admin_editProject.vm");
        tmp.put("admin/editProject/editTaxonomy", CONTENT_PACKAGE + "admin_editProject_editTaxonomy.vm");
        tmp.put("admin/editProject/chooseCensusMethods", CONTENT_PACKAGE + "admin_editProject_chooseCensusMethods.vm");
        tmp.put("admin/editProject/editLocations", CONTENT_PACKAGE + "admin_editProject_editLocations.vm");
        tmp.put("admin/editProject/editLocation", CONTENT_PACKAGE + "admin_editProject_editLocation.vm");
        tmp.put("admin/manageThresholds", CONTENT_PACKAGE + "admin_manageThresholds.vm");
        tmp.put("admin/editPreferences", CONTENT_PACKAGE + "admin_editPreferences.vm");
        tmp.put("admin/taxonomy/editTaxonomy", CONTENT_PACKAGE + "admin_taxonomy_editTaxonomy.vm");
        tmp.put("admin/taxonomy/editTaxonomicGroups", CONTENT_PACKAGE + "admin_taxonomy_editTaxonomicGroups.vm");
        tmp.put("admin/taxonomy/listing", CONTENT_PACKAGE + "admin_taxonomy_listing.vm");
        tmp.put("admin/map/editMapLayer", CONTENT_PACKAGE + "admin_map_editMapLayer.vm");
        tmp.put("admin/manageFiles/editFile", CONTENT_PACKAGE + "admin_managedFiles_editFile.vm");
        tmp.put("admin/gallery/editGallery", CONTENT_PACKAGE + "admin_gallery_editGallery.vm");
        tmp.put("admin/gallery/listGallery", CONTENT_PACKAGE + "admin_gallery_listGallery.vm");
        tmp.put("admin/content/edit", CONTENT_PACKAGE + "admin_content_edit.vm");
        tmp.put("user/review/mySightings", CONTENT_PACKAGE + "user_review_mySightings.vm");
        
        tmp.put("user/profile/editProfile", CONTENT_PACKAGE + "user_profile_editProfile.vm");
        tmp.put("user/locations/edit.vm", CONTENT_PACKAGE + "user_locations_edit.vm");
        
        tmp.put("user/report/listing", CONTENT_PACKAGE + "user_report_listing.vm");
        
        /* In email package resources */
        tmp.put("email/ExpertConfirmation", "/au/com/gaiaresources/bdrs/email/ExpertConfirmation.vm");
        tmp.put("email/PasswordReminder", "/au/com/gaiaresources/bdrs/email/PasswordReminder.vm");
        tmp.put("email/StudentSignUp", "/au/com/gaiaresources/bdrs/email/StudentSignUp.vm");
        tmp.put("email/StudentSurveyLink", "/au/com/gaiaresources/bdrs/email/StudentSurveyLink.vm");
        tmp.put("email/TeacherClassListing", "/au/com/gaiaresources/bdrs/email/TeacherClassListing.vm");
        tmp.put("email/UnhandledError", "/au/com/gaiaresources/bdrs/email/UnhandledError.vm");
        tmp.put("email/UserSignUp", "/au/com/gaiaresources/bdrs/email/UserSignUp.vm");
        tmp.put("email/UserSignUpApproval", "/au/com/gaiaresources/bdrs/email/UserSignUpApproval.vm");
        tmp.put("email/UserSignUpApproved", "/au/com/gaiaresources/bdrs/email/UserSignUpApproved.vm");
        tmp.put("email/UserSignUpWait", "/au/com/gaiaresources/bdrs/email/UserSignUpWait.vm");
        tmp.put("email/ContactRecordOwner", "/au/com/gaiaresources/bdrs/email/ContactRecordOwner.vm");
        tmp.put("email/ContactRecordOwnerSendToSelf", "/au/com/gaiaresources/bdrs/email/ContactRecordOwnerSendToSelf.vm");
        tmp.put("email/ModerationPerformed", "/au/com/gaiaresources/bdrs/email/ModerationPerformed.vm");
        tmp.put("email/ModerationRequired", "/au/com/gaiaresources/bdrs/email/ModerationRequired.vm");
        
        CONTENT = Collections.unmodifiableMap(tmp);
    }
    
    @Autowired
    private ContentDAO contentDAO;
    
    /**
     * Initialize all of the content to the default template file content.
     * @param portal
     * @throws IOException
     */
    public void initContent(Session sesh, Portal portal) throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("portal", portal);

        for (Entry<String, String> entry : CONTENT.entrySet()) {
            initContent(sesh, portal, entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Initialize the content of the given key to the default value.
     * @param portal
     * @param key
     * @param applicationUrl - the domain + context path of the application
     * @throws IOException
     */
    public Content initContent(Session sesh, Portal portal, String key, String value) throws IOException {
        if (value == null) {
            value = CONTENT.get(key);
        }
        InputStream stream = PortalInitialiser.class.getResourceAsStream(value);

        if (stream == null) {
            log.debug("Error getting content stream for : "+value);
        }
        try {
            String text = readStream(stream);
            
            // we might be reinitializing content, so check first if it exists
            Content content = contentDAO.getContent(sesh, key);
            if (content == null) {
                content = contentDAO.saveNewContent(sesh, key, text);
            } else {
                content = contentDAO.saveContent(sesh, key, text);
            }
            if (portal != null) {
                content.setPortal(portal);
            }
            return content;
        } catch (Exception e) {
            log.error("Could not initialise content. key = " + key + " and filename = " + value, e);
            return null;
        } finally {
            if (stream != null)
                stream.close();
        }
    }
    
    /**
     * Gets the content for the key and creates it if it doesn't exist.
     * Assumes that the content is for the current portal.
     * @param sesh
     * @param key
     * @return
     */
    public String getContent(Session sesh, String key) {
        return getContent(sesh, RequestContextHolder.getContext().getPortal(), key);
    }

    /**
     * 
     * @param currentPortal
     * @param key - the key of the content to retrieve
     * @param applicationUrl - the domain + context path of the application
     */
    public String getContent(Session sesh, Portal currentPortal, String key) {
        Content item = contentDAO.getContent(sesh, key);
        if (item == null) {
            log.warn("Didn't find content \"" + key + "\" in the DAO, " +
                    "loading default value.");
            // couldn't find the content in the DAO,
            // load the default from the file
            if (currentPortal == null) {
                throw new IllegalArgumentException("Portal cannot be null");
            }
            
            try {
                item = initContent(sesh, currentPortal, key, null);
                if (item == null) {
                    log.warn("Couldn't load default for content \"" + key + "\".");
                    return "";
                }    
            } catch (IOException ioe) {
                log.warn("IOException when loading default content \"" + key + "\".");
                return "";
            }
        }
        return item.getValue();
    }

    /**
     * Saves content. Could make it only save content keys that exist in the static map?
     * 
     */
    public void saveContent(Session sesh, Portal currentPortal, String key, String value) {
        contentDAO.saveContent(sesh, key, value);
    }

    private static String readStream(InputStream inStream) throws IOException {
        StringWriter outStream = new StringWriter();
        IOUtils.copy(inStream, outStream);
        return outStream.toString();
    }

    public static String getRequestURL(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        return getRequestURL(url);
    }
    
    private static final Pattern requestUrlPattern = Pattern.compile("(http://[^/]*/[^/]+?)/.*");

    public static String getRequestURL(String url) {
        if (url == null) {
            throw new IllegalArgumentException("String, url, cannot be null");
        }
        
        Matcher m = requestUrlPattern.matcher(url);
        if (m.matches()) {
            // group 0 is the whole string
            // group 1 is the request url up to the context path
            return m.group(1);
        }
        throw new IllegalArgumentException("No request url detected : " + url);
    }
    
    private static final Pattern contextPathPattern = Pattern.compile("http://[^/]*(/[^/]+?)/.*");
    
    public static String getContextPath(String url) {
        if (url == null) {
            throw new IllegalArgumentException("String, url, cannot be null");
        }
        
        Matcher m = contextPathPattern.matcher(url);
        if (m.matches()) {
            // group 0 is the whole string
            // group 1 is the context path
            return m.group(1);
        }
        log.warn("url does not have context path : " + url);
        return null;
    }

    public static Map<String, Object> getContentParams() {
        return getContentParams(null, null, null);
    }
    
    public static Map<String, Object> getContentParams(Portal portal, String requestPath, User currentUser) {
        Map<String, Object> params = new HashMap<String, Object>();
        putContentParams(portal, requestPath, currentUser, params);
        return params;
    }
    
    private static void putContentParams(Portal portal, String requestPath, User currentUser, Map<String, Object> params) {
        if (portal == null) {
            portal = RequestContextHolder.getContext().getPortal();
        }
        
        if (requestPath == null) {
            requestPath = RequestContextHolder.getContext().getRequestPath();
        }
        
        if (currentUser == null) {
            currentUser = RequestContextHolder.getContext().getUser();
        }

        if (portal != null && !params.containsKey("portal")) {
            params.put("portal", portal);
        }
        if (currentUser != null && !params.containsKey("currentUser")) {
            params.put("currentUser", currentUser);
        }
        if (requestPath != null) {
            try {
                if (!params.containsKey(BDRS_CONTEXT_PATH)) {
                    params.put(BDRS_CONTEXT_PATH, getContextPath(requestPath));
                }
            } catch (Exception e) {
                // catch an illegal argument exception 
            }
            try {
                if (!params.containsKey(BDRS_APPLICATION_URL)) {
                    params.put(BDRS_APPLICATION_URL, getRequestURL(requestPath) + "/portal/"+portal.getId());
                }
            } catch (Exception e) {
                // catch an illegal argument exception
            }
        }
    }
    
    public static final String BDRS_APPLICATION_URL = "bdrsApplicationUrl",
                               BDRS_CONTEXT_PATH = "bdrsContextPath",
                               BDRS_CURRENT_USER_FIRST_NAME = "current";

    /**
     * Add the required content substitution parameters to the given map
     * @param subsitutionParams
     */
    public static void putContentParams(Map<String, Object> params) {
        putContentParams(null, null, null, params);
    }
}
