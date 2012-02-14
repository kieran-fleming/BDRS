package au.com.gaiaresources.bdrs.model.menu.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.menu.MenuItem;
import au.com.gaiaresources.bdrs.model.menu.MenuDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.FileUtils;

/**
 * Creates and accesses menus from a JSON file.
 * 
 * @author stephanie
 */
public class JsonMenuDAOImpl implements MenuDAO {

    private Logger log = Logger.getLogger(getClass());
    
    protected JSONArray array;
    
    /**
     * Constants for accessing entries in the JSONObjects.
     */
    private static final String NAME = "name";
    private static final String PATH = "path";
    private static final String ITEMS = "items";
    private static final String ROLES = "roles";
    
    protected static final String CONFIG_NAME = "menu.config";
    /**
     * Reads the static menus from the menu.config file.
     */
    public JsonMenuDAOImpl() {
        // read the file
        InputStream configStream = MenuItem.class.getResourceAsStream(CONFIG_NAME);
        try {
            readMenuFromStream(configStream);
        } finally {
            if (configStream != null) {
                try {
                    configStream.close();
                } catch (IOException e) {
                    log.warn("Error closing menu config file", e);
                }
            }
        }
    }

    /**
     * Reads a menu structure from an {@link InputStream}.
     * @param configStream
     */
    protected void readMenuFromStream(InputStream configStream) {
        try {
            array = (JSONArray) FileUtils.readJsonStream(configStream);
        } catch (Exception e) {
            log.error("Error loading menu config", e);
            array = null;
        } finally {
            try {
                if (configStream != null) {
                    configStream.close();
                }
            } catch (IOException e) {
                log.warn("Error closing menu config file", e);
            }
        }
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.menu.MenuItemDAO#getUserMenus(au.com.gaiaresources.bdrs.model.user.User)
     */
    @Override
    public List<MenuItem> getUserMenus(User user) {
        List<MenuItem> menu = new ArrayList<MenuItem>();
        
        addMenuItemsToList(menu, array, user);
        
        return menu;
    }

    /**
     * Converts the {@link JSONArray} of menu items into a {@link List} of {@link MenuItem}
     * objects. Will only add menu items that have one of roles that the {@link User} has.
     * @param menu The list to add {@link MenuItem MenuItems} to
     * @param menuItems The {@link JSONArray} of menu items to convert to objects
     * @param user The {@link User} to get menus for
     */
    private void addMenuItemsToList(List<MenuItem> menu, JSONArray menuItems, User user) {
        if (menuItems == null) {
            return;
        }
        
        for (int i = 0; i < menuItems.size(); i++) {
            JSONObject menuItem = menuItems.optJSONObject(i);
            if (menuItem != null) {
                String name = menuItem.optString(NAME);
                String path = menuItem.optString(PATH);
                JSONArray subArr = menuItem.optJSONArray(ITEMS);
                List<MenuItem> subMenu = null;
                if (subArr != null) {
                    subMenu = new ArrayList<MenuItem>(subArr.size());
                    addMenuItemsToList(subMenu, subArr, user);
                }
                MenuItem mi = new MenuItem(name, path, subMenu);
                JSONArray rolesArray = menuItem.optJSONArray(ROLES);
                if (rolesArray != null) {
                    // boolean flag to break out of loop when a menu item has 
                    // been added to avoid duplicates
                    boolean itemAdded = false;
                    for (int j = 0; j < rolesArray.size() && !itemAdded; j++) {
                        String roleString = rolesArray.optString(j);
                        // if the user is null, it is an anonymous user
                        if ((user == null && Role.ANONYMOUS.equals(roleString)) || 
                                (user != null && user.hasRole(roleString))) {
                            // add the menu item as soon as a matching role is found
                            menu.add(mi);
                            itemAdded = true;
                        }
                    }
                }
            }
        }
    }
}
