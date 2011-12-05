package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import edu.emory.mathcs.backport.java.util.Arrays;

import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import net.sf.json.JSONObject;

/**
 * Creates a {@link Facet} for showing records by location.  This will allow only 
 * records for a given location to be shown.  
 * @author stephanie
 */
public class LocationFacet extends AbstractFacet {
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "location";
    /**
     * The human readable name of this facet.
     */
    public static final String DISPLAY_NAME = "Locations";
    
    /**
     * Limits the number of options to show in the facet.
     */
    public static final int OPTIONS_LIMIT = 10;
    
    /**
     * Creates a Location Facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public LocationFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, userParams);
        
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        String[] selectedOptions = parameterMap.get(getInputName());
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        for(Pair<Location, Long> pair : recordDAO.getDistinctLocations(null, user, OPTIONS_LIMIT)) {
            super.addFacetOption(new LocationFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
        }
    }

}
