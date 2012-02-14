package au.com.gaiaresources.bdrs.json;

import java.io.IOException;
import java.io.Writer;

/**
 * A utility class to facilitate the serialization of enumerated types to 
 * valid JSON.
 */
public final class JSONEnumUtil {
    
    /**
     * Utility classes should not be instantiated.
     */
    private JSONEnumUtil() {
    }

    /**
     * Serializes the specified enum to a JSON formatted String. 
     * @param e the enum to be serialized.
     * @return a JSON formatted representation of the enum.
     */
    public static String toJSONString(Enum<?> e) {
        
        return String.format("\"%s\"", e.toString());
    }
    
    /**
     * Serializes the specified enum to JSON writing it out to the specified
     * writer.
     * @param out the writer to output the serialized enum.
     * @param e the enum to be serialized.
     * @throws IOException thrown if there has been an error outputing the 
     * formatted enum to the writer.
     */
    public static void writeJSONString(Writer out, Enum<?> e) throws IOException {
        out.write(JSONEnumUtil.toJSONString(e));
    }
}
