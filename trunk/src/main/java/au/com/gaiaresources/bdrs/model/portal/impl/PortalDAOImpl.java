package au.com.gaiaresources.bdrs.model.portal.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.portal.PortalEntryPoint;

@Repository
public class PortalDAOImpl extends AbstractDAOImpl implements PortalDAO {

    private Logger log = Logger.getLogger(getClass());
    
    @Override
    public Portal getPortal(Integer portalId) {
        return this.getPortal(null, portalId);
    }

    @Override
    public Portal getPortal(Session sesh, Integer portalId) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        return (Portal)sesh.get(Portal.class, portalId);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Portal getPortalByName(Session sesh, String portalName) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        List<Portal> portalList = find(sesh, "from Portal p where p.name = ?", portalName);
        if(portalList.isEmpty()) {
            return null;
        }
        
        if(portalList.size() > 1) {
            log.warn(String.format("More than one portals with the name \"%s\" found. Returning the first.", portalName));
        }
        
        return portalList.get(0);
    }
    
    @Override
    public List<Portal> getPortals() {
        return this.getPortals(null);
    }

    @Override
    public List<Portal> getPortals(Session sesh) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        return super.find(sesh, "from Portal p order by p.name");
    }
    
    @Override
    public PortalEntryPoint getPortalEntryPointByPattern(Integer portalId,
            String pattern) {
        return this.getPortalEntryPointByPattern(null, portalId, pattern);
    }

    @Override
    public PortalEntryPoint getPortalEntryPointByPattern(Session sesh,
            Integer portalId, String pattern) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        Query query = sesh.createQuery("select e from PortalEntryPoint e where e.portal.id = :portalId and e.pattern = :pattern");
        query.setParameter("portalId", portalId);
        query.setParameter("pattern", pattern);
        List<PortalEntryPoint> entryPointList = query.list();
        if (entryPointList.isEmpty()) {
            return null;
        } else {
            if (entryPointList.size() > 1) {
                log.warn(String.format("More than one PortalEntryPoint returned for the Portal ID: %d and pattern \"%s\". Returning the first.", portalId, pattern));
            }
            return entryPointList.get(0);
        }
    }
    
    @Override
    public List<PortalEntryPoint> getPortalEntryPoints(Portal portal) {
        return this.getPortalEntryPoints(null, portal);
    }
    
    @Override
    public PortalEntryPoint getPortalEntryPoint(Integer id) {
        return super.getByID(PortalEntryPoint.class, id);
    }
    
    @Override
    public void delete(PortalEntryPoint portalEntryPoint) {
        super.delete(portalEntryPoint);
    }

    @Override
    public List<PortalEntryPoint> getPortalEntryPoints(Session sesh, Portal portal) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        return find(sesh, "from PortalEntryPoint e where e.portal = ? order by id asc", portal);
    }

    @Override
    public Portal save(Portal portal) throws Exception {
        return this.save(null, portal);
    }

    /**
     * {@inheritDoc}
     * @throws Exception 
     */
    @Override
    public Portal save(Session sesh, Portal portal) throws Exception {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        // if the portal is already persisted...
        if (portal.getId() != null) {
            return super.save(sesh, portal);
        }
            
        // else do the initialisation
        Portal p = super.save(sesh, portal);
        try
        {
            // seed portal with essential data...
            new PortalInitialiser().init(p);
        }
        catch (Exception e)
        {
            log.error("Could not initialise portal. Rolling back transaction...", e);
            throw e;
        }
        return p;
    }

    @Override
    public PortalEntryPoint save(Session sesh, PortalEntryPoint entryPoint) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        return super.save(sesh, entryPoint);
    }

    @Override
    public PortalEntryPoint save(PortalEntryPoint entryPoint) {
        return this.save(null, entryPoint);
    }
    
    @Override
    public Portal getPortal(boolean isDefault) {
        return this.getPortal(null, isDefault);
    }

    @Override
    public Portal getPortal(Session sesh, boolean isDefault) {
        if(sesh == null) {
            sesh = getSessionFactory().getCurrentSession();
        }
        
        List<Portal> portalList = find(sesh, "from Portal p where p.default = ?", isDefault);
        
        if(portalList.isEmpty()) {
            return null;
        }
        
        if(portalList.size() > 1) {
            log.warn(String.format("More than one portals with default = \"%s\" found. Returning the first.", new Boolean(isDefault).toString()));
        }
        
        return portalList.get(0);
    }
}
