package au.com.gaiaresources.bdrs.event;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.event.PreInsertEvent;
import org.hibernate.event.PreInsertEventListener;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class PortalPersistentListener implements PreInsertEventListener {
    private static final long serialVersionUID = -2277148183866046715L;

    private Logger log = Logger.getLogger(getClass());

    @Override
    public boolean onPreInsert(PreInsertEvent event) {
        
        if(event.getEntity() instanceof PortalPersistentImpl) {
            PortalPersistentImpl ppi = (PortalPersistentImpl) event.getEntity();
            if(ppi.getId() == null && ppi.getPortal() == null) {
                RequestContext context = RequestContextHolder.getContext();
                if(context.getPortal() == null) {
                    log.warn("Retrieved null Portal Id when saving or updating: "+ppi);
                } else {
                    Session sesh;
                    Transaction tx;
                    boolean commitRequired;
                    SessionFactory sessionFactory = event.getSession().getSessionFactory();
                    if(sessionFactory.getCurrentSession().getTransaction().isActive()) {
                        // This section will run if we are unit testing.
                        sesh = sessionFactory.getCurrentSession();
                        tx = sesh.getTransaction();
                        commitRequired = false;
                    } else {
                        // This section will run under normal conditions.
                        sesh = sessionFactory.openSession();
                        tx = sesh.beginTransaction();
                        commitRequired = true;
                    }
                    
                    
                    Portal portal = (Portal)sesh.get(Portal.class, context.getPortal().getId());

                    ppi.setPortal(portal);
                    
                    if(commitRequired) {
                        tx.commit();
                        sesh.close();
                    }
                }
            }
        }
        return false;
    }
}
