package au.com.gaiaresources.bdrs.model.content.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;

/**
 * @author aj
 */
@Repository
public class ContentDAOImpl extends AbstractDAOImpl implements ContentDAO {
    Logger log = Logger.getLogger(ContentDAOImpl.class);

    public String getContentValue(String key) {
        List<Content> matches = this.find("from Content where key = ?", key);
        if (matches.size() > 0) {
            return matches.get(0).getValue();
        }
        return null;
    }

    public Content saveContent(String key, String value) {
        Content helpItem = getContent(key);
        boolean newrecord = false;
        if(helpItem == null){
            helpItem = new Content();
            helpItem.setKey(key);
            newrecord = true;
        }
        helpItem.setValue(value);
        if (newrecord) {
            return save(helpItem);
        } else {
            return update(helpItem);
        }

    }
    public Content getContent(String key){
        List<Content> matches = this.find("from Content where key = ?", key);
        if (matches.size() > 0) {
            return matches.get(0);
        }
        return null;
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public List<String> getAllKeys(){
        Query query = getSessionFactory().getCurrentSession().createQuery("select c.key from Content c");
        return (List<String>)query.list();
    }
}
