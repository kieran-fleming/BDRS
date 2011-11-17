package au.com.gaiaresources.bdrs.model.survey;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertySetting;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;

public enum SurveyFormRendererType {
	
    DEFAULT("Default"),
    ATLAS("Atlas of Living Australia"),
    YEARLY_SIGHTINGS("Yearly Sightings"),
    SINGLE_SITE_MULTI_TAXA("Single-Site Multi-Species"),
    SINGLE_SITE_ALL_TAXA("Single-Site All Species");
    Logger log = Logger.getLogger(getClass());
    private String name;

    private SurveyFormRendererType(String name) {
        this.name = name;
    }

    public String getName() {
        return this.name;
    }

    /**
     * Tests if this form type can be used to represent the specified survey.
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
            case SINGLE_SITE_ALL_TAXA:
            	//Checks if any of the RecordPropertyAttributeFormFields have a recordProperty that is not set to hidden
            	for (RecordPropertyType type : RecordPropertyType.values()) {
            		String mdKeyHidden = RecordProperty.getMetaDataKeys(type).get(RecordPropertySetting.HIDDEN);
            		Metadata md = survey.getMetadataByKey(mdKeyHidden);
            		//Checks if the survey is not persisted yet, then it becomes eligible
            		if (survey.getCreatedAt() == null || (md != null && md.getValue().equals("false"))) {
            			isEligible = true;
            		} else if(type.equals(RecordPropertyType.SPECIES) && this.equals(SurveyFormRendererType.SINGLE_SITE_ALL_TAXA)){
            			//We can't have a survey which is single site all species if the species field is hidden
        				isEligible = false;
        				break;
        			}
            	}
            	break;
            default:
                isEligible = false;
                break;
        }
        return isEligible;
    }

}
