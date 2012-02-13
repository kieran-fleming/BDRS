package au.com.gaiaresources.bdrs.security;

import java.util.Collection;

import org.apache.log4j.Logger;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import au.com.gaiaresources.bdrs.model.user.User;

public class UserDetails implements org.springframework.security.core.userdetails.UserDetails {
        /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private transient Logger log = Logger.getLogger(getClass());
	private transient User user;
    
    public UserDetails(User user) {
        this.user = user;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
        if (user == null) {
            return AuthorityUtils.commaSeparatedStringToAuthorityList(Role.ANONYMOUS);
        }
    	StringBuilder roles = new StringBuilder();
    	for (String role : user.getRoles())
    	{
    		roles.append(role);
    		roles.append(",");
    	}
        return AuthorityUtils.commaSeparatedStringToAuthorityList(roles.toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        if (user == null) {
            return User.ANONYMOUS_PASSWORD;
        }
        return user.getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
        if (user == null) {
            return User.ANONYMOUS_USERNAME;
        }
        return user.getName();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonExpired() {
        return isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAccountNonLocked() {
        return isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return isEnabled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled() {
        // the anonymous account is always enabled
        if (user == null) {
            return true;
        }
        return user.isActive();
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
    	this.user = user;
    }
}
