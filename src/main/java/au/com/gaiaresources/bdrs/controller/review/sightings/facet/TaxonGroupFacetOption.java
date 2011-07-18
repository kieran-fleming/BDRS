package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents a single {@link TaxonGroup}
 */
public class TaxonGroupFacetOption extends FacetOption {
    
    private TaxonGroup taxonGroup;

    public TaxonGroupFacetOption(TaxonGroup taxonGroup, Long count, String[] selectedOpts) {
        super(taxonGroup.getName(), String.valueOf(taxonGroup.getId()), count, 
              Arrays.binarySearch(selectedOpts, String.valueOf(taxonGroup.getId())) > -1);
        
        this.taxonGroup = taxonGroup;           
    }

    public Predicate getPredicate() {
        return Predicate.eq("species.taxonGroup.id", taxonGroup.getId());
    }
}
