package au.com.gaiaresources.bdrs.security;

import org.apache.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.util.StringUtils;


/**
 * Service for authenticating users.
 * @author AJ
 */
public class RegisteringAuthenticationService extends AuthenticationService {
    @Autowired
    private UserDAO userDAO;
    
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * {@inheritDoc}
     */
    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException, DataAccessException {
    	User u = userDAO.getUser(userName);
        if (u == null) {
        	UsernameNotFoundException e = new UsernameNotFoundException("User " + userName + " not found.");
        	// new user, need to create
    		u = userDAO.createUser(userName, 
    				"", // first
    				"", // last
    				"", // email 
    				new Md5PasswordEncoder().encodePassword(StringUtils.generateRandomString(10, 50), userName), // password 
    				new Md5PasswordEncoder().encodePassword(StringUtils.generateRandomString(10, 50), userName), // regkey
    				"ROLE_USER");
    		u.setActive(true);
    		userDAO.updateUser(u);   
        }
        return new UserDetails(u);
    }
    
    
    public User getUserByRegistrationKey(String rego){
        User u = userDAO.getUserByRegistrationKey(rego);
        return u;
    }
}
