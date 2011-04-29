package au.com.gaiaresources.bdrs.db.impl;

import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.MappedSuperclass;

import org.hibernate.annotations.Filter;

import au.com.gaiaresources.bdrs.model.portal.Portal;

@MappedSuperclass
public abstract class PortalPersistentImpl extends PersistentImpl {
    
    public static final String PORTAL_FILTER_NAME = "portalFilter";
    public static final String PORTAL_FILTER_PORTALID_PARAMETER_NAME = "portalId";
    
    private Portal portal;
    
    @ManyToOne
    @JoinColumn(name = "PORTAL_ID", nullable = true)
    public Portal getPortal() {
        return portal;
    }

    public void setPortal(Portal portal) {
        this.portal = portal;
    }
}
