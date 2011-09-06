package au.com.gaiaresources.bdrs.controller.user;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentInitialiserService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Calling it UserMessage vs UserEmail or anything like that because eventually this will not default
 * to using email (I think). It will be an internal BDRS notification system. Users will be able to
 * choose whether the BDRS will send emails to them or not.W
 * 
 * @author aaron
 *
 */
@Controller
public class UserMessageController extends AbstractController {

    public static final String CONTACT_RECORD_OWNER_URL = "/bdrs/user/contactRecordOwner.htm";
    
    public static final String VIEW_MESSAGE_USER = "messageUser";
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String PARAM_REPLY_EMAIL = "replyEmail";
    public static final String PARAM_TEXT = "text";
    public static final String PARAM_SEND_TO_SELF = "sendToSelf";
    
    public static final String MV_TEXT = "text";
    public static final String MV_REPLY_EMAIL = "replyEmail";
    public static final String MV_RECORD_ID = "recordId";
    
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private EmailService emailService;
    @Autowired
    private RedirectionService redirService;
    
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value=CONTACT_RECORD_OWNER_URL, method=RequestMethod.GET)
    public ModelAndView getContactRecordOwnerForm(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_RECORD_ID, required=true) int recordPk) {
        
        Record rec = recordDAO.getRecord(recordPk);
        if (rec == null) {
            // bad request
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return null;
        }
        
        ModelAndView mv = new ModelAndView(VIEW_MESSAGE_USER);
        mv.addObject(MV_RECORD_ID, rec.getId());
        if (getRequestContext().getUser() != null) {
            mv.addObject(MV_REPLY_EMAIL, getRequestContext().getUser().getEmailAddress());
        }
        return mv;
    }
    
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value=CONTACT_RECORD_OWNER_URL, method=RequestMethod.POST)
    public ModelAndView postContactRecordOwnerForm(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value=PARAM_RECORD_ID, required=true) int recordPk,
            @RequestParam(value=PARAM_REPLY_EMAIL, required=true) String replyEmail,
            @RequestParam(value=PARAM_TEXT, required=true) String text,
            @RequestParam(value=PARAM_SEND_TO_SELF, required=false, defaultValue="false") boolean sendToSelf) {
        
        Record rec = recordDAO.getRecord(recordPk);
        if (rec == null) {
            ModelAndView mv = new ModelAndView(VIEW_MESSAGE_USER);
            mv.addObject(MV_TEXT, text);
            mv.addObject(MV_REPLY_EMAIL);
            getRequestContext().addMessage("bdrs.user.message.failureRecordInvalid");
            return mv;
        }
        
        // attempt to send email...
        Map<String, Object> emailParams = new HashMap<String, Object>();
        emailParams.put("userFirstName", rec.getUser().getFirstName());
        emailParams.put("userLastName", rec.getUser().getLastName());
        emailParams.put("viewRecordUrl", redirService.getViewRecordUrl(rec));
        emailParams.put("contactText", text);
        emailParams.put("portal", getRequestContext().getPortal());
        emailParams.put("contextPath", ContentInitialiserService.getRequestURL(request));
        
        emailService.sendTemplateMessage(rec.getUser().getEmailAddress(), 
                                          replyEmail,
                                          "Someone has contact you regarding a record you have created", 
                                          "ContactRecordOwner.vm", 
                                          emailParams);
        
        if (sendToSelf) {
            // set the record url to the 'contact record owner' url
            emailParams.put("viewRecordUrl", CONTACT_RECORD_OWNER_URL+"?"+PARAM_RECORD_ID+"="+rec.getId().toString());
            
            emailService.sendTemplateMessage(replyEmail, 
                                             replyEmail,
                                             "You have contacted someone regarding a record they have created", 
                                             "ContactRecordOwnerSendToSelf.vm", 
                                             emailParams);
        }
        
        ModelAndView mv = new ModelAndView(new RedirectView(redirService.getMySightingsUrl(rec.getSurvey()), true));
        getRequestContext().addMessage("bdrs.user.message.success");
        return mv;
    }
}
