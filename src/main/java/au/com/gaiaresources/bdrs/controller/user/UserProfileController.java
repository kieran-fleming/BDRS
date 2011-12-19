package au.com.gaiaresources.bdrs.controller.user;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.message.Message;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.user.UserMetaData;
import au.com.gaiaresources.bdrs.service.user.UserMetaDataService;
import au.com.gaiaresources.bdrs.service.user.UserMetaData.UserMetaDataType;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

@Controller
public class UserProfileController extends AbstractController {

    public final static String USER_ID = "USER_ID";
    public final static String USER_ACTIVE = "USER_ACTIVE";
    public final static String FIRST_NAME = "FIRST_NAME";
    public final static String LAST_NAME = "LAST_NAME";
    public final static String USER_NAME = "USER_NAME";
    public final static String EMAIL_ADDR = "EMAIL_ADDR";
    public final static String PASSWORD = "PASSWORD";
    public final static int PASSWORD_MIN_LENGTH = 6;
    public final static int PASSWORD_MAX_LENGTH = 12;
    public final static String USER_INDICATOR = "user";
    public final static String ADMIN_INDICATOR = "admin";
    public final static String EDIT_AS = "editAs";
    public final static String PARAM_CANCEL = "cancel";

    public final static String ERROR_EMAIL_TAKEN = "The email is already taken by another user";
    public final static String ERROR_PASSWORD = "Password must be between 6 and 12 characters";
    public final static String SUCCESS_STANDARD = "Profile details successfully changed";

    public final static String ERROR_EDIT_LOWER = "You cannot lower the roles of another user at or above your level";
    public final static String ERROR_EDIT_PROMOTE = "You cannot increase the roles of another user above your own level";
    public final static String ERROR_EDIT_HIGHER = "You cannot edit a user of a higher role than your own level";
    
    public final static String EDIT_AS_USER_URL = "/user/profile.htm";
    public final static String EDIT_AS_ADMIN_URL = "/admin/profile.htm";
    
    public final static String USER_PROFILE_VIEW = "userProfile";

    @Autowired
    private UserDAO userDAO;
    @Autowired
    private MetadataDAO metadataDAO;
    @Autowired
    private UserMetaDataService umdService;
    @Autowired
    private RegistrationService regService;
    @Autowired
    private RedirectionService redirService;

    Logger log = Logger.getLogger(getClass());

    @RolesAllowed( { Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN, Role.ROOT })
    @RequestMapping(value = EDIT_AS_USER_URL, method = RequestMethod.GET)
    public ModelAndView userRenderForm(HttpServletRequest request)
            throws Exception {

        ModelAndView mv = renderForm(request, getRequestContext().getUser());
        addUserIndicator(mv);
        return mv;
    }

    @RolesAllowed( { Role.SUPERVISOR, Role.ADMIN, Role.ROOT })
    @RequestMapping(value = EDIT_AS_ADMIN_URL, method = RequestMethod.GET)
    public ModelAndView adminRenderForm(HttpServletRequest request)
            throws Exception {
        User targetUser = getUserFromQueryParam(request);
        ModelAndView mv = renderForm(request, targetUser);
        addAdminIndicator(mv);
        User editingUser = getRequestContext().getUser();

        addAssignedAndAllowedRoles(mv, editingUser, targetUser);

        return mv;
    }

    private void addAssignedAndAllowedRoles(ModelAndView mv, User editingUser,
            User targetUser) {
        String[] allowedRoles = Role.getRolesLowerThanOrEqualTo(Role.getHighestRole(editingUser.getRoles()));
        ArrayList<String> allowedRolesArrayList = new ArrayList<String>();
        for (String r : allowedRoles) {
            // don't add the anonymous role because it is not an allowed role, it
            // is the default role for anonymous access
            if (!r.equals(Role.ANONYMOUS)) {
                allowedRolesArrayList.add(r);
            }
        }
        mv.addObject("allowedRoles", allowedRolesArrayList);

        ArrayList<String> assignedRolesArrayList = new ArrayList<String>();
        for (String r : targetUser.getRoles()) {
            assignedRolesArrayList.add(r);
        }
        mv.addObject("assignedRoles", assignedRolesArrayList);
    }

    private void addAdminIndicator(ModelAndView mv) {
        mv.addObject(EDIT_AS, ADMIN_INDICATOR);
    }

    private void addUserIndicator(ModelAndView mv) {
        mv.addObject(EDIT_AS, USER_INDICATOR);
    }

    private User getUserFromQueryParam(HttpServletRequest request)
            throws Exception {
        if (!StringUtils.hasLength(request.getParameter(USER_ID))) {
            throw new Exception("Must pass argument USER_ID");
        }
        Integer userId = Integer.parseInt(request.getParameter(USER_ID));
        if (userId == null) {
            throw new Exception("Cannot use null Integer for user ID");
        }
        User u = userDAO.getUser(userId);
        if (u == null) {
            throw new Exception("Could not find valid user for User ID = "
                    + userId.toString());
        }
        return u;
    }

    private ModelAndView renderForm(HttpServletRequest request, User u)
            throws Exception {

        ModelAndView mav = new ModelAndView("userProfile");

        mav.addObject(USER_NAME, u.getName());
        mav.addObject(FIRST_NAME, u.getFirstName());
        mav.addObject(LAST_NAME, u.getLastName());
        mav.addObject(EMAIL_ADDR, u.getEmailAddress());
        mav.addObject(USER_ID, u.getId().toString());
        mav.addObject(USER_ACTIVE, u.isActive());

        List<UserMetaData> metaList = umdService.getMetadataMap(request);
        for (UserMetaData umd : metaList) {
            umd.setValue(u.getMetadataValue(umd.getKey()));
        }

        mav.addObject("metaList", metaList);
        return mav;
    }

    /**
     * Create a model and review from the request parameters. Useful for
     * allowing the user to keep editing a 1/2 filled out form
     * 
     * @param request
     * @return
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    private ModelAndView createModelAndView(HttpServletRequest request, User u, boolean isAdmin, boolean success)
            throws Exception {
        
        if (success) {
            if (isAdmin) {
                return redirect(redirService.getUserListUrl());   
            } else {
                return redirect(redirService.getHomeUrl());
            }
        } else {
            ModelAndView mav = new ModelAndView(USER_PROFILE_VIEW);
            Map<String, String[]> params = request.getParameterMap();
    
            mav.addObject(USER_NAME, u.getName());
            mav.addObject(FIRST_NAME, params.get(FIRST_NAME)[0]);
            mav.addObject(LAST_NAME, params.get(LAST_NAME)[0]);
            mav.addObject(EMAIL_ADDR, params.get(EMAIL_ADDR)[0]);
            mav.addObject(USER_ID, u.getId().toString());
            mav.addObject(USER_ACTIVE, u.isActive());
    
            List<UserMetaData> metaList = umdService.getMetadataMap(request);
            for (UserMetaData umd : metaList) {
                String[] p = params.get(umd.getKey());
                String value = p != null ? p[0] : "";
                umd.setValue(value);
            }
            mav.addObject("metaList", metaList);
    
            return mav;
        }
    }

    /**
     * On POST, save the user.
     * 
     * @param userForm
     * @return
     * @throws Exception
     */
    @RolesAllowed( { Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN, Role.ROOT })
    @RequestMapping(value = EDIT_AS_USER_URL, method = RequestMethod.POST)
    public ModelAndView userSave(HttpServletRequest request,
            HttpServletResponse response) throws Exception {
        
        // return early if cancelled
        if (request.getParameter(PARAM_CANCEL) != null) {
            return redirect(redirService.getHomeUrl());
        }

        ModelAndView mv = save(request, response, getRequestContext().getUser(), false);
        addUserIndicator(mv);
        return mv;
    }

    @RolesAllowed( { Role.SUPERVISOR, Role.ADMIN, Role.ROOT })
    @RequestMapping(value = EDIT_AS_ADMIN_URL, method = RequestMethod.POST)
    public ModelAndView adminSave(HttpServletRequest request,
            HttpServletResponse response) throws Exception {

        // return early if cancelled
        if (request.getParameter(PARAM_CANCEL) != null) {
            return redirect(redirService.getUserListUrl());
        }
        
        User targetUser = getUserFromQueryParam(request);
        User editingUser = getRequestContext().getUser();

        ArrayList<String> newRoles = new ArrayList<String>();
        for (String r : Role.getAllRoles()) {
            if (StringUtils.hasLength(request.getParameter(r))) {
                newRoles.add(r);
            }
        }

        // must contain this role.
        // Decided on just protecting the admin from being able to remove
        // this basic role. No warnings will be given. Note the view is implemented
        // such that the ROLE_USER parameter is _never_ returned
        if (!newRoles.contains(Role.USER)) {
            newRoles.add(Role.USER);
        }

        String[] newRoleArray = new String[newRoles.size()];
        for (int i = 0; i < newRoles.size(); ++i) {
            newRoleArray[i] = newRoles.get(i);
        }

        String highestEditingUserRole = Role.getHighestRole(editingUser.getRoles());
        String highestNewTargetUserRole = Role.getHighestRole(newRoleArray);
        String highestOldTargetUserRole = Role.getHighestRole(targetUser.getRoles());

        // I'm trying to promote someone above my own level.
        if (Role.isRoleHigher(highestNewTargetUserRole, highestEditingUserRole)) {
            ModelAndView mav = createModelAndView(request, targetUser, true, false);
            getRequestContext().addMessage(new Message(ERROR_EDIT_PROMOTE));
            return mav;
        }

        // Is my role higher or the same as the user i'm editing?
        if (Role.isRoleHigher(highestOldTargetUserRole, highestEditingUserRole)) {
            ModelAndView mav = createModelAndView(request, targetUser, true, false);
            getRequestContext().addMessage(new Message(ERROR_EDIT_HIGHER));
            return mav;
        }

        // Am i trying to demote someone who is at my level?
        if (highestOldTargetUserRole.equals(highestEditingUserRole)
                && Role.isRoleHigher(highestOldTargetUserRole, highestNewTargetUserRole)) {
            ModelAndView mav = createModelAndView(request, targetUser, true, false);
            getRequestContext().addMessage(new Message(ERROR_EDIT_LOWER));
            return mav;
        }

        targetUser.setRoles(newRoleArray);
        if (StringUtils.hasLength(request.getParameter(USER_ACTIVE))
                && request.getParameter(USER_ACTIVE).equalsIgnoreCase("active")) {
            if (!targetUser.isActive()) {
                // Enable the users account 
                targetUser.setActive(true);
                // Email the user that their account is activated
                regService.notifyUserAccountActive(targetUser, RequestContextHolder.getContext().getPortal());
            }
        } else {
            targetUser.setActive(false);
        }

        ModelAndView mv = save(request, response, targetUser, true);
        addAdminIndicator(mv);
        addAssignedAndAllowedRoles(mv, editingUser, targetUser);
        return mv;
    }

    @SuppressWarnings("unchecked")
    private ModelAndView save(HttpServletRequest request,
            HttpServletResponse response, User u, boolean isAdmin) throws Exception {
        Map<String, String[]> params = request.getParameterMap();

        List<UserMetaData> metaList = umdService.getMetadataMap(request);

        // note the user name is not changable.
        u.setFirstName(params.get(FIRST_NAME)[0]);
        u.setLastName(params.get(LAST_NAME)[0]);

        String newEmail = params.get(EMAIL_ADDR)[0];

        User userByEmail = userDAO.getUserByEmailAddress(newEmail);
        if (userByEmail != null && !userByEmail.getId().equals(u.getId())) {
            // The email address is already taken.
            getRequestContext().addMessage(new Message(ERROR_EMAIL_TAKEN));

            // remove the email from the model to prompt the user to change it
            ModelAndView result = createModelAndView(request, u, isAdmin, false);
            result.getModel().remove(EMAIL_ADDR);
            result.addObject(EMAIL_ADDR, "");
            return result;
        }
        u.setEmailAddress(newEmail);

        if (params.get(PASSWORD) != null
                && params.get(PASSWORD)[0].length() > 0) {
            String newPassword = params.get(PASSWORD)[0];
            if (newPassword != null) {
                if (newPassword.length() >= PASSWORD_MIN_LENGTH
                        && newPassword.length() <= PASSWORD_MAX_LENGTH) {
                    PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
                    u.setPassword(passwordEncoder.encodePassword(newPassword, null));
                } else {
                    getRequestContext().addMessage(new Message(ERROR_PASSWORD));
                    return createModelAndView(request, u, isAdmin, false);
                }
            }
        }

        u = userDAO.updateUser(u);

        for (UserMetaData umd : metaList) {
            String[] p = params.get(umd.getKey());
            String metaValue = p != null ? p[0] : null;
            String metaKey = umd.getKey();

            if (umd.getType() == UserMetaDataType.Boolean) {
                metaValue = UserMetaData.TRUE.equals(metaValue) ? UserMetaData.TRUE
                        : UserMetaData.FALSE;
            }

            if (metaValue != null) {
                if (u.metadataExists(metaKey)) {
                    Metadata md = u.getMetadataObj(metaKey);
                    md.setValue(metaValue);
                    metadataDAO.update(md);
                } else {
                    Metadata md = new Metadata();
                    md.setKey(metaKey);
                    md.setValue(metaValue);
                    metadataDAO.save(md);
                    u.addMetaDataObj(md);
                }
            }
        }
        userDAO.updateUser(u);

        getRequestContext().addMessage(new Message(SUCCESS_STANDARD));

        // display the updated form...
        return createModelAndView(request, u, isAdmin, true);
    }
}
