package au.com.gaiaresources.bdrs.test;

import org.apache.log4j.Logger;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.test.context.transaction.BeforeTransaction;
import org.springframework.transaction.annotation.Transactional;

import au.com.gaiaresources.bdrs.controller.BdrsMockHttpServletRequest;
import au.com.gaiaresources.bdrs.db.FilterManager;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.impl.PortalInitialiser;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;


@Transactional
public abstract class AbstractTransactionalTest extends AbstractSpringContextTest {

    private Logger log = Logger.getLogger(getClass());
    
    // unfortunately we need the request in this class (instead of AbstractControllerTest)
    // because our database read and writes rely on the RequestContext, which requires
    // a request object to instantiate properly.
    protected MockHttpServletRequest request;
    
    protected Session sesh;
    
    @Autowired
    protected SessionFactory sessionFactory;
    
    protected Portal defaultPortal;
    
    private boolean dropDatabase = false;
    
    @Before
    public final void primeDatabase() {
        dropDatabase = false;
        try {
            sesh = sessionFactory.getCurrentSession();
            Portal portal = new PortalInitialiser().initRootPortal(sesh, null);
            defaultPortal = portal;
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

    
    protected static final String REQUEST_SCHEME = "http";
    protected static final String REQUEST_SERVER_NAME = "www.mybdrs.com.au";
    protected static final int REQUEST_SERVER_PORT = 8080;
    protected static final String REQUEST_CONTEXT_PATH = "CONTEXTPATH";
    
    /**
     * This function should be overriden by tests that require a multipart
     * request.
     * 
     * @return
     */
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return createStandardRequest();
    }
    
    protected MockHttpServletRequest createStandardRequest() {
        // Use BdrsMockHttpServletRequest so we can manipulate the request parameters.
        MockHttpServletRequest request = new BdrsMockHttpServletRequest();
        request.setScheme(REQUEST_SCHEME);
        request.setServerName(REQUEST_SERVER_NAME);
        request.setContextPath(REQUEST_CONTEXT_PATH);
        request.setServerPort(REQUEST_SERVER_PORT);
        MockHttpSession session = new MockHttpSession(); 
        request.setSession(session);
        return request;
    }
    
    protected MockHttpServletRequest createUploadRequest() {
        MockHttpServletRequest request = new MockMultipartHttpServletRequest();
        MockHttpSession session = new MockHttpSession(); 
        request.setSession(session);
        return request;
    }
}
