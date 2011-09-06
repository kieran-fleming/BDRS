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

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

/**
 * @author stephanie
 *
 */
public class ContentInitialiserService {
    private static Logger log = Logger.getLogger(ContentInitialiserService.class);
    
    public static final Map<String, String> CONTENT;
    static {
        Map<String, String> tmp = new HashMap<String, String>();
        tmp.put("public/home", "public_home.vm");
        tmp.put("public/about", "public_about.vm");
        tmp.put("user/home", "user_home.vm");
        tmp.put("public/contact", "public_contact.vm");
        tmp.put("public/help", "public_help.vm");
        tmp.put("public/privacyStatement", "public_privacyStatement.vm");
        tmp.put("public/termsAndConditions", "termsAndConditions.vm");
        tmp.put("user/managedFileListing", "user_managedFileListing.vm");
        tmp.put("user/widgetBuilder", "user_widgetBuilder.vm");
        tmp.put("root/portalListing", "root_portalListing.vm");
        tmp.put("root/portalEdit", "root_portalEdit.vm");
        tmp.put("root/portalEdit/entryPoints", "root_portalEdit_entryPoints.vm");
        tmp.put("root/portalEdit/patternTester", "root_portalEdit_patternTester.vm");
        tmp.put("admin/thresholdEdit", "admin_thresholdEdit.vm");
        
        tmp.put("root/theme/listing", "root_theme_listing.vm");
        tmp.put("root/theme/edit", "root_theme_edit.vm");
        tmp.put("root/theme/edit/themeElements", "root_theme_edit_themeElements.vm");
        tmp.put("root/theme/edit/advanced", "root_theme_edit_advanced.vm");
        tmp.put("root/theme/edit/advanced/editFile", "root_theme_edit_advanced_editFile.vm");
        
        tmp.put("admin/groupEdit", "admin_groupEdit.vm");
        tmp.put("admin/groupListing", "admin_groupListing.vm");
        
        tmp.put("admin/censusMethodEdit", "admin_censusMethodEdit.vm");
        tmp.put("admin/censusMethodListing", "admin_censusMethodListing.vm");
        
        tmp.put("email/ExpertConfirmation", "/au/com/gaiaresources/bdrs/email/ExpertConfirmation.vm");
        tmp.put("email/PasswordReminder", "/au/com/gaiaresources/bdrs/email/PasswordReminder.vm");
        tmp.put("email/StudentSignUp", "/au/com/gaiaresources/bdrs/email/StudentSignUp.vm");
        tmp.put("email/StudentSurveyLink", "/au/com/gaiaresources/bdrs/email/StudentSurveyLink.vm");
        tmp.put("email/TeacherClassListing", "/au/com/gaiaresources/bdrs/email/TeacherClassListing.vm");
        tmp.put("email/UnhandledError", "/au/com/gaiaresources/bdrs/email/UnhandledError.vm");
        tmp.put("email/UserSignUp", "/au/com/gaiaresources/bdrs/email/UserSignUp.vm");
        tmp.put("email/UserSignupApproval", "/au/com/gaiaresources/bdrs/email/UserSignupApproval.vm");
        tmp.put("email/UserSignupApproved", "/au/com/gaiaresources/bdrs/email/UserSignUpApproved.vm");
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

    private static String readStream(InputStream inStream) throws IOException {
        StringWriter outStream = new StringWriter();
        IOUtils.copy(inStream, outStream);
        return outStream.toString();
    }

    public static String getRequestURL(HttpServletRequest request) {
        // TODO Auto-generated method stub
        String url = request.getRequestURL().toString();
        return url.substring(0, url.indexOf(request.getContextPath())+request.getContextPath().length());
    }
}
