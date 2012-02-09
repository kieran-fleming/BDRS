package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormFieldFactory;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordFormFieldCollection;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationNameComparator;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.record.impl.AdvancedRecordFilter;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeUtil;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValueUtil;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.StringUtils;

import com.vividsolutions.jts.geom.Geometry;

/**
 * The <code>SingleSiteMultiTaxa</code> controller is a record add form renderer
 * that allows multiple sightings of differing taxa to be created for a single
 * location.
 * 
 * @author benk
 */

public abstract class SingleSiteController extends AbstractController {

    private static final RecordPropertyType[] TAXA_RECORD_PROPERTY_NAMES = new RecordPropertyType[] {
            RecordPropertyType.SPECIES, RecordPropertyType.NUMBER };

    public static final String PREFIX_TEMPLATE = "%d_";
    public static final String PARAM_ROW_PREFIX = "rowPrefix";

    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_SURVEY_ID = "surveyId";
    public static final String PARAM_CENSUS_METHOD_ID = "censusMethodId";
    public static final String PARAM_ACCURACY = "accuracyInMeters";
    
    public static final String PARAM_SPECIES = "species";
    public static final String PARAM_NUMBER = "number";
    
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "longitude";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_TIME_HOUR = "time_hour";
    public static final String PARAM_TIME_MINUTE = "time_minute";
    public static final String PARAM_NOTES = "notes";
    public static final String PARAM_LOCATION = "location";
    
    public static final String PARAM_SIGHTING_INDEX = "sightingIndex";
    
    private static final int STARTING_SIGHTING_INDEX = 0;
    
    public static final String ROW_VIEW = "singleSiteMultiTaxaRow";
    
    public static final String MSG_CODE_SUCCESS_MY_SIGHTINGS = "bdrs.record.singlesitemultitaxa.save.success.mySightings";
    public static final String MSG_CODE_SUCCESS_STAY_ON_FORM = "bdrs.record.singlesitemultitaxa.save.success.stayOnForm";
    public static final String MSG_CODE_SUCCESS_ADD_ANOTHER = "bdrs.record.singlesitemultitaxa.save.success.addAnother";
    
    /**
     * The survey scoped form fields that may be populated with attribute
     * value data.
     */
    public static final String MODEL_SURVEY_FORM_FIELD_LIST = "formFieldList";
    
    /**
     * The list of record form field collection objects that may be populated with
     * attribute value data,.
     */
    public static final String MODEL_RECORD_ROW_LIST = "recordFieldCollectionList";
    
    /**
     * The form fields used to create the header of the sightings table
     */
    public static final String MODEL_SIGHTING_ROW_LIST = "sightingRowFormFieldList";
    
    /**
     * The record object used to populate the form fields.
     */
    public static final String MODEL_RECORD = "record";

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private AttributeDAO attributeDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private CensusMethodDAO cmDAO;
    @Autowired
    private MetadataDAO metadataDAO;

    @Autowired
    private LocationService locationService;
    @Autowired
    private FileService fileService;

    private FormFieldFactory formFieldFactory = new FormFieldFactory();

    private Logger log = Logger.getLogger(getClass());

    /**
     * Saves multiple records for a single site. Site information that is used
     * for all records are specified via parameters. Record specific data such
     * as the sighted taxon will be retrieved from the request parameters.
     * Record specific parameters are prefixed by the row index
     * 
     * @param request
     *            the browser request
     * @param response
     *            the server response
     * @param surveyId
     *            the primary key of the survey where the record shall be added
     * @param latitude
     *            the latitude of the sighting
     * @param longitude
     *            the longitude of the sighting
     * @param date
     *            the calendar date of the sighting
     * @param time
     *            the time when the sighting occurred
     * @param notes
     *            additional notes to be attached to all records
     * @param sightingIndex
     *            the number of records to be saved.
     * @return
     * @throws ParseException
     *             throws if the date cannot be parsed
     * @throws IOException
     *             thrown if uploaded files cannot be saved
     */
    protected ModelAndView saveRecordHelper(
            MultipartHttpServletRequest request, HttpServletResponse response,
            int surveyId, Double latitude, Double longitude, Date date,
            String time_hour, String time_minute, String notes,
            Integer sightingIndex, String[] rowIds) throws ParseException, IOException {

        Map<String, String[]> paramMap = this.getModifiableParameterMap(request);

        User user = getRequestContext().getUser();

        Survey survey = surveyDAO.getSurvey(surveyId);

        // Dates
        Date dateTemplate = null;
        Calendar cal = new GregorianCalendar();
        if (date != null) {
            cal.setTime(date);
        }
        Integer hour, minute;
        if (StringUtils.nullOrEmpty(time_minute)) {
            minute = null;
        } else {
            //minute = Integer.parseInt(request.getParameter("time_minute"));
            minute = Integer.parseInt(time_minute);
            cal.set(Calendar.MINUTE, minute);
        }
        if (StringUtils.nullOrEmpty(time_hour)) {
            hour = null;
        } else {
            //hour = Integer.parseInt(request.getParameter("time_hour"));
            hour = Integer.parseInt(time_hour);
            cal.set(Calendar.HOUR_OF_DAY, hour);
        }
        cal.clear(Calendar.MILLISECOND);
        if (date != null || hour != null || minute != null) {
            dateTemplate = cal.getTime();
        }
        
        Double accuracy = null;
        String accuracyString = request.getParameter(PARAM_ACCURACY);
        if (!StringUtils.nullOrEmpty(accuracyString)) {
            try {
                accuracy = Double.parseDouble(accuracyString);    
            } catch (NumberFormatException nfe) {
                accuracy = null;
            }
        }
        
        Location loc = null;
        String locIdString = request.getParameter(PARAM_LOCATION);
        if (!StringUtils.nullOrEmpty(locIdString)) {
            try {
                loc = locationDAO.getLocation(Integer.parseInt(locIdString));
            } catch (NumberFormatException nfe) {
                loc = null;
            }
        }

        Record record = null;

        String surveyPrefix = AttributeParser.DEFAULT_PREFIX;
        WebFormAttributeParser attributeParser = new WebFormAttributeParser();
        List<Record> records = new ArrayList<Record>();

        for (String recordPrefix : rowIds) {
        	
            if (StringUtils.notEmpty(recordPrefix)) {
                // Attempt to retrieve a record
                try {
                    String recIdString = request.getParameter(recordPrefix + PARAM_RECORD_ID);
                    if (StringUtils.notEmpty(recIdString)) {
                        Integer recId = Integer.parseInt(recIdString);
                        record = recordDAO.getRecord(recId);
                        // the rec id we attempted to retrieve does not exist, new record
                        if (record == null) {
                            record = new Record();
                        }    
                    } else {
                        // rec id string is null or empty, make a new record
                        record = new Record();
                    }
                    
                } catch (NumberFormatException nfe) {
                    // fall back to a new record
                    record = new Record();
                }

                // Set record visibility to survey default. Setting via web form not supported.
                // Survey's default record visibility can be set in the 'admin -> projects' interface
                // This will also set an existing record's visibility back to the survey default.
                record.setRecordVisibility(survey.getDefaultRecordVisibility());

                // Preserve the original owner of the record if this is a record edit.
                if (record.getUser() == null) {
                    record.setUser(user);
                }
                
                record.setSurvey(survey);
                record.setAccuracyInMeters(accuracy);
                if (notes != null) {
                    record.setNotes(notes);
                }
                if (loc != null) {
                    record.setLocation(loc);
                    record.setPoint(loc.getPoint());
                } else {
                    // clear the location item...
                    record.setLocation(null);
                    if (latitude != null && longitude != null) {
                        record.setPoint(locationService.createPoint(latitude, longitude));
                    }
                }

                // Avoiding (for no reason) using the same instance of the date
                if (dateTemplate != null) {
                    Date recordDate = new Date(dateTemplate.getTime());
                    record.setWhen(recordDate);
                    record.setTime(recordDate.getTime());
                    record.setLastDate(recordDate);
                    record.setLastTime(recordDate.getTime());
                }

                // Constants
                record.setFirstAppearance(false);
                record.setLastAppearance(false);

                // Taxonomy
                String speciesPkStr = request.getParameter(String.format("%s" + PARAM_SPECIES, recordPrefix));
                if (speciesPkStr != null && !speciesPkStr.trim().isEmpty()) {
                    int speciesPk = Integer.parseInt(speciesPkStr);
                    IndicatorSpecies species = taxaDAO.getIndicatorSpecies(speciesPk);
                    record.setSpecies(species);
                }
                // Number
                // this can happen in the singleSiteAllTaxa page
                String count = request.getParameter(String.format("%s" + PARAM_NUMBER, recordPrefix));
                Integer number = null;
                if (!StringUtils.nullOrEmpty(count)) {
                    try {
                        number = Integer.parseInt(count);
                    } catch (NumberFormatException e) {
                        log.error("Value should be an integer: " + e.getMessage());
                    }
                }
                record.setNumber(number);
                // check that we can save the record at this point based on the count
                boolean canSave = canSaveRecord(number);
                // Record Attributes
                AttributeValue recAttr;
                String prefix;
                Map<AttributeValue, MultipartFile> attsToSave = new HashMap<AttributeValue, MultipartFile>();
                List<AttributeValue> attsToDelete = new ArrayList<AttributeValue>();
                for (Attribute attribute : survey.getAttributes()) {
                    if (!AttributeScope.LOCATION.equals(attribute.getScope())) {
                        if (AttributeUtil.isModifiableByScopeAndUser(attribute, user)) {
                            prefix = AttributeScope.SURVEY.equals(attribute.getScope()) || 
                                 AttributeScope.SURVEY_MODERATION.equals(attribute.getScope()) ? surveyPrefix
                                : recordPrefix;
                            recAttr = attributeParser.parse(prefix, attribute, record, paramMap, request.getFileMap());
                            if (attributeParser.isAddOrUpdateAttribute()) {
                                attsToSave.put(recAttr, attributeParser.getAttrFile());
                            } else {
                                attsToDelete.add(recAttr);
                            }
                            if (!canSave) {
                                canSave = AttributeScope.RECORD.equals(attribute.getScope()) && 
                                          recAttr != null && recAttr.isPopulated();
                            }
                        }
                    }
                }
                
                // always save records that are being edited
                canSave = canSave || record.getId() != null;
                if (canSave) {
                    records.add(recordDAO.saveRecord(record));
                    
                    for (Entry<AttributeValue, MultipartFile> entry : attsToSave.entrySet()) {
                        recAttr = entry.getKey();
                        recAttr = attributeDAO.save(recAttr);
                        if (entry.getValue() != null) {
                            fileService.createFile(recAttr, entry.getValue());
                        }
                        record.getAttributes().add(recAttr);
                    }
                    
                    for (AttributeValue attributeValue : attsToDelete) {
                        record.getAttributes().remove(attributeValue);
                        attributeDAO.delete(attributeValue);
                    }
                }
            }

        }

        ModelAndView mv = RecordWebFormContext.getSubmitRedirect(request, record);

        if (request.getParameter(RecordWebFormContext.PARAM_SUBMIT_AND_ADD_ANOTHER) != null) {
            mv.addObject("surveyId", survey.getId());
            getRequestContext().addMessage(MSG_CODE_SUCCESS_ADD_ANOTHER, new Object[] { records.size() });
        } else {
            switch (survey.getFormSubmitAction()) {
            case MY_SIGHTINGS:
                getRequestContext().addMessage(MSG_CODE_SUCCESS_MY_SIGHTINGS, new Object[] { records.size() });
                break;
            case STAY_ON_FORM:
                getRequestContext().addMessage(MSG_CODE_SUCCESS_STAY_ON_FORM, new Object[] { records.size() });
                break;
            default:
                throw new IllegalStateException("Submit form action not handled : " + survey.getFormSubmitAction());
            }
        }
        return mv;
    }

    /**
     * Determines if we can save a record based on the value of the count field.
     * @param number the count field for the record
     * @return true if the record can be saved, false otherwise
     */
    protected boolean canSaveRecord(Integer number) {
        return true;
    }

    @InitBinder
    public void initBinder(HttpServletRequest request,
            ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }

    protected ModelAndView ajaxGetSightingsTable(HttpServletRequest request,
            HttpServletResponse response, int surveyId, int sightingIndex) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = new Record();

        // Add survey scope attribute form fields
        String prefix = getSightingPrefix(sightingIndex);
        List<FormField> formFieldList = new ArrayList<FormField>();
        for (Attribute attribute : survey.getAttributes()) {
            // Only record scoped attributes should be in the sightings table.
            if (AttributeScope.RECORD.equals(attribute.getScope())
                    && !attribute.isTag()) {
                formFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute, null, prefix));
            }
        }
        // Add all property form fields
        for (RecordPropertyType type : TAXA_RECORD_PROPERTY_NAMES) {
            RecordProperty recordProperty = new RecordProperty(survey, type,
                    metadataDAO);
            formFieldList.add(formFieldFactory.createRecordFormField(record, recordProperty, null, null, prefix));
        }
        Collections.sort(formFieldList);

        ModelAndView mv = new ModelAndView(ROW_VIEW);
        mv.addObject("record", record);
        mv.addObject("survey", survey);
        mv.addObject("formFieldList", formFieldList);
        mv.addObject("sightingIndex", prefix);
        // by definition editing must be enabled for items to be added to the
        // sightings table.
        mv.addObject(RecordWebFormContext.MODEL_EDIT, true);
        return mv;
    }

    /**
     * This is used by subclasses in the GET handler
     * 
     * @param request
     * @param response
     * @param surveyId
     * @param viewName
     * @param censusMethodId
     * @return
     */
    protected ModelAndView addRecord(HttpServletRequest request,
            HttpServletResponse response, int surveyId, String viewName,
            Integer censusMethodId) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = null;
        CensusMethod censusMethod = null;
        if (request.getParameter(PARAM_RECORD_ID) != null
                && !request.getParameter(PARAM_RECORD_ID).isEmpty()) {
            try {
                record = recordDAO.getRecord(Integer.parseInt(request.getParameter(PARAM_RECORD_ID)));
                censusMethod = record.getCensusMethod();
            } catch (NumberFormatException nfe) {
                record = new Record();
                // Set record visibility to survey default. Setting via web form not supported.
                // Survey's default record visibility can be set in the 'admin -> projects' interface
                record.setRecordVisibility(survey.getDefaultRecordVisibility());
            }
        } else {
            record = new Record();
            censusMethod = cmDAO.get(censusMethodId);
            // Set record visibility to survey default. Setting via web form not supported.
            // Survey's default record visibility can be set in the 'admin -> projects' interface
            record.setRecordVisibility(survey.getDefaultRecordVisibility());
        }
        
        User accessor = getRequestContext().getUser();
        
        RecordWebFormContext context = new RecordWebFormContext(request, record, accessor, survey);
        
        // get the records for this form instance (if any)
        List<Record> recordsForFormInstance = getRecordsForFormInstance(record, accessor);
        
        // Add survey scope attribute form fields
        List<FormField> sightingRowFormFieldList = new ArrayList<FormField>();
        List<FormField> formFieldList = new ArrayList<FormField>();
        
        // save a list of the record scoped attributes for construction of form fields for each
        // record (aka sighting table row) later...
        List<Attribute> recordScopedAttributeList = new ArrayList<Attribute>();
        
        for (Attribute attribute : survey.getAttributes()) {
            if (!attribute.isTag()
                    && !AttributeScope.LOCATION.equals(attribute.getScope())) {
                AttributeValue attrVal = AttributeValueUtil.getAttributeValue(attribute, record);
                if (AttributeScope.SURVEY.equals(attribute.getScope()) || 
                        (AttributeScope.SURVEY_MODERATION.equals(attribute.getScope()) && 
                                ((accessor != null && accessor.isModerator()) || (attrVal != null && attrVal.isPopulated())))) {
                    // only add moderation attributes if the accessor is a moderator or the value is populated
                    formFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute, attrVal));
                } else if (AttributeScope.RECORD.equals(attribute.getScope()) || 
                        (AttributeScope.RECORD_MODERATION.equals(attribute.getScope()) && 
                                ((accessor != null && accessor.isModerator()) || (attrVal != null && attrVal.isPopulated())))) {
                    recordScopedAttributeList.add(attribute);
                    sightingRowFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute));
                }
            }
        }
        
        // Add all property form fields.
        // save a list of the record scoped record properties for construction of form fields
        // for each record (aka sighting table row) later...
        List<RecordProperty> recordScopedRecordPropertyList = new ArrayList<RecordProperty>();
        boolean showMap = false;
        for (RecordPropertyType type : RecordPropertyType.values()) {
        	
            RecordProperty recordProperty = new RecordProperty(survey, type,
                    metadataDAO);
            
            //showMap if location or point fields are hidden.
            if(!showMap && (type.equals(RecordPropertyType.LOCATION) || type.equals(RecordPropertyType.POINT))){
            	showMap = !recordProperty.isHidden();
            }
            
            if (!recordProperty.isHidden()) {
                if (recordProperty.getScope().equals(AttributeScope.SURVEY) || 
                        AttributeScope.SURVEY_MODERATION.equals(recordProperty.getScope())) {
                    formFieldList.add(formFieldFactory.createRecordFormField(record, recordProperty));
                } else {
                    recordScopedRecordPropertyList.add(recordProperty);
                    sightingRowFormFieldList.add(formFieldFactory.createRecordFormField(record, recordProperty));
                }
            }
        }

        Collections.sort(formFieldList);
        Collections.sort(sightingRowFormFieldList);

        Metadata predefinedLocationsMD = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        boolean predefinedLocationsOnly = predefinedLocationsMD != null
                && Boolean.parseBoolean(predefinedLocationsMD.getValue());

        Set<Location> locations = new TreeSet<Location>(
                new LocationNameComparator());
        locations.addAll(survey.getLocations());
        if (!predefinedLocationsOnly) {
            locations.addAll(locationDAO.getUserLocations(getRequestContext().getUser()));
        }
        ModelAndView mv = new ModelAndView(viewName);
        
        int sightingIndex = STARTING_SIGHTING_INDEX;
        
        // the final list of populated form field collections that we will use to render the web form.
        List<RecordFormFieldCollection> recFormFieldCollectionList = new ArrayList<RecordFormFieldCollection>();
        
        // modify the list
        // note we need to reassign as it is a new list instance...
        recordsForFormInstance = modifyRecordDisplayList(recordsForFormInstance, survey);
        
        for (Record rec : recordsForFormInstance) {
            boolean highlight = rec.equals(record);
            String prefix = getSightingPrefix(sightingIndex++);
            
            RecordFormFieldCollection rffc = new RecordFormFieldCollection(prefix, 
                                                                           rec, 
                                                                           highlight, 
                                                                           recordScopedRecordPropertyList,
                                                                           recordScopedAttributeList);
            
            recFormFieldCollectionList.add(rffc);
        }

        // form field list is the survey scoped attributes.
        // contains the form field and the data (optional).
        // note: NON record scoped attributes only!
        mv.addObject(MODEL_SURVEY_FORM_FIELD_LIST, formFieldList);
        
        // sightings row form field list is the record scoped attributes
        // this is used to create the sightings table header row - no values!
        // note: record scoped attributes only!
        mv.addObject(MODEL_SIGHTING_ROW_LIST, sightingRowFormFieldList);
        
        // form field collections used to poplate the body of the sightings
        // table. i.e., data is in here!
        // note: record scoped attributes only!
        mv.addObject(MODEL_RECORD_ROW_LIST, recFormFieldCollectionList);
        
        mv.addObject(MODEL_RECORD, record);
        
        mv.addObject("survey", survey);
        mv.addObject("locations", locations);
        mv.addObject("preview", request.getParameter("preview") != null);
        mv.addObject("censusMethod", censusMethod);
        mv.addObject(RecordWebFormContext.MODEL_WEB_FORM_CONTEXT, context);
        if (accessor != null) {
        	mv.addObject("ident", accessor.getRegistrationKey());
        }
        mv.addObject("displayMap", showMap);
        
        return mv;
    }

    private List<Record> getRecordsForFormInstance(Record rec, User accessor) {
        
        // early return if the record is a non persisted instance - i.e. this will return
        // an empty form.
        if (rec.getId() == null) {
            return Collections.emptyList();
        }
        
        AdvancedRecordFilter recFilter = new AdvancedRecordFilter();
        recFilter.setStartDate(rec.getWhen());
        recFilter.setEndDate(rec.getWhen());
        recFilter.setSurveyPk(rec.getSurvey().getId().intValue());
        recFilter.setUser(rec.getUser());
        recFilter.setAccessor(accessor);
        
        ScrollableRecords scrollableRecords = recordDAO.getScrollableRecords(recFilter);
        
        List<Record> result = new ArrayList<Record>();

        while (scrollableRecords.hasMoreElements()) {
            Record recordUnderTest = scrollableRecords.nextElement();
            
            // Make sure the geometry is the same. I'm not sure whether it's better to
            // do this in the database or not. I'll consider that an optimization
            // and will look into it if performance becomes an issue.
            Geometry geomA = rec.getGeometry();
            Geometry geomB = recordUnderTest.getGeometry();
            if (geomA != null && geomB == null) {
                // early continue, records cannot be from the same form instance
                continue;
            }
            if (geomA == null && geomB != null) {
                // early continue, records cannot be from the same form instance
                continue;
            }
            // finally, check for both non null and whether the geometries are the same
            if ((geomA != null && geomB != null) && !geomA.equalsExact(geomB)) {
                // early continue, records cannot be from the same form instance
                continue;
            }
            
            // make sure all survey scoped attributes are the same...
            
            // if even 1 survey scoped attribute value has been deemed to be different, the record will
            // not be accepted.
            boolean identicalSurveyScopedAttributeValues = true;
            
            for (AttributeValue av : rec.getAttributes()) {
                // we are only concerned about survey scoped attributes.
                // not sure if we need to consider location scoped attributes or not.
                // record attributes should definitely NOT be considered here.
                if (AttributeScope.SURVEY.equals(av.getAttribute().getScope())  || 
                        AttributeScope.SURVEY_MODERATION.equals(av.getAttribute().getScope())) {
                    AttributeValue avToTest = AttributeValueUtil.getAttributeValue(av.getAttribute(), recordUnderTest);
                    
                    if (avToTest == null) {
                        // early loop continue - item not added to final result
                        identicalSurveyScopedAttributeValues = false;
                        break;
                    }
                    if (!AttributeValueUtil.isAttributeValuesEqual(av, avToTest)) {
                        identicalSurveyScopedAttributeValues = false;
                        break;
                    }
                }
            }
            
            // record has been deemed to be part of the same single site form instance.
            // add to the result.
            if (identicalSurveyScopedAttributeValues) {
                result.add(recordUnderTest);
            }
        }
        
        return result;
    }
    
    
    private static String getSightingPrefix(int sightingIndex) {
        return String.format(PREFIX_TEMPLATE, sightingIndex);
    }
    
    /**
     * Overridable method that we can use to alter what items are displayed in the form instance.
     * 
     * Original intent was to allow SingleSiteAllTaxa form to have non persisted records that
     * contained species that weren't part of the recordsForFormInstance list.
     * 
     * @param recordsForFormInstance - the records determined by SingleSiteController that belong to the form instance
     * @param survey - The survey for the records
     * @return List<Record>, a new list instance with an updated record list
     */
    protected List<Record> modifyRecordDisplayList(List<Record> recordsForFormInstance, Survey survey) {
        return recordsForFormInstance;
    }
}