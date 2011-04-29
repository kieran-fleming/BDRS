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
	private Logger log = Logger.getLogger(getClass());
	private User user;
    
    public UserDetails(User user) {
        if(user == null) {
            throw new NullPointerException();
        }
        this.user = user;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Collection<GrantedAuthority> getAuthorities() {
    	String roles = "";
    	for (String role : user.getRoles())
    	{
    		roles += role + ",";
    	}
        return AuthorityUtils.commaSeparatedStringToAuthorityList(roles);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getPassword() {
        return user.getPassword();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getUsername() {
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
        return user.isActive();
    }
    
    public User getUser() {
        return user;
    }
    
    public void setUser(User user) {
        if(user == null) {
            throw new NullPointerException();
        }
    	this.user = user;
    }
}
