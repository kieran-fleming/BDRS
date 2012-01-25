package au.com.gaiaresources.bdrs.controller.insecure.taxa;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;

public class FieldGuideControllerTest extends AbstractControllerTest {

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    
    private Survey surveyA;
    private Survey surveyB;
    private Survey surveyC;
    
    private IndicatorSpecies speciesA;
    private IndicatorSpecies speciesB;
    private IndicatorSpecies speciesC;
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() {
        
        TaxonGroup group = new TaxonGroup();
        group.setName("test taxon group");
        group = taxaDAO.save(group);
        
        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("speciesA common");
        speciesA.setScientificName("speciesA scientific");
        speciesA.setTaxonGroup(group);
        speciesA = taxaDAO.save(speciesA);
        
        speciesB = new IndicatorSpecies();
        speciesB.setCommonName("speciesB common");
        speciesB.setScientificName("speciesB scientific");
        speciesB.setTaxonGroup(group);
        speciesB = taxaDAO.save(speciesB);
        
        speciesC = new IndicatorSpecies();
        speciesC.setCommonName("speciesC common");
        speciesC.setScientificName("speciesC scientific");
        speciesC.setTaxonGroup(group);
        speciesC = taxaDAO.save(speciesC);
        
        Calendar cal = Calendar.getInstance();
        cal.set(2010, 1, 2, 14, 30);
        Date startDate = cal.getTime();
        
        surveyA = new Survey();
        surveyA.setName("surveyA name");
        surveyA.setDescription("surveyA description");
        surveyA.setStartDate(startDate);
        {
            Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
            // empty species set i.e. ALL SPECIES
            surveyA.setSpecies(speciesSet);
        }
        surveyA = surveyDAO.save(surveyA);
        
        surveyB = new Survey();
        surveyB.setName("surveyB name");
        surveyB.setDescription("surveyB description");
        surveyB.setStartDate(startDate);
        {
            Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
            speciesSet.add(speciesB);
            surveyB.setSpecies(speciesSet);
        }
        surveyB = surveyDAO.save(surveyB);
        
        surveyC = new Survey();
        surveyC.setName("surveyC name");
        surveyC.setDescription("surveyC description");
        surveyC.setStartDate(startDate);
        {
            Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
            speciesSet.add(speciesC);
            surveyC.setSpecies(speciesSet);
        }
        surveyC = surveyDAO.save(surveyC);
    }
    
    @Test
    public void testRedirect_singleSurvey() throws Exception {
        
        this.login("admin", "password", new String[] { Role.ADMIN });
        
        request.setRequestURI(FieldGuideController.RECORD_NOW_SURVEY_REDIRECT_URL);
        request.setMethod("GET");
        request.setParameter(FieldGuideController.PARAM_SPECIES_ID, speciesA.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, TrackerController.EDIT_URL);       
        this.assertStringArray(mv, FieldGuideController.PARAM_SPECIES_ID, speciesA.getId().toString());
    }
    
    @Test
    public void testRedirect_multiSurvey() throws Exception {
        
        this.login("admin", "password", new String[] { Role.ADMIN });
        
        request.setRequestURI(FieldGuideController.RECORD_NOW_SURVEY_REDIRECT_URL);
        request.setMethod("GET");
        request.setParameter(FieldGuideController.PARAM_SPECIES_ID, speciesB.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertViewName(mv, FieldGuideController.VIEW_RECORD_NOW_SURVEY_CHOOSER);
        List<Survey> surveyList = (List<Survey>)mv.getModel().get(FieldGuideController.MODEL_SURVEY_LIST);
        Assert.assertNotNull("survey list cannot be null", surveyList);
        Assert.assertEquals("survey list unexpected size", 2, surveyList.size());
        Assert.assertTrue("survey list doesn't contain expected survey", surveyList.contains(surveyA));
        Assert.assertTrue("survey list doesn't contain expected survey", surveyList.contains(surveyB));
        IndicatorSpecies speciesUnderTest = (IndicatorSpecies)mv.getModel().get(FieldGuideController.MODEL_SPECIES);
        Assert.assertNotNull("species object cannot be null", speciesUnderTest);
        Assert.assertEquals("mismatch species", speciesB, speciesUnderTest);
    }
    
    @Test
    public void testRedirect_surveySelected() throws Exception {
        
        this.login("admin", "password", new String[] { Role.ADMIN });
        
        request.setRequestURI(FieldGuideController.RECORD_NOW_SURVEY_REDIRECT_URL);
        request.setMethod("GET");
        request.setParameter(FieldGuideController.PARAM_SPECIES_ID, speciesC.getId().toString());
        request.setParameter(FieldGuideController.PARAM_SURVEY_ID, surveyC.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, TrackerController.EDIT_URL);
        this.assertStringArray(mv, FieldGuideController.PARAM_SPECIES_ID, speciesC.getId().toString());
        ModelAndViewAssert.assertModelAttributeValue(mv, FieldGuideController.PARAM_SURVEY_ID, surveyC.getId());
    }
    
    /**
     * @param mv
     * @param modelName
     * @param expectedValue
     */
    private void assertStringArray(ModelAndView mv, String modelName, String expectedValue) {
        Assert.assertTrue("unexpected type", mv.getModel().get(modelName) instanceof String[]);
        String[] valueArray = (String[])mv.getModel().get(modelName);
        Assert.assertEquals("string array size mismatch", 1, valueArray.length);
        Assert.assertEquals("string array value does not match", expectedValue, valueArray[0]);
    }
}
