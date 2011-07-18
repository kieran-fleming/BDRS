package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.Map;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The <code>UserFacet</code> restricts records to the selected set of users. 
 */
public class UserFacet extends AbstractFacet {
    
    public static final String QUERY_PARAM_NAME = "user";
    public static final String DISPLAY_NAME = "User";

    public UserFacet(RecordDAO recordDAO,  Map<String, String[]> parameterMap) {
        super(QUERY_PARAM_NAME, DISPLAY_NAME, parameterMap.containsKey(QUERY_PARAM_NAME));
        
        String[] selectedOptions = parameterMap.get(QUERY_PARAM_NAME);
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        RequestContext context = RequestContextHolder.getContext();
        User user = context.getUser();
        
        super.addFacetOption(new UserFacetOption(user, Long.valueOf(recordDAO.countRecords(user)), selectedOptions));
    }
    
    @Override
    public void applyPredicate(HqlQuery q) {
        Predicate facetPredicate = null;
        for(FacetOption opt : super.getFacetOptions()) {
            if(opt.isSelected()) {
                Predicate optPredicate = ((UserFacetOption)opt).getPredicate(); 
                facetPredicate = facetPredicate == null ? optPredicate : facetPredicate.or(optPredicate);
            }
        }
        
        if(facetPredicate != null) {
            q.and(facetPredicate);
        }
    }
}
