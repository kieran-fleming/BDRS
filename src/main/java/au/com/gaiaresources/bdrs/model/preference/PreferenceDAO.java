package au.com.gaiaresources.bdrs.model.preference;

import java.util.List;
import java.util.Map;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;

/**
 * Performs database access for {@link Preference} and
 * {@link PreferenceCategory}.
 */
public interface PreferenceDAO extends TransactionDAO {

    /**
     * Returns the <code>PreferenceCategory</code> for the specified primary
     * key.
     * 
     * @param pk
     *            the primary key of the <code>PreferenceCategory</code>
     * @return the <code>PreferenceCategory</code> for the specified primary key
     *         or null if one does not exist.
     */
    public PreferenceCategory getPreferenceCategory(Integer pk);

    /**
     * Returns the <code>Preference</code> for the specified primary key.
     * 
     * @param pk
     *            the primary key of the <code>Preference</code>
     * @return the <code>Preference</code> for the specified primary key or null
     *         if one does not exist.
     */
    public Preference getPreference(Integer pk);

    /**
     * Returns the <code>Preference</code> for the specified key in the current
     * portal.
     * 
     * @param key
     *            the unique key (not primary key) of the
     *            <code>Preference</code>.
     * @return the <code>Preference</code> for the specified key in the current
     *         portal.
     */
    public Preference getPreferenceByKey(String key);

    /**
     * Returns the <code>Preference</code> for the specified key in the current
     * portal.
     * 
     * @param sesh the session to use when accessing the database.
     * @param key
     *            the unique key (not primary key) of the
     *            <code>Preference</code>.
     * @return the <code>Preference</code> for the specified key in the current
     *         portal.
     */
    public Preference getPreferenceByKey(Session sesh, String key);

    /**
     * Gets all preferences for the current <code>Portal</code>.
     * @return map of all preferences in the current <code>Portal</code> where
     * the key is the key of the <code>Preference</code> and the value is the
     * <code>Preference</code> object.
     */
    public Map<String, Preference> getPreferences();

    /**
     * Gets all preferences for the current <code>Portal</code>.
     * @param sesh the session to use when retrieving preferences.
     * @return map of all preferences in the current <code>Portal</code> where
     * the key is the key of the <code>Preference</code> and the value is the
     * <code>Preference</code> object.

     */
    public Map<String, Preference> getPreferences(Session sesh);

    /**
     * Saves the specified preference to the database.
     * @param pref the preference to be saved to the database.
     * @return the preference that was saved to the database.
     */
    public Preference save(Preference pref);

    /**
     * Saves the specified preference to the database.
     * @param sesh the session to use when accessing the database.
     * @param pref the preference to be saved to the database.
     * @return the preference that was saved to the database.
     */
    public Preference save(Session sesh, Preference pref);

    /***
     * Removes the specified preference from the database.
     * @param sesh the session to use for database access.
     * @param pref the preference to be removed.
     */
    public void delete(Session sesh, Preference pref);

    /***
     * Removes the specified preference from the database.
     * @param pref the preference to be removed.
     */
    public void delete(Preference pref);
    
    public List<Preference> getPreferenceByKeyPrefix(String prefix);
    
    public PreferenceCategory save(Session sesh, PreferenceCategory cat);
    
    /*
     * Returns all of the preference categories
     */
    public List<PreferenceCategory> getPreferenceCategories();
}
