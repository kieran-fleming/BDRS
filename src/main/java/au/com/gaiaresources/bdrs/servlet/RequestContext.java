package au.com.gaiaresources.bdrs.servlet;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.validation.ObjectError;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.message.Messages;
import au.com.gaiaresources.bdrs.model.menu.MenuItem;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

public class RequestContext {

    public static final String REQUEST_CONTEXT_SESSION_ATTRIBUTE_KEY = "ClimateWatch-RequestContext";
    
    private static final String GLOBAL_MESSAGE_KEY = "globalmessages";

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    private ApplicationContext applicationContext;

    private HttpServletRequest request;
    private HttpSession session;
    private UserDetails user;
    private Portal portal;
    private Theme theme;
    private String[] roles;
    private Session hibernate;

    private List<MenuItem> menu;
    
    public RequestContext() {
        // TODO Auto-generated constructor stub
    }

    public RequestContext(HttpServletRequest req,
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.request = req;
        this.session = req.getSession(false);
        if (this.session != null) {
            if (this.session.getAttribute(GLOBAL_MESSAGE_KEY) == null) {
                this.session.setAttribute(GLOBAL_MESSAGE_KEY, new Messages());
            }
        }
    }
    
    public String getSessionID() {
        return request.getSession().getId();
    }

    public String getRemoteAddress() {
        return request.getRemoteAddr();
    }

    /**
     * Add a message to the request context with the given message code.
     * 
     * @param messageCode
     *            {@link String}.
     */
    public void addMessage(String messageCode) {
        addMessage(messageCode, null);
    }

    /**
     * Add a message to the request context with the given message code and
     * arguments.
     * 
     * @param messageCode
     *            {@link String}.
     * @param args
     *            {@link Object} array.
     */
    public void addMessage(String messageCode, Object[] args) {
        addMessage(new Message(messageCode, args, messageCode));
    }

    /**
     * Add a message to the request context.
     * 
     * @param error
     *            {@link ObjectError}.
     */
    public void addMessage(ObjectError error) {
        if (this.session != null) {
            if (this.session.getAttribute(GLOBAL_MESSAGE_KEY) == null) {
                this.session.setAttribute(GLOBAL_MESSAGE_KEY, new Messages());
            }
            ((Messages) this.session.getAttribute(GLOBAL_MESSAGE_KEY)).addError(error);
        }
    }

    /**
     * Get the contents of the message that are in the request context. The
     * messages are constructed with the {@link java.util.Locale} of the
     * request. The messages are then cleared.
     * 
     * @return {@link List} of {@link String}
     */
    public List<String> getMessageContents() {
        // Property service should* be non null whenever we are attempting to process
        // message codes
        //
        // * may or may not be true (just kidding, it should** be ok)
        // ** see *
        PropertyService propertyService = AppContext.getBean(PropertyService.class);
        
        List<String> messageContents = new ArrayList<String>();
        List<Message> msgList = getMessages();
        
        for (Message e : msgList) {
         // attempt to use the message code with the property service. if this fails, attempt
            // to get the string from the application context.
            // PropertyService uses messages.properties
            // The app context uses bdrs-errors.properties
            // This is an attempt to make how we use error codes more uniform.
            String tmpl = propertyService.getMessage(e.getCode());
            if (tmpl != null) {
                messageContents.add(String.format(tmpl, e.getArguments()));
            } else {
                messageContents.add(applicationContext.getMessage(e, request.getLocale()));
            }
        }
        
        // clear the messages from the session
        if (this.session != null) {
            this.session.setAttribute(GLOBAL_MESSAGE_KEY, new Messages());
        }
        return messageContents;
    }
    
    /**
     * Get the keys for any messages that are not null or empty.
     *      
     * @return
     */
    public List<String> getMessageCodes() {
        List<String> msgCodes = new ArrayList<String>();
        List<Message> msgList = getMessages();
        for (Message e : msgList) {
            if (StringUtils.hasLength(e.getCode())) {
                msgCodes.add(e.getCode());
            }
        }
        return msgCodes;
    }
    
    /**
     * Gets all the messages in the session
     * 
     * @return list of Message objects
     */
    public List<Message> getMessages() {
        List<Message> result = new ArrayList<Message>();
        if (this.session != null
                && this.session.getAttribute(GLOBAL_MESSAGE_KEY) != null) {
            for (Object o : ((Messages) this.session.getAttribute(GLOBAL_MESSAGE_KEY)).getAllErrors()) {
                if (o instanceof Message) {
                    result.add((Message)o);
                }
            }
        }
        return result;
    }

    public void setUserDetails(UserDetails userDetails) {
        this.user = userDetails;
        this.roles = new String[userDetails.getAuthorities().size()];
        int i = 0;
        for (GrantedAuthority auth : userDetails.getAuthorities()) {
            this.roles[i] = auth.getAuthority();
            i++;
        }
    }

    public boolean isAuthenticated() {
        return this.user != null;
    }

    public User getUser() {
        return this.user != null ? ((au.com.gaiaresources.bdrs.security.UserDetails) (this.user)).getUser()
                : null;
    }

    public String[] getRoles() {
        return this.roles;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }

    public Portal getPortal() {
        return this.portal;
    }
    
    public void setTheme(Theme theme) {
        this.theme = theme;
    }
    
    public Theme getTheme() {
        return this.theme;
    }

    public void removeSessionAttribute(String attributeName) {
        request.getSession().removeAttribute(attributeName);
    }

    public Object getSessionAttribute(String parameterName) {
        return request.getSession().getAttribute(parameterName);
    }

    public void setSessionAttribute(String attributeName, Object attributeValue) {
        request.getSession().setAttribute(attributeName, attributeValue);
    }

    public String getRequestPath() {
        if (request == null) {
            return null;
        }
        StringBuffer requestURL = request.getRequestURL();
        if (requestURL == null) {
            return null;
        }
        return requestURL.toString();
    }

    public Session getHibernate() {
        return hibernate;
    }

    public void setHibernate(Session hibernate) {
        this.hibernate = hibernate;
    }

    public void newTx() {
        SessionFactory factory = hibernate.getSessionFactory();
        if (hibernate.isOpen() && hibernate.getTransaction().isActive()) {
            hibernate.getTransaction().commit();
        }
        hibernate = factory.openSession();
        hibernate.beginTransaction();
    }

    public UserDetails getUserDetails() {
        return user;
    }

    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    /**
     * Set the menu for the view
     * @param userMenus
     */
    public void setMenu(List<MenuItem> userMenus) {
        this.menu = userMenus;
    }

    /**
     * The menu for the view, based on the user
     * @return the menu
     */
    public List<MenuItem> getMenu() {
        return menu;
    }

}
