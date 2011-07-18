package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The <code>MultimediaFacet</code> restricts records depending if
 * it contains a non-empty file or image record attribute.
 */
public class MultimediaFacet extends AbstractFacet {
    
    public static final String QUERY_PARAM_NAME = "multimedia";
    public static final String DISPLAY_NAME = "Multimedia";

    public MultimediaFacet(RecordDAO recordDAO,  Map<String, String[]> parameterMap) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, parameterMap.containsKey(QUERY_PARAM_NAME));
        
        String[] selectedOptions = parameterMap.get(QUERY_PARAM_NAME);
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        for(Pair<String, Long> pair : recordDAO.getDistinctAttributeTypes(null, new AttributeType[]{AttributeType.FILE, AttributeType.IMAGE})) {
            super.addFacetOption(new MultimediaFacetOption(AttributeType.find(pair.getFirst(), AttributeType.values()), pair.getSecond(), selectedOptions));
        }
    }
    
    @Override
    public void applyPredicate(HqlQuery q) {
        Predicate facetPredicate = null;
        for(FacetOption opt : super.getFacetOptions()) {
            if(opt.isSelected()) {
                Predicate optPredicate = ((MultimediaFacetOption)opt).getPredicate(); 
                facetPredicate = facetPredicate == null ? optPredicate : facetPredicate.or(optPredicate);
            }
        }
        
        if(facetPredicate != null) {
            q.and(facetPredicate);
        }
    }
}
