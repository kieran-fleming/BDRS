package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.w3c.tidy.TidyMessage;
import org.w3c.tidy.TidyMessageListener;
import org.w3c.tidy.TidyMessage.Level;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.StringUtils;

/**
 * Validates HTML input using JTidy.
 * 
 * @author stephanie
 */
public class HtmlValidator extends StringValidator {

    /**
     * 
     * @param propertyService
     * @param required
     * @param blank
     */
    public HtmlValidator(PropertyService propertyService, boolean required,
            boolean blank) {
        super(propertyService, required, blank);
    }

    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key,
            Attribute attribute, Map<String, String> errorMap) {
        String value = getSingleParameter(parameterMap, key);
        try {
            MyTidyMessageListener listener = new MyTidyMessageListener();
            String temp = StringUtils.validateHtml(value, listener);
            if (temp == null) {
                for (String error : listener.getErrors()) {
                    errorMap.put(attribute != null ? attribute.getName() : key, error);
                }
            }
        }
        catch (Exception e) {
            errorMap.put(attribute != null ? attribute.getName() : key, e.getMessage());
        }
        return !errorMap.containsKey(key);
    }
    
    /**
     * Message listener for Tidy that makes errors accessible.
     */
    public static class MyTidyMessageListener implements TidyMessageListener {
        List<String> errors = new ArrayList<String>();
        List<String> warnings = new ArrayList<String>();
        
        @Override
        public void messageReceived(TidyMessage arg0) {
            if (arg0.getLevel() == Level.ERROR) {
                errors.add(arg0.getMessage());
            } else if (arg0.getLevel() == Level.WARNING) {
                warnings.add(arg0.getMessage());
            }
        }
        
        public List<String> getErrors() {
            return this.errors;
        }
        
        public List<String> getWarnings() {
            return this.warnings;
        }
    }
}
