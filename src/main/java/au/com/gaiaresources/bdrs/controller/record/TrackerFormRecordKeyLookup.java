package au.com.gaiaresources.bdrs.controller.record;

import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;

public class TrackerFormRecordKeyLookup implements RecordKeyLookup {
    
    @Override
    public String getCensusMethodAttributePrefix() {
        return TrackerController.CENSUS_METHOD_ATTRIBUTE_PREFIX;
    }

    @Override
    public String getCensusMethodIdKey() {
        return TrackerController.PARAM_CENSUS_METHOD_ID;
    }

    @Override
    public String getRecordIdKey() {
        return TrackerController.PARAM_RECORD_ID;
    }

    @Override
    public String getSpeciesIdKey() {
        return TrackerController.PARAM_SPECIES_ID;
    }

    @Override
    public String getSurveyAttributePrefix() {
        return "";
    }

    @Override
    public String getSurveyIdKey() {
        return TrackerController.PARAM_SURVEY_ID;
    }

    @Override
    public String getTaxonAttributePrefix() {
        return TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX;
    }
    
    @Override
    public String getSpeciesNameKey() {
        return TrackerController.PARAM_SPECIES_NAME;
    }
    
    @Override
    public String getIndividualCountKey() {
        return TrackerController.PARAM_INDIVIDUAL_COUNT;
    }

    @Override
    public String getAccuracyKey() {
        return TrackerController.PARAM_ACCURACY;
    }

    @Override
    public String getDateKey() {
        return TrackerController.PARAM_DATE;
    }

    @Override
    public String getLatitudeKey() {
        return TrackerController.PARAM_LATITUDE;
    }

    @Override
    public String getLocationKey() {
        return TrackerController.PARAM_LOCATION;
    }

    @Override
    public String getLocationNameKey() {
        return TrackerController.PARAM_LOCATION_NAME;
    }

    @Override
    public String getLongitudeKey() {
        return TrackerController.PARAM_LONGITUDE;
    }

    @Override
    public String getNotesKey() {
        return TrackerController.PARAM_NOTES;
    }

    @Override
    public String getTimeHourKey() {
        return TrackerController.PARAM_TIME_HOUR;
    }

    @Override
    public String getTimeMinuteKey() {
        return TrackerController.PARAM_TIME_MINUTE;
    }

    @Override
    public String getAttributeNameTemplate() {
        return AttributeParser.ATTRIBUTE_NAME_TEMPLATE;
    }

    @Override
    public String getTimeKey() {
        return TrackerController.PARAM_TIME;
    }

    @Override
    public String getRecordVisibilityKey() {
        return TrackerController.PARAM_RECORD_VISIBILITY;
    }
    
    @Override
    public String getParentRecordIdKey() {
        return TrackerController.PARAM_PARENT_RECORD_ID;
    }
}
