package au.com.gaiaresources.bdrs.model.survey;

import java.io.IOException;
import java.io.Writer;
import java.util.Calendar;
import java.util.Date;
import java.util.Map;
import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertySetting;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.json.JSONEnum;
import au.com.gaiaresources.bdrs.json.JSONEnumUtil;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

public enum SurveyFormRendererType implements JSONEnum {
	
    // Order the enum in alphabetical order so it is displayed properly
    // on the survey editing page.
    ATLAS("Atlas of Living Australia"),
    DEFAULT("Default"),
    SINGLE_SITE_ALL_TAXA("Single-Site All Species"),
    SINGLE_SITE_MULTI_TAXA("Single-Site Multi-Species"),
    YEARLY_SIGHTINGS("Yearly Sightings");
    
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
                // single site multi taxa are only eligible if something will appear in the sightings table
                // (i.e. if species or number is not hidden or if there is at least one Record attribute)
                if (survey.getId() == null) {
                    // it is eligible if the survey has not yet been persisted
                    isEligible = true;
                } else {
                    // check the survey
                    isEligible = checkFormField(survey, RecordPropertyType.SPECIES);
                    if (!isEligible) {
                        // if it is not eligible, check the number
                        isEligible = checkFormField(survey, RecordPropertyType.NUMBER);
                    }
                    // if survey and number are hidden, check for Record scoped properties
                    if (!isEligible) {
                        isEligible = isOneRecordAttribute(survey);
                    }
                }
                break;
            case SINGLE_SITE_ALL_TAXA:
                // single site all taxa is only eligible for surveys that do not have the 
                // species field and at least one other record scoped attribute hidden
                if (survey.getId() == null) {
                    // it is eligible if the survey has not yet been persisted
                    isEligible = true;
                } else {
                    isEligible = checkFormField(survey, RecordPropertyType.SPECIES);
                    isEligible &= (checkFormField(survey, RecordPropertyType.NUMBER) || 
                            isOneRecordAttribute(survey));
                }
            	break;
            default:
                isEligible = false;
                break;
        }
        return isEligible;
    }

    /**
     * Get the start date for the sightings for the given survey. Needed
     * because survey.startDate is not always indicative
     * of the start date of the records.
     * 
     * @param survey to get start date for
     * @return start date
     */
    public Date getStartDateForSightings(Survey survey) {
        Date surveyStartDate = survey.getStartDate();
        Date startDate = surveyStartDate != null ? surveyStartDate : new Date(0);
        
        switch(this) {
            case YEARLY_SIGHTINGS:
            {
                Calendar cal = Calendar.getInstance();
                // survey start date cannot be null.
                cal.setTime(startDate);
                cal.set(Calendar.HOUR_OF_DAY, 0);
                cal.set(Calendar.MINUTE, 0);
                cal.set(Calendar.SECOND, 0);
                cal.set(Calendar.MILLISECOND, 0);
                cal.set(Calendar.DAY_OF_MONTH, 0);
                cal.add(Calendar.MONTH, -6);
                return cal.getTime();
            }
            case DEFAULT:
            case ATLAS:
            case SINGLE_SITE_MULTI_TAXA:
            case SINGLE_SITE_ALL_TAXA:
            default:
                return startDate;
        }
    }
    
    /**
     * Get the end date for the sightings for the given survey. Needed
     * because survey.endDate is not always indicative
     * of the end date of the records.
     * 
     * @param survey to get end date for
     * @return end date
     */
    public Date getEndDateForSightings(Survey survey) {
        switch(this) {
            case YEARLY_SIGHTINGS:
            {
                Calendar cal = Calendar.getInstance();
                cal.setTime(this.getStartDateForSightings(survey));
                cal.add(Calendar.YEAR, 1);
                return cal.getTime();
            }
            case DEFAULT:
            case ATLAS:
            case SINGLE_SITE_MULTI_TAXA:
            case SINGLE_SITE_ALL_TAXA:
            default:
                Date endDate = survey.getEndDate();
                return endDate != null ? endDate : new Date();
        }
    }
    
    /**
     * Returns some text that describes any survey settings that are required for a survey form type
     * to be eligible for use.
     * 
     * @return 
     */
    public String getDescription() {
        PropertyService propService = AppContext.getBean(PropertyService.class);
        switch (this) {
        case YEARLY_SIGHTINGS:
            return propService.getMessage("SurveyFormRendererType.yearlySightingsFormDescription");
        case DEFAULT:
            return propService.getMessage("SurveyFormRendererType.trackerFormDescription");
        case ATLAS:
            return propService.getMessage("SurveyFormRendererType.atlasFormDescription");
        case SINGLE_SITE_MULTI_TAXA:
            return propService.getMessage("SurveyFormRendererType.ssmtDescription");
        case SINGLE_SITE_ALL_TAXA:
            return propService.getMessage("SurveyFormRendererType.ssatDescription");
        default:
            return "";
        }
    }

    /**
     */
    private boolean isOneRecordAttribute(Survey survey) {
        for (Attribute attr : survey.getAttributes()) {
            if (attr.getScope().equals(AttributeScope.RECORD)) {
                // stop when we find one Record scoped attribute
                return true;
            }
        }
        return false;
    }

    /**
     */
    private boolean checkFormField(Survey survey, RecordPropertyType propertyType) {
        // checks if the species property is set to hidden, making this ineligible
        boolean isEligible = false;
        Map<RecordPropertySetting, String> keys = RecordProperty.getMetaDataKeys(propertyType);
        if (keys == null) {
            // if there are no keys for SPECIES, this has not been set up yet and is true
            isEligible = true;
        } else {
            String mdKeyHidden = keys.get(RecordPropertySetting.HIDDEN);
            // if there is no hidden property, it is not set up yet and true
            if (mdKeyHidden == null) {
                isEligible = true;
            } else {
                Metadata md = survey.getMetadataByKey(mdKeyHidden);
                // if there is no metadata for the hidden property key, it is not set up yet and true
                if (md == null) {
                    isEligible = true;
                } else {
                    // if the hidden property == false, it is eligible,
                    // otherwise it is not
                    isEligible = Boolean.parseBoolean(md.getValue()) == false;
                }
            }
        }
        return isEligible;
    }
    
    @Override
    public void writeJSONString(Writer out) throws IOException {
        JSONEnumUtil.writeJSONString(out, this);
    }

    @Override
    public String toJSONString() {
        return JSONEnumUtil.toJSONString(this);
    }
}
