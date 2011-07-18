package au.com.gaiaresources.bdrs.controller.review.sightings;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.CensusMethodTypeFacet;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.Facet;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.FacetOption;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.MonthFacet;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.MultimediaFacet;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.SurveyFacet;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.TaxonGroupFacet;
import au.com.gaiaresources.bdrs.controller.review.sightings.facet.UserFacet;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery.SortOrder;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.bulkdata.AbstractBulkDataService;
import au.com.gaiaresources.bdrs.util.KMLUtils;
import edu.emory.mathcs.backport.java.util.Collections;

/**
 * Provides view controllers for the Facet, Map and List view of the ALA 
 * 'My Sightings' Page. 
 */
@SuppressWarnings("unchecked")
@Controller
public class AdvancedReviewSightingsController extends AbstractController{
    
    public static final String VIEW_TYPE_TABLE = "table";
    public static final Set<String> VALID_SORT_PROPERTIES;
    
    public static final String SURVEY_ID_QUERY_PARAM_NAME = "surveyId";
    public static final String SORT_BY_QUERY_PARAM_NAME = "sortBy";
    public static final String SORT_ORDER_QUERY_PARAM_NAME = "sortOrder";
    public static final String SEARCH_QUERY_PARAM_NAME = "searchText";
    public static final String RESULTS_PER_PAGE_QUERY_PARAM_NAME = "resultsPerPage";
    public static final String PAGE_NUMBER_QUERY_PARAM_NAME = "pageNumber";
    
    public static final String DEFAULT_RESULTS_PER_PAGE = "20";
    public static final String DEFAULT_PAGE_NUMBER = "1";
    
    static {
        Set<String> temp = new HashSet<String>();
        temp.add("record.when");
        temp.add("species.scientificName");
        temp.add("species.commonName"); 
        temp.add("location.name");
        temp.add("censusMethod.type");
        VALID_SORT_PROPERTIES = Collections.unmodifiableSet(temp);
    }
    
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private AbstractBulkDataService bulkDataService;
    
    /**
     * Provides a view of the facet listing and a skeleton of the map or list
     * view. The map or list view will populate itself via asynchronous 
     * javascript requests. 
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/review/sightings/advancedReview.htm", method = RequestMethod.GET)
    public ModelAndView advancedReview(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       @RequestParam(value=SURVEY_ID_QUERY_PARAM_NAME, required=false) Integer surveyId,
                                       @RequestParam(value=RESULTS_PER_PAGE_QUERY_PARAM_NAME, required=false, defaultValue=DEFAULT_RESULTS_PER_PAGE) Integer resultsPerPage,
                                       @RequestParam(value=PAGE_NUMBER_QUERY_PARAM_NAME, required=false, defaultValue=DEFAULT_PAGE_NUMBER) Integer pageNumber) {
        // We are changing the flush mode here to prevent checking for dirty
        // objects in the session cache. Normally this is desireable so that
        // you will not receive stale objects however in this situation
        // the controller will only be performing reads and the objects cannot
        // be stale. We are explicitly setting the flush mode here because
        // we are potentially loading a lot of objects into the session cache
        // and continually checking if it is dirty is prohibitively expensive.
        // https://forum.hibernate.org/viewtopic.php?f=1&t=936174&view=next
        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);
        
        List<Facet> facetList = getFacetList((Map<String, String[]>)request.getParameterMap());
        Long recordCount = countMatchingRecords(facetList,
                                                surveyId,
                                                request.getParameter(SEARCH_QUERY_PARAM_NAME));
        long pageCount = recordCount / resultsPerPage;
        if((recordCount % resultsPerPage) > 0) {
            pageCount += 1;
        }
        
        ModelAndView mv = new ModelAndView("advancedReview");
        mv.addObject("mapViewSelected", !VIEW_TYPE_TABLE.equals(request.getParameter("viewType")));
        mv.addObject("facetList", facetList);
        mv.addObject("surveyId", request.getParameter(SURVEY_ID_QUERY_PARAM_NAME));
        mv.addObject("sortBy", request.getParameter(SORT_BY_QUERY_PARAM_NAME));
        mv.addObject("sortOrder", request.getParameter(SORT_ORDER_QUERY_PARAM_NAME));
        mv.addObject("searchText", request.getParameter(SEARCH_QUERY_PARAM_NAME));
        mv.addObject("recordCount", recordCount);
        mv.addObject("resultsPerPage", resultsPerPage);
        mv.addObject("pageCount", pageCount);
        
        // Avoid the situation where the number of results per page is increased
        // thereby leaving a page number higher than the total page count.
        mv.addObject("pageNumber", Math.min(pageCount, pageNumber.longValue()));

        return mv;
    }
    
    /**
     * Returns the list of records matching the {@link Facet} criteria as KML.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/review/sightings/advancedReviewKMLSightings.htm", method = RequestMethod.GET)
    public void advancedReviewKMLSightings(HttpServletRequest request, HttpServletResponse response) throws IOException, JAXBException {
        
        // We are changing the flush mode here to prevent checking for dirty
        // objects in the session cache. Normally this is desireable so that
        // you will not receive stale objects however in this situation
        // the controller will only be performing reads and the objects cannot
        // be stale. We are explicitly setting the flush mode here because
        // we are potentially loading a lot of objects into the session cache
        // and continually checking if it is dirty is prohibitively expensive.
        // https://forum.hibernate.org/viewtopic.php?f=1&t=936174&view=next
        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);
        
        Integer surveyId = null;
        if(request.getParameter(SURVEY_ID_QUERY_PARAM_NAME) != null) {
            surveyId = Integer.parseInt(request.getParameter(SURVEY_ID_QUERY_PARAM_NAME));
        }
        List<Facet> facetList = getFacetList((Map<String, String[]>)request.getParameterMap());
        List<Record> recordList = getMatchingRecords(facetList, 
                                                     surveyId,
                                                     request.getParameter(SORT_BY_QUERY_PARAM_NAME), 
                                                     request.getParameter(SORT_ORDER_QUERY_PARAM_NAME),
                                                     request.getParameter(SEARCH_QUERY_PARAM_NAME),
                                                     null, null);
        response.setContentType(KMLUtils.KML_CONTENT_TYPE);
        KMLUtils.writeRecordsToKML(request.getContextPath(), 
                                   null, 
                                   recordList, 
                                   response.getOutputStream());
    }

    /**
     * Returns a JSON array of records matching the {@link Facet} criteria.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/review/sightings/advancedReviewJSONSightings.htm", method = RequestMethod.GET)
    public void advancedReviewJSONSightings(HttpServletRequest request, 
                                            HttpServletResponse response,
                                            @RequestParam(value=RESULTS_PER_PAGE_QUERY_PARAM_NAME, required=false, defaultValue=DEFAULT_RESULTS_PER_PAGE) Integer resultsPerPage,
                                            @RequestParam(value=PAGE_NUMBER_QUERY_PARAM_NAME, required=false, defaultValue=DEFAULT_PAGE_NUMBER) Integer pageNumber) throws IOException {
        
        // We are changing the flush mode here to prevent checking for dirty
        // objects in the session cache. Normally this is desireable so that
        // you will not receive stale objects however in this situation
        // the controller will only be performing reads and the objects cannot
        // be stale. We are explicitly setting the flush mode here because
        // we are potentially loading a lot of objects into the session cache
        // and continually checking if it is dirty is prohibitively expensive.
        // https://forum.hibernate.org/viewtopic.php?f=1&t=936174&view=next
        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);
        
        Integer surveyId = null;
        if(request.getParameter(SURVEY_ID_QUERY_PARAM_NAME) != null) {
            surveyId = new Integer(request.getParameter(SURVEY_ID_QUERY_PARAM_NAME));
        }
        List<Facet> facetList = getFacetList((Map<String, String[]>)request.getParameterMap());
        List<Record> recordList = getMatchingRecords(facetList,
                                                     surveyId,
                                                     request.getParameter(SORT_BY_QUERY_PARAM_NAME), 
                                                     request.getParameter(SORT_ORDER_QUERY_PARAM_NAME),
                                                     request.getParameter(SEARCH_QUERY_PARAM_NAME),
                                                     resultsPerPage, pageNumber);

        JSONArray array = new JSONArray();
        for(Record rec : recordList) {
            array.add(JSONObject.fromObject(rec.flatten(2)));
        }
        
        response.setContentType("application/json");
        response.getWriter().write(array.toString());
        response.getWriter().flush();
    }
    
    /**
     * Returns an XLS representation of representation of records matching the
     * {@link Facet} criteria. This function should only be used if the records
     * are part of a single survey.
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/review/sightings/advancedReviewDownload.htm", method = RequestMethod.GET)
    public void advancedReviewDownload(HttpServletRequest request, 
                                       HttpServletResponse response,
                                       @RequestParam(value=SURVEY_ID_QUERY_PARAM_NAME, required=true) int surveyId) throws IOException {
        
        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);
        
        List<Facet> facetList = getFacetList((Map<String, String[]>)request.getParameterMap());
        List<Record> recordList = getMatchingRecords(facetList,
                                                     surveyId,
                                                     request.getParameter(SORT_BY_QUERY_PARAM_NAME), 
                                                     request.getParameter(SORT_ORDER_QUERY_PARAM_NAME),
                                                     request.getParameter(SEARCH_QUERY_PARAM_NAME),
                                                     null, null);
        
        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition",
                        "attachment;filename=records_"
                                        + String.valueOf(System.currentTimeMillis()) + ".xls");
        bulkDataService.exportSurveyRecords(surveyDAO.getSurvey(surveyId), 
                                            recordList, response.getOutputStream());
    }
        
    /**
     * Generates the {@link List} of {@link Facet}s. Each facet will be configured
     * with the necessary {@link FacetOption}s and selection state.
     * @param parameterMap a mapping of query parameters.
     * @return the ordered {@link List} of {@link Facet}s. 
     */
    private List<Facet> getFacetList(Map<String, String[]> parameterMap) {
        
        List<Facet> facetList = new ArrayList<Facet>();
        facetList.add(new TaxonGroupFacet(recordDAO, parameterMap));
        facetList.add(new MonthFacet(recordDAO, parameterMap));
        
        boolean addSurveyFacet = true;
        if(parameterMap.get(SURVEY_ID_QUERY_PARAM_NAME) != null && 
                parameterMap.get(SURVEY_ID_QUERY_PARAM_NAME).length == 1) {
            try {
                Integer.parseInt(parameterMap.get(SURVEY_ID_QUERY_PARAM_NAME)[0].toString());
                addSurveyFacet = false;
            } catch(NumberFormatException nfe) {
                addSurveyFacet = true;
            }
        }
        
        if(addSurveyFacet) {
            facetList.add(new SurveyFacet(recordDAO, parameterMap));
        }
        
        facetList.add(new UserFacet(recordDAO, parameterMap));
        facetList.add(new MultimediaFacet(recordDAO, parameterMap));
        facetList.add(new CensusMethodTypeFacet(recordDAO, parameterMap));
        
        return facetList;
    }
    
    long countMatchingRecords(List<Facet> facetList, Integer surveyId, String searchText) {
        HqlQuery hqlQuery = new HqlQuery("select count(distinct record) from Record record");
        applyFacetsToQuery(hqlQuery, facetList, surveyId, searchText);
        
        Query query = toHibernateQuery(hqlQuery);
        Object result = query.uniqueResult();
        return Long.parseLong(result.toString(), 10);
    }

    private void applyFacetsToQuery(HqlQuery hqlQuery, List<Facet> facetList,
            Integer surveyId, String searchText) {
        
        hqlQuery.leftJoin("record.location", "location");
        hqlQuery.leftJoin("record.species", "species");
        hqlQuery.leftJoin("record.censusMethod", "censusMethod");
        hqlQuery.leftJoin("record.attributes", "recordAttribute");
        hqlQuery.leftJoin("recordAttribute.attribute", "attribute");
        
        for(Facet f : facetList) {
            f.applyPredicate(hqlQuery);
        }
        
        if(searchText != null && !searchText.isEmpty()) {
            Predicate searchPredicate = Predicate.ilike("record.notes", String.format("%%%s%%", searchText));
            searchPredicate.or(Predicate.ilike("species.scientificName", String.format("%%%s%%", searchText)));
            searchPredicate.or(Predicate.ilike("species.commonName", String.format("%%%s%%", searchText)));
            
            hqlQuery.and(searchPredicate);
        }
        
        if(surveyId != null) {
            hqlQuery.and(Predicate.eq("record.survey.id", surveyId));
        }
    }

    /**
     * Applies the selection criteria represented by the provided {@link Facet}s
     * and the associated {@link FacetOption}s returning the matching {@link List}
     * of {@link Record}. 
     * 
     * @param facetList the {@link Facet}s providing the selection criteria.
     * @param surveyId the primary key of the survey containing all eligible records.
     * The <code>surveyId</code> may be null if all surveys are allowed.
     * @param sortProperty the HQL property that should be used for sorting. 
     * The sortProperty may be null if no sorting is necessary.
     * @param sortOrder the sorting order
     * @param searchText textual restriction to be applied to matching records.
     * @return the {@link List} of matching {@link Record}s.
     * 
     * @see SortOrder
     */
    List<Record> getMatchingRecords(List<Facet> facetList,
                                            Integer surveyId,
                                            String sortProperty, 
                                            String sortOrder, 
                                            String searchText,
                                            Integer resultsPerPage,
                                            Integer pageNumber) {
        HqlQuery hqlQuery = new HqlQuery("select distinct record, species.scientificName, species.commonName, location.name, censusMethod.type from Record record");
        applyFacetsToQuery(hqlQuery, facetList, surveyId, searchText);
        
        if(sortProperty != null && sortOrder != null) {
            // NO SQL injection for you
            if(VALID_SORT_PROPERTIES.contains(sortProperty)) {
                hqlQuery.order(sortProperty, 
                               SortOrder.valueOf(sortOrder).name(),
                               null);
            }
        }
        
        Query query = toHibernateQuery(hqlQuery);
        
        if(resultsPerPage != null && pageNumber != null && resultsPerPage > 0 && pageNumber > 0) {
            log.debug("First result: " + ((pageNumber-1) * resultsPerPage));
            log.debug("Max results: " + resultsPerPage);
            query.setFirstResult((pageNumber-1) * resultsPerPage);
            query.setMaxResults(resultsPerPage);
        }
        
        List<Object[]> rowList = query.list();
        List<Record> recordList = new ArrayList<Record>(rowList.size());
        for(Object[] rowObj : rowList) {
            recordList.add((Record)rowObj[0]);
        }
        
        return recordList;
    }

    /**
     * Converts the {@link HQLQuery} to a {@ Query} representation.
     */
    private Query toHibernateQuery(HqlQuery hqlQuery) {
        log.debug(hqlQuery.getQueryString());
        Session sesh = getRequestContext().getHibernate();
        Query query = sesh.createQuery(hqlQuery.getQueryString());
        Object[] parameterValues = hqlQuery.getParametersValue();
        for(int i=0; i<parameterValues.length; i++) {
            query.setParameter(i, parameterValues[i]);
            log.debug(i+": "+parameterValues[i] == null ? "null" : parameterValues[i].toString());
        }
        return query;
    }
}
