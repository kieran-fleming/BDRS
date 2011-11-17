package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class RegExpValidator extends AbstractValidator {
    
    private Logger log = Logger.getLogger(this.getClass());
    
    private String regExp = ".*";

    
    private static final String FIT_REGEXP_MESSSAGE_KEY = "RegExpValidator.regExp";
    private static final String FIT_REGEXP_MESSSAGE = "Must fit regular expression %s ";
    private static final String BLANK_MESSSAGE_KEY = "AbstractValidator.blank";
    private static final String BLANK_MESSSAGE = "This cannot be blank";

    public RegExpValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        super(propertyService, required, blank);
    }
    
    public RegExpValidator(PropertyService propertyService, boolean required,
            boolean blank, String regExp) {
        super(propertyService, required, blank);
        this.regExp = regExp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key, Attribute attribute, Map<String, String> errorMap) {
        boolean isValid = super.validate(parameterMap, key, attribute, errorMap);
        
        if(isValid){
            String value = getSingleParameter(parameterMap, key);
            if (!StringUtils.nullOrEmpty(value)) {
                //use regular expression in attribute options if there is one, otherwise use default one
                List<AttributeOption> options = null;
                if (attribute != null)
                    options = attribute.getOptions();
                
                if (options != null && options.size() > 0){
                    regExp = attribute.getOptionString();
                }
                
                //actual validation
                Pattern pattern = Pattern.compile(regExp);
                Matcher matcher = pattern.matcher(value);
                if(!matcher.matches()){
                    errorMap.put(key, String.format(propertyService.getMessage(FIT_REGEXP_MESSSAGE_KEY, FIT_REGEXP_MESSSAGE), regExp));
                }
            } else {
                // the string is null or empty
                if (required) {
                    errorMap.put(key, propertyService.getMessage(BLANK_MESSSAGE_KEY, BLANK_MESSSAGE));
                }
            }
        }
        
        return isValid && !errorMap.containsKey(key);
    }
}
