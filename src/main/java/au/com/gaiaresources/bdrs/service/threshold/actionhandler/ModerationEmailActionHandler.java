package au.com.gaiaresources.bdrs.service.threshold.actionhandler;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.record.Record;
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
            for (User user : surveyUsers) {
                if (user.isModerator()) {
                    emailParams.put("userFirstName", user.getFirstName());
                    emailParams.put("userLastName", user.getLastName());
                    emailService.sendMessage(user.getEmailAddress(), record.getUser().getEmailAddress(), 
                                             "A record requires moderation", 
                                             contentService.getContent(sesh, "email/ModerationRequired"), 
                                             emailParams);
                }
            }
            
        } else {
            // a moderator has changed a moderated record, email the user that 
            // their record was moderated
            emailParams.put("userFirstName", record.getUser().getFirstName());
            emailParams.put("userLastName", record.getUser().getLastName());
            emailService.sendMessage(record.getUser().getEmailAddress(), loggedInUser.getEmailAddress(), 
                                     "One of your records has been moderated", 
                                     contentService.getContent(sesh, "email/ModerationPerformed"), 
                                     emailParams);
        }
    }
}
