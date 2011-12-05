package au.com.gaiaresources.bdrs.service.facet;

import edu.emory.mathcs.backport.java.util.Arrays;
import au.com.gaiaresources.bdrs.db.impl.Predicate;

/**
 * Creates a {@link FacetOption} for showing records for an {@link AttributeValue} of 
 * type String. 
 * @author stephanie
 */
public class StringAttributeFacetOption extends FacetOption {

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
    public StringAttributeFacetOption(String attrValue, Long count, String[] selectedOpts, int facetIndex) {
        super(attrValue, attrValue, count, Arrays.binarySearch(selectedOpts, String.valueOf(attrValue)) > -1);
        this.attributeValue = attrValue;
        this.facetIndex = facetIndex;
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.FacetOption#getPredicate()
     */
    @Override
    public Predicate getPredicate() {
        return Predicate.eq("recordAttribute"+facetIndex+".stringValue", attributeValue);
    }

}
