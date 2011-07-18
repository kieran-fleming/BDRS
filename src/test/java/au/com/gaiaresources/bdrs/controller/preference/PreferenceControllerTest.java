package au.com.gaiaresources.bdrs.controller.preference;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.preference.impl.PreferenceDAOImpl;
import au.com.gaiaresources.bdrs.security.Role;

public class PreferenceControllerTest extends AbstractControllerTest {
    
    @Autowired
    private PreferenceDAO prefDAO;
       
    @Before
    public void setUp() throws Exception {
        // Force reinitialisation of the DAO.
        // This causes the cache to be recreated from scratch.
        ((PreferenceDAOImpl)prefDAO).init(sessionFactory.getCurrentSession());
    }
    
    @Test
    public void testListPreferences() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/preference/preference.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "preference");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "categoryMap");
        
        List<PreferenceCategory> allCats = prefDAO.getPreferenceCategories();
        Map<PreferenceCategory, List<Preference>> categoryMap = (Map<PreferenceCategory, List<Preference>>)mv.getModel().get("categoryMap");
        Assert.assertEquals(allCats.size(), categoryMap.size());
    }
    
    @Test
    public void testEditPreferences() throws Exception {
        // Modify value and description of existing prefs.
        Map<String, String> expectedPrefDescMap = new HashMap<String, String>();
        Map<String, String> expectedPrefValueMap = new HashMap<String, String>();
        for(Preference pref : prefDAO.getPreferences().values()) {
            String modifiedValue = pref.getValue()+"edit";
            String modifiedDesc = pref.getDescription()+"edit";
            
            request.addParameter("preference_id", pref.getId().toString());
            request.addParameter(String.format("preference_key_%d", pref.getId()), pref.getKey());
            request.addParameter(String.format("preference_value_%d", pref.getId()), modifiedValue);
            request.addParameter(String.format("preference_description_%d", pref.getId()), modifiedDesc);
            
            expectedPrefDescMap.put(pref.getKey(), modifiedDesc);
            expectedPrefValueMap.put(pref.getKey(), modifiedValue);
        }

        // Just get the first category.
        PreferenceCategory cat = prefDAO.getPreferences().values().iterator().next().getPreferenceCategory();
        
        // Added Preferences
        for(int i=0; i<2; i++) {
            request.addParameter("add_preference", String.format("%s",i));
            request.addParameter(String.format("add_preference_category_%d", i), cat.getId().toString());
            request.addParameter(String.format("add_preference_key_%d", i), "Herp.Derp"+i);
            request.addParameter(String.format("add_preference_value_%d", i), "Derrrrp"+i);
            request.addParameter(String.format("add_preference_description_%d", i), "The Derp that was Herped."+i);
        }
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/preference/preference.htm");
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/admin/preference/preference.htm", redirect.getUrl());
        
        // Assert edited prefs
        for(Map.Entry<String, String> expectedValueEntry : expectedPrefValueMap.entrySet()) {
            Preference pref = prefDAO.getPreferenceByKey(expectedValueEntry.getKey());
            Assert.assertEquals(expectedValueEntry.getValue(), pref.getValue()); 
        }
        for(Map.Entry<String, String> expectedDescEntry : expectedPrefDescMap.entrySet()) {
            Preference pref = prefDAO.getPreferenceByKey(expectedDescEntry.getKey());
            Assert.assertEquals(expectedDescEntry.getValue(), pref.getDescription()); 
        }
        
        // Assert added prefs
        for(int i=0; i<2; i++) {
            Preference pref = prefDAO.getPreferenceByKey(request.getParameter(String.format("add_preference_key_%d", i)));
            
            Assert.assertEquals(request.getParameter(String.format("add_preference_key_%d", i)), pref.getKey());
            Assert.assertEquals(request.getParameter(String.format("add_preference_category_%d", i)), pref.getPreferenceCategory().getId().toString());
            Assert.assertEquals(request.getParameter(String.format("add_preference_value_%d", i)), pref.getValue());
            Assert.assertEquals(request.getParameter(String.format("add_preference_description_%d", i)), pref.getDescription());
        }
        
        for(Preference pref : prefDAO.getPreferences().values()) {
            Assert.assertNotNull(pref.getPortal());
        }
    }
    
    @Test
    public void testDeletePreferences() throws Exception {
        // Post an empty dictionary
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/preference/preference.htm");
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/admin/preference/preference.htm", redirect.getUrl());
        
        for(Preference pref : prefDAO.getPreferences().values()) {
            Assert.assertNull(pref.getPortal());
        }
    }
}
