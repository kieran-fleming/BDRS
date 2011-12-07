package au.com.gaiaresources.bdrs.controller.location;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

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
import au.com.gaiaresources.bdrs.controller.attribute.AttributeFormFieldFactory;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormFieldFactory;
import au.com.gaiaresources.bdrs.controller.record.RecordWebFormContext;
import au.com.gaiaresources.bdrs.controller.record.WebFormAttributeParser;
import au.com.gaiaresources.bdrs.controller.survey.SurveyBaseController;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataBuilder;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataRow;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

import com.vividsolutions.jts.geom.Geometry;

@Controller
public class LocationBaseController extends AbstractController {
    
    public static final String GET_SURVEY_LOCATIONS_FOR_USER = "/bdrs/location/getSurveyLocationsForUser.htm";
    
    public static final String PARAM_SURVEY_ID = "surveyId";
    

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;

    @Autowired
    private AttributeDAO attributeDAO;
    
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private MetadataDAO metadataDAO;
    
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private FileService fileService;

    @Autowired
    private LocationService locationService;

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/location/editUserLocations.htm", method = RequestMethod.GET)
    public ModelAndView editUserLocations(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value="redirect", defaultValue="/bdrs/location/editUserLocations.htm") String redirect) {

        User user = getRequestContext().getUser();
        
        Metadata defaultLocId = user.getMetadataObj(Metadata.DEFAULT_LOCATION_ID);
        Location defaultLocation;
        if(defaultLocId == null) {
            defaultLocation = null;
        } else {
            int defaultLocPk = Integer.parseInt(defaultLocId.getValue());
            defaultLocation = locationDAO.getLocation(defaultLocPk);
        }
        
        ModelAndView mv = new ModelAndView("userEditLocations");
        mv.addObject("locations", locationDAO.getUserLocations(user));
        mv.addObject("defaultLocationId", defaultLocation == null ? -1 : defaultLocation.getId());
        mv.addObject("redirect", redirect);
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/location/editUserLocations.htm", method = RequestMethod.POST)
    public ModelAndView submitUserLocations(HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value="add_location", required=false) int[] addLocationIndexes,
            @RequestParam(value="location", required=false) int[] locationIds,
            @RequestParam(value="defaultLocationId", required=false) String defaultLocationId,
            @RequestParam(value="redirect", defaultValue="/bdrs/location/editUserLocations.htm") String redirect) {
        
        addLocationIndexes = addLocationIndexes == null ? new int[]{} : addLocationIndexes;
        locationIds = locationIds == null ? new int[]{} : locationIds;
        User user = getRequestContext().getUser();

        // This map represents all locations for a user.
        // As locations are updated, they will be removed from this map.
        // At the end of this method, any locations still in this map will
        // be deleted.
        Map<Integer, Location> locationMap = new HashMap<Integer, Location>();
        for(Location loc : locationDAO.getUserLocations(user)) {
            locationMap.put(loc.getId(), loc);
        }

        // Added Locations
        Map<Integer, Location> addedLocationMap = new HashMap<Integer, Location>();
        for(int rawIndex : addLocationIndexes) {
            Location location = createNewLocation(request, String.valueOf(rawIndex));
            location.setUser(user);
            location = locationDAO.save(location);
            
            addedLocationMap.put(rawIndex, location);
        }

        // Updated Locations
        for(int pk : locationIds) {
            Location location = locationMap.remove(pk);
            location = updateLocation(request, String.valueOf(pk), location);
            location.setUser(user);
            locationDAO.save(location);
        }

        // Location to be Deleted
        // We cannot actually delete the location object because it may be
        // connected to an Record. Instead we are going to unlink it from the
        // User. This means that it is possible for orphan locations to be 
        // created.
        for(Map.Entry<Integer, Location> tuple : locationMap.entrySet()) {
            //locationDAO.delete(tuple.getValue());
        	Location loc = tuple.getValue();
        	loc.setUser(null);
        	loc = locationDAO.save(loc);
        }
        
        try{
            if(defaultLocationId != null) {
                Metadata defaultLocMD = user.getMetadataObj(Metadata.DEFAULT_LOCATION_ID);
                if(defaultLocMD == null) {
                    defaultLocMD = new Metadata();
                    defaultLocMD.setKey(Metadata.DEFAULT_LOCATION_ID);
                }
                
                String[] split = defaultLocationId.split("_");
                if(split.length == 2) {
                    Integer val = new Integer(split[1]);
                    if(defaultLocationId.startsWith("id_")) {
                        defaultLocMD.setValue(val.toString());
                    } else if(addedLocationMap.containsKey(val)) {
                        defaultLocMD.setValue(addedLocationMap.get(val).getId().toString());
                    } else {
                        throw new IllegalArgumentException("Unable to match default location with an id or an index."+defaultLocationId);
                    }
                } else {
                    throw new IllegalArgumentException("Invalid default location id format received: "+ defaultLocationId);
                }
                
                metadataDAO.save(defaultLocMD);
                
                user.getMetadata().add(defaultLocMD);
                userDAO.updateUser(user);
            }
        } catch(NumberFormatException nfe) {
            // Do nothing. Bad data.
            log.error("Invalid location PK or index received: "+defaultLocationId, nfe);
        } catch(IllegalArgumentException iae) {
            log.error(iae.getMessage(), iae);
        }

        ModelAndView mv = new ModelAndView(new RedirectView(redirect, true));
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/location/ajaxAddUserLocationRow.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddUserLocationRow(HttpServletRequest request, HttpServletResponse response) {
        
        Location defaultLocation = null;
        User user = getRequestContext().getUser();
        Metadata defaultLocId = user.getMetadataObj(Metadata.DEFAULT_LOCATION_ID);
        if(defaultLocId == null) {
            defaultLocation = null;
        } else {
            int defaultLocPk = Integer.parseInt(defaultLocId.getValue());
            defaultLocation = locationDAO.getLocation(defaultLocPk);
        }
        
        ModelAndView mv = new ModelAndView("userLocationRow");
        mv.addObject("index", Integer.parseInt(request.getParameter("index")));
        mv.addObject("defaultLocationId", defaultLocation == null ? -1 : defaultLocation.getId());
        return mv;
    }
    
    
    // ----------------------------------------
    // Admin Functionality
    // ----------------------------------------
    
    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/location/ajaxAddSurveyLocationRow.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddSurveyLocationRow(HttpServletRequest request, HttpServletResponse response) {
        
        ModelAndView mv = new ModelAndView("surveyLocationRow");
        mv.addObject("index", Integer.parseInt(request.getParameter("index")));
        return mv;
    }
    
    @RolesAllowed( {Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/locationListing.htm", method = RequestMethod.GET)
    public ModelAndView editSurveyLocationListing(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="surveyId", required = true) int surveyId) {
        Survey survey = getSurvey(surveyId);
        if (survey == null) {
            return SurveyBaseController.nullSurveyRedirect(getRequestContext());
        }
        ModelAndView mv = new ModelAndView("locationListing");
        mv.addObject("survey", survey);
        return mv;
    }

    @RolesAllowed( {Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/locationListing.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyLocationListing(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="surveyId", required = true) int surveyId) {
        Survey survey = getSurvey(surveyId);
        if (survey == null) {
            return SurveyBaseController.nullSurveyRedirect(getRequestContext());
        }
        List<Location> locationList = new ArrayList<Location>();

        // Updated Locations
        if(request.getParameter("location") != null ) {
            for(String rawPK : request.getParameterValues("location")) {
                int pk = Integer.parseInt(rawPK);
                Location location = locationDAO.getLocation(pk);
                locationList.add(location);
            }
        }

        survey.setLocations(locationList);
        
        boolean predefined_locations_only = request.getParameter("restrict_locations") != null;
        Metadata predefinedLocMetadataData = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        
        if(predefinedLocMetadataData == null) {
            predefinedLocMetadataData = new Metadata();
            predefinedLocMetadataData.setKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        }
        predefinedLocMetadataData.setValue(String.valueOf(predefined_locations_only));
        metadataDAO.save(predefinedLocMetadataData);
        survey.getMetadata().add(predefinedLocMetadataData);
        
        // Update the form rendering type given the new criteria
        SurveyFormRendererType formRenderType = survey.getFormRendererType();
        if(formRenderType == null || (formRenderType != null && !formRenderType.isEligible(survey))) {
            Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
            metadataDAO.save(md);
        }
        surveyDAO.save(survey);

        getRequestContext().addMessage("bdrs.survey.locations.success", new Object[]{survey.getName()});

        ModelAndView mv;
        if(request.getParameter("saveAndContinue") != null) {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/editUsers.htm", true));
            mv.addObject("surveyId", survey.getId());
        }
        else {
            mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/listing.htm", true));
        }
        return mv;
    }

    /**
     * Renderer for the editLocation page. 
     * This method creates the location attribute form fields.
     * @param request
     * @param response
     * @param surveyId The id of the survey for the location.
     * @param locationId (optional) The id of the location to edit.  If null, a new location will be created.
     * @return
     */
    @RolesAllowed( {Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editLocation.htm", method = RequestMethod.GET)
    public ModelAndView editSurveyLocation(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="surveyId", required = true) int surveyId,
            @RequestParam(value="locationId", required = false) Integer locationId) {
        Survey survey = getSurvey(surveyId);
        if (survey == null) {
            return SurveyBaseController.nullSurveyRedirect(getRequestContext());
        }
        FormFieldFactory formFieldFactory = new FormFieldFactory();
        Location location = null;
        List<FormField> surveyFormFieldList = new ArrayList<FormField>();
        List<Attribute> surveyAttributeList = new ArrayList<Attribute>(survey.getAttributes());
        
        Set<AttributeValue> locationAttributes = null;
        if (locationId != null) {
            location = getLocation(locationId);
            locationAttributes = location.getAttributes();
            // add the location attribute form fields
            for (AttributeValue attr : locationAttributes) {
                if (surveyAttributeList.remove(attr.getAttribute())) {
                    surveyFormFieldList.add(formFieldFactory.createLocationFormField(attr.getAttribute(), attr));
                }
            }
        } else {
            location = new Location();
        }
        
        for (Attribute surveyAttr : surveyAttributeList) {
            if(AttributeScope.LOCATION.equals(surveyAttr.getScope())) {
                surveyFormFieldList.add(formFieldFactory.createLocationFormField(surveyAttr));
            }
        }
        
        Collections.sort(surveyFormFieldList);
        
        ModelAndView mv = new ModelAndView("surveyEditLocation");
        mv.addObject("survey", survey);
        mv.addObject("locationFormFieldList", surveyFormFieldList);
        mv.addObject("location", location);
        // location scoped attributes are always editable on the edit location page...
        mv.addObject(RecordWebFormContext.MODEL_EDIT, true);
        
        return mv;
    }

    /**
     * This method creates a new location or updates an existing one, including its attributes.
     * @param request
     * @param response
     * @param surveyId The id of the survey for the location.
     * @param locationId (optional) The id of the location to edit.  If null, a new location will be created.
     * @return
     */
    @RolesAllowed( {Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editLocation.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyLocation(MultipartHttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="surveyId", required = true) int surveyId,
            @RequestParam(value="locationId", required = false) Integer locationId) {
        Survey survey = getSurvey(surveyId);
        if (survey == null) {
            return SurveyBaseController.nullSurveyRedirect(getRequestContext());
        }
        if (request.getParameter("goback") == null) {
            List<Location> locationList = survey.getLocations();

            Location location = null;
            // Added Locations
            if(locationId == null) {
                location = createNewLocation(request);
            } 
            else {
                location = locationDAO.getLocation(locationId);
                // remove the location before updating and re-adding it
                locationList.remove(location);
                location = updateLocation(request, location);
            }
            // save the location attributes
            try {
                Set locAtts = saveAttributes(request, survey, location);
                location.setAttributes(locAtts);
            } catch (Exception e) {
                log.error("Error setting the location attributes: ", e);
            }
            locationDAO.save(location);
            locationList.add(location);
            
            survey.setLocations(locationList);
            surveyDAO.save(survey);
    
            getRequestContext().addMessage("bdrs.survey.locations.success", new Object[]{survey.getName()});
        }
        
        ModelAndView mv = new ModelAndView(new RedirectView("/bdrs/admin/survey/locationListing.htm", true));
        mv.addObject("surveyId", survey.getId());
        return mv;
    }
    
    @RolesAllowed( {Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = GET_SURVEY_LOCATIONS_FOR_USER, method = RequestMethod.GET)
    public void getSurveyLocationsForUser(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_SURVEY_ID, required=true) int surveyId) throws Exception {
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        User currentUser = getRequestContext().getUser();
        PagedQueryResult<Location> queryResult = locationDAO.getSurveylocations(filter, currentUser, surveyId);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());

        if (queryResult.getCount() > 0) {
            for (Location loc : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(loc.getId());
                row
                .addValue("name", loc.getName())
                .addValue("description", loc.getDescription());
                builder.addRow(row);
            }
        }
        writeJson(request, response, builder.toJson());
    }
    
    @SuppressWarnings("unchecked")
    private Set<TypedAttributeValue> saveAttributes(
            MultipartHttpServletRequest request, Survey survey, Location location) throws ParseException, IOException {
        TypedAttributeValue recAttr;
        WebFormAttributeParser attributeParser = new WebFormAttributeParser();
        Set recAtts = location.getAttributes();
        for(Attribute attribute : survey.getAttributes()) {
            if(AttributeScope.LOCATION.equals(attribute.getScope())) {
                recAttr = attributeParser.parse(attribute, location, request.getParameterMap(), request.getFileMap());
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
        return recAtts;
    }

    /**
     * Convenience method for updating a location from an HTTP request.
     * @param request The HTTP request
     * @param id The unique identifier of the location in the request
     * @param location The existing location object from the DAO
     * @return The modified Location object
     */
    private Location updateLocation(HttpServletRequest request, String id,
            Location location) {
        return updateLocation(location, request.getParameter("location_WKT_"+id), request.getParameter("name_"+id));
    }

    /**
     * Convenience method for updating a location from an HTTP request.
     * @param request The HTTP request
     * @param id The unique identifier of the location in the request
     * @param location The existing location object from the DAO
     * @return The modified Location object
     */
    private Location updateLocation(HttpServletRequest request, Location location) {
        return updateLocation(location, request.getParameter("location_WKT"), 
                              request.getParameter("locationName"),
                              request.getParameter("locationDescription"));
    }

    /**
     * Convenience method for setting the name and location of a Location object
     * to the given wktString/name
     * @param location The Location to update
     * @param wktString The WKT string of the Geometry to set as the location
     * @param name The name of the location
     * @return The modified Location object
     */
    private Location updateLocation(Location location, String wktString, String name) {
        return updateLocation(location, wktString, name, null);
    }
    
    /**
     * Convenience method for setting the name and location of a Location object
     * to the given wktString/name
     * @param location The Location to update
     * @param wktString The WKT string of the Geometry to set as the location
     * @param name The name of the location
     * @return The modified Location object
     */
    private Location updateLocation(Location location, String wktString, String name, String description) {
        Geometry geometry = locationService.createGeometryFromWKT(wktString);
        location.setName(name);
        location.setLocation(geometry);
        location.setDescription(description);
        return location;
    }

    /**
     * Convenience method for creating a new location from an HTTP request
     * @param request The request object
     * @param id The unique identifier of the location in the request
     * @return The newly created Location object
     */
    private Location createNewLocation(HttpServletRequest request, String id) {
        Location location = new Location();
        return updateLocation(location, request.getParameter("add_location_WKT_"+id), request.getParameter("add_name_"+id));
    }

    /**
     * Convenience method for creating a new location from an HTTP request
     * @param request The request object
     * @return The newly created Location object
     */
    private Location createNewLocation(HttpServletRequest request) {
        Location location = new Location();
        return updateLocation(location, request.getParameter("location_WKT"), 
                              request.getParameter("locationName"),
                              request.getParameter("locationDescription"));
    }
    
    private Survey getSurvey(Integer rawSurveyId) {
        if(rawSurveyId == null){
            // Do not know which survey to deal with. Bail out.
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        return surveyDAO.getSurvey(rawSurveyId);
    }

    private Location getLocation(Integer rawLocationId) {
        if(rawLocationId == null){
            // Do not know which survey to deal with. Bail out.
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        return locationDAO.getLocation(rawLocationId);
    }
    
    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}
