package au.com.gaiaresources.bdrs.db.impl;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.db.QueryCriteria;

public abstract class AbstractDAOImpl {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(AbstractDAOImpl.class);

    @Autowired
    private SessionFactory sessionFactory;

    public SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    protected <T extends PersistentImpl> T save(T instance) {
        updateTimestamp(instance);
        return save(sessionFactory.getCurrentSession(), instance);
    }

    public <T extends PersistentImpl> T save(Session sesh, T instance) {
        updateTimestamp(instance);
        sesh.save(instance);
        return instance;
    }
    
    public <T extends PersistentImpl> T saveOrUpdate(Session sesh, T instance) {
        updateTimestamp(instance);
        sesh.saveOrUpdate(instance);
        return instance;
    }
    
    protected int deleteByQuery(PersistentImpl instance) {
        if(instance.getId() == null) {
            return 0;
        }
        
        Map<String, Object> params = new HashMap<String, Object>(1);
        params.put("id", instance.getId());
        String queryString = String.format("delete %s where id = :id", instance.getClass().getSimpleName());
        log.debug(queryString.replaceAll(":id", instance.getId().toString()));
        return execute(queryString, params);
    }
    
    public int execute(String query, Map<String, Object> params) {
        return execute(getSessionFactory().getCurrentSession(), query, params);
    }
    
    public int execute(Session sesh, String query, Map<String, Object> params) {
        Query q = sesh.createQuery(query);
        for(Map.Entry<String, Object> entry : params.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
        return q.executeUpdate();
    }
    
    public <T extends PersistentImpl> void delete(Session sesh, T instance) {
        sesh.delete(instance);
    }
    

    public <T extends PersistentImpl> void delete(T instance) {
        this.delete(sessionFactory.getCurrentSession(),instance);
    }

    protected <T extends PersistentImpl> T update(T instance) {
        updateTimestamp(instance);
        return update(sessionFactory.getCurrentSession(), instance);
    }

    public <T extends PersistentImpl> T update(Session sesh, T instance) {
        updateTimestamp(instance);
        sesh.update(instance);
        return instance;
    }
    
    public <T extends PersistentImpl> Long count(Class<T> clazz) { 
        String queryString = String.format("select count(*) from %s", clazz.getSimpleName());
        return (Long)sessionFactory.getCurrentSession().createQuery(queryString).iterate().next();
    }
    
    private void updateTimestamp(PersistentImpl persistent) {
        persistent.setUpdatedAt(new Date());
    }

    @SuppressWarnings("unchecked")
    protected <T extends PersistentImpl> T merge(T instance) {
        T ob = (T)sessionFactory.getCurrentSession().merge(instance);
        return ob;
    }
    
    @SuppressWarnings("unchecked")
    protected <T extends PersistentImpl> T getByID(Session sesh, Class<T> clazz, Integer id) {
        // The following subterfuge is required because hibernate does not apply
        // filters to 'get' requests.
        // https://forum.hibernate.org/viewtopic.php?f=1&t=966610
        if(PortalPersistentImpl.class.isAssignableFrom(clazz)) {
            List<PersistentImpl> list = this.find(sesh,
                                                  String.format("from %s where id = ?", clazz.getSimpleName()), new Object[]{id}, 1);
            return list.isEmpty() ? null : (T)list.get(0);
        } else {
            return (T)sesh.get(clazz, id);
        }
    }

    @SuppressWarnings("unchecked")
    protected <T extends PersistentImpl> T getByID(Class<T> clazz, Integer id) {
        return getByID(sessionFactory.getCurrentSession(), clazz, id);
    }

    protected <T extends PersistentImpl> QueryCriteria<T> newQueryCriteria(Class<T> persistentClass) {
        return new QueryCriteriaImpl<T>(sessionFactory.getCurrentSession().createCriteria(persistentClass));
    }

    protected <T extends PersistentImpl> List<T> find(Session sesh, String hql, Object[] args, int limit)
    {
        Query query = sesh.createQuery(hql);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i, args[i]);
        }
        query.setMaxResults(limit);
        return query.list();
    }

    protected <T extends PersistentImpl> List<T> find(String hql, Object[] args, int limit)
    {
        return find(sessionFactory.getCurrentSession(), hql, args, limit);
    }

    protected <T extends PersistentImpl> List<T> find(Session sesh, String hql, Object[] args)
    {
        Query query = sesh.createQuery(hql);
        for (int i = 0; i < args.length; i++) {
            query.setParameter(i, args[i]);
        }
        return query.list();
    }

    protected <T extends PersistentImpl> List<T> find(String hql, Object[] args)
    {
        return find(sessionFactory.getCurrentSession(), hql, args);
    }

    protected <T extends PersistentImpl> List<T> find(Session sesh, String hql, Object args)
    {
        return find(sesh, hql, new Object[]{args});
    }

    protected <T extends PersistentImpl> List<T> find(String hql, Object args)
    {
        return find(sessionFactory.getCurrentSession(), hql, new Object[]{args});
    }

    protected <T extends PersistentImpl> List<T> find(Session sesh, String hql)
    {
        return find(sesh, hql, new Object[0]);
    }

    protected <T extends PersistentImpl> List<T> find(String hql)
    {
        return find(sessionFactory.getCurrentSession(), hql, new Object[]{});
    }

    protected Session getSession()
    {
        return sessionFactory.getCurrentSession();
    }
}
