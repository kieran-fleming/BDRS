package au.com.gaiaresources.bdrs.displaytag;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.jsp.PageContext;

import org.apache.log4j.Logger;
import org.displaytag.util.RequestHelper;
import org.displaytag.util.RequestHelperFactory;

public class BdrsRequestHelperFactory implements RequestHelperFactory {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Override
    public RequestHelper getRequestHelperInstance(PageContext context) {
        return new BdrsRequestHelper((HttpServletRequest) context.getRequest(),
                (HttpServletResponse) context.getResponse());
    }

}
