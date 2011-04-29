package au.com.gaiaresources.bdrs.model.location;

import java.math.BigDecimal;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

public interface LocationService {

    public GeometryFactory getGeometryFactory();

    public Point createPoint(BigDecimal locationLatitude,
            BigDecimal locationLongitude);

    public Point createPoint(double latitude, double longitude);
}
