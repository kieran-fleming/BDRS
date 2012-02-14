package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The <code>MultimediaFacet</code> restricts records depending if
 * it contains a non-empty file or image record attribute.
 */
public class MultimediaFacet extends AbstractFacet {
    
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "multimedia";

    /**
     * Creates a new instance.
     * 
     * @param defaultDisplayName the default human readable name of this facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public MultimediaFacet(String defaultDisplayName, RecordDAO recordDAO,  Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, defaultDisplayName, userParams);
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        String[] selectedOptions = parameterMap.get(getInputName());
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        for(Pair<String, Long> pair : recordDAO.getDistinctAttributeTypes(null, user, new AttributeType[]{AttributeType.FILE, AttributeType.IMAGE})) {
            super.addFacetOption(new MultimediaFacetOption(AttributeType.find(pair.getFirst(), AttributeType.values()), pair.getSecond(), selectedOptions));
        }
    }
}
