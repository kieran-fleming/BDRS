package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;


public class DynamicIntRangeValidator extends IntValidator {
	
	Logger log = Logger.getLogger(getClass());

    private static final String RANGE_MESSAGE_KEY = "IntRangeValidator.range";
    private static final String RANGE_MESSAGE = "Must be a number between %d and %d.";

    private static final String RANGE_OR_BLANK_MESSAGE_KEY = "IntRangeValidator.rangeOrBlank";
    private static final String RANGE_OR_BLANK_MESSAGE = "Must be a number between %d and %d or blank.";
 
    
    /**
    * Creates a new <code>IntRangeValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    */
    public DynamicIntRangeValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        super(propertyService, required, blank);

    }
    
    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key, Attribute attribute, Map<String, String> errorMap){
        boolean isValid = super.validate(parameterMap, key, attribute, errorMap);
        
        
        Integer min = Integer.parseInt(attribute.getOptions().get(0).getValue());
    	Integer max = Integer.parseInt(attribute.getOptions().get(1).getValue());
        
    	if (isValid) {
            
            String value = getSingleParameter(parameterMap, key);
            if (value != null && !value.isEmpty()) {
                
                int val = Integer.parseInt(value, 10);
                if(val < min || val > max) {
                        if(blank) {
                            errorMap.put(key, String.format(propertyService.getMessage(RANGE_OR_BLANK_MESSAGE_KEY, RANGE_OR_BLANK_MESSAGE), min, max));
                        } else {
                            errorMap.put(key, String.format(propertyService.getMessage(RANGE_MESSAGE_KEY, RANGE_MESSAGE), min, max));
                        }
                }
                // Otherwise it is valid
            }
        }

        return isValid && !errorMap.containsKey(key);
    }

}

