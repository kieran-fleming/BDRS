package au.com.gaiaresources.bdrs.model.map;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;

public interface GeoMapLayerDAO extends TransactionDAO {
    GeoMapLayer save(GeoMapLayer obj);
    GeoMapLayer update(GeoMapLayer obj);
    GeoMapLayer get(Integer pk);
    
    GeoMapLayer save(Session sesh, GeoMapLayer obj);
    GeoMapLayer update(Session sesh, GeoMapLayer obj);
    GeoMapLayer get(Session sesh, Integer pk);
    
    PagedQueryResult<GeoMapLayer> search(PaginationFilter filter, String name, String description);
    
    /**
     * Gets the AssignedGeoMapLayer(s) for a GeoMap
     * @param geoMapPk pk of the GeoMap object to retrieve assigned layers for
     * @return the query result
     */
    List<AssignedGeoMapLayer> getForMap(Integer geoMapPk);
    
    /**
     * Saves the collection of AssignedGeoMapLayers. Will preserve the order of the list
     * @param assignedLayerList list to save
     * @return the saved list
     */
    List<AssignedGeoMapLayer> save(List<AssignedGeoMapLayer> assignedLayerList);
    
    /**
     * Deletes the list 
     * @param assignedLayerList - the list to delete
     */
    void delete(List<AssignedGeoMapLayer> assignedLayerList);
}
