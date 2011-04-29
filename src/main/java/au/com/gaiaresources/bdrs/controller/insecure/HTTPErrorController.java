package au.com.gaiaresources.bdrs.controller.insecure;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.email.EmailService;


@Controller
public class HTTPErrorController {
    @Autowired
    private EmailService emailService;
    private Logger log = Logger.getLogger(getClass());

    @RequestMapping(value = "/error/redirect404.htm", method = RequestMethod.GET)
    public ModelAndView handleRedirect404() {
        // When a 404 error occurs, the container will redirect to this url.
        // This function will perform any error logging required, then
        // redirect to the proper 404 page. The redirect is performed in order
        // to fix the url displayed by the browser.

        ModelAndView view = new ModelAndView(new RedirectView("/error/404.htm", true));
        return view;
    }

    @RequestMapping(value = "/error/redirect500.htm", method = RequestMethod.GET)
    public ModelAndView handleRedirect500(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        log.error("Exception",ex);
        
        // Send the e-mail
        try {
            Map<String, Object> params = new HashMap<String, Object>();
            params.put("stacktrace", "No Stack Trace. Exception caught from web container.");
            params.put("requesturi", request.getHeader("referer"));
            emailService.sendMessage(emailService.getErrorToAddress(), "Unhandled Error", "UnhandledError.vm",
                                     params);
        } catch (Throwable t) {
            
            log.error("Failed to send error e-mail.", t);
        }

        ModelAndView view = new ModelAndView(new RedirectView("/error/500.htm", true));
        return view;
    }

    @RequestMapping(value = "/error/404.htm", method = RequestMethod.GET)
    public ModelAndView handle404(HttpServletRequest req) {
    	ModelAndView view;
    	if ((req.getSession().getAttribute("sessionType") != null) 
    			&& req.getSession().getAttribute("sessionType").equals("mobile")) {
    		view = new ModelAndView("errorMobile");
    	} else {
    		view = new ModelAndView("error");
    	}
        view.addObject("statusCode", 404);
        return view;
    }

    @RequestMapping(value = "/error/500.htm", method = RequestMethod.GET)
    public ModelAndView handle500(HttpServletRequest req) {
    	ModelAndView view;
    	if ((req.getSession().getAttribute("sessionType") != null) 
    			&& req.getSession().getAttribute("sessionType").equals("mobile")) {
    		view = new ModelAndView("errorMobile");
    	} else {
    		view = new ModelAndView("error");
    	}
    	view.addObject("statusCode", 500);
        return view;
    }
}
