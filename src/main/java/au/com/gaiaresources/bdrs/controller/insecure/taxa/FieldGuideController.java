/**
 *
 */
package au.com.gaiaresources.bdrs.controller.insecure.taxa;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.lang.StringEscapeUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.record.AtlasController;
import au.com.gaiaresources.bdrs.controller.record.SingleSiteAllTaxaController;
import au.com.gaiaresources.bdrs.controller.record.SingleSiteMultiTaxaController;
import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.controller.record.YearlySightingsController;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * A controller class to load data into the field guide pages
 * 
 * @author kehan
 * 
 */
@Controller
public class FieldGuideController extends AbstractController {
    Logger log = Logger.getLogger(FieldGuideController.class);
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private SpeciesProfileDAO speciesProfileDAO;
    @Autowired
    private ContentDAO helpDAO;

    @Autowired
    private SurveyDAO surveyDAO;
    public static final String PARAM_SPECIES_ID = "speciesId";
    /**
     * Species Information Page
     * 
     * @param model
     * @param request
     * @return
     */
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/public/speciesInfo.htm", method = RequestMethod.GET)
    public ModelAndView showSpeciesInfo(ModelMap model,
            HttpServletRequest request) {
        IndicatorSpecies selectedSpecies = taxaDAO.getIndicatorSpecies(new Integer(
                request.getParameter("spid")));
        TaxonGroup taxonGroup = selectedSpecies.getTaxonGroup();
        if (taxonGroup == null) {
            throw new NullPointerException(
                    "Taxon group for an indicator species cannot be null.");
        }

        List<SpeciesProfile> infoItems = speciesProfileDAO.getSpeciesProfileForSpecies(selectedSpecies.getId());
        Map<String, SpeciesProfile> infoItemsText = new HashMap<String, SpeciesProfile>();
        HashMap<Attribute, HashSet<String>> tags = new HashMap<Attribute, HashSet<String>>();
        HashMap<String, String> tagDescs = new HashMap<String, String>();
        /**
         * Add the individual species profiles individually into the model,
         * grouped by their type
         */
        for (SpeciesProfile s : infoItems) {
            if (s.getType().equalsIgnoreCase("text")) {
                infoItemsText.put(s.getHeader(), s);
            } else {
                if (!model.containsKey(s.getType())) {
                    model.addAttribute(s.getType(), new ArrayList<SpeciesProfile>());
                }
                ((ArrayList<SpeciesProfile>) model.get(s.getType())).add(s);
            }
        }
        for (IndicatorSpeciesAttribute indicatorSpeciesAttribute : selectedSpecies.getAttributes()) {
            Attribute attribute = indicatorSpeciesAttribute.getAttribute();
            if (tags.get(attribute) == null) {
                tags.put(attribute, new HashSet<String>());
            }
            tags.get(attribute).add(indicatorSpeciesAttribute.getStringValue());
            if ((indicatorSpeciesAttribute.getDescription() != null)
                    && (!indicatorSpeciesAttribute.getDescription().isEmpty()))
                tagDescs.put(indicatorSpeciesAttribute.getStringValue(), indicatorSpeciesAttribute.getDescription());
        }
        model.addAttribute("commonName", StringEscapeUtils.escapeHtml(selectedSpecies.getCommonName()));
        model.addAttribute("scientificName", StringEscapeUtils.escapeHtml(selectedSpecies.getScientificName()));
        model.addAttribute("featureLookup", tagDescs);
        model.addAttribute("features", tags);
        if (!infoItemsText.isEmpty()) {
            // Sort order of text items for displaying
            final String[] sortHeaders = { "DISTINCTIVE",
                    "IDENTIFYINGCHARACTERS", "HABITAT", "BIOLOGY",
                    "DISTRIBUTION", "BITE", "FOODPLANT", "DIET",
                    "NOCTURNAL", "PHYLUM", "CLASS", "ORDER", "FAMILY",
                    "GENUS", "SPECIES" };
            //				Map<String, String> sortHead = new HashMap<String, String>();
            model.addAttribute("text", new ArrayList<SpeciesProfile>());
            for (String header : sortHeaders) {
                if (infoItemsText.containsKey(header)) {
                    ((ArrayList<SpeciesProfile>) model.get("text")).add(infoItemsText.remove(header));
                }
            }
            for (SpeciesProfile s : infoItemsText.values()) {
                ((ArrayList<SpeciesProfile>) model.get("text")).add(s);
            }
        }
        
        model.addAttribute("speciesid", request.getParameter("spid"));
        model.addAttribute("taxonGroup", taxonGroup.getName());
        LinkedHashMap<String, String> breadcrumbs = new LinkedHashMap<String, String>();
        boolean isDesktop;
        if (request.getSession().getAttribute("surveyId") == null) {
            isDesktop = true;
        } else {
            isDesktop = false;
        }
        String base = "";
        if (!isDesktop) {
            base += "/mobile/";
        }
        String mode = request.getParameter("mode");
        model.addAttribute("mode", mode);
        String breadcrumbHome = "Groups";
        if (mode != null && mode.equalsIgnoreCase("helpmeid")) {
            breadcrumbHome = "Identification Tool";
        } else {
            breadcrumbHome = "Field Guide";
            mode = "fieldguide";
        }
        breadcrumbs.put(breadcrumbHome, base + "taxonList.htm?mode=" + mode);
        breadcrumbs.put(selectedSpecies.getTaxonGroup().getName(), base
                + "speciesList.htm?taxa="
                + selectedSpecies.getTaxonGroup().getName());
        if (mode.equalsIgnoreCase("helpmeid")) {
            breadcrumbs.put("Results", "back");
        }
        breadcrumbs.put(selectedSpecies.getCommonName(), null);
        model.addAttribute("breadcrumbs", breadcrumbs);
        ModelAndView mav;

        if (isDesktop) {
            mav = new ModelAndView("desktopSpeciesInfo", model);
            try {
                request.setCharacterEncoding("UTF8");
            } catch (UnsupportedEncodingException e) {
                // TODO Auto-generated catch block
                log.error(e);
            }

        } else {
            mav = new ModelAndView("mobileSpeciesInfo", model);
        }
        return mav;
    }

    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/fieldGuide.htm", method = RequestMethod.GET)
    public ModelAndView showTaxonList(ModelMap model, HttpServletRequest request) {
        List<? extends TaxonGroup> taxonGroups = taxaDAO.getTaxonGroups();
        ComparePersistentImplByWeight comp = new ComparePersistentImplByWeight();
        Collections.sort(taxonGroups, comp);
        model.addAttribute("taxaList", taxonGroups);
        String mode = request.getParameter("mode");
        if (mode == null) {
            mode = "fieldguide";
        }
        model.addAttribute("mode", mode);

        // Add help items.
        HashMap<String, String> helpItemMap = new HashMap<String, String>();
        helpItemMap.put(Content.HELP_FIELD_GUIDE, helpDAO.getContentValue(
                        getRequestContext().getHibernate(), Content.HELP_FIELD_GUIDE, 
                        getRequestContext().getPortal()));
        model.addAttribute(Content.HELP_ITEM_MAP, helpItemMap);

        return new ModelAndView("desktop-taxonList", model);
    }

    /**
     * Redirects the request to the appropriate survey renderer depending upon
     * the <code>SurveyFormRendererType</code> of the survey.
     * The survey is selected from the species id.  If no survey is found for the 
     * species, return an error message as such.  If more than one survey is found for 
     * the species, returns the first with a message to check the survey.  Otherwise, 
     * returns the one matching survey for the species.
     *
     * @param request the http request.
     * @param response the http response.
     * @param speciesId the primary key of the species to be recorded.
     * @return redirected view to the survey renderer.
     */
    @RolesAllowed({Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN})
    @SuppressWarnings("unchecked")
    @RequestMapping(value = "/bdrs/user/taxonSurveyRenderRedirect.htm", method = RequestMethod.GET)
    public ModelAndView recordNow(HttpServletRequest request,
            HttpServletResponse response) {
        
        try {
            int speciesId = Integer.parseInt(request.getParameter(PARAM_SPECIES_ID));
            IndicatorSpecies species = taxaDAO.getIndicatorSpecies(speciesId);
            List<Survey> surveys = surveyDAO.getSurveys(species);
            if (surveys.size() < 1) {
                // add a message that no surveys were found for the species
                getRequestContext().addMessage("bdrs.survey.noneForTaxa", new String[]{species.getCommonName()});
                log.warn("No surveys found for species id "+speciesId);
                return new ModelAndView(new RedirectView(request.getParameter("redirectURL")), request.getParameterMap());
            } else if (surveys.size() > 1) {
                // add a message that more than 1 surveys were found for the species
                log.warn("Multiple surveys found for species id "+speciesId);
                getRequestContext().addMessage("bdrs.survey.multipleForTaxa", new String[]{species.getCommonName()});
            }
            Survey survey = surveys.get(0);
            return getSurveyRenderRedirect(survey, request);
        } catch (Exception e) {
            log.error("Error occurred rendering survey for species id: "+request.getParameter(PARAM_SPECIES_ID), e);
            getRequestContext().addMessage("bdrs.survey.taxonomy.error");
            return new ModelAndView(new RedirectView(request.getParameter("redirectURL")), request.getParameterMap());
        }
    }

    private ModelAndView getSurveyRenderRedirect(Survey survey,
            HttpServletRequest request) {
        SurveyFormRendererType renderer = survey.getFormRendererType();

        renderer = renderer == null ? SurveyFormRendererType.DEFAULT : renderer;
        String redirectURL;
        switch (renderer) {
        // only handle atlas controller, all others redirect to tracker form by default
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
        mv.addObject("surveyId", survey.getId());
        return mv;
    }
}
