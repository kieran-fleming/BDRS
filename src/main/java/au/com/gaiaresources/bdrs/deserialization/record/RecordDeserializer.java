package au.com.gaiaresources.bdrs.deserialization.record;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.vividsolutions.jts.geom.Geometry;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.controller.record.RecordFormValidator;
import au.com.gaiaresources.bdrs.controller.record.ValidationType;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.DateFormatter;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class RecordDeserializer {

    private RecordDAO recordDAO = AppContext.getBean(RecordDAO.class);
    private SurveyDAO surveyDAO = AppContext.getBean(SurveyDAO.class);
    private TaxaDAO taxaDAO = AppContext.getBean(TaxaDAO.class);
    private LocationDAO locationDAO = AppContext.getBean(LocationDAO.class);
    private CensusMethodDAO cmDAO = AppContext.getBean(CensusMethodDAO.class);
    private LocationService locationService = AppContext.getBean(LocationService.class);
    private FileService fileService = AppContext.getBean(FileService.class);
    private PropertyService propertyService = AppContext.getBean(PropertyService.class);
    
    RecordKeyLookup klu;
    AttributeParser attributeParser;
    AttributeDictionaryFactory attrDictFact;
    
    Logger log = Logger.getLogger(getClass());
    
    public static final String TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE_KEY = "Tracker.TaxonAndNumberRequiredTogether";
    public static final String TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE = "Species and number must both be blank, or both filled in.";
    
    public static final String TAXON_NOT_IN_SURVEY_KEY = "Tracker.TaxonNotValidForSurvey";
    public static final String TAXON_NOT_IN_SURVEY_KEY_DEFAULT_MESSAGE = "This species is not valid for the survey.";
    
    public static final String GEOM_INVALID_KEY = "Tracker.GeometryInvalid";
    public static final String GEOM_INVALID_DEFAULT_MESSAGE = "The geometry is invalid. Self intersecting polygons are not allowed";
    
    /**
     * Create a new record deserializer.
     * 
     * RecordDeserializer is intended to provide agnostic parameter map parsing via the implementations of the interfaces passed into
     * the ctor.
     * 
     * @param recKeyLookup
     * @param attributeDictionaryFactory
     * @param parser
     */
    public RecordDeserializer(RecordKeyLookup recKeyLookup, AttributeDictionaryFactory attributeDictionaryFactory, AttributeParser parser) {
        if (recKeyLookup == null) {
            throw new IllegalArgumentException("arg recKeyLookup cannot be null");
        }
        if (attributeDictionaryFactory == null) {
            throw new IllegalArgumentException("arg attrDictFact cannot be null");
        }
        if (parser == null) {
            throw new IllegalArgumentException("arg parser cannot be null");
        }
        this.attrDictFact = attributeDictionaryFactory;
        klu = recKeyLookup;
        attributeParser = parser;
    }

    /**
     * Does a few things:
     * 1. Parse recordEntry param map for record values
     * 2. Validate and create error maps if appropriate
     * 3. Persist records
     * 
     * @param currentUser - the user that the records will be attributed to
     * @param entries - RecordEntry objects - similar to a form backing object but more generic
     * @return
     * @throws ParseException
     * @throws IOException
     */
    public List<RecordDeserializerResult> deserialize(User currentUser, List<RecordEntry> entries) throws ParseException, IOException {
        
        List<RecordDeserializerResult> results = new LinkedList<RecordDeserializerResult>();
        
        for (RecordEntry entry : entries) {
            
            RecordDeserializerResult rsResult = new RecordDeserializerResult(entry);
            results.add(rsResult);
            
            Map<String, String[]> dataMap = entry.getDataMap();
            
            Integer surveyPk = Integer.parseInt(entry.getValue(klu.getSurveyIdKey()));
            
            String censusMethodIdString = entry.getValue(klu.getCensusMethodIdKey());
            
            censusMethodIdString = censusMethodIdString == null ? "0" : censusMethodIdString; 
            Integer censusMethodId = Integer.parseInt(censusMethodIdString);
            
            Survey survey = surveyDAO.getSurvey(surveyPk);
            CensusMethod censusMethod = cmDAO.get(censusMethodId);
            
            // Get the record here so we can first check authorization without doing all of the 
            // other checks...
            Record record;
            String recordId = entry.getValue(klu.getRecordIdKey());
            
            if (recordId != null
                    && !recordId.isEmpty()) {
                
                int recordIdInt;
                try {
                    recordIdInt = Integer.parseInt(recordId);
                } catch (NumberFormatException nfe) {
                    setErrorMessage(rsResult, "Invalid record ID", "Record id: " + recordId + " is not a valid integer. If you want to create a new record indicate '0' as the record ID");
                    continue;
                }
                
                // Unfortunately in shapefiles when you leave an integer field blank it becomes 
                // a '0'. If we detect such a record_id we will create a new record.
                if (recordIdInt == 0) {
                    record = new Record();
                } else {
                    record = recordDAO.getRecord(recordIdInt);
                    
                    // we are attempting to edit an existing record but the record
                    // query has returned null. Error!
                    if (record == null) {
                        setErrorMessage(rsResult, "Record retrieval failure", "Record id: " + recordId + " is not an existing record.");
                        // don't do any further processing for this record
                        continue;
                    }
                }
            } else {
                record = new Record();
            }

            // check authorization!
            if (!record.canWrite(currentUser)) {
                // failed, do reporting.
                rsResult.setAuthorizedAccess(false);
                setErrorMessage(rsResult, "Authorization failure", "You do not have authorization to edit this record");
                // don't do any further processing for this record
                continue;
            }
        
            Taxonomic taxonomic;
            if(censusMethod != null && censusMethod.getTaxonomic() != null) {
                    taxonomic = censusMethod.getTaxonomic();
            }
            else {
                    taxonomic = Taxonomic.OPTIONALLYTAXONOMIC;
            }
            
            // If taxon is denoted by numeric ID, then use it!
            IndicatorSpecies species;
            try {
                species = taxaDAO.getIndicatorSpecies(Integer.parseInt(entry.getValue(klu.getSpeciesIdKey())));
            } catch (NumberFormatException nfe) {
                species = null;
            }
            
            // Validate Mandatory Fields AKA darwin core fields
            RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);
            Map<String, String[]> params = dataMap;
            Map<String, String[]> dateRangeParams = new HashMap<String, String[]>(params);
            dateRangeParams.put("dateRange", 
                       new String[] {
                            survey.getStartDate() == null ? "" : DateFormatter.format(survey.getStartDate(), DateFormatter.DAY_MONTH_YEAR),
                            survey.getEndDate() == null ? "" : DateFormatter.format(survey.getEndDate(), DateFormatter.DAY_MONTH_YEAR) } );
            
            boolean isValid = validator.validate(params, ValidationType.REQUIRED_BLANKABLE_STRING, klu.getNotesKey(), null)
                    & validator.validate(params, ValidationType.REQUIRED_HISTORICAL_DATE, klu.getDateKey(), null)
                    & validator.validate(params, ValidationType.DOUBLE, klu.getAccuracyKey(), null)
                    & validator.validate(dateRangeParams, ValidationType.DATE_WITHIN_RANGE, klu.getDateKey(), null)
                    & validator.validate(params, ValidationType.INTEGER, klu.getRecordIdKey(), null);
            
            if (entry.getGeometry() == null) {
                isValid &= validator.validate(params, ValidationType.REQUIRED_DEG_LATITUDE, klu.getLatitudeKey(), null);
                isValid &= validator.validate(params, ValidationType.REQUIRED_DEG_LONGITUDE, klu.getLongitudeKey(), null);    
            } else {
                // make sure the geometry is valid !
                boolean geomValid = entry.getGeometry().isValid();
                if (!geomValid) {
                    Map<String, String> errorMap = validator.getErrorMap();
                    String errMsg = propertyService.getMessage(
                                    GEOM_INVALID_KEY, 
                                    GEOM_INVALID_DEFAULT_MESSAGE);
                    errorMap.put(klu.getLatitudeKey(), errMsg);
                    errorMap.put(klu.getLongitudeKey(), errMsg);
                } else {
                    // attempt to do geometry conversion as we only support multiline, multipolygon and singlepoint
                    try {
                        Geometry geom = locationService.convertToMultiGeom(entry.getGeometry());
                        entry.setGeometry(geom);
                    } catch (IllegalArgumentException iae) {
                        Map<String, String> errorMap = validator.getErrorMap();
                        errorMap.put(klu.getLatitudeKey(), iae.getMessage());
                        errorMap.put(klu.getLongitudeKey(), iae.getMessage());
                    }
                }
                isValid = isValid && geomValid;
            }
    
            boolean isTaxonomicRecord = Taxonomic.TAXONOMIC.equals(taxonomic) || Taxonomic.OPTIONALLYTAXONOMIC.equals(taxonomic);
            String speciesSearch = entry.getValue(klu.getSpeciesNameKey());
            String numberString = entry.getValue(klu.getIndividualCountKey());
            
            if(isTaxonomicRecord) {
                    ValidationType numberValidationType;
                    ValidationType speciesValidationType;
                if(Taxonomic.TAXONOMIC.equals(taxonomic)) {
                        numberValidationType = ValidationType.REQUIRED_POSITIVE_LESSTHAN;
                        speciesValidationType = ValidationType.REQUIRED_TAXON;
                } else {
                        numberValidationType = ValidationType.POSITIVE_LESSTHAN;
                        speciesValidationType = ValidationType.TAXON;
                }
                
                isValid &= validator.validate(params, numberValidationType, klu.getIndividualCountKey(), null);
                
                // No need to check if the species primary key has already resolved a species
                IndicatorSpecies speciesForSurveyCheck = species;
                if(species == null) {
                    boolean speciesValid = validator.validate(params, speciesValidationType, klu.getSpeciesNameKey(), null); 
                    if (speciesValid) {
                        speciesForSurveyCheck = getSpeciesFromName(speciesSearch);
                    }
                    isValid = isValid & speciesValid;
                }
                
                // If the record is optionally taxonomic and there is a species with
                // no number or a number with no species, then there is an error
                if(Taxonomic.OPTIONALLYTAXONOMIC.equals(taxonomic)) {
                    // What's with this logic? surely it can be done cleaner!
                    if(             (species == null) &&
                                    (!((StringUtils.nullOrEmpty(speciesSearch) && StringUtils.nullOrEmpty(numberString)) ||
                                    (!StringUtils.nullOrEmpty(speciesSearch) && !StringUtils.nullOrEmpty(numberString))))) {
                            isValid = false;
                            Map<String, String> errorMap = validator.getErrorMap();
                            String errMsg = propertyService.getMessage(
                                            TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE_KEY, 
                                            TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE);
                            errorMap.put(klu.getIndividualCountKey(), errMsg);
                            errorMap.put(klu.getSpeciesNameKey(), errMsg);
                    }
                }
                
                if (speciesForSurveyCheck != null && survey.getSpecies() != null && survey.getSpecies().size() > 0) {

                    // a species set size > 0 indicates the survey has a limited number of species
                    // to accept...
                    if (!survey.getSpecies().contains(speciesForSurveyCheck)) {

                        Map<String, String> errorMap = validator.getErrorMap();
                        String errMsg = propertyService.getMessage(
                                        TAXON_NOT_IN_SURVEY_KEY, 
                                        TAXON_NOT_IN_SURVEY_KEY_DEFAULT_MESSAGE);
                        errorMap.put(klu.getSpeciesNameKey(), errMsg);
                    }
                }
            }
            
            // Here is the point we require our name dictionary as we are about to start validating attributes...
            TaxonGroup taxonGroup = species != null ? species.getTaxonGroup() : null;
            Map<Attribute, String> attrNameMap = attrDictFact.createNameKeyDictionary(survey, taxonGroup, censusMethod);
            Map<Attribute, String> attrFilenameMap = attrDictFact.createFileKeyDictionary(survey, taxonGroup, censusMethod);

            for(Attribute attr : survey.getAttributes()) {
                isValid = isValid & attributeParser.validate(validator, attrNameMap.get(attr), attrFilenameMap.get(attr), attr, params, entry.getFileMap());
            }
            if(species != null) {
                for(Attribute attr : species.getTaxonGroup().getAttributes()) {
                    if(!attr.isTag()) {
                        isValid = isValid & attributeParser.validate(validator, attrNameMap.get(attr), attrFilenameMap.get(attr), attr, params, entry.getFileMap());
                    }
                }
            }
            if (censusMethod != null) {
                for (Attribute attr : censusMethod.getAttributes()) {
                    isValid = isValid & attributeParser.validate(validator, attrNameMap.get(attr), attrFilenameMap.get(attr), attr, params, entry.getFileMap());
                }
            }
            
            if(!isValid) {
                rsResult.setErrorMap(validator.getErrorMap());
                // and early end of loop
                continue;
            }
           
            // At this point we know that, 
            // the species primary key does not match an indicator species possibly
            // due to it not being set because javascript being disabled, however
            // the taxon validator has been run so the scientific name has been
            // entered so we can search on the name.
            
            Integer number = null;
            if (isTaxonomicRecord) {
                    if(species != null) {
                            number = new Integer(numberString);
                    }
                    else if(!StringUtils.nullOrEmpty(speciesSearch) && !StringUtils.nullOrEmpty(numberString)) {
                            species = getSpeciesFromName(speciesSearch);
                            number = new Integer(numberString);
                    }
            } else {
                    species = null;
                    number = null;
            }
            
            User user = currentUser;
    
            // Check if taxonomic record!
            record.setSpecies(species);
            record.setNumber(number);
            
            record.setUser(user);
            record.setSurvey(survey);
            record.setNotes(entry.getValue(klu.getNotesKey()));
            record.setHeld(false);
            record.setFirstAppearance(false);
            record.setLastAppearance(false);
            // is possible to set this to null
            record.setCensusMethod(censusMethod);
            
            // attempt to parse the record visibility key. if not cannot be parsed i.e. invalid or not filled in,
            // use the default record visibility for the survey.
            record.setRecordVisibility(RecordVisibility.parse(entry.getValue(klu.getRecordVisibilityKey()), survey.getDefaultRecordVisibility()));
    
            // Dates
            String timeString = attributeParser.getTimeValue(klu.getTimeKey(), klu.getTimeHourKey(), klu.getTimeMinuteKey(), entry.getDataMap());
            String[] timeStringSplit = timeString != null ? timeString.split(":") : null;;
            
            // default record time is midnight.
            int hour = 0;
            int minute = 0;
            
            if (timeStringSplit != null && timeStringSplit.length == 2) {
                try {
                    hour = Integer.parseInt(timeStringSplit[0]);
                } catch(NumberFormatException nfe) {
                    
                }
                try {
                    minute = Integer.parseInt(timeStringSplit[1]);
                } catch (NumberFormatException nfe) {
                    
                }
            } else {
                log.warn("Error parsing record time - using 00:00 as time.");
            }
    
            Calendar cal = new GregorianCalendar();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
            Date date = dateFormat.parse(entry.getValue(klu.getDateKey()));
            cal.setTime(date);
            cal.set(Calendar.HOUR_OF_DAY, hour);
            cal.set(Calendar.MINUTE, minute);
            cal.clear(Calendar.MILLISECOND);
            date = cal.getTime();
            record.setWhen(date);
            record.setTime(date.getTime());
            record.setLastDate(date);
            record.setLastTime(date.getTime());
    
            if (entry.getGeometry() == null) {
                // Position
                double latitude = Double.parseDouble(entry.getValue(klu.getLatitudeKey()));
                double longitude = Double.parseDouble(entry.getValue(klu.getLongitudeKey()));
                record.setPoint(locationService.createPoint(latitude, longitude));    
            } else {
                // we always store our geometries in SRID 4326 aka WGS84
                Geometry theGeom = entry.getGeometry();
                
                theGeom.setSRID(4326);
                record.setGeometry(theGeom);
            }

            Location loc = null;
            // First try to get the location by primary key.
            if(entry.getValue(klu.getLocationKey()) != null) {
                int locationId = Integer.parseInt(entry.getValue(klu.getLocationKey()));
                // At this point locationId may be -1 and therefore loc will be null.
                loc = locationDAO.getLocation(locationId);
            }
            
            // If the location lookup fails, try to see if we should create
            // a new location.
            if(loc == null) {
                String locationName = entry.getValue(klu.getLocationNameKey());
                if(locationName != null && !locationName.isEmpty()) {
                    loc = new Location();
                    loc.setName(locationName);
                    
                    double latitude = record.getGeometry().getCentroid().getY();
                    double longitude = record.getGeometry().getCentroid().getX();
                    
                    loc.setLocation(locationService.createPoint(latitude, longitude));
                    loc = locationDAO.save(loc);
                }
            }
            // This loc here may still be null but that is ok.
            record.setLocation(loc);
            
            String accuracyStr = entry.getValue(klu.getAccuracyKey());
            record.setAccuracyInMeters(StringUtils.notEmpty(accuracyStr) ? Double.parseDouble(accuracyStr) : null);
    
            // Attach the record Attributes.
            AttributeValue recAttr;
            List<AttributeValue> attrValuesToDelete = new ArrayList<AttributeValue>();
    
            // Survey Attributes
            for (Attribute attribute : survey.getAttributes()) {
                recAttr = attributeParser.parse(attrNameMap.get(attribute), attrFilenameMap.get(attribute), attribute, record, entry.getDataMap(), entry.getFileMap());
                if (attributeParser.isAddOrUpdateAttribute()) {
                    recAttr = recordDAO.saveAttributeValue(recAttr);
                    if (attributeParser.getAttrFile() != null) {
                        fileService.createFile(recAttr, attributeParser.getAttrFile());
                    }
                    record.getAttributes().add(recAttr);
                } else {
                    record.getAttributes().remove(recAttr);
                    attrValuesToDelete.add(recAttr);
                }
            }
            
            if (isTaxonomicRecord && species != null) {
                // Taxon Group Attributes
                for (Attribute attribute : species.getTaxonGroup().getAttributes()) {
                    if(!attribute.isTag()) {
                        recAttr = attributeParser.parse(attrNameMap.get(attribute), attrFilenameMap.get(attribute), attribute, record, entry.getDataMap(), entry.getFileMap());
                        if (attributeParser.isAddOrUpdateAttribute()) {
                            recAttr = recordDAO.saveAttributeValue(recAttr);
                            if (attributeParser.getAttrFile() != null) {
                                fileService.createFile(recAttr, attributeParser.getAttrFile());
                            }
                            record.getAttributes().add(recAttr);
                        } else {
                            record.getAttributes().remove(recAttr);
                            attrValuesToDelete.add(recAttr);
                        }
                    }
                }
            }
            
            // Census Method Attributes
            if (censusMethod != null) {
                for (Attribute attribute : censusMethod.getAttributes()) {
                    if(!attribute.isTag()) {
                        recAttr = attributeParser.parse(attrNameMap.get(attribute), attrFilenameMap.get(attribute), attribute, record, entry.getDataMap(), entry.getFileMap());
                        if (attributeParser.isAddOrUpdateAttribute()) {
                            recAttr = recordDAO.saveAttributeValue(recAttr);
                            if (attributeParser.getAttrFile() != null) {
                                fileService.createFile(recAttr, attributeParser.getAttrFile());
                            }
                            record.getAttributes().add(recAttr);
                        } else {
                            record.getAttributes().remove(recAttr);
                            attrValuesToDelete.add(recAttr);
                        }
                    }
                }
            }            
            recordDAO.saveRecord(record);
            rsResult.setRecord(record);
            
            for(AttributeValue attrVal : attrValuesToDelete) {
                recordDAO.saveAttributeValue(attrVal);
                recordDAO.delete(attrVal);
            }
        }
        return results;
    }
    
    /**
     * Just avoids some repetition...
     * @param name
     * @return
     */
    private IndicatorSpecies getSpeciesFromName(String name) {
        if (name == null) {
            return null;
        }
        List<IndicatorSpecies> taxaList = taxaDAO.getIndicatorSpeciesByNameSearch(name);
        if (taxaList.size() == 0) {
            return null;
        }
        if (taxaList.size() > 1) {
            log.warn(taxaList.size() + " entries found for name: " + name + ". Returning the first result: " + taxaList.get(0).getScientificName());
        }
        return taxaList.get(0);
    }
    
    /**
     * Used for appending a single message to the record deserializer result in the case of early failure.
     * 
     * @param rdr
     * @param errorKey
     * @param errorMessage
     */
    private void setErrorMessage(RecordDeserializerResult rdr, String errorKey, String errorMessage) {
        Map<String, String> errMap = new HashMap<String, String>(1);
        errMap.put(errorKey, errorMessage);
        rdr.setErrorMap(errMap);
    }
}
