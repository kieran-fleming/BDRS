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
    
    @Autowired
    private RecordDAO recordDAO;
    
    @Autowired
    private RedirectionService redirectionService;

    @RolesAllowed( {  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = "/bdrs/user/deleteRecord.htm", method = RequestMethod.POST)
    public ModelAndView addRecord(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "recordId", required = false, defaultValue = "0") int recordId) throws IOException {
        
    	Record record = recordDAO.getRecord(recordId);
    	
        if (record != null) {
        	Survey s = record.getSurvey();
        		// only allow deletion of own records, or by admin.
        	if (record.getUser().equals(getRequestContext().getUser()) || getRequestContext().getUser().isAdmin()) {
        		recordDAO.delete(record);
        		getRequestContext().addMessage(new Message("bdrs.record.delete.success"));
        	} else {
        		getRequestContext().addMessage(new Message("bdrs.record.delete.authfail"));
        	}
        	
        	// get the appropriate redirection URL either from the request or 
        	// the default one if none exists in the request
        	String redirectURL = null;
                if (request.getSession().getAttribute("redirecturl") != null) {
                    redirectURL = request.getSession().getAttribute("redirecturl").toString();
                } else if (request.getParameter("redirecturl") != null) { 
                    redirectURL = request.getParameter("redirecturl");
                } else {
                    redirectURL = redirectionService.getMySightingsUrl(s);
                }
        	return new ModelAndView(new RedirectView(redirectURL, true));
        }
        log.warn("Requested delete of non-existent record, id:" + recordId);
        response.sendError(HttpServletResponse.SC_BAD_REQUEST);
        return null;
    }
}