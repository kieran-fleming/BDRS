package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * This controller allows moderators (ROOT, ADMIN, and SUPERVISOR roles)
 * to hold/release records.
 *
 * @author stephanie
 */
@Controller
public class ModerationController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_HOLD = "hold";
    
    public static final String RECORD_MODERATE_URL = "/bdrs/user/moderateRecord.htm";
    
    /**
     * Property service key
     */
    public static final String MSG_CODE_RECORD_MULTI_MODERATE_SUCCESS = "bdrs.record.multiModerate.success";
    /**
     * Property service key
     */
    public static final String MSG_CODE_RECORD_MULTI_MODERATE_AUTHFAIL = "bdrs.record.multiModerate.authfail";

    public static final String PARAM_REDIRECT_URL = "redirecturl";
    
    /**
     * Velocity engine parameter - the list of record urls
     */
    public static final String VM_PARAM_KEY_RECORD_URL_LIST = "viewRecordUrlList";
    
    /**
     * Velocity engine parameter - moderator first name
     */
    public static final String VM_PARAM_KEY_MOD_FIRST_NAME = "moderatorFirstName";
    
    /**
     * Velocity engine parameter - moderator last name
     */
    public static final String VM_PARAM_KEY_MOD_LAST_NAME = "moderatorLastName";
    
    /**
     * Velocity engine parameter - record owner first name
     */
    public static final String VM_PARAM_KEY_OWNER_FIRST_NAME = "userFirstName";
    
    /**
     * Velocity engine parameter - record owner last name
     */
    public static final String VM_PARAM_KEY_OWNER_LAST_NAME = "userLastName";
    
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RedirectionService redirService;
    @Autowired
    private ContentService contentService;

    /**
     * POST handler for holding or releasing one or more records
     * 
     * @param request - http request object
     * @param response - http response object
     * @param recordId - an integer array full of record ids
     * @param hold - boolean flag indicating whether to hold or release the records
     * @return - ModelAndView for redirection
     * @throws IOException
     */
    @RolesAllowed( { Role.ROOT, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = RECORD_MODERATE_URL, method = RequestMethod.POST)
    public ModelAndView moderateRecord(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = PARAM_RECORD_ID, required = true) Integer[] recordId,
            @RequestParam(value = PARAM_HOLD, required = true) boolean hold) throws IOException {
        
        Map<User, List<Record>> recMap = new HashMap<User, List<Record>>();
        
        String holdString = hold ? "hold" : "release";
        String msgHoldString = hold ? "held" : "released";
        
        User moderator = getRequestContext().getUser();
        
        boolean authFail = false;
        int recordModerateCount = 0;

        // only allow moderation of records by a moderator.
        if (moderator != null && moderator.isModerator()) {
            for (Integer id : recordId) {
                if (id > 0) {
                    Record record = recordDAO.getRecord(id);
                    
                    if (record != null) {
                        if (record.getHeld() != hold) {
                            record.setHeld(hold);
                            recordDAO.saveRecord(record);
                        }
                        ++recordModerateCount;
                        
                        List<Record> recList = recMap.get(record.getUser());
                        if (recList == null) {
                            // add new entry to map if required
                            recList = new ArrayList<Record>();
                            recMap.put(record.getUser(), recList);
                        }
                        recList.add(record);
                    } else {
                        // could not find record for given id but handle silently. log a warning.
                        log.warn("Requested " + holdString + " of non-existent record, id:" + id);
                    }
                }
            }
        } else {
            log.info("No permissions to " + holdString + " record");
            authFail = true;
        }
        
        // get the appropriate redirection URL either from the request or 
        // the default one if none exists in the request
        String redirectURL = null;
        if (request.getSession().getAttribute(PARAM_REDIRECT_URL) != null) {
            redirectURL = request.getSession().getAttribute(PARAM_REDIRECT_URL).toString();
        } else if (request.getParameter(PARAM_REDIRECT_URL) != null) { 
            redirectURL = request.getParameter(PARAM_REDIRECT_URL);
        }
        
        if (authFail) {
            // rollback transaction. if one record fails to delete they _all_ fail
            requestRollback(request);
            
            getRequestContext().addMessage(new Message(MSG_CODE_RECORD_MULTI_MODERATE_AUTHFAIL, new Object[] { msgHoldString }));
        } else {
            getRequestContext().addMessage(new Message(MSG_CODE_RECORD_MULTI_MODERATE_SUCCESS, new Object[] { msgHoldString, recordModerateCount }));
            
            if (!hold) {
                // When releasing records from the moderation cycle send out emails...
                Session sesh = getRequestContext().getHibernate();
                
                for (Entry<User, List<Record>> entry : recMap.entrySet()) {
                    List<String> viewRecordUrlList = new ArrayList<String>(recMap.size());
                    Map<String, Object> emailParams = new HashMap<String, Object>(ContentService.getContentParams());
                    User user = entry.getKey();

                    emailParams.put(VM_PARAM_KEY_MOD_FIRST_NAME, moderator.getFirstName());
                    emailParams.put(VM_PARAM_KEY_MOD_LAST_NAME, moderator.getLastName());
                    emailParams.put(VM_PARAM_KEY_OWNER_FIRST_NAME, user.getFirstName());
                    emailParams.put(VM_PARAM_KEY_OWNER_LAST_NAME, user.getLastName());
                    
                    for (Record r : entry.getValue()) {
                        viewRecordUrlList.add(redirService.getViewRecordUrl(r));
                    }
                    emailParams.put(VM_PARAM_KEY_RECORD_URL_LIST, viewRecordUrlList);
                    
                    emailService.sendMessage(user.getEmailAddress(), moderator.getEmailAddress(), 
                                             "Record(s) have been released from moderation", 
                                             contentService.getContent(sesh, "email/RecordReleased"), 
                                             emailParams);
                }    
            }
        }
        return new ModelAndView(new RedirectView(redirectURL, true));
    }
}
