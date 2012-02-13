package au.com.gaiaresources.bdrs.controller.admin;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.displaytag.tags.TableTagParameters;
import org.displaytag.util.ParamEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

@RolesAllowed({ Role.ADMIN, Role.SUPERVISOR })
@Controller
public class EditUsersController extends AbstractController {
    
    public static final String USER_LISTING_URL = "/admin/userSearch.htm";

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
       
    public static final String APPROVE_USER_WEBSERVICE_URL = "/admin/approveUser.htm";
    public static final String PARAM_USER_PK = "userPk";

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private RegistrationService regService;

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

    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = USER_LISTING_URL, method = RequestMethod.GET)
    public ModelAndView searchUsers(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        return new ModelAndView("editUsers");
    }
    
    @RolesAllowed({Role.ADMIN, Role.ROOT, Role.POWERUSER, Role.SUPERVISOR})
    @RequestMapping(value = APPROVE_USER_WEBSERVICE_URL, method = RequestMethod.POST)
    public void approveUser(HttpServletRequest request, HttpServletResponse response, 
            @RequestParam(value=PARAM_USER_PK, required=true) int userPk) {
        
        User u = userDAO.getUser(userPk);
        if (u == null) {
            throw new NullPointerException("User returned by id is null : " + userPk);
        }
        u.setActive(true);
        userDAO.updateUser(u);

        // notify user that their account is active
        regService.notifyUserAccountActive(u, RequestContextHolder.getContext().getPortal());
    }
}
