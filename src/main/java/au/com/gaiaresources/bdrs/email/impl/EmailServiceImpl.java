package au.com.gaiaresources.bdrs.email.impl;

import java.util.Map;
import java.util.Properties;

import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.service.template.TemplateService;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class EmailServiceImpl implements EmailService {
    @Autowired
    private JavaMailSender mailSender;
    @Autowired
    private TemplateService templateService;
    @Autowired
    private ContentDAO contentDAO;
    
    private Properties emailProperties;
    
    private static final String DEFAULT_SENDER_PROPERTY = "default.mail.from";
    private static final String ERROR_MAIL_RECIPIENT_PROPERTY = "error.mail.to";
    private static final String SUBJECT_PREFIX_PROPERTY = "mail.subject.prefix";
    
    public void init() throws Exception {
        emailProperties = new Properties();
        emailProperties.load(EmailService.class.getResourceAsStream("email.properties"));
    }

    @Override
    public void sendMessage(String to, String from, String subject, String message) {
        SimpleMailMessage m = new SimpleMailMessage();
        m.setTo(to);
        m.setFrom(from == null ? getDefaultFromAddress() : from);
        m.setSubject(subject);
        m.setText(message);
        mailSender.send(m);
    }
    
    public void sendMessage(final String to, final String subject, String templateName, 
                            Map<String, Object> subsitutionParams) 
    {
        // first check the contentDAO for the template
        // if it is not found, use the file system template
        String templateString = contentDAO.getContentValue(
            "email/"+templateName.substring(0, templateName.indexOf(".")));
        String text = "";
        if (!StringUtils.nullOrEmpty(templateString)) {
            text = templateService.evaluate(templateString, subsitutionParams);
        } else {
            text = templateService.transformToString(templateName, EmailService.class, subsitutionParams);
        }
        
        mailSender.send(createPreparator(to, getDefaultFromAddress(), subject, text));
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
