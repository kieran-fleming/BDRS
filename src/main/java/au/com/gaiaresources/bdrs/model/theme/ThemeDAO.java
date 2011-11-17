package au.com.gaiaresources.bdrs.model.theme;

import java.util.List;

import au.com.gaiaresources.bdrs.model.portal.Portal;

/**
 * Performs all database access for <code>Theme</code>s and <code>ThemeElement</code>s.
 */ 
public interface ThemeDAO {

    /**
     * @return all themes for the current portal.
     */
    public List<Theme> getThemes();
    
    /**
     * @param portal the portal associated with the themes requested.
     * @return all themes for the specified portal.
     */
    public List<Theme> getThemes(Portal portal);
    
    /**
     * Gets the theme with the specified ID regardless of the current portal.
     * @param themeId the primary key of the theme.
     * @return the theme specified by the id or null if one does not exist.
     */
    public Theme getTheme(int themeId);

    /**
     * Saves the specified theme to the database.
     * @param theme the theme to be saved.
     * @return the persisted instance.
     */
    public Theme save(Theme theme);

    /**
     * Deletes the specified <code>ThemeElement</code> from the database.
     * @param themeElement the item to be removed.
     */
    public void delete(ThemeElement themeElement);

    /**
     * Saves the specified theme element to the database.
     * @param themeElement the element to be saved.
     * @return the persisted theme element.
     */
    public ThemeElement save(ThemeElement themeElement);

    /**
     * Gets the active theme for the specified portal or null if one does not exist.
     * @param portal the portal associated with the active theme to be retrieved.
     * @return the active theme for the specified portal.
     */
    public Theme getActiveTheme(Portal portal);
    
    /**
     * Gets the theme element for the specified primary key no matter the portal.
     * @param themeElementId the primary key of the theme element.
     * @return the theme element specified by the primary key.
     */
    public ThemeElement getThemeElement(int themeElementId);
    
    /**
     * Gets the list of customised theme pages for the theme id no matter the portal
     * @param themeId the primary key of the theme to get pages for
     * @return the list of pages
     */
    public List<ThemePage> getThemePages(int themeId);
    
    /**
     * Get the theme page for the given theme id and the page key
     * @param themeId the primary key of the theme the page belongs to
     * @param key the unique key that links the page to a view
     * @return
     */
    public ThemePage getThemePage(int themeId, String key);
    
    /**
     * Deletes the theme page
     * @param page the page to be deleted
     */
    public void delete(ThemePage page);
    
    /**
     * Saves a theme page
     * @param page the page to be saved
     * @return the saved page
     */
    public ThemePage save(ThemePage page);

    /**
     * Gets the default theme for the specified portal or null if one does not exist.
     * @param portal the portal associated with the default theme to be retrieved.
     * @return the default theme for the specified portal.
     */
    public Theme getDefaultTheme(Portal portal);

    /**
     * Gets a Theme with a given name for the specified portal.
     * @param portal The portal to get the theme from
     * @param name The name of the theme to get
     * @return The theme with name name in portal portal
     */
    public Theme getTheme(Portal portal, String name);

    /**
     * Gets a theme element by theme id and key.
     * @param themeId The id of the theme to get the element from
     * @param key The key of the theme element
     * @return The theme element matching the key for the theme with id themeId
     */
    public ThemeElement getThemeElement(int themeId, String key);
}
