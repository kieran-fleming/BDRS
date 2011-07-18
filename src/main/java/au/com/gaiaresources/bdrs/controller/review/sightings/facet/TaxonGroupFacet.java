package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents taxonomic records based on the {@link TaxonGroup} of the associated
 * @link {@link IndicatorSpecies}.
 */
public class TaxonGroupFacet extends AbstractFacet {
    
    public static final String QUERY_PARAM_NAME = "taxonGroup";
    public static final String DISPLAY_NAME = "Species Group";

    public TaxonGroupFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, parameterMap.containsKey(QUERY_PARAM_NAME));
        
        String[] selectedOptions = parameterMap.get(QUERY_PARAM_NAME);
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        for(Pair<TaxonGroup, Long> pair : recordDAO.getDistinctTaxonGroups(null)) {
            super.addFacetOption(new TaxonGroupFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
        }
    }

    @Override
    public void applyPredicate(HqlQuery q) {
        Predicate facetPredicate = null;
        for(FacetOption opt : super.getFacetOptions()) {
            if(opt.isSelected()) {
                Predicate optPredicate = ((TaxonGroupFacetOption)opt).getPredicate(); 
                facetPredicate = facetPredicate == null ? optPredicate : facetPredicate.or(optPredicate);  
            }
        }
        
        if(facetPredicate != null) {
            q.and(Predicate.enclose(facetPredicate));
        }
    }
}
