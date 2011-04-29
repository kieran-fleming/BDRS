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
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

/**
 * Webservice for performing attribute operations.
 * 
 * @author anthony
 *
 */
@Controller
public class AttributeService extends AbstractController {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private AttributeDAO attributeDAO;

    @Autowired
    private UserDAO userDAO;

    /**
     * Search through the list of record attributes for values that match the
     * given pattern. This is mainly used for autocomplete of fields.
     * 
     * @param ident - Ident key for the user
     * @param q - The search string fragment
     * @param attributePk - Primary key of the attribute to search record attributes
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = "/webservice/attribute/searchValues.htm", method = RequestMethod.GET)
    public void searchValues(@RequestParam(value="ident", defaultValue="") String ident,
                                @RequestParam(value="q", defaultValue="") String q,
                                @RequestParam(value="attribute", defaultValue="0") int attributePk,
                                HttpServletResponse response) throws IOException {

        User user;
        if(ident.isEmpty()) {
            throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
        }
        else {
            user = userDAO.getUserByRegistrationKey(ident);
            if(user == null) {
                throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }

        List<String> values = attributeDAO.getAttributeValues(attributePk, q);
        JSONArray array = new JSONArray();
        for(String value : values) {
            array.add(value);
        }

        response.setContentType("application/json");
        response.getWriter().write(array.toString());
    }

    @InitBinder
    public void initBinder(HttpServletRequest request, ServletRequestDataBinder binder) {
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        binder.registerCustomEditor(Date.class, new CustomDateEditor(
                dateFormat, true));
    }
}
