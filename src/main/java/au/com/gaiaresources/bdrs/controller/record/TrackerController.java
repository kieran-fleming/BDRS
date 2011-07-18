package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
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
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormFieldFactory;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationNameComparator;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;
import au.com.gaiaresources.bdrs.util.DateFormatter;
import au.com.gaiaresources.bdrs.util.DateUtils;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Controller
public class TrackerController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    
    public static final String TAXON_GROUP_ATTRIBUTE_PREFIX = "taxonGroupAttr_";
    public static final String CENSUS_METHOD_ATTRIBUTE_PREFIX = "censusMethodAttr_";
    
    public static final String TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE_KEY = "Tracker.TaxonAndNumberRequiredTogether";
    public static final String TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE = "Species and number must both be blank, or both filled in.";

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
    private FileService fileService;
    @Autowired
    private PropertyService propertyService;
    
    @Autowired
    private RedirectionService redirectionService;

    private FormFieldFactory formFieldFactory = new FormFieldFactory();

    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/tracker.htm", method = RequestMethod.GET)
    public ModelAndView addRecord(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyId", required = true) int surveyId,
            @RequestParam(value = "taxonSearch", required = false) String taxonSearch,
            @RequestParam(value = "recordId", required = false, defaultValue = "0") int recordId,
            @RequestParam(value = "guid", required = false) String guid,
            @RequestParam(value = "censusMethodId", required = false, defaultValue = "0") Integer censusMethodId) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = recordDAO.getRecord(recordId);
        CensusMethod censusMethod = record == null? cmDAO.get(censusMethodId) : record.getCensusMethod();
        
        record = record == null ? new Record() : record;
        
        IndicatorSpecies species = null;
        
        if (guid != null && !guid.isEmpty()) {
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

        // Add all attribute form fields
        FormField formField;
        Attribute attr;
        List<FormField> surveyFormFieldList = new ArrayList<FormField>();
        List<FormField> taxonGroupFormFieldList = new ArrayList<FormField>();
        List<FormField> censusMethodFormFieldList = new ArrayList<FormField>();
        List<Attribute> surveyAttributeList = new ArrayList<Attribute>(survey.getAttributes());
        List<Attribute> taxonGroupAttributeList = new ArrayList<Attribute>();
        List<Attribute> censusMethodAttributeList = censusMethod != null ? new ArrayList<Attribute>(censusMethod.getAttributes()) : new ArrayList<Attribute>();
        
        IndicatorSpecies sp = species != null ? species : record.getSpecies();
        if(sp != null) {
            for(Attribute taxonGroupAttribute : sp.getTaxonGroup().getAttributes()) {
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
                formField = formFieldFactory.createRecordFormField(survey, record, attr, recAttr);
                surveyFormFieldList.add(formField);
            } else if(taxonGroupAttributeList.remove(attr)) {
                formField = formFieldFactory.createRecordFormField(survey, record, attr, recAttr, TAXON_GROUP_ATTRIBUTE_PREFIX);
                taxonGroupFormFieldList.add(formField);
            } else if (censusMethodAttributeList.remove(attr)) {
                formField = formFieldFactory.createRecordFormField(survey, record, attr, recAttr, CENSUS_METHOD_ATTRIBUTE_PREFIX);
            }
        }
        // If there were no pre-existing values for the attributes, add 
        // the blank fields now.
        for (Attribute surveyAttr : surveyAttributeList) {
            surveyFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, surveyAttr));
        }
        for (Attribute taxonGroupAttr : taxonGroupAttributeList) {
            taxonGroupFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, taxonGroupAttr, TAXON_GROUP_ATTRIBUTE_PREFIX));
        }

    	Taxonomic taxonomic;
    	if(censusMethod != null && censusMethod.getTaxonomic() != null) {
    		taxonomic = censusMethod.getTaxonomic();
    	}
    	else {
    		taxonomic = Taxonomic.OPTIONALLYTAXONOMIC;
    	}
    	
        String[] recordProperties;
        if(Taxonomic.OPTIONALLYTAXONOMIC.equals(taxonomic) || Taxonomic.TAXONOMIC.equals(taxonomic)) {
        	recordProperties = Record.RECORD_PROPERTY_NAMES;
        } else {
        	recordProperties = Record.NON_TAXONOMIC_RECORD_PROPERTY_NAMES;
        }
        // Add all property form fields
        for (String propertyName : recordProperties) {
            surveyFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, propertyName, species, taxonomic));
        }
        
        // Add census method form fields
        for (Attribute cmAttr : censusMethodAttributeList) {
            censusMethodFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, cmAttr, CENSUS_METHOD_ATTRIBUTE_PREFIX));
        }
        
        Collections.sort(surveyFormFieldList);
        Collections.sort(taxonGroupFormFieldList);
        Collections.sort(censusMethodFormFieldList);
        
        Map<String, String> errorMap = (Map<String, String>)getRequestContext().getSessionAttribute("errorMap");
        getRequestContext().removeSessionAttribute("errorMap");
        Map<String, String> valueMap = (Map<String, String>)getRequestContext().getSessionAttribute("valueMap");
        getRequestContext().removeSessionAttribute("valueMap");
        
        Metadata predefinedLocationsMD = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        boolean predefinedLocationsOnly = predefinedLocationsMD != null && 
            Boolean.parseBoolean(predefinedLocationsMD.getValue());
        
        Set<Location> locations = new TreeSet<Location>(new LocationNameComparator());
        locations.addAll(survey.getLocations());
        if(!predefinedLocationsOnly) {
            locations.addAll(locationDAO.getUserLocations(getRequestContext().getUser()));
        }
        
        ModelAndView mv = new ModelAndView("tracker");
        mv.addObject("censusMethod", censusMethod);
        mv.addObject("record", record);
        mv.addObject("survey", survey);
        mv.addObject("locations", locations);
        mv.addObject("surveyFormFieldList", surveyFormFieldList);
        mv.addObject("taxonGroupFormFieldList", taxonGroupFormFieldList);
        mv.addObject("censusMethodFormFieldList", censusMethodFormFieldList);
        mv.addObject("preview", request.getParameter("preview") != null);
        mv.addObject("taxonomic", taxonomic);
        
        mv.addObject("errorMap", errorMap);
        mv.addObject("valueMap", valueMap);
        
        return mv;
    }

    @SuppressWarnings("unchecked")
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/tracker.htm", method = RequestMethod.POST)
    public ModelAndView saveRecord(MultipartHttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value="surveyId", required=true) int surveyPk,
            // We are allowing a null census method ID to indicate a default form
            @RequestParam(value="censusMethodId", required=false, defaultValue="0") int censusMethodId) throws ParseException, IOException {

        Survey survey = surveyDAO.getSurvey(surveyPk);
        CensusMethod censusMethod = cmDAO.get(censusMethodId);

        Taxonomic taxonomic;
    	if(censusMethod != null && censusMethod.getTaxonomic() != null) {
    		taxonomic = censusMethod.getTaxonomic();
    	}
    	else {
    		taxonomic = Taxonomic.OPTIONALLYTAXONOMIC;
    	}
        
        IndicatorSpecies species;
        try {
            species = taxaDAO.getIndicatorSpecies(Integer.parseInt(request.getParameter("species")));
        } catch (NumberFormatException nfe) {
            species = null;
        }
        
        // Validate Mandatory Fields
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);
        Map<String, String[]> params = request.getParameterMap();
        Map<String, String[]> dateRangeParams = new HashMap<String, String[]>(params);
        dateRangeParams.put("dateRange", 
                   new String[] {
                        survey.getStartDate() == null ? "" : DateFormatter.format(survey.getStartDate(), DateFormatter.DAY_MONTH_YEAR),
                        survey.getEndDate() == null ? "" : DateFormatter.format(survey.getEndDate(), DateFormatter.DAY_MONTH_YEAR) } );
        
        boolean isValid = validator.validate(params, ValidationType.REQUIRED_BLANKABLE_STRING, "notes", null)
                & validator.validate(params, ValidationType.REQUIRED_DEG_LATITUDE, "latitude", null)
                & validator.validate(params, ValidationType.REQUIRED_DEG_LONGITUDE, "longitude", null)
                & validator.validate(params, ValidationType.REQUIRED_HISTORICAL_DATE, "date", null)
                & validator.validate(params, ValidationType.DOUBLE, "accuracyInMeters", null)
                & validator.validate(dateRangeParams, ValidationType.DATE_WITHIN_RANGE, "date", null);

        boolean isTaxonomicRecord = Taxonomic.TAXONOMIC.equals(taxonomic) || Taxonomic.OPTIONALLYTAXONOMIC.equals(taxonomic);
        String speciesSearch = request.getParameter("survey_species_search");
    	String numberString = request.getParameter("number");
    	
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
        	
        	isValid &= validator.validate(params, numberValidationType, "number", null);
            
        	// No need to check if the species primary key has already resolved a species
            if(species == null) {
                isValid = isValid & validator.validate(params, speciesValidationType, "survey_species_search", null);
            }
            
            // If the record is optionally taxonomic and there is a species with
            // no number or a number with no species, then there is an error
            if(Taxonomic.OPTIONALLYTAXONOMIC.equals(taxonomic)) {
            	if(		(species == null) &&
            			(!((speciesSearch.isEmpty() && numberString.isEmpty()) ||
            			(!speciesSearch.isEmpty() && !numberString.isEmpty())))) {
            		isValid = false;
            		Map<String, String> errorMap = validator.getErrorMap();
            		String errMsg = propertyService.getMessage(
            				TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE_KEY, 
            				TAXON_AND_NUMBER_REQUIRED_TOGETHER_MESSAGE);
            		errorMap.put("number", errMsg);
            		errorMap.put("survey_species_search", errMsg);
            	}
            }
        }
        
        AttributeParser attributeParser = new AttributeParser();
        for(Attribute attr : survey.getAttributes()) {
            isValid = isValid & attributeParser.validate(validator, attr, params, request.getFileMap());
        }
        if(species != null) {
            for(Attribute attr : species.getTaxonGroup().getAttributes()) {
                if(!attr.isTag()) {
                    isValid = isValid & attributeParser.validate(validator, TAXON_GROUP_ATTRIBUTE_PREFIX, attr, params, request.getFileMap());
                }
            }
        }
        
        if(!isValid) {
            Map<String, String> valueMap = new HashMap<String, String>();
            for(Map.Entry<String, String[]> entry : params.entrySet()) {
                if(entry.getValue() != null && entry.getValue().length > 0) {
                    valueMap.put(entry.getKey(), entry.getValue()[0]);
                }
            }
            getRequestContext().setSessionAttribute("errorMap", validator.getErrorMap());
            getRequestContext().setSessionAttribute("valueMap", valueMap);
            
            ModelAndView mv = new ModelAndView(new RedirectView("/bdrs/user/tracker.htm", true));
            mv.addObject("surveyId", surveyPk);
            mv.addObject("censusMethodId", new Integer(censusMethodId));
            if(species != null) {
                mv.addObject("taxonSearch", species.getScientificName());
            }
            String recordId = request.getParameter("recordId");
            if(recordId != null && !recordId.isEmpty()) {
                mv.addObject("recordId", Integer.parseInt(recordId));
            }
            getRequestContext().addMessage("form.validation");
            return mv;
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
        	else if(!speciesSearch.isEmpty() && !numberString.isEmpty()) {
        		species = taxaDAO.getIndicatorSpeciesByScientificName(speciesSearch);
        		number = new Integer(numberString);
        	}
        } else {
        	species = null;
        	number = null;
        }
        
        User user = getRequestContext().getUser();

        Record record;
        if (request.getParameter("recordId") != null
                && !request.getParameter("recordId").isEmpty()) {
            record = recordDAO.getRecord(Integer.parseInt(request.getParameter("recordId")));
        } else {
            record = new Record();
        }

        // Check if taxonomic record!
        record.setSpecies(species);
        record.setNumber(number);
        
        record.setUser(user);
        record.setSurvey(survey);
        record.setNotes(request.getParameter("notes"));
        record.setHeld(false);
        record.setFirstAppearance(false);
        record.setLastAppearance(false);
        // is possible to set this to null
        record.setCensusMethod(censusMethod);

        // Dates
        int hour = Integer.parseInt(request.getParameter("time_hour"));
        int minute = Integer.parseInt(request.getParameter("time_minute"));

        Calendar cal = new GregorianCalendar();
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Date date = dateFormat.parse(request.getParameter("date"));
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.clear(Calendar.MILLISECOND);
        date = cal.getTime();
        record.setWhen(date);
        record.setTime(date.getTime());
        record.setLastDate(date);
        record.setLastTime(date.getTime());

        // Position
        double latitude = Double.parseDouble(request.getParameter("latitude"));
        double longitude = Double.parseDouble(request.getParameter("longitude"));
        record.setPoint(locationService.createPoint(latitude, longitude));
        
        Location loc = null;
        // First try to get the location by primary key.
        if(request.getParameter("location") != null) {
            int locationId = Integer.parseInt(request.getParameter("location"));
            // At this point locationId may be -1 and therefore loc will be null.
            loc = locationDAO.getLocation(locationId);
        }
        
        // If the location lookup fails, try to see if we should create
        // a new location.
        if(loc == null) {
            String locationName = request.getParameter("locationName");
            if(locationName != null && !locationName.isEmpty()) {
                loc = new Location();
                loc.setName(locationName);
                loc.setLocation(locationService.createPoint(latitude, longitude));
                loc = locationDAO.save(loc);
            }
        }
        // This loc here may still be null but that is ok.
        record.setLocation(loc);
        
        String accuracyStr = request.getParameter("accuracyInMeters");
        record.setAccuracyInMeters(StringUtils.notEmpty(accuracyStr) ? Double.parseDouble(accuracyStr) : null);

        // Attach the record Attributes.
        AttributeValue recAttr;
        List<AttributeValue> attrValuesToDelete = new ArrayList<AttributeValue>();

        // Survey Attributes
        for (Attribute attribute : survey.getAttributes()) {
            recAttr = attributeParser.parse(attribute, record, request.getParameterMap(), request.getFileMap());
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
                    recAttr = attributeParser.parse(TAXON_GROUP_ATTRIBUTE_PREFIX, attribute, record, request.getParameterMap(), request.getFileMap());
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
                    recAttr = attributeParser.parse(CENSUS_METHOD_ATTRIBUTE_PREFIX, attribute, record, request.getParameterMap(), request.getFileMap());
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
        for(AttributeValue attrVal : attrValuesToDelete) {
            recordDAO.saveAttributeValue(attrVal);
            recordDAO.delete(attrVal);
        }

        ModelAndView mv;

        if (request.getParameter("submitAndAddAnother") != null) {
            mv = new ModelAndView(new RedirectView(
                    "/bdrs/user/surveyRenderRedirect.htm", true));
            mv.addObject("surveyId", survey.getId());            
            mv.addObject("censusMethodId", new Integer(censusMethodId));
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
        }

        return mv;
    }
    
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/ajaxTrackerTaxonAttributeTable.htm", method = RequestMethod.GET)
    public ModelAndView ajaxTaxonAttributeTable(HttpServletRequest request,
                                                HttpServletResponse response,
                                                @RequestParam(value="surveyId", required=true) int surveyPk,
                                                @RequestParam(value="taxonId", required=true) int taxonPk,
                                                @RequestParam(value="recordId", required=false, defaultValue="0") int recordPk) {
 
        Survey survey = surveyDAO.getSurvey(surveyPk);
        IndicatorSpecies taxon = taxaDAO.getIndicatorSpecies(taxonPk);
        Record record = recordPk > 0 ? recordDAO.getRecord(recordPk) : new Record();
        
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
        return mv;
    }
}