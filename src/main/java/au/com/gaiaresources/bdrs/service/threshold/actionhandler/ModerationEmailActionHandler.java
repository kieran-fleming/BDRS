package au.com.gaiaresources.bdrs.service.threshold.actionhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

/**
 * This action handler sends moderation emails:
 * 
 * When a record is modified by a moderator, an email is sent from the moderator 
 * to the owner of the record telling the owner that their record has been moderated.
 * 
 * When a record is created or updated by the owner, an email is sent from the 
 * record owner to all moderators for the survey telling them that the record 
 * requires moderation.
 * 
 * @author stephanie
 */
public class ModerationEmailActionHandler extends EmailActionHandler {

    private Logger log = Logger.getLogger(getClass());
    
    private ContentService contentService;
    
    private RedirectionService redirService;
    
    private UserDAO userDAO;
    
    public ModerationEmailActionHandler(EmailService emailService,
            PropertyService propertyService, ContentService contentService, 
            RedirectionService redirService, UserDAO userDAO) {
        super(emailService, propertyService);
        this.contentService = contentService;
        this.redirService = redirService;
        this.userDAO = userDAO;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.threshold.actionhandler.EmailActionHandler#executeAction(org.hibernate.Session, au.com.gaiaresources.bdrs.model.threshold.Threshold, java.lang.Object, au.com.gaiaresources.bdrs.model.threshold.Action)
     */
    @Override
    public void executeAction(Session sesh, Threshold threshold, Object entity,
            Action action) throws ClassNotFoundException {
        Record record = (Record) entity;
        User loggedInUser = RequestContextHolder.getContext().getUser();
        Map<String, Object> emailParams = new HashMap<String, Object>(ContentService.getContentParams());
        emailParams.put("viewRecordUrl", redirService.getViewRecordUrl(record));
        if (loggedInUser.equals(record.getUser())) {
            // the user has inserted/changed their moderated record, send an email to 
            // the moderators asking them to check it
            Set<User> surveyUsers = record.getSurvey().getUsers();
            if (surveyUsers.isEmpty()) {
                // this means all users can access the survey, get all the moderators
                surveyUsers.addAll(userDAO.getUsersByRoles(sesh, new String[]{Role.ROOT, Role.ADMIN, Role.SUPERVISOR}));
            }
            String emailContent = getEmailContent(sesh, record.getSurvey(), Metadata.MODERATION_REQUIRED_EMAIL, ContentService.MODERATION_REQUIRED_EMAIL_KEY);
            for (User user : surveyUsers) {
                // send the message to any moderator, but not to yourself
                if (user.isModerator() && !user.equals(loggedInUser)) {
                    emailParams.put("moderatorFirstName", user.getFirstName());
                    emailParams.put("moderatorLastName", user.getLastName());
                    emailParams.put("userFirstName", loggedInUser.getFirstName());
                    emailParams.put("userLastName", loggedInUser.getLastName());
                    emailService.sendMessage(user.getEmailAddress(), record.getUser().getEmailAddress(), 
                                             "A record requires moderation", 
                                             emailContent, 
                                             emailParams);
                }
            }
            
        } else {
            // a moderator has changed a moderated record, email the user that 
            // their record was moderated
            
            String emailContent = getEmailContent(sesh, record.getSurvey(), Metadata.MODERATION_PERFORMED_EMAIL, ContentService.MODERATION_PERFORMED_EMAIL_KEY);
            
            emailParams.put("moderatorFirstName", loggedInUser.getFirstName());
            emailParams.put("moderatorLastName", loggedInUser.getLastName());
            emailParams.put("userFirstName", record.getUser().getFirstName());
            emailParams.put("userLastName", record.getUser().getLastName());
            emailService.sendMessage(record.getUser().getEmailAddress(), loggedInUser.getEmailAddress(), 
                                     "One of your records has been moderated", 
                                     emailContent, 
                                     emailParams);
        }
    }

    /**
     * Gets the email message content either from the survey metadata or the default content.
     * @param sesh The current session
     * @param survey The survey to request {@link Metadata} from
     * @param metadataKey The key for the {@link Metadata} containing the content
     * @param defaultContentKey The default {@link Content} key to use when the {@link Metadata} doesn't exist
     * @return The {@link Content} in String form that matches the key from the {@link Metadata} or the defaultContentKey
     */
    private String getEmailContent(Session sesh, Survey survey, String metadataKey, String defaultContentKey) {
        // get the email content from the metadata or use the default if no metadata is specified
        Metadata md = survey.getMetadataByKey(metadataKey);
        String emailContentKey = defaultContentKey;
        if (md != null) {
            emailContentKey = md.getValue();
        }
        return contentService.getContent(sesh, emailContentKey);
    }
}
