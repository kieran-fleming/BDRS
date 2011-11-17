package au.com.gaiaresources.bdrs.model.content;

import java.util.List;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;

/**
 * Note we shouldn't be using ContentDAO for reading/writing content in the application.
 * {@link ContentService} handles the lazy initialisation of content so we should use the methods
 * provided there.
 * 
 */
public interface ContentDAO extends TransactionDAO {
    public String getContentValue(String key);

    /**
     * Creates or updates a help item in the system.
     * 
     * @param key
     *            The key for the help item
     * @param value
     *            The value returned
     * @return returns the saved help item object
     */
    public Content saveContent(String key, String value);

    /**
     * Creates a help item in the system.
     * 
     * @param key
     *            The key for the help item
     * @param value
     *            The value returned
     * @return returns the saved help item object
     */
    public Content saveNewContent(String key, String value);

    /**
     * Get a HelpItem
     * 
     * @param key
     * @return HelpItem matching the requested key
     */
    public Content getContent(String key);
    
    public List<String> getAllKeys();

    public List<String> getKeysLike(String string);

    public String getContentValue(String key, Portal portal);
}
