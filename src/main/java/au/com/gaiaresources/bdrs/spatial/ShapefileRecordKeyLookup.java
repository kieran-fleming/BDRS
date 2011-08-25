package au.com.gaiaresources.bdrs.spatial;

import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;

public class ShapefileRecordKeyLookup implements RecordKeyLookup {

    @Override
    public String getAccuracyKey() {
        return ShapefileFields.ACCURACY;
    }

    @Override
    public String getCensusMethodAttributePrefix() {
        return TrackerController.CENSUS_METHOD_ATTRIBUTE_PREFIX;
    }

    @Override
    public String getCensusMethodIdKey() {
        return ShapefileFields.CENSUS_METHOD_ID;
    }

    @Override
    public String getDateKey() {
        return ShapefileFields.DATE;
    }

    @Override
    public String getIndividualCountKey() {
        return ShapefileFields.NUMBER;
    }

    // Used for error reporting on invalid geometry
    @Override
    public String getLatitudeKey() {
        return "Geometry";
    }

    @Override
    public String getLocationKey() {
        return ShapefileFields.PARAM_LOCATION;
    }

    @Override
    public String getLocationNameKey() {
        return ShapefileFields.PARAM_LOCATION_NAME;
    }

    // Used for error reporting on invalid geometry
    @Override
    public String getLongitudeKey() {
        return "Geometry";
    }

    @Override
    public String getNotesKey() {
        return ShapefileFields.NOTES;
    }

    @Override
    public String getRecordIdKey() {
        return ShapefileFields.RECORD_ID;
    }

    @Override
    public String getSpeciesIdKey() {
        return ShapefileFields.SPECIES_ID;
    }

    @Override
    public String getSpeciesNameKey() {
        return ShapefileFields.SPECIES_NAME;
    }

    @Override
    public String getSurveyAttributePrefix() {
        return "";
    }

    @Override
    public String getSurveyIdKey() {
        return ShapefileFields.SURVEY_ID;
    }

    @Override
    public String getTaxonAttributePrefix() {
        return TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX;
    }

    @Override
    public String getTimeHourKey() {
        return ShapefileFields.TIME_HOUR;
    }

    @Override
    public String getTimeMinuteKey() {
        return ShapefileFields.TIME_MINUTE;
    }

    /*
    @Override
    public Integer getKeyLengthLimit() {
        // shape file attribute fields can only be 10 characters long!
        return 10;
    }
    */
    
    @Override
    public String getAttributeNameTemplate() {
        // no template at the moment.
        return "";
    }

    @Override
    public String getTimeKey() {
        return "time";
    }

    @Override
    public String getRecordVisibilityKey() {
        return ShapefileFields.RECORD_VISIBILITY;
    }

}
