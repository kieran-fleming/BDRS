package au.com.gaiaresources.bdrs.model.taxa;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class AttributeValueUtil {

    /**
     * Orders a set of attribute values by the weightings of the attributes contained within 
     * the attribute values. Returns a list because it implies the AttributeValues are now ordered.
     * 
     * @param avSet
     * @return
     */
    @SuppressWarnings("unchecked")
    public static List<AttributeValue> orderAttributeValues(Set<AttributeValue> avSet) {
        if (avSet == null) {
            return Collections.EMPTY_LIST;
        }
        AttributeValue[] avArray = avSet.toArray(new AttributeValue[0]);
        Arrays.sort(avArray, new AttributeValueComparator());
        return Arrays.asList(avArray);
    }
}
