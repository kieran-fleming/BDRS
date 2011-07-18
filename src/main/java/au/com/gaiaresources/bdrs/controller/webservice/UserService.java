package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import net.sf.json.JSONArray;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.Filter;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.filter.PortalSelectionFilter;


/**
 * The User Service provides a web API for User, Group and Class based
 * services.
 */
@Controller
public class UserService extends AbstractController {

    private Logger log = Logger.getLogger(getClass());

    public static final String USER_NAME = "userName";
    public static final String EMAIL_ADDRESS = "emailAddress";
    public static final String FULL_NAME = "fullName";
    
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private GroupDAO groupDAO;
    
    @Autowired
    private LocationDAO locationDAO;
    
    @Autowired
    private PortalDAO portalDAO;


    /**
     * <p>
     *     Performs a query into the database for Users and Groups given the
     *     username or group name, user firstname or user lastname.
     * </p>
     *
     * <p>
     *     This function expects the following get parameters
     * </p>
     * <ul>
     *     <li>
     *         ident - The registration key of the user performing the request.
     *     </li>
     *     <li>
     *         q - The name fragment
     *     </li>
     * </ul>
     *
     * <p>
     *     The function will return a JSON encoded representation of all
     *     matching User and Group objects.
     * </p>
     * <p>
     *     This function is restricted to administration users only.
     * </p>
     */
    @RequestMapping(value="/webservice/user/searchUserAndGroup.htm", method=RequestMethod.GET)
    public void searchUserAndGroup(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String ident = request.getParameter("ident");
        String query = request.getParameter("q");

        JSONObject result = new JSONObject();

        if(ident != null) {
            User user = userDAO.getUserByRegistrationKey(ident);
            if(user != null && user.isAdmin()) {

                JSONArray array = new JSONArray();
                for(User u : userDAO.getUsersByNameSearch(query)) {
                    array.add(u.flatten());
                }
                result.put(User.class.getSimpleName(), array);

                array = new JSONArray();
                for(Group g : groupDAO.getGroupsByNameSearch(query)) {
                    array.add(g.flatten());
                }
                result.put(Group.class.getSimpleName(), array);
            }
            else {
                throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        else {
            throw new HTTPException(HttpServletResponse.SC_UNAUTHORIZED);
        }

        response.setContentType("application/json");
        response.getWriter().write(result.toString());
    }

    
    /* Ping service used by the client to see if there is a connection
     * Returns true
     */
    @RequestMapping(value="/webservice/user/ping.htm")
    public void ping(HttpServletRequest request, HttpServletResponse response)
        throws IOException {
    	log.debug("ping received");
    	String output = request.getParameter("callback") + "({0:1});";
    	response.setContentType("text/javascript");
        response.getWriter().write(output);
    }

    
    /**
     * <p>
     *     Performs a query into the database to see whether a given username is
     *     available
     * </p>
     *
     * <p>
     *     This function expects the following get parameters
     * </p>
     * <ul>
     *     <li>
     *         q - The desired username
     *     </li>
     * </ul>
     *
     * <p>
     *     The function will return a JSON encoded representation of the form:
     *     { available : true } or { available : false } based on the 
     *     availability of the username
     * </p>
     * <p>
     *     This function is unrestricted.
     * </p>
     */
    @RequestMapping(value="/webservice/user/checkUsername.htm", method=RequestMethod.GET)
    public void searchUser(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String query = request.getParameter("q");

        JSONObject ob = new JSONObject();
        if (userDAO.getUser(query) != null) {
        	ob.put("available", false);
        } else {
        	ob.put("available", true);
        }
        
        response.setContentType("application/json");
        response.getWriter().write(ob.toString());
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
     *         signature - MD5 Hash of details + signature
     *     </li>
     * </ul>
     *
     * <p>
     *     The function will return a JSON encoded representation of the form:
     *     { user_id : <id of newly registered user>, ident : <user regKey } or { user_id : -1 } if
     *     the registration failed.
     * </p>
     * <p>
     *     This function is restricted by a secret.
     * </p>
     */
    @RequestMapping(value="/webservice/user/registerUser.htm")
    public void registerUser(HttpServletRequest request, HttpServletResponse response)
        throws IOException {

        String query = request.getParameter("details");
        JSONObject ob = new JSONObject();
        if (query != null && !query.isEmpty()) {
        	String sig = request.getParameter("signature");
        	try {
	        	JSONObject details = JSONObject.fromObject(query);
	        	
	        	MessageDigest m = MessageDigest.getInstance("MD5");
	        	String key = (query + "0a43f170d6c4282682511317d843d050");
				m.update(key.getBytes(),0,key.length());
				String mySig = new BigInteger(1,m.digest()).toString(16);
				while (mySig.length() < 32) {
					  mySig = "0" + mySig;
				}

	        	String timestamp = String.valueOf(details.get("timestamp"));
	        	SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	        	Date date = format.parse(timestamp);
	        	
	        	if (mySig.equals(sig) && 
	        			(Math.abs(System.currentTimeMillis() - date.getTime()) < 30000)) {
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
	        	} else {
	        		log.warn("Attempted user creation from : " + request.getRemoteAddr() + ", Details : " + details + 
	        				", My Sig : " + mySig + ", Their Sig : " + sig);
	                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
	        	}
        	} catch (JSONException jse) {
        		log.error("Failed to add user", jse);
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        	} catch (ParseException pe) {
        		log.error("Failed to add user", pe);
        		response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        	} catch (NoSuchAlgorithmException nsae) {
        		log.error("Failed to add user", nsae);
        	}
        } else {
      	  log.warn("Attempted user creation from : " + request.getRemoteAddr() + ", No or invalid details supplied");
  	      response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
        
        response.setContentType("application/json");
        response.getWriter().write(ob.toString());
    }
    
    /**
     * TODO WE MUST ADD CRYPTO TO THIS. (another webservice to get a public key, 
     * then this webservice will have to decrypt using a session based private key)
     * @param userName
     * @param password
     * @param request
     * @param response
     * @throws IOException
     * @throws NoSuchAlgorithmException
     */
    @RequestMapping(value="/webservice/user/validate.htm")
    public void validateUser(
    		        @RequestParam(value = "username", defaultValue = "") String userName,
			@RequestParam(value = "password", defaultValue = "") String password,
			@RequestParam(value = "portalName", required=false) String portalName,
    		        HttpServletRequest request, HttpServletResponse response)
        throws IOException, NoSuchAlgorithmException, InterruptedException {
    	// This is a precaution to stop people using this service to brute force passwords.
    	Thread.sleep(1000);
    	
    	RequestContext requestContext = getRequestContext();
    	if(portalName != null) {
    	    Portal portal = portalDAO.getPortalByName(getRequestContext().getHibernate(), 
    	                                              portalName);
    	    if(portal != null) {
    	        requestContext.setPortal(portal);

    	        Session sesh = requestContext.getHibernate();
    	        Filter filter = sesh.getEnabledFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
    	        filter.setParameter(PortalPersistentImpl.PORTAL_FILTER_PORTALID_PARAMETER_NAME, portal.getId());
    	        
    	        request.getSession().setAttribute(PortalSelectionFilter.PORTAL_ID_KEY, portal.getId());
    	    }
    	}

    	JSONObject validationResponse = new JSONObject();
	User user = userDAO.getUser(userName);
	Md5PasswordEncoder encoder = new Md5PasswordEncoder();
	
    	if (user != null && encoder.isPasswordValid(user.getPassword(), password, null)) {
    	    validationResponse.put("user", user.flatten(1, true, true));
    	    validationResponse.put("ident", user.getRegistrationKey());
    	    validationResponse.put("portal_id", user.getPortal().getId());
    	    JSONArray locations = new JSONArray();
    	    for(Location l : locationDAO.getUserLocations(user)){
    	        locations.add(l.flatten(true, true));
    	    }
    	    validationResponse.put("location",locations);
    	} else {
    	    response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
    	    return;
    	}
    	
    	// support for JSONP
    	if (request.getParameter("callback") != null) {
    		response.setContentType("application/javascript");        	
    		response.getWriter().write(request.getParameter("callback") + "(");
    	} else {
    		response.setContentType("application/json");
    	}

        response.getWriter().write(validationResponse.toString());
    	if (request.getParameter("callback") != null) {
    		response.getWriter().write(");");
    	}
    }
    
    
    /**
     * Web logging service for the mobile API. This takes and ident and if valid, prints a log message
     */
    @RequestMapping(value="/webservice/user/log.htm")
    public void logRequest(
    		@RequestParam(value = "ident", defaultValue = "") String ident,
    		@RequestParam(value = "message", defaultValue = "") String message,
    		@RequestParam(value = "level", defaultValue = "") String level,
			HttpServletRequest request, HttpServletResponse response) throws IOException {
    	
        if(ident != null) {
            User user = userDAO.getUserByRegistrationKey(ident);
            if(user != null) {
            	log.debug(user.getName() + " : " + message);
            	
                // support for JSONP
                if (request.getParameter("callback") != null) {
                        response.setContentType("application/javascript");              
                        response.getWriter().write(request.getParameter("callback") + "(");
                } else {
                        response.setContentType("application/json");
                }

                response.getWriter().write("{}");
                if (request.getParameter("callback") != null) {
                        response.getWriter().write(");");
                }
            }
            else {
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            }
        }
        else {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
        }
    }
    
    @RequestMapping(value="/webservice/user/searchUsers.htm", method=RequestMethod.GET)
    public void searchUsers(
                @RequestParam(value = "userName", defaultValue = "") String username,
                @RequestParam(value = "emailAddress", defaultValue = "") String emailAddress,
                @RequestParam(value = "fullName", defaultValue = "") String fullName,
                @RequestParam(value = "parentGroupId", defaultValue = "") Integer parentGroupId,
                HttpServletRequest request, HttpServletResponse response) throws Exception {
        
        JqGridDataHelper jqGridHelper = new JqGridDataHelper(request);       
        PaginationFilter filter = jqGridHelper.createFilter(request);
        
        User currentUser = this.getRequestContext().getUser();
        String[] allowedRolesToSearchFor = Role.getRolesLowerThanOrEqualTo(Role.getHighestRole(currentUser.getRoles()));
        String[] rolesToExclude = Role.getRolesHigherThan(Role.getHighestRole(currentUser.getRoles()));
        PagedQueryResult<User> queryResult = userDAO.search(username, emailAddress, fullName, filter, allowedRolesToSearchFor, rolesToExclude, parentGroupId);
        
        JqGridDataBuilder builder = new JqGridDataBuilder(jqGridHelper.getMaxPerPage(), queryResult.getCount(), jqGridHelper.getRequestedPage());
        //builder.addRow(new JqGridDataRow()
        if (queryResult.getCount() > 0) {
            for (User user : queryResult.getList()) {
                JqGridDataRow row = new JqGridDataRow(user.getId());
                row
                .addValue("userName", user.getName())
                .addValue("firstName", user.getFirstName())
                .addValue("lastName", user.getLastName())
                .addValue("emailAddress", user.getEmailAddress());
                builder.addRow(row);
            }
        }
        response.setContentType("application/json");
        response.getWriter().write(builder.toJson());
    }
}
