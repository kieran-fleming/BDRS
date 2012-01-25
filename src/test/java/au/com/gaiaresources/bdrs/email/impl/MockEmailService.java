package au.com.gaiaresources.bdrs.email.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.email.EmailService;

public class MockEmailService implements EmailService {
    
    private List<MockEmail> mockEmailList = new ArrayList<MockEmail>();
    
    private Logger log = Logger.getLogger(getClass());
    
    public void init() {
      
    }

    @Override
    public String getErrorToAddress() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void sendMessage(String to, String from, String subject,
            String message) {
        MockEmail mockEmail = new MockEmail(to, from, subject, message, null, null);
        this.mockEmailList.add(mockEmail);
    }

    @Override
    public void sendMessage(String[] to, String from, String subject,
            String message) {
        MockEmail mockEmail = new MockEmail(to, from, subject, message);
        this.mockEmailList.add(mockEmail);
    }

    @Override
    public void sendMessage(String to, String from, String subject,
            String message, Map<String, Object> substitutionParams) {
        MockEmail mockEmail = new MockEmail(to, from, subject, message, null, substitutionParams);
        this.mockEmailList.add(mockEmail);
    }

    @Override
    public void sendMessage(String to, String subject, String templateName,
            Map<String, Object> subsitutionParams) {
        MockEmail mockEmail = new MockEmail(to, null, subject, null, templateName, subsitutionParams);
        this.mockEmailList.add(mockEmail);
    }

    @Override
    public void sendTemplateMessage(String to, String from, String subject,
            String templateName, Map<String, Object> subsitutionParams) {
        MockEmail mockEmail = new MockEmail(to, from, subject, null, templateName, subsitutionParams);
        this.mockEmailList.add(mockEmail);
    }

    /**
     * Clears the list of interally stored mock emails. 
     * 
     * Because this mock email service is
     * injected by Spring we want to CLEAR the internal list at an appropriate time for your test,
     * usually before invoking the controller which will use the email service.
     */
    public void clearEmails() {
        this.mockEmailList.clear();
    }
    
    /**
     * Get the list of mock emails sent using this mock object
     * 
     * @return list of MockEmail objects
     */
    public List<MockEmail> getMockEmailList() {
        return this.mockEmailList;
    }
}
