package au.com.gaiaresources.bdrs.controller.record;

import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormSubmitAction;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

public class YearlySightingsControllerMsgAndRedirectTest extends
        AbstractControllerTest {
    
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired 
    private MetadataDAO metadataDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RedirectionService redirectionService;
    
    private Survey survey;
    private TaxonGroup taxonGroup;
    private IndicatorSpecies speciesA;
    private Location locationA;
    
    private Logger log = Logger.getLogger(getClass());

    @Before
    public void setUp() throws Exception {
        taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);
        
        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);
        
        HashSet<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
        speciesSet.add(speciesA);

        survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA);
        metadataDAO.save(md);
        survey.setSpecies(speciesSet);
        survey = surveyDAO.save(survey);
        
        User admin = userDAO.getUser("admin");
        
        locationA = new Location();
        locationA.setName("Location A");        
        locationA.setUser(admin);
        locationA.setLocation(locationService.createPoint(-40.58, 153.1));
        locationDAO.save(locationA);
        
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI(YearlySightingsController.YEARLY_SIGHTINGS_URL);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put(YearlySightingsController.PARAM_SURVEY_ID, survey.getId().toString());
        params.put(YearlySightingsController.PARAM_LOCATION_ID, locationA.getId().toString());

        Date startDate = SurveyFormRendererType.YEARLY_SIGHTINGS.getStartDateForSightings(survey);
        Calendar cal = Calendar.getInstance();
        cal.setTime(startDate);  // start at the start date for the survey...
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
       
        Integer sighting = Integer.valueOf(cal.get(Calendar.DAY_OF_YEAR));
        params.put(String.format("date_%d", cal.getTimeInMillis()), sighting.toString());
        
        request.setParameters(params);
    }
    
    @Test
    public void testAddAnother() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.MY_SIGHTINGS, metadataDAO);
        request.setParameter(RecordWebFormContext.PARAM_SUBMIT_AND_ADD_ANOTHER, "true");
        ModelAndView mv = handle(request, response);
        this.assertRedirect(mv, RecordWebFormContext.SURVEY_RENDER_REDIRECT_URL);
        this.assertMessageCode(YearlySightingsController.MSG_CODE_SUCCESS_ADD_ANOTHER);
    }
    
    @Test
    public void testMySightingsRedirect() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.MY_SIGHTINGS, metadataDAO);
        ModelAndView mv = handle(request, response);
        this.assertRedirect(mv, redirectionService.getMySightingsUrl(survey));
        this.assertMessageCode(YearlySightingsController.MSG_CODE_SUCCESS_MY_SIGHTINGS);
    }
    
    @Test
    public void testStayOnFormRedirect() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.STAY_ON_FORM, metadataDAO);
        ModelAndView mv = handle(request, response);
        Assert.assertEquals("count mismatch", 1, recordDAO.countAllRecords().intValue());
        Record rec = recordDAO.getLatestRecord();
        this.assertRedirect(mv, redirectionService.getViewRecordUrl(rec));
        this.assertMessageCode(YearlySightingsController.MSG_CODE_SUCCESS_STAY_ON_FORM);
    }
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
    }
}
