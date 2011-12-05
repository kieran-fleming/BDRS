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
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

@Controller
public class RecordDeletionController extends AbstractController {

    private Logger log = Logger.getLogger(getClass());
    
    public static final String PARAM_RECORD_ID = "recordId";
    public static final String RECORD_DELETE_URL = "/bdrs/user/deleteRecord.htm";
    
    public static final String MSG_CODE_RECORD_DELETE_SUCCESS = "bdrs.record.delete.success";
    public static final String MSG_CODE_RECORD_DELETE_REDIRECT_SUCCESS = "bdrs.record.deleteRedirect.success";
    
    public static final String MSG_CODE_RECORD_MULTI_DELETE_SUCCESS = "bdrs.record.multiDelete.success";
    public static final String MSG_CODE_RECORD_MULTI_DELETE_REDIRECT_SUCCESS = "bdrs.record.multiDeleteRedirect.success";
    
    public static final String MSG_CODE_RECORD_DELETE_AUTHFAIL = "bdrs.record.delete.authfail";
    public static final String MSG_CODE_RECORD_MULTI_DELETE_AUTHFAIL = "bdrs.record.multiDelete.authfail";
    public static final String MSG_CODE_RECORD_DELETE_REDIRECT_AUTHFAIL = "bdrs.record.deleteRedirect.authfail";
    public static final String MSG_CODE_RECORD_MULTI_DELETE_REDIRECT_AUTHFAIL = "bdrs.record.multiDeleteRedirect.authfail";
    
    public static final String PARAM_REDIRECT_URL = "redirecturl";
    
    @Autowired
    private RecordDAO recordDAO;
    
    @Autowired
    private RedirectionService redirectionService;

    /**
     * POST handler for deleting one or more records
     * 
     * @param request - http request object
     * @param response - http response object
     * @param recordId - an integer array full of record ids
     * @return - ModelAndView for redirection
     * @throws IOException
     */
    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = RECORD_DELETE_URL, method = RequestMethod.POST)
    public ModelAndView deleteRecord(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = PARAM_RECORD_ID, required = true) Integer[] recordId) throws IOException {
        
        // survey to redirect to...
        // note we'll redirect to the last survey in the record list in the case of redirecting to the
        // my sightings page.
        Survey s = null;
        boolean authFail = false;
        int recordDeleteCount = 0;

        for (Integer id : recordId) {
            
            if (id > 0) {
                Record record = recordDAO.getRecord(id);
                
                if (record != null) {
                    log.info("Requested delete of record: " + record.getId());
                    s = record.getSurvey();
                    // only allow deletion of own records, or by admin.
                    if (record.getUser().equals(getRequestContext().getUser()) || getRequestContext().getUser().isAdmin()) {
                        log.info("Deleting record: + " + record.getId());
                        recordDAO.delete(record);
                        ++recordDeleteCount;
                    } else {
                        log.info("No permissions to delete record: + " + record.getId());
                        authFail = true;
                        break;
                    }
                } else {
                    // could not find record for given id but handle silently. log a warning.
                    log.warn("Requested delete of non-existent record, id:" + id);
                }
            }
        }
        
        // get the appropriate redirection URL either from the request or 
        // the default one if none exists in the request
        String redirectURL = null;
        boolean defaultRedirect = true;
        if (request.getSession().getAttribute(PARAM_REDIRECT_URL) != null) {
            redirectURL = request.getSession().getAttribute(PARAM_REDIRECT_URL).toString();
            defaultRedirect = false;
        } else if (request.getParameter(PARAM_REDIRECT_URL) != null) { 
            redirectURL = request.getParameter(PARAM_REDIRECT_URL);
            defaultRedirect = false;
        } else {
            // the default redirect case
            redirectURL = redirectionService.getMySightingsUrl(s);
        }
        
        // set messages appropriately
        if (authFail) {
            // rollback transaction. if one record fails to delete they _all_ fail
            requestRollback(request);
            
            if (recordId.length > 1) {
                if (defaultRedirect) {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_MULTI_DELETE_AUTHFAIL));
                } else {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_MULTI_DELETE_REDIRECT_AUTHFAIL));
                }
            } else {
                if (defaultRedirect) {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_DELETE_AUTHFAIL));
                } else {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_DELETE_REDIRECT_AUTHFAIL));
                }
            }
        } else {
            if (recordDeleteCount > 1) {
                // multi delete case
                if (defaultRedirect) {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_MULTI_DELETE_SUCCESS, new Object[] { recordDeleteCount }));
                } else {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_MULTI_DELETE_REDIRECT_SUCCESS, new Object[] { recordDeleteCount } ));
                }
            } else {
                // single delete case
                if (defaultRedirect) {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_DELETE_SUCCESS));
                } else {
                    getRequestContext().addMessage(new Message(MSG_CODE_RECORD_DELETE_REDIRECT_SUCCESS));
                }
            }
        }
        return new ModelAndView(new RedirectView(redirectURL, true));
    }
}