package au.com.gaiaresources.bdrs.service.facet;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONException;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.builder.AttributeFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.CensusMethodTypeFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.LocationAttributeFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.LocationFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.ModerationFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.MonthFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.MultimediaFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.SurveyFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.TaxonGroupFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.UserFacetBuilder;
import au.com.gaiaresources.bdrs.service.facet.builder.YearFacetBuilder;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * The Facet Service is the one stop shop for retrieving Facets. The FacetService
 * is responsible for instantiating Facets and ensuring that they have valid
 * Preferences.
 */
@Service
public class FacetService {
    /**
     * The name of the {@link PreferenceCategory} where facet related preferences
     * shall be grouped.
     */
    public static final String FACET_CATEGORY_NAME = "category.facets";
    
    /**
     * An unmodifiable list of all {@link FacetBuilder}s that shall be used
     * in creating default preferences and instantiating new {@link Facet}s.
     */
    public static final List<FacetBuilder> FACET_BUILDER_REGISTRY;
    
    static {
        List<FacetBuilder> temp = new ArrayList<FacetBuilder>();
        
        temp.add(new UserFacetBuilder());
        temp.add(new TaxonGroupFacetBuilder());
        temp.add(new MonthFacetBuilder());
        temp.add(new YearFacetBuilder());
        temp.add(new LocationFacetBuilder());
        temp.add(new SurveyFacetBuilder());
        temp.add(new MultimediaFacetBuilder());
        temp.add(new CensusMethodTypeFacetBuilder());
        temp.add(new AttributeFacetBuilder());
        temp.add(new ModerationFacetBuilder());
        temp.add(new LocationAttributeFacetBuilder());
        
        FACET_BUILDER_REGISTRY = Collections.unmodifiableList(temp);
    }
    
    private Logger log = Logger.getLogger(getClass());
    @Autowired
    private PreferenceDAO prefDAO;
    @Autowired
    private RecordDAO recordDAO;
    
    /**
     * Generates the {@link List} of {@link Facet}s. Each facet will be configured
     * with the necessary {@link FacetOption}s and selection state.
     * @param user the user requesting the record list.
     * @param parameterMap a mapping of query parameters.
     * @return the ordered {@link List} of {@link Facet}s. 
     */
    public List<Facet> getFacetList(User user, Map<String, String[]> parameterMap) {
        List<Facet> facetList = new ArrayList<Facet>();
        for(FacetBuilder builder : FACET_BUILDER_REGISTRY) {

            Preference pref = prefDAO.getPreferenceByKey(builder.getPreferenceKey());
            if(pref == null) {
                log.error("Cannot create facet. Cannot find preference with key: "+builder.getPreferenceKey());
            } else {
                try {
                    JSONArray configArray = JSONArray.fromString(pref.getValue());
                    for(int i=0; i<configArray.size(); i++) {
                        try {
                            JSONObject configParams = configArray.getJSONObject(i);
                            configParams.put(Facet.JSON_PREFIX_KEY, String.valueOf(i));
                            
                            Facet facet = builder.createFacet(recordDAO, parameterMap, user, configParams);
                            facetList.add(facet);
                        } catch(JSONException ex) {
                            log.error(String.format("The configuration parameter at index %d for preference key %s is not a JSON object or is improperly configured: %s", i, pref.getKey(), pref.getValue()), ex);
                        }
                    }
                } catch(JSONException je) {
                    // Improperly configured JSON String.
                    log.error("Improperly configured JSON String for preference key: "+pref.getKey());
                }
            } 
        }
        Collections.sort(facetList, new FacetWeightComparator());
        return facetList;
    }
    
    /**
     * Gets a facet by type from the specified list of facets.
     * @param facetList - the list of facets to search.
     * @param facetClazz - the facet class to return
     * @return the facet if found, otherwise null
     */
    public <C extends Facet> C getFacetByType(List<Facet> facetList, Class<C> facetClazz) {
        for (Facet f : facetList) {
            if (f.getClass().equals(facetClazz)) {
                return (C)f;
            }
        }
        return null;
    }
    
    /**
     * Performs the creation of default preferences for each facet if one 
     * does not already exist.
     * @param sesh the session used to get and/or create Preferences.
     * @param portal the portal to be associated with the created preferences.
     * @params category the category that contains facet preferences.
     */
    public void initFacetPreferences(Session sesh, Portal portal, PreferenceCategory category) {
        for(FacetBuilder builder : FACET_BUILDER_REGISTRY) {
            Preference pref = prefDAO.getPreferenceByKey(sesh, builder.getPreferenceKey(), portal);
            if(pref == null) {
                pref = builder.getDefaultPreference(portal, category);
                prefDAO.save(sesh, pref);
            }
        }
    }
}
