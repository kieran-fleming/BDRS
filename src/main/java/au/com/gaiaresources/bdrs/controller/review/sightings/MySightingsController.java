package au.com.gaiaresources.bdrs.controller.review.sightings;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.map.RecordDownloadFormat;
import au.com.gaiaresources.bdrs.controller.map.RecordDownloadWriter;
import au.com.gaiaresources.bdrs.db.impl.SortOrder;
import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.record.impl.AdvancedCountRecordFilter;
import au.com.gaiaresources.bdrs.model.record.impl.AdvancedRecordFilter;
import au.com.gaiaresources.bdrs.model.record.impl.RecordFilter;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.CompareAttributeValueByAttributeWeight;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * This controller provides a more simplistic view of the records in a survey.
 */
@Controller
public class MySightingsController extends SightingsController {
    private static final String JSON_CONTENT_TYPE = "application/json";

    public static final String DEFAULT_MAX_PAGES = "20";
    public static final String DEFAULT_RECORDS_PER_PAGE = "20";
    public static final String RECORD_COUNT_COUNT_KEY = "record_count";
    public static final String RECORD_COUNT_RECORDS_PER_PAGE_KEY = "records_per_page";
    public static final String RECORD_COUNT_MAX_PAGES_KEY = "max_pages";
    
    public static final String DEFAULT_LIMIT = "300";
    
    public static final String MY_SIGHTINGS_URL = "/map/mySightings.htm";
    public static final String MY_SIGHTINGS_JSON_URL = "/map/ajaxMySightingsJSON.htm";
    public static final String MY_SIGHTINGS_KML_URL = "/map/ajaxMySightingsKML.htm";
    public static final String MY_SIGHTINGS_DOWNLOAD_URL = "/map/ajaxMySightingsDownload.htm";
    public static final String MY_SIGHTINGS_RECORD_COUNT_URL = "/map/ajaxMySightingsRecordCount.htm";
    
    public static final String MAP_TAB = "map";
    public static final String TABLE_TAB = "table";
    public static final String DOWNLOAD_TAB = "download";
    public static final String DEFAULT_TAB = MAP_TAB;
    
    // SortOrder.ASCENDING.toString();
    public static final String DEFAULT_SORT_ORDER = "ASCENDING";
    public static final String DEFAULT_SORT_COL = "record.when";
    public static final String DEFAULT_USER_RECORDS_ONLY = "true";
    public static final String DEFAULT_SELECTED_SURVEY_ID = "0";
    public static final String DEFAULT_RECORD_ID = "0";
    public static final String DEFAULT_GROUP_ID = "0";
    public static final String DEFAULT_TAXON_SEARCH = "";
    public static final String DEFAULT_DOWNLOAD_FORMAT = "";
    public static final String DEFAULT_PAGE_NUM = "1";
    
    public static final String QUERY_PARAM_SURVEY_ID = "survey_id";
    public static final String QUERY_PARAM_TAXON_GROUP_ID = "taxon_group_id";
    public static final String QUERY_PARAM_TAXON_SEARCH = "taxon_search";
    public static final String QUERY_PARAM_START_DATE = "start_date";
    public static final String QUERY_PARAM_END_DATE = "end_date";
    public static final String QUERY_PARAM_USER_RECORDS_ONLY = "user_records_only";
    public static final String QUERY_PARAM_LIMIT = "limit";
    public static final String QUERY_PARAM_SELECTED_TAB = "selected_tab";
    public static final String QUERY_PARAM_RECORD_ID = "record_id";
    public static final String QUERY_PARAM_SORT_BY = "sort_by";
    public static final String QUERY_PARAM_SORT_ORDER = "sort_order";
    public static final String QUERY_PARAM_PAGE_NUMBER = "page_number";

    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    
    /**
     * Displays a tabbed view of a selected set of records.
     * 
     * @param request
     *            the browser request
     * @param response
     *            the server response
     * @param selectedSurveyId
     *            the primary key of the survey containing the records
     * @param startDate
     *            the earliest date for possible records
     * @param endDate
     *            the latest date for possible records
     * @param limit
     *            the maximum number of records to display
     * @param selectedTab
     *            the tab to be displayed
     * @param recordId
     *            the primary key of the record to highlight
     * @param taxonGroupId
     *            the primary key of the selected taxon group
     * @param taxonSearch
     *            a part of the common or scientific name used to limit the
     *            number of records
     * @param userRecordsOnly
     *            true if only records belonging to the logged in user should be
     *            displayed, false otherwise
     * @param sortBy
     *            the column used to sort the results
     * @param sortOrderStr
     *            the sorted order of the results, ASCENDING or DESCENDING.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = MY_SIGHTINGS_URL, method = RequestMethod.GET)
    public ModelAndView showMySightings(HttpServletRequest request,
                                        HttpServletResponse response,
                                        @RequestParam(defaultValue = DEFAULT_SELECTED_SURVEY_ID, required = false, value = QUERY_PARAM_SURVEY_ID) int selectedSurveyId,
                                        @RequestParam(required = false, value = QUERY_PARAM_START_DATE) Date startDate,
                                        @RequestParam(required = false, value = QUERY_PARAM_END_DATE) Date endDate,
                                        @RequestParam(defaultValue = DEFAULT_LIMIT, required = false, value = QUERY_PARAM_LIMIT) int limit,
                                        @RequestParam(defaultValue = DEFAULT_TAB, required = false, value = QUERY_PARAM_SELECTED_TAB) String selectedTab,
                                        @RequestParam(defaultValue = DEFAULT_RECORD_ID, required = false, value = QUERY_PARAM_RECORD_ID) int recordId,
                                        @RequestParam(defaultValue = DEFAULT_GROUP_ID, value = QUERY_PARAM_TAXON_GROUP_ID, required = true) int taxonGroupId,
                                        @RequestParam(defaultValue = DEFAULT_TAXON_SEARCH, value = QUERY_PARAM_TAXON_SEARCH, required = true) String taxonSearch,
                                        @RequestParam(defaultValue = DEFAULT_USER_RECORDS_ONLY, value = QUERY_PARAM_USER_RECORDS_ONLY, required = false) boolean userRecordsOnly,
                                        @RequestParam(defaultValue = DEFAULT_SORT_COL, value = QUERY_PARAM_SORT_BY, required = true) String sortBy,
                                        @RequestParam(defaultValue = DEFAULT_SORT_ORDER, value = QUERY_PARAM_SORT_ORDER, required = true) String sortOrderStr,
                                        @RequestParam(defaultValue = DEFAULT_DOWNLOAD_FORMAT, value = QUERY_PARAM_DOWNLOAD_FORMAT, required = false) String[] downloadFormat,
                                        @RequestParam(defaultValue = DEFAULT_PAGE_NUM, value = QUERY_PARAM_PAGE_NUMBER, required = false) int pageNumber) {
        
        User user = getRequestContext().getUser();
        List<Survey> surveyList = surveyDAO.getActiveSurveysForUser(user);
        Survey selectedSurvey = surveyDAO.getSurvey(selectedSurveyId);
        
        List<? extends TaxonGroup> groupList;
        if(selectedSurvey == null) {
            groupList = taxaDAO.getTaxonGroups();
            startDate = new Date(0);
            endDate = new Date(System.currentTimeMillis());

            // New fake survey
            selectedSurvey = new Survey();
            selectedSurvey.setId(0);
            selectedSurvey.setStartDate(startDate);
            selectedSurvey.setEndDate(endDate);
            
        } else {
            groupList = taxaDAO.getTaxonGroup(selectedSurvey);
            
            startDate = selectedSurvey.getFormRendererType().getStartDateForSightings(selectedSurvey);
            endDate = selectedSurvey.getFormRendererType().getEndDateForSightings(selectedSurvey);
        }
        
        // If the current selectedTab string is some random garbage,
        if(!(MAP_TAB.equals(selectedTab) || 
                DOWNLOAD_TAB.equals(selectedTab) || 
                TABLE_TAB.equals(selectedTab))) {
            selectedTab = DEFAULT_TAB;
        }
        
        ModelAndView mv = new ModelAndView("mySightings");
        
        mv.addObject("survey_list", surveyList);
        mv.addObject("selected_survey", selectedSurvey);
        mv.addObject("group_list", groupList);
        mv.addObject("start_date", startDate);
        mv.addObject("end_date", endDate);
        mv.addObject("limit", limit < 0 ? Integer.parseInt(DEFAULT_LIMIT) : limit);
        mv.addObject("user", user);
        mv.addObject("record_id", recordId);
        
        mv.addObject("taxon_group_id", taxonGroupId);
        mv.addObject("taxon_search", taxonSearch);
        mv.addObject("user_records_only", (user.isAdmin() || user.isRoot()) ? userRecordsOnly : true);
        mv.addObject("sort_by", sortBy);
        mv.addObject("sort_order", sortOrderStr);
        
        mv.addObject("selected_tab", selectedTab);
        mv.addObject("page_number", pageNumber);
        
        Arrays.sort(downloadFormat);

        mv.addObject("download_kml_selected", Arrays.binarySearch(downloadFormat, RecordDownloadFormat.KML.toString()) > -1);
        mv.addObject("download_shp_selected", Arrays.binarySearch(downloadFormat, RecordDownloadFormat.SHAPEFILE.toString()) > -1);
        mv.addObject("download_xls_selected", Arrays.binarySearch(downloadFormat, RecordDownloadFormat.XLS.toString()) > -1);
        
        return mv;
    }
    
    /**
     * Queries the database for the number of records matching the criteria specified
     * returning the result to the browser in JSON format.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey containing the records
     * @param taxonGroupId the primary key of the selected taxon group
     * @param taxonSearch a part of the common or scientific name used to limit the number of records
     * @param startDate the earliest date for possible records
     * @param endDate the latest date for possible records
     * @param userRecordsOnly true if only records belonging to the logged in user should be displayed, false otherwise
     * @param limit the maximum number of records to display
     * 
     * @throws IOException thrown if data cannot be written to the socket.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = MY_SIGHTINGS_RECORD_COUNT_URL, method = RequestMethod.GET) 
    public void getMySightingsRecordCount(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value = QUERY_PARAM_SURVEY_ID, required = true) int surveyId,
                                @RequestParam(value = QUERY_PARAM_TAXON_GROUP_ID, required = true) int taxonGroupId,
                                @RequestParam(value = QUERY_PARAM_TAXON_SEARCH, required = true) String taxonSearch,
                                @RequestParam(value = QUERY_PARAM_START_DATE, required = true) Date startDate,
                                @RequestParam(value = QUERY_PARAM_END_DATE, required = true) Date endDate,
                                @RequestParam(defaultValue = DEFAULT_USER_RECORDS_ONLY, value = QUERY_PARAM_USER_RECORDS_ONLY, required=false) boolean userRecordsOnly,
                                @RequestParam(value = QUERY_PARAM_LIMIT, required = true) int limit) throws IOException {
        
        User user = getRequestContext().getUser();
        
        // Limit of < 1 has a special meaning of "no limit"
        limit = limit < 1 ? Integer.MAX_VALUE : limit;

        RecordFilter filter = getRecordFilter(surveyId, taxonGroupId, taxonSearch, startDate, endDate, user, userRecordsOnly, limit, true);
        
        JSONObject obj = new JSONObject();
        obj.put(RECORD_COUNT_COUNT_KEY, Math.min(recordDAO.countRecords(filter), limit));
        obj.put(RECORD_COUNT_RECORDS_PER_PAGE_KEY, Integer.parseInt(DEFAULT_RECORDS_PER_PAGE, 10));
        obj.put(RECORD_COUNT_MAX_PAGES_KEY, Integer.parseInt(DEFAULT_MAX_PAGES, 10));
        
        if(!response.isCommitted()) {
            response.setContentType(JSON_CONTENT_TYPE);
            response.getWriter().write(obj.toString());
        }
    }
    
    /**
     * Queries the database for a result set of records based upon the specified
     * parameters encoding and returning the result to the browser in JSON format.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey containing the records
     * @param taxonGroupId the primary key of the selected taxon group
     * @param taxonSearch a part of the common or scientific name used to limit the number of records
     * @param startDate the earliest date for possible records
     * @param endDate the latest date for possible records
     * @param userRecordsOnly true if only records belonging to the logged in user should be displayed, false otherwise
     * @param limit the maximum number of records to display
     * @param sortBy the column used to sort the results
     * @param sortOrderStr the sorted order of the results, ASCENDING or DESCENDING
     * 
     * @throws IOException thrown if data cannot be written to the socket.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = MY_SIGHTINGS_JSON_URL, method = RequestMethod.GET) 
    public void getMySightingsJSON(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value = QUERY_PARAM_SURVEY_ID, required = true) int surveyId,
                                @RequestParam(value = QUERY_PARAM_TAXON_GROUP_ID, required = true) int taxonGroupId,
                                @RequestParam(value = QUERY_PARAM_TAXON_SEARCH, required = true) String taxonSearch,
                                @RequestParam(value = QUERY_PARAM_START_DATE, required = true) Date startDate,
                                @RequestParam(value = QUERY_PARAM_END_DATE, required = true) Date endDate,
                                @RequestParam(defaultValue = DEFAULT_USER_RECORDS_ONLY, value = QUERY_PARAM_USER_RECORDS_ONLY, required=false) boolean userRecordsOnly,
                                @RequestParam(value = QUERY_PARAM_LIMIT, required = true) int limit,
                                @RequestParam(value = QUERY_PARAM_SORT_BY, required = true) String sortBy,
                                @RequestParam(value = QUERY_PARAM_SORT_ORDER, required = true) String sortOrderStr,
                                @RequestParam(value = QUERY_PARAM_PAGE_NUMBER, required = false) int pageNumber) throws IOException {
        
        User user = getRequestContext().getUser();
        
        // Limit of < 1 has a special meaning of "no limit"
        limit = limit < 1 ? Integer.MAX_VALUE : limit;
        
        RecordFilter countFilter = getRecordFilter(surveyId, taxonGroupId, taxonSearch, startDate, endDate, user, userRecordsOnly, limit, true);
        RecordFilter recordFilter = getRecordFilter(surveyId, taxonGroupId, taxonSearch, startDate, endDate, user, userRecordsOnly, limit, false);
        int recordCount = recordDAO.countRecords(countFilter);
        
        int defaultRecordsPerPage = Integer.parseInt(DEFAULT_RECORDS_PER_PAGE, 10);
        defaultRecordsPerPage = Math.min(defaultRecordsPerPage, limit);
        recordFilter.setEntriesPerPage(defaultRecordsPerPage);
        
        // Calculate the largest possible page and make sure you do not exceed that.
        int record_count = Math.min(limit, recordCount);
        pageNumber = Math.min(pageNumber, (int)Math.round(Math.ceil((double)record_count / (double)defaultRecordsPerPage)));  
        recordFilter.setPageNumber(pageNumber);
        
        // We wish to render the record at the record Index, up to 
        // the end of the page, end of the records, or the last rec index.
        int recordIndex = (pageNumber-1) * defaultRecordsPerPage;
        int lastRec = Math.min(record_count, limit);
        
        SortOrder sortOrder = SortOrder.valueOf(sortOrderStr);
        ScrollableRecords sr = getScrollableRecords(recordFilter, sortBy, sortOrder);
        
        Session sesh = getRequestContext().getHibernate();
        JSONArray array = new JSONArray();
        int count = 0;
        while (sr.hasMoreElements() && recordIndex < lastRec) {
            Record rec = sr.nextElement();
            Map<String, Object> rec_flatten = rec.flatten();
            IndicatorSpecies species = rec.getSpecies();
            if(species != null) {
                rec_flatten.put("species", species.flatten());
            }
            
            List<Map<String, Object>> attrList = new ArrayList<Map<String,Object>>(rec.getAttributes().size());
            List<AttributeValue> sortedAttrs = new ArrayList<AttributeValue>(rec.getAttributes());
            Collections.sort(sortedAttrs, new CompareAttributeValueByAttributeWeight());
            
            for(AttributeValue attrVal : rec.getAttributes()) {
                attrList.add(attrVal.flatten(1));
            }
            rec_flatten.put("attributes", attrList);
            
            array.add(rec_flatten);
            
            // evict to ensure garbage collection
            if (++count % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
                sesh.clear();
            }
            
            recordIndex += 1;
        }
        response.setContentType(JSON_CONTENT_TYPE);
        response.getWriter().write(array.toString());
    }
    
    /**
     * Queries the database for a result set of records based upon the specified
     * parameters encoding and returning the result to the browser in KML format.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey containing the records
     * @param taxonGroupId the primary key of the selected taxon group
     * @param taxonSearch a part of the common or scientific name used to limit the number of records
     * @param startDate the earliest date for possible records
     * @param endDate the latest date for possible records
     * @param userRecordsOnly true if only records belonging to the logged in user should be displayed, false otherwise
     * @param limit the maximum number of records to display
     * @param sortBy the column used to sort the results
     * @param sortOrderStr the sorted order of the results, ASCENDING or DESCENDING
     * 
     * @throws IOException thrown if data cannot be written to the socket.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = MY_SIGHTINGS_KML_URL, method = RequestMethod.GET)
    public void getMySightingsKML(HttpServletRequest request,
                                HttpServletResponse response,
                                @RequestParam(value = QUERY_PARAM_SURVEY_ID, required = true) int surveyId,
                                @RequestParam(value = QUERY_PARAM_TAXON_GROUP_ID, required = true) int taxonGroupId,
                                @RequestParam(value = QUERY_PARAM_TAXON_SEARCH, required = true) String taxonSearch,
                                @RequestParam(value = QUERY_PARAM_START_DATE, required = true) Date startDate,
                                @RequestParam(value = QUERY_PARAM_END_DATE, required = true) Date endDate,
                                @RequestParam(defaultValue = DEFAULT_USER_RECORDS_ONLY, value = QUERY_PARAM_USER_RECORDS_ONLY, required=false) boolean userRecordsOnly,
                                @RequestParam(value = QUERY_PARAM_LIMIT, required = true) int limit,
                                @RequestParam(value = QUERY_PARAM_SORT_BY, required = true) String sortBy,
                                @RequestParam(value = QUERY_PARAM_SORT_ORDER, required = true) String sortOrderStr) throws Exception {
        
        User user = getRequestContext().getUser();
        
        SortOrder sortOrder = SortOrder.valueOf(sortOrderStr);
        RecordFilter filter = getRecordFilter(surveyId, taxonGroupId, taxonSearch, startDate, endDate, user, userRecordsOnly, limit, false);
        ScrollableRecords sr = getScrollableRecords(filter, sortBy, sortOrder);
        
        RecordDownloadWriter.write(getRequestContext().getHibernate(), request, response, sr, RecordDownloadFormat.KML, user);
    }
    
    /**
     * Sends back a compress file containing the records matching the criteria
     * below and in the specified formats.
     * 
     * @param request the browser request
     * @param response the server response
     * @param surveyId the primary key of the survey containing the records
     * @param taxonGroupId the primary key of the selected taxon group
     * @param taxonSearch a part of the common or scientific name used to limit the number of records
     * @param startDate the earliest date for possible records
     * @param endDate the latest date for possible records
     * @param userRecordsOnly true if only records belonging to the logged in user should be displayed, false otherwise
     * @param limit the maximum number of records to display
     * @param sortBy the column used to sort the results
     * @param sortOrderStr the sorted order of the results, ASCENDING or DESCENDING
     * @param downloadFormat an array of desired file formats.
     * 
     * @throws IOException thrown if data cannot be written to the socket.
     */
    @RolesAllowed( { Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = MY_SIGHTINGS_DOWNLOAD_URL, method = RequestMethod.GET)
    public void downloadMySightings(HttpServletRequest request, HttpServletResponse response,
                                    @RequestParam(value = QUERY_PARAM_SURVEY_ID, required = true) int surveyId,
                                    @RequestParam(value = QUERY_PARAM_TAXON_GROUP_ID, required = true) int taxonGroupId,
                                    @RequestParam(value = QUERY_PARAM_TAXON_SEARCH, required = true) String taxonSearch,
                                    @RequestParam(value = QUERY_PARAM_START_DATE, required = true) Date startDate,
                                    @RequestParam(value = QUERY_PARAM_END_DATE, required = true) Date endDate,
                                    @RequestParam(defaultValue = DEFAULT_USER_RECORDS_ONLY, value = QUERY_PARAM_USER_RECORDS_ONLY, required = false) boolean userRecordsOnly,
                                    @RequestParam(value = QUERY_PARAM_LIMIT, required = true) int limit,
                                    @RequestParam(value = QUERY_PARAM_SORT_BY, required = true) String sortBy,
                                    @RequestParam(value = QUERY_PARAM_SORT_ORDER, required = true) String sortOrderStr, 
                                    @RequestParam(value = QUERY_PARAM_DOWNLOAD_FORMAT, required = false) String[] downloadFormat) throws Exception {
        
        User user = getRequestContext().getUser();
        SortOrder sortOrder = SortOrder.valueOf(sortOrderStr);

        RecordFilter filter = getRecordFilter(surveyId, taxonGroupId, taxonSearch, startDate, endDate, user, userRecordsOnly, limit, false);
        ScrollableRecords sr = getScrollableRecords(filter, sortBy, sortOrder);
        
        List<Survey> surveyList;
        if (surveyId == 0) {
            surveyList = surveyDAO.getActiveSurveysForUser(user);
        } else {
            surveyList = new ArrayList<Survey>();
            surveyList.add(surveyDAO.get(surveyId));
        }
        this.downloadSightings(request, response, downloadFormat, sr, surveyList);
    }
    

    
    private ScrollableRecords getScrollableRecords(RecordFilter filter,
            String sortBy, SortOrder sortOrder) {
        List<SortingCriteria> sc = new ArrayList<SortingCriteria>(1);
        sc.add(new SortingCriteria(sortBy, sortOrder));

        return recordDAO.getScrollableRecords(filter, sc);
    }

    private RecordFilter getRecordFilter(int surveyId, int taxonGroupId,
            String taxonSearch, Date startDate, Date endDate, User accessor,
            boolean userRecordsOnly, int limit, boolean count) {
        
        RecordFilter filter = null;
        if (count) {
            filter = new AdvancedCountRecordFilter();
        } else {
            filter = new AdvancedRecordFilter();
        }
        filter.setSurveyPk(surveyId);
        filter.setStartDate(startDate);
        // set the end date to the end of the day, instead of the default start of the day
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, 23);
        cal.set(Calendar.MINUTE, 59);
        cal.set(Calendar.SECOND, 59);
        cal.set(Calendar.MILLISECOND, 999);
        filter.setEndDate(cal.getTime());
        
        if(taxonGroupId > 0) {
            filter.setTaxonGroupPk(taxonGroupId);
        }
        if(!taxonSearch.trim().isEmpty()) {
            filter.setSpeciesSearch(taxonSearch);
        }
        
        if(accessor != null && userRecordsOnly) {
            filter.setUser(accessor);
        } else {
            filter.setAccessor(accessor);
        }

        if(limit > 0) {
            filter.setPageNumber(1);
            filter.setEntriesPerPage(limit);
        }
        
        return filter;
    }
    
    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}
