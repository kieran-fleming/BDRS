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
import java.util.Set;
import java.util.TreeSet;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
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
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationNameComparator;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * The <code>SingleSiteMultiTaxa</code> controller is a record add form renderer
 * that allows multiple sightings of differing taxa to be created for a single
 * location.
 * 
 * @author benk
 */
@Controller
public class SingleSiteMultiTaxaController extends AbstractController {
    
    private static final String[] SITE_RECORD_PROPERTY_NAMES = new String[] {
            Record.RECORD_PROPERTY_LOCATION, Record.RECORD_PROPERTY_POINT,
            Record.RECORD_PROPERTY_WHEN, Record.RECORD_PROPERTY_TIME,
            Record.RECORD_PROPERTY_NOTES };
    private static final String[] TAXA_RECORD_PROPERTY_NAMES = new String[] {
            Record.RECORD_PROPERTY_SPECIES, Record.RECORD_PROPERTY_NUMBER };
    
    public static final String PREFIX_TEMPLATE = "%d_";

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
    
    private FormFieldFactory formFieldFactory = new FormFieldFactory();
    
    @Autowired
    private RedirectionService redirectionService;

    /**
     * Displays a blank form displaying inputs for the latitude, longitude,
     * date, time and notes.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey where the record shall be added.
     * @return
     */
    @RolesAllowed( {"ROLE_USER","ROLE_STUDENT","ROLE_POWERSTUDENT","ROLE_TEACHER","ROLE_ADMIN"} )
    @RequestMapping(value = "/bdrs/user/singleSiteMultiTaxa.htm", method = RequestMethod.GET)
    public ModelAndView addRecord(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value="surveyId", required=true) int surveyId) {

        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = null;
        if(request.getParameter("recordId") != null && !request.getParameter("recordId").isEmpty()) {
            record = recordDAO.getRecord(Integer.parseInt(request.getParameter("recordId")));
        }
        else {
            record = new Record();
        }
        
        // Add survey scope attribute form fields
        List<FormField> sightingRowFormFieldList = new ArrayList<FormField>();
        List<FormField> formFieldList = new ArrayList<FormField>();
        for(Attribute attribute : survey.getAttributes()) {
            if(!attribute.isTag()) {
                if(AttributeScope.SURVEY.equals(attribute.getScope())) {
                    formFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute));
                }
                else {
                    sightingRowFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute));
                }
            }
        }
        // Add all property form fields
        for (String propertyName : SITE_RECORD_PROPERTY_NAMES) {
            formFieldList.add(formFieldFactory.createRecordFormField(survey, record, propertyName));
        }
        for (String propertyName : TAXA_RECORD_PROPERTY_NAMES) {
            // No need for a prefix here because this is only used to generate a header
            sightingRowFormFieldList.add(formFieldFactory.createRecordFormField(survey, record, propertyName));
        }
        Collections.sort(formFieldList);
        Collections.sort(sightingRowFormFieldList);
        
        Metadata predefinedLocationsMD = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        boolean predefinedLocationsOnly = predefinedLocationsMD != null && 
            Boolean.parseBoolean(predefinedLocationsMD.getValue());
        
        Set<Location> locations = new TreeSet<Location>(new LocationNameComparator());
        locations.addAll(survey.getLocations());
        if(!predefinedLocationsOnly) {
            locations.addAll(locationDAO.getUserLocations(getRequestContext().getUser()));
        }
        
        ModelAndView mv = new ModelAndView("singleSiteMultiTaxa");
        mv.addObject("record", record);
        mv.addObject("survey", survey);
        mv.addObject("locations", locations);
        mv.addObject("preview", request.getParameter("preview") != null);
        mv.addObject("formFieldList", formFieldList);
        mv.addObject("sightingRowFormFieldList", sightingRowFormFieldList);
        
        return mv;
    }
    
    /**
     * Provides a single row representing a sighting. This view is typically
     * invoked by an AJAX request.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey where the record shall be added.
     * @param sightingIndex the row index where 0 is the first row.
     * @return 
     */
    @RolesAllowed( {"ROLE_USER","ROLE_STUDENT","ROLE_POWERSTUDENT","ROLE_TEACHER","ROLE_ADMIN"} )
    @RequestMapping(value = "/bdrs/user/singleSiteMultiTaxa/sightingRow.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddSightingRow(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value="surveyId", required=true) int surveyId,
                                    @RequestParam(value="sightingIndex", defaultValue="0") int sightingIndex) {
        
        Survey survey = surveyDAO.getSurvey(surveyId);
        Record record = new Record();
        
        // Add survey scope attribute form fields
        String prefix = String.format(PREFIX_TEMPLATE, sightingIndex);
        List<FormField> formFieldList = new ArrayList<FormField>();
        for(Attribute attribute : survey.getAttributes()) {
            if(!AttributeScope.SURVEY.equals(attribute.getScope()) && !attribute.isTag()) {
                formFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute, null, prefix));
            }
        }
        // Add all property form fields
        for (String propertyName : TAXA_RECORD_PROPERTY_NAMES) {
            formFieldList.add(formFieldFactory.createRecordFormField(survey, record, propertyName, null, null, prefix));
        }
        Collections.sort(formFieldList);
        
        ModelAndView mv = new ModelAndView("singleSiteMultiTaxaRow");
        mv.addObject("record", record);
        mv.addObject("survey", survey);
        mv.addObject("formFieldList", formFieldList);
        return mv;
    }

    /**
     * Saves multiple records for a single site. Site information that is used
     * for all records are specified via parameters. Record specific data such
     * as the sighted taxon will be retrieved from the request parameters. 
     * Record specific parameters are prefixed by the row index 
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey where the record shall be added
     * @param latitude the latitude of the sighting
     * @param longitude the longitude of the sighting
     * @param date the calendar date of the sighting
     * @param time the time when the sighting occured
     * @param notes additional notes to be attached to all records
     * @param sightingIndex the number of records to be saved.
     * @return 
     * @throws ParseException throws if the date cannot be parsed
     * @throws IOException thrown if uploaded files cannot be saved
     */
    @SuppressWarnings("unchecked")
    @RolesAllowed( {"ROLE_USER","ROLE_STUDENT","ROLE_POWERSTUDENT","ROLE_TEACHER","ROLE_ADMIN"} )
    @RequestMapping(value = "/bdrs/user/singleSiteMultiTaxa.htm", method = RequestMethod.POST)
    public ModelAndView saveRecord(MultipartHttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value="surveyId", required=true) int surveyId,
                                    @RequestParam(value="latitude", required=true) double latitude,
                                    @RequestParam(value="longitude", required=true) double longitude,
                                    @RequestParam(value="date", required=true) Date date,
                                    @RequestParam(value="time_hour", required=true) String time_hour,
                                    @RequestParam(value="time_minute", required=true) String time_minute,
                                    @RequestParam(value="notes", required=true) String notes,
                                    @RequestParam(value="sightingIndex", required=true) int sightingIndex) throws ParseException, IOException {

        User user = getRequestContext().getUser();
        Survey survey = surveyDAO.getSurvey(surveyId);
        
        // Dates
        int hour = Integer.parseInt(request.getParameter("time_hour"));
        int minute = Integer.parseInt(request.getParameter("time_minute"));

        Calendar cal = new GregorianCalendar();
        cal.setTime(date);
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, minute);
        cal.clear(Calendar.MILLISECOND);
        Date dateTemplate = cal.getTime();
        
        Record record;
        
        String surveyPrefix = AttributeParser.DEFAULT_PREFIX;
        AttributeParser attributeParser = new AttributeParser();
        
        for(int index=0; index < sightingIndex; index++) {
            String recordPrefix = String.format(PREFIX_TEMPLATE, index);
            
            record = new Record();
            record.setUser(user);
            record.setSurvey(survey);
            record.setNotes(notes);
            record.setPoint(locationService.createPoint(latitude, longitude));

            // Avoiding (for no reason) using the same instance of the date
            Date recordDate = new Date(dateTemplate.getTime());
            record.setWhen(recordDate);
            record.setTime(recordDate.getTime());
            record.setLastDate(recordDate);
            record.setLastTime(recordDate.getTime());
            
            // Constants
            record.setHeld(false);
            record.setFirstAppearance(false);
            record.setLastAppearance(false);
            
            // Taxonomy
            int speciesPk = Integer.parseInt(request.getParameter(String.format("%sspecies", recordPrefix)));
            IndicatorSpecies species = taxaDAO.getIndicatorSpecies(speciesPk);
            record.setSpecies(species);
            
            // Number
            record.setNumber(Integer.parseInt(request.getParameter(String.format("%snumber", recordPrefix))));
            
            Map<Attribute, AttributeValue> recAttrMap = new HashMap<Attribute, AttributeValue>();
            for(AttributeValue recAttr : record.getAttributes()) {
                recAttrMap.put(recAttr.getAttribute(), recAttr);
            }
            
            // Record Attributes
            AttributeValue recAttr;
            String prefix;
            for(Attribute attribute : survey.getAttributes()) {
                prefix = AttributeScope.SURVEY.equals(attribute.getScope()) ? surveyPrefix : recordPrefix;  
                recAttr = attributeParser.parse(prefix, attribute, record, request.getParameterMap(), request.getFileMap());
                if(attributeParser.isAddOrUpdateAttribute()) {
                    recAttr = recordDAO.saveAttributeValue(recAttr);
                    if(attributeParser.getAttrFile() != null) {
                        fileService.createFile(recAttr, attributeParser.getAttrFile());
                    }
                    record.getAttributes().add(recAttr);
                }
                else {
                    record.getAttributes().remove(recAttr);
                    recordDAO.delete(recAttr);
                }
            }

            recordDAO.saveRecord(record);
        }

        ModelAndView mv;

        if(request.getParameter("submitAndAddAnother") != null) {
            mv = new ModelAndView(new RedirectView("/bdrs/user/surveyRenderRedirect.htm", true));
            mv.addObject("surveyId", survey.getId());
            
            String tmpl = propertyService.getMessage("bdrs.record.singlesitemultitaxa.save.success");
            getRequestContext().addMessage(String.format(tmpl, sightingIndex));
        }
        else {
            mv = new ModelAndView(new RedirectView(redirectionService.getMySightingsUrl(survey), true));
            String tmpl = propertyService.getMessage("bdrs.record.singlesitemultitaxa.save.success");
            getRequestContext().addMessage(String.format(tmpl, sightingIndex));
        }

        return mv;
    }
    
    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}