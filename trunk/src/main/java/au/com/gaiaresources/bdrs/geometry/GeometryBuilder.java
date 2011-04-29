package au.com.gaiaresources.bdrs.geometry;

import java.awt.geom.Point2D;

import javax.measure.converter.UnitConverter;
import javax.measure.quantity.Length;
import javax.measure.unit.Unit;

import org.geotools.referencing.GeodeticCalculator;
import org.springframework.stereotype.Component;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

@Component("wgs84GeometryBuilder")
public class GeometryBuilder {
    private GeometryFactory factory;
    
    public GeometryBuilder() {
        this(4326);
    }
    
    public GeometryFactory getFactory(){
        return this.factory;
    }
    public GeometryBuilder(int srid) {
        factory = new GeometryFactory(new PrecisionModel(), srid);
    }
    
    public Geometry bufferInM(Point p, Double radiusM) {
        return bufferInKm(p, radiusM / 1000);
    }
    
    @SuppressWarnings("unchecked")
    public Geometry bufferInKm(Point p, Double radiusKm) {
        GeodeticCalculator c = new GeodeticCalculator();
        c.setStartingGeographicPoint(p.getX(), p.getY());
        
        Unit<Length> u = c.getEllipsoid().getAxisUnit();
        Unit<Length> km = (Unit<Length>) Unit.valueOf("km");
        
        if (u.isCompatible(km)) {
            UnitConverter converter = km.getConverterTo(u);
            double converted = converter.convert(radiusKm);
            c.setDirection(0, converted);
            
            Point2D p2 = c.getDestinationGeographicPoint();
            
            double difference = p2.getY() - p.getY();
            Geometry buffer = p.buffer(difference);
            if (buffer.getSRID() != p.getSRID()) {
                buffer.setSRID(p.getSRID());
            }
            return buffer;
        }
        
        throw new IllegalStateException("Unable to convert between " + u + " and " + km);
    }
    
    public Polygon createSquare(double minX, double minY, double width) {
        return createRectangle(minX, minY, width, width);
    }
    
    public Polygon createRectangle(double minX, double minY, double width, double height) {
        LinearRing ring = factory.createLinearRing(new Coordinate[] {
                new Coordinate(minX, minY),
                new Coordinate(minX, minY + height),
                new Coordinate(minX + width, minY + height),
                new Coordinate(minX + width, minY),
                new Coordinate(minX, minY)
            });
        return factory.createPolygon(ring, null);
    }
    
    public Point createPoint(double x, double y) {
        return factory.createPoint(new Coordinate(x, y));
    }
}
