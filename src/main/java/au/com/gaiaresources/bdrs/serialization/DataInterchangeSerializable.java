package au.com.gaiaresources.bdrs.serialization;

import java.util.Map;

public interface DataInterchangeSerializable {

    public Map<String, Object> flatten();
    public Map<String, Object> flatten(int depth);
    
    /**
     * Flattens out an object to a map.
     * @param compact whether to include fields annotated with @CompactAttribute
     * @param mobileFields whether to rename fields according to @MobileField
     * @return a flattened map of the attributes to their values.
     */
    public Map<String, Object> flatten(boolean compact, boolean mobileFields);
    
    /**
     * Flattens out an object to a map.
     * @param compact whether to include fields annotated with @CompactAttribute
     * @param mobileFields whether to rename fields according to @MobileField
     * @param depth the depth to flatten included collections to.
     * @return a flattened map of the attributes to their values.
     */
    public Map<String, Object> flatten(int depth, boolean compact, boolean mobileFields);
    
}
