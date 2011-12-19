package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
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
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationNameComparator;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeUtil;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValueUtil;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Controller to render the yearly sightings form
 *
 */
@Controller
public class YearlySightingsController extends AbstractController {

    public static final String YEARLY_SIGHTINGS_URL = "/bdrs/user/yearlySightings.htm";
    
    public static final String PARAM_SURVEY_ID = "surveyId";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_LOCATION_ID = "locationId";
    
    public static final String YEARLY_SIGHTINGS_FORM_VIEW_NAME = "yearlySightings";
    
    
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private AttributeDAO attributeDAO;
    @Autowired
    private FileService fileService;
    
    @Autowired
    private RedirectionService redirectionService;
    
    private FormFieldFactory formFieldFactory = new FormFieldFactory();

    /**
     * GET handler to return the yearly sightings form
     * 
     * @param request - the http request object
     * @param response - the http response object
     * @param surveyId - the survey ID to create the form for
     * @param recordId - the record ID to retrieve the form for. The form will likely contain many records.
     * @return ModelAndView for rendering the yearly sightings form
     */
    @RequestMapping(value = YEARLY_SIGHTINGS_URL, method = RequestMethod.GET)
    public ModelAndView addRecord(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value=PARAM_SURVEY_ID, required=true) int surveyId,
                                    @RequestParam(value=PARAM_RECORD_ID, required=false, defaultValue="0") int recordId) {

        User loggedInUser = getRequestContext().getUser();
        Record record = recordDAO.getRecord(recordId);
        record = record == null ? new Record() : record;
        
        Survey survey = surveyDAO.getSurvey(surveyId);
        
        RecordWebFormContext context = new RecordWebFormContext(request, record, loggedInUser, survey);

        // Jan Feb Mar
        // 1
        // 2
        // 3

        // If the date is defined in the survey then that is the key date
        // and we go backwards and forwards by 6 months, otherwise we select
        // today as the key date and go backwards and forwards by 6 months.
        Calendar start = Calendar.getInstance();
        Calendar end = Calendar.getInstance();

        start.setTime(SurveyFormRendererType.YEARLY_SIGHTINGS.getStartDateForSightings(survey));
        end.setTime(SurveyFormRendererType.YEARLY_SIGHTINGS.getEndDateForSightings(survey));

        Calendar today = Calendar.getInstance();
        today.setTime(new Date());
        today.set(Calendar.HOUR_OF_DAY, 0);
        today.set(Calendar.MINUTE, 0);
        today.set(Calendar.SECOND, 0);
        today.set(Calendar.MILLISECOND, 0);

        // Enter the Matrix
        Date[][] dateMatrix = new Date[31][12];
        for(Date[] dateArray: dateMatrix) {
            Arrays.fill(dateArray, null);
        }

        while(start.before(end)) {
            start.add(Calendar.DAY_OF_MONTH, 1);
            dateMatrix[start.get(Calendar.DAY_OF_MONTH)-1][start.get(Calendar.MONTH)] = start.getTime();
        }
        
        // location may be null
        Location location = record.getLocation();       
        
        // Add survey scope attribute form fields
        List<FormField> formFieldList = new ArrayList<FormField>();
        for(Attribute attribute : survey.getAttributes()) {
            AttributeValue av = AttributeValueUtil.getByAttribute(record.getAttributes(), attribute);
            if((AttributeScope.SURVEY.equals(attribute.getScope()) || 
                    (AttributeScope.SURVEY_MODERATION.equals(attribute.getScope()) && 
                            (loggedInUser.isModerator() || (av != null && av.isPopulated())))) && 
                    !attribute.isTag()) {
                formFieldList.add(formFieldFactory.createRecordFormField(survey, record, attribute, av));
            }
        }
        Collections.sort(formFieldList);
        
        Metadata predefinedLocationsMD = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        boolean predefinedLocationsOnly = predefinedLocationsMD != null && 
            Boolean.parseBoolean(predefinedLocationsMD.getValue());
        
        Set<Location> locations = new TreeSet<Location>(new LocationNameComparator());
        locations.addAll(survey.getLocations());
        if(!predefinedLocationsOnly) {
            locations.addAll(locationDAO.getUserLocations(getRequestContext().getUser()));
        }
        
        ModelAndView mv = new ModelAndView(YEARLY_SIGHTINGS_FORM_VIEW_NAME);
        mv.addObject("survey", survey);
        mv.addObject("preview", request.getParameter("preview") != null);
        mv.addObject("species", survey.getSpecies().iterator().next());
        mv.addObject("locations", locations);
        mv.addObject("predefinedLocationsOnly", predefinedLocationsOnly);
        mv.addObject("dateMatrix", dateMatrix);
        mv.addObject("today", today.getTime());
        mv.addObject("location", location);
        mv.addObject("formFieldList", formFieldList);
        mv.addObject("record", record);
        mv.addObject(RecordWebFormContext.MODEL_WEB_FORM_CONTEXT, context);

        return mv;
    }

    /**
     * POST handler for the yearly sightings form
     * 
     * @param request - the http request object
     * @param response - the http response object
     * @param surveyId - the survey ID to save a new form instance for
     * @param locationId - the location ID for the form instance
     * @return A ModelAndView with a RedirectView
     * @throws ParseException
     * @throws IOException
     */
    @SuppressWarnings("unchecked")
    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = YEARLY_SIGHTINGS_URL, method = RequestMethod.POST)
    public ModelAndView submitRecord(MultipartHttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value=PARAM_SURVEY_ID, required=true) int surveyId,
                                    @RequestParam(value=PARAM_LOCATION_ID, required=true) int locationId) 
        throws ParseException, IOException {
        
        Location location = locationDAO.getLocation(locationId);
        Survey survey = surveyDAO.getSurvey(surveyId);
        IndicatorSpecies species = survey.getSpecies().iterator().next();
        User user = getRequestContext().getUser();
        
        List<Record> recordList = recordDAO.getRecords(user, survey, location, 
                                                       SurveyFormRendererType.YEARLY_SIGHTINGS.getStartDateForSightings(survey),
                                                       SurveyFormRendererType.YEARLY_SIGHTINGS.getEndDateForSightings(survey));
        Map<Long, Record> timeToRecordMap = new HashMap<Long, Record>(365);
        Date when;
        // Build a map of the records that we c
        for(Record rec : recordList) {
            when = rec.getWhen();
            if(when != null) {
                timeToRecordMap.put(when.getTime(), rec);
            }
        }

        // need separate 'recToDisplay' local variable as 'rec' can be assigned
        // records that do not appear in our yearly sightings range.
        Record recToDisplay = null;
        Record rec;
        String key;
        String value;
        Enumeration postKeys = request.getParameterNames();
        while(postKeys.hasMoreElements()) {
            key = postKeys.nextElement().toString();
            value = request.getParameter(key);

            if(key.startsWith("date_")) {
                Long time = Long.parseLong(key.split("_")[1]);
                rec = timeToRecordMap.get(time);
                if(value.isEmpty()) {
                    if(rec != null) {
                        recordDAO.deleteById(rec.getId());
                    }
                    // Otherwise value is empty and record is null. Do nothing.
                } else {
                    if(rec == null) {
                        rec = new Record();
                        rec.setSurvey(survey);
                        rec.setSpecies(species);
                        rec.setUser(user);
                        rec.setLocation(location);
                        rec.setPoint(location.getLocation().getCentroid());
                        rec.setHeld(false);

                        Date recordTime = new Date(time);
                        rec.setWhen(recordTime);
                        rec.setTime(time);
                        rec.setLastDate(recordTime);
                        rec.setLastTime(time);

                        rec.setFirstAppearance(false);
                        rec.setLastAppearance(false);
                    }
                    
                    // Record Attributes
                    TypedAttributeValue recAttr;
                    WebFormAttributeParser attributeParser = new WebFormAttributeParser();
                    Set recAtts = rec.getAttributes();
                    for(Attribute attribute : survey.getAttributes()) {
                        if(AttributeScope.SURVEY.equals(attribute.getScope()) || 
                                AttributeScope.SURVEY_MODERATION.equals(attribute.getScope())) {
                            if (AttributeUtil.isModifiableByScopeAndUser(attribute, user)) {
                                recAttr = attributeParser.parse(attribute, rec, request.getParameterMap(), request.getFileMap());
                                if(attributeParser.isAddOrUpdateAttribute()) {
                                    recAttr = attributeDAO.save(recAttr);
                                    if(attributeParser.getAttrFile() != null) {
                                        fileService.createFile(recAttr, attributeParser.getAttrFile());
                                    }
                                    recAtts.add(recAttr);
                                }
                                else {
                                    recAtts.remove(recAttr);
                                    attributeDAO.delete(recAttr);
                                }
                            }
                        }
                    }
                    rec.setAttributes(recAtts);

                    rec.setNumber(Integer.parseInt(value));
                    recordDAO.saveRecord(rec);
                    // we know 'rec' is not null here so...
                    recToDisplay = rec;
                }
            }
        }
        
        // highlight the last record...
        ModelAndView mv = new ModelAndView(new RedirectView(redirectionService.getMySightingsUrl(survey), true));
        RecordWebFormContext.addRecordHighlightId(mv, recToDisplay);
        return mv;
    }

    @SuppressWarnings("unused")
    private void logRequestParameters(HttpServletRequest request) {
        Enumeration e = request.getParameterNames();
        while(e.hasMoreElements()) {
            String key = e.nextElement().toString();
            log.debug("Key:"+key);
            for(String val : request.getParameterValues(key)) {
                log.debug("Value: "+val);
            }
            log.debug("---------");
        }
    }
}
