package au.com.gaiaresources.bdrs.controller.mobile;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

@Controller
public class BdrsCoreOfflineController extends AbstractController {
	
	Logger log = Logger.getLogger(BdrsCoreOfflineController.class);
	@Autowired
	private TaxaDAO taxaDAO;
	@Autowired
	private SurveyDAO surveyDAO;
	
	/**
	 * Handles the request for the one javaScript file that gets downloaded to the mobile device.
	 * @param 	request
	 * @param 	response
	 * @return	ModelAndView	A combined javaScript file generated through jsp.
	 */
	@RequestMapping(value = "/js/mobile/bdrs-mobile.js")
	public ModelAndView getDbJs(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("bdrsMobileJs");
		mv.addObject("contextPath", "cp");
		mv.addObject("deviceType", request.getSession().getAttribute("device"));
		response.setContentType("text/javascript");
		return mv;
	}
	
	/**
	 * Handles the request for the micro templates that gets downloaded to the mobile device.
	 * @param 	request
	 * @param 	response
	 * @return	ModelAndView	A combined javaScript file generated through jsp.
	 */
	@RequestMapping(value = "/js/mobile/bdrs-mobile-micro-templates.js")
	public ModelAndView getMicroTemplatesJs(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("bdrsMobileMicroTemplates");
		response.setContentType("text/javascript");
		return mv;
	}
	
	/**
	 * Handles the request for the manifest file that gets downloaded to the mobile device.
	 * @param 	request
	 * @param 	response
	 * @return	ModelAndView	A combined manifest file generated through jsp.
	 */
	@RequestMapping(value = "/bdrs/mobile/mobile.manifest")
	public ModelAndView getManifst(HttpServletRequest request,
			HttpServletResponse response) {
		request.getSession().setAttribute("manifestRequested", "true");
		ModelAndView mv = new ModelAndView("bdrsMobileManifest");
		List<Survey> surveys = surveyDAO.getActiveSurveysForUser(getRequestContext().getUser());
		Set<TaxonGroup> taxaSet = new HashSet<TaxonGroup>();
		Set<IndicatorSpecies> indicatorSpeciesSet = new HashSet<IndicatorSpecies>();
		for(Survey survey: surveys){
			if (survey != null){
				 for(IndicatorSpecies indicatorspecies:survey.getSpecies()){
					 taxaSet.add(indicatorspecies.getTaxonGroup());
					 indicatorSpeciesSet.add(indicatorspecies);
				 }
			}
		}
		
		Set<String> urls = getOfflineImageData(indicatorSpeciesSet, request);
		String version = String.valueOf(surveys.hashCode());
		version += String.valueOf(taxaSet.hashCode());
		version += String.valueOf(getRequestContext().getUser().hashCode());
		
		mv.addObject("imagesList", urls);
		mv.addObject("manifestVersion", version);
		response.setContentType("text/cache-manifest");

		return mv;
	}
    
	/**
	 * Handles the request for the device database content.
	 * @param request
	 * @param response
	 * @return ModelAndView
	 */
	@RequestMapping(value = "/js/mobile/bdrs-mobile-database-content.js")
	public ModelAndView prepDownloadJs(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("bdrsMobileDatabaseContent");
		List<Survey> surveys = surveyDAO
				.getActiveSurveysForUser(getRequestContext().getUser());
		Set<TaxonGroup> taxaSet = new HashSet<TaxonGroup>();
		Set<IndicatorSpecies> indicatorSpeciesSet = new HashSet<IndicatorSpecies>();
		//key is surveyId and value = arrayList with speciesValues
		HashMap<Integer,ArrayList<Integer>> surveySpecies = new HashMap<Integer, ArrayList<Integer>>();
		
		int surveysLocationsSize = 0;
		int surveyAttributesSize = 0;
		int surveyAttributesOptionsSize = 0;
		int surveysSpeciesSize = 0;
		int surveysSpeciesAttributesSize = 0;
		
		for (Survey survey : surveys) {
			if (survey != null) {
				surveysLocationsSize += survey.getLocations().size();
				surveyAttributesSize += survey.getAttributes().size();
				surveysSpeciesSize += survey.getSpecies().size();
				List<Attribute> surveyAttributes = survey.getAttributes();
				for (Attribute surveyAtt : surveyAttributes){
					surveyAttributesOptionsSize += surveyAtt.getOptions().size();
				}

				ArrayList<Integer> speciesIdsForSurvey=  new ArrayList< Integer>();
				Collection<IndicatorSpecies> speciesForSurvey = new ArrayList<IndicatorSpecies>();
				if (survey.getSpecies().size() == 0) {
					speciesForSurvey = taxaDAO.getIndicatorSpecies();
				}
				else {
					speciesForSurvey = survey.getSpecies();
				}
				for (IndicatorSpecies indicatorspecies : speciesForSurvey) {
					surveysSpeciesAttributesSize += indicatorspecies.getAttributes().size();
					taxaSet.add(indicatorspecies.getTaxonGroup());
					indicatorSpeciesSet.add(indicatorspecies);
					speciesIdsForSurvey.add(indicatorspecies.getId());
				}
				surveySpecies.put(survey.getId(), speciesIdsForSurvey);
			}
		}
		
		int userSize = 1;
		int taxaSize = taxaSet.size();
		int taxaAttributesSize = 0;
		for(TaxonGroup t : taxaSet){
			taxaAttributesSize += t.getAttributes().size();
		}
		int surveysSize = surveys.size();
		
		int total = userSize + taxaSize + (taxaAttributesSize  * 2) + surveysSize + (surveysLocationsSize * 2) + (surveyAttributesSize * 2) + surveyAttributesOptionsSize + (surveysSpeciesSize * 2) + surveysSpeciesAttributesSize;
		//TODO: include infoItems in calculation
		double stepSize = 100.0 / total;
		
		mv.addObject("taxa", taxaSet);
		mv.addObject("indicatorSpecies", indicatorSpeciesSet);
		mv.addObject("surveySpeciesMap", surveySpecies);
		
		mv.addObject("surveys", surveys);
		mv.addObject("user", getRequestContext().getUser());
		mv.addObject("stepSize",stepSize);
		response.setContentType("text/javascript");
		return mv;
	}
	

    private Set<String> getOfflineImageData(Set<IndicatorSpecies> species,
            HttpServletRequest request) {
        Set<String> urlSet = new HashSet<String>();
        Set<String> urls = new HashSet<String>();
        Set<IndicatorSpeciesAttribute> speciesAtts = new HashSet<IndicatorSpeciesAttribute>();
        List<? extends SpeciesProfile> infoItems = new ArrayList<SpeciesProfile>();
        for (IndicatorSpecies s : species) {
        	boolean hasThumb = false;
        	boolean hasImage = false;

            s = taxaDAO.refresh(s);

            // gets the thumbNail from the species taxonGroup
            urlSet.add(s.getTaxonGroup().getThumbNail());
            // gets the silhouette from the speciesAttributes
            speciesAtts = s.getAttributes();
            for (AttributeValue ia : speciesAtts) {
                if (ia.getStringValue().contains("/")) {
                    urlSet.add(ia.getStringValue());
                }
                infoItems = s.getInfoItems();
                // Gets the speciesThumbnail and distributionMap from the
                // speciesProfileAttributes
                for (SpeciesProfile p : infoItems) {
                    if (p.getType()
                            .matches("^(map_thumb|profile_img_med|thumb)$")) {
                    	if (p.getType().equals("thumb")) {
                    		if (!hasThumb) {
                    			urlSet.add(p.getContent());
                    			hasThumb = true;
                    		}
                    	} else if (p.getType().equals("profile_img_med")) {
                    		if (!hasImage) {
                    			urlSet.add(p.getContent());
                    			hasImage = true;
                    		}
                    	} else {
                    		urlSet.add(p.getContent());
                    	}
                    }

                }

            }

        }
        for (String u : urlSet) {

            File f = new File(request.getSession().getServletContext()
                    .getRealPath('/' + u));
            if (f.exists()) {
                urls.add(u.replaceAll(" ", "%20"));
            }
        }
        return urls;
    }
    
/*    @RequestMapping(value = "/bdrs/mobile/offlineConfirm.htm", method = RequestMethod.GET)
	public ModelAndView renderConfirm(HttpServletRequest request,
			HttpServletResponse response) {
		ModelAndView mv = new ModelAndView("offlinereq");
		DecimalFormat df = new DecimalFormat("#.##");
		Set<IndicatorSpecies> species = getSurvey(request).getSpecies();
		Set<String> urls = getOfflineImageData(species, request);
		double downloadFileSize  = getDownloadFileSize(request, urls)/(1024*1024);
		mv.addObject("dataSize", df.format(downloadFileSize));
		return mv;
	}*/

    private double getDownloadFileSize(HttpServletRequest request, Set<String> urls) {
        // TODO Auto-generated method stub
        double downloadFileSize = 0;
        for(String url: urls){

            File f = new File(request.getSession().getServletContext()
                    .getRealPath('/' + url));
            if(f.exists()){
                downloadFileSize += f.length();
            }
        }
        return downloadFileSize;
    }

   
    
	@RequestMapping(value = "/bdrs/mobile/hlp.htm", method = RequestMethod.GET)
	public ModelAndView getHelp(HttpServletRequest request,
			HttpServletResponse response) {
	    return new ModelAndView("help");
	}

    private Survey getSurvey(HttpServletRequest req) {
        Survey survey;
    	if (req.getSession().getAttribute("surveyId") != null) {
        	int surveyId = (Integer)req.getSession().getAttribute("surveyId");
            survey = surveyDAO.get(surveyId);
        } else {
        survey =  surveyDAO.getLastSurveyForUser(getRequestContext().getUser());
        }
        return survey;
    }

}
