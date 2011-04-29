package au.com.gaiaresources.bdrs.db;

import org.hibernate.Filter;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;

public class FilterManager {
    public static void setPortalFilter(org.hibernate.Session sesh, Portal portal) {
        Filter filter = sesh.enableFilter(PortalPersistentImpl.PORTAL_FILTER_NAME);
        filter.setParameter(PortalPersistentImpl.PORTAL_FILTER_PORTALID_PARAMETER_NAME, portal.getId());
    }
}
