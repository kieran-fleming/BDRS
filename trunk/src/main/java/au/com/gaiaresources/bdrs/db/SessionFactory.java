package au.com.gaiaresources.bdrs.db;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Map;
import java.util.Set;

import javax.naming.NamingException;
import javax.naming.Reference;

import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Interceptor;
import org.hibernate.StatelessSession;
import org.hibernate.classic.Session;
import org.hibernate.engine.FilterDefinition;
import org.hibernate.metadata.ClassMetadata;
import org.hibernate.metadata.CollectionMetadata;
import org.hibernate.stat.Statistics;

public class SessionFactory implements org.hibernate.SessionFactory {

    private static final long serialVersionUID = 4314695889492006974L;

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    private org.hibernate.SessionFactory rawSessionFactory;
    
    public SessionFactory(org.hibernate.SessionFactory rawSf) {
        this.rawSessionFactory = rawSf;
    }

    @Override
    public void close() throws HibernateException {
        this.rawSessionFactory.close();
    }

    @Override
    public void evict(Class persistentClass) throws HibernateException {
        this.rawSessionFactory.evict(persistentClass);
    }

    @Override
    public void evict(Class persistentClass, Serializable id)
            throws HibernateException {
        this.rawSessionFactory.evict(persistentClass, id);
    }

    @Override
    public void evictCollection(String roleName) throws HibernateException {
        this.rawSessionFactory.evictCollection(roleName);
    }

    @Override
    public void evictCollection(String roleName, Serializable id)
            throws HibernateException {
        this.rawSessionFactory.evictCollection(roleName, id);
    }

    @Override
    public void evictEntity(String entityName) throws HibernateException {
        this.rawSessionFactory.evictEntity(entityName);
    }

    @Override
    public void evictEntity(String entityName, Serializable id)
            throws HibernateException {
        this.rawSessionFactory.evictEntity(entityName, id);
    }

    @Override
    public void evictQueries() throws HibernateException {
        this.rawSessionFactory.evictQueries();
    }

    @Override
    public void evictQueries(String cacheRegion) throws HibernateException {
        this.rawSessionFactory.evictQueries(cacheRegion);
    }

    @Override
    public Map getAllClassMetadata() throws HibernateException {
        return this.rawSessionFactory.getAllClassMetadata();
    }

    @Override
    public Map getAllCollectionMetadata() throws HibernateException {
        return this.rawSessionFactory.getAllCollectionMetadata();
    }

    @Override
    public ClassMetadata getClassMetadata(Class persistentClass)
            throws HibernateException {
        return this.rawSessionFactory.getClassMetadata(persistentClass);
    }

    @Override
    public ClassMetadata getClassMetadata(String entityName)
            throws HibernateException {
        return this.rawSessionFactory.getClassMetadata(entityName);
    }

    @Override
    public CollectionMetadata getCollectionMetadata(String roleName)
            throws HibernateException {
        return this.rawSessionFactory.getCollectionMetadata(roleName);
    }

    @Override
    public Session getCurrentSession() throws HibernateException {
        Session sesh = this.rawSessionFactory.getCurrentSession();
        if(!(sesh instanceof au.com.gaiaresources.bdrs.db.Session)) {
            sesh = new au.com.gaiaresources.bdrs.db.Session(sesh);
        }
        return sesh;
    }

    @Override
    public Set getDefinedFilterNames() {
        return this.rawSessionFactory.getDefinedFilterNames();
    }

    @Override
    public FilterDefinition getFilterDefinition(String filterName)
            throws HibernateException {
        return this.rawSessionFactory.getFilterDefinition(filterName);
    }

    @Override
    public Statistics getStatistics() {
        return this.rawSessionFactory.getStatistics();
    }

    @Override
    public boolean isClosed() {
        return this.rawSessionFactory.isClosed();
    }

    @Override
    public org.hibernate.classic.Session openSession() throws HibernateException {
        return new au.com.gaiaresources.bdrs.db.Session(this.rawSessionFactory.openSession());
    }

    @Override
    public org.hibernate.classic.Session openSession(Connection connection) {
        return new au.com.gaiaresources.bdrs.db.Session(this.rawSessionFactory.openSession(connection));
    }

    @Override
    public org.hibernate.classic.Session openSession(Interceptor interceptor)
            throws HibernateException {
        return new au.com.gaiaresources.bdrs.db.Session(this.rawSessionFactory.openSession(interceptor));
    }

    @Override
    public org.hibernate.classic.Session openSession(Connection connection, Interceptor interceptor) {
        return new au.com.gaiaresources.bdrs.db.Session(this.rawSessionFactory.openSession(connection, interceptor));
    }

    @Override
    public StatelessSession openStatelessSession() {
        return this.rawSessionFactory.openStatelessSession();
    }

    @Override
    public StatelessSession openStatelessSession(Connection connection) {
        return this.rawSessionFactory.openStatelessSession(connection);
    }

    @Override
    public Reference getReference() throws NamingException {
        return this.rawSessionFactory.getReference();
    }

}
