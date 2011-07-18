package au.com.gaiaresources.bdrs.model.preference.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
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
    
    @Before
    public void setup() {
        cat = new PreferenceCategory();
        cat.setDescription("cat desc");
        cat.setDisplayName("catdisplayname");
        cat.setName("catname");
        
        cat2 = new PreferenceCategory();
        cat2.setDescription("cat desc");
        cat2.setDisplayName("catdisplayname");
        cat2.setName("catname");
        
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
}
