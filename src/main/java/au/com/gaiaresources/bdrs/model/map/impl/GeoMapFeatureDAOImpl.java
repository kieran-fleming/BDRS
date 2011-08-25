package au.com.gaiaresources.bdrs.model.map.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.QueryPaginator;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

import com.vividsolutions.jts.geom.Geometry;

@Repository
public class GeoMapFeatureDAOImpl extends AbstractDAOImpl implements GeoMapFeatureDAO {

    Logger log = Logger.getLogger(this.getClass());
    
    public static final int DEFAULT_MAX_RESULTS = 10;
    
    @Override
    public void deleteCascade(GeoMapFeature feature) {
        deleteCascade(getSession(), feature);
    }

    @Override
    public GeoMapFeature get(Integer pk) {
        return super.getByID(GeoMapFeature.class, pk);
    }

    @Override
    public GeoMapFeature save(GeoMapFeature feature) {
        return super.save(feature);
    }

    @Override
    public GeoMapFeature update(GeoMapFeature feature) {
        return super.update(feature);
    }
    
    @Override
    public GeoMapFeature get(Session sesh, Integer pk) {
        return super.getByID(sesh, GeoMapFeature.class, pk);
    }
    
    @Override
    public GeoMapFeature save(Session sesh, GeoMapFeature feature) {
        return super.save(sesh, feature);
    }
    
    @Override
    public GeoMapFeature update(Session sesh, GeoMapFeature feature) {
        return super.save(sesh, feature);
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public void deleteCascade(Session sesh, GeoMapFeature feature) {
        Set<AttributeValue> recAttrToDelete = new HashSet<AttributeValue>();
        Set<AttributeValue> recAttrSet = feature.getAttributes();
        recAttrToDelete.addAll(feature.getAttributes());
        feature.setAttributes(Collections.EMPTY_SET);
        for (AttributeValue ra : recAttrSet) {
            super.update(sesh, ra);
        }
        delete(sesh, feature);
        for (AttributeValue ra : recAttrToDelete) {
            super.delete(sesh, ra);
        }
    }
    
    @Override
    public List<GeoMapFeature> find(Session sesh, Integer[] mapLayerId, Geometry pointIntersect) {
        return find(sesh, mapLayerId, pointIntersect, null).getList();
    }

    @Override
    public List<GeoMapFeature> find(Integer[] mapLayerId, Geometry pointIntersect) {
        return find(getSession(), mapLayerId, pointIntersect);
    }
    
    @Override
    public List<GeoMapFeature> find(Integer mapLayerId) {
        return find(new Integer[] { mapLayerId }, null);
    }
    
    @Override
    public long count() {
        return super.count(GeoMapFeature.class);
    }
    
    @Override
    public List<GeoMapFeature> find(Session sesh, Integer mapLayerId) {
        return find(sesh, new Integer[] { mapLayerId}, null);
    }

    @Override
    public PagedQueryResult<GeoMapFeature> find(Integer[] mapLayerId,
            Geometry intersectGeom, PaginationFilter filter) {
        return find(getSession(), mapLayerId, intersectGeom, filter);
    }

    @Override
    public PagedQueryResult<GeoMapFeature> find(Session sesh,
            Integer[] mapLayerId, Geometry pointIntersect,
            PaginationFilter filter) {
        
        if (mapLayerId == null || mapLayerId.length == 0) {
            mapLayerId = new Integer[] { 0 };
        }
        
        Map<String, Object> params = new HashMap<String, Object>();
                
        StringBuilder hb = new StringBuilder();
        hb.append("select gmf from GeoMapFeature as gmf inner join gmf.layer as layer where layer.id in (:layerIds)");
        if (pointIntersect != null) {
            hb.append(" and st_intersects(:geom, gmf.geometry) = true");
        }
        params.put("layerIds", java.util.Arrays.asList(mapLayerId));

        if (pointIntersect != null) {
            params.put("geom", pointIntersect);
        }
        return new QueryPaginator<GeoMapFeature>().page(sesh, hb.toString(), params, filter, "gmf");
    }
}
