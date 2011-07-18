package au.com.gaiaresources.bdrs.model.map.impl;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernatespatial.GeometryUserType;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Repository
public class GeoMapFeatureDAOImpl extends AbstractDAOImpl implements GeoMapFeatureDAO {

    Logger log = Logger.getLogger(this.getClass());
    
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
        StringBuilder hb = new StringBuilder();
        hb.append("select gmf from GeoMapFeature as gmf inner join gmf.layer as layer where layer.id in (:layerIds)");
        if (pointIntersect != null) {
            hb.append(" ");
            hb.append("and intersects(:geom, gmf.geometry) = true");
        }
        Query q = sesh.createQuery(hb.toString());
        q.setParameterList("layerIds", mapLayerId);
        if (pointIntersect != null) {
            q.setParameter("geom", pointIntersect, GeometryUserType.TYPE);
        }
        return (List<GeoMapFeature>)q.list();
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
}
