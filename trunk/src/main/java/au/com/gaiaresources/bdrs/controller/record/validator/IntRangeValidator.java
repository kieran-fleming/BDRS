package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;

import org.w3c.dom.ls.LSException;

import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validates that the input is a parseable integer value and if specified, 
 * is between the min and max range.
 */
public class IntRangeValidator extends IntValidator {

    private static final String RANGE_MESSAGE_KEY = "IntRangeValidator.range";
    private static final String RANGE_MESSAGE = "Must be a number between %d and %d.";

    private static final String RANGE_OR_BLANK_MESSAGE_KEY = "IntRangeValidator.rangeOrBlank";
    private static final String RANGE_OR_BLANK_MESSAGE = "Must be a number between %d and %d or blank.";
    
    private static final String POSITIVE_MESSAGE_KEY = "PositiveIntValidator.range";
    private static final String POSITIVE_MESSAGE = "Must be a positive number.";
    
    private static final String POSITIVE_OR_BLANK_MESSAGE_KEY = "PositiveIntValidator.rangeOrBlank";
    private static final String POSITIVE_OR_BLANK_MESSAGE = "Must be a positive number or blank.";
    
    private static final String POSITIVE_LESSTHAN_MESSAGE_KEY = "PositiveIntLessThanValidator.range";
    private static final String POSITIVE_LESSTHAN_MESSAGE = "Must be a positive number (less than %d).";
    
    private static final String POSITIVE_LESSTHAN_OR_BLANK_MESSAGE_KEY = "PositiveIntLessThanValidator.rangeOrBlank";
    private static final String POSITIVE_LESSTHAN_OR_BLANK_MESSAGE = "Must be a positive number (less than %d) or blank.";
    
    private int min = Integer.MIN_VALUE;
    private int max = Integer.MAX_VALUE;
    
    private boolean positive = false;
    private boolean lessThan = false;

    /**
    * Creates a new <code>IntRangeValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    * @param min the lowest valid value.
    * @param max the largest valid value.
    */
    public IntRangeValidator(PropertyService propertyService, boolean required,
            boolean blank, int min, int max) {
        
        super(propertyService, required, blank);
        this.min = min;
        this.max = max;
    }
    
    /**
    * Creates a new <code>IntRangeValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    */
    public IntRangeValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        
        super(propertyService, required, blank);
        this.min = 0;
        this.max = Integer.MAX_VALUE;
        
        this.positive = true;
    }

    
    public IntRangeValidator(PropertyService propertyService, boolean required,
            boolean blank, int max) {
        super(propertyService, required, blank);
        this.min = 0;
        this.max = max;
        this.positive = true;
        this.lessThan = true;
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
                
                int val = Integer.parseInt(value, 10);
                if(val < min || val > max) {
                    
                    if(positive) {
                        if(lessThan) {
                            if(blank) {
                                errorMap.put(key, String.format(propertyService.getMessage(POSITIVE_LESSTHAN_OR_BLANK_MESSAGE_KEY, POSITIVE_LESSTHAN_OR_BLANK_MESSAGE), max+1));
                            } else {
                                errorMap.put(key, String.format(propertyService.getMessage(POSITIVE_LESSTHAN_MESSAGE_KEY, POSITIVE_LESSTHAN_MESSAGE), max+1));
                            }
                        } else {
                            if(blank) {
                                errorMap.put(key, String.format(propertyService.getMessage(POSITIVE_OR_BLANK_MESSAGE_KEY, POSITIVE_OR_BLANK_MESSAGE), min, max));
                            } else {
                                errorMap.put(key, String.format(propertyService.getMessage(POSITIVE_MESSAGE_KEY, POSITIVE_MESSAGE), min, max));
                            }
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
