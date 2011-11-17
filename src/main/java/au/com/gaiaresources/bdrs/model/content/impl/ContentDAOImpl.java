package au.com.gaiaresources.bdrs.model.content.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
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
    
    public String getContentValue(String key) {
        return getContentValue(key, null);
    }

    public Content saveContent(String key, String value) {
        Content content = getContent(key);
        if(content == null){
            return saveNewContent(key, value);
        } else {
            content.setValue(value);
            return update(content);
        }
    }
    
    public Content saveNewContent(String key, String value) {
        Content helpItem = new Content();
        helpItem.setKey(key);
        helpItem.setValue(value);
        return save(helpItem);
    }
    
    public Content getContent(String key){
        return findUnique("from Content where key = ?", new Object[] { key });
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
    public String getContentValue(String key, Portal portal) {
        
        if (key == null) {
            throw new IllegalArgumentException("String, key, cannot be null");
        }
        
        HqlQuery query = new HqlQuery("from Content ");
        query.and(Predicate.eq("key", key));
        
        if (portal != null) {
            query.and(Predicate.eq("portal", portal));
        }
        Content c = this.findUnique(query.getQueryString(), query.getParametersValue());
        return c != null ? c.getValue() : null;
    }
}
