package au.com.gaiaresources.bdrs.servlet;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

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
import au.com.gaiaresources.bdrs.model.menu.MenuItem;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.theme.ThemeDAO;
import au.com.gaiaresources.bdrs.model.theme.ThemeElement;
import au.com.gaiaresources.bdrs.model.theme.ThemePage;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.security.UserDetails;
import au.com.gaiaresources.bdrs.service.menu.MenuService;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.web.GoogleKeyService;
import au.com.gaiaresources.bdrs.servlet.view.FileView;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class Interceptor implements HandlerInterceptor {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    public static final String REQUEST_ROLLBACK = "requestRollback";
    
    @Autowired
    SessionFactory sessionFactory;
    
    @Autowired
    UserDAO userDAO;

    @Autowired
    private PortalDAO portalDAO;

    @Autowired
    private MenuService menuService;
    @Autowired
    private ThemeDAO themeDAO;
    @Autowired
    private PropertyService propertyService;
    
    @Autowired
    private GoogleKeyService gkService;
    
    private static final String GOOGLE_MAP_KEY = "bdrsGoogleMapsKey";
    public static final String PARAM_PAGE_TITLE = "pageTitle";
    public static final String PARAM_PAGE_DESCRIPTION = "pageDescription";
    private static final String MV_USER_ID = "authenticatedUserId";
    private static final String MV_USER_ROLE = "authenticatedRole";
    private static final String MV_USER_IS_ADMIN = "isAdmin";
    
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
            if (rebind != null && c.getUserDetails() != null) {
                ((au.com.gaiaresources.bdrs.security.UserDetails)c.getUserDetails()).setUser(rebind);
            }
        }
        
        if(c.getPortal() == null) {
            c.setTheme(null);
        } else {
            // Rebind the Portal to the current session.
            c.setPortal(portalDAO.getPortal(c.getPortal().getId()));
            c.setTheme(themeDAO.getActiveTheme(c.getPortal()));
        }
        
        // bind the menu to the context
        List<MenuItem> menu = menuService.getMenus(c.getUser());
        if (menu == null || menu.size() < 1) {
            // write an error message so the user will know why they don't have any menus
            RequestContextHolder.getContext().addMessage("bdrs.menu.error");
        }
        c.setMenu(menu);
        
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, 
                           Object handler, ModelAndView modelAndView) throws Exception {
    	if (modelAndView != null) {
    	    RequestContext requestContext = RequestContextHolder.getContext();
            
            
            // Theming
            Theme theme = null;
            Map<String, ThemeElement> themeElementMap = new HashMap<String, ThemeElement>();
            Portal portal = requestContext.getPortal();
            
            if(request.getParameter(Theme.DISABLE_THEME) != null) {
                requestContext.addMessage(propertyService.getMessage("theme.disabled"));
                // fallback to the default theme
                log.info("Theme is disabled, falling back to the default theme");
                theme = themeDAO.getDefaultTheme(portal);
            } else {
                theme = requestContext.getTheme();
                if (theme == null && portal != null && 
                        !(modelAndView.getView() instanceof FileView) && 
                        !(modelAndView.getView() instanceof RedirectView)) {
                    theme = themeDAO.getActiveTheme(portal);
                }
            }
            
            if (modelAndView.getView() instanceof RedirectView) {
                requestContext.newTx();
            } else {
                // RedirectView by default, exposes model attributes in the URL querystring.
                // So, only add these model items if we aren't using a redirectview. The items
                // will get added to the model eventually when the handler for the non redirect
                // view occurs.
                String googleMapKey = gkService.getGoogleMapApiKey(request.getServerName());
                if (googleMapKey == null) {
                    if (!request.getRequestURL().toString().startsWith("http://localhost")) {
                        // google maps always works with localhost so only log when NOT requested from localhost
                        log.error("No google maps key found - google maps will not work");   
                    }
                    modelAndView.getModel().put(GOOGLE_MAP_KEY, "");
                } else {
                    modelAndView.getModel().put(GOOGLE_MAP_KEY, googleMapKey);
                }
                User loggedInUser = requestContext.getUser();
                if (loggedInUser != null && loggedInUser.getId() != null) {
                    modelAndView.getModel().put(MV_USER_ID, loggedInUser.getId().toString());
                    modelAndView.getModel().put(MV_USER_ROLE, Role.getHighestRole(loggedInUser.getRoles()));
                    modelAndView.getModel().put(MV_USER_IS_ADMIN, loggedInUser.isAdmin());
                }
                
                modelAndView.getModel().put("context", requestContext);
                
                // Add the plugin facade to the model.
                BdrsPluginFacade facade = new BdrsPluginFacade(requestContext.getHibernate(), requestContext.getPortal(), request.getRequestURI(), requestContext.getUser());
                modelAndView.getModel().put("bdrsPluginFacade", facade);
                
                if(theme != null) {
                    sessionFactory.getCurrentSession().update(theme);
                    modelAndView.getModel().put("theme", theme);
                    for(ThemeElement elem : theme.getThemeElements()) {
                        themeElementMap.put(elem.getKey(), elem);
                    }
                    modelAndView.getModel().put("themeMap", themeElementMap);
                    if (theme.getId() != null) {
                        // if performance becomes an issue consider caching the ThemePages using a service.
                        // remember to invalidate the cache when changing the theme!
                        
                        // the page key will be the view name
                        ThemePage themePage = themeDAO.getThemePage(theme.getId().intValue(), modelAndView.getViewName());
                        if (themePage != null) {
                            modelAndView.addObject(PARAM_PAGE_TITLE, themePage.getTitle());
                            modelAndView.addObject(PARAM_PAGE_DESCRIPTION, themePage.getDescription());
                        }
                    }
                }
            }
            
            if (request.getParameter("format") != null) {
            	if (request.getParameter("format").equalsIgnoreCase("java")) {
	            	modelAndView.setViewName("jsonDummyView");
	            	response.setContentType("text/plain");
	            	response.getWriter().write(modelAndView.getModel().toString());
	            	response.getWriter().flush();
	            	response.getWriter().close();
            	} else if (request.getParameter("format").equalsIgnoreCase("json")) {
            		modelAndView.setViewName("jsonDummyView");
	            	response.setContentType("application/json");
	            	response.getWriter().write(parseJSON(modelAndView.getModel()).toString());
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
    	    
    	    Object requestRollback = request.getAttribute(REQUEST_ROLLBACK); 
    	    if (requestRollback != null && requestRollback instanceof Boolean && ((Boolean)requestRollback) == true) {
    	        log.info("roll back requested");
    	        tx.rollback();
    	    } else {
                tx.commit();
    	    }
    	}
    	RequestContextHolder.clear();
    }
    
    /**
     * If called will rollback instead of commit at the end of the request
     * 
     * @param request
     */
    public static void requestRollback(HttpServletRequest request) {
        request.setAttribute(Interceptor.REQUEST_ROLLBACK, true);
    }

    @SuppressWarnings("unchecked")
    protected JSONObject parseJSON(Map modelMap) {
    	JSONObject json = new JSONObject();
    	
    	for (Object key : modelMap.keySet()) {
    		if (modelMap.get(key) instanceof PersistentImpl) {
    			PersistentImpl ob = (PersistentImpl)modelMap.get(key);
    			json.put(key.toString(), ob.flatten());
    		} else if (modelMap.get(key) instanceof Map) {
    			Map ob = (Map)modelMap.get(key);
    			json.put(key.toString(), parseJSON(ob));
    		} else if (modelMap.get(key) instanceof Set) {
    			json.put(key.toString(), parseJSON((Set)modelMap.get(key)));
    		} else if (modelMap.get(key) instanceof List) {
    			json.put(key.toString(), parseJSON((List)modelMap.get(key)));    			
    		} else {
    			json.put(key.toString(), String.valueOf(modelMap.get(key)));
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
