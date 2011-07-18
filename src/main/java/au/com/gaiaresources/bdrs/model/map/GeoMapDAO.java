package au.com.gaiaresources.bdrs.model.map;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;

public interface GeoMapDAO extends TransactionDAO {
    GeoMap save(GeoMap obj);
    GeoMap update(GeoMap obj);
    GeoMap get(Integer pk);
    void delete(GeoMap obj);
    PagedQueryResult<GeoMap> search(PaginationFilter filter, String name, String description, Integer geoMapPk, Boolean anonAccess,
                                    Boolean publish);
}
