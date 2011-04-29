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
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

@Controller
public class TrackerController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    
    public static final String TAXON_GROUP_ATTRIBUTE_PREFIX = "taxonGroupAttr_";

    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private LocationDAO locationDAO;

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
            @RequestParam(value = "guid", required = false) String guid) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = recordDAO.getRecord(recordId);
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
        List<Attribute> surveyAttributeList = new ArrayList<Attribute>(survey.getAttributes());
        List<Attribute> taxonGroupAttributeList = new ArrayList<Attribute>();
        
        IndicatorSpecies sp = species != null ? species : record.getSpecies();
        if(sp != null) {
            for(Attribute taxonGroupAttribute : sp.getTaxonGroup().getAttributes()) {
                if(!taxonGroupAttribute.isTag()) {
                    taxonGroupAttributeList.add(taxonGroupAttribute);
                }
            }
        }
        
        for (AttributeValue recAttr : record.getAttributes()) {
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

        // Add all property form fields
        for (String propertyName : Record.RECORD_PROPERTY_NAMES) {
            surveyFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, propertyName, species));
        }
        
        Collections.sort(surveyFormFieldList);
        Collections.sort(taxonGroupFormFieldList);
        
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
        mv.addObject("record", record);
        mv.addObject("survey", survey);
        mv.addObject("locations", locations);
        mv.addObject("surveyFormFieldList", surveyFormFieldList);
        mv.addObject("taxonGroupFormFieldList", taxonGroupFormFieldList);
        mv.addObject("preview", request.getParameter("preview") != null);
        
        mv.addObject("errorMap", errorMap);
        mv.addObject("valueMap", valueMap);
        
        return mv;
    }

    @SuppressWarnings("unchecked")
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/tracker.htm", method = RequestMethod.POST)
    public ModelAndView saveRecord(MultipartHttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value="surveyId", required=true) int surveyPk) throws ParseException, IOException {

        Survey survey = surveyDAO.getSurvey(surveyPk);
        IndicatorSpecies species;
        try {
            species = taxaDAO.getIndicatorSpecies(Integer.parseInt(request.getParameter("species")));
        } catch (NumberFormatException nfe) {
            species = null;
        }
        
        // Validate Mandatory Fields
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);
        Map<String, String[]> params = request.getParameterMap();
        
        boolean isValid = validator.validate(params, ValidationType.REQUIRED_POSITIVE_LESSTHAN, "number")
                & validator.validate(params, ValidationType.REQUIRED_BLANKABLE_STRING, "notes")
                & validator.validate(params, ValidationType.REQUIRED_DEG_LATITUDE, "latitude")
                & validator.validate(params, ValidationType.REQUIRED_DEG_LONGITUDE, "longitude")
                & validator.validate(params, ValidationType.REQUIRED_HISTORICAL_DATE, "date");
        // No need to check if the species primary key has already resolved a species
        if(species == null) {
            isValid = isValid & validator.validate(params, ValidationType.REQUIRED_TAXON, "survey_species_search");
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
        if(species == null) {
            species = taxaDAO.getIndicatorSpeciesByScientificName(request.getParameter("survey_species_search"));
        }
        
        User user = getRequestContext().getUser();

        Record record;
        if (request.getParameter("recordId") != null
                && !request.getParameter("recordId").isEmpty()) {
            record = recordDAO.getRecord(Integer.parseInt(request.getParameter("recordId")));
        } else {
            record = new Record();
        }

        record.setSpecies(species);
        record.setUser(user);
        record.setSurvey(survey);
        record.setNumber(Integer.parseInt(request.getParameter("number")));
        record.setNotes(request.getParameter("notes"));
        record.setHeld(false);
        record.setFirstAppearance(false);
        record.setLastAppearance(false);

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
        if(request.getParameter("location") != null) {
            int locationId = Integer.parseInt(request.getParameter("location"));
            // At this point locationId may be -1 and therefore loc will be null.
            Location loc = locationDAO.getLocation(locationId);
            record.setLocation(loc);
        }

        // Attach the record Attributes.
        RecordAttribute recAttr;

        // Survey Attributes
        for (Attribute attribute : survey.getAttributes()) {
            recAttr = attributeParser.parse(attribute, record, request.getParameterMap(), request.getFileMap());
            if (attributeParser.isAddOrUpdateAttribute()) {
                recAttr = recordDAO.saveRecordAttribute(recAttr);
                if (attributeParser.getAttrFile() != null) {
                    fileService.createFile(recAttr, attributeParser.getAttrFile());
                }
                record.getAttributes().add(recAttr);
            } else {
                record.getAttributes().remove(recAttr);
                recordDAO.delete(recAttr);
            }
        }
        
        // Taxon Group Attributes
        for (Attribute attribute : species.getTaxonGroup().getAttributes()) {
            if(!attribute.isTag()) {
                recAttr = attributeParser.parse(TAXON_GROUP_ATTRIBUTE_PREFIX, attribute, record, request.getParameterMap(), request.getFileMap());
                if (attributeParser.isAddOrUpdateAttribute()) {
                    recAttr = recordDAO.saveRecordAttribute(recAttr);
                    if (attributeParser.getAttrFile() != null) {
                        fileService.createFile(recAttr, attributeParser.getAttrFile());
                    }
                    record.getAttributes().add(recAttr);
                } else {
                    record.getAttributes().remove(recAttr);
                    recordDAO.delete(recAttr);
                }
            }
        }
        
        recordDAO.saveRecord(record);

        ModelAndView mv;

        if (request.getParameter("submitAndAddAnother") != null) {
            mv = new ModelAndView(new RedirectView(
                    "/bdrs/user/surveyRenderRedirect.htm", true));
            mv.addObject("surveyId", survey.getId());
            getRequestContext().addMessage(new Message(
                    "bdrs.record.save.successAddAnother"));
        } else {
            if (request.getSession().getAttribute("redirecturl") != null) {
                mv = new ModelAndView("redirect:"
                        + request.getSession().getAttribute("redirecturl"));
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
        for(RecordAttribute recAttr : record.getAttributes()) {
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