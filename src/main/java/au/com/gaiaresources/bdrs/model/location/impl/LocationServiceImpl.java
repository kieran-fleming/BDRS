package au.com.gaiaresources.bdrs.model.location.impl;

import java.math.BigDecimal;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.location.LocationService;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.PrecisionModel;

@Service
public class LocationServiceImpl implements LocationService {

    private static final double DECIMAL_PLACES_TO_TRUNCATE_TO = 6;
    private static final double TRUNCATE_FACTOR = Math.pow(10, DECIMAL_PLACES_TO_TRUNCATE_TO);
    
    private Logger log = Logger.getLogger(getClass());
    
    private GeometryFactory geometryFactory;

    /**
     * Constructor. Assumes WGS84.
     */
    public LocationServiceImpl() {
        geometryFactory = new GeometryFactory(new PrecisionModel(), 4326);
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
}
