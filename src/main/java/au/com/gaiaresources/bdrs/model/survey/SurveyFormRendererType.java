package au.com.gaiaresources.bdrs.model.survey;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;

public enum SurveyFormRendererType {
    DEFAULT("Default"),
    ATLAS("Atlas of Living Australia"),
    YEARLY_SIGHTINGS("Yearly Sightings"),
    SINGLE_SITE_MULTI_TAXA("Single-Site Multi-Species");

    private String name;

    private SurveyFormRendererType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Tests if this form type can be used to represent the specicied survey.
     *
     * @param survey the survey to be tested for eligibility
     * @return true if this form type can be used to represent the specified
     * survey, false otherwise.
     */
    public boolean isEligible(Survey survey) {
        boolean isEligible = false;
        switch(this) {
            case DEFAULT:
            case ATLAS:
                isEligible = true;
                break;
            case YEARLY_SIGHTINGS:
                // Can only represent a single taxon and survey scope attributes
                isEligible = true;
                isEligible = isEligible && (survey.getSpecies().size() == 1);
                for(Attribute attrib : survey.getAttributes()) {
                    isEligible = isEligible && AttributeScope.SURVEY.equals(attrib.getScope());
                }
                break;
            case SINGLE_SITE_MULTI_TAXA:
                isEligible = true;
                break;
            default:
                isEligible = false;
                break;
        }
        return isEligible;
    }

}
