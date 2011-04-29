package au.com.gaiaresources.bdrs.controller.record;

import java.util.HashMap;
import java.util.Map;

import au.com.gaiaresources.bdrs.controller.record.validator.DateValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.DoubleRangeValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.DoubleValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.HistoricalDateValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.IntRangeValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.IntValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.StringValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.TaxonValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.Validator;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * The <code>RecordFormValidator</code> provides a single access point to 
 * validate POST parameters.
 */
public class RecordFormValidator {

    private Map<ValidationType, Validator> validatorMap;
    
    private Map<String, String> errorMap;

    private PropertyService propertyService;

    private TaxaDAO taxaDAO;
    
    /**
     * Creates a new <code>RecordFormValidator</code>.
     * @param propertyService used to access configurable messages displayed to the user.
     * @param taxaDAO used to determine if species names can be resolved.
     */
    public RecordFormValidator(PropertyService propertyService, TaxaDAO taxaDAO) {
        
        this.propertyService = propertyService;
        this.taxaDAO = taxaDAO;
        errorMap = new HashMap<String, String>();
        createValidators();
    }
    
    private void createValidators() {
        validatorMap = new HashMap<ValidationType, Validator>();
        validatorMap.put(ValidationType.PRIMARYKEY, new IntRangeValidator(propertyService, true, false, 0, Integer.MAX_VALUE));  
        
        validatorMap.put(ValidationType.STRING, new StringValidator(propertyService, false, true));
        validatorMap.put(ValidationType.REQUIRED_BLANKABLE_STRING, new StringValidator(propertyService, true, true));
        validatorMap.put(ValidationType.REQUIRED_NONBLANK_STRING, new StringValidator(propertyService, true, false));
        
        validatorMap.put(ValidationType.INTEGER, new IntValidator(propertyService, false, true));
        validatorMap.put(ValidationType.REQUIRED_INTEGER, new IntValidator(propertyService, true, false));
        validatorMap.put(ValidationType.REQUIRED_POSITIVE_INT, new IntRangeValidator(propertyService, true, false));
        // less than, not less than or equal to.
        validatorMap.put(ValidationType.REQUIRED_POSITIVE_LESSTHAN, new IntRangeValidator(propertyService, true, false, 1000000-1));
        
        validatorMap.put(ValidationType.DOUBLE, new DoubleValidator(propertyService, false, true));
        validatorMap.put(ValidationType.REQUIRED_DOUBLE, new DoubleValidator(propertyService, true, false));
        validatorMap.put(ValidationType.REQUIRED_DEG_LONGITUDE, new DoubleRangeValidator(propertyService, true, false, -180, 180));
        validatorMap.put(ValidationType.REQUIRED_DEG_LATITUDE, new DoubleRangeValidator(propertyService, true, false, -90, 90));
        
        validatorMap.put(ValidationType.DATE, new DateValidator(propertyService, false, true));
        validatorMap.put(ValidationType.REQUIRED_DATE, new DateValidator(propertyService, true, false));
        validatorMap.put(ValidationType.REQUIRED_HISTORICAL_DATE, new HistoricalDateValidator(propertyService, true, false));
        
        validatorMap.put(ValidationType.REQUIRED_TAXON, new TaxonValidator(propertyService, true, false, taxaDAO));
    }
    
    /**
     * Returns a mapping of validation error messages. Messages are keyed against
     * the input name that was validated.
     * @return mapping of validation error messages.
     */
    public Map<String, String> getErrorMap() {
        return errorMap;
    }

    /**
     * Validates the first value in the array for the specified <code>key</code>
     * in the <code>parameterMap</code>. The <code>ValidationType</code> indicates
     * the kind of validation to apply. If the input is invalid, error messages
     * are stored in an internal map that is accessible via {@link #getErrorMap()}. 
     * 
     * @param parameterMap the POST dictionary of values. The key of the map 
     * is the input name and the value array is the input values. Note that only
     * the first input value gets validated.
     * @param type the kind of validation to apply to the input.
     * @param key the name of the input to validate.
     * @return true if the input was valid, false otherwise.
     */
    public boolean validate(Map<String, String[]> parameterMap, ValidationType type, String key) {
        Validator v = validatorMap.get(type);
        if(v == null) {
            throw new IllegalArgumentException("Cannot find validator for: "+type);
        } else {
            return v.validate(parameterMap, key, errorMap);
        }
    }
}

