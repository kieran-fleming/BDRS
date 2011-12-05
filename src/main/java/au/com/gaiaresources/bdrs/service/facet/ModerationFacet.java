package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.impl.CountRecordFilter;
import au.com.gaiaresources.bdrs.model.record.impl.RecordFilter;
import au.com.gaiaresources.bdrs.model.user.User;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Restricts records by moderation status.
 * @author stephanie
 */
public class ModerationFacet extends AbstractFacet {
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "held";
    /**
     * The human readable name of this facet.
     */
    public static final String DISPLAY_NAME = "Moderation Status";

    /**
     * Creates a new instance.
     * 
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public ModerationFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap, User user, JSONObject userParams) {
          super(QUERY_PARAM_NAME, DISPLAY_NAME, userParams);
          
          if (user == null || !user.isModerator()) {
              setActive(false);
          }
          
          setContainsSelected(parameterMap.containsKey(getInputName()));
          
          String[] selectedOptions = parameterMap.get(getInputName());
          if(selectedOptions == null) {
              selectedOptions = new String[]{};
          }
          Arrays.sort(selectedOptions);
          
          RecordFilter filter = new CountRecordFilter();
          filter.setAccessor(user);
          filter.setHeld(true);
          int count = recordDAO.countRecords(filter);
          super.addFacetOption(new ModerationFacetOption(QUERY_PARAM_NAME, Long.valueOf(count), selectedOptions.length > 0));
    }
}
