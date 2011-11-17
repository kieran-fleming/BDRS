package au.com.gaiaresources.bdrs.service.facet;

import au.com.gaiaresources.bdrs.db.impl.Predicate;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The </code>CensusMethodTypeFacetOption</code> represents a single census 
 * method type of the records to be retrieved. 
 */
public class CensusMethodTypeFacetOption extends FacetOption {
    /**
     * A special value used to denote the lack of a census method.
     */
    public static final String NOCENSUSMETHODVALUE = "NoCensusMethod";
    
    private String methodType;

    /**
     * Creates a new instance of this class.
     * 
     * @param methodType the human readable name of this option.
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public CensusMethodTypeFacetOption(String methodType, Long count, String[] selectedOpts) {
        super(methodType, methodType, count, 
              Arrays.binarySearch(selectedOpts, methodType) > -1);
        this.methodType = methodType;
    }

    /**
     * Creates a new instance of this class. This constructor will create
     * an option to indicate the lack of a census method.
     * 
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public CensusMethodTypeFacetOption(Long count, String[] selectedOpts) {
        super("Observation", NOCENSUSMETHODVALUE, count, 
              Arrays.binarySearch(selectedOpts, NOCENSUSMETHODVALUE) > -1);
        
        this.methodType = null;
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        if(this.methodType == null) {
            return new Predicate("(censusMethod = null)");
        } else {
            return Predicate.eq("censusMethod.type", methodType);
        }
    }
}
