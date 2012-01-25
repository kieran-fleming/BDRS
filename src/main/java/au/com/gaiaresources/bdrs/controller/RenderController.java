package au.com.gaiaresources.bdrs.controller;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.record.AtlasController;
import au.com.gaiaresources.bdrs.controller.record.SingleSiteAllTaxaController;
import au.com.gaiaresources.bdrs.controller.record.SingleSiteMultiTaxaController;
import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.controller.record.YearlySightingsController;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class RenderController extends AbstractController {
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private RecordDAO recordDAO;

    @Autowired
    private PreferenceDAO prefsDAO;

    private Logger log = Logger.getLogger(this.getClass());

    public static final String SURVEY_RENDER_REDIRECT_URL = "/bdrs/user/surveyRenderRedirect.htm";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_SURVEY_ID = "surveyId";

    /**
     * Redirects the request to the appropriate survey renderer depending upon
     * the <code>SurveyFormRendererType</code> of the survey.
     * 
     * @param request
     *            the http request.
     * @param response
     *            the http response.
     * @param surveyId
     *            the primary key of the survey in question.
     * @return redirected view to the survey renderer.
     */
    @RolesAllowed( { Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @SuppressWarnings("unchecked")
    @RequestMapping(value = SURVEY_RENDER_REDIRECT_URL, method = RequestMethod.GET)
    public ModelAndView surveyRendererRedirect(HttpServletRequest request,
            HttpServletResponse response) {

        int surveyId = 0;
        
        // if there is a record id present we shouldn't need to look for the
        // survey id separately.
        if (StringUtils.hasLength(request.getParameter(PARAM_RECORD_ID))) {
            try {
                Integer recId = Integer.valueOf(request.getParameter(PARAM_RECORD_ID));
                Record rec = recordDAO.getRecord(recId);
                if (rec != null && rec.getSurvey() != null && rec.getSurvey().getId() != null) {
                    surveyId = rec.getSurvey().getId().intValue();
                }
            } catch (NumberFormatException nfe) {
                log.error("Invalid record requested. recordId : " + request.getParameter(PARAM_RECORD_ID));
            }
        } 

		if (surveyId == 0) {
            try {
                surveyId = Integer.parseInt(request.getParameter(PARAM_SURVEY_ID));
            } catch (NumberFormatException nfe) {
                try {
                    log.debug("Default is : "
                            + prefsDAO.getPreferenceByKey("survey.default").getValue());
                    surveyId = Integer.parseInt(prefsDAO.getPreferenceByKey("survey.default").getValue());
                } catch (Exception e) {
                    // Either preference isn't set (nullpointer) or it's not an integer (numberformatexception)
                    try {
                        log.error("Default survey is incorrectly configured");
                        response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                    } catch (IOException ioe) {
                        log.error(ioe.getMessage(), ioe);
                    }
                    return null;
                }
            }   
		}

        Survey survey = surveyDAO.getSurvey(surveyId);
        SurveyFormRendererType renderer = survey.getFormRendererType();

        renderer = renderer == null ? SurveyFormRendererType.DEFAULT : renderer;
        String redirectURL;
        switch (renderer) {
        case YEARLY_SIGHTINGS:
            redirectURL = YearlySightingsController.YEARLY_SIGHTINGS_URL;
            break;
        case SINGLE_SITE_MULTI_TAXA:
            redirectURL = SingleSiteMultiTaxaController.SINGLE_SITE_MULTI_TAXA_URL;
            break;
        case SINGLE_SITE_ALL_TAXA:
            redirectURL = SingleSiteAllTaxaController.SINGLE_SITE_ALL_TAXA_URL;
            break;
        case ATLAS:
            redirectURL = AtlasController.ATLAS_URL;
            break;
        case DEFAULT:
            // Fall through
        default:
            redirectURL = TrackerController.EDIT_URL;
        }

        ModelAndView mv = this.redirect(redirectURL);
        
        mv.addAllObjects(request.getParameterMap());
        
        if (!StringUtils.hasLength(request.getParameter(PARAM_SURVEY_ID))) {
            mv.addObject(PARAM_SURVEY_ID, Integer.toString(surveyId));    
        }
        return mv;
    }
}
