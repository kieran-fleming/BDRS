package au.com.gaiaresources.bdrs.service.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;

/**
 * Creates a {@link FacetOption} for anonymous users.  This will allow only public 
 * and unheld records to be shown to anonymous users.
 * 
 * @author stephanie
 */
public class AnonymousUserFacetOption extends FacetOption {

    /**
     * 
     * @param count
     * @param selected
     */
    public AnonymousUserFacetOption(Long count, boolean selected) {
        super("Anonymous", null, count, selected);
    }

    @Override
    public Predicate getPredicate() {
        return Predicate.eq("record.recordVisibility", RecordVisibility.PUBLIC).and(
               Predicate.eq("record.held", false));
    }
}
