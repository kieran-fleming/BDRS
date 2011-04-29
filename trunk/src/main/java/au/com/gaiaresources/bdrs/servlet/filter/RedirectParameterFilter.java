package au.com.gaiaresources.bdrs.servlet.filter;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

public class RedirectParameterFilter implements Filter {

    Logger log = Logger.getLogger(RedirectParameterFilter.class);

    @Override
    public void destroy() {
            // TODO Auto-generated method stub

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                    FilterChain chain) throws IOException, ServletException {
            log.debug("handleRequest");
            if (request instanceof HttpServletRequest) {
                    if (request.getParameter("login-redirect") != null) {
                            ((HttpServletRequest) request).getSession().setAttribute(
                                            "login-redirect",
                                            request.getParameter("login-redirect"));
                    }
                    if (request.getParameter("redirecturl") != null) {
                            ((HttpServletRequest) request).getSession().setAttribute(
                                            "redirecturl",
                                            request.getParameter("redirecturl"));
                    }
                    if (request.getParameter("species") != null) {
                            ((HttpServletRequest) request).getSession().setAttribute(
                                            "species",
                                            request.getParameter("species"));
                    }
            }
            chain.doFilter(request, response);
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
            // TODO Auto-generated method stub

    }

}
