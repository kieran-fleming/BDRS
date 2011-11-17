package au.com.gaiaresources.bdrs.email.impl;

import java.util.Map;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.template.TemplateService;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ContentService contentService;
    
    private Properties emailProperties;
    
    private Logger log = Logger.getLogger(getClass());
    
    private static final String DEFAULT_SENDER_PROPERTY = "default.mail.from";
    private static final String ERROR_MAIL_RECIPIENT_PROPERTY = "error.mail.to";
    private static final String SUBJECT_PREFIX_PROPERTY = "mail.subject.prefix";
    
    public void init() throws Exception {
        emailProperties = new Properties();
        emailProperties.load(EmailService.class.getResourceAsStream("email.properties"));
    }

    @Override
    public void sendMessage(String to, String from, String subject, String message) {
        sendMessage(new String[]{to}, from, subject, message);
    }
    
    @Override
    public void sendMessage(String[] to, String from, String subject, String message) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(to);
        m.setFrom(from == null ? getDefaultFromAddress() : from);
        m.setSubject(subject);
        m.setText(message);
        mailSender.send(m);
    }
    
    public void sendMessage(final String to, final String from, final String subject, String message, Map<String, Object> substitutionParams) 
    {
        String text = templateService.evaluate(message, substitutionParams);
        mailSender.send(createPreparator(to, from == null ? getDefaultFromAddress() : from, subject, text));
    }

    public void sendMessage(final String to, final String subject, String templateName, 
                            Map<String, Object> subsitutionParams) {
        sendTemplateMessage(to, getDefaultFromAddress(), subject, templateName, subsitutionParams);
    }
    
    @Override
    public void sendTemplateMessage(String to, String from, String subject, String templateName, Map<String, Object> subsitutionParams) {
        ContentService.putContentParams(subsitutionParams);
        Portal portal = (Portal) subsitutionParams.get("portal");
        // first check the contentDAO for the template
        // if it is not found, use the file system template        
        String contentKey = "email/"+templateName.substring(0, templateName.indexOf("."));
        // although the param name is 'contextPath' it's actually the application url
        String templateString = contentService.getContent(portal, contentKey);
        
        String text = "";
        // this is probably unnecessary now as the contentDAO should handle loading 
        // the file system value when the value does not exist
        if (!StringUtils.nullOrEmpty(templateString)) {
            text = templateService.evaluate(templateString, subsitutionParams);
        } else {
            text = templateService.transformToString(templateName, EmailService.class, subsitutionParams);
        }

        mailSender.send(createPreparator(to, from, subject, text));
    }

    
    private MimeMessagePreparator createPreparator(final String to, final String from, 
                                                   final String subject, final String text) 
    {
        return new MimeMessagePreparator() {
            public void prepare(MimeMessage mimeMessage) throws Exception {
                MimeMessageHelper message = new MimeMessageHelper(mimeMessage);
                message.setTo(new InternetAddress(to));
                message.setFrom(from);
                message.setSubject(getSubjectPrefix() + subject);
                message.setText(text, true);
            }
        };
    }
    
    private String getDefaultFromAddress() {
        return emailProperties.getProperty(DEFAULT_SENDER_PROPERTY);
    }
    
    public String getErrorToAddress() {
        return emailProperties.getProperty(ERROR_MAIL_RECIPIENT_PROPERTY);
    }
    
    private String getSubjectPrefix() {
        if (StringUtils.notEmpty(emailProperties.getProperty(SUBJECT_PREFIX_PROPERTY))) {
            return emailProperties.getProperty(SUBJECT_PREFIX_PROPERTY).trim() + " ";
        }
        return "";
    }
}
