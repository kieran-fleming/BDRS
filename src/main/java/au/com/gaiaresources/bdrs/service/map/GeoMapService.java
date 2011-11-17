package au.com.gaiaresources.bdrs.service.map;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.map.GeoMap;
import au.com.gaiaresources.bdrs.model.map.GeoMapDAO;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * A service that provides access to commonly used map retrieval code.
 * @author stephanie
 */
@Service
public class GeoMapService {

    @Autowired
    private GeoMapDAO geoMapDAO;
    
    /**
     * Gets all available maps for the user.
     * @param user The user requesting maps to view
     * @return A list of maps that the user can access
     */
    public List<GeoMap> getAvailableMaps(User user) {
        Boolean anonAccess = user == null ? true : null;
        
        PagedQueryResult<GeoMap> queryResult = geoMapDAO.search(null, null, null, null, anonAccess, true);
        
        return queryResult.getList();
    }
}
