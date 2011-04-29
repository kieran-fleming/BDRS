package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;

import au.com.gaiaresources.bdrs.controller.record.RecordFormValidator;
import au.com.gaiaresources.bdrs.controller.record.ValidationType;

/**
 * Implemented by validators to provide a unified interface for the
 * {@link RecordFormValidator} to apply validations to POST data.
 */
public interface Validator {

    /**
     * Validates the first value in the array for the specified <code>key</code>.  
     * 
     * @param parameterMap the map of all POST parameters.
     * @param key the name of the input.
     * @param errorMap the map of error messages where validation errors shall
     * be put.
     * @return true if the input value is valid, false otherwise.
     * 
     * @see RecordFormValidator#validate(Map, ValidationType, String)
     */
    boolean validate(Map<String, String[]> parameterMap,
            String key, Map<String, String> errorMap);

}
