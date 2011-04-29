package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.Calendar;
import java.util.GregorianCalendar;

import au.com.gaiaresources.bdrs.service.property.PropertyService;

/**
 * Validates that the input is a parseable date and is in the past.
 */
public class HistoricalDateValidator extends DateValidator {
    
    /**
    * Creates a new <code>HistoricalDateValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    */
    public HistoricalDateValidator(PropertyService propertyService, boolean required, boolean blank) {
        super(propertyService, required, blank);
        
        Calendar cal = new GregorianCalendar();
        cal.setTimeInMillis(System.currentTimeMillis());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        latest = cal.getTime();
    }
}
