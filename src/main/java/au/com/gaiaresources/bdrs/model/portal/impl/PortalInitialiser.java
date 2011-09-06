package au.com.gaiaresources.bdrs.model.portal.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.detect.BDRSWurflLoadService;
import au.com.gaiaresources.bdrs.service.portal.AbstractPortalService;

// non autowired class...
public class PortalInitialiser extends AbstractPortalService implements ServletContextListener {

    public static final String ROOT_PORTAL_SETTINGS = "rootPortal.json";

    private Logger log = Logger.getLogger(getClass());
    
    // create admin user
    // create portal preferences
    // create default content
    public void init(Portal p) throws IOException {
        initPortal(p);
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
            BDRSWurflLoadService loadService = AppContext.getBean(BDRSWurflLoadService.class);
            if (!portalDAO.getPortals().isEmpty()) {
                log.info("ROOT portal already exists, skipping initialisation");
               // return;
            }else{
            	 log.info("Initialising ROOT portal");
                 initRootPortal(arg0.getServletContext().getContextPath());
                 // Running this here because if you run it in initRootPortal the tests
                 // just never finish
                 loadService.loadWurflXML("wurfl.xml");
            }
            loadService.loadWurflXML("wurfl_patch.xml");
            

            tx.commit();
        } catch (Exception e) {
            log.error("Failed to initialise ROOT portal", e);
        }
        
    }

    // call this one on server startup if no portal already exists....
    public Portal initRootPortal(String contextPath) throws Exception {
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
        User rootUser = registrationService.signUp("root", "root@gaiabdrs.com", "root", "root", "password", contextPath, Role.ROOT, true);
        rootUser.setRoles(new String[] { Role.ROOT, Role.ADMIN });
        rootUser.setPortal(portal);

        // Just note we don't need to call PortalInitialiser.init() on this new portal.
        // The init method is called inside of PortalDAO.save.
        
        return portal;
    }

    private Portal createPortal(Session sesh, JSONObject jsonObject) throws Exception {
        PortalDAO portalDAO = AppContext.getBean(PortalDAO.class);
        String portalName = jsonObject.getString("name");
        Portal portal = new Portal();
        portal.setRunThreshold(false);
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
        entryPoint.setRunThreshold(false);
        entryPoint.setPattern(entryPattern);
        entryPoint.setRedirect(jsonObject.getString("redirect"));
        entryPoint.setPortal(portal);
        portalDAO.save(sesh, entryPoint);
    }
}
