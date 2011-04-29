package au.com.gaiaresources.bdrs.service.property;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import javax.annotation.PostConstruct;

import org.springframework.stereotype.Service;

/**
 * This service reads key value pairs from a properties file and provides a one
 * stop shop for retrieving these values.
 */
@Service
public class PropertyService {

    public static final String MESSAGE = "messages.properties";
    public static final String BULKDATA = "bulkdata.properties";

    private Map<String, Properties> propertyMap;

    @PostConstruct
    public void init() throws Exception {

        propertyMap = new HashMap<String, Properties>();
        Properties properties;
        for(String propertyName : new String[]{MESSAGE, BULKDATA}) {
            properties = new Properties();
            properties.load(getClass().getResourceAsStream(propertyName));
            propertyMap.put(propertyName, properties);
        }
    }

    /**
     * Returns the value from message properties with the specified key.
     * 
     * @param key
     *            the key of the message.
     * @return the value specified by the key or null if the message cannot be
     *         found.
     */
    public String getMessage(String key) {
        return getPropertyValue(MESSAGE, key, null);
    }

    /**
     * Returns the value from message properties with the specified key.
     * 
     * @param key
     *            the key of the message.
     * @param defaultValue
     *            the value that shall be returned if the message cannot be
     *            found.
     * @return the value specified by the key or the defaultValue if the message
     *         cannot be found.
     */
    public String getMessage(String key, String defaultValue) {
        return getPropertyValue(MESSAGE, key, defaultValue);
    }

    /**
     * Return the value from the properties dictionary with the specified name.
     * 
     * @param propertyName
     *            the name of the properties dictionary.
     * @param key
     *            the key of the value to be retrieved.
     * @param defaultValue
     *            the default value that shall be returned if the key cannot be
     *            found.
     * @return the value specified by the key in the named property dictionary.
     */
    public String getPropertyValue(String propertyName, String key,
            String defaultValue) {
        String value = defaultValue;
        Properties p = propertyMap.get(propertyName);
        if (p != null) {
            value = p.getProperty(key, defaultValue);
        }

        return value;
    }
}
