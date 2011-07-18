package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents a single survey containing records. 
 */
public class SurveyFacetOption extends FacetOption {

    private Survey survey;

    public SurveyFacetOption(Survey survey, Long count, String[] selectedOpts) {
        super(survey.getName(), String.valueOf(survey.getId()), count, 
              Arrays.binarySearch(selectedOpts, String.valueOf(survey.getId())) > -1);
        
        this.survey = survey;           
    }

    public Predicate getPredicate() {
        return Predicate.eq("record.survey.id", survey.getId());
    }
}
