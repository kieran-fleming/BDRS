package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;
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
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.util.Pair;

/**
 * The Taxon Service provides a web API for Taxonomy and Taxonomy Group
 * based services.
 */
@Controller
public class TaxonomyService extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    @Autowired
    private TaxaDAO taxaDAO;

    @RequestMapping(value = "/webservice/taxon/searchTaxonGroup.htm", method = RequestMethod.GET)
    public void searchTaxonGroup(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        JSONArray array = new JSONArray();

        if(request.getParameter("q") != null) {
            List<TaxonGroup> taxonGroupList =
                taxaDAO.getTaxonGroupSearch(request.getParameter("q"));

            for(TaxonGroup group : taxonGroupList) {
                array.add(group.flatten());
            }
        }

        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }

    @RequestMapping(value = "/webservice/taxon/searchTaxon.htm", method = RequestMethod.GET)
    public void searchTaxon(HttpServletRequest request,
            HttpServletResponse response) throws IOException {

        JSONArray array = new JSONArray();

        if(request.getParameter("q") != null) {
            List<IndicatorSpecies> speciesList =
                taxaDAO.getIndicatorSpeciesByNameSearch(request.getParameter("q"));
            
            String depthStr = request.getParameter("depth");
            int depth = depthStr == null ? 0 : Integer.parseInt(depthStr);
            
            for(IndicatorSpecies species : speciesList) {
                array.add(species.flatten(depth));
            }
        }
        
        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }

    @RequestMapping(value = "/webservice/taxon/getTaxonById.htm", method = RequestMethod.GET)
    public void getTaxonById(@RequestParam(value="id", defaultValue="0") int taxonPk,
                             @RequestParam(value="depth", defaultValue="0", required=false) int depth,
                             HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        if(taxonPk > 0) {
            JSONObject obj = JSONObject.fromObject(taxaDAO.getIndicatorSpecies(taxonPk).flatten(depth));
            response.getWriter().write(obj.toString());
        }
    }
    
    @RequestMapping(value = "/webservice/taxon/getTaxonGroupById.htm", method = RequestMethod.GET)
    public void getTaxonGroupById(@RequestParam(value="id", defaultValue="0") int groupPk,
                            HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        if(groupPk > 0) {
            JSONObject obj = JSONObject.fromObject(taxaDAO.getTaxonGroup(groupPk).flatten());
            response.getWriter().write(obj.toString());
        }
    }
    
    @RequestMapping(value = "/webservice/taxon/getSpeciesProfileById.htm", method = RequestMethod.GET)
    public void getSpeciesProfileById(@RequestParam(value="id", required=true) int profilePk,
                            HttpServletResponse response) throws IOException {

        response.setContentType("application/json");
        JSONObject obj = JSONObject.fromObject(taxaDAO.getSpeciesProfileById(profilePk).flatten());
        response.getWriter().write(obj.toString());
    }
    
    @RequestMapping(value = "/webservice/taxon/topSpecies.htm", method = RequestMethod.GET)
    public void getTopSpecies(@RequestParam(value="user", defaultValue="0") int userPk,
            @RequestParam(value="limit", defaultValue="5") int limit,
    		HttpServletRequest request, HttpServletResponse response)
    			throws IOException
    {
    	// RequestParam user - the user
    	// RequestParam limit - the number of species to return
    	// [{ species : {species}, count : <number> }, ... ]
    	JSONArray array = new JSONArray();
    	log.debug("Top Species");
    	try {
    	   	List<Pair<IndicatorSpecies, Integer>> counts = taxaDAO.getTopSpecies(userPk, limit);
    	    for (Pair<IndicatorSpecies, Integer> i : counts) {
    	    	JSONObject ob = new JSONObject();
    	    	ob.put("species", i.getFirst().flatten());
    	    	ob.put("count", i.getSecond());
    	    	array.add(ob);
    	    }
    	} catch (Exception e) {
    		log.error(e);
    	}
    	
    	response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }
}
