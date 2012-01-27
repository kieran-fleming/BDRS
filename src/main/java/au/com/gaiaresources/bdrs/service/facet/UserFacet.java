package au.com.gaiaresources.bdrs.service.facet;

import java.util.List;
import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
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
     * A limit for the number of options to show in the facet.
     */
    private static final Integer OPTIONS_LIMIT = 8;
    
    /**
     * Creates a new instance.
     * 
     * @param defaultDisplayName the default human readable name of this facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public UserFacet(String defaultDisplayName, RecordDAO recordDAO,  Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, defaultDisplayName, userParams);
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        String[] selectedOptions = parameterMap.get(getInputName());
        if(selectedOptions == null) {
            if (user != null) {
                // select "My Records Only" by default when there is a user
                selectedOptions = new String[]{String.valueOf(user.getId())};
            } else {
                // select "All Public Records" by default when user is null (anonymous view)
                selectedOptions = new String[]{String.valueOf(-1)};
            }
        }
        Arrays.sort(selectedOptions);
        
        Long count = Long.valueOf(recordDAO.countAllRecords(user));
        
        // add the public records item as the first thing in the list
        super.addFacetOption(new AllPublicRecordsUserFacetOption(user, count, selectedOptions));
        
        int userCount = 0;
        List<Pair<User, Long>> userCounts = recordDAO.getDistinctUsers(null, user);
        for(Pair<User, Long> pair : userCounts) {
            if (pair.getFirst().equals(user)) {
                super.insertFacetOption(new UserFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions), 0);
            } else if (userCount < OPTIONS_LIMIT) {
                super.addFacetOption(new UserFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
                userCount++;
            }
        }
        
        // if the user is not null (anonymous view) and
        // if the options are not the min of the limit or the count + 2 for my records and all public records
        // the user has no records and has not been added, so add an option for their 0 records now
        if (user != null && getFacetOptions().size() < Math.min(OPTIONS_LIMIT, userCount) + 2) {
            super.insertFacetOption(new UserFacetOption(user, Long.valueOf(0), selectedOptions), 0);
        }
    }
}
