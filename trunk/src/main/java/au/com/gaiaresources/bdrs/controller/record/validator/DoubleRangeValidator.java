package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;

import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validates that the input is a parseable decimal value and if specified, 
 * is between the min and max range.
 */
public class DoubleRangeValidator extends DoubleValidator {

    private static final String RANGE_MESSAGE_KEY = "DoubleRangeValidator.range";
    private static final String RANGE_MESSAGE = "Must be a number between %f and %f.";

    private static final String RANGE_OR_BLANK_MESSAGE_KEY = "DoubleRangeValidator.rangeOrBlank";
    private static final String RANGE_OR_BLANK_MESSAGE = "Must be a number between %f and %f or blank.";
    
    private static final String POSITIVE_MESSAGE_KEY = "PositiveDoubleValidator.range";
    private static final String POSITIVE_MESSAGE = "Must be a positive number.";
    
    private static final String POSITIVE_OR_BLANK_MESSAGE_KEY = "PositiveDoubleValidator.rangeOrBlank";
    private static final String POSITIVE_OR_BLANK_MESSAGE = "Must be a positive number or blank.";
    
    private double min = Double.MIN_VALUE;
    private double max = Double.MAX_VALUE;
    
    private boolean positive = false;

    /**
     * Creates a new <code>DoubleRangeValidator</code>.
     * 
     * @param propertyService used to access configurable messages displayed to the user.
     * @param required true if the input is mandatory, false otherwise.
     * @param blank true if the value can be an empty string, false otherwise.
     * @param min the smallest valid value.
     * @param max the largest valid value.
     */
    public DoubleRangeValidator(PropertyService propertyService, boolean required,
            boolean blank, double min, double max) {
        
        super(propertyService, required, blank);
        this.min = min;
        this.max = max;
    }
    
    /**
     * Creates a new <code>DoubleRangeValidator</code>.
     * 
     * @param propertyService used to access configurable messages displayed to the user.
     * @param required true if the input is mandatory, false otherwise.
     * @param blank true if the value can be an empty string, false otherwise.
     */
    public DoubleRangeValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        
        super(propertyService, required, blank);
        this.min = 0;
        this.max = Double.MAX_VALUE;
        
        this.positive = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key, Map<String, String> errorMap) {

        boolean isValid = super.validate(parameterMap, key, errorMap);
        if (isValid) {
            
            String value = getSingleParameter(parameterMap, key);
            if (value != null && !value.isEmpty()) {
                
                double val = Double.parseDouble(value);
                if(val < min || val > max) {
                    
                    if(positive) {
                        if(blank) {
                            errorMap.put(key, String.format(propertyService.getMessage(POSITIVE_OR_BLANK_MESSAGE_KEY, POSITIVE_OR_BLANK_MESSAGE), min, max));
                        } else {
                            errorMap.put(key, String.format(propertyService.getMessage(POSITIVE_MESSAGE_KEY, POSITIVE_MESSAGE), min, max));
                        }
                    } else {
                        if(blank) {
                            errorMap.put(key, String.format(propertyService.getMessage(RANGE_OR_BLANK_MESSAGE_KEY, RANGE_OR_BLANK_MESSAGE), min, max));
                        } else {
                            errorMap.put(key, String.format(propertyService.getMessage(RANGE_MESSAGE_KEY, RANGE_MESSAGE), min, max));
                        }
                    }
                }
                // Otherwise it is valid
            }
        }
        return isValid && !errorMap.containsKey(key);
    }
}
