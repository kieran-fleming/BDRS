package au.com.gaiaresources.bdrs.model.showcase;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;

public interface GalleryDAO extends TransactionDAO {
    public Gallery get(Integer pk);
    public Gallery save(Gallery g);
    public Gallery update(Gallery g);
    public void delete(Gallery g);
    public Long count();
    public PagedQueryResult<Gallery> search(PaginationFilter filter, String name, String description);
}
