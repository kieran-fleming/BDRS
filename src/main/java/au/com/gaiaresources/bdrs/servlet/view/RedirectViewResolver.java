package au.com.gaiaresources.bdrs.servlet.view;

import java.util.Locale;

import org.springframework.core.Ordered;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.view.RedirectView;

public class RedirectViewResolver implements ViewResolver, Ordered {

    // Uses this prefix to avoid interference with the default behaviour
    public static final String REDIRECT_URL_PREFIX = "redirectWithoutModel:";     
    // Have a highest priority by default
    private int order = Integer.MIN_VALUE; 

    /**
     * Used by spring to parse the redirection URL and instantiate the 
     * RedirectView appropriately.
     * 
     */
    public View resolveViewName(String viewName, Locale arg1) throws Exception {
        if (viewName.startsWith(REDIRECT_URL_PREFIX)) {
            String redirectUrl = viewName.substring(REDIRECT_URL_PREFIX.length());
            return new RedirectView(redirectUrl, true, true, false);
        }
        return null;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }
}