package au.com.gaiaresources.bdrs.controller.location;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import com.vividsolutions.jts.geom.Geometry;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.*;

@Controller
public class LocationBaseController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;

    @Autowired
    private LocationDAO locationDAO;

    @Autowired
    private MetadataDAO metadataDAO;
    
    @Autowired
    private UserDAO userDAO;

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
    
    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editLocations.htm", method = RequestMethod.GET)
    public ModelAndView editSurveyLocations(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));
        ModelAndView mv = new ModelAndView("surveyEditLocations");
        mv.addObject("survey", survey);
        return mv;
    }

    @RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
    @RequestMapping(value = "/bdrs/admin/survey/editLocations.htm", method = RequestMethod.POST)
    public ModelAndView submitSurveyLocations(HttpServletRequest request, HttpServletResponse response) {
        Survey survey = getSurvey(request.getParameter("surveyId"));
        List<Location> locationList = new ArrayList<Location>();

        // Added Locations
        if(request.getParameter("add_location") != null ) {
            for(String rawIndex : request.getParameterValues("add_location")) {
                Location location = createNewLocation(request, rawIndex);
                locationDAO.save(location);
                locationList.add(location);
            }
        }

        // Updated Locations
        if(request.getParameter("location") != null ) {
            for(String rawPK : request.getParameterValues("location")) {
                int pk = Integer.parseInt(rawPK);
                Location location = locationDAO.getLocation(pk);
                location = updateLocation(request, rawPK, location);
                locationDAO.save(location);

                locationList.add(location);
            }
        }

        survey.setLocations(locationList);
        
        boolean predefined_locations_only = request.getParameter("restrict_locations") != null;
        Metadata predefinedLocMetadataData = survey.getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        if(predefined_locations_only) {
            if(predefinedLocMetadataData == null) {
                predefinedLocMetadataData = new Metadata();
                predefinedLocMetadataData.setKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
            }
            predefinedLocMetadataData.setValue(Boolean.TRUE.toString());
            metadataDAO.save(predefinedLocMetadataData);
            survey.getMetadata().add(predefinedLocMetadataData);
        }
        else {
            if(predefinedLocMetadataData != null) {
                metadataDAO.delete(predefinedLocMetadataData);
                survey.getMetadata().remove(predefinedLocMetadataData);
            }
        }

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
     * Convenience method for setting the name and location of a Location object
     * to the given wktString/name
     * @param location The Location to update
     * @param wktString The WKT string of the Geometry to set as the location
     * @param name The name of the location
     * @return The modified Location object
     */
    private Location updateLocation(Location location, String wktString, String name) {
        Geometry geometry = locationService.createGeometryFromWKT(wktString);
        location.setName(name);
        location.setLocation(geometry);
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

    private Survey getSurvey(String rawSurveyId) {
        if(rawSurveyId == null){
            // Do not know which survey to deal with. Bail out.
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
        return surveyDAO.getSurvey(Integer.parseInt(rawSurveyId));
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}
