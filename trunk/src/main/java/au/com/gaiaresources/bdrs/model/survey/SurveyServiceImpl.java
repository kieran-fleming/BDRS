package au.com.gaiaresources.bdrs.model.survey;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

@Service
public class SurveyServiceImpl implements SurveyService {
	 Logger log = Logger.getLogger(SurveyServiceImpl.class);
	
	@Autowired
	private SurveyDAO surveyDAO;

	@Override
	public Set<TaxonGroup> getTaxa(Survey s){
		Set<TaxonGroup> taxaSet = new HashSet<TaxonGroup>();
		Set<IndicatorSpecies> species = surveyDAO.getSpeciesWithinSurveyLocations(s);
		log.debug("Retrieving TaxonGroups for survey + " + s.getId() + " found : " + species.size());
		for(IndicatorSpecies aSpecies : species) {
			taxaSet.add(aSpecies.getTaxonGroup());
		}
		return taxaSet;
	}

	@Override
	public List<IndicatorSpecies> getSpecies(Survey s, String taxa) {
		List<IndicatorSpecies> taxaSpecies = new ArrayList<IndicatorSpecies>();
		Set<IndicatorSpecies> species = surveyDAO.getSpeciesWithinSurveyLocations(s);
		for(IndicatorSpecies aSpecies : species){
			if(aSpecies.getTaxonGroup().getName().equalsIgnoreCase(taxa))
				taxaSpecies.add(aSpecies);
		}
		return taxaSpecies;
	}

}
