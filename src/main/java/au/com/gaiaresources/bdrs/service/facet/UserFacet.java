package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The <code>UserFacet</code> restricts records to the selected set of users. 
 */
public class UserFacet extends AbstractFacet {
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "user";
    /**
     * The human readable name of this facet.
     */
    public static final String DISPLAY_NAME = "User";
    
    /**
     * Creates a new instance.
     * 
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public UserFacet(RecordDAO recordDAO,  Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, userParams);
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        String[] selectedOptions = parameterMap.get(getInputName());
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        Long count = Long.valueOf(recordDAO.countRecords(user));
        if (user != null) {
            // add the current user as a facet option if there is one
            super.addFacetOption(new UserFacetOption(user, count, selectedOptions));
        } else {
            // set the isActive flag to prevent the facet from showing
            setActive(false);
            // create an anonymous user facet option for no user
            super.addFacetOption(new AnonymousUserFacetOption(count, true));
        }
    }
}
