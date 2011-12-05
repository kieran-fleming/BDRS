package au.com.gaiaresources.bdrs.service.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;

/**
 * A {@link FacetOption} for showing only held records.
 * @author stephanie
 */
public class ModerationFacetOption extends FacetOption {

    private boolean held = false;
    
    public ModerationFacetOption(String value, Long count, boolean selected) {
        super("Held Records Only", value, count, selected);
        this.held = selected;
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.FacetOption#getPredicate()
     */
    @Override
    public Predicate getPredicate() {
        return Predicate.eq("record.held", held);
    }

}
