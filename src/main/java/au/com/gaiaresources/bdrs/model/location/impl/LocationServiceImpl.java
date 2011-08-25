package au.com.gaiaresources.bdrs.model.location.impl;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.location.LocationService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;
import com.vividsolutions.jts.io.WKTReader;

@Service
public class LocationServiceImpl implements LocationService {

    private static final double DECIMAL_PLACES_TO_TRUNCATE_TO = 6;
    private static final double TRUNCATE_FACTOR = Math.pow(10, DECIMAL_PLACES_TO_TRUNCATE_TO);
    
    private Logger log = Logger.getLogger(getClass());
    
    private GeometryFactory geometryFactory;
    private WKTReader wktReader;

    /**
     * Constructor. Assumes WGS84.
     */
    public LocationServiceImpl() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
        wktReader = new WKTReader(geometryFactory);
    }

    @Override
    public GeometryFactory getGeometryFactory() {
        return geometryFactory;
    }

    @Override
    public Point createPoint(BigDecimal locationLatitude,
            BigDecimal locationLongitude) {
        return this.createPoint(locationLatitude.doubleValue(), locationLongitude.doubleValue());
    }

    @Override
    public Point createPoint(double latitude, double longitude) {
        return this.geometryFactory.createPoint(new Coordinate(truncate(longitude),
                truncate(latitude)));
    }

    private double truncate(double x) {
        return x > 0 ? (Math.floor(x * TRUNCATE_FACTOR)) / TRUNCATE_FACTOR : (Math.ceil(x * TRUNCATE_FACTOR)) / TRUNCATE_FACTOR;
    }

    /**
     * We can support point, polygon (which gets turned into a multi polygon) and
     * multi polygon.
     * 
     */
    @Override
    public Geometry createGeometryFromWKT(String wktString) {
        
        Geometry geom = null;
        try {
            geom = wktReader.read(wktString);
        } catch (Exception e) {
            log.error("Error occurred parsing WKT string:", e);
        }
        return convertToMultiGeom(geom);
    }
    
    @Override
    public Geometry convertToMultiGeom(Geometry geom) {
        if (geom instanceof MultiPoint) {
            throw new IllegalArgumentException("wkt string is multi point which is not supported");
        }
        
        // convert to multi linestring!
        if (geom instanceof LineString) {
            LineString ls = (LineString)geom;
            geom = new MultiLineString(new LineString[] { ls }, geometryFactory);
        }
        
        // we can't store polygons, convert to multi polygon!
        if (geom instanceof Polygon) {
            Polygon poly = (Polygon)geom;
            geom = new MultiPolygon(new Polygon[] { poly }, geometryFactory);
        }
        return geom;
    }
}
