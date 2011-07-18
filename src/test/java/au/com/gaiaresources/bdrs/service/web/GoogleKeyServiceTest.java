package au.com.gaiaresources.bdrs.service.web;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;

public class GoogleKeyServiceTest extends AbstractControllerTest {
    
    @Autowired
    GoogleKeyService gkservice;
    @Autowired
    PreferenceDAO prefDAO;
    
    @Test
    public void testFromPropertyFile() throws IOException {
        // Don't set up properties to force reading from file...
        String key = gkservice.getGoogleMapApiKey("myhostname");
        Assert.assertEquals("testkeyvalue", key);
    }
    
    @Test
    public void testFromPreference() throws IOException {
        PreferenceCategory cat = new PreferenceCategory();
        cat.setDescription("cat desc");
        cat.setDisplayName("catdisplayname");
        cat.setName("catname");
        
        Preference pref1 = new Preference();
        pref1.setDescription("desc1");
        pref1.setIsRequired(false);
        pref1.setKey(Preference.GOOGLE_MAP_KEY_PREFIX + "my_key");
        pref1.setLocked(false);
        pref1.setValue("www.domain.com,ABQIAAAAZlF008vltzKxmUQHWEa5NhRaOKH3sAO32VjZbMIdlTqlXV415xRG9hF_uu0AFKHptaJtEIEmZRJxLg");
        pref1.setPreferenceCategory(cat);
        
        Preference pref2 = new Preference();
        pref2.setDescription("desc2");
        pref2.setIsRequired(false);
        pref2.setKey(Preference.GOOGLE_MAP_KEY_PREFIX + "second_key");
        pref2.setLocked(false);
        pref2.setValue("myhostname.wooo.com,ABQIAAAAZlF008vltzKxmUQHWEa5NhTI9lENksmi9WbNk4V9cfHLKZK1bRRNv_6X-JTe6SLnWqTfqfjvALWaAQ");
        pref2.setPreferenceCategory(cat);
        
        prefDAO.save(null, cat);
        prefDAO.save(null, pref1);
        prefDAO.save(null, pref2);
  
        String googleKey = gkservice.getGoogleMapApiKey("www.domain.com");
        Assert.assertEquals("ABQIAAAAZlF008vltzKxmUQHWEa5NhRaOKH3sAO32VjZbMIdlTqlXV415xRG9hF_uu0AFKHptaJtEIEmZRJxLg", googleKey);
    }
    
    @Test
    public void testFromPreferenceBadHostname() throws IOException {
        PreferenceCategory cat = new PreferenceCategory();
        cat.setDescription("cat desc");
        cat.setDisplayName("catdisplayname");
        cat.setName("catname");
        
        Preference pref1 = new Preference();
        pref1.setDescription("desc1");
        pref1.setIsRequired(false);
        pref1.setKey(Preference.GOOGLE_MAP_KEY_PREFIX + "my_key");
        pref1.setLocked(false);
        pref1.setValue("www.domain.com,ABQIAAAAZlF008vltzKxmUQHWEa5NhRaOKH3sAO32VjZbMIdlTqlXV415xRG9hF_uu0AFKHptaJtEIEmZRJxLg");
        pref1.setPreferenceCategory(cat);
        
        Preference pref2 = new Preference();
        pref2.setDescription("desc2");
        pref2.setIsRequired(false);
        pref2.setKey(Preference.GOOGLE_MAP_KEY_PREFIX + "second_key");
        pref2.setLocked(false);
        pref2.setValue("myhostname.wooo.com,ABQIAAAAZlF008vltzKxmUQHWEa5NhTI9lENksmi9WbNk4V9cfHLKZK1bRRNv_6X-JTe6SLnWqTfqfjvALWaAQ");
        pref2.setPreferenceCategory(cat);
        
        prefDAO.save(null, cat);
        prefDAO.save(null, pref1);
        prefDAO.save(null, pref2);
  
        String googleKey = gkservice.getGoogleMapApiKey("hostname.does.not.exist.com");
        Assert.assertNull(googleKey);
    }
}
