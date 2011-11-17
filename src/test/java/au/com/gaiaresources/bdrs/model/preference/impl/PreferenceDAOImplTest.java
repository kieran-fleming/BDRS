package au.com.gaiaresources.bdrs.model.preference.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;

public class PreferenceDAOImplTest extends AbstractControllerTest {
    
    @Autowired
    PreferenceDAO prefDAO;
    
    Preference pref1;
    Preference pref2;
    PreferenceCategory cat;
    PreferenceCategory cat2;
    
    Portal anotherPortal;
    
    @Before
    public void setup() throws Exception {
        cat = new PreferenceCategory();
        cat.setDescription("cat desc");
        cat.setDisplayName("catdisplayname");
        cat.setName("catname");
        
        cat2 = new PreferenceCategory();
        cat2.setDescription("cat desc");
        cat2.setDisplayName("catdisplayname");
        cat2.setName("catnametwo");
        
        pref1 = new Preference();
        pref1.setDescription("desc1");
        pref1.setIsRequired(false);
        pref1.setKey(Preference.GOOGLE_MAP_KEY_PREFIX + "my_key");
        pref1.setLocked(false);
        pref1.setValue("www.domain.com,ABQIAAAAZlF008vltzKxmUQHWEa5NhTI9lENksmi9WbNk4V9cfHLKZK1bRRNv_6X-JTe6SLnWqTfqfjvALWaAQ");
        pref1.setPreferenceCategory(cat);
        
        pref2 = new Preference();
        pref2.setDescription("desc2");
        pref2.setIsRequired(false);
        pref2.setKey(Preference.GOOGLE_MAP_KEY_PREFIX + "second_key");
        pref2.setLocked(false);
        pref2.setValue("second.domain.com.au,ABQIAAAAZlF008vltzKxmUQHWEa5NhTI9lENksmi9WbNk4V9cfHLKZK1bRRNv_6X-JTe6SLnWqTfqfjvALWaAQ");
        pref2.setPreferenceCategory(cat);
        
        prefDAO.save(null, cat);
        prefDAO.save(null, cat2);
        prefDAO.save(null, pref1);
        prefDAO.save(null, pref2);
        
        anotherPortal = new Portal();
        anotherPortal.setName("another portal");
        anotherPortal = portalDAO.save(anotherPortal);
    }
    
    @Test
    public void testGetByKeyPrefix() {
        List<Preference> result = prefDAO.getPreferenceByKeyPrefix(Preference.GOOGLE_MAP_KEY_PREFIX);
        Assert.assertEquals(2, result.size());
        
        Assert.assertTrue(result.contains(pref1));
        Assert.assertTrue(result.contains(pref2));
    }
    
    @Test
    public void testGetAllPrefCats() {
        List<PreferenceCategory> result = prefDAO.getPreferenceCategories();
        // dont assert the size as we have some preference categories seeded as part of the test setup stuff
        Assert.assertTrue(result.contains(cat));
        Assert.assertTrue(result.contains(cat2));
    }
    
    @Test
    public void testGetPrefCatByNameAndPortal() {
        PreferenceCategory cat = prefDAO.getPreferenceCategoryByName(null, "catname", this.defaultPortal);
        Assert.assertNotNull("pref cat should be returned", cat);
        
        PreferenceCategory cat2 = prefDAO.getPreferenceCategoryByName(null, "sdfkfkjhsdfjkg", this.defaultPortal);
        Assert.assertNull("no pref cat should be returned", cat2);
        
        PreferenceCategory cat3 = prefDAO.getPreferenceCategoryByName(null, "catname", this.anotherPortal);
        Assert.assertNull("no pref cat should be returned", cat3);
    }
    
    @Test
    public void testGetPrefByKeyAndPortal() {
        Preference pref = prefDAO.getPreferenceByKey(null, Preference.GOOGLE_MAP_KEY_PREFIX + "my_key", this.defaultPortal);
        Assert.assertNotNull("pref should be returned", pref);
        
        Preference badPref = prefDAO.getPreferenceByKey(null, "sadlfkjsdafjklsadf", this.defaultPortal);
        Assert.assertNull("no pref should be returned", badPref);
        
        Preference badPref2 = prefDAO.getPreferenceByKey(null, Preference.GOOGLE_MAP_KEY_PREFIX + "my_key", anotherPortal);
        Assert.assertNull("no pref should be returned", badPref2);
    }
}
