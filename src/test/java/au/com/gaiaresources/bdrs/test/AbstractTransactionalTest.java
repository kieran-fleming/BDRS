package au.com.gaiaresources.bdrs.test;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import au.com.gaiaresources.bdrs.db.FilterManager;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;


@Transactional
public abstract class AbstractTransactionalTest extends
    AbstractSpringContextTest {

    private Logger log = Logger.getLogger(getClass());
    
    // unfortunately we need the request in this class (instead of AbstractControllerTest)
    // because our database read and writes rely on the RequestContext, which requires
    // a request object to instantiate properly.
    protected MockHttpServletRequest request;
    
    @Autowired
    protected SessionFactory sessionFactory;
    
    protected Portal defaultPortal;
    
    private boolean dropDatabase = false;
    
    @Before
    public final void primeDatabase() {
        dropDatabase = false;
        try {
            Portal portal = new PortalInitialiser().initRootPortal();
            defaultPortal = portal;
            Session sesh = sessionFactory.getCurrentSession();
            FilterManager.setPortalFilter(sesh, portal);
            RequestContext c = RequestContextHolder.getContext();
            c.setPortal(defaultPortal);
        } catch (Exception e) {
            log.error("db setup error", e);
        }
    }
    
    @BeforeTransaction
    public final void beginTransaction() throws Exception {
        request = createMockHttpServletRequest();
        RequestContext c = new RequestContext(request, applicationContext);
        RequestContextHolder.set(c);
        c.setHibernate(sessionFactory.getCurrentSession());
        
        sessionFactory.getCurrentSession().beginTransaction();
    }
    
    @AfterTransaction
    public final void rollbackTransaction() throws Exception {
        if (dropDatabase) {
            // do this instead of the rollback...
            Transaction tx = sessionFactory.getCurrentSession().getTransaction();
            if (!tx.isActive()) {
                tx = sessionFactory.getCurrentSession().beginTransaction();
            } else {
                tx.rollback();
                tx = sessionFactory.getCurrentSession().beginTransaction();
            }
            // clean up our database and commit the change...
            SQLQuery q = sessionFactory.getCurrentSession().createSQLQuery("truncate table portal cascade;");
            q.executeUpdate();
            tx.commit();
        } else {
            // do normal rollback...
            sessionFactory.getCurrentSession().getTransaction().rollback();
        }
    }
    
    protected final void requestDropDatabase() {
        dropDatabase = true;
    }
    
    protected void commit() {
        sessionFactory.getCurrentSession().getTransaction().commit();
        sessionFactory.getCurrentSession().beginTransaction();
    }

    /**
     * This function should be overriden by tests that require a multipart
     * request.
     * 
     * @return
     */
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockHttpServletRequest();
    }
}
