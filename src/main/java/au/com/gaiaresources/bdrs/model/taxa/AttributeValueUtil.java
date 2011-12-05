package au.com.gaiaresources.bdrs.model.taxa;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import au.com.gaiaresources.bdrs.model.record.Record;

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
    
    /**
     * Returns the AttributeValue with the corresponding Attribute from the 
     * attribute values contained inside the record parameter.
     * 
     * @param a - attribute to look for
     * @param r - record to look for the attribute value set
     * @return AttributeValue if found, otherwise null
     */
    public static AttributeValue getAttributeValue(Attribute a, Record r) {
        return getByAttribute(r.getAttributes(), a);
    }
    
    /**
     * Gets an attribute value from the set that contains the passed attribute
     * 
     * @param avSet - Set<Attribute> the set to search
     * @param a - the Attribute to search for
     * @return AttributeValue if found, else null
     */
    public static AttributeValue getByAttribute(Set<AttributeValue> avSet, Attribute a) {
        
        if (avSet == null) {
            throw new IllegalArgumentException("List cannot be null");
        }
        if (a == null) {
            throw new IllegalArgumentException("Attribute cannot be null");
        }
        
        for (AttributeValue av : avSet) {
            if (a.equals(av.getAttribute())) {
                return av;
            }
        }
        return null;
    }
    
    /**
     * Compares fields of 2 AttributeValues. Returns true if the 2 objects are deemed
     * to have an equal value
     * 
     * @param a - first AttributeValue to compare
     * @param b - second AttributeValue to compare
     * @return
     */
    public static boolean isAttributeValuesEqual(AttributeValue a, AttributeValue b) {
        // these should already be checked by the client
        if (a == null) {
            throw new IllegalArgumentException("AttributeValue, a, cannot be null");
        }
        if (b == null) {
            throw new IllegalArgumentException("AttributeValue, b, cannot be null");
        }
        
        // compare string values ....
        String strA = a.getStringValue();
        String strB = b.getStringValue();
        if (strA != null && strB == null) {
            return false;
        }
        if (strA == null && strB != null) {
            return false;
        } 
        if ((strA != null && strB != null) && 
                !strA.equals(strB)) {
            return false;
        }
        // comapre number values ....
        BigDecimal numA = a.getNumericValue();
        BigDecimal numB = b.getNumericValue();;
        if (numA != null && numB == null) {
            return false;
        }
        if (numA == null && numB != null) {
            return false;
        }
        // i hope there are no rounding problems here ...
        if ((numA != null && numB != null) && 
                (numA.doubleValue() != numB.doubleValue())) {
            return false;
        }
        // compare boolean values ....
        Boolean boolA = a.getBooleanValue();
        Boolean boolB = b.getBooleanValue();
        if (boolA != null && boolB == null) {
            return false;
        }
        if (boolA == null && boolB != null) {
            return false;
        }
        if ((boolA != null && boolB != null) &&
                (boolA.booleanValue() != boolB.booleanValue())) {
            return false;
        }
        
        // the attribute value has been deemed to have an equal value.
        return true;
    }
}
