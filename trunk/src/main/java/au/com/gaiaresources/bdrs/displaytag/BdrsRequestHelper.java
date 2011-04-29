package au.com.gaiaresources.bdrs.displaytag;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.displaytag.util.DefaultHref;
import org.displaytag.util.DefaultRequestHelper;
import org.displaytag.util.Href;
import org.displaytag.util.RequestHelper;

public class BdrsRequestHelper implements RequestHelper {
    private HttpServletRequest request;
    private HttpServletResponse response;

    private DefaultRequestHelper defaultHelper;

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    /**
     * Construct a new RequestHelper for the given request.
     * 
     * @param servletRequest
     *            HttpServletRequest needed to generate the base href
     * @param servletResponse
     *            HttpServletResponse needed to encode generated urls
     */
    public BdrsRequestHelper(HttpServletRequest servletRequest,
            HttpServletResponse servletResponse) {
        defaultHelper = new DefaultRequestHelper(servletRequest,
                servletResponse);
        request = servletRequest;
        response = servletResponse;
    }

    // needed to change this from the default implementation as it was returning the url of the
    // vanilla template instead of the actual requested url.
    @Override
    public Href getHref() {
        // using empty base url to use relative paths to get around the problem
        Href href = new DefaultHref(this.response.encodeURL(""));
        href.setParameterMap(getParameterMap());
        return href;
    }

    @Override
    public Integer getIntParameter(String arg0) {
        return defaultHelper.getIntParameter(arg0);
    }

    @Override
    public String getParameter(String arg0) {
        return defaultHelper.getParameter(arg0);
    }

    @Override
    public Map getParameterMap() {
        return defaultHelper.getParameterMap();
    }
}
