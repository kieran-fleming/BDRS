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
}
