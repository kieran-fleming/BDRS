package au.com.gaiaresources.bdrs.service.facet;

import edu.emory.mathcs.backport.java.util.Arrays;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Creates a {@link FacetOption} for showing all public records.  This will allow only public 
 * and unheld records to be shown to users and all records to be shown to admin users.
 * 
 * @author stephanie
 */
public class AllPublicRecordsUserFacetOption extends FacetOption {
    
    private User user;

    /**
     * Creates a new instance of this class.
     * 
     * @param user the user accessing the records
     * @param count the number of records that match this option.
     * @param selectedOpts an array of selected elements, to select this option, -1 should be added to this array
     */
    public AllPublicRecordsUserFacetOption(User user, Long count, String[] selectedOpts) {
        super("All Public Records", String.valueOf(-1), count, 
              Arrays.binarySearch(selectedOpts, String.valueOf(-1)) > -1);
        
        this.user = user;
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        if (user == null || !user.isAdmin()) {
            Predicate p = Predicate.eq("record.recordVisibility", RecordVisibility.PUBLIC).and(
                          Predicate.eq("record.held", false));
            return p;
        } else {
            return null;
        }
    }
}
