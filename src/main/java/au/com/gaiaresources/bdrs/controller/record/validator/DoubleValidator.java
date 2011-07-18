package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validates that the input is a parseable decimal value.
 */
public class DoubleValidator extends AbstractValidator {

    private static final String NUMBER_MESSAGE_KEY = "DoubleValidator.number";
    private static final String NUMBER_MESSAGE = "Must be a decimal.";
    
    private static final String NUMBER_OR_BLANK_MESSAGE_KEY = "DoubleValidator.numberOrBlank";
    private static final String NUMBER_OR_BLANK_MESSAGE = "Must be a number or blank.";
    
    /**
    * Creates a new <code>DoubleValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    */
    public DoubleValidator(PropertyService propertyService, boolean required,
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
                    double val = Double.parseDouble(value);
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
