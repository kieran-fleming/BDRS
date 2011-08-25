package au.com.gaiaresources.bdrs.model.taxa;

import java.io.Serializable;
import java.util.Comparator;


/**
 * Comparator implementation to help order AttributeValues in AttributeValueUtil
 * @author aaron
 *
 */
public class AttributeValueComparator implements Comparator<AttributeValue>, Serializable {

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(AttributeValue arg0, AttributeValue arg1) {
        if (arg0 == null) {
            return 1;
        }
        if (arg1 == null) {
            return 1;
        }
        if (arg0.getAttribute() == null) {
            return 1;
        }
        if (arg1.getAttribute() == null) {
            return 1;
        }
        return arg0.getAttribute().getWeight() - arg1.getAttribute().getWeight(); 
    }
}
