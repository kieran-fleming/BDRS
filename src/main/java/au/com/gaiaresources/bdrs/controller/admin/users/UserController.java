package au.com.gaiaresources.bdrs.controller.admin.users;

import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

/* UNUSED 2011-09-22 */

// admin/registerUser.htm may be used by an outside entity.

@Controller
public class UserController extends AbstractController {
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private UserDAO userDAO;
    
    @RequestMapping(value = "/admin/users/list.htm", method = RequestMethod.GET)
    public String render() {
        return "adminUserList";
    }
    
    /**
     * <p>
     *     Registers a user in the BDRS database
     * </p>
     *
     * <p>
     *     This function expects the following get parameters
     * </p>
     * <ul>
     *     <li>
     *         details - a JSON representation of the user to register.
     *     </li>
     * </ul>
     *
     * <p>
     *     The function will return a JSON encoded representation of the form:
     *     { user_id : <id of newly registered user>, ident : <user regKey } or { user_id : -1 } if
     *     the registration failed.
     * </p>
     */
    @RequestMapping(value="/admin/registerUser.htm", method = RequestMethod.POST)
    public void registerUser(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String query = request.getParameter("details");
        JSONObject ob = new JSONObject();
        if (query != null && !query.isEmpty()) {
        	try {
        		JSONObject details = JSONObject.fromObject(query);
        		
        		User user = userDAO.getUser(String.valueOf(details.get("name")));
        		if (user == null) {
        			if (String.valueOf(details.get("admin")).equals("true")) {
	        			user = userDAO.createUser(String.valueOf(details.get("name")),
	            			String.valueOf(details.get("first_name")), 
	    					String.valueOf(details.get("last_name")), 
	    					String.valueOf(details.get("email_address")), 
	    					String.valueOf(details.get("password")), 
	            			(new Md5PasswordEncoder()).encodePassword(String.valueOf(details.get("name")), ""),
	            			 Role.ADMIN);
        			} else {
	        			user = userDAO.createUser(String.valueOf(details.get("name")),
	            			String.valueOf(details.get("first_name")), 
	    					String.valueOf(details.get("last_name")), 
	    					String.valueOf(details.get("email_address")), 
	    					String.valueOf(details.get("password")), 
	            			(new Md5PasswordEncoder()).encodePassword(String.valueOf(details.get("name")), ""),
	            			"ROLE_USER");	        				
        			}
        		} else {
        			user.setFirstName(String.valueOf(details.get("first_name")));
        			user.setLastName(String.valueOf(details.get("last_name")));
        			user.setEmailAddress(String.valueOf(details.get("email_address")));
        			user.setPassword(String.valueOf(details.get("password")));
        			
        			if (String.valueOf(details.get("admin")).equals("true")) {
        				String[] roles = {  Role.ADMIN };
        				user.setRoles(roles);
        			}
        			else {
        				String[] roles = { "ROLE_USER" };
        				user.setRoles(roles);
        			}
        			userDAO.updateUser(user);
        		}
            	ob.put("user_id", String.valueOf(user.getId()));
            	ob.put("ident", user.getRegistrationKey());
            	if (details.get("active").equals("1")) {
            		userDAO.makeUserActive(user, true);
            	}
	    	} catch (JSONException jse) {
        		log.error("Failed to add user", jse);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        	}
        } else {
      	  log.warn("Attempted user creation from : " + request.getRemoteAddr() + ", No or invalid details supplied");
  	      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
  	      return;
        }
        
        response.setContentType("application/json");
        response.getWriter().write(ob.toString());
    }
}
