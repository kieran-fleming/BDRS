package au.com.gaiaresources.bdrs.service.facet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;
import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;
import edu.emory.mathcs.backport.java.util.Collections;

public class FacetServiceTest extends AbstractTransactionalTest {
    
    public static final Map<String, String[]> EMPTY_PARAMETER_MAP;
    static {
        Map<String, String[]> temp = new HashMap<String, String[]>();
        EMPTY_PARAMETER_MAP = Collections.unmodifiableMap(temp);
    }
    
    @Autowired
    private FacetService facetService;
    
    @Autowired
    private PreferenceDAO prefDAO;
    
    @Autowired
    private UserDAO userDAO;
    
    @Test
    public void testBasicFaceting() {
        User user = userDAO.getUser("admin");
        List<Facet> facetList = facetService.getFacetList(user, EMPTY_PARAMETER_MAP);
        
        // We have the right number of facets
        Assert.assertEquals(FacetService.FACET_BUILDER_REGISTRY.size(), facetList.size());
        
        // Facets are correctly initialised with the default values except
        // for the attribute facet.
        for(Facet facet : facetList) {
            if(facet instanceof AttributeFacet || facet instanceof LocationAttributeFacet) {
                Assert.assertEquals(false, facet.isActive());
                Assert.assertEquals(Facet.DEFAULT_WEIGHT_CONFIG, facet.getWeight());
            } else {
                Assert.assertEquals(Facet.DEFAULT_ACTIVE_CONFIG, facet.isActive());
                Assert.assertEquals(Facet.DEFAULT_WEIGHT_CONFIG, facet.getWeight());
            }
        }
        
        // Test that there are the right number of instances of each facet.
        PreferenceCategory category = prefDAO.getPreferenceCategoryByName(sesh, FacetService.FACET_CATEGORY_NAME, defaultPortal);
        Map<Class<? extends Facet>, List<Facet>> facetTypes = createFacetTypeMapping(facetList);
        for(FacetBuilder bob : FacetService.FACET_BUILDER_REGISTRY) {
            Preference pref = bob.getDefaultPreference(defaultPortal, category);
            JSONArray userConfig = JSONArray.fromString(pref.getValue());
            // config for AttributeFacet will be null and so will facet types for 
            // that class so skip them
            if (userConfig != null && facetTypes.get(bob.getFacetClass()) != null) {
                Assert.assertEquals(userConfig.size(), facetTypes.get(bob.getFacetClass()).size());
            }
        }
    }
    
    @Test
    public void testFacetSorting() {
        int weight = 1234;
        boolean addOffset = true;
        for(FacetBuilder builder : FacetService.FACET_BUILDER_REGISTRY) {
            Preference pref = prefDAO.getPreferenceByKey(builder.getPreferenceKey());
            JSONArray userConfigArray = JSONArray.fromString(pref.getValue());
            for(int i=0; i<userConfigArray.size(); i++) {
                JSONObject userConfig = userConfigArray.getJSONObject(i);
                userConfig.put(Facet.JSON_WEIGHT_KEY, weight);
                
                weight += addOffset ? 17 : -9;
                addOffset = !addOffset;
            }
            
            weight += addOffset ? 17 : -9;
            addOffset = !addOffset;
            
            pref.setValue(userConfigArray.toString());
            prefDAO.save(pref);
        }
        
        User user = userDAO.getUser("admin");
        int curWeight = Integer.MIN_VALUE;
        for(Facet facet : facetService.getFacetList(user, EMPTY_PARAMETER_MAP)) {
            Assert.assertTrue(facet.getWeight() >= curWeight);
            curWeight = facet.getWeight();
        }
    }
    
    @Test
    public void testMultiInstanceFacet() {
        User user = userDAO.getUser("admin");
        int originalFacetListSize = facetService.getFacetList(user, EMPTY_PARAMETER_MAP).size();

        // Triple the number of instances in the preference value.
        for(FacetBuilder builder : FacetService.FACET_BUILDER_REGISTRY) {
            Preference pref = prefDAO.getPreferenceByKey(builder.getPreferenceKey());
            JSONArray userConfigArray = JSONArray.fromString(pref.getValue());
            int targetCount = 3 * userConfigArray.size();
            for(int i=userConfigArray.size(); i<targetCount; i++) {
                // Implicitly this also tests the default value feature.
                userConfigArray.add(new JSONObject());
            }
            pref.setValue(userConfigArray.toString());
            prefDAO.save(pref);
        }
        
        List<Facet> facetList = facetService.getFacetList(user, EMPTY_PARAMETER_MAP);
        Assert.assertEquals(3 * originalFacetListSize, facetList.size());
        
        // Test that each facet instance of the same type has a unique prefix and name
        Set<String> inputNameSet = new HashSet<String>();
        for(Facet facet : facetList) {
            // The input name is a combination of the prefix and the facet query param name.
            inputNameSet.add(facet.getInputName());
        }
        
        Assert.assertEquals(facetList.size(), inputNameSet.size());
        
    }
    
    @Test
    public void testInvalidUserConfig() {
        for(FacetBuilder builder : FacetService.FACET_BUILDER_REGISTRY) {
            Preference pref = prefDAO.getPreferenceByKey(builder.getPreferenceKey());
            pref.setValue("abcdef");
            prefDAO.save(pref);
        }
        
        User user = userDAO.getUser("admin");
        for(Facet facet : facetService.getFacetList(user, EMPTY_PARAMETER_MAP)) {
            Assert.assertEquals(false, facet.isActive());
        }
    }
    
    @Test
    public void testGetFacetByType() {
        User user = userDAO.getUser("admin");
        List<Facet> facetList = facetService.getFacetList(user, EMPTY_PARAMETER_MAP);
        Assert.assertTrue("Expect facet to be of type SurveyFacet", facetService.getFacetByType(facetList, SurveyFacet.class) instanceof SurveyFacet);
        Assert.assertTrue("Expect facet to be of type MonthFacet", facetService.getFacetByType(facetList, MonthFacet.class) instanceof MonthFacet);
    }
    
    @Test
    public void testFacetCustomNames() {
        Map<Class<? extends Facet>, String> facetLookup = new HashMap<Class<? extends Facet>, String>();
        
        for(int i=0; i<FacetService.FACET_BUILDER_REGISTRY.size(); i++) {
            FacetBuilder builder = FacetService.FACET_BUILDER_REGISTRY.get(i);
            Preference pref = prefDAO.getPreferenceByKey(builder.getPreferenceKey());
            JSONArray array = JSONArray.fromString(pref.getValue());
            String customName = String.format("Custom Name %s", pref.getId().toString());
            for(int j=0; j<array.size(); j++) {
                JSONObject obj = array.getJSONObject(j);
                obj.put(Facet.JSON_NAME_KEY, customName);
            }
            
            pref.setValue(array.toString());
            prefDAO.save(pref);
            
            facetLookup.put(builder.getFacetClass(), customName);
        }
        
        User user = userDAO.getUser("admin");
        List<Facet> facetList = facetService.getFacetList(user, EMPTY_PARAMETER_MAP);
        for(Facet facet : facetList) {
            Assert.assertEquals(facetLookup.get(facet.getClass()), facet.getDisplayName());
        }
    }
    
    private Map<Class<? extends Facet>, List<Facet>> createFacetTypeMapping(List<Facet> facetList) {
        Map<Class<? extends Facet>, List<Facet>> facetTypes = new HashMap<Class<? extends Facet>, List<Facet>>();
        for(Facet facet : facetList) {
            List<Facet> facetTypeList = facetTypes.get(facet.getClass());
            if(facetTypeList == null) {
                facetTypeList = new ArrayList<Facet>();
                facetTypes.put(facet.getClass(), facetTypeList);
            }
            facetTypeList.add(facet);
        }
        
        return facetTypes;
    }
}
