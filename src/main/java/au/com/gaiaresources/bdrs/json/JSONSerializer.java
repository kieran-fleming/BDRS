package au.com.gaiaresources.bdrs.json;

import java.util.Collection;

import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

/**
 * Transforms java objects into JSON and back.
 */
public final class JSONSerializer {
    
    private static JSONParser parser = new JSONParser();
    private static ContainerFactory containerFactory = new ContainerFactory();
    
    /**
     * Utility classes should not be instantiated.
     */
    private JSONSerializer() {
    }
    
    /**
     * Creates a {@link JSONObject} or {@link JSONArray} from a JSON formatted
     * string.
     * 
     * @param jsonFormattedString
     *            a JSON formatted string
     * @return a {@link JSONObject} or {@link JSONArray}.
     */
    public static JSON toJSON(String jsonFormattedString) {
        try {
            Object obj = parser.parse(jsonFormattedString, containerFactory);
            if(obj instanceof JSON) {
                return (JSON)obj;
            } else {
                throw new JSONException(
                        "Content must be a JSON Object or JSON Array.");
            }
        } catch (ParseException pe) {
            throw new JSONException(pe);
        }
    }
    
    /**
     * JSON serializes a collection of {@link PersistentImpl} instances.
     * 
     * @param list the collection of {@link PersistentImpl} instances to be
     * serialized. Any null values contained in this list will be ommited. 
     * @return a JSON serialized array of {@link PersistentImpl} instances.
     */
    @SuppressWarnings("unchecked")
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
}
