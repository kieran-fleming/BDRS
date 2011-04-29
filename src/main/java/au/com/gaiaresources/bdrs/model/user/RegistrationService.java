package au.com.gaiaresources.bdrs.model.user;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Service
public class RegistrationService {
	
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private EmailService emailService;
    
    private PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
    
    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String role, Boolean autoCompleteSignup, Boolean requireAdminApproval) {
        // Create the user object.
        String encodedPassword = passwordEncoder.encodePassword(password, null);
        String registrationKey = passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), userName);
        User user = userDAO.createUser(userName, firstName, lastName, emailAddress, encodedPassword, registrationKey, role);

        if (autoCompleteSignup) {
            user.setActive(true);
        } else
        {
        	Map<String, Object> params = new HashMap<String, Object>();
        	params.put("newUser", user);
        	if(requireAdminApproval){
        		// Send approval request email to administrators.
        		 String[] searchRoles = {Role.ADMIN};
        		 List<User> administrators = userDAO.search(null, null, null, null, searchRoles, null).getList();
        		 for(User admin : administrators){
        			 params.put("admin", admin);
                     emailService.sendMessage(admin.getEmailAddress(), "Registration approval", "UserSignupApproval.vm", params);
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

    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String role, Boolean autoCompleteSignup) {
    	return signUp(userName, emailAddress, firstName, lastName, password, role, autoCompleteSignup, false);
    }

    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password, String role) {
        return signUp(userName, emailAddress, firstName, lastName, password, role, false);
    }

    public User signUp(String userName, String emailAddress, String firstName,
            String lastName, String password) {
        return signUp(userName, emailAddress, firstName, lastName, password, Role.SUPERVISOR);
    }

    public void completeRegistration(String registrationKey) {
        User user = userDAO.getUserByRegistrationKey(registrationKey);
        if (user != null) {
            userDAO.makeUserActive(user, true);
        } else {
            throw new IllegalArgumentException("Registration key " + registrationKey + " not found.");
        }
    }
    
    public void resetPassword(User user) {
        String newPassword = StringUtils.generateRandomString(10, 15);
        changePassword(user, newPassword);
        
        // Send the e-mail
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("newUser", user);
        params.put("newpassword", newPassword);
        emailService.sendMessage(user.getEmailAddress(), "Password Reminder", "PasswordReminder.vm", params);
    }
    
    public void changePassword(User user, String newPassword) {
        String encodedPassword = passwordEncoder.encodePassword(newPassword, null);
        userDAO.updatePassword(user, encodedPassword);
    }
}
