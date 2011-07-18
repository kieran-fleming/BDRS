package au.com.gaiaresources.bdrs.controller.admin;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.DisplayTagHelper;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

@Controller
public class AdminUserSearchController extends AbstractController {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    // need to change this from just 'name' as maven filtering was giving us conflicts
    public static final String OUTPUT_ARG_NAME = "userName";
    public static final String EMAIL = "emailAddress";
    public static final String FULL_NAME = "FULL_NAME";
    //public static final String SORT = "SORT";
    public static final String MAX_PER_PAGE = "MAX_PER_PAGE";
    
    
    
    // this can't change unless the model changes
    public static final String INPUT_ARG_NAME = "name";

    public static final Integer EXPECTED_NUM_SORT_PARAM = 2;

    public static final Integer SORT_ARG = 0;
    public static final Integer SORT_ORDER = 1;

    // "1" and "2" is returned by displaytag for ASC and DESC so...
    public static final String ASC_ORDER = "1";
    public static final String DESC_ORDER = "2";

    public static final String SEARCH = "search";

    public static final String TableID = "usersearchresults";

    public static final String PAGED_USER_RESULT = "pagedUserResult";

    public static final Integer DEFAULT_MAX_PER_PAGE = 10;

    @Autowired
    private UserDAO userDAO;

    private static ParamEncoder paramEncoder = new ParamEncoder(TableID);

    public static String getPageNumberParamName() {
        return paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_PAGE);
    }

    public static String getSortParamName() {
        return paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_SORT);
    }

    public static String getOrderParamName() {
        return paramEncoder.encodeParameterName(TableTagParameters.PARAMETER_ORDER);
    }

    @RequestMapping(value = "/admin/userSearch.htm", method = RequestMethod.GET)
    public ModelAndView searchUsers(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        if (!StringUtils.hasLength(request.getParameter(SEARCH))) {
            // just return the form to allow searching
            ModelAndView mav = new ModelAndView("adminUserSearch");
            return mav;
        }

        // must be "name" as this is the getter/setter for the login in the model.
        String username = request.getParameter(INPUT_ARG_NAME);
        String email = request.getParameter(EMAIL);
        String fullname = request.getParameter(FULL_NAME);
        
        //PaginationFilter filter = new DisplayTagHelper(TableID).createFilter(request);
        //if (StringUtils.hasLength(req))
        PaginationFilter filter = StringUtils.hasLength(request.getParameter(MAX_PER_PAGE)) ?
                new DisplayTagHelper(TableID).createFilter(request, Integer.parseInt(request.getParameter(MAX_PER_PAGE))) :
                    new DisplayTagHelper(TableID).createFilter(request);

        User u = this.getRequestContext().getUser();
        String[] allowedRolesToSearchFor = Role.getRolesLowerThanOrEqualTo(Role.getHighestRole(u.getRoles()));
        String[] rolesToExclude = Role.getRolesHigherThan(Role.getHighestRole(u.getRoles()));

        PagedQueryResult<User> queryResult = userDAO.search(username, email, fullname, filter, allowedRolesToSearchFor, rolesToExclude);

        ModelAndView mav = new ModelAndView("adminUserSearch");
        mav.addObject(PAGED_USER_RESULT, queryResult);

        // pass back the search parameters...
        mav.addObject(OUTPUT_ARG_NAME, username);
        mav.addObject(EMAIL, email);
        mav.addObject(FULL_NAME, fullname);

        return mav;
    }
}
