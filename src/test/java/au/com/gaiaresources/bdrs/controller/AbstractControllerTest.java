package au.com.gaiaresources.bdrs.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.TestingAuthenticationProvider;
import org.springframework.security.authentication.TestingAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.GrantedAuthorityImpl;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.db.FilterManager;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.servlet.Interceptor;
import au.com.gaiaresources.bdrs.servlet.RecaptchaInterceptor;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = {
        "file:src/main/webapp/WEB-INF/climatewatch.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-hibernate.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-hibernate-datasource-test.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-security.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-daos.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-email.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-servlet.xml",
        "file:src/main/webapp/WEB-INF/climatewatch-profileConfig-test.xml"})
@Transactional
public abstract class AbstractControllerTest extends
        AbstractTransactionalJUnit4SpringContextTests {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    protected SessionFactory sessionFactory;
    @Autowired
    @Qualifier(value = "org.springframework.security.authenticationManager")
    protected ProviderManager authProviderManager;
    @Autowired
    protected UserDetailsService authenticationService;
    @Autowired
    protected UserDAO userDAO;
    @Autowired
    protected PortalDAO portalDAO;

    protected MockHttpServletRequest request;
    protected MockHttpServletResponse response;
    protected MockHttpSession session;

    private ModelAndView mv;
    private Object controller;
    private HandlerInterceptor[] interceptors;

    @Before
    public void primeDatabase() {
        try {
            Portal portal = new PortalInitialiser().initRootPortal();

            RequestContext c = RequestContextHolder.getContext();
            c.setPortal(portal);
            Session sesh = c.getHibernate();
            FilterManager.setPortalFilter(sesh, portal);

            //if (c.getUser() != null) {
                // rebind the user in the context to the current session.
            //    ((au.com.gaiaresources.bdrs.security.UserDetails) c.getUserDetails()).setUser(userDAO.getUser(c.getUser().getId()));
            //}
            //if (c.getPortal() != null) {
            //    c.setPortal(portalDAO.getPortal(c.getPortal().getId()));
            //}
        } catch (Exception e) {
            log.error("db setup error", e);
        }
    }

    @BeforeTransaction
    public void beforeTx() throws Exception {
        request = createMockHttpServletRequest();
        response = new MockHttpServletResponse();
        session = new MockHttpSession();

        // Override the security provider.
        List<TestingAuthenticationProvider> providerList = new ArrayList<TestingAuthenticationProvider>();
        providerList.add(new TestingAuthenticationProvider());
        authProviderManager.setProviders(providerList);

        // The following block would normally be done by the interceptor.
        RequestContext c = new RequestContext(request, applicationContext);
        request.setAttribute(RequestContext.REQUEST_CONTEXT_SESSION_ATTRIBUTE_KEY, c);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if ((securityContext.getAuthentication() != null)
                && (securityContext.getAuthentication().getPrincipal() instanceof UserDetails)) {
            c.setUserDetails((au.com.gaiaresources.bdrs.security.UserDetails) securityContext.getAuthentication().getPrincipal());
        }

        RequestContextHolder.set(c);
        c.setHibernate(sessionFactory.getCurrentSession());
        sessionFactory.getCurrentSession().beginTransaction();
    }

    protected RequestContext getRequestContext() {
        return RequestContextHolder.getContext();
    }

    protected void login(String username, String password, String[] roles)
            throws Exception {
        List<GrantedAuthority> grantedAuth = new ArrayList<GrantedAuthority>(
                roles.length);
        for (String role : roles) {
            grantedAuth.add(new GrantedAuthorityImpl(role));
        }

        SecurityContextImpl secureContext = new SecurityContextImpl();
        TestingAuthenticationToken token = new TestingAuthenticationToken(
                username, password, grantedAuth);
        secureContext.setAuthentication(token);
        SecurityContextHolder.setContext(secureContext);
        UserDetails userDetails = authenticationService.loadUserByUsername(username);
        RequestContextHolder.getContext().setUserDetails(userDetails);
    }

    protected ModelAndView handle(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        final HandlerMapping handlerMapping = applicationContext.getBean(HandlerMapping.class);
        final HandlerAdapter handlerAdapter = applicationContext.getBean(HandlerAdapter.class);
        final HandlerExecutionChain handler = handlerMapping.getHandler(request);
        Assert.assertNotNull("No handler found for request, check you request mapping", handler);

        controller = handler.getHandler();
        // if you want to override any injected attributes do it here

        interceptors = handlerMapping.getHandler(request).getInterceptors();
        for (HandlerInterceptor interceptor : interceptors) {
            if (handleInterceptor(interceptor)) {
                final boolean carryOn = interceptor.preHandle(request, response, controller);
                if (!carryOn) {
                    return null;
                }
            }
        }
        mv = handlerAdapter.handle(request, response, controller);
        
        return mv;
    }

    protected Object getController(HttpServletRequest request) throws Exception {
        final HandlerMapping handlerMapping = applicationContext.getBean(HandlerMapping.class);
        final HandlerExecutionChain handler = handlerMapping.getHandler(request);
        Assert.assertNotNull("No handler found for request, check you request mapping", handler);
        return handler.getHandler();
    }

    /**
     * This function should be overriden by tests taht require a multipart
     * request.
     * 
     * @return
     */
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockHttpServletRequest();
    }

    @AfterTransaction
    public void afterTx() throws Exception {
        sessionFactory.getCurrentSession().getTransaction().rollback();
        //sessionFactory.getCurrentSession().getTransaction().commit();

        HandlerInterceptor interceptor;
        if (interceptors != null) {
            for (int i = interceptors.length - 1; i > -1; i--) {
                interceptor = interceptors[i];
                if (handleInterceptor(interceptor)) {
                    interceptor.postHandle(request, response, controller, mv);
                }
            }

            Exception viewException = null;
            for (int i = interceptors.length - 1; i > -1; i--) {
                interceptor = interceptors[i];
                if (handleInterceptor(interceptor)) {
                    interceptor.afterCompletion(request, response, controller, viewException);
                }
            }
        }
        // Normally done by the interceptor.
        RequestContextHolder.clear();
    }

    private boolean handleInterceptor(HandlerInterceptor interceptor) {
        return !(interceptor instanceof Interceptor)
                && !(interceptor instanceof RecaptchaInterceptor);
    }
}