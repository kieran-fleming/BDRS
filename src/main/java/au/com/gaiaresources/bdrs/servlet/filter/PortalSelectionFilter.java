package au.com.gaiaresources.bdrs.servlet.filter;

import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.openid4java.util.HttpResponse;
import org.springframework.beans.factory.config.AutowireCapableBeanFactory;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class PortalSelectionFilter implements Filter {
    public static final String PORTAL_ID_KEY = "portalId";
    public static final String DEFAULT_REDIRECT_URL = "/authenticated/redirect.htm";
    
    public static final String RESTFUL_PORTAL_PATTERN_STR = "^(/portal/){1}(\\d+)(/{1}|$)";

    private Logger log = Logger.getLogger(getClass());

    private PortalDAO portalDAO;
    private SessionFactory sessionFactory;
    private WebApplicationContext webApplicationContext;
    private PortalSelectionFilterMatcher portalMatcher;

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
            FilterChain chain) throws IOException, ServletException {
        // We can only add a session variable to a HttpServletRequest
        if (request instanceof HttpServletRequest) {

            HttpServletRequest httpRequest = (HttpServletRequest) request;
            
            // Retrieve the stored RequestContext from the session attribute
            // and store it in the RequestContextHolder. It is very important
            // to do this before creating any Hibernate Sessions because
            // the hibernate session will use the RequestContext in the 
            // RequestContextHolder to retrieve the Portal ID and enable and
            // configure the portal filter.
            RequestContext requestContext = (RequestContext) httpRequest.getAttribute(RequestContext.REQUEST_CONTEXT_SESSION_ATTRIBUTE_KEY);
            if (requestContext == null) {
                requestContext = new RequestContext(httpRequest,
                        webApplicationContext);
                request.setAttribute(RequestContext.REQUEST_CONTEXT_SESSION_ATTRIBUTE_KEY, requestContext);
            }
            RequestContextHolder.set(requestContext);

            boolean rollbackRequired = true;
            Session sesh;
            Transaction tx;
            if (sessionFactory.getCurrentSession().getTransaction().isActive()) {
                // This section will run if we are unit testing.
                sesh = sessionFactory.getCurrentSession();
                tx = sesh.getTransaction();
                rollbackRequired = false;
            } else {
                // This section will run under normal conditions.
                sesh = sessionFactory.openSession();
                tx = sesh.beginTransaction();
                rollbackRequired = true;
            }
            
            Object rawPortalId = httpRequest.getSession().getAttribute(PORTAL_ID_KEY);
            String url = httpRequest.getRequestURL().toString();
            
            // Test if the servlet path has the form "/portal/<portal_pk>/.../..."
            Pattern restfulPortalPattern = Pattern.compile(RESTFUL_PORTAL_PATTERN_STR);
            Matcher servletPathMatcher = restfulPortalPattern.matcher(httpRequest.getServletPath());
            
            if(servletPathMatcher.find()) {
                int portalPk = Integer.parseInt(servletPathMatcher.group(2));
                
                Portal portal = portalDAO.getPortal(sesh, portalPk);
                
                httpRequest.getSession().setAttribute(PORTAL_ID_KEY, portal.getId());
                requestContext.setPortal(portal);
                
                if(rawPortalId != null &&
                        !portal.getId().equals(new Integer(rawPortalId.toString()))) {
                    httpRequest.getSession().invalidate();
                    String queryString = httpRequest.getQueryString();
                    String redirect = url + (queryString != null ? "?"+queryString : "");
                    sendRedirect(response, redirect);
                }
            } else {
                List<Portal> portalList = portalDAO.getPortals(sesh);
                if (!portalList.isEmpty() && !response.isCommitted()) {
    
                    PortalMatches matches = portalMatcher.match(sesh, url);
                    Portal defaultPortal = matches.getDefaultPortal();
                    Portal matchedPortal = matches.getMatchedPortal();
                    PortalEntryPoint matchedEntryPoint = matches.getMatchedEntryPoint();
    
                    // For each portal, test the entry points.
                    if (defaultPortal == null) {
                        log.debug("No default Portal. Treating first portal as default.");
                        defaultPortal = portalList.get(0);
                    }
    
                    if (matchedPortal == null) {
                        if (rawPortalId == null) {
                            log.debug("URL does not match any known portal entry pattern. Using default portal.");
                            matchedPortal = defaultPortal;
                        } else {
                            // The Portal ID has been set so there is nothing left to do.
                            requestContext.setPortal(portalDAO.getPortal(sesh, new Integer(
                                    rawPortalId.toString())));
                            matchedPortal = null;
                        }
                    } else {
                        // Assume that they are logged into Portal 1 and type in the URL
                        // that matches Portal 2, we want to log them out of Portal 1
                        // and redirect them to their desired URL which will take them
                        // to Portal 2.
                        if (rawPortalId != null
                                && !matchedPortal.getId().equals(new Integer(
                                        rawPortalId.toString()))) {
                            httpRequest.getSession().invalidate();
                            sendRedirect(response, url);
                        }
                        // else set the Portal ID for the matched portal (below)
                    }
    
                    // If we have not already decided to perform a redirect, and
                    // we have a matched portal.
                    if (!response.isCommitted() && matchedPortal != null) {
                        // Set the portalID session attribute
                        httpRequest.getSession().setAttribute(PORTAL_ID_KEY, matchedPortal.getId());
                        requestContext.setPortal(matchedPortal);
    
                        // Redirect for the matched portal if needed.
                        if (matchedEntryPoint != null) {
                            String redirect;
                            if (matchedEntryPoint.getRedirect().isEmpty()) {
                                redirect = httpRequest.getContextPath()
                                        + DEFAULT_REDIRECT_URL;
                            } else {
                                redirect = matchedEntryPoint.getRedirect();
                            }
                            sendRedirect(response, redirect);
                        } 
                        // otherwise fall through to the requested url
                        // if it did not exactly match a portal entry point.
    
                    }
    
                } else {
                    log.error("No portals defined. Unable to set Portal ID");
                }
            }
            if (rollbackRequired) {
                tx.rollback();
                sesh.close();
            }  
        } else {
            log.error("Unsupported request type: " + request.getClass());
            log.error("Not setting Portal ID");
            // Otherwise pass through?
        }

        // The response may be committed if a redirect has been set. If so
        // there is no point progressing with the filter chain. Your fate
        // has been sealed.
        if (!response.isCommitted()) {
            chain.doFilter(request, response);
        }
    }

    private void sendRedirect(ServletResponse response, String url)
            throws IOException {
        if (response instanceof HttpServletResponse) {
            HttpServletResponse httpResponse = ((HttpServletResponse) response);
            httpResponse.sendRedirect(url);
        } else {
            log.error(String.format("Unable to redirect to \"%s\" because the response is a \"%s\" and not a \"%s\"", url, response.getClass().toString(), HttpServletResponse.class.toString()));
        }
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

        ServletContext servletContext = filterConfig.getServletContext();
        webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);
        AutowireCapableBeanFactory autowireCapableBeanFactory = webApplicationContext.getAutowireCapableBeanFactory();
        portalDAO = autowireCapableBeanFactory.getBean(PortalDAO.class);
        sessionFactory = autowireCapableBeanFactory.getBean(SessionFactory.class);
        
        portalMatcher = new PortalSelectionFilterMatcher(portalDAO);
    }
}
