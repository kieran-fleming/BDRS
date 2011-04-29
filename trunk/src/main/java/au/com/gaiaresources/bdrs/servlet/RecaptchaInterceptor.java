package au.com.gaiaresources.bdrs.servlet;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.RecaptchaController;

public class RecaptchaInterceptor extends HandlerInterceptorAdapter {
    private static final String RECAPTCHA_PERFORMED_SESSION_ATTRIBUTE = "climatewatch.recaptcha.performed";
    private static final String REDIRECT_TO_AFTER_RECAPTCHA_SESSION_ATTRIBUTE = "climatewatch.after.recaptcha.redirect";
    private Logger log = Logger.getLogger(getClass());

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler.getClass().getAnnotation(RecaptchaProtected.class) != null) {
            HttpSession s = request.getSession();
            Object recaptchaPerformed = s.getAttribute(RECAPTCHA_PERFORMED_SESSION_ATTRIBUTE);
            if (recaptchaPerformed == null) {
                String r = request.getRequestURL().toString();
                String redirect = r.substring(0, r.indexOf(request.getServletPath())) + "/recaptcha.htm";
                response.sendRedirect(redirect);
                s.setAttribute(REDIRECT_TO_AFTER_RECAPTCHA_SESSION_ATTRIBUTE, r);
                return false;
            }
        }
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           ModelAndView modelAndView) throws Exception 
    {
        if (handler instanceof RecaptchaController) {
            if (modelAndView.getModel().containsKey("success")) {
                RedirectView rv = null;
                Boolean result = (Boolean) modelAndView.getModel().get("success");
                String redirectTo = null;
                if (result) {
                    request.getSession().setAttribute(RECAPTCHA_PERFORMED_SESSION_ATTRIBUTE, Boolean.TRUE);
                    redirectTo = (String) request.getSession().getAttribute(REDIRECT_TO_AFTER_RECAPTCHA_SESSION_ATTRIBUTE);
                    if (redirectTo == null || redirectTo.length() == 0) {
                        rv = new RedirectView("/home.htm", true);
                    } else {
                        rv = new RedirectView(redirectTo, false);
                    }
                } else {
                    rv = new RedirectView("/recaptcha.htm", true);
                }
                modelAndView.getModel().clear();
                modelAndView.setView(rv);
            }
        }
    }
}
