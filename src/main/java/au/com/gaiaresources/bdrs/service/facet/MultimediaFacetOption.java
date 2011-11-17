package au.com.gaiaresources.bdrs.service.facet;


import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The </code>MultimediaFacetOption</code> represents a type of multimedia -
 * Image or File.
 */
public class MultimediaFacetOption extends FacetOption {
    
    private AttributeType attributeType;

    /**
     * Creates a new instance of this class.
     * 
     * @param attributeType the human readable name of this option.
     * @param count the number of records that match this option.
     * @param selectedOpts true if this option is applied, false otherwise.
     */
    public MultimediaFacetOption(AttributeType attributeType, Long count, String[] selectedOpts) {
        super(attributeType.getName(), attributeType.getCode(), count, 
              Arrays.binarySearch(selectedOpts, attributeType.getCode()) > -1);
        
        this.attributeType = attributeType;
    }

    /**
     * Returns the predicate represented by this option that may be applied to a
     * query.
     */
    public Predicate getPredicate() {
        return Predicate.eq("attribute.typeCode", attributeType.getCode()).and(
               new Predicate("(length(trim(recordAttribute.stringValue)) > 0)"));
    }
}
