package au.com.gaiaresources.bdrs.model.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
import au.com.gaiaresources.bdrs.model.preference.PreferenceUtil;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Service
public class RegistrationService {
	
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private EmailService emailService;
    @Autowired
    private PreferenceDAO prefDAO;
    
    private PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
    
    public static final String ADMIN_APPROVAL_REQUIRED_PREFERENCE_KEY = "user.account.approvalRequired";
    
    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String contextPath, String role, 
            boolean autoCompleteSignup, Boolean requireAdminApproval) {
        // Create the user object.
        String encodedPassword = passwordEncoder.encodePassword(password, null);
        String registrationKey = passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), userName);
        User user = userDAO.createUser(userName, firstName, lastName, emailAddress, encodedPassword, registrationKey, role);

        if (autoCompleteSignup) {
            user.setActive(true);
        } else {
                Map<String, Object> params = new HashMap<String, Object>();
        	params.put("newUser", user);
        	params.put("portal", RequestContextHolder.getContext().getPortal());
                params.put("contextPath", contextPath);
        	if (requireAdminApproval) {
        		// Send approval request email to administrators.
        		 String[] searchRoles = {Role.ADMIN};
        		 List<User> administrators = userDAO.search(null, null, null, null, searchRoles, null).getList();
        		 for(User admin : administrators){
        		     params.put("admin", admin);
                             emailService.sendMessage(admin.getEmailAddress(), "Registration approval", "UserSignUpApproval.vm", params);
        		 }
        		 // Send welcome awaiting approval e-mail to user.
        		 emailService.sendMessage(user.getEmailAddress(), "Registration awaits approval", "UserSignUpWait.vm", params);
        	}else{
        	    // Send the welcome e-mail to the user.
        	    params.put("registrationkey", registrationKey);
        	    emailService.sendMessage(user.getEmailAddress(), "Registration Confirmation", "UserSignUp.vm", params);
        	}
        }
        return user;
    }

    /**
     * 
     * @param userName
     * @param emailAddress
     * @param firstName
     * @param lastName
     * @param password
     * @param contextPath
     * @param role
     * @param autoCompleteSignup
     * @return
     */
    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String contextPath, String role, boolean autoCompleteSignup) {
        
        boolean adminApprovalRequired = !autoCompleteSignup && isAdminApprovalRequired();
    	return signUp(userName, emailAddress, firstName, lastName, password, contextPath, role, autoCompleteSignup, adminApprovalRequired);
    }

    /**
     * 
     * @param userName
     * @param emailAddress
     * @param firstName
     * @param lastName
     * @param password
     * @param contextPath
     * @param role
     * @return
     */
    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String contextPath, String role) {
        return signUp(userName, emailAddress, firstName, lastName, password, contextPath, role, false);
    }

    /**
     * 
     * @param userName
     * @param emailAddress
     * @param firstName
     * @param lastName
     * @param password
     * @param contextPath
     * @return
     */
    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String contextPath) {
        return signUp(userName, emailAddress, firstName, lastName, password, contextPath, Role.SUPERVISOR);
    }

    /**
     * 
     * @param registrationKey
     */
    public void completeRegistration(String registrationKey) {
        User user = userDAO.getUserByRegistrationKey(registrationKey);
        if (user != null) {
            userDAO.makeUserActive(user, true);
        } else {
            throw new IllegalArgumentException("Registration key " + registrationKey + " not found.");
        }
    }
    
    /**
     * 
     * @param user
     */
    public void resetPassword(User user) {
        String newPassword = StringUtils.generateRandomString(10, 15);
        changePassword(user, newPassword);
        
        // Send the e-mail
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newUser", user);
        params.put("newpassword", newPassword);
        params.put("portal", RequestContextHolder.getContext().getPortal());
        emailService.sendMessage(user.getEmailAddress(), "Password Reminder", "PasswordReminder.vm", params);
    }
    
    /**
     * 
     * @param user
     * @param newPassword
     */
    public void changePassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encodePassword(newPassword, null);
        userDAO.updatePassword(user, encodedPassword);
    }
    
    /**
     * send an email to a user once their account has become active
     * 
     * @param u
     * @param p
     */
    public void notifyUserAccountActive(User u, Portal p) {
        // Email the user that their account is activated
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newUser", u);
        params.put("portal", p);
        emailService.sendMessage(u.getEmailAddress(), "Registration approved", "UserSignUpApproved.vm", params);
    }
    
    /**
     * Portal preference setting if admins need to approve users before their accounts become active
     * @return true if admin approval required
     */
    public boolean isAdminApprovalRequired() {
        PreferenceUtil prefUtil = new PreferenceUtil(prefDAO);
        return prefUtil.getBooleanPreference(ADMIN_APPROVAL_REQUIRED_PREFERENCE_KEY);
    }
}
