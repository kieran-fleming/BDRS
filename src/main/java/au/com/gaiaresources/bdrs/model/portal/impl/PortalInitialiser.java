package au.com.gaiaresources.bdrs.model.portal.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.model.portal.PortalUtil;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.detect.BDRSWurflLoadService;
import au.com.gaiaresources.bdrs.service.theme.ThemeService;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

// non autowired class...
public class PortalInitialiser implements ServletContextListener {

    public static final String ROOT_PORTAL_SETTINGS = "rootPortal.json";

    private String context_path = null;

    public static ServletContext servletContext = null;
    
    protected ThemeDAO themeDAO;
    
    protected ThemeService themeService;
    
    private Logger log = Logger.getLogger(getClass());
    
    // create admin user
    // create portal preferences
    // create moderation thresholds
    public void init(Session sesh, Portal p) throws IOException {
        if (context_path == null) {
            context_path = ContentService.getContextPath(RequestContextHolder.getContext().getRequestPath());
        }
        PortalUtil.initPortalData(sesh, p);
        initPortalTheme(p);
    }

    @Override
    public void contextDestroyed(ServletContextEvent arg0) {
    }

    @Override
    public void contextInitialized(ServletContextEvent arg0) {
        // autowire yourself
        context_path = arg0.getServletContext().getContextPath();
        PortalDAO portalDAO = AppContext.getBean(PortalDAO.class);
        themeDAO = AppContext.getBean(ThemeDAO.class);
        themeService = AppContext.getBean(ThemeService.class);
        Session sesh = portalDAO.getSessionFactory().getCurrentSession();
        
        try {
            Transaction tx = sesh.beginTransaction();
            BDRSWurflLoadService loadService = AppContext.getBean(BDRSWurflLoadService.class);
            List<Portal> portalList = portalDAO.getPortals();
            if (!portalList.isEmpty()) {
                log.info("ROOT portal already exists, skipping initialisation");
                // return;
                log.info("Making sure existing portals have all of the default portal parameters and moderation thresholds");
                for (Portal p : portalList) {
                    // do lazy init of portal preferences and preference categories
                    //log.debug("initialising prefs for portal " + p.getName());
                    PortalUtil.initPortalPreferences(sesh, p, true);
                    // this is here for legacy portals
                    // can be removed if we don't want to include this feature in 
                    // legacy portals or if we come up with a different way of managing them
                    PortalUtil.initModerationThreshold(sesh, p);
                }
            } else {
                log.info("Initialising ROOT portal");
                initRootPortal(sesh, context_path);
                // Running this here because if you run it in initRootPortal the tests
                // just never finish
                loadService.loadWurflXML("wurfl.xml");
            }
            loadService.loadWurflXML("wurfl_patch.xml");
            
            // update the default themes for all portals
            log.info("Updating default themes for all portals");
            for (Portal portal : portalList) {
                List<Theme> themes = themeService.createDefaultThemes(portal, context_path);
                if (themeDAO.getActiveTheme(portal) == null) {
                    Theme activeTheme = themes.get(0);
                    activeTheme.setActive(true);
                    themeDAO.save(activeTheme);
                }
            }
            
            tx.commit();
        } catch (Exception e) {
            log.error("Failed to initialise ROOT portal", e);
        }
        
    }

    // call this one on server startup if no portal already exists....
    public Portal initRootPortal(Session sesh, String contextPath) throws Exception {
        RegistrationService registrationService = AppContext.getBean(RegistrationService.class);

        String filename = ROOT_PORTAL_SETTINGS;
        JSONArray jsonArray = PortalUtil.getPreferenceJson(filename);

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

        return portalDAO.save(this, sesh, portal);
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
    
    /**
     * Sets the default theme to active if there is no active theme and creates 
     * the default theme if it does not already exist.
     * @param p The portal to initialize the theme for
     * @throws IOException
     */
    protected void initPortalTheme(Portal p) throws IOException {
        if (themeDAO == null) {
            // autowire yourself if you haven't been wired yet
            themeDAO = AppContext.getBean(ThemeDAO.class);
        }
        // if there is no active theme for the portal, set the default one
        Theme pTheme = themeDAO.getActiveTheme(p);
        if (pTheme == null) {
            List<Theme> themes = themeDAO.getThemes(p);
            if (themes.size() <= 0) {
                // initialize the default theme if the portal has no themes
                // this only happens the first time the portal is initialized
                pTheme = createDefaultTheme(p);
            } else {
                // find the default theme if the portal has themes
                pTheme = themeDAO.getDefaultTheme(p);
                if (pTheme == null) {
                    // error, this should never happen as the default theme is 
                    // created on portal creation and this cannot be modified
                    
                    // if it does, it means this is a legacy root portal and the default
                    // theme must be created here!
                    pTheme = createDefaultTheme(p);
                } else {
                    pTheme.setActive(true);
                    themeDAO.save(pTheme);
                }
            }
        }
    }

    /**
     * Create the default theme for the portal.
     * @param p The portal
     * @return The theme created from the default files
     * @throws IOException 
     */
    private Theme createDefaultTheme(Portal portal) throws IOException {
        if (themeService == null) {
            // autowire yourself if you haven't been wired yet
            themeService = AppContext.getBean(ThemeService.class);
        }
        
        List<Theme> themes = themeService.createDefaultThemes(portal, context_path);
        if (themeDAO.getActiveTheme(portal) == null) {
            Theme activeTheme = themes.get(0);
            activeTheme.setActive(true);
            themeDAO.save(activeTheme);
        }
        return themeDAO.getActiveTheme(portal);
    }
}
