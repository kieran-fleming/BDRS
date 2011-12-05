package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Map;
import java.util.regex.Pattern;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validator for time fields.
 *
 */
public class TimeValidator extends AbstractValidator {
    
    public static final String TIME_INVALID_KEY = "TimeValidator.time";
    
    public static final String MESSAGE_MISSING = "TimeValidator.time message is missing";
    
    private final Pattern timePattern = Pattern.compile("([012]?\\d:\\d{2})?");

    /**
     * Construct a new TimeValidator
     * 
     * @param propertyService property service for retrieving messages
     * @param required is the field required
     * @param blank can the field be blank
     */
    public TimeValidator(PropertyService propertyService, boolean required,
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
                if (!timePattern.matcher(value).matches()) {
                    String msg = propertyService.getMessage(TIME_INVALID_KEY, MESSAGE_MISSING);
                    errorMap.put(key, msg);
                }
            }
            // else is valid since blank is allowed
        }
        return isValid && !errorMap.containsKey(key);
    }
}
