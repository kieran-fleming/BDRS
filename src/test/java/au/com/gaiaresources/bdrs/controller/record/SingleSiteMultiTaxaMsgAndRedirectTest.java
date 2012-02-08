package au.com.gaiaresources.bdrs.controller.record;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormSubmitAction;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

public class SingleSiteMultiTaxaMsgAndRedirectTest extends
        AbstractControllerTest {

    @Autowired
    protected SurveyDAO surveyDAO;
    @Autowired
    protected TaxaDAO taxaDAO;
    @Autowired
    protected MetadataDAO metadataDAO;
    @Autowired
    protected RecordDAO recordDAO;
    @Autowired
    protected RedirectionService redirectionService;

    protected Survey survey;
    protected TaxonGroup taxonGroup;
    protected IndicatorSpecies speciesA;

    @Before
    public void setUp() throws Exception {
        setup(SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA);
    }
    
    protected void setup(SurveyFormRendererType renderType) throws Exception {
        taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);

        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);

        survey = new Survey();
        // make sure that the survey's record visibility is applied...
        survey.setDefaultRecordVisibility(RecordVisibility.CONTROLLED, metadataDAO);
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setName(renderType.getName()+" 1234");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription(renderType.getName()+" Survey Description");
        Metadata md = survey.setFormRendererType(renderType);
        metadataDAO.save(md);
        survey = surveyDAO.save(survey);
        
        
        // setup request params...
        // ------------------------------------------------------------------
        
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI(SingleSiteMultiTaxaController.SINGLE_SITE_MULTI_TAXA_URL);

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);

        Calendar cal = Calendar.getInstance();
        cal.set(2010, 10, 12, 15, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date sightingDate = cal.getTime();

        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());
        params.put("latitude", "-36.879620605027");
        params.put("longitude", "126.650390625");
        params.put("date", dateFormat.format(sightingDate));
        params.put("time_hour", new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString());
        params.put("time_minute", new Integer(cal.get(Calendar.MINUTE)).toString());
        params.put("notes", "This is a test record");
        params.put("sightingIndex", "1");

        Map<IndicatorSpecies, Map<Attribute, Object>> recordScopeAttributeValueMapping = new HashMap<IndicatorSpecies, Map<Attribute, Object>>(
                2);
        Map<Attribute, Object> attributeValueMapping;

        // We have 2 species set up so lets save them both
        int sightingIndex = 0;
        for (IndicatorSpecies taxon : new IndicatorSpecies[] { speciesA }) {
            params.put(String.format("%d_survey_species_search", sightingIndex), taxon.getScientificName());
            params.put(String.format("%d_species", sightingIndex), taxon.getId().toString());
            params.put(String.format("%d_number", sightingIndex), Integer.valueOf(sightingIndex + 21).toString());
            params.put(SingleSiteController.PARAM_ROW_PREFIX, String.format("%d_", sightingIndex));

            attributeValueMapping = new HashMap<Attribute, Object>();
            recordScopeAttributeValueMapping.put(taxon, attributeValueMapping);
            sightingIndex += 1;
        }

        request.setParameters(params);
    }
    
    @Test
    public void testAddAnother() throws Exception {
        
        survey.setFormSubmitAction(SurveyFormSubmitAction.MY_SIGHTINGS, metadataDAO);
        request.setParameter(RecordWebFormContext.PARAM_SUBMIT_AND_ADD_ANOTHER, "true");
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        this.assertRedirect(mv, RecordWebFormContext.SURVEY_RENDER_REDIRECT_URL);
        this.assertMessageCode(SingleSiteController.MSG_CODE_SUCCESS_ADD_ANOTHER);
    }
    
    @Test
    public void testMySightings() throws Exception {
        
        survey.setFormSubmitAction(SurveyFormSubmitAction.MY_SIGHTINGS, metadataDAO);
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        this.assertRedirect(mv, redirectionService.getMySightingsUrl(survey));
        this.assertMessageCode(SingleSiteController.MSG_CODE_SUCCESS_MY_SIGHTINGS);
    }
    
    @Test
    public void testStayOnForm() throws Exception {
        
        survey.setFormSubmitAction(SurveyFormSubmitAction.STAY_ON_FORM, metadataDAO);
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        Record rec = recordDAO.getLatestRecord();
        
        this.assertRedirect(mv, redirectionService.getViewRecordUrl(rec));
        this.assertMessageCode(SingleSiteController.MSG_CODE_SUCCESS_STAY_ON_FORM);
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
    }
}
