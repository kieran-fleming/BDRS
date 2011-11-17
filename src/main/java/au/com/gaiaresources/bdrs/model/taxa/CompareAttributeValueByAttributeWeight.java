package au.com.gaiaresources.bdrs.model.taxa;

import java.io.Serializable;
import java.util.Comparator;

/**
 * A comparator for sorting {@link AttributeValue}s according to the weight of 
 * their associated {@link Attribute} 
 */
public class CompareAttributeValueByAttributeWeight implements Comparator<AttributeValue>, Serializable {

    private static final long serialVersionUID = 1L;

    @Override
    public int compare(AttributeValue o1, AttributeValue o2) {
        // The attribute on attribute value is not nullable.
        return o1.getAttribute().getWeight() - o2.getAttribute().getWeight();
    }
}
