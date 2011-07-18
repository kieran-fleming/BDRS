package au.com.gaiaresources.bdrs.model.portal.impl;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.detect.BDRSWurflLoadService;

// non autowired class...
public class PortalInitialiser implements ServletContextListener {

    public static final String DEFAULT_PREFERENCES = "preferences.json";
    public static final String ROOT_PORTAL_SETTINGS = "rootPortal.json";

    private Logger log = Logger.getLogger(getClass());
    
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
        
        CONTENT = Collections.unmodifiableMap(tmp);
    }

    // create admin user
    // create portal preferences
    // create default content
    public void init(Portal p) throws IOException {
        RegistrationService registrationService = AppContext.getBean(RegistrationService.class);

        User u = registrationService.signUp("admin", "admin@gaiabdrs.com", "admin", "admin", "password", Role.ADMIN, true);
        u.setPortal(p);

        initContent(p);

        initPortalPreferences(p);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
    	 PortalDAO portalDAO = AppContext.getBean(PortalDAO.class);
         Session sesh = portalDAO.getSessionFactory().getCurrentSession();
    	
        try {
            Transaction tx = sesh.beginTransaction();

            if (!portalDAO.getPortals().isEmpty()) {
                log.info("ROOT portal already exists, skipping initialisation");
               // return;
            }else{
            	 log.info("Initialising ROOT portal");
                 initRootPortal();
            }
            
            BDRSWurflLoadService loadService = AppContext.getBean(BDRSWurflLoadService.class);
            loadService.loadWurflXML("wurfl.xml");
            loadService.loadWurflXML("wurfl_patch.xml");
            


            tx.commit();
        } catch (Exception e) {
            log.error("Failed to initialise ROOT portal", e);
        }
        
    }

    // call this one on server startup if no portal already exists....
    public Portal initRootPortal() throws Exception {
        PreferenceDAO prefDAO = AppContext.getBean(PreferenceDAO.class);
        RegistrationService registrationService = AppContext.getBean(RegistrationService.class);

        String filename = ROOT_PORTAL_SETTINGS;
        JSONArray jsonArray = getPreferenceJson(filename);

        List<JSONObject> portalJsonObjList = new ArrayList<JSONObject>(
                jsonArray.size());
        List<JSONObject> portalEntryPointJsonObjList = new ArrayList<JSONObject>(
                jsonArray.size());

        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String className = jsonObject.getString("classname");

            if (Portal.class.getCanonicalName().equals(className)) {
                portalJsonObjList.add(jsonObject);
            } else if (PortalEntryPoint.class.getCanonicalName().equals(className)) {
                portalEntryPointJsonObjList.add(jsonObject);
            } else {
                log.warn(String.format("Unknown preference class \"%s\" found. Ignoring.", className));
            }
        }

        if (portalJsonObjList.size() != 1) {
            throw new Exception("Must only have 1 portal to create in "
                    + filename);
        }

        Session sesh = prefDAO.getSessionFactory().getCurrentSession();
        Portal portal = createPortal(sesh, portalJsonObjList.get(0));
        
        if (portal == null) {
            throw new Exception("Root portal creation failed!");
        }

        // multiple entry points allowed i guess...
        for (JSONObject jsonObject : portalEntryPointJsonObjList) {
            createPortalEntryPoint(sesh, portal, jsonObject);
        }

        // create root user...
        User rootUser = registrationService.signUp("root", "root@gaiabdrs.com", "root", "root", "password", Role.ROOT, true);
        rootUser.setRoles(new String[] { Role.ROOT, Role.ADMIN });
        rootUser.setPortal(portal);

        // Just note we don't need to call PortalInitialiser.init() on this new portal.
        // The init method is called inside of PortalDAO.save.
        
        return portal;
    }
    
    private static String readStream(InputStream inStream) throws IOException {
        StringWriter outStream = new StringWriter();
        IOUtils.copy(inStream, outStream);
        return outStream.toString();
    }

    // Exposing as a public method so we can reinit the portal content to the defaults.
    // Also good for testing our default content config files...
    public void initContent(Portal portal) throws IOException {
        ContentDAO contentDAO = AppContext.getBean(ContentDAO.class);

        Map<String, Object> params = new HashMap<String, Object>();
        params.put("portal", portal);

        for (Entry<String, String> entry : CONTENT.entrySet()) {
            initContent(contentDAO, portal, entry.getKey(), entry.getValue());
        }
    }

    /**
     * Initialize the content of the given key to the default value.
     * @param contentDAO
     * @param portal
     * @param key
     * @throws IOException
     */
    public void initContent(ContentDAO contentDAO, Portal portal, String key) throws IOException {
        initContent(contentDAO, portal, key, null);
    }

    /**
     * Initialize the content of the given key to the given value.
     * @param contentDAO
     * @param portal
     * @param key
     * @param value
     * @throws IOException
     */
    public Content initContent(ContentDAO contentDAO, Portal portal, String key, String value) throws IOException {
        // note we now copy stuff directly from the file - all the templating stuff is 
        // left in its original form.
        if (value == null)
            value = CONTENT.get(key);
        InputStream stream = PortalInitialiser.class.getResourceAsStream(value);
        try {
            String text = readStream(stream);
            Content content = contentDAO.saveContent(key, text);
            content.setPortal(portal);
            return content;
        } catch (IOException e) {
            log.error("Could not initialise content. key = " + key + " and filename = " + value, e);
            return null;
        } finally {
            if (stream != null)
                stream.close();
        }
    }

    private Portal createPortal(Session sesh, JSONObject jsonObject) throws Exception {
        PortalDAO portalDAO = AppContext.getBean(PortalDAO.class);
        String portalName = jsonObject.getString("name");
        Portal portal = new Portal();
        portal.setName(portalName);
        portal.setDefault(jsonObject.getBoolean("isDefault"));

        return portalDAO.save(sesh, portal);
    }

    private void createPortalEntryPoint(Session sesh, Portal portal,
            JSONObject jsonObject) {
        if (portal == null) {
            throw new NullPointerException("portal cannot be null");
            // No need to check for the entry point because it cannot exist.
        }
        
        PortalDAO portalDAO = AppContext.getBean(PortalDAO.class);
        String entryPattern = jsonObject.getString("pattern");

        PortalEntryPoint entryPoint = new PortalEntryPoint();
        entryPoint.setPattern(entryPattern);
        entryPoint.setRedirect(jsonObject.getString("redirect"));
        entryPoint.setPortal(portal);
        portalDAO.save(sesh, entryPoint);
    }

    private void initPortalPreferences(Portal portal) throws IOException {
        PreferenceDAO prefDAO = AppContext.getBean(PreferenceDAO.class);
        JSONArray jsonArray = getPreferenceJson(DEFAULT_PREFERENCES);

        List<JSONObject> catJsonObjList = new ArrayList<JSONObject>(
                jsonArray.size());
        List<JSONObject> prefJsonObjList = new ArrayList<JSONObject>(
                jsonArray.size());
        for (int i = 0; i < jsonArray.size(); i++) {

            JSONObject jsonObject = jsonArray.getJSONObject(i);
            String className = jsonObject.getString("classname");

            if (Portal.class.getCanonicalName().equals(className)) {
                // do nothing
            } else if (PortalEntryPoint.class.getCanonicalName().equals(className)) {
                // do nothing
            } else if (PreferenceCategory.class.getCanonicalName().equals(className)) {
                catJsonObjList.add(jsonObject);
            } else if (Preference.class.getCanonicalName().equals(className)) {
                prefJsonObjList.add(jsonObject);
            } else {
                log.warn(String.format("Unknown preference class \"%s\" found. Ignoring.", className));
            }
        }

        Session sesh = prefDAO.getSessionFactory().getCurrentSession();
        Map<String, PreferenceCategory> catMap = new HashMap<String, PreferenceCategory>();
        for (JSONObject jsonObject : catJsonObjList) {
            PreferenceCategory cat = createCategory(sesh, portal, jsonObject);
            catMap.put(cat.getName(), cat);
        }

        for (JSONObject jsonObject : prefJsonObjList) {
            createPreference(sesh, portal, catMap, jsonObject);
        }
    }

    private JSONArray getPreferenceJson(String filename) throws IOException {
        // Load the JSON data from file.
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                getClass().getResourceAsStream(filename)));
        StringBuilder builder = new StringBuilder();
        for (String line = reader.readLine(); line != null; line = reader.readLine()) {
            builder.append(line);
        }
        reader.close();
        return JSONArray.fromObject(builder.toString());
    }

    private PreferenceCategory createCategory(Session sesh, Portal portal,
            JSONObject jsonObject) {
        PreferenceDAO prefDAO = AppContext.getBean(PreferenceDAO.class);
        String name = jsonObject.getString("name");
        String displayName = jsonObject.getString("displayName");
        String description = jsonObject.getString("description");

        PreferenceCategory cat = new PreferenceCategory();
        cat.setName(name);
        cat.setPortal(portal);
        cat.setDisplayName(displayName);
        cat.setDescription(description);
        return prefDAO.save(sesh, cat);
    }

    private Preference createPreference(Session sesh, Portal portal,
            Map<String, PreferenceCategory> catMap, JSONObject jsonObject) {
        PreferenceDAO prefDAO = AppContext.getBean(PreferenceDAO.class);
        String key = jsonObject.getString("key");
        String value = jsonObject.getString("value");
        boolean locked = jsonObject.getBoolean("locked");
        String description = jsonObject.getString("description");
        String prefCatName = jsonObject.getString("preferenceCategoryName");
        boolean isRequired = jsonObject.getBoolean("isRequired");

        Preference pref = new Preference();
        pref.setLocked(locked);
        pref.setPortal(portal);
        pref.setPreferenceCategory(catMap.get(prefCatName));
        pref.setKey(key);
        pref.setValue(value);
        pref.setDescription(description);
        pref.setIsRequired(isRequired);
        return prefDAO.save(sesh, pref);
    }
}
