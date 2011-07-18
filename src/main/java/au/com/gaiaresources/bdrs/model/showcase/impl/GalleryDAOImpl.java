package au.com.gaiaresources.bdrs.model.showcase.impl;

import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.showcase.Gallery;
import au.com.gaiaresources.bdrs.model.showcase.GalleryDAO;

@Repository
public class GalleryDAOImpl extends AbstractDAOImpl implements GalleryDAO {
    
    @Override
    public Gallery get(Integer pk) {
        return super.getByID(Gallery.class, pk);
    }
    
    @Override
    public Gallery save(Gallery g) {
        return super.save(g);
    }
    
    @Override
    public Gallery update(Gallery g) {
        return super.update(g);
    }
    
    @Override
    public void delete(Gallery g) {
        super.delete(g);
    }
    
    @Override
    public Long count() {
        return super.count(Gallery.class);
    }
    
    @Override
    public PagedQueryResult<Gallery> search(PaginationFilter filter, String name, String description) {
        HqlQuery q = new HqlQuery("from Gallery g ");
            
        if (StringUtils.hasLength(name)) {
            q.and(Predicate.ilike("g.name", name + "%"));
        }
        if (StringUtils.hasLength(description)) {
            q.and(Predicate.ilike("g.description", description + "%"));
        }
        return new QueryPaginator<Gallery>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter);
    }
}
