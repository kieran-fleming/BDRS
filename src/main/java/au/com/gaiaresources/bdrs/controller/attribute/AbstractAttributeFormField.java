package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;
import org.apache.log4j.Logger;


/**
 * Provides the common {@link Comparable} implementation that is common to all
 * <code>AttributeFormField</code> implementations.
 */
public abstract class AbstractAttributeFormField implements AttributeFormField {

    private Logger log = Logger.getLogger(getClass());

    /**
     * Compares the weight of this <code>AttributeFormField</code> to the other.
     */
    @Override
    public int compareTo(AttributeFormField other) {
        return new Integer(this.getWeight()).compareTo(other.getWeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttributeField() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPropertyField() {
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWeightName(String weightName) {
        throw new NotImplementedException();
    }

    /**
     * Utility method to retrieve the first parameter for the specified key from
     * from the parameter map.
     * @param paramMap
     * @param key
     * @return
     */
    protected String getParameter(Map<String, String[]> paramMap, String key) {
        String[] value = paramMap.get(key);
        if (value == null) {
            return null;
        } else if (value.length == 1) {
            return value[0];
        } else {
            log.warn(String.format("There are multiple parameters for the key \"%s\". Returning the first.", key));
            return value[0];
        }
    }
}
