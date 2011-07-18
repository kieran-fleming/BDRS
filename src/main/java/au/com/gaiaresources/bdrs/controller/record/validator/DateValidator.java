package au.com.gaiaresources.bdrs.controller.record.validator;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.DateFormatter;
import au.com.gaiaresources.bdrs.util.DateUtils;

/**
 * Validates that the input is a parseable date and if specified, is between
 * the date range.
 */
public class DateValidator extends AbstractValidator {

    private static final String DATE_MESSAGE_KEY = "DateValidator.date";
    private static final String DATE_MESSAGE = "Must be a valid date with the form dd MMM yy e.g %te %tb %tY";

    private static final String DATE_OR_BLANK_MESSAGE_KEY = "DoubleValidator.dateOrBlank";
    private static final String DATE_OR_BLANK_MESSAGE = "Must be blank or a valid date with the form dd MMM yy e.g %te %tb %tY";
    
    private static final String DATE_BEFORE_MESSAGE_KEY = "DateValidator.dateBefore";
    private static final String DATE_BEFORE_MESSAGE = "Date must be before %te %tb %tY";
    
    private static final String DATE_BEFORE_OR_BLANK_MESSAGE_KEY = "DateValidator.dateBeforeOrBlank";
    private static final String DATE_BEFORE_OR_BLANK_MESSAGE = "Date must be before %te %tb %tY or blank.";
    
    private static final String DATE_AFTER_MESSAGE_KEY = "DateValidator.dateAfter";
    private static final String DATE_AFTER_MESSAGE = "Date must be after %te %tb %tY";
    
    private static final String DATE_AFTER_OR_BLANK_MESSAGE_KEY = "DateValidator.dateAfterOrBlank";
    private static final String DATE_AFTER_OR_BLANK_MESSAGE = "Date must be after %te %tb %tY or blank.";
    
    protected Date latest = new Date(Long.MAX_VALUE);
    protected Date earliest = new Date(0);

    /**
     * Creates a new <code>DateValidator</code>.
     * 
     * @param propertyService used to access configurable messages displayed to the user.
     * @param required true if the input is mandatory, false otherwise.
     * @param blank true if the value can be an empty string, false otherwise.
     * @param earliest the earliest valid date.
     * @param latest the latest valid date.
     */
    public DateValidator(PropertyService propertyService, boolean required,
            boolean blank, Date earliest, Date latest) {
        super(propertyService, required, blank);
        this.earliest = earliest;
        this.latest = latest;
    }
    
    /**
     * Creates a new <code>DateValidator</code>.
     * 
     * @param propertyService used to access configurable messages displayed to the user.
     * @param required true if the input is mandatory, false otherwise.
     * @param blank true if the value can be an empty string, false otherwise.
     * @param earliest the earliest valid date as a String.
     * @param latest the latest valid date as a String.
     */
    public DateValidator(PropertyService propertyService, boolean required,
            boolean blank, String earliest, String latest) {
        super(propertyService, required, blank);
        this.earliest = DateUtils.getDate(earliest);
        this.latest = DateUtils.getDate(latest);
    }
    
    /**
     * Creates a new <code>DateValidator</code>.
     * 
     * @param propertyService used to access configurable messages displayed to the user.
     * @param required true if the input is mandatory, false otherwise.
     * @param blank true if the value can be an empty string, false otherwise.
     */
    public DateValidator(PropertyService propertyService, boolean required,
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
                Date date = DateFormatter.parse(value, DateFormatter.DAY_MONTH_YEAR);
                if (date != null) {
                    if (earliest != null && date.before(earliest)) {
                        
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(earliest);
                        String template;
                        
                        if(blank) {
                            template = propertyService.getMessage(DATE_AFTER_OR_BLANK_MESSAGE_KEY, DATE_AFTER_OR_BLANK_MESSAGE);
                            
                        } else {
                            template = propertyService.getMessage(DATE_AFTER_MESSAGE_KEY, DATE_AFTER_MESSAGE);
                        }
                        errorMap.put(key, String.format(template, cal, cal, cal));
                    } else if (latest != null && date.after(latest)) {
                        
                        Calendar cal = new GregorianCalendar();
                        cal.setTime(latest);
                        String template;
                        
                        if(blank) {
                            template = propertyService.getMessage(DATE_BEFORE_OR_BLANK_MESSAGE_KEY, DATE_BEFORE_OR_BLANK_MESSAGE);
                            
                        } else {
                            template = propertyService.getMessage(DATE_BEFORE_MESSAGE_KEY, DATE_BEFORE_MESSAGE);
                        }
                        errorMap.put(key, String.format(template, cal, cal, cal));
                        
                    }
                    // Otherwise it is valid.
                } else {
                    // date is null, invalid parse exception
                    Calendar cal = new GregorianCalendar();
                    String template;
                    if(blank) {
                        template = propertyService.getMessage(DATE_OR_BLANK_MESSAGE_KEY, DATE_OR_BLANK_MESSAGE);
                        
                    } else {
                        template = propertyService.getMessage(DATE_MESSAGE_KEY, DATE_MESSAGE);
                    }
                    errorMap.put(key, String.format(template, cal, cal, cal));
                }
            }
        }

        return isValid && !errorMap.containsKey(key);
    }
}
