package au.com.gaiaresources.bdrs.model.portal;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;

public interface PortalDAO extends TransactionDAO {

    Portal save(Portal portal) throws Exception;
    
    Portal save(Session sesh, Portal portal) throws Exception;

    Portal getPortal(Session sesh, Integer portalId);
    Portal getPortal(Integer portalId);
    
    Portal getPortalByName(Session sesh, String portalName);

    List<Portal> getPortals();
    List<Portal> getPortals(Session sesh);
    
    List<PortalEntryPoint> getPortalEntryPoints(Session sesh, Portal portal);
    List<PortalEntryPoint> getPortalEntryPoints(Portal portal);
    
    PortalEntryPoint getPortalEntryPointByPattern(Session sesh, Integer portalId, String pattern);
    PortalEntryPoint getPortalEntryPointByPattern(Integer portalId, String pattern);
    
    PortalEntryPoint save(Session sesh, PortalEntryPoint entryPoint);
    
    PortalEntryPoint save(PortalEntryPoint entryPoint);

    
    Portal getPortal(Session sesh, boolean isDefault);
    Portal getPortal(boolean isDefault);

    PortalEntryPoint getPortalEntryPoint(Integer id);

    void delete(PortalEntryPoint value);

}
