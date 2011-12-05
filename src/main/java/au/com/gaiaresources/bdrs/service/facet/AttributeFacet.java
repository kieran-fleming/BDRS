package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import edu.emory.mathcs.backport.java.util.Arrays;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import net.sf.json.JSONObject;

/**
 * Creates a {@link Facet} for showing records by attribute values.
 * @author stephanie
 */
public class AttributeFacet extends AbstractFacet {
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "attribute_%s";
    
    /**
     * The name of the attribute that this facet will filter by.
     */
    private String attributeName;
    
    private int facetIndex;
    
    /**
     * Creates an instance of this facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public AttributeFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap, User user,
            JSONObject userParams, int facetIndex) {
        super(String.format(QUERY_PARAM_NAME, userParams.getString("attributeName")), 
              userParams.getString("attributeName"), userParams);
        this.attributeName = userParams.getString("attributeName");
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
        for(Pair<String, Long> pair : recordDAO.getDistinctAttributeValues(null, user, this.attributeName, userParams.optInt("optionCount"))) {
            super.addFacetOption(new StringAttributeFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions, facetIndex));
        }
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.AbstractFacet#getPredicate()
     */
    @Override
    public Predicate getPredicate() {
        Predicate facetPredicate = super.getPredicate();
        
        if(facetPredicate != null) {
            return Predicate.enclose(facetPredicate.and(
                                     Predicate.eq("attribute"+facetIndex+".description", this.attributeName)));
        } else {
            return null;
        }
    }

    public int getFacetIndex() {
        return facetIndex;
    }
}
