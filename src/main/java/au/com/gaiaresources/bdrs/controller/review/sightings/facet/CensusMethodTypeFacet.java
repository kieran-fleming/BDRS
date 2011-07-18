package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The <code>CensusMethodTypeFacet</code> restricts records to the type of the
 * associated census method or a null census method. 
 */
public class CensusMethodTypeFacet extends AbstractFacet {
    
    public static final String QUERY_PARAM_NAME = "censusMethod";
    public static final String DISPLAY_NAME = "Record Type";

    public CensusMethodTypeFacet(RecordDAO recordDAO,  Map<String, String[]> parameterMap) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, parameterMap.containsKey(QUERY_PARAM_NAME));
        
        String[] selectedOptions = parameterMap.get(QUERY_PARAM_NAME);
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        // Special entry for null census methods. (Observation Type)
        
        Long count = Long.valueOf(recordDAO.countNullCensusMethodRecords());
        super.addFacetOption(new CensusMethodTypeFacetOption(count, selectedOptions));
        
        // All other situations
        for(Pair<String, Long> pair : recordDAO.getDistinctCensusMethodTypes(null)) {
            super.addFacetOption(new CensusMethodTypeFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
        }
    }
    
    @Override
    public void applyPredicate(HqlQuery q) {
        Predicate facetPredicate = null;
        for(FacetOption opt : super.getFacetOptions()) {
            if(opt.isSelected()) {
                Predicate optPredicate = ((CensusMethodTypeFacetOption)opt).getPredicate(); 
                facetPredicate = facetPredicate == null ? optPredicate : facetPredicate.or(optPredicate);
            }
        }
        
        if(facetPredicate != null) {
            q.and(facetPredicate);
        }
    }
}
