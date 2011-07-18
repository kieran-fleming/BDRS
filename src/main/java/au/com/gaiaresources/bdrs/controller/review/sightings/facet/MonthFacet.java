package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.Date;
import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Restricts records on a per monthly basis. 
 */
public class MonthFacet extends AbstractFacet {
    
    public static final String QUERY_PARAM_NAME = "month";
    public static final String DISPLAY_NAME = "Month";

    public MonthFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, parameterMap.containsKey(QUERY_PARAM_NAME));
        
        String[] selectedOptions = parameterMap.get(QUERY_PARAM_NAME);
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        for(Pair<Date, Long> pair : recordDAO.getDistinctMonths(null)) {
            super.addFacetOption(new MonthFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
        }
    }

    @Override
    public void applyPredicate(HqlQuery q) {
        Predicate facetPredicate = null;
        for(FacetOption opt : super.getFacetOptions()) {
            if(opt.isSelected()) {
                
                MonthFacetOption mfo = (MonthFacetOption)opt;
                Predicate optPredicate = mfo.getPredicate();
                
                facetPredicate = facetPredicate == null ?  optPredicate : facetPredicate.or(optPredicate); 
            }
        }
        
        if(facetPredicate != null) {
            q.and(facetPredicate);
        }
    }
}
