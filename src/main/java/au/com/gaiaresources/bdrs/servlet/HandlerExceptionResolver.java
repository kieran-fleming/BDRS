package au.com.gaiaresources.bdrs.servlet;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class HandlerExceptionResolver implements org.springframework.web.servlet.HandlerExceptionResolver {
    @Autowired
    private EmailService emailService;
    private Logger logger = Logger.getLogger(getClass());

    @Override
    public ModelAndView resolveException(HttpServletRequest request,
                                         HttpServletResponse response, Object handler, Exception ex) {
        ModelAndView mv;
        if(ex instanceof AccessDeniedException) {

            if(request.getRemoteUser() == null) {
                // Go to the login page
                mv = new ModelAndView(new RedirectView("/home.htm", true));
                mv.addObject("signin", true);
            } else {
                // Go to the home page
                mv = new ModelAndView(new RedirectView("/authenticated/redirect.htm", true));
            }

        } else {

            logger.error("An unhandled error occured", ex);

            // Create a pretty version of the exception.
            String exceptionText = StringUtils.prettyPrintThrowable(ex);

            // Send the e-mail
            Map<String, Object> params = new HashMap<String, Object>();
           
            Map<String, String> parameters = new HashMap<String, String>();
            Enumeration en = request.getParameterNames();
            while (en.hasMoreElements()) {
            	String key = en.nextElement().toString();
            	String[] values = request.getParameterValues(key);
            	parameters.put(key, Arrays.toString(values));
            }
            params.put("parameters", parameters);
            
            Map<String, String> headers = new HashMap<String, String>();
            en = request.getHeaderNames();
            while (en.hasMoreElements()) {
            	String key = en.nextElement().toString();
            	headers.put(key, request.getHeader(key).toString());
            }
            params.put("headers", headers.toString());
            
            params.put("stacktrace", exceptionText);
            params.put("requesturi", request.getRequestURI());
            params.put("userName", request.getRemoteUser());
            try {
                emailService.sendMessage(emailService.getErrorToAddress(), "Unhandled Error", "UnhandledError.vm",
                                         params);
            } catch (Throwable t) {
                logger.error("Failed to send error e-mail.", t);
                logger.error("RequestURI : " + request.getRequestURI());
                logger.error("Headers : " + headers.toString());
                logger.error("Parameters : " + parameters.toString());
            }

            mv = new ModelAndView(new RedirectView("/error/500.htm", true));
        }
        return mv;
    }
}
