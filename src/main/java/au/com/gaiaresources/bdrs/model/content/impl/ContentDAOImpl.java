package au.com.gaiaresources.bdrs.model.content.impl;

import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.content.Content;
import au.com.gaiaresources.bdrs.model.content.ContentDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.service.content.ContentService;

/**
 * @author aj
 */
@Repository
public class ContentDAOImpl extends AbstractDAOImpl implements ContentDAO {
    Logger log = Logger.getLogger(ContentDAOImpl.class);

    private ContentService contentService = new ContentService();
    
    public String getContentValue(String key) {
        return getContentValue(key, null);
    }

    public Content saveContent(String key, String value) {
        Content helpItem = getContent(key);
        if(helpItem == null){
            return saveNewContent(key, value);
        } else {
            helpItem.setValue(value);
            return update(helpItem);
        }
    }
    
    public Content saveNewContent(String key, String value) {
        Content helpItem = new Content();
        helpItem.setKey(key);
        helpItem.setValue(value);
        return save(helpItem);
    }
    
    public Content getContent(String key){
        return getContent(key, true);
    }
    
    public Content getContent(String key, boolean loadIfNotFound) {
        List<Content> matches = this.find("from Content where key = ?", key);
        if (matches.size() > 0) {
            return matches.get(0);
        } else if (loadIfNotFound) {
            try {
                return contentService.initContent(this, null, key, null);
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
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
        return getContentValue(key, portal, null);
    }
    
    @Override
    public String getContentValue(String key, Portal portal, String contextPath) {
        List<Content> matches = this.find("from Content where key = ?", key);
        if (matches.size() == 1) {
            return matches.get(0).getValue();
        } else {
            try {
                // if we didn't find the value, initialize it from the file
                Content content = contentService.initContent(this, portal, key, contextPath);
                if (content != null) {
                    return content.getValue();
                }
            } catch (Exception e) {
                log.error(e);
            }
        }
        return null;
    }
}
