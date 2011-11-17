package au.com.gaiaresources.bdrs.controller.review.sightings;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.json.JSONArray;
import org.junit.Before;
import org.junit.Test;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.ui.ModelMap;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.controller.map.RecordDownloadFormat;
import au.com.gaiaresources.bdrs.db.impl.SortOrder;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

/**
 * Tests all aspects of the <code>MySightingsController</code>.
 */
public class MySightingsControllerTest extends AbstractGridControllerTest {
    
    public static final String ALL_SURVEYS_ID = "0";
    public static final String ALL_GROUPS_ID = "0";
    
    private DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
    private Map<String, String[]> allRecordsCriteriaParamMap;
    private Map<String, String[]> noFormatRecordsCriteriaParamMap;
    private Map<String, String[]> limitRecordsCriteriaParamMap;
    private Map<String, String[]> surveyRecordsCriteriaParamMap;
    private Map<String, String[]> taxonGroupsRecordsCriteriaParamMap;
    private Map<String, String[]> taxonSearchRecordsCriteriaParamMap;
    private Map<String, String[]> userRecordsCriteriaParamMap;
    private Map<String, String[]> dateRangeRecordsCriteriaParamMap;
    private Map<String, String[]> sortedRecordsCriteriaParamMap;
    
    // Limit and Record Count Testing
    // limit = record_count
    private Map<String, String[]> limitEqualsRecordCountParamMap;
    // limit > record_count
    private Map<String, String[]> limitLessThanRecordCountParamMap;
    // limit < record_count
    private Map<String, String[]> limitGreaterThanRecordCountParamMap;
    
    // limit = 0, record_count = 0
    private Map<String, String[]> recordCountZeroLimitZeroParamMap;
    // limit > 0, record_count = 0
    private Map<String, String[]> recordCountZeroLimitPositiveParamMap;
    
    // limit = 0, record_count > 0
    private Map<String, String[]> limitZeroRecordCountPositiveParamMap;
    
    @Before
    public void setup() {

        HashMap<String, String[]> temp = new HashMap<String, String[]>();
        temp.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] {ALL_SURVEYS_ID});
        temp.put(MySightingsController.QUERY_PARAM_TAXON_GROUP_ID, new String[] {ALL_GROUPS_ID});
        temp.put(MySightingsController.QUERY_PARAM_TAXON_SEARCH, new String[] {MySightingsController.DEFAULT_TAXON_SEARCH});
        temp.put(MySightingsController.QUERY_PARAM_START_DATE, new String[] {dateFormat.format(new Date(0))});
        temp.put(MySightingsController.QUERY_PARAM_END_DATE, new String[] {dateFormat.format(new Date(System.currentTimeMillis()))});
        temp.put(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY, new String[] {MySightingsController.DEFAULT_USER_RECORDS_ONLY});
        temp.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] {MySightingsController.DEFAULT_LIMIT});
        temp.put(MySightingsController.QUERY_PARAM_SORT_BY, new String[] {MySightingsController.DEFAULT_SORT_COL});
        temp.put(MySightingsController.QUERY_PARAM_SORT_ORDER, new String[] {MySightingsController.DEFAULT_SORT_ORDER});
        temp.put(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, new String[]{
                RecordDownloadFormat.KML.toString(),
                RecordDownloadFormat.SHAPEFILE.toString(),
                RecordDownloadFormat.XLS.toString()
        });
        temp.put(MySightingsController.QUERY_PARAM_PAGE_NUMBER, new String[] {MySightingsController.DEFAULT_PAGE_NUM});
            
        allRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        
        noFormatRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        noFormatRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, new String[]{});
        
        limitRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        limitRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { "5" });
        
        surveyRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        surveyRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { survey1.getId().toString() });
        
        taxonGroupsRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        taxonGroupsRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_TAXON_GROUP_ID, new String[] { g1.getId().toString() });
        
        taxonSearchRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        taxonSearchRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_TAXON_SEARCH, new String[] { nyanCat.getScientificName().split(" ")[0] });
        
        userRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        userRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY, new String[] { Boolean.toString(false) });
        
        dateRangeRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        dateRangeRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_START_DATE, new String[] { bdrsDateFormat.format(getDate(2010, 9, 23)) });
        dateRangeRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_END_DATE, new String[] { bdrsDateFormat.format(getDate(2010, 9, 28)) });
        
        sortedRecordsCriteriaParamMap = new HashMap<String, String[]>(temp);
        sortedRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_SORT_BY, new String[] { "species.scientificName" });
        sortedRecordsCriteriaParamMap.put(MySightingsController.QUERY_PARAM_SORT_ORDER, new String[] { SortOrder.DESCENDING.toString() });
        
        // limit = record_count
        limitEqualsRecordCountParamMap = new HashMap<String, String[]>(temp);
        limitEqualsRecordCountParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { survey1.getId().toString() });
        limitEqualsRecordCountParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { String.valueOf(survey1RecordsList.size()) });
        
        // limit > record_count
        limitLessThanRecordCountParamMap = new HashMap<String, String[]>(temp);
        limitLessThanRecordCountParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { survey1.getId().toString() });
        limitLessThanRecordCountParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { String.valueOf(survey1RecordsList.size()/2) });
        
        
        // limit < record_count
        limitGreaterThanRecordCountParamMap = new HashMap<String, String[]>(temp);
        limitGreaterThanRecordCountParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { survey1.getId().toString() });
        limitGreaterThanRecordCountParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { String.valueOf(7 * survey1RecordsList.size()) });
        
        // limit = 0, record_count = 0
        recordCountZeroLimitZeroParamMap = new HashMap<String, String[]>(temp);
        recordCountZeroLimitZeroParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { empty_survey.getId().toString() });
        recordCountZeroLimitZeroParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { "0" });
        
        // limit > 0, record_count = 0
        recordCountZeroLimitPositiveParamMap = new HashMap<String, String[]>(temp);
        recordCountZeroLimitPositiveParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { empty_survey.getId().toString() });
        recordCountZeroLimitPositiveParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { "100" });
        
        // limit = 0, record_count > 0
        limitZeroRecordCountPositiveParamMap = new HashMap<String, String[]>(temp);
        limitZeroRecordCountPositiveParamMap.put(MySightingsController.QUERY_PARAM_SURVEY_ID, new String[] { survey1.getId().toString() });
        limitZeroRecordCountPositiveParamMap.put(MySightingsController.QUERY_PARAM_LIMIT, new String[] { "0" });
    }
    
    @Test
    public void testMySightingsNoParams() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testMySightingsDownloadTab() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SELECTED_TAB, MySightingsController.DOWNLOAD_TAB);

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testMySightingsInvalidTab() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SELECTED_TAB, "Thundercats");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testMySightingsSortBy() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SORT_BY, "record.when");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testMySightingsInvalidSortBy() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SORT_BY, "Spam Eggs");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testUserRecordsOnlyTrue() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY, "true");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testUserRecordsOnlyFalse() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY, "false");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testUserRecordsOnlyAsUser() throws Exception {
        login("user", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY, "false");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testTaxonSearch() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_TAXON_SEARCH, "Sword of Thundera");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testLimitPositive() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_LIMIT, "123");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testLimitZero() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_LIMIT, "0");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testLimitNegative() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_LIMIT, "-1");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testSurvey() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SURVEY_ID, survey1.getId().toString());

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testAllSurvey() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SURVEY_ID, "0");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testInvalidSurvey() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SURVEY_ID, "-12");

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testSurveyEndDate() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_SURVEY_ID, survey2.getId().toString());

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testRecordId() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_RECORD_ID, r2.getId().toString());

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testTaxonGroupId() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.setParameter(MySightingsController.QUERY_PARAM_TAXON_GROUP_ID, g1.getId().toString());

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testSearchCriteriaAllRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(allRecordsCriteriaParamMap);
        handle(request, response);

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(allRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(allRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaLimitRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(limitRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(limitRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(limitRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaSurveyRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(surveyRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(surveyRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(surveyRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaTaxonGroupsRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(taxonGroupsRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(taxonGroupsRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(taxonGroupsRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaTaxonSearch() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(taxonSearchRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(taxonSearchRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(taxonSearchRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaUserRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(userRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(userRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(userRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaDateRangeRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(dateRangeRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(dateRangeRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(dateRangeRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testSearchCriteriaSortedRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_RECORD_COUNT_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_KML_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_DOWNLOAD_URL);
        request.setParameters(sortedRecordsCriteriaParamMap);
        handle(request, response);
    }
    
    @Test
    public void testMySightingsDownloadSelectionAll() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.addParameter(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.KML.toString());
        request.addParameter(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.SHAPEFILE.toString());
        request.addParameter(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.XLS.toString());

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testMySightingsDownloadSelectionSelected() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_URL);
        request.addParameter(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.KML.toString());
        request.addParameter(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.XLS.toString());

        ModelAndView mv = handle(request, response);
        testMySightings(request.getParameterMap(), mv);
    }
    
    @Test
    public void testPaginationLimitEqualsRecordCount() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(limitEqualsRecordCountParamMap);
        handle(request, response);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        Assert.assertEquals(survey1RecordsList.size(), array.length());
    }
    
    @Test
    public void testPaginationLimitGreaterThanRecordCount() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(limitGreaterThanRecordCountParamMap);
        handle(request, response);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        Assert.assertEquals(survey1RecordsList.size(), array.length());
    }
    
    @Test
    public void testPaginationLimitLessThanRecordCount() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(limitLessThanRecordCountParamMap);
        handle(request, response);
        
        int limit = Integer.parseInt(limitLessThanRecordCountParamMap.get(MySightingsController.QUERY_PARAM_LIMIT)[0]);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        Assert.assertEquals(limit, array.length());
    }
    
    @Test
    public void testPaginationRecordCountZeroLimitZero() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(recordCountZeroLimitZeroParamMap);
        handle(request, response);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        Assert.assertEquals(0, array.length());
    }
    
    @Test
    public void testPaginationRecordCountZeroLimitPositive() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(recordCountZeroLimitPositiveParamMap);
        handle(request, response);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        Assert.assertEquals(0, array.length());
    }
    
    @Test
    public void testPaginationLimitZeroRecordCountPositive() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(MySightingsController.MY_SIGHTINGS_JSON_URL);
        request.setParameters(limitZeroRecordCountPositiveParamMap);
        handle(request, response);
        
        JSONArray array = new JSONArray(response.getContentAsString());
        Assert.assertEquals(survey1RecordsList.size(), array.length());
    }
    
    private void testMySightings(Map<String, String[]> queryParams, ModelAndView mv) throws Exception {
        ModelAndViewAssert.assertViewName(mv, "mySightings");
        ModelMap modelMap = mv.getModelMap();
        
        
        // ------------------
        // Selected Tab
        // ------------------ 
        {
            // You either match one of the tabs, or you will be defaulted.
            String actualTab = modelMap.get("selected_tab").toString();
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_SELECTED_TAB)) {
                String param = queryParams.get(MySightingsController.QUERY_PARAM_SELECTED_TAB)[0].toString();
                if((param.equals(MySightingsController.DOWNLOAD_TAB) ||
                        param.equals(MySightingsController.TABLE_TAB) ||
                        param.equals(MySightingsController.MAP_TAB))) {
                    
                    Assert.assertEquals(param, actualTab);
                } else {
                    Assert.assertEquals(MySightingsController.DEFAULT_TAB, actualTab);
                }
            } else {
                Assert.assertEquals(MySightingsController.DEFAULT_TAB, actualTab);
            }
        }
        
        // ------------------
        // Sort By
        // ------------------
        {
            String actualSortBy = modelMap.get("sort_by").toString();
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_SORT_BY)) {
                String param = queryParams.get(MySightingsController.QUERY_PARAM_SORT_BY)[0].toString();
                
                // The value passed in is the value given to the jsp.
                Assert.assertEquals(param, actualSortBy);
            } else {
                Assert.assertEquals(MySightingsController.DEFAULT_SORT_COL, actualSortBy);
            }
        }
        
        // ------------------
        // Sort Order
        // ------------------
        {
            String actualSortOrder = modelMap.get("sort_order").toString();
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_SORT_ORDER)) {
                String param = queryParams.get(MySightingsController.QUERY_PARAM_SORT_ORDER)[0].toString();
                // The value passed in is the value given to the jsp.
                Assert.assertEquals(param, actualSortOrder);
            } else {
                Assert.assertEquals(MySightingsController.DEFAULT_SORT_ORDER, actualSortOrder);
            }
        }
        
        // ------------------
        // User Records Only
        // ------------------
        {
            User user = RequestContextHolder.getContext().getUser();
            boolean actualUserRecordsOnly = Boolean.parseBoolean(modelMap.get("user_records_only").toString());
            boolean expected;
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY)) {
                String param = queryParams.get(MySightingsController.QUERY_PARAM_USER_RECORDS_ONLY)[0].toString();
                if(user.isAdmin() || user.isRoot()) {
                    expected = Boolean.parseBoolean(param);
                } else {
                    expected = true;
                }
            } else {
                expected = Boolean.parseBoolean(MySightingsController.DEFAULT_USER_RECORDS_ONLY);
            }
            Assert.assertEquals(expected, actualUserRecordsOnly);
        }
        
        // ------------------
        // Taxon Search
        // ------------------
        {
            String actualTaxonSearch = modelMap.get("taxon_search").toString();
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_TAXON_SEARCH)) {
                String param = queryParams.get(MySightingsController.QUERY_PARAM_TAXON_SEARCH)[0].toString();
                // The value passed in is the value given to the jsp.
                Assert.assertEquals(param, actualTaxonSearch);
            } else {
                Assert.assertEquals(MySightingsController.DEFAULT_TAXON_SEARCH, actualTaxonSearch);
            }
        }        
        
        // ------------------
        // Limit
        // ------------------
        {
            int actualLimit = Integer.parseInt(modelMap.get("limit").toString());
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_LIMIT)) {
                int param = Integer.parseInt(queryParams.get(MySightingsController.QUERY_PARAM_LIMIT)[0].toString());
                if(param < 0) {
                    Assert.assertEquals(Integer.parseInt(MySightingsController.DEFAULT_LIMIT), actualLimit);
                } else {
                    Assert.assertEquals(param, actualLimit);
                }
            } else {
                Assert.assertEquals(Integer.parseInt(MySightingsController.DEFAULT_LIMIT), actualLimit);
            }
        }
        
        // ------------------
        // User
        // ------------------
        {
            User modelUser = (User)modelMap.get("user");
            Assert.assertEquals(modelUser, RequestContextHolder.getContext().getUser());
        }
        
        // ------------------
        // Survey, Start Date, End Date, Taxon Group
        // ------------------
        {
            Survey actualSurvey = (Survey)modelMap.get("selected_survey");
            Date actualStartDate = (Date)modelMap.get("start_date");
            Date actualEndDate = (Date)modelMap.get("end_date");
            List<TaxonGroup> actualGroupList = (List<TaxonGroup>)modelMap.get("group_list");
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_SURVEY_ID)) {
                int surveyId = Integer.parseInt(queryParams.get(MySightingsController.QUERY_PARAM_SURVEY_ID)[0].toString());
                Survey survey = surveyDAO.getSurvey(surveyId);
                if(survey == null) {
                    Assert.assertEquals(0, actualSurvey.getId().intValue());
                    Assert.assertEquals(new Date(0), actualStartDate);
                    // Sometime in the last 5 seconds
                    Assert.assertTrue(actualEndDate.getTime() >= System.currentTimeMillis() - 5000);
                    Assert.assertEquals(1, actualGroupList.size());
                } else {
                    Assert.assertEquals(surveyId, actualSurvey.getId().intValue());
                    Assert.assertEquals(survey.getStartDate(), actualStartDate);
                    if(survey.getEndDate() == null) {
                        // Sometime in the last 5 seconds
                        Assert.assertTrue(actualEndDate.getTime() >= System.currentTimeMillis() - 5000);
                    }
                    Assert.assertEquals(actualGroupList, taxaDAO.getTaxonGroup(survey));
                }
            } else {
                Assert.assertEquals(0, actualSurvey.getId().intValue());
                Assert.assertEquals(new Date(0), actualStartDate);
                // Sometime in the last 5 seconds
                Assert.assertTrue(actualEndDate.getTime() >= System.currentTimeMillis() - 5000);
                Assert.assertEquals(1, actualGroupList.size());
            }
        }
        
        // ------------------
        // Record ID
        // ------------------
        {
            int actualRecordId = Integer.parseInt(modelMap.get("record_id").toString());
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_RECORD_ID)) {
                int param = Integer.parseInt(queryParams.get(MySightingsController.QUERY_PARAM_RECORD_ID)[0].toString());
                // The value passed in is the value given to the jsp.
                Assert.assertEquals(param, actualRecordId);
            } else {
                Assert.assertEquals(Integer.parseInt(MySightingsController.DEFAULT_RECORD_ID), actualRecordId);
            }
        }
        
        // ------------------
        // Survey List
        // ------------------
        {
            User u = RequestContextHolder.getContext().getUser(); 
            List<Survey> actualSurveyList = (List<Survey>)modelMap.get("survey_list");
            Assert.assertEquals(surveyDAO.getActiveSurveysForUser(u), actualSurveyList);
        }
        
        // ------------------
        // Taxon Group ID
        // ------------------
        {
            int actualTaxonGroupID = Integer.parseInt(modelMap.get("taxon_group_id").toString());
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_TAXON_GROUP_ID)) {
                int param = Integer.parseInt(queryParams.get(MySightingsController.QUERY_PARAM_TAXON_GROUP_ID)[0].toString());
                if(param < 0) {
                    Assert.assertEquals(Integer.parseInt(MySightingsController.DEFAULT_GROUP_ID), actualTaxonGroupID);
                } else {
                    Assert.assertEquals(param, actualTaxonGroupID);
                }
            } else {
                Assert.assertEquals(Integer.parseInt(MySightingsController.DEFAULT_GROUP_ID), actualTaxonGroupID);
            }
        }
        
        // ------------------
        // Download Format
        // ------------------
        {
            boolean actualKMLSelected = (Boolean)modelMap.get("download_kml_selected");
            boolean actualSHPSelected = (Boolean)modelMap.get("download_shp_selected");
            boolean actualXLSSelected = (Boolean)modelMap.get("download_xls_selected");
            
            String[] params;
            if(queryParams.containsKey(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT)) {
                params = queryParams.get(MySightingsController.QUERY_PARAM_DOWNLOAD_FORMAT);
            } else {
                params = new String[] {};
            }
            
            Arrays.sort(params);
            
            boolean expectedKMLSelected = Arrays.binarySearch(params, RecordDownloadFormat.KML.toString()) > -1;
            boolean expectedSHPSelected = Arrays.binarySearch(params, RecordDownloadFormat.SHAPEFILE.toString()) > -1;
            boolean expectedXLSSelected = Arrays.binarySearch(params, RecordDownloadFormat.XLS.toString()) > -1;
            
            Assert.assertEquals(expectedKMLSelected, actualKMLSelected);
            Assert.assertEquals(expectedSHPSelected, actualSHPSelected);
            Assert.assertEquals(expectedXLSSelected, actualXLSSelected);
        }
        
        // ------------------
        // Page Number
        // ------------------
        {
            Assert.assertEquals(1, ((Integer)modelMap.get("page_number")).intValue());
        }
    }
}
