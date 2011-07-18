package au.com.gaiaresources.bdrs.controller;

import javax.servlet.http.HttpServletRequest;

import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;

public class DisplayTagHelper {

    private ParamEncoder paramEncoder;
    
    // "1" and "2" is returned by displaytag for ASC and DESC so...
    public static final String ASC_ORDER = "1";
    public static final String DESC_ORDER = "2";
    
    public static final Integer DEFAULT_MAX_PER_PAGE = 10;
    
    public DisplayTagHelper(String tableId) {
        paramEncoder = new ParamEncoder(tableId);
    }
    
    public String getPageNumberParamName() {
        return paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
    }

    public String getSortParamName() {
        return paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_SORT);
    }

    public String getOrderParamName() {
        return paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER);
    }
    
    public PaginationFilter createFilter(HttpServletRequest request, int maxPerPage) throws Exception {
        String pnArg = request.getParameter(getPageNumberParamName());

        Integer pageNum = StringUtils.hasLength(pnArg) ? Integer.parseInt(pnArg)
                : 1;
        if (pageNum < 1) {
            throw new Exception("Cannot have page number argument less than 1");
        }
        Integer start = (pageNum - 1) * maxPerPage;

        PaginationFilter filter = new PaginationFilter(start, maxPerPage);

        if (StringUtils.hasLength(request.getParameter(getSortParamName()))
                && StringUtils.hasLength(request.getParameter(getOrderParamName()))) {
            String sortArg = request.getParameter(getSortParamName());
            String sortOrder = request.getParameter(getOrderParamName());

            if (!sortOrder.equals(ASC_ORDER) && !sortOrder.equals(DESC_ORDER)) {
                throw new Exception(
                        "Must indicate \"1\" or \"2\" for sort argument");
            }

            filter.addSortingCriteria(sortArg, SortOrder.fromString(sortOrder));
        }
        return filter;
    }
    
    public PaginationFilter createFilter(HttpServletRequest request) throws Exception { 
        return createFilter(request, DEFAULT_MAX_PER_PAGE);
    }
}
