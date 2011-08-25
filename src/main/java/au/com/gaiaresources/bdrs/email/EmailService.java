package au.com.gaiaresources.bdrs.email;

import java.util.Map;

public interface EmailService {
    void sendMessage(String to, String from, String subject, String message);
    
    void sendMessage(String[] to, String from, String subject, String message);
    
    void sendMessage(final String to, final String from, final String subject, String message, Map<String, Object> substitutionParams); 
    
    void sendMessage(String to, String subject, String templateName, Map<String, Object> subsitutionParams);
    
    void sendTemplateMessage(String to, String from, String subject, String templateName, Map<String, Object> subsitutionParams);
    
    String getErrorToAddress();
}
