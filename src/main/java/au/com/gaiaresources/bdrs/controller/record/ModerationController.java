package au.com.gaiaresources.bdrs.controller.record;

import java.io.IOException;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.security.Role;

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
    
    public static final String MSG_CODE_RECORD_MULTI_MODERATE_SUCCESS = "bdrs.record.multiModerate.success";
    public static final String MSG_CODE_RECORD_MULTI_MODERATE_AUTHFAIL = "bdrs.record.multiModerate.authfail";

    public static final String PARAM_REDIRECT_URL = "redirecturl";
    
    @Autowired
    private RecordDAO recordDAO;

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
        
        String holdString = hold ? "hold" : "release";
        String msgHoldString = hold ? "held" : "released";
        
        boolean authFail = false;
        int recordModerateCount = 0;

        for (Integer id : recordId) {
            if (id > 0) {
                Record record = recordDAO.getRecord(id);
                
                if (record != null) {
                    // only allow moderation of records by a moderator.
                    if (getRequestContext().getUser().isModerator()) {
                        if (record.getHeld() != hold) {
                            record.setHeld(hold);
                            recordDAO.saveRecord(record);
                        }
                        ++recordModerateCount;
                    } else {
                        log.info("No permissions to " + holdString + " record: + " + record.getId());
                        authFail = true;
                        break;
                    }
                } else {
                    // could not find record for given id but handle silently. log a warning.
                    log.warn("Requested " + holdString + " of non-existent record, id:" + id);
                }
            }
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
        }
        return new ModelAndView(new RedirectView(redirectURL, true));
    }
    
}
