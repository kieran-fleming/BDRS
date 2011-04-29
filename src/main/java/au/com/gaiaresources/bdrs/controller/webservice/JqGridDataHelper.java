package au.com.gaiaresources.bdrs.controller.webservice;

import javax.servlet.http.HttpServletRequest;

import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;

public class JqGridDataHelper {
    
    public static final String MAX_PER_PAGE_PARAM = "rows";
    public static final String REQUESTED_PAGE_PARAM = "page";
    // the parameter to sort by
    public static final String SORT_IDX_PARAM = "sidx";
    // the sort order 
    public static final String SORT_ORDER_PARAM = "sord";
    
    private static final Integer DEFAULT_MAX_PER_PAGE = 10;
    
    public static final String ASC = "asc";
    public static final String DESC = "desc";
    
    private Integer start;
    private Integer maxPerPage;
    private Integer requestedPage;
    
    // empty for now, might add some things to handle sorting later...
    // pass expected column names or something in the ctor...
    public JqGridDataHelper(HttpServletRequest request) throws Exception {
        String maxPerPageArg = request.getParameter(MAX_PER_PAGE_PARAM);
        maxPerPage = StringUtils.hasLength(maxPerPageArg) ? Integer.parseInt(maxPerPageArg)
                : DEFAULT_MAX_PER_PAGE;
        
        String pnArg = request.getParameter(REQUESTED_PAGE_PARAM);
        requestedPage = StringUtils.hasLength(pnArg) ? Integer.parseInt(pnArg)
                : 1;
        if (requestedPage < 1) {
            throw new Exception("Cannot have page number argument less than 1");
        }
        start = (requestedPage - 1) * maxPerPage;
    }
    
    public PaginationFilter createFilter(HttpServletRequest request) throws Exception {
        PaginationFilter filter = new PaginationFilter(start, maxPerPage);
        // add functionality for sorting stuff here....
        
        if (StringUtils.hasLength(request.getParameter(SORT_IDX_PARAM))
                && StringUtils.hasLength(request.getParameter(SORT_ORDER_PARAM))) {
            String sortArg = request.getParameter(SORT_IDX_PARAM);
            String sortOrder = request.getParameter(SORT_ORDER_PARAM);
            filter.addSortingCriteria(sortArg, SortOrder.fromString(sortOrder));
        }
        
        return filter;
    }
    
    public Integer getStart() {
        return start;
    }
    
    public Integer getMaxPerPage() {
        return maxPerPage;
    }
    
    public Integer getRequestedPage() {
        return requestedPage;
    }
}
