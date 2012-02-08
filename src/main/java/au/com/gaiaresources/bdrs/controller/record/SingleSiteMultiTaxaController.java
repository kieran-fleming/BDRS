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

import au.com.gaiaresources.bdrs.security.Role;

/**
 * The <code>SingleSiteMultiTaxa</code> controller is a record add form renderer
 * that allows multiple sightings of differing taxa to be created for a single
 * location.
 * 
 * @author benk
 */

@Controller
public class SingleSiteMultiTaxaController extends SingleSiteController {
    
    public static final String SINGLE_SITE_MULTI_TAXA_URL = "/bdrs/user/singleSiteMultiTaxa.htm";
    public static final String PARAM_RECORD_ID = SingleSiteController.PARAM_RECORD_ID;
    public static final String PARAM_SURVEY_ID = SingleSiteController.PARAM_SURVEY_ID;
    public static final String PARAM_CENSUS_METHOD_ID = SingleSiteController.PARAM_CENSUS_METHOD_ID;
    public static final String SINGLE_SITE_MULTI_TAXA_VIEW_NAME = "singleSiteMultiTaxa";
    
    /**
     * Displays a blank form displaying inputs for the latitude, longitude,
     * date, time and notes.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey where the record shall be added.
     * @return
     */
    @RequestMapping(value = SINGLE_SITE_MULTI_TAXA_URL, method = RequestMethod.GET)
    public ModelAndView addRecord(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value=PARAM_SURVEY_ID, required=true) int surveyId,
                                    @RequestParam(value = PARAM_CENSUS_METHOD_ID, required = false, defaultValue = "0") Integer censusMethodId) {
        return addRecord(request, response, surveyId, SINGLE_SITE_MULTI_TAXA_VIEW_NAME, censusMethodId);
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
    @RequestMapping(value = "/bdrs/user/singleSiteMultiTaxa/sightingRow.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddSightingRow(HttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value= PARAM_SURVEY_ID, required=true) int surveyId,
                                    @RequestParam(value=PARAM_SIGHTING_INDEX, defaultValue="0") int sightingIndex) {
        
        return super.ajaxGetSightingsTable(request, response, surveyId, sightingIndex);
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
     * @param rowIds an Array of prefix ids for each row
     * @return 
     * @throws ParseException throws if the date cannot be parsed
     * @throws IOException thrown if uploaded files cannot be saved
     */
    @RequestMapping(value = SINGLE_SITE_MULTI_TAXA_URL, method = RequestMethod.POST)
    @RolesAllowed( {Role.USER,"ROLE_STUDENT","ROLE_POWERSTUDENT","ROLE_TEACHER",Role.ADMIN, Role.POWERUSER, Role.SUPERVISOR} )
    public ModelAndView saveRecord(MultipartHttpServletRequest request,
                                    HttpServletResponse response,
                                    @RequestParam(value=PARAM_SURVEY_ID, required=true) int surveyId,
                                    @RequestParam(value=SingleSiteController.PARAM_LATITUDE, required=false) Double latitude,
                                    @RequestParam(value=SingleSiteController.PARAM_LONGITUDE, required=false) Double longitude,
                                    @RequestParam(value=SingleSiteController.PARAM_DATE, required=false) Date date,
                                    @RequestParam(value=SingleSiteController.PARAM_TIME_HOUR, required=false) String time_hour,
                                    @RequestParam(value=SingleSiteController.PARAM_TIME_MINUTE, required=false) String time_minute,
                                    @RequestParam(value=SingleSiteController.PARAM_NOTES, required=false) String notes,
                                    @RequestParam(value=SingleSiteController.PARAM_SIGHTING_INDEX, required=true) int sightingIndex,
                                    @RequestParam(value=SingleSiteController.PARAM_ROW_PREFIX, required=true, defaultValue="") String[] rowIds) throws ParseException, IOException {
        return saveRecordHelper(request, response, surveyId, latitude, longitude, date, time_hour, time_minute, notes, sightingIndex, rowIds);
    }
}