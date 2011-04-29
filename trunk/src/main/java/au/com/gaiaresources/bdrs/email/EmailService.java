package au.com.gaiaresources.bdrs.email;

import java.util.Map;

public interface EmailService {
    void sendMessage(String to, String from, String subject, String message);
    
    void sendMessage(String to, String subject, String templateName, Map<String, Object> subsitutionParams);
    
    String getErrorToAddress();
}
