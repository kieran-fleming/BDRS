package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormFieldFactory;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializer;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializerResult;
import au.com.gaiaresources.bdrs.deserialization.record.RecordEntry;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationNameComparator;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeUtil;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValueUtil;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Controller for the default 'tracker' form.
 * 
 */
@Controller
public class TrackerController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    
    public static final String EDIT_URL = "/bdrs/user/tracker.htm";
    
    public static final String TAXON_GROUP_ATTRIBUTE_PREFIX = "taxonGroupAttr_";
    public static final String CENSUS_METHOD_ATTRIBUTE_PREFIX = "censusMethodAttr_";
    
    public static final String TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE_KEY = "Tracker.TaxonAndNumberRequiredTogether";
    public static final String TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE = "Species and number must both be blank, or both filled in.";
    
    public static final String PARAM_SURVEY_ID = "surveyId";
    public static final String PARAM_CENSUS_METHOD_ID = "censusMethodId";
    public static final String PARAM_SPECIES_ID = "species";
    public static final String PARAM_SPECIES_NAME = "survey_species_search";
    public static final String PARAM_INDIVIDUAL_COUNT = "number";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_NOTES = "notes";
    public static final String PARAM_TIME_HOUR = "time_hour";
    public static final String PARAM_TIME_MINUTE = "time_minute";
    public static final String PARAM_DATE = "date";
    public static final String PARAM_LATITUDE = "latitude";
    public static final String PARAM_LONGITUDE = "longitude";
    public static final String PARAM_LOCATION = "location";
    public static final String PARAM_ACCURACY = "accuracyInMeters";
    public static final String PARAM_LOCATION_NAME = "locationName";
    public static final String PARAM_TIME = "time";
    public static final String PARAM_WKT = "wkt";
    public static final String PARAM_RECORD_VISIBILITY = "recordVisibility";
    
    public static final String MV_WKT = "wkt";
    public static final String MV_ERROR_MAP = "errorMap";
    
    public static final String NO_SURVEY_ERROR_KEY = "bdrs.record.noSurveyError";
    
    public static final String TRACKER_VIEW_NAME = "tracker";

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private CensusMethodDAO cmDAO;
    
    @Autowired
    private LocationService locationService;
    
    @Autowired
    private RedirectionService redirectionService;
    
    @Autowired
    private MetadataDAO metadataDAO;

    private FormFieldFactory formFieldFactory = new FormFieldFactory();

    /**
     * Handler to get the tracker form. Will populate the form with values if an existing
     * record ID is passed.
     * 
     * @param request - the http request object
     * @param response - the http response object
     * @param surveyId - the ID for the survey to create a new record for. Is still required when requesting
     * an existing record
     * @param taxonSearch - a taxon scientific name to search for to populate the species field of a record
     * @param recordId - the ID for the record to retrieve and populate form values with
     * @param guid - a guid for a species to search for to populate the species field of a record
     * @param censusMethodId - the census method id to use when creating a new record
     * @return a ModelAndView for rendering the tracker form
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = EDIT_URL, method = RequestMethod.GET)
    public ModelAndView addRecord(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = PARAM_SURVEY_ID, required = true) int surveyId,
            @RequestParam(value = "taxonSearch", required = false) String taxonSearch,
            @RequestParam(value = PARAM_RECORD_ID, required = false, defaultValue = "0") int recordId,
            @RequestParam(value = "guid", required = false) String guid,
            @RequestParam(value = PARAM_CENSUS_METHOD_ID, required = false, defaultValue = "0") Integer censusMethodId,
            @RequestParam(value = "speciesId", required = false, defaultValue="0") Integer speciesId) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        if (survey == null) {
            return nullSurveyError();
        }
        
        Record record = recordDAO.getRecord(recordId);
        CensusMethod censusMethod = record == null? cmDAO.get(censusMethodId) : record.getCensusMethod();
        
        record = record == null ? new Record() : record;
        
        User loggedInUser = getRequestContext().getUser();
        RecordWebFormContext context = new RecordWebFormContext(request, record, loggedInUser, survey);
        
        // if this is a new record...
        if (record.getId() == null) {
            // set survey specific record defaults
            record.setRecordVisibility(survey.getDefaultRecordVisibility());
        }
        
        IndicatorSpecies species = null;
        
        // check for the speciesId first
        if (speciesId > 0) {
            species = taxaDAO.getIndicatorSpecies(speciesId);
        }
        // first attempt to assign to the species from the form fields....
        if (species == null && guid != null && !guid.isEmpty()) {
            species = taxaDAO.getIndicatorSpeciesByGuid(guid);
        } 
        if (species == null && taxonSearch != null && !taxonSearch.isEmpty()) {
            List<IndicatorSpecies> speciesList = surveyDAO.getSpeciesForSurveySearch(surveyId, taxonSearch);
            if (speciesList.isEmpty()) {
                species = null;
            } else if (speciesList.size() == 1) {
                species = speciesList.get(0);
            } else {
                log.warn("Multiple species found for survey " + surveyId
                        + " and taxon search \"" + taxonSearch
                        + "\". Using the first.");
                species = speciesList.get(0);
            }
        }
        // if the species is still null, use the species from the record.
        species = species != null ? species : record.getSpecies();
        

        // Add all attribute form fields
        FormField formField;
        Attribute attr;
        List<FormField> surveyFormFieldList = new ArrayList<FormField>();
        List<FormField> taxonGroupFormFieldList = new ArrayList<FormField>();
        List<FormField> censusMethodFormFieldList = new ArrayList<FormField>();
        List<Attribute> surveyAttributeList = new ArrayList<Attribute>(survey.getAttributes());
        List<Attribute> taxonGroupAttributeList = new ArrayList<Attribute>();
        List<Attribute> censusMethodAttributeList = censusMethod != null ? new ArrayList<Attribute>(censusMethod.getAttributes()) : new ArrayList<Attribute>();
        
        if(species != null) {
            for(Attribute taxonGroupAttribute : species.getTaxonGroup().getAttributes()) {
                if(!taxonGroupAttribute.isTag()) {
                    taxonGroupAttributeList.add(taxonGroupAttribute);
                }
            }
        }
        
        for (TypedAttributeValue recAttr : record.getAttributes()) {
            attr = recAttr.getAttribute();
            // If you are a survey attribute, add to the survey form fields
            // otherwise add to the group form fields. This is done because
            // group form fields are sorted separately with survey form fields
            // displayed above group form fields.
            if(surveyAttributeList.remove(attr)) {
                // only add moderation attributes if the user is a moderator
                if (AttributeUtil.isVisibleByScopeAndUser(attr, loggedInUser, recAttr) ||
                        !AttributeScope.isModerationScope(attr.getScope())) {
                    formField = formFieldFactory.createRecordFormField(survey, record, attr, recAttr);
                    surveyFormFieldList.add(formField);
                }
            } else if(taxonGroupAttributeList.remove(attr)) {
                formField = formFieldFactory.createRecordFormField(survey, record, attr, recAttr, TAXON_GROUP_ATTRIBUTE_PREFIX);
                taxonGroupFormFieldList.add(formField);
            } else if (censusMethodAttributeList.remove(attr)) {
                formField = formFieldFactory.createRecordFormField(survey, record, attr, recAttr, CENSUS_METHOD_ATTRIBUTE_PREFIX);
                censusMethodFormFieldList.add(formField);
            }
        }
        // If there were no pre-existing values for the attributes, add 
        // the blank fields now.
        for (Attribute surveyAttr : surveyAttributeList) {
            if(!AttributeScope.LOCATION.equals(surveyAttr.getScope())) {
                AttributeValue attrVal = AttributeValueUtil.getAttributeValue(surveyAttr, record);
                // only add moderation attributes if the user is a moderator
                if (AttributeUtil.isVisibleByScopeAndUser(surveyAttr, loggedInUser, attrVal) ||
                        !AttributeScope.isModerationScope(surveyAttr.getScope())) {
                    surveyFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, surveyAttr));
                }
            }
        }
        for (Attribute taxonGroupAttr : taxonGroupAttributeList) {
            taxonGroupFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, taxonGroupAttr, TAXON_GROUP_ATTRIBUTE_PREFIX));
        }
        // Add census method form fields
        for (Attribute cmAttr : censusMethodAttributeList) {
            censusMethodFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, cmAttr, CENSUS_METHOD_ATTRIBUTE_PREFIX));
        }

    	Taxonomic taxonomic;
    	if(censusMethod != null && censusMethod.getTaxonomic() != null) {
    		taxonomic = censusMethod.getTaxonomic();
    	}
    	else {
    		taxonomic = Taxonomic.OPTIONALLYTAXONOMIC;
    	}
    	
    	RecordPropertyType[] recordProperties;
        if(Taxonomic.OPTIONALLYTAXONOMIC.equals(taxonomic) || Taxonomic.TAXONOMIC.equals(taxonomic)) {
            recordProperties = RecordPropertyType.values();
        } else {
            recordProperties = Record.NON_TAXONOMIC_RECORD_PROPERTY_NAMES;
        }
        // Add all property form fields
        for (RecordPropertyType type : recordProperties) {
            RecordProperty recordProperty = new RecordProperty(survey, type, metadataDAO);
            surveyFormFieldList.add(formFieldFactory.createRecordFormField(record, recordProperty, species, taxonomic));
        }
        
        Collections.sort(surveyFormFieldList);
        Collections.sort(taxonGroupFormFieldList);
        Collections.sort(censusMethodFormFieldList);
        
        Map<String, String> errorMap = (Map<String, String>)getRequestContext().getSessionAttribute(MV_ERROR_MAP);
        getRequestContext().removeSessionAttribute(MV_ERROR_MAP);
        Map<String, String> valueMap = (Map<String, String>)getRequestContext().getSessionAttribute("valueMap");
        getRequestContext().removeSessionAttribute("valueMap");
        String wktString = (String)getRequestContext().getSessionAttribute(MV_WKT);
        getRequestContext().removeSessionAttribute(MV_WKT);
        
        Metadata predefinedLocationsMD = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        boolean predefinedLocationsOnly = predefinedLocationsMD != null && 
            Boolean.parseBoolean(predefinedLocationsMD.getValue());
        
        Set<Location> locations = new TreeSet<Location>(new LocationNameComparator());
        locations.addAll(survey.getLocations());
        if(!predefinedLocationsOnly) {
            locations.addAll(locationDAO.getUserLocations(getRequestContext().getUser()));
        }
        
        ModelAndView mv = new ModelAndView(TRACKER_VIEW_NAME);
        mv.addObject("censusMethod", censusMethod);
        mv.addObject("record", record);
        mv.addObject("recordId", record.getId());
        mv.addObject("survey", survey);
        mv.addObject("locations", locations);
        mv.addObject("surveyFormFieldList", surveyFormFieldList);
        mv.addObject("taxonGroupFormFieldList", taxonGroupFormFieldList);
        mv.addObject("censusMethodFormFieldList", censusMethodFormFieldList);
        mv.addObject("preview", request.getParameter("preview") != null);
        mv.addObject("taxonomic", taxonomic);
        mv.addObject(RecordWebFormContext.MODEL_WEB_FORM_CONTEXT, context);
        
        if (StringUtils.hasLength(wktString)) {
            mv.addObject(MV_WKT, wktString);
        } else {
            mv.addObject(MV_WKT, (record != null && record.getGeometry() != null) ? record.getGeometry().toText() : "");   
        }
        
        mv.addObject(MV_ERROR_MAP, errorMap);
        mv.addObject("valueMap", valueMap);
        
        return mv;
    }

    /**
     * The POST handler for saving tracker forms
     * 
     * @param request - the http request object
     * @param response - the http response object
     * @param surveyPk - the survey to create  a new record for (if not editing an existing record)
     * @param censusMethodId - the census method to create a new record for (if not editing an existing record)
     * @return - A RedirectView
     * @throws ParseException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = EDIT_URL, method = RequestMethod.POST)
    public ModelAndView saveRecord(MultipartHttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value=PARAM_SURVEY_ID, required=true) int surveyPk,
            // We are allowing a null census method ID to indicate a default form
            @RequestParam(value=PARAM_CENSUS_METHOD_ID, required=false, defaultValue="0") int censusMethodId) throws ParseException, IOException {
        Map<String, String[]> parameterMap = this.getModifiableParameterMap(request);
        
        Survey survey = surveyDAO.getSurvey(surveyPk);
        if (survey == null) {
            return nullSurveyError();
        }
        
        RecordKeyLookup lookup = new TrackerFormRecordKeyLookup();
        TrackerFormToRecordEntryTransformer transformer = new TrackerFormToRecordEntryTransformer(locationService);
        TrackerFormAttributeDictionaryFactory adf = new TrackerFormAttributeDictionaryFactory();
        AttributeParser parser = new WebFormAttributeParser();
        
        RecordDeserializer rds = new RecordDeserializer(lookup, adf, parser);
        List<RecordEntry> entries = transformer.httpRequestParamToRecordMap(parameterMap, request.getFileMap());
        List<RecordDeserializerResult> results = rds.deserialize(getRequestContext().getUser(), entries);
        
        // there should be exactly 1 result since we are only putting in 1 RecordEntry...
        if (results.size() != 1) {
            log.warn("Expecting only 1 deserialization result but got: " + results.size());
        }
        RecordDeserializerResult res = results.get(0);
        
        if (!res.isAuthorizedAccess()) {
            // Required since there will be an auto commit otherwise at the end of controller handling.
            requestRollback(request);
            
            throw new AccessDeniedException(RecordWebFormContext.MSG_CODE_EDIT_AUTHFAIL);
        }
        
        if (!res.getErrorMap().isEmpty()) {
            // an error has occured
            requestRollback(request);
            
            // create valueMap to repopulate the form...
            Map<String, String[]> params = request.getParameterMap();
            Map<String, String> valueMap = new HashMap<String, String>();
            for(Map.Entry<String, String[]> paramEntry : params.entrySet()) {
                if(paramEntry.getValue() != null && paramEntry.getValue().length > 0) {
                    if(paramEntry.getValue().length == 1) {
                            valueMap.put(paramEntry.getKey(), paramEntry.getValue()[0]);
                    } else {
                            // Not bothering with a csv writer here because the
                            // jsp template does a simple String.contains to check
                            // if the the multi select or multi combo should be picked.
                            StringBuilder b = new StringBuilder();
                            for(int q = 0; q<paramEntry.getValue().length; q++) {
                                    b.append(paramEntry.getValue()[q]);
                                    b.append(',');
                            }
                            valueMap.put(paramEntry.getKey(), b.toString());
                    }
                }
            }
            
            // collect some details for reporting
            IndicatorSpecies species;
            try {
                species = taxaDAO.getIndicatorSpecies(Integer.parseInt(request.getParameter(PARAM_SPECIES_ID)));
            } catch (NumberFormatException nfe) {
                species = null;
            }
            
            getRequestContext().setSessionAttribute(MV_ERROR_MAP, res.getErrorMap());
            getRequestContext().setSessionAttribute("valueMap", valueMap);
            getRequestContext().setSessionAttribute(MV_WKT, request.getParameter(PARAM_WKT));
            
            ModelAndView mv = new ModelAndView(new RedirectView("/bdrs/user/tracker.htm", true));
            mv.addObject(MV_ERROR_MAP, res.getErrorMap());

            mv.addObject("surveyId", surveyPk);
            mv.addObject("censusMethodId", Integer.valueOf(censusMethodId));
            if(species != null) {
                mv.addObject("taxonSearch", species.getScientificName());
            }
            String recordId = request.getParameter(PARAM_RECORD_ID);
            if(recordId != null && !recordId.isEmpty()) {
                mv.addObject(PARAM_RECORD_ID, Integer.parseInt(recordId));
            }
            getRequestContext().addMessage("form.validation");
            return mv;
        }
        
        ModelAndView mv;

        if (request.getParameter("submitAndAddAnother") != null) {
            mv = new ModelAndView(new RedirectView(
                    "/bdrs/user/surveyRenderRedirect.htm", true));
            mv.addObject("surveyId", survey.getId());            
            mv.addObject("censusMethodId", Integer.valueOf(censusMethodId));
            getRequestContext().addMessage(new Message(
                    "bdrs.record.save.successAddAnother"));
        } else {
            if (request.getSession().getAttribute("redirecturl") != null) {
                mv = new ModelAndView("redirect:"
                        + request.getSession().getAttribute("redirecturl"));
            } else if (request.getParameter("redirecturl") != null) { 
            	mv = new ModelAndView("redirect:"
                        + request.getParameter("redirecturl"));
            	getRequestContext().addMessage(new Message("bdrs.record.save.success"));
            } else {
                mv = new ModelAndView(new RedirectView(redirectionService.getMySightingsUrl(survey),
                        true));
                getRequestContext().addMessage(new Message("bdrs.record.save.success"));
            }
            
            // add the record id to the redirection view for record highlighting
            if (res.getRecord() != null) {
            	mv.addObject("record_id", res.getRecord().getId());
            }
        }

        return mv;
    }
    
    /**
     * Ajax handler for returning the taxon attribute table, a dynamic set of fields
     * for displaying taxon group attributes when the species field on the form is
     * changed
     * 
     * @param request - the http request object
     * @param response - the http response object
     * @param surveyPk - the survey id to create form fields with
     * @param taxonPk - the taxon id for the species to retrieve the taxon group from
     * @param recordPk - the record id to create form fields with
     * @return ModelAndView used to render the table
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/ajaxTrackerTaxonAttributeTable.htm", method = RequestMethod.GET)
    public ModelAndView ajaxTaxonAttributeTable(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestParam(value="surveyId", required=true) int surveyPk,
                                                @RequestParam(value="taxonId", required=true) int taxonPk,
                                                @RequestParam(value=PARAM_RECORD_ID, required=false, defaultValue="0") int recordPk) {
 
        Survey survey = surveyDAO.getSurvey(surveyPk);
        IndicatorSpecies taxon = taxaDAO.getIndicatorSpecies(taxonPk);
        Record record = recordPk > 0 ? recordDAO.getRecord(recordPk) : new Record();
        
        User loggedInUser = getRequestContext().getUser();
        RecordWebFormContext context = new RecordWebFormContext(request, record, loggedInUser, survey);
        
        List<FormField> formFieldList = new ArrayList<FormField>();
        List<Attribute> taxonGroupAttributeList = new ArrayList<Attribute>();
        for(Attribute taxonGroupAttribute : taxon.getTaxonGroup().getAttributes()) {
            if(!taxonGroupAttribute.isTag()) {
                taxonGroupAttributeList.add(taxonGroupAttribute);
            }
        }
        
        // For those attribute that have a record attribute, create
        // the form field with that value pre-populated
        Attribute attr;
        for(AttributeValue recAttr : record.getAttributes()) {
            attr = recAttr.getAttribute();
            if(taxonGroupAttributeList.remove(attr)) {
                // its a taxon group attribute
                formFieldList.add(formFieldFactory.createRecordFormField(survey, record, attr, recAttr, TAXON_GROUP_ATTRIBUTE_PREFIX));
            }
        }

        // Add the remaining taxon group attributes.
        for (Attribute taxonGroupAttr : taxonGroupAttributeList) {
            if(!taxonGroupAttr.isTag()) {
                formFieldList.add(formFieldFactory.createRecordFormField(survey, record, taxonGroupAttr, TAXON_GROUP_ATTRIBUTE_PREFIX));
            }
        }
        
        Collections.sort(formFieldList);
        
        ModelAndView mv = new ModelAndView("formFieldListRenderer");
        mv.addObject("formFieldList", formFieldList);
        // this isn't an entire web form so it doesn't need the complete web form context
        mv.addObject(RecordWebFormContext.MODEL_EDIT, context.isEditable());
        return mv;
    }
    
    private ModelAndView nullSurveyError() {
        getRequestContext().addMessage(NO_SURVEY_ERROR_KEY);
        return new ModelAndView(new RedirectView(redirectionService.getMySightingsUrl(null), true));
    }
}
