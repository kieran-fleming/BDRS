package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;

import javax.servlet.http.HttpServletResponse;

import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
 
/**
 * Webservice for retrieving managed file information.
 */
@Controller
public class ManagedFileWebService extends AbstractController {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private ManagedFileDAO managedFileDAO;

    /**
     */
    @RequestMapping(value = "/webservice/managedfile/getFile.htm", method = RequestMethod.GET)
    public void searchValues(   @RequestParam(value="uuid", required=true) String uuid,
                                HttpServletResponse response) throws IOException {
    	ManagedFile theFile = managedFileDAO.getManagedFile(uuid);
    	if (theFile != null) {
    		String json = JSONObject.fromMapToString(theFile.flatten());
    		response.setContentType("application/json");
            response.getWriter().write(json);
    	}
    	else {
    		response.sendError(HttpServletResponse.SC_NOT_FOUND);
    	}
    }
}
