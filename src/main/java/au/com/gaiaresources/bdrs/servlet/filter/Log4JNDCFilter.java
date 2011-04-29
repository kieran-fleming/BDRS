package au.com.gaiaresources.bdrs.servlet.filter;

import java.io.IOException;
import java.security.Principal;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.NDC;
import org.springframework.web.filter.OncePerRequestFilter;

public class Log4JNDCFilter extends OncePerRequestFilter {
    /**
     * Forwards the request to the next filter in the chain and delegates
     * down to the subclasses to perform the actual request logging both
     * before and after the request is processed.
     */
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
                                    throws ServletException, IOException 
    {
        NDC.push(getNestedDiagnosticContextMessage(request));
        try {
            filterChain.doFilter(request, response);
        }
        finally {
            NDC.pop();
            if (NDC.getDepth() == 0) {
                NDC.remove();
            }
        }
    }
    
    /**
     * Determine the message to be pushed onto the Log4J nested diagnostic context.
     * @param request current HTTP request
     * @return the message to be pushed onto the Log4J NDC
     */
    protected String getNestedDiagnosticContextMessage(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Principal principal = request.getUserPrincipal();
        if (principal != null) {
            return principal.getName();
        }
        if (session != null) {
            return "Session: " + session.getId();
        } else {
            return "Thread: " + Thread.currentThread().getName();
        }
    }
}
