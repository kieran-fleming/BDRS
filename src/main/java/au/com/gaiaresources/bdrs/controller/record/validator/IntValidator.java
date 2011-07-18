package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validates that the input is a parseable integer value.
 */
public class IntValidator extends AbstractValidator {

    private static final String NUMBER_MESSAGE_KEY = "IntValidator.number";
    private static final String NUMBER_MESSAGE = "Must be a number.";
    
    private static final String NUMBER_OR_BLANK_MESSAGE_KEY = "IntValidator.numberOrBlank";
    private static final String NUMBER_OR_BLANK_MESSAGE = "Must be a number or blank.";
    
    /**
    * Creates a new <code>IntValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    */
    public IntValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        super(propertyService, required, blank);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key, Attribute attribute, Map<String, String> errorMap) {

        boolean isValid = super.validate(parameterMap, key, attribute, errorMap);
        if (isValid) {
            String value = getSingleParameter(parameterMap, key);
            if (value != null && !value.isEmpty()) {
                try {
                    int val = Integer.parseInt(value, 10);
                    // Otherwise it is valid
                } catch (NumberFormatException nfe) {
                    if(blank) {
                        errorMap.put(key, propertyService.getMessage(NUMBER_OR_BLANK_MESSAGE_KEY, NUMBER_OR_BLANK_MESSAGE));
                    } else {
                        errorMap.put(key, propertyService.getMessage(NUMBER_MESSAGE_KEY, NUMBER_MESSAGE));
                    }
                }
            }
        }

        return isValid && !errorMap.containsKey(key);
    }
}
