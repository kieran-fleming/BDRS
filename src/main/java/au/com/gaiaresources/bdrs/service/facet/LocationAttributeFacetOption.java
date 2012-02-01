package au.com.gaiaresources.bdrs.service.facet;

import edu.emory.mathcs.backport.java.util.Arrays;
import au.com.gaiaresources.bdrs.db.impl.Predicate;

/**
 * Creates a {@link FacetOption} for showing records with a location for 
 * an {@link AttributeValue} of type String. 
 */
public class LocationAttributeFacetOption extends FacetOption {

    /**
     * The value of the attribute to show records for
     */
    private String attributeValue;
    
    private int facetIndex;
    
    /**
     * Creates an instance of this class.
     * @param attrValue The attribute value to query
     * @param count The number of records matching the attribute value
     * @param selectedOpts options for selecting the option by attribute value
     * @param facetIndex 
     */
    public LocationAttributeFacetOption(String attrValue, Long count, String[] selectedOpts, int facetIndex) {
        super(attrValue, attrValue, count, Arrays.binarySearch(selectedOpts, String.valueOf(attrValue)) > -1);
        this.attributeValue = attrValue;
        this.facetIndex = facetIndex;
    }

    @Override
    public Predicate getPredicate() {
        return Predicate.eq("locAttributeVal"+facetIndex+".stringValue", attributeValue);
    }
}
