package au.com.gaiaresources.bdrs.model.survey;

import java.util.List;
import java.util.Set;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

public interface SurveyService {
	
	List<IndicatorSpecies> getSpecies(Survey s, String taxa);

	Set<TaxonGroup> getTaxa(Survey s);
	
	/*
         * looks through the census methods in the survey and traverses the census method tree, collecting
         * all of the unique census methods that take part in this survey.
         */
	Set<CensusMethod> catalogCensusMethods(Survey s);
}
