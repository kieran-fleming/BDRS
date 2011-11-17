package au.com.gaiaresources.bdrs.service.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents a single survey containing records. 
 */
public class SurveyFacetOption extends FacetOption {

    private Survey survey;

    /**
     * Creates a new instance of this class.
     * 
     * @param survey the human readable name of this option.
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public SurveyFacetOption(Survey survey, Long count, String[] selectedOpts) {
        super(survey.getName(), String.valueOf(survey.getId()), count, 
              Arrays.binarySearch(selectedOpts, String.valueOf(survey.getId())) > -1);
        
        this.survey = survey;           
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        return Predicate.eq("record.survey.id", survey.getId());
    }
}
