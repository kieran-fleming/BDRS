/**
 * 
 */
package au.com.gaiaresources.bdrs.service.portal;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentInitialiserService;

/**
 * @author stephanie
 *
 */
public abstract class AbstractPortalService implements PortalService {

    private static Logger log = Logger.getLogger(AbstractPortalService.class);
    
    public static final String DEFAULT_PREFERENCES = "preferences.json";

    protected ContentInitialiserService contentService = new ContentInitialiserService();
    
    public void initPortal(Portal p) throws IOException {
        RegistrationService registrationService = AppContext.getBean(RegistrationService.class);
        User u = registrationService.signUp("admin", "admin@gaiabdrs.com", "admin", "admin", "password", null, Role.ADMIN, true);
        u.setPortal(p);
        initPortalPreferences(p);
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
    

    protected JSONArray getPreferenceJson(String filename) throws IOException {
        // Load the JSON data from file.
        BufferedReader reader = new BufferedReader(new InputStreamReader(
                PortalInitialiser.class.getResourceAsStream(filename)));
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
        cat.setRunThreshold(false);
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
        pref.setRunThreshold(false);
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
