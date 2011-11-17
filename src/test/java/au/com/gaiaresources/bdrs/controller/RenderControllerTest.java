package au.com.gaiaresources.bdrs.controller;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.record.AtlasController;
import au.com.gaiaresources.bdrs.controller.record.SingleSiteAllTaxaController;
import au.com.gaiaresources.bdrs.controller.record.SingleSiteMultiTaxaController;
import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.controller.record.YearlySightingsController;
import au.com.gaiaresources.bdrs.security.Role;

public class RenderControllerTest extends AbstractGridControllerTest {

    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(RenderController.SURVEY_RENDER_REDIRECT_URL);
        
        this.login("admin", "password", new String[] { Role.USER });
    }
    
    @Test
    public void redirectToSingleSiteMultiTaxa() throws Exception {
        request.setParameter(RenderController.PARAM_X_SURVEY_ID, this.singleSiteMultiTaxaSurvey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, SingleSiteMultiTaxaController.SINGLE_SITE_MULTI_TAXA_URL);
    }
    
    @Test
    public void redirectToSingleSiteAllTaxa() throws Exception {
        request.setParameter(RenderController.PARAM_X_SURVEY_ID, this.singleSiteAllTaxaSurvey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, SingleSiteAllTaxaController.SINGLE_SITE_ALL_TAXA_URL);
    }
    
    @Test
    public void redirectToAtlas() throws Exception {
        request.setParameter(RenderController.PARAM_X_SURVEY_ID, this.atlasSurvey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, AtlasController.ATLAS_URL);
    }
    
    @Test
    public void redirectToTracker() throws Exception {
        request.setParameter(RenderController.PARAM_X_SURVEY_ID, this.survey1.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, TrackerController.EDIT_URL);
    }
    
    @Test
    public void redirectToYearlySightings() throws Exception {
        request.setParameter(RenderController.PARAM_X_SURVEY_ID, this.yearlySightingSurvey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, YearlySightingsController.YEARLY_SIGHTINGS_URL);
    }
}
