package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The <code>CensusMethodTypeFacet</code> restricts records to the type of the
 * associated census method or a null census method. 
 */
public class CensusMethodTypeFacet extends AbstractFacet {
    
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "censusMethod";

    /**
     * Creates a new instance.
     * 
     * @param defaultDisplayName the default human readable name of this facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public CensusMethodTypeFacet(String defaultDisplayName, RecordDAO recordDAO,  Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, defaultDisplayName, userParams);
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        String[] selectedOptions = parameterMap.get(getInputName());
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        // Special entry for null census methods. (Observation Type)
        
        Long count = Long.valueOf(recordDAO.countNullCensusMethodRecords());
        super.addFacetOption(new CensusMethodTypeFacetOption(count, selectedOptions));
        
        // All other situations
        for(Pair<String, Long> pair : recordDAO.getDistinctCensusMethodTypes(null, user)) {
            super.addFacetOption(new CensusMethodTypeFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
        }
    }
}
