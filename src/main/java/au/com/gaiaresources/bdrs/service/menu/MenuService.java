package au.com.gaiaresources.bdrs.service.menu;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.map.GeoMap;
import au.com.gaiaresources.bdrs.model.menu.MenuDAO;
import au.com.gaiaresources.bdrs.model.menu.MenuItem;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.map.GeoMapService;

/**
 * Creates menus based on user credentials. This class assembles {@link MenuItem MenuItems}
 * from the {@link MenuDAO}, creates Contribute menu items from user surveys, and 
 * creates Review menu items from user and public maps
 * 
 * @author stephanie
 */
@Service
public class MenuService {

    /**
     * The name of the menu that contains map menu items.
     */
    private static final String REVIEW_MENU = "Review";
    /**
     * The name of the menu that contains survey menu items.
     */
    private static final String CONTRIBUTE_MENU = "Contribute";
    
    @Autowired
    private MenuDAO menuDAO;
    
    @Autowired
    private GeoMapService mapService;
    
    @Autowired
    private SurveyDAO surveyDAO;
    
    /**
     * Returns a list of {@link MenuItem MenuItems} that the {@link User} can access.
     * This includes dynamically generated Contribute and Review menus.
     * @param user The user accessing the page
     * @return A List of MenuItems representing a menu in a view
     */
    public List<MenuItem> getMenus(User user) {
        // get the menus from the DAO
        List<MenuItem> menu = menuDAO.getUserMenus(user);
        // create the map items
        List<GeoMap> maps = mapService.getAvailableMaps(user);
        List<MenuItem> mapMenu = createMapMenu(maps);
        List<Survey> surveys = user != null ? surveyDAO.getActiveSurveysForUser(user) : null;
        List<MenuItem> surveyMenu = createSurveyMenu(surveys);
        
        for (MenuItem menuItem : menu) {
            // insert the mapMenu at the appropriate item
            if (REVIEW_MENU.equals(menuItem.getName())) {
                // insert at the end of the menu's items
                menuItem.getItems().addAll(mapMenu);
            } else if (CONTRIBUTE_MENU.equals(menuItem.getName())) {
                // insert the surveyMenu at the appropriate item
                menuItem.getItems().addAll(surveyMenu);
            }
        }
        
        return menu;
    }

    /**
     * Create the menu items for the surveys
     * @param surveys A list of surveys to create menus for
     * @return A list of menu items representing the user surveys
     */
    private List<MenuItem> createSurveyMenu(List<Survey> surveys) {
        List<MenuItem> menu = new ArrayList<MenuItem>();
        if (surveys != null) {
            for (Survey survey : surveys) {
                MenuItem item = new MenuItem(survey.getName(), 
                                             "bdrs/user/surveyRenderRedirect.htm?surveyId="+survey.getId(), 
                                             survey.getDescription(), 
                                             createCensusMethodMenu(survey));
                menu.add(item);
            }
        }
        return menu;
    }

    /**
     * Create the menu items for the census methods
     * @param survey The survey to create a census method menu for
     * @return A list of menu items representing survey census methods
     */
    private List<MenuItem> createCensusMethodMenu(Survey survey) {
        List<MenuItem> menu = new ArrayList<MenuItem>();
        if (survey.isDefaultCensusMethodProvided()) {
            // cover the default case...
            MenuItem item = new MenuItem(CensusMethod.DEFAULT_NAME, 
                                         "bdrs/user/surveyRenderRedirect.htm?surveyId="+survey.getId()+"&censusMethodId=0", 
                                         null);
            menu.add(item);
        }
        for (CensusMethod censusMethod : survey.getCensusMethods()) {
            MenuItem item = new MenuItem(censusMethod.getName(), 
                                         "bdrs/user/surveyRenderRedirect.htm?surveyId="+survey.getId()+"&censusMethodId="+censusMethod.getId(), 
                                         censusMethod.getDescription(),
                                         null);
            menu.add(item);
        }
        return menu;
    }

    /**
     * Creates the menu items for review maps
     * @param maps A list of maps to create menus for
     * @return A list of menu items representing user maps
     */
    private List<MenuItem> createMapMenu(List<GeoMap> maps) {
        List<MenuItem> menu = new ArrayList<MenuItem>();
        for (GeoMap map : maps) {
            MenuItem item = new MenuItem(map.getName(), 
                                         "bdrs/map/view.htm?geoMapId=" + map.getId(), 
                                         map.getDescription(),
                                         null);
            menu.add(item);
        }
        return menu;
    }
}
