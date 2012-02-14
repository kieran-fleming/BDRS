package au.com.gaiaresources.bdrs.json;
/*
Copyright (c) 2002 JSON.org

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

The Software shall be used for Good, not Evil.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
*/
import java.util.List;
import java.util.Map;

import org.json.simple.JSONValue;

/**
 * A JSONObject is an unordered collection of name/value pairs. Its
 * external form is a string wrapped in curly braces with colons between the
 * names and values, and commas between the values and names.
 */
@SuppressWarnings({ "unchecked", "rawtypes" })
public class JSONObject extends org.json.simple.JSONObject implements JSON {

    private static final long serialVersionUID = -5266474387127029063L;
    private static final String EXCEPTION_MESSAGE_TMPL = "JSONObject[%s] %s.";

    @Override
    public boolean isArray() {
        return false;
    }
    
    /**
     * Determine if the value associated with the key is null or if there is no
     * value.
     * 
     * @param key
     *            A key string.
     * @return true if there is no value associated with the key or if the value
     *         is the JSONObject.NULL object.
     */
    public boolean isNull(String key) {
        // The key is present but the value is null.
        return super.containsKey(key) && super.get(key) == null;
    }

    /**
     * Accumulates values under a key. If an existing value is found, a
     * {@link List}ist shall be created to hold all the accumulated values.
     * 
     * @param key
     *            a key string
     * @param value
     *            An object to be accumulated under the key.
     * @return this
     */
    public JSONObject accumulate(String key, Object value) {
        if (super.containsKey(key)) {
            // Accumulate
            List list;
            Object current = super.get(key);
            if (current instanceof List) {
                list = ((JSONArray) current);
            } else {
                list = new JSONArray();
                list.add(current);
            }
            list.add(value);
            super.put(key, list);
        } else {
            // Add
            super.put(key, value);
        }

        return this;
    }

    /**
     * Accumulate all values in the specified map.
     * 
     * @param map
     *            the map to be merged with this instance.
     */
    public void accumulateAll(Map<?, ?> map) {
        for (Map.Entry<?, ?> entry : map.entrySet()) {
            this.accumulate(entry.getKey().toString(), entry.getValue());
        }
    }

    /**
     * Parses a JSON formatted string into a {@link JSONObject}. 
     * @param jsonFormattedString a JSON formatted String.
     * @return the deserialized representation of the JSON string.
     */
    public static JSONObject fromStringToJSONObject(String jsonFormattedString) {
        JSON json = JSONSerializer.toJSON(jsonFormattedString);
        if (json.isArray()) {
            throw new JSONException(
                    String.format("Formatted string is not a JSON object: %s", jsonFormattedString));
        } else {
            return (JSONObject) json;
        }
    }

    /**
     * Converts the specified {@link Map} into a JSON formatted string.
     * @param map the map to be serialized into a JSON formatted string.
     * @return a JSON formatted string representation.
     */
    public static String fromMapToString(Map<?, ?> map) {
        return JSONValue.toJSONString(map);
    }

    /**
     * Converts the specified {@link Map} into a {@link JSONObject}.
     * @param map the map to be represented as a {@link JSONObject}.
     * @return a {@link JSONObject} representation of the specified map.
     */
    public static JSONObject fromMapToJSONObject(Map<?, ?> map) {
        JSONObject obj = new JSONObject();
        obj.accumulateAll(map);
        return obj;
    }

    /**
     * Get the value object associated with a key.
     * 
     * @param key
     *            A key string.
     * @return The object associated with the key.
     * @throws JSONException
     *             if the key is not found.
     */
    public Object get(String key) throws JSONException {
        if (key == null) {
            throw new JSONException("Null key.");
        }
        Object object = super.get(key);
        if (object == null) {
            throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "not found"));
        }
        return object;
    }

    /**
     * Determine if the JSONObject contains a specific key.
     * 
     * @param key
     *            A key string.
     * @return true if the key exists in the JSONObject.
     */
    public boolean has(String key) {
        return super.containsKey(key);
    }

    /**
     * Get the boolean value associated with a key.
     * 
     * @param key
     *            A key string.
     * @return The truth.
     * @throws JSONException
     *             if the value is not a Boolean or the String "true" or
     *             "false".
     */
    public boolean getBoolean(String key) throws JSONException {
        Object object = this.get(key);
        if (object.equals(Boolean.FALSE)
                || (object instanceof String && ((String) object).equalsIgnoreCase("false"))) {
            return false;
        } else if (object.equals(Boolean.TRUE)
                || (object instanceof String && ((String) object).equalsIgnoreCase("true"))) {
            return true;
        }
        throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not a Boolean"));
    }

    /**
     * Get the double value associated with a key.
     * 
     * @param key
     *            A key string.
     * @return The numeric value.
     * @throws JSONException
     *             if the key is not found or if the value is not a Number
     *             object and cannot be converted to a number.
     */
    public double getDouble(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).doubleValue()
                    : Double.parseDouble((String) object);
        } catch (Exception e) {
            
            throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not a number"), e);
        }
    }

    /**
     * Get the int value associated with a key.
     * 
     * @param key
     *            A key string.
     * @return The integer value.
     * @throws JSONException
     *             if the key is not found or if the value cannot be converted
     *             to an integer.
     */
    public int getInt(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).intValue()
                    : Integer.parseInt((String) object);
        } catch (Exception e) {
            
            throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not an int"), e);
        }
    }

    /**
     * Get the long value associated with a key.
     * 
     * @param key
     *            A key string.
     * @return The long value.
     * @throws JSONException
     *             if the key is not found or if the value cannot be converted
     *             to a long.
     */
    public long getLong(String key) throws JSONException {
        Object object = this.get(key);
        try {
            return object instanceof Number ? ((Number) object).longValue()
                    : Long.parseLong((String) object);
        } catch (Exception e) {
            throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not a long"), e);
        }
    }
    
    /**
     * Get the string associated with a key.
     *
     * @param key   A key string.
     * @return      A string which is the value.
     * @throws   JSONException if there is no string value for the key.
     */
    public String getString(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof String) {
            return (String)object;
        }
        throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not a string"));
    }

    /**
     * Get the JSONArray value associated with a key.
     * 
     * @param key
     *            A key string.
     * @return A JSONArray which is the value.
     * @throws JSONException
     *             if the key is not found or if the value is not a JSONArray.
     */
    public JSONArray getJSONArray(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONArray) {
            return (JSONArray) object;
        }
        throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not a JSONArray"));
    }

    /**
     * Get the JSONObject value associated with a key.
     * 
     * @param key
     *            A key string.
     * @return A JSONObject which is the value.
     * @throws JSONException
     *             if the key is not found or if the value is not a JSONObject.
     */
    public JSONObject getJSONObject(String key) throws JSONException {
        Object object = this.get(key);
        if (object instanceof JSONObject) {
            return (JSONObject) object;
        }
        throw new JSONException(String.format(EXCEPTION_MESSAGE_TMPL, quote(key), "is not a JSONObject"));
    }

    /**
     * Get an optional boolean associated with a key. It returns false if there
     * is no such key, or if the value is not Boolean.TRUE or the String "true".
     * 
     * @param key
     *            A key string.
     * @return The truth.
     */
    public boolean optBoolean(String key) {
        return this.optBoolean(key, false);
    }

    /**
     * Get an optional boolean associated with a key. It returns the
     * defaultValue if there is no such key, or if it is not a Boolean or the
     * String "true" or "false" (case insensitive).
     * 
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return The truth.
     */
    public boolean optBoolean(String key, boolean defaultValue) {
        try {
            return this.getBoolean(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional double associated with a key, or NaN if there is no such
     * key or if its value is not a number. If the value is a string, an attempt
     * will be made to evaluate it as a number.
     * 
     * @param key
     *            A string which is the key.
     * @return An object which is the value.
     */
    public double optDouble(String key) {
        return this.optDouble(key, Double.NaN);
    }

    /**
     * Get an optional double associated with a key, or the defaultValue if
     * there is no such key or if its value is not a number. If the value is a
     * string, an attempt will be made to evaluate it as a number.
     * 
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public double optDouble(String key, double defaultValue) {
        try {
            return this.getDouble(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional int value associated with a key, or zero if there is no
     * such key or if the value is not a number. If the value is a string, an
     * attempt will be made to evaluate it as a number.
     * 
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public int optInt(String key) {
        return this.optInt(key, 0);
    }

    /**
     * Get an optional int value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number.
     * 
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public int optInt(String key, int defaultValue) {
        try {
            return this.getInt(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional JSONArray associated with a key. It returns null if there
     * is no such key, or if its value is not a JSONArray.
     * 
     * @param key
     *            A key string.
     * @return A JSONArray which is the value.
     */
    public JSONArray optJSONArray(String key) {
        Object o = super.get(key);
        return o instanceof JSONArray ? (JSONArray) o : null;
    }

    /**
     * Get an optional JSONObject associated with a key. It returns null if
     * there is no such key, or if its value is not a JSONObject.
     * 
     * @param key
     *            A key string.
     * @return A JSONObject which is the value.
     */
    public JSONObject optJSONObject(String key) {
        Object object = super.get(key);
        return object instanceof JSONObject ? (JSONObject) object : null;
    }

    /**
     * Get an optional long value associated with a key, or zero if there is no
     * such key or if the value is not a number. If the value is a string, an
     * attempt will be made to evaluate it as a number.
     * 
     * @param key
     *            A key string.
     * @return An object which is the value.
     */
    public long optLong(String key) {
        return this.optLong(key, 0);
    }

    /**
     * Get an optional long value associated with a key, or the default if there
     * is no such key or if the value is not a number. If the value is a string,
     * an attempt will be made to evaluate it as a number.
     * 
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return An object which is the value.
     */
    public long optLong(String key, long defaultValue) {
        try {
            return this.getLong(key);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    /**
     * Get an optional string associated with a key. It returns an empty string
     * if there is no such key. If the value is not a string and is not null,
     * then it is converted to a string.
     * 
     * @param key
     *            A key string.
     * @return A string which is the value.
     */
    public String optString(String key) {
        return this.optString(key, "");
    }

    /**
     * Get an optional string associated with a key. It returns the defaultValue
     * if there is no such key. If the value is not a string and is not null,
     * then it is converted to a string.
     * 
     * @param key
     *            A key string.
     * @param defaultValue
     *            The default.
     * @return A string which is the value.
     */
    public String optString(String key, String defaultValue) {
        Object object = super.get(key);
        return object == null ? defaultValue : object.toString();
    }

    /**
     * Produce a string in double quotes with backslash sequences in all the
     * right places. A backslash will be inserted within </, producing <\/,
     * allowing JSON text to be delivered in HTML. In JSON text, a string cannot
     * contain a control character or an unescaped quote or backslash.
     * 
     * @param string
     *            A String
     * @return A String correctly formatted for insertion in a JSON text.
     */
    private String quote(String string) {
        if (string == null || string.length() == 0) {
            return "\"\"";
        }

        char b;
        char c = 0;
        String hhhh;
        int i;
        int len = string.length();
        StringBuffer sb = new StringBuffer(len + 4);

        sb.append('"');
        for (i = 0; i < len; i += 1) {
            b = c;
            c = string.charAt(i);
            switch (c) {
            case '\\':
            case '"':
                sb.append('\\');
                sb.append(c);
                break;
            case '/':
                if (b == '<') {
                    sb.append('\\');
                }
                sb.append(c);
                break;
            case '\b':
                sb.append("\\b");
                break;
            case '\t':
                sb.append("\\t");
                break;
            case '\n':
                sb.append("\\n");
                break;
            case '\f':
                sb.append("\\f");
                break;
            case '\r':
                sb.append("\\r");
                break;
            default:
                if (c < ' ' || (c >= '\u0080' && c < '\u00a0')
                        || (c >= '\u2000' && c < '\u2100')) {
                    hhhh = "000" + Integer.toHexString(c);
                    sb.append("\\u" + hhhh.substring(hhhh.length() - 4));
                } else {
                    sb.append(c);
                }
            }
        }
        sb.append('"');
        return sb.toString();
    }
}
