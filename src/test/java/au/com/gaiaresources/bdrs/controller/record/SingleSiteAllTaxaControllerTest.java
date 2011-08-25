package au.com.gaiaresources.bdrs.controller.record;

import org.junit.Before;
import org.junit.Test;

import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;

/**
 * Tests all aspects of the <code>SingleSiteMultiTaxaController</code>.
 */
public class SingleSiteAllTaxaControllerTest extends SingleSiteMultiTaxaControllerTest {

    @Before
    public void setUp() throws Exception {
        setup(SurveyFormRendererType.SINGLE_SITE_ALL_TAXA);
    }

    /**
     * Tests that a blank form can be retrieved.
     * 
     * @throws Exception
     */
    @Test
    public void testAddRecord() throws Exception {
        testAddRecord("/bdrs/user/singleSiteAllTaxa.htm", "singleSiteAllTaxa");
    }

    /**
     * Tests that additional rows can be retrieved. This is normally done via
     * ajax.
     * 
     * @throws Exception
     */
    @Test
    public void testAjaxAddSightingRow() throws Exception {
        testAjaxAddSightingRow("/bdrs/user/singleSiteAllTaxa/sightingTableAllTaxa.htm", "singleSiteAllTaxaRow");
    }

    /**
     * Tests that multiple records can be saved.
     * 
     * @throws Exception
     */
    @Test 
    public void testSaveRecordLowerLimitOutside() throws Exception{
    	testSaveRecord("99");
    }
    
    @Test 
    public void testSaveRecordLowerLimitEdge() throws Exception{
    	testSaveRecord("100");
    }
    
    @Test 
    public void testSaveRecordInRange() throws Exception{
    	testSaveRecord("101");
    }
    
    @Test 
    public void testSaveRecordUpperLimitEdge() throws Exception{
    	testSaveRecord("200");
    }
    
    @Test 
    public void testSaveRecordUpperLimitOutside() throws Exception{
    	testSaveRecord("201");
    }
    public void testSaveRecord(String intWithRangeValue) throws Exception {
        testSaveRecord(intWithRangeValue, "/bdrs/user/singleSiteAllTaxa.htm");
    }
    
    @Test
    public void testRecordFormPredefinedLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteAllTaxa.htm", true, SurveyFormRendererType.SINGLE_SITE_ALL_TAXA, true);
    }
    
    @Test
    public void testRecordFormLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteAllTaxa.htm", false, SurveyFormRendererType.SINGLE_SITE_ALL_TAXA, true);
    }
    
    @Test
    public void testRecordFormPredefinedLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteAllTaxa.htm", true, SurveyFormRendererType.SINGLE_SITE_ALL_TAXA, false);
    }
    
    @Test
    public void testRecordFormLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteAllTaxa.htm", false, SurveyFormRendererType.SINGLE_SITE_ALL_TAXA, false);
    }
}
