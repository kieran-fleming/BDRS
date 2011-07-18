package au.com.gaiaresources.bdrs.db;

import org.apache.poi.hssf.record.formula.functions.T;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

public interface TransactionDAO {

    public <T extends PersistentImpl> T save(Session sesh, T instance);
    public <T extends PersistentImpl> T update(Session sesh, T instance);
    public <T extends PersistentImpl> T saveOrUpdate(Session sesh, T instance);
    public <T extends PersistentImpl> void delete(Session sesh, T instance);
    public <T extends PersistentImpl> Long count(Class<T> clazz);
    public SessionFactory getSessionFactory();
}
