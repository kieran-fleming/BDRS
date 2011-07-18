/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Date;
import java.util.Map;

import au.com.gaiaresources.bdrs.controller.record.ValidationType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.DateUtils;

/**
 * @author stephanie
 * 
 */
public class DynamicDateRangeValidator extends DateValidator {

    /**
     * @param propertyService
     * @param required
     * @param blank
     * @param earliest
     * @param latest
     */
    public DynamicDateRangeValidator(PropertyService propertyService,
            boolean required, boolean blank, Date earliest, Date latest) {
        super(propertyService, required, blank, earliest, latest);
    }

    /**
     * @param propertyService
     * @param required
     * @param blank
     * @param earliest
     * @param latest
     */
    public DynamicDateRangeValidator(PropertyService propertyService,
            boolean required, boolean blank, String earliest, String latest) {
        super(propertyService, required, blank, earliest, latest);
    }

    /**
     * @param propertyService
     * @param required
     * @param blank
     */
    public DynamicDateRangeValidator(PropertyService propertyService,
            boolean required, boolean blank) {
        super(propertyService, required, blank);
    }

    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key,
            Attribute attribute, Map<String, String> errorMap) {
        // set the earliest and latest from passed parameters
        String[] range = parameterMap.get("dateRange");
        this.earliest = DateUtils.getDate(range[0]);
        this.latest = DateUtils.getDate(range[1]);

        return super.validate(parameterMap, key, attribute, errorMap);
    }
}
