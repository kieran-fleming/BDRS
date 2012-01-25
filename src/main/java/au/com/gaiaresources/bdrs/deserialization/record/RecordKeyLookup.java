package au.com.gaiaresources.bdrs.deserialization.record;



/**
 * A lookup for the *keys* that when used in an appropriate Map will
 * return the following values.
 * 
 * @author aaron
 *
 */
public interface RecordKeyLookup {
       
    /**
     * The bdrs survey id number
     * @return
     */
    String getSurveyIdKey();
    
    /**
     * the bdrs species id number
     * @return
     */
    String getSpeciesIdKey();
    
    /**
     * the bdrs record id number
     * @return
     */
    String getRecordIdKey();
    
    /**
     * the bdrs census method number
     * @return
     */
    String getCensusMethodIdKey();
    
    /**
     * The name of the species. may be common or scientific.
     * @return
     */
    String getSpeciesNameKey();
    
    /**
     * the number of individuals counted during a recording.
     * @return
     */
    String getIndividualCountKey();
    
    /**
     * Additional notes for the record
     * @return
     */
    String getNotesKey();
    
    /**
     * The time of the recording
     * @return
     */
    String getTimeKey();
    
    /**
     * The hour of the day when the recording was taken
     * @return
     */
    String getTimeHourKey();
    
    /**
     * The minute of the hour when the recording was taken
     * @return
     */
    String getTimeMinuteKey();
    
    /**
     * The date of the recording
     * @return
     */
    String getDateKey();
    
    /**
     * The latitude when the recording was taken. Some methods of record entry
     * will not require this
     * @return
     */
    String getLatitudeKey();
    
    /**
     * The longitude when the recording was taken. Some methods of record entry
     * will not require this
     * @return
     */
    String getLongitudeKey();
    
    /**
     * The bdrs location id number where the recording was taken
     * @return
     */
    String getLocationKey();
    
    /**
     * The accuracy in meters of the location of the recording
     * @return
     */
    String getAccuracyKey();
    
    /**
     * The bdrs location name where the recording was taken. This may be used to 
     * indicate a new location
     * @return
     */
    String getLocationNameKey();
    
    /**
     * The prefix of any survey attributes in the data map
     * @return
     */
    String getSurveyAttributePrefix();
    
    /**
     * The prefix of any taxon attributes in the data map
     * @return
     */
    String getTaxonAttributePrefix();
    
    /**
     * The prefix of any census method attributes in the data map
     * @return
     */
    String getCensusMethodAttributePrefix();
    
    /**
     * The maximum length of any key in the data map
     * @return
     */
    //Integer getKeyLengthLimit();
    
    /**
     * 
     * @return
     */
    String getAttributeNameTemplate();
    
    /**
     * The visibility of the record. See RecordVisibility.java for details.
     * @return
     */
    String getRecordVisibilityKey();
    
    /**
     * The parent record's id number
     * this can be empty
     * @return
     */
    String getParentRecordIdKey();
}
