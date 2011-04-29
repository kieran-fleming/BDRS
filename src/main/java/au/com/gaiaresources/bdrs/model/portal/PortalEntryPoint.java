package au.com.gaiaresources.bdrs.model.portal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;


@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "PORTALENTRYPOINT")
@AttributeOverride(name = "id", column = @Column(name = "PORTAL_ENTRY_POINT_ID"))
public class PortalEntryPoint extends PortalPersistentImpl {
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    private String pattern;
    private String redirect = new String();
    
    @Column(name = "PATTERN", nullable = false)
    public String getPattern() {
        return pattern;
    }
    
    public void setPattern(String pattern) {
        this.pattern = pattern;
    }

    @Column(name = "REDIRECT", nullable = false)
    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }
}
