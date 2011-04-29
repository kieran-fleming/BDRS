package au.com.gaiaresources.bdrs.controller.portal;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import junit.framework.Assert;

import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Filter;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;
import au.com.gaiaresources.bdrs.servlet.filter.PortalSelectionFilter;

public class PortalControllerTest extends AbstractControllerTest {
    
    @Autowired
    PreferenceDAO prefDAO;
    
    private Logger log = Logger.getLogger(this.getClass()); 
    
    @Test
    public void testPortalListing() throws Exception {
        createTestPortals(false, "");
        
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/bdrs/root/portal/listing.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "portalSetup");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "portalList");
    }
    
    @Test
    public void testPortalEdit() throws Exception {
        
        Portal defaultPortal = portalDAO.getPortal(true);
        createTestPortals(false, "");
        defaultPortal.setDefault(true);
        defaultPortal = portalDAO.save(defaultPortal);
        
        login("root", "password", new String[] { Role.ROOT });
        
        Portal portal = portalDAO.getPortalByName(null, "myportal");
        
        request.setMethod("GET");
        request.setRequestURI("/bdrs/root/portal/edit.htm");
        request.setParameter("id", portal.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "portalEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "portal");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "portalEntryPointList");
        
        Assert.assertEquals(portal, mv.getModel().get("portal"));
        Assert.assertEquals(portalDAO.getPortalEntryPoints(portal), 
                            mv.getModel().get("portalEntryPointList"));
    }
    
    @Test
    public void testPortalEditSubmit() throws Exception {
        
        Portal defaultPortal = portalDAO.getPortal(true);
        createTestPortals(false, "");
        defaultPortal.setDefault(true);
        defaultPortal = portalDAO.save(defaultPortal);
        
        login("root", "password", new String[] { Role.ROOT });
        
        Portal portal = portalDAO.getPortalByName(null, "myportal");
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/root/portal/edit.htm");
        request.setParameter("portalId", portal.getId().toString());
        request.setParameter("name", "my portal edited");
        request.setParameter("default", String.valueOf(!portal.isDefault()));
        request.setParameter("testUrl", "http://example.com/BDRS/test/");
        
        // Edit existing entry points.
        for(PortalEntryPoint entry : portalDAO.getPortalEntryPoints(portal)) {
            if((entry.getId() % 2) == 0) {
                // Edit this entry point.
                request.addParameter("portalEntryPoint_id", entry.getId().toString());
                request.setParameter(String.format(PortalController.PORTAL_ENTRY_POINT_EDIT_PATTERN_TMPL, entry.getId()), 
                                     entry.getPattern().replace("myportal", "my\\ portal\\ edited"));
                request.setParameter(String.format(PortalController.PORTAL_ENTRY_POINT_EDIT_REDIRECT_TMPL, entry.getId()), 
                                     "http://www.example.com/edited");
            } // Otherwise delete the entry point.
        }
        
        // Add two new entry points.
        for(int index=0; index<2; index++) {
            request.addParameter("add_portalEntryPoint", String.valueOf(index));
            request.setParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_PATTERN_TMPL, index), 
                                 String.format("^(test){%d}\\/$", index));
            request.setParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_REDIRECT_TMPL, index), 
                                 String.format("http://www.example.com/%d", index));
        }
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/root/portal/listing.htm", redirect.getUrl());
        
        Portal actual = portalDAO.getPortal(portal.getId());
        Assert.assertEquals(request.getParameter("name"), actual.getName());
        Assert.assertEquals(request.getParameter("default"), String.valueOf(actual.isDefault()));
        for(PortalEntryPoint actualEntry : portalDAO.getPortalEntryPoints(actual)) {
            if(actualEntry.getRedirect().endsWith("edited")) {
                Assert.assertEquals(request.getParameter(String.format(PortalController.PORTAL_ENTRY_POINT_EDIT_PATTERN_TMPL, actualEntry.getId())), 
                                    actualEntry.getPattern());
                Assert.assertEquals(request.getParameter(String.format(PortalController.PORTAL_ENTRY_POINT_EDIT_REDIRECT_TMPL, actualEntry.getId())), 
                                    actualEntry.getRedirect());
            } else {
                int start = "http://www.example.com/".length();
                int index = Integer.parseInt(actualEntry.getRedirect().substring(start));
                Assert.assertEquals(request.getParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_PATTERN_TMPL, index)), 
                                    actualEntry.getPattern());
                Assert.assertEquals(request.getParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_REDIRECT_TMPL, index)), 
                                    actualEntry.getRedirect());
            }
        }
    }
    
    @Test
    public void testPortalAdd() throws Exception {
        
        Portal defaultPortal = portalDAO.getPortal(true);
        createTestPortals(false, "");
        defaultPortal.setDefault(true);
        defaultPortal = portalDAO.save(defaultPortal);
        
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/bdrs/root/portal/edit.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "portalEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "portal");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "portalEntryPointList");
        
        Assert.assertNull(((Portal)mv.getModel().get("portal")).getId());
        Assert.assertEquals(0, ((List)mv.getModel().get("portalEntryPointList")).size());
    }
    
    @Test
    public void testPortalAddSubmit() throws Exception {
        
        Portal defaultPortal = portalDAO.getPortal(true);
        createTestPortals(false, "");
        defaultPortal.setDefault(true);
        defaultPortal = portalDAO.save(defaultPortal);
        
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/root/portal/edit.htm");
        request.setParameter("name", "my portal added");
        request.setParameter("default", String.valueOf(true));
        request.setParameter("testUrl", "http://example.com/BDRS/test/");
        
        // Add two new entry points.
        for(int index=0; index<5; index++) {
            request.addParameter("add_portalEntryPoint", String.valueOf(index));
            request.setParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_PATTERN_TMPL, index), 
                                 String.format("^(test){%d}\\/$", index));
            request.setParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_REDIRECT_TMPL, index), 
                                 String.format("http://www.example.com/%d", index));
        }
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/root/portal/listing.htm", redirect.getUrl());
        
        Portal actual = portalDAO.getPortalByName(null, request.getParameter("name"));
        Assert.assertEquals(request.getParameter("default"), String.valueOf(actual.isDefault()));
        for(PortalEntryPoint actualEntry : portalDAO.getPortalEntryPoints(actual)) {
            int start = "http://www.example.com/".length();
            int index = Integer.parseInt(actualEntry.getRedirect().substring(start));
            Assert.assertEquals(request.getParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_PATTERN_TMPL, index)), 
                                actualEntry.getPattern());
            Assert.assertEquals(request.getParameter(String.format(PortalController.PORTAL_ENTRY_POINT_ADD_REDIRECT_TMPL, index)), 
                                actualEntry.getRedirect());
        }
    }
    
    @Test
    public void testAjaxAddPortalEntryPoint() throws Exception {
        
        Portal defaultPortal = portalDAO.getPortal(true);
        createTestPortals(false, "");
        defaultPortal.setDefault(true);
        defaultPortal = portalDAO.save(defaultPortal);
        
        login("root", "password", new String[] { Role.ROOT });
        
        request.setMethod("GET");
        request.setRequestURI("/bdrs/root/portal/ajaxAddPortalEntryPoint.htm");
        request.setParameter("index", "7");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "portalEntryPointRow");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "portalEntryPoint");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "index");
        
        Assert.assertNull(((PortalEntryPoint)mv.getModel().get("portalEntryPoint")).getId());
        Assert.assertEquals(7, mv.getModel().get("index"));
    }
    
    @Test
    public void testPortalInit() throws Exception {
        Portal defaultPortal = portalDAO.getPortal(true);
        Portal portal = new Portal();
        portal.setName("myportal");
        portal.setDefault(false);
        
        portal = portalDAO.save(portal);
    }
    
    @Test
    public void testPortalDoubleInit() throws Exception {
        Portal defaultPortal = portalDAO.getPortal(true);
        Map<String, Preference> prefmap = prefDAO.getPreferences();
        int origCount = prefmap.size();
        ArrayList<String> origKeys = new ArrayList<String>();
        for (Entry<String, Preference> entry : prefmap.entrySet()) {
            origKeys.add(entry.getKey());
            // we need to change the key otherwise all that happens is we get multiple entries with the
            // same key so they aren't exposed beyond the PreferenceDAO interface - I believe the map
            // entry simply gets overwritten. So....
            entry.getValue().setKey(entry.getKey() + "hello");
            prefDAO.save(entry.getValue());
        }
        
        // save the portal which has the init inside of it
        portalDAO.save(defaultPortal);

        Map<String, Preference> prefmap2 = prefDAO.getPreferences();
        
        Assert.assertEquals(origCount, prefmap2.size());      
        for (String key : origKeys) {
            Assert.assertTrue(prefmap2.containsKey(key + "hello"));
        }
    }
    

    private void createTestPortals(boolean includeDefault, String redirectUrl) throws Exception {

        // Make all existing portals non-default
        for (Portal p : portalDAO.getPortals(null)) {
            p.setDefault(false);
            portalDAO.save(p);
        }

        boolean isDefault = includeDefault;
        String contextPath = request.getServletContext().getContextPath();
        List<PortalEntryPoint> entryPointList;
        for (String name : new String[] { "default", "decoy", "other",
                "myportal" }) {

            Portal decoyPortal = new Portal();
            decoyPortal.setDefault(isDefault);
            isDefault = false;
            decoyPortal.setName(name);
            decoyPortal = portalDAO.save(decoyPortal);

            if (decoyPortal.isDefault()) {
                RequestContextHolder.getContext().setPortal(decoyPortal);
                request.setAttribute(PortalSelectionFilter.PORTAL_ID_KEY, decoyPortal.getId());
                Filter filter = sessionFactory.getCurrentSession().getEnabledFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
                filter.setParameter("portalId", decoyPortal.getId());
            }

            entryPointList = new ArrayList<PortalEntryPoint>();
            for (String pattern : new String[] {
                    "^(\\S+)?\\/(%1$s\\/)?(%2$s){1}\\/$",
                    "^(%2$s\\.example\\.com)\\/(%1$s){1}\\/.*$" }) {
                PortalEntryPoint entryPoint = new PortalEntryPoint();
                entryPoint.setPattern(String.format(pattern, contextPath, name));
                entryPoint.setRedirect(redirectUrl);
                entryPoint.setPortal(decoyPortal);
                entryPoint = portalDAO.save(entryPoint);
                entryPointList.add(entryPoint);
            }
        }
    }
}