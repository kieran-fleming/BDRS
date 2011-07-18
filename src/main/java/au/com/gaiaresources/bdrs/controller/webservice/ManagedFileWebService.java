package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.ServletRequestDataBinder;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

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

        String json = JSONObject.fromObject(managedFileDAO.getManagedFile(uuid).flatten()).toString();
        response.setContentType("application/json");
        response.getWriter().write(json);
    }
}
