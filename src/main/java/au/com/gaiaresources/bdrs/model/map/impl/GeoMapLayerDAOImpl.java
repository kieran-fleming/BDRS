package au.com.gaiaresources.bdrs.model.map.impl;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.Predicate;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.map.AssignedGeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;

@Repository
public class GeoMapLayerDAOImpl extends AbstractDAOImpl implements GeoMapLayerDAO {

    private Logger log = Logger.getLogger(getClass());
    
    @Override
    public GeoMapLayer get(Integer pk) {
        return super.getByID(GeoMapLayer.class, pk);
    }

    @Override
    public GeoMapLayer save(GeoMapLayer obj) {
        return super.save(obj);
    }

    @Override
    public PagedQueryResult<GeoMapLayer> search(PaginationFilter filter, String name, String description) {
        
        String sortTargetAlias = "ml";
        HqlQuery q;
      
        q = new HqlQuery("from GeoMapLayer ml ");
        
        if (StringUtils.hasLength(name)) {
            q.and(Predicate.ilike("ml.name", name + "%"));
        }
        if (StringUtils.hasLength(description)) {
            q.and(Predicate.ilike("ml.description", description + "%"));
        }
        return new QueryPaginator<GeoMapLayer>().page(this.getSession(), q.getQueryString(), q.getParametersValue(), filter, sortTargetAlias);
    }

    @Override
    public GeoMapLayer update(GeoMapLayer obj) {
        return super.update(obj);
    }
    
    
    private AssignedGeoMapLayer save(AssignedGeoMapLayer obj) {
        return super.save(obj);
    }
    
    private AssignedGeoMapLayer update(AssignedGeoMapLayer obj) {
        return super.update(obj);
    }
    
    private void delete(AssignedGeoMapLayer obj) {
        super.delete(obj);
    }
    
    @Override
    public List<AssignedGeoMapLayer> getForMap(Integer mapPk) {        
        if (mapPk == null || mapPk == 0) {
            return new ArrayList<AssignedGeoMapLayer>();
        }
        Query query = getSession().createQuery("select l from AssignedGeoMapLayer l inner join l.map m where m.id = " + mapPk.toString() + " order by l.weight");
        List<AssignedGeoMapLayer> result = (List<AssignedGeoMapLayer>)query.list();
        if (result == null || result.size() == 0) {
            return new ArrayList<AssignedGeoMapLayer>();
        }
        return result;
    }
    
    @Override
    public List<AssignedGeoMapLayer> save(List<AssignedGeoMapLayer> assignedLayerList) {
        if (assignedLayerList == null) {
            return assignedLayerList;
        }
        for (int i=0; i<assignedLayerList.size(); ++i) {
            AssignedGeoMapLayer asLayer = assignedLayerList.get(i);
            asLayer.setWeight(i);
            save(asLayer);
        }
        return assignedLayerList;
    }
    
    @Override
    public void delete(List<AssignedGeoMapLayer> assignedLayerList) {
        if (assignedLayerList == null) {
            return;
        }
        for (int i=0; i<assignedLayerList.size(); ++i) {
            AssignedGeoMapLayer asLayer = assignedLayerList.get(i);
            asLayer.setWeight(i);
            delete(asLayer);
        }
        assignedLayerList.clear();
    }
    
    @Override
    public GeoMapLayer save(Session sesh, GeoMapLayer obj) {
        return super.save(sesh, obj);
    }
    
    @Override
    public GeoMapLayer update(Session sesh, GeoMapLayer obj) {
        return super.update(sesh, obj);
    }
    
    @Override
    public GeoMapLayer get(Session sesh, Integer pk) {
        return super.getByID(sesh, GeoMapLayer.class, pk);
    }
}
