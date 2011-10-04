package au.com.gaiaresources.bdrs.controller;

import org.springframework.mock.web.MockHttpServletRequest;

/**
 * For overriding a single method which is breaking my tests.
 * 
 * @author aaron
 *
 */
public class BdrsMockHttpServletRequest extends MockHttpServletRequest {
    
    /**
     * does the same thing as the superclass but adds the context path onto the request url.
     */
    @Override
    public StringBuffer getRequestURL() {
        StringBuffer url = new StringBuffer(getScheme());
        url.append("://").append(getServerName()).append(':').append(getServerPort()).append('/').append(getContextPath());
        url.append(getRequestURI());
        return url;
    }
}
