package au.com.gaiaresources.bdrs.controller.record.validator;

import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validates that the input exists (if required) and not blank (if specified).
 */
public class StringValidator extends AbstractValidator {
    /**
     * Creates a new <code>StringValidator</code>.
     * 
     * @param propertyService
     *            used to access configurable messages displayed to the user.
     * @param required
     *            true if the input is mandatory, false otherwise.
     * @param blank
     *            true if the value can be an empty string, false otherwise.
     */
    public StringValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        super(propertyService, required, blank);
    }
}
