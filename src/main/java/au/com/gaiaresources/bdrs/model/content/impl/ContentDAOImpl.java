package au.com.gaiaresources.bdrs.model.content.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;

/**
 * @author aj
 */
@Repository
public class ContentDAOImpl extends AbstractDAOImpl implements ContentDAO {
    Logger log = Logger.getLogger(ContentDAOImpl.class);
    
    public String getContentValue(Session sesh, String key) {
        return getContentValue(sesh, key, null);
    }
    
    public Content saveContent(Session sesh, String key, String value) {
        Content content = getContent(key);
        if(content == null){
            return saveNewContent(sesh, key, value);
        } else {
            content.setValue(value);
            return update(content);
        }
    }
    
    public Content saveNewContent(Session sesh, String key, String value) {
        Content helpItem = new Content();
        helpItem.setKey(key);
        helpItem.setValue(value);
        return save(sesh, helpItem);
    }
    
    public Content getContent(String key){
        return findUnique("from Content where key = ?", new Object[] { key });
    }
    @Override
    public Content getContent(Session sesh, String key){
        return findUnique(sesh, "from Content where key = ?", new Object[] { key });
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAllKeys(){
        Query query = getSessionFactory().getCurrentSession().createQuery("select c.key from Content c");
        return (List<String>)query.list();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List<String> getKeysLike(String string) {
        Query query = getSessionFactory().getCurrentSession().createQuery("select c.key from Content c where c.key like '%" + string + "%'");
        return (List<String>)query.list();
    }
    
    @Override
    public String getContentValue(Session sesh, String key, Portal portal) {
        
        if (key == null) {
            throw new IllegalArgumentException("String, key, cannot be null");
        }
        
        HqlQuery query = new HqlQuery("from Content ");
        query.and(Predicate.eq("key", key));
        
        if (portal != null) {
            query.and(Predicate.eq("portal", portal));
        }
        Content c = this.findUnique(sesh, query.getQueryString(), query.getParametersValue());
        return c != null ? c.getValue() : null;
    }
}
