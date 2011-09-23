package au.com.gaiaresources.bdrs.db;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

public interface TransactionDAO {

    public <T extends Persistent> T save(Session sesh, T instance);
    public <T extends Persistent> T update(Session sesh, T instance);
    public <T extends Persistent> T saveOrUpdate(Session sesh, T instance);
    public <T extends Persistent> void delete(Session sesh, T instance);
    public <T extends Persistent> Long count(Class<T> clazz);
    public SessionFactory getSessionFactory();
}
