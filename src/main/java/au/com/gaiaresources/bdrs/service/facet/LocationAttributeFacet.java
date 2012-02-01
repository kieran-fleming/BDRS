package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import org.apache.log4j.Logger;

import edu.emory.mathcs.backport.java.util.Arrays;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import net.sf.json.JSONObject;

/**
 * Creates a {@link Facet} for showing records by location attribute values.
 */
public class LocationAttributeFacet extends AbstractFacet {
    
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "loc_attribute_%s";
    
    /**
     * The expected JSON key from user preferences indicating the 
     * name of the attribute to be queried in the predicate. 
     */
    public static final String JSON_ATTRIBUTE_NAME_KEY = "attributeName";
    
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * The name of the attribute that this facet will filter by.
     */
    private String attributeName;
    
    private int facetIndex;
    
    /**
     * Creates an instance of this facet.
     * 
     * @param defaultDisplayName the default human readable name of this facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public LocationAttributeFacet(String defaultDisplayName, RecordDAO recordDAO, Map<String, String[]> parameterMap, User user,
            JSONObject userParams, int facetIndex) {
        // The query param name being passed to the super constructor here is
        // just a placeholder. We need to check if the 'attributeName' attribute
        // exists in the userParms. If it does not exist, the facet will be
        // deactivated.
        super(String.format(QUERY_PARAM_NAME, userParams.optString(JSON_ATTRIBUTE_NAME_KEY, "")), 
              defaultDisplayName, userParams);
        
        if(userParams.has(JSON_ATTRIBUTE_NAME_KEY)) {
            this.attributeName = userParams.getString(JSON_ATTRIBUTE_NAME_KEY);
            this.facetIndex = facetIndex;
            
            setContainsSelected(parameterMap.containsKey(getInputName()));
            
            String[] selectedOptions = parameterMap.get(getInputName());
            if(selectedOptions == null) {
                selectedOptions = new String[]{};
            }
            Arrays.sort(selectedOptions);
            
            // for now this just handles String type attributes, 
            // later it should retrieve attribute objects vs count
            // and determine which type of attribute options to add 
            // based on the type of the attribute
            for(Pair<String, Long> pair : recordDAO.getDistinctLocationAttributeValues(null, user, this.attributeName, userParams.optInt("optionCount"))) {
                super.addFacetOption(new LocationAttributeFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions, facetIndex));
            }
        } else {
            // The JSON object is malformed.
            super.setActive(false);
            log.info(String.format("Deactivating the LocationAttributeFacet because the JSON configuration is missing the \"%s\" attribute.", JSON_ATTRIBUTE_NAME_KEY));
        }
    }

    @Override
    public Predicate getPredicate() {
        Predicate facetPredicate = super.getPredicate();
        
        if(facetPredicate != null) {
            return Predicate.enclose(facetPredicate.and(
                                     Predicate.eq("locAttribute"+facetIndex+".description", this.attributeName)));
        } else {
            return null;
        }
    }

    @Override
    public void applyCustomJoins(HqlQuery query) {
        super.applyCustomJoins(query);
        // create an additional join to the attributes/attribute 
        // tables to accomodate multiple attribute values
        query.leftJoin("location.attributes", "locAttributeVal" + facetIndex);
        query.leftJoin("locAttributeVal" + facetIndex + ".attribute", "locAttribute" + facetIndex);
    }
}
