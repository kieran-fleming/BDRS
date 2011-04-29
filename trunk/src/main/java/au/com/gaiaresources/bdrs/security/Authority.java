/**
 * 
 */
package au.com.gaiaresources.bdrs.security;

import org.springframework.security.core.GrantedAuthority;

public class Authority implements GrantedAuthority {
    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private String roleName;
    
    Authority(String roleName) {
        this.roleName = roleName;
    }
    
    @Override
    public String getAuthority() {
        return roleName;
    }

    public int compareTo(Object o) {
        if (o instanceof GrantedAuthority) {
            GrantedAuthority ga = (GrantedAuthority) o;
            return getAuthority().compareTo(ga.getAuthority());
        }
        return -1;
    }
}
