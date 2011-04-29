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
import org.springframework.validation.ObjectError;

import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.message.Messages;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.theme.Theme;
import au.com.gaiaresources.bdrs.model.user.User;

public class RequestContext {

    public static final String REQUEST_CONTEXT_SESSION_ATTRIBUTE_KEY = "ClimateWatch-RequestContext";

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

    public RequestContext() {
        // TODO Auto-generated constructor stub
    }

    public RequestContext(HttpServletRequest req,
            ApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
        this.request = req;
        this.session = req.getSession(false);
        if (this.session != null) {
            if (this.session.getAttribute("globalmessages") == null) {
                this.session.setAttribute("globalmessages", new Messages());
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
            if (this.session.getAttribute("globalmessages") == null) {
                this.session.setAttribute("globalmessages", new Messages());
            }
            ((Messages) this.session.getAttribute("globalmessages")).addError(error);
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
        List<String> messageContents = new ArrayList<String>();
        if (this.session != null
                && this.session.getAttribute("globalmessages") != null) {
            for (Object o : ((Messages) this.session.getAttribute("globalmessages")).getAllErrors()) {
                if (o instanceof Message) {
                    Message e = (Message) o;
                    messageContents.add(applicationContext.getMessage(e, request.getLocale()));
                }
            }
            this.session.setAttribute("globalmessages", new Messages());
        }
        return messageContents;
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
        return request.getRequestURL().toString();
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

}
