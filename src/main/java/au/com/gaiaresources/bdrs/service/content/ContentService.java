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

import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;

/**
 * @author stephanie
 *
 */
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
        tmp.put("admin/manageUsers", CONTENT_PACKAGE + "admin_manageUsers.vm");
        tmp.put("admin/groupEdit/members", CONTENT_PACKAGE + "admin_groupEdit_members.vm");
        tmp.put("admin/groupEdit/groups", CONTENT_PACKAGE + "admin_groupEdit_groups.vm");
        tmp.put("admin/emailUsers", CONTENT_PACKAGE + "admin_emailUsers.vm");
        tmp.put("admin/editProjects", CONTENT_PACKAGE + "admin_editProjects.vm");
        tmp.put("admin/editProject", CONTENT_PACKAGE + "admin_editProject.vm");
        tmp.put("admin/editProject/editTaxonomy", CONTENT_PACKAGE + "admin_editProject_editTaxonomy.vm");
        tmp.put("admin/editProject/chooseCensusMethods", CONTENT_PACKAGE + "admin_editProject_chooseCensusMethods.vm");
        tmp.put("admin/editProject/editLocations", CONTENT_PACKAGE + "admin_editProject_editLocations.vm");
        tmp.put("admin/manageThresholds", CONTENT_PACKAGE + "admin_manageThresholds.vm");
        tmp.put("admin/editPreferences", CONTENT_PACKAGE + "admin_editPreferences.vm");
        tmp.put("admin/taxonomy/editTaxonomy", CONTENT_PACKAGE + "admin_taxonomy_editTaxonomy.vm");
        tmp.put("admin/taxonomy/editTaxonomicGroups", CONTENT_PACKAGE + "admin_taxonomy_editTaxonomicGroups.vm");
        tmp.put("admin/map/editMapLayer", CONTENT_PACKAGE + "admin_map_editMapLayer.vm");
        tmp.put("admin/manageFiles/editFile", CONTENT_PACKAGE + "admin_managedFiles_editFile.vm");
        
        /* In email package resources */
        tmp.put("email/ExpertConfirmation", "/au/com/gaiaresources/bdrs/email/ExpertConfirmation.vm");
        tmp.put("email/PasswordReminder", "/au/com/gaiaresources/bdrs/email/PasswordReminder.vm");
        tmp.put("email/StudentSignUp", "/au/com/gaiaresources/bdrs/email/StudentSignUp.vm");
        tmp.put("email/StudentSurveyLink", "/au/com/gaiaresources/bdrs/email/StudentSurveyLink.vm");
        tmp.put("email/TeacherClassListing", "/au/com/gaiaresources/bdrs/email/TeacherClassListing.vm");
        tmp.put("email/UnhandledError", "/au/com/gaiaresources/bdrs/email/UnhandledError.vm");
        tmp.put("email/UserSignUp", "/au/com/gaiaresources/bdrs/email/UserSignUp.vm");
        tmp.put("email/UserSignupApproval", "/au/com/gaiaresources/bdrs/email/UserSignupApproval.vm");
        tmp.put("email/UserSignupApproved", "/au/com/gaiaresources/bdrs/email/UserSignupApproved.vm");
        tmp.put("email/UserSignUpWait", "/au/com/gaiaresources/bdrs/email/UserSignUpWait.vm");
        tmp.put("email/ContactRecordOwner", "/au/com/gaiaresources/bdrs/email/ContactRecordOwner.vm");
        tmp.put("email/ContactRecordOwnerToSelf", "/au/com/gaiaresources/bdrs/email/ContactRecordOwnerSendToSelf.vm");
        
        CONTENT = Collections.unmodifiableMap(tmp);
    }
    
    /**
     * Initialize all of the content to the default template file content.
     * @param portal
     * @throws IOException
     */
    public void initContent(ContentDAO contentDAO, Portal portal, String contextPath) throws IOException {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("portal", portal);

        for (Entry<String, String> entry : CONTENT.entrySet()) {
            initContent(contentDAO, portal, entry.getKey(), entry.getValue(), contextPath);
        }
    }

    /**
     * Initialize the content of the given key to the default value.
     * @param portal
     * @param key
     * @throws IOException
     */
    public Content initContent(ContentDAO contentDAO, Portal portal, String key, String contextPath) throws IOException {
        return initContent(contentDAO, portal, key, null, contextPath);
    }

    /**
     * Initialize the content of the given key to the given value.
     * @param portal
     * @param key
     * @param value
     * @throws IOException
     */
    public Content initContent(ContentDAO contentDAO, Portal portal, String key, String value, String contextPath) throws IOException {
        // note we now copy stuff directly from the file - all the templating stuff is 
        // left in its original form.
        if (value == null)
            value = CONTENT.get(key);
        InputStream stream = PortalInitialiser.class.getResourceAsStream(value);
        if (stream == null) {
            log.debug("Error getting content stream for : "+value);
        }
        try {
            String text = readStream(stream);
            // replace ${bdrs.application.url}
            if (text.contains("bdrs.application.url")) {
                String repString = (contextPath == null ? "" : contextPath)+"/portal/"+
                     (portal != null ? portal.getId() : "\\$\\{portal.id\\}");
                text = text.replaceAll("\\$\\{bdrs.application.url\\}", repString);
            }
            
            // we might be reinitializing content, so check first if it exists
            Content content = contentDAO.getContent(key, false);
            if (content == null) {
                content = contentDAO.saveNewContent(key, text);
            } else {
                content = contentDAO.saveContent(key, text);
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
    
    public String getContent(ContentDAO contentDAO, Portal currentPortal, String key, String contextPath) {
        Content item = contentDAO.getContent(key);
        if (item == null) {
            log.warn("Didn't find content \"" + key + "\" in the DAO, " +
                    "loading default value.");
            // couldn't find the content in the DAO,
            // load the default from the file
            if (currentPortal == null) {
                throw new IllegalArgumentException("Portal cannot be null");
            }
            
            try {
                item = initContent(contentDAO, currentPortal, key, null, contextPath);
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

    private static String readStream(InputStream inStream) throws IOException {
        StringWriter outStream = new StringWriter();
        IOUtils.copy(inStream, outStream);
        return outStream.toString();
    }

    public static String getRequestURL(HttpServletRequest request) {
        String url = request.getRequestURL().toString();
        return url.substring(0, url.indexOf(request.getContextPath())+request.getContextPath().length());
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
        throw new IllegalArgumentException("No request url detected : " + url);
    }
}
