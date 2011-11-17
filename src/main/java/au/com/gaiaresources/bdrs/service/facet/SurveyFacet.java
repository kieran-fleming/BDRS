package au.com.gaiaresources.bdrs.service.facet;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents records on a per survey basis.
 */
public class SurveyFacet extends AbstractFacet {
    public static final String SURVEY_ID_QUERY_PARAM_NAME = "surveyId";
    
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "survey";
    /**
     * The human readable name of this facet.
     */
    public static final String DISPLAY_NAME = "Survey";

    /**
     * Creates a new instance.
     * 
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public SurveyFacet(RecordDAO recordDAO,  Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, userParams);
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        if(parameterMap.get(SURVEY_ID_QUERY_PARAM_NAME) != null && 
            parameterMap.get(SURVEY_ID_QUERY_PARAM_NAME).length == 1) {
            
            try {
                Integer.parseInt(parameterMap.get(SURVEY_ID_QUERY_PARAM_NAME)[0]);
                setActive(false);
            } catch(NumberFormatException nfe) {
                setActive(true);
            }
        }
        
        if(isActive()) {
            String[] selectedOptions = parameterMap.get(getInputName());
            if(selectedOptions == null) {
                selectedOptions = new String[]{};
            }
            Arrays.sort(selectedOptions);
            
            for(Pair<Survey, Long> pair : recordDAO.getDistinctSurveys(null, user)) {
                super.addFacetOption(new SurveyFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
            }
        }
    }
}
