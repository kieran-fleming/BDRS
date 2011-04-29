package au.com.gaiaresources.bdrs.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.jasig.cas.client.authentication.AttributePrincipal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.UserDetails;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class Interceptor implements HandlerInterceptor {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    SessionFactory sessionFactory;
    
    @Autowired
    UserDAO userDAO;

    @Autowired
    private PortalDAO portalDAO;
    
    @Autowired
    private ThemeDAO themeDAO;
    
    @Autowired
    private PropertyService propertyService;
    /**
     * 
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) 
                             throws Exception 
    {
        RequestContext c = RequestContextHolder.getContext();
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if ((securityContext.getAuthentication() != null) && (securityContext.getAuthentication().getPrincipal() instanceof UserDetails)) {
    		c.setUserDetails((UserDetails) securityContext.getAuthentication().getPrincipal());
        } else {
        	if (request.getUserPrincipal() != null) {
	        	AttributePrincipal principal = (org.jasig.cas.client.authentication.AttributePrincipal)request.getUserPrincipal();
	        	//System.err.println("REMOVEME : Authenticated via CAS : " + principal.getName());
	        	User u = userDAO.getUser(principal.getName());
	        	boolean updateUser = false;
	        	if (u == null) {
	        		// new user, need to create
	        		u = userDAO.createUser(principal.getName(), 
	        				String.valueOf(principal.getAttributes().get("firstname")),
	        				String.valueOf(principal.getAttributes().get("lastname")),
	        				String.valueOf(principal.getAttributes().get("email")), 
	        				new Md5PasswordEncoder().encodePassword(StringUtils.generateRandomString(10, 50), principal.getName()), // password 
	        				new Md5PasswordEncoder().encodePassword(StringUtils.generateRandomString(10, 50), principal.getName()), // regkey
	        				"ROLE_USER");
	        		u.setActive(true);
	        		updateUser = true;
	        	} 
	        	if (!u.getEmailAddress().equals(String.valueOf(principal.getAttributes().get("email")))) {
	        		u.setEmailAddress(String.valueOf(principal.getAttributes().get("email")));
	        		updateUser = true;
	        	}
	        	if (!u.getFirstName().equals(String.valueOf(principal.getAttributes().get("firstname")))) {
	        		u.setFirstName(String.valueOf(principal.getAttributes().get("firstname")));
	        		updateUser = true;
		        }
	        	if (!u.getLastName().equals(String.valueOf(principal.getAttributes().get("lastname")))) {
	        		u.setLastName(String.valueOf(principal.getAttributes().get("lastname")));
	        		updateUser = true;
		        }
	        	
	        	if (updateUser) {
	        		userDAO.updateUser(u);
	        	}
        		c.setUserDetails(new UserDetails(u));
                List<GrantedAuthority> grantedAuth = new ArrayList<GrantedAuthority>(u.getRoles().length);
                for(String role : u.getRoles()) {
                    grantedAuth.add(new GrantedAuthorityImpl(role));
                }
        			
        		Authentication auth = new AnonymousAuthenticationToken(u.getName(), 
        				principal, 
        				grantedAuth);
        		auth.setAuthenticated(true);
        		securityContext.setAuthentication(auth);
        		SecurityContextHolder.setContext(securityContext);
	        } else {
        		//System.err.println("REMOVEME : User Principal is null");
        	}	
        }
        
        c.setHibernate(sessionFactory.getCurrentSession());
        sessionFactory.getCurrentSession().beginTransaction();
        
        if (c.getUser() != null) {
            // rebind the user in the context to the current session.
            // Intentionally using session.get to work around filtering in this case.
            // the user may be the root user which has no portal id.
            // User rebind = (User)c.getHibernate().get(User.class, c.getUser().getId());    
            User rebind = userDAO.getUser(c.getUser().getId());
            ((au.com.gaiaresources.bdrs.security.UserDetails)c.getUserDetails()).setUser(rebind);
        }
        if(c.getPortal() == null) {
            c.setTheme(null);
        } else {
            // Rebind the Portal to the current session.
            c.setPortal(portalDAO.getPortal(c.getPortal().getId()));
            c.setTheme(themeDAO.getActiveTheme(c.getPortal()));
        }
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler, ModelAndView modelAndView) throws Exception {
    	if (modelAndView != null) {
    	    RequestContext requestContext = RequestContextHolder.getContext();
            modelAndView.getModel().put("context", requestContext);
            
            // Theming
            Theme theme = null;
            Map<String, ThemeElement> themeElementMap = new HashMap<String, ThemeElement>();
            if(request.getParameter(Theme.DISABLE_THEME) == null) {
                theme = requestContext.getTheme();
                modelAndView.getModel().put("theme", theme);
                if(theme != null) {
                    for(ThemeElement elem : theme.getThemeElements()) {
                        themeElementMap.put(elem.getKey(), elem);
                    }
                }
                modelAndView.getModel().put("themeMap", themeElementMap);
            } else {
                requestContext.addMessage(propertyService.getMessage("theme.disabled"));
            }
            
            if (modelAndView.getView() instanceof RedirectView) {
                requestContext.newTx();
            }
            if (request.getParameter("format") != null) {
            	if (request.getParameter("format").equalsIgnoreCase("java")) {
	            	modelAndView.setViewName("json");
	            	response.setContentType("text/plain");
	            	response.getWriter().write(modelAndView.getModel().toString());
	            	response.getWriter().flush();
	            	response.getWriter().close();
            	} else if (request.getParameter("format").equalsIgnoreCase("json")) {
            		modelAndView.setViewName("json");
	            	response.setContentType("application/json");
	            	response.getWriter().write(parseJSON(modelAndView.getModel()).toString(4));
	            	response.getWriter().flush();
	            	response.getWriter().close();
            	}
            }
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
                                throws Exception 
    {
    	if (RequestContextHolder.getContext().getHibernate() != null && 
    	        (RequestContextHolder.getContext().getHibernate().isOpen()) &&
    			(RequestContextHolder.getContext().getHibernate().getTransaction().isActive())) {
    	    Transaction tx = RequestContextHolder.getContext().getHibernate().getTransaction();
    	    tx.commit();
    	}
    	RequestContextHolder.clear();
    }

    @SuppressWarnings("unchecked")
    protected JSONObject parseJSON(Map modelMap) {
    	JSONObject json = new JSONObject();
    	
    	for (Object key : modelMap.keySet()) {
    		if (modelMap.get(key) instanceof PersistentImpl) {
    			PersistentImpl ob = (PersistentImpl)modelMap.get(key);
    			json.element(key.toString(), ob.flatten());
    		} else if (modelMap.get(key) instanceof Map) {
    			Map ob = (Map)modelMap.get(key);
    			json.element(key.toString(), parseJSON(ob));
    		} else if (modelMap.get(key) instanceof Set) {
    			json.element(key.toString(), parseJSON((Set)modelMap.get(key)));
    		} else if (modelMap.get(key) instanceof List) {
    			json.element(key.toString(), parseJSON((List)modelMap.get(key)));    			
    		} else {
    			json.element(key.toString(), String.valueOf(modelMap.get(key)));
    		}
    	}
    	return json;
    }
    
    @SuppressWarnings("unchecked")
    protected JSONArray parseJSON(List list) {
    	JSONArray array = new JSONArray();
    	for (Object ob : list) {
    		if (ob instanceof PersistentImpl) {
    			array.add(((PersistentImpl)ob).flatten());
    		} else if (ob instanceof Map) {
    			array.add(parseJSON((Map)ob));
    		} else if (ob instanceof Set) {
    			array.add(parseJSON((Set)ob));
    		} else if (ob instanceof List) {
    			array.add(parseJSON((List)ob));
    		} else {
    			array.add(String.valueOf(ob));
    		}
    	}
    	return array;
    }
    
    @SuppressWarnings("unchecked")
    protected JSONArray parseJSON(Set set) {
    	JSONArray array = new JSONArray();
    	for (Object ob : set) {
    		if (ob instanceof PersistentImpl) {
    			array.add(((PersistentImpl)ob).flatten());
    		} else if (ob instanceof Map) {
    			array.add(parseJSON((Map)ob));
    		} else if (ob instanceof Set) {
    			array.add(parseJSON((Set)ob));
    		} else if (ob instanceof List) {
    			array.add(parseJSON((List)ob));
    		} else {
    			array.add(String.valueOf(ob));
    		}
    	}
    	return array;    	
    }
}
