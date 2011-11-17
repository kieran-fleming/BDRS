package au.com.gaiaresources.bdrs.service.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The </code>UserFacetOption</code> represents a single user whose records
 * will be retrieved. 
 */
public class UserFacetOption extends FacetOption {
    
    private User user;

    /**
     * Creates a new instance of this class.
     * 
     * @param user the human readable name of this option.
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public UserFacetOption(User user, Long count, String[] selectedOpts) {
        super(user.getFullName(), String.valueOf(user.getId()), count, 
              Arrays.binarySearch(selectedOpts, String.valueOf(user.getId())) > -1);
        
        this.user = user;
        
        if(this.user.equals(RequestContextHolder.getContext().getUser())) {
            super.setDisplayName("My Records Only");
        }
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        return Predicate.eq("record.user.id", user.getId());
    }
}
