package au.com.gaiaresources.bdrs.model.survey;

import java.util.List;
import java.util.Set;

import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

public interface SurveyService {
	
	List<IndicatorSpecies> getSpecies(Survey s, String taxa);

	Set<TaxonGroup> getTaxa(Survey s);
}
