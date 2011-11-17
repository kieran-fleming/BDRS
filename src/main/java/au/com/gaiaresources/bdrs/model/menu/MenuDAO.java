package au.com.gaiaresources.bdrs.model.menu;

import java.util.List;

import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Interface for creating and persisting {@link MenuItem MenuItems}.
 * 
 * @author stephanie
 */
public interface MenuDAO {
    
    /**
     * Returns the highest level {@link MenuItem MenuItems}, complete with submenus, 
     * but only ones that this user can access.
     * @param user The {@link User} for which to retrieve menus. Retrieves menus 
     *             with anonymous access only when this is null.
     * @return An ordered {@link List} of {@link MenuItem MenuItems} that 
     *         represent the menu to display on the page.
     */
    public List<MenuItem> getUserMenus(User user);
}
