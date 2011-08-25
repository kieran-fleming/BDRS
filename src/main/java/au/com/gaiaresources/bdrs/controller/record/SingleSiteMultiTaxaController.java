package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
import java.text.ParseException;
import java.util.Date;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

/**
 * The <code>SingleSiteMultiTaxa</code> controller is a record add form renderer
 * that allows multiple sightings of differing taxa to be created for a single
 * location.
 * 
 * @author benk
 */
@Controller
public class SingleSiteMultiTaxaController extends SingleSiteController {
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
        return addRecord(request, response, surveyId, "singleSiteMultiTaxa");
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
        
        return ajaxGetSightingsTable(request, response, surveyId, sightingIndex, "singleSiteMultiTaxaRow");
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
        return saveRecordHelper(request, response, surveyId, latitude, longitude, date, time_hour, time_minute, notes, sightingIndex);
    }
}