package au.com.gaiaresources.bdrs.controller.review.sightings.facet;


import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * The </code>MultimediaFacetOption</code> represents a type of multimedia -
 * Image or File.
 */
public class MultimediaFacetOption extends FacetOption {
    
    private AttributeType attributeType;

    public MultimediaFacetOption(AttributeType attributeType, Long count, String[] selectedOpts) {
        super(attributeType.getName(), attributeType.getCode(), count, 
              Arrays.binarySearch(selectedOpts, attributeType.getCode()) > -1);
        
        this.attributeType = attributeType;
    }

    public Predicate getPredicate() {
        return Predicate.eq("attribute.typeCode", attributeType.getCode()).and(
               new Predicate("(length(trim(recordAttribute.stringValue)) > 0)"));
    }
}
