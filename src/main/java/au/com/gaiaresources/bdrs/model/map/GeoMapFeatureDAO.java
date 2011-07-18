package au.com.gaiaresources.bdrs.model.map;

import java.util.List;

import org.hibernate.Session;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

import au.com.gaiaresources.bdrs.db.TransactionDAO;

public interface GeoMapFeatureDAO extends TransactionDAO {
    GeoMapFeature get(Integer pk);
    GeoMapFeature save(GeoMapFeature feature);
    GeoMapFeature update(GeoMapFeature feature);
    void deleteCascade(GeoMapFeature feature);
    long count();
    
    GeoMapFeature get(Session sesh, Integer pk);
    GeoMapFeature save(Session sesh, GeoMapFeature feature);
    GeoMapFeature update(Session sesh, GeoMapFeature feature);
    void deleteCascade(Session sesh, GeoMapFeature feature);
    
    List<GeoMapFeature> find(Integer mapLayerId);
    List<GeoMapFeature> find(Integer[] mapLayerId, Geometry intersectGeom);
    
    List<GeoMapFeature> find(Session sesh, Integer mapLayerId);
    List<GeoMapFeature> find(Session sesh, Integer[] mapLayerId, Geometry pointIntersect);
}
