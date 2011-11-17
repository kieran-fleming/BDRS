package au.com.gaiaresources.bdrs.controller;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.mock.web.MockHttpServletResponse;
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
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerExecutionChain;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.servlet.Interceptor;
import au.com.gaiaresources.bdrs.servlet.RecaptchaInterceptor;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;


@Transactional
public abstract class AbstractControllerTest extends
    AbstractTransactionalTest {

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    @Qualifier(value = "org.springframework.security.authenticationManager")
    protected ProviderManager authProviderManager;
    @Autowired
    protected UserDetailsService authenticationService;
    @Autowired
    protected UserDAO userDAO;
    @Autowired
    protected PortalDAO portalDAO;

    protected MockHttpServletResponse response;

    private ModelAndView mv;
    private Object controller;
    private HandlerInterceptor[] interceptors;
    

    @BeforeTransaction
    public final void beforeTx() throws Exception {
        response = new MockHttpServletResponse();

        // Override the security provider.
        List<TestingAuthenticationProvider> providerList = new ArrayList<TestingAuthenticationProvider>();
        providerList.add(new TestingAuthenticationProvider());
        authProviderManager.setProviders(providerList);

        // The following block would normally be done by the interceptor.
        RequestContext c = RequestContextHolder.getContext();
        request.setAttribute(RequestContext.REQUEST_CONTEXT_SESSION_ATTRIBUTE_KEY, c);
        SecurityContext securityContext = SecurityContextHolder.getContext();
        if ((securityContext.getAuthentication() != null)
                && (securityContext.getAuthentication().getPrincipal() instanceof UserDetails)) {
            c.setUserDetails((au.com.gaiaresources.bdrs.security.UserDetails) securityContext.getAuthentication().getPrincipal());
        }
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

    @AfterTransaction
    public final void afterTx() throws Exception {
        try {
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
        } finally {
            // Normally done by the interceptor.
            RequestContextHolder.clear();
            // equivalent to logging out
            SecurityContextHolder.clearContext();
        }
    }

    private boolean handleInterceptor(HandlerInterceptor interceptor) {
        return !(interceptor instanceof Interceptor)
                && !(interceptor instanceof RecaptchaInterceptor);
    }
    
    protected void assertNotRedirect(ModelAndView mav) {
        Assert.assertFalse("should not be redirect view", mav.getView() instanceof RedirectView);
    }
    
    protected void assertViewName(ModelAndView mav, String viewName) {
        assertNotRedirect(mav);
        Assert.assertNotNull("model and view should not be null", mav);
        Assert.assertEquals("view name does not match", viewName, mav.getViewName());
    }
    
    protected void assertRedirect(ModelAndView mav, String url) {
        Assert.assertTrue("should be redirect view", mav.getView() instanceof RedirectView);
        RedirectView view = (RedirectView)mav.getView();
        Assert.assertEquals("assert redirect url", url, view.getUrl());
    }
    
    protected void assertRedirectAndErrorCode(ModelAndView mav, String url, String errorCode) {
        assertRedirect(mav, url);
        assertMessageCode(errorCode);
    }
    
    /**
     * Checks whether the code exists in the request context
     * @param code - the string code to check for
     */
    protected void assertMessageCode(String code) {
        List<String> msgCodes = getRequestContext().getMessageCodes();
        Assert.assertTrue("Expect error key '" + code + "' in context", listContains(msgCodes, code));
    }
    
    private boolean listContains(List<String> list, String str) {
        for (String ls : list) {
            if (ls.equals(str)) {
                return true;
            }
        }
        return false;
    }
}