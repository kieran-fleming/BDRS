package au.com.gaiaresources.bdrs.db;

import java.io.Serializable;
import java.sql.Connection;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.CacheMode;
import org.hibernate.Criteria;
import org.hibernate.EntityMode;
import org.hibernate.Filter;
import org.hibernate.FlushMode;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Query;
import org.hibernate.ReplicationMode;
import org.hibernate.SQLQuery;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.jdbc.Work;
import org.hibernate.stat.SessionStatistics;
import org.hibernate.type.Type;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

public class Session implements org.hibernate.classic.Session {
    private static final long serialVersionUID = 1858108706839266005L;
     
    private Logger log = Logger.getLogger(getClass());
    private org.hibernate.classic.Session session;

    public Session(org.hibernate.classic.Session session) {
        this.session = session;
    }

    @Override
    public Query createSQLQuery(String sql, String returnAlias,
            Class returnClass) {
        return this.session.createSQLQuery(sql, returnAlias, returnClass);
    }

    @Override
    public Query createSQLQuery(String sql, String[] returnAliases,
            Class[] returnClasses) {
        return this.session.createSQLQuery(sql, returnAliases, returnClasses);
    }

    @Override
    public int delete(String query, Object value, Type type)
            throws HibernateException {
        return this.session.delete(query, value, type);
    }

    @Override
    public int delete(String query, Object[] values, Type[] types)
            throws HibernateException {
        return this.session.delete(query, values, types);
    }

    @Override
    public int delete(String query) throws HibernateException {
        return this.session.delete(query);
    }

    @Override
    public Collection filter(Object collection, String filter, Object value,
            Type type) throws HibernateException {
        return this.session.filter(collection, filter, value, type);
    }

    @Override
    public Collection filter(Object collection, String filter, Object[] values,
            Type[] types) throws HibernateException {
        return this.session.filter(collection, filter, values, types);
    }

    @Override
    public Collection filter(Object collection, String filter)
            throws HibernateException {
        return this.session.filter(collection, filter);
    }

    @Override
    public List find(String query, Object value, Type type)
            throws HibernateException {
        return this.session.find(query, value, type);
    }

    @Override
    public List find(String query, Object[] values, Type[] types)
            throws HibernateException {
        return this.session.find(query, values, types);
    }

    @Override
    public List find(String query) throws HibernateException {
        return this.session.find(query);
    }

    @Override
    public Iterator iterate(String query, Object value, Type type)
            throws HibernateException {
        return this.session.iterate(query, value, type);
    }

    @Override
    public Iterator iterate(String query, Object[] values, Type[] types)
            throws HibernateException {
        return this.session.iterate(query, values, types);
    }

    @Override
    public Iterator iterate(String query) throws HibernateException {
        return this.session.iterate(query);
    }

    @Override
    public void save(Object object, Serializable id) throws HibernateException {
        this.session.save(object, id);
    }

    @Override
    public void save(String entityName, Object object, Serializable id)
            throws HibernateException {
        this.session.save(entityName, object, id);
    }

    @Override
    public Object saveOrUpdateCopy(Object object, Serializable id)
            throws HibernateException {
        return this.session.saveOrUpdateCopy(object, id);
    }

    @Override
    public Object saveOrUpdateCopy(Object object) throws HibernateException {
        return this.session.saveOrUpdateCopy(object);
    }

    @Override
    public Object saveOrUpdateCopy(String entityName, Object object,
            Serializable id) throws HibernateException {
        return this.session.saveOrUpdateCopy(entityName, object, id);
    }

    @Override
    public Object saveOrUpdateCopy(String entityName, Object object)
            throws HibernateException {
        return this.session.saveOrUpdateCopy(entityName, object);
    }

    @Override
    public void update(Object object, Serializable id)
            throws HibernateException {
        this.session.update(object, id);
    }

    @Override
    public void update(String entityName, Object object, Serializable id)
            throws HibernateException {
        this.session.update(entityName, object, id);
    }

    @Override
    public Transaction beginTransaction() throws HibernateException {

        Transaction tx = this.session.beginTransaction();
        
        RequestContext context = RequestContextHolder.getContext();
        Portal portal = context.getPortal();

        if (portal != null) {
            FilterManager.setPortalFilter(this.session, portal);
        } else {
            log.warn("Portal is not set. Not enabling hibernate portal filter.");
        }

        return tx;
    }

    @Override
    public void cancelQuery() throws HibernateException {
        this.session.cancelQuery();
    }

    @Override
    public void clear() {
        this.session.clear();
    }

    @Override
    public Connection close() throws HibernateException {
        return this.session.close();
    }

    @Override
    public Connection connection() throws HibernateException {
        return this.session.connection();
    }

    @Override
    public boolean contains(Object object) {
        return this.session.contains(object);
    }

    @Override
    public Criteria createCriteria(Class persistentClass, String alias) {
        return this.session.createCriteria(persistentClass, alias);
    }

    @Override
    public Criteria createCriteria(Class persistentClass) {
        return this.session.createCriteria(persistentClass);
    }

    @Override
    public Criteria createCriteria(String entityName, String alias) {
        return this.session.createCriteria(entityName, alias);
    }

    @Override
    public Criteria createCriteria(String entityName) {
        return this.session.createCriteria(entityName);
    }

    @Override
    public Query createFilter(Object collection, String queryString)
            throws HibernateException {
        return this.session.createFilter(collection, queryString);
    }

    @Override
    public Query createQuery(String queryString) throws HibernateException {
        return this.session.createQuery(queryString);
    }

    @Override
    public SQLQuery createSQLQuery(String queryString)
            throws HibernateException {
        return this.session.createSQLQuery(queryString);
    }

    @Override
    public void delete(Object object) throws HibernateException {
        this.session.delete(object);
    }

    @Override
    public void delete(String entityName, Object object)
            throws HibernateException {
        this.session.delete(entityName, object);
    }

    @Override
    public void disableFilter(String filterName) {
        this.session.disableFilter(filterName);
    }

    @Override
    public Connection disconnect() throws HibernateException {
        return this.session.disconnect();
    }

    @Override
    public void doWork(Work work) throws HibernateException {
        this.session.doWork(work);
    }

    @Override
    public Filter enableFilter(String filterName) {
        return this.session.enableFilter(filterName);
    }

    @Override
    public void evict(Object object) throws HibernateException {
        this.session.evict(object);
    }

    @Override
    public void flush() throws HibernateException {
        this.session.flush();
    }

    @Override
    public Object get(Class clazz, Serializable id, LockMode lockMode)
            throws HibernateException {
        return this.session.get(clazz, id, lockMode);
    }

    @Override
    public Object get(Class clazz, Serializable id) throws HibernateException {
        return this.session.get(clazz, id);
    }

    @Override
    public Object get(String entityName, Serializable id, LockMode lockMode)
            throws HibernateException {
        return this.session.get(entityName, id, lockMode);
    }

    @Override
    public Object get(String entityName, Serializable id)
            throws HibernateException {
        return this.session.get(entityName, id);
    }

    @Override
    public CacheMode getCacheMode() {
        return this.session.getCacheMode();
    }

    @Override
    public LockMode getCurrentLockMode(Object object) throws HibernateException {
        return this.session.getCurrentLockMode(object);
    }

    @Override
    public Filter getEnabledFilter(String filterName) {
        return this.session.getEnabledFilter(filterName);
    }

    @Override
    public EntityMode getEntityMode() {
        return this.session.getEntityMode();
    }

    @Override
    public String getEntityName(Object object) throws HibernateException {
        return this.session.getEntityName(object);
    }

    @Override
    public FlushMode getFlushMode() {
        return this.session.getFlushMode();
    }

    @Override
    public Serializable getIdentifier(Object object) throws HibernateException {
        return this.session.getIdentifier(object);
    }

    @Override
    public Query getNamedQuery(String queryName) throws HibernateException {
        return this.session.getNamedQuery(queryName);
    }

    @Override
    public org.hibernate.Session getSession(EntityMode entityMode) {
        return this.session.getSession(entityMode);
    }

    @Override
    public SessionFactory getSessionFactory() {
        return this.session.getSessionFactory();
    }

    @Override
    public SessionStatistics getStatistics() {
        return this.session.getStatistics();
    }

    @Override
    public Transaction getTransaction() {
        return this.session.getTransaction();
    }

    @Override
    public boolean isConnected() {
        return this.session.isConnected();
    }

    @Override
    public boolean isDirty() throws HibernateException {
        return this.session.isDirty();
    }

    @Override
    public boolean isOpen() {
        return this.session.isOpen();
    }

    @Override
    public Object load(Class theClass, Serializable id, LockMode lockMode)
            throws HibernateException {
        return this.session.load(theClass, id, lockMode);
    }

    @Override
    public Object load(Class theClass, Serializable id)
            throws HibernateException {
        return this.session.load(theClass, id);
    }

    @Override
    public void load(Object object, Serializable id) throws HibernateException {
        this.session.load(object, id);
    }

    @Override
    public Object load(String entityName, Serializable id, LockMode lockMode)
            throws HibernateException {
        return this.session.load(entityName, id, lockMode);
    }

    @Override
    public Object load(String entityName, Serializable id)
            throws HibernateException {
        return this.session.load(entityName, id);
    }

    @Override
    public void lock(Object object, LockMode lockMode)
            throws HibernateException {
        this.session.lock(object, lockMode);
    }

    @Override
    public void lock(String entityName, Object object, LockMode lockMode)
            throws HibernateException {
        this.session.lock(entityName, object, lockMode);
    }

    @Override
    public Object merge(Object object) throws HibernateException {
        return this.session.merge(object);
    }

    @Override
    public Object merge(String entityName, Object object)
            throws HibernateException {
        return this.session.merge(entityName, object);
    }

    @Override
    public void persist(Object object) throws HibernateException {
        this.session.persist(object);
    }

    @Override
    public void persist(String entityName, Object object)
            throws HibernateException {
        this.session.persist(entityName, object);
    }

    @Override
    public void reconnect() throws HibernateException {
        this.session.reconnect();
    }

    @Override
    public void reconnect(Connection connection) throws HibernateException {
        this.session.reconnect(connection);
    }

    @Override
    public void refresh(Object object, LockMode lockMode)
            throws HibernateException {
        this.session.refresh(object, lockMode);
    }

    @Override
    public void refresh(Object object) throws HibernateException {
        this.session.refresh(object);
    }

    @Override
    public void replicate(Object object, ReplicationMode replicationMode)
            throws HibernateException {
        this.session.replicate(object, replicationMode);
    }

    @Override
    public void replicate(String entityName, Object object,
            ReplicationMode replicationMode) throws HibernateException {
        this.session.replicate(entityName, object, replicationMode);
    }

    @Override
    public Serializable save(Object object) throws HibernateException {
        return this.session.save(object);
    }

    @Override
    public Serializable save(String entityName, Object object)
            throws HibernateException {
        return this.session.save(entityName, object);
    }

    @Override
    public void saveOrUpdate(Object object) throws HibernateException {
        this.session.saveOrUpdate(object);
    }

    @Override
    public void saveOrUpdate(String entityName, Object object)
            throws HibernateException {
        this.session.saveOrUpdate(entityName, object);
    }

    @Override
    public void setCacheMode(CacheMode cacheMode) {
        this.session.setCacheMode(cacheMode);
    }

    @Override
    public void setFlushMode(FlushMode flushMode) {
        this.session.setFlushMode(flushMode);
    }

    @Override
    public void setReadOnly(Object entity, boolean readOnly) {
        this.session.setReadOnly(entity, readOnly);
    }

    @Override
    public void update(Object object) throws HibernateException {
        this.session.update(object);
    }

    @Override
    public void update(String entityName, Object object)
            throws HibernateException {
        this.session.update(entityName, object);
    }

}
