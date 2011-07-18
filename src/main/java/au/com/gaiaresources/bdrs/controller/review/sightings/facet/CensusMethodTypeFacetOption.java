package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The </code>CensusMethodTypeFacetOption</code> represents a single census 
 * method type of the records to be retrieved. 
 */
public class CensusMethodTypeFacetOption extends FacetOption {
    public static final String NOCENSUSMETHODVALUE = "NoCensusMethod";
    private String methodType;

    public CensusMethodTypeFacetOption(String methodType, Long count, String[] selectedOpts) {
        super(methodType, methodType, count, 
              Arrays.binarySearch(selectedOpts, methodType) > -1);
        this.methodType = methodType;
    }

    public CensusMethodTypeFacetOption(Long count, String[] selectedOpts) {
        super("Observation", NOCENSUSMETHODVALUE, count, 
              Arrays.binarySearch(selectedOpts, NOCENSUSMETHODVALUE) > -1);
        
        this.methodType = null;
    }

    public Predicate getPredicate() {
        if(this.methodType == null) {
            return new Predicate("(censusMethod = null)");
        } else {
            return Predicate.eq("censusMethod.type", methodType);
        }
    }
}
