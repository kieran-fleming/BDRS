package au.com.gaiaresources.bdrs.controller.report.python.model;

import java.util.Collection;

import au.com.gaiaresources.bdrs.json.JSON;
import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

/**
 * A collection of utility methods that are generally useful for simplifying
 * communications between the Java/Python bridge.
 */
public class PyDAOUtil {
    private static Logger log = Logger.getLogger(PyDAOUtil.class);
    
    /**
     * An empty JSON object - that is, '{}'.
     */
    public static final String EMPTY_JSON_OBJECT_STR = new JSONObject().toString();
    /**
     * An empty JSON array - that is, '[]'.
     */
    public static final String EMPTY_JSON_ARRAY_STR = new JSONArray().toString();
    
    /**
     * Utility classes cannot be instantiated.
     */
    protected PyDAOUtil() {
        throw new UnsupportedOperationException();
    }
    
    /**
     * JSON serializes a collection of {@link PersistentImpl} instances.
     * 
     * @param list the collection of {@link PersistentImpl} instances to be
     * serialized. Any null values contained in this list will be ommited. 
     * @return a JSON serialized array of {@link PersistentImpl} instances.
     */
    public static <T extends PersistentImpl> JSON toJSON(Collection<T> list) {
        JSONArray array = new JSONArray();
        if(list != null) {
            for(T item : list) {
                if(item != null) {
                    array.add(item.flatten());
                }
            }
        }
        return array;
    }

    /**
     * JSON serializes a single {@link PersistentImpl} to a {@link JSONObject}.
     * 
     * @param obj the item to be serialized. 
     * @return a {@link JSONObject} representing the specified item.
     */
    public static <T extends PersistentImpl> JSON toJSON(T obj) {
        if(obj == null) {
            return new JSONObject();
        } else {
            return JSONObject.fromMapToJSONObject(obj.flatten());
        }
    }
    
}
