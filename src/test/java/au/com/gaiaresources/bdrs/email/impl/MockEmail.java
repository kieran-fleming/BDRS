package au.com.gaiaresources.bdrs.email.impl;

import java.util.Map;

public class MockEmail {

    private String to;
    private String[] toArray;
    private String from;
    private String subject;
    private String template;
    private String message;
    private Map<String,Object> params;
    
    public MockEmail(String[] to, String from, String subject,
            String message) {
        this.toArray = to;
        this.from = from;
        this.subject = subject;
        this.message = message;
    }
    
    public MockEmail(String to, String from, String subject, String message, String template, Map<String, Object> params) {
        this.to = to;
        this.from = from;
        this.subject = subject;
        this.message = message;
        this.template = template;
        this.params = params;
    }
    
    public String getTo() {
        return to;
    }
    public void setTo(String to) {
        this.to = to;
    }
    public String[] getToArray() {
        return toArray;
    }
    public void setToArray(String[] toArray) {
        this.toArray = toArray;
    }
    public String getFrom() {
        return from;
    }
    public void setFrom(String from) {
        this.from = from;
    }
    public String getSubject() {
        return subject;
    }
    public void setSubject(String subject) {
        this.subject = subject;
    }
    public String getMessage() {
        return message;
    }
    public void setMessage(String message) {
        this.message = message;
    }
    public String getTemplate() {
        return template;
    }
    public void setTemplate(String template) {
        this.template = template;
    }
    public Map<String, Object> getParams() {
        return params;
    }
    public void setParams(Map<String, Object> params) {
        this.params = params;
    }
}
