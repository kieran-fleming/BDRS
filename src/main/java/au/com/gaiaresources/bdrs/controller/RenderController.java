package au.com.gaiaresources.bdrs.controller;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class RenderController extends AbstractController {
    @Autowired
    private SurveyDAO surveyDAO;
    
    @Autowired
    private PreferenceDAO prefsDAO;
    
    private Logger log = Logger.getLogger(this.getClass());

    /**
     * Redirects the request to the appropriate survey renderer depending upon
     * the <code>SurveyFormRendererType</code> of the survey.
     *
     * @param request the http request.
     * @param response the http response.
     * @param surveyId the primary key of the survey in question.
     * @return redirected view to the survey renderer.
     */
    @RolesAllowed({Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN})
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/bdrs/user/surveyRenderRedirect.htm", method = RequestMethod.GET)
    public ModelAndView surveyRendererRedirect(HttpServletRequest request,
            HttpServletResponse response) {
    	
    	int surveyId = 0;
    	
    	try {
			surveyId = Integer.parseInt(request.getParameter("surveyId"));
    	} catch (NumberFormatException nfe) {
    		try {
    			log.debug("Default is : " + prefsDAO.getPreferenceByKey("survey.default").getValue());
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
    	
        Survey survey = surveyDAO.getSurvey(surveyId);
        SurveyFormRendererType renderer = survey.getFormRendererType();

        renderer = renderer == null ? SurveyFormRendererType.DEFAULT : renderer;
        String redirectURL;
        switch(renderer) {
            case YEARLY_SIGHTINGS:
                redirectURL = "yearlySightings.htm";
                break;
            case SINGLE_SITE_MULTI_TAXA:
                // The single site multiple taxa form is only used for adding
                // records. 
                if(request.getParameter("recordId") == null) {
                    redirectURL = "singleSiteMultiTaxa.htm";
                    
                } else {
                    redirectURL = "tracker.htm";
                }
                break;
            case ATLAS:
                redirectURL = "atlas.htm";
                break;
            case DEFAULT:
                // Fall through
            default:
                redirectURL = "tracker.htm";
        }

        ModelAndView mv = new ModelAndView(new RedirectView(redirectURL));
        mv.addAllObjects(request.getParameterMap());
        mv.addObject("surveyId", surveyId);
        return mv;
    }
}
