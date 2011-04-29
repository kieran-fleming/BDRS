package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

@Controller
public class ApplicationService extends AbstractController {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;

    @Autowired
    private TaxaDAO taxaDAO;

    @Autowired
    private UserDAO userDAO;

    @RequestMapping(value = "/webservice/application/survey.htm", method = RequestMethod.GET)
    public void getSurvey(
            HttpServletRequest request,
            HttpServletResponse response,

            @RequestParam(value = "ident", defaultValue = "") String ident,
            @RequestParam(value = "sid", defaultValue = "-1") int surveyRequested)
            throws IOException {

        // Checks if a user exists with the provided ident. If not a response error is returned.
        if (userDAO.getUserByRegistrationKey(ident) == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        //retrieve requested survey
        Survey survey = surveyDAO.getSurvey(surveyRequested);

        if (surveyRequested >= 0) {

            //Retrieve species from survey if any, otherwise get all from model
            Collection<IndicatorSpecies> species;
            if (survey.getSpecies().size() == 0) {
                species = new ArrayList<IndicatorSpecies>();
                species.addAll(taxaDAO.getIndicatorSpecies());
            } else {
                species = new HashSet<IndicatorSpecies>();
                species.addAll(survey.getSpecies());
            }

            List<TaxonGroup> taxonGroups = taxaDAO.getTaxonGroup(survey);

            // Remove data from the requested survey that already exists on the device.
            if (request.getParameter("surveysOnDevice") != null) {

                JSONArray surveysOnDeviceArray = JSONArray.fromObject(request.getParameter("surveysOnDevice"));

                for (Object sid : surveysOnDeviceArray) {
                    //remove species
                    species.removeAll(surveyDAO.getSurveyData((Integer) sid).getSpecies());
                    //remove taxonGroups
                    taxonGroups.removeAll(taxaDAO.getTaxonGroup(surveyDAO.getSurvey((Integer) sid)));
                }

            }

            // Restructure survey data
            JSONArray attArray = new JSONArray();
            JSONArray locArray = new JSONArray();
            JSONArray speciesArray = new JSONArray();
            JSONArray taxonGroupArray = new JSONArray();
            for (Attribute a : survey.getAttributes()) {
                attArray.add(a.flatten(1, true, true));
            }
            for (Location l : survey.getLocations()) {
                locArray.add(l.flatten(1, true, true));
            }
            for (IndicatorSpecies s : species) {
                speciesArray.add(s.flatten(2, true, true));
            }
            for (TaxonGroup t : taxonGroups) {
                taxonGroupArray.add(t.flatten(2, true, true));
            }

            // Store restructured survey data in JSONObject
            JSONObject surveyData = new JSONObject();
            surveyData.put("attributesAndOptions", attArray);
            surveyData.put("locations", locArray.toString());
            surveyData.put("indicatorSpecies_server_ids", survey.flatten());
            surveyData.put("indicatorSpecies", speciesArray);
            surveyData.put("taxonGroups", taxonGroupArray);

            // support for JSONP
            if (request.getParameter("callback") != null) {
                response.setContentType("application/javascript");
                response.getWriter().write(request.getParameter("callback")
                        + "(");
            } else {
                response.setContentType("application/json");
            }

            response.getWriter().write(surveyData.toString());
            if (request.getParameter("callback") != null) {
                response.getWriter().write(");");
            }

        }
    }

}
