package au.com.gaiaresources.bdrs.geometry;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.geotools.geometry.jts.JTS;
import org.geotools.referencing.CRS;
import org.junit.Test;
import org.opengis.referencing.crs.CoordinateReferenceSystem;
import org.opengis.referencing.operation.MathTransform;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class TestGeometryBuilder {
    @Test
    public void testBufferInKm() {
        GeometryBuilder builder = new GeometryBuilder();
        
        GeometryFactory f = new GeometryFactory(new PrecisionModel(), 4326);
        Point p = f.createPoint(new Coordinate(10, 10));
        
        Geometry g = builder.bufferInKm(p, 10.0);
        
        assertEquals(4326, g.getSRID());
        
        Point top = f.createPoint(new Coordinate(10, 10.09));
        Point bottom = f.createPoint(new Coordinate(10, 9.91));
        Point right = f.createPoint(new Coordinate(10.09, 10));
        Point left = f.createPoint(new Coordinate(9.91, 10));
        
        assertTrue(g.contains(top));
        assertTrue(g.contains(left));
        assertTrue(g.contains(right));
        assertTrue(g.contains(bottom));
        
        Point outTop = f.createPoint(new Coordinate(10, 10.1));
        Point outBottom = f.createPoint(new Coordinate(10, 9.9));
        Point outRight = f.createPoint(new Coordinate(10.1, 10));
        Point outLeft = f.createPoint(new Coordinate(9.9, 10));
        
        assertFalse(g.contains(outTop));
        assertFalse(g.contains(outLeft));
        assertFalse(g.contains(outRight));
        assertFalse(g.contains(outBottom));
        
        Point outNE = f.createPoint(new Coordinate(10.09, 10.09));
        Point outSE = f.createPoint(new Coordinate(10.09, 9.91));
        Point outSW = f.createPoint(new Coordinate(9.91, 9.91));
        Point outNW = f.createPoint(new Coordinate(9.91, 10.09));
        
        assertFalse(g.contains(outNE));
        assertFalse(g.contains(outSE));
        assertFalse(g.contains(outSW));
        assertFalse(g.contains(outNW));
    }
    
    @Test
    public void testCreateSquare() {
        GeometryBuilder builder = new GeometryBuilder();
        
        Polygon p = builder.createSquare(111, -44, 2);
        System.out.println(p.toText());
    }
    
    //@Test
    public void testGoogleProjection() throws Exception {
        GeometryBuilder builder = new GeometryBuilder(4326);
        Polygon p = builder.createSquare(115, -32, 10);
        System.out.println(p.getSRID());
        
        String crs900913 = "PROJCS[\"WGS84 / Simple Mercator\", GEOGCS[\"WGS 84\", DATUM[\"WGS_1984\", SPHEROID[\"WGS_1984\", 6378137.0, 298.257223563]], "
                                                  + "PRIMEM[\"Greenwich\", 0.0],"
                                                  + "UNIT[\"degree\", 0.017453292519943295],"
                                                  + "AXIS[\"Longitude\", EAST],"
                                                  + "AXIS[\"Latitude\", NORTH]],"
                                                  + "PROJECTION[\"Mercator_1SP\"],"
                                                  + "PARAMETER[\"latitude_of_origin\", 0.0],"
                                                  + "PARAMETER[\"central_meridian\", 0.0],"
                                                  + "PARAMETER[\"scale_factor\", 1.0],"
                                                  + "PARAMETER[\"false_easting\", 0.0],"
                                                  + "PARAMETER[\"false_northing\", 0.0], UNIT[\"m\", 1.0],"
                                                  + "AXIS[\"x\", EAST], AXIS[\"y\", NORTH], AUTHORITY[\"EPSG\",\"900913\"]]";
        
        CoordinateReferenceSystem googleCRS = CRS.parseWKT(crs900913);
        CoordinateReferenceSystem wgs84 = CRS.decode("EPSG:4326");
        
        MathTransform t = CRS.findMathTransform(wgs84, googleCRS);
        
        Geometry g = JTS.transform(p, t);
        
        System.out.println(g);
        
        Point[] points = new Point[] {
                builder.createPoint(0, 0),
                builder.createPoint(0, 5),
                builder.createPoint(0, 10),
                builder.createPoint(0, 15),
                builder.createPoint(0, 20),
                builder.createPoint(0, 25)
        };
        
        Geometry[] geoms = new Geometry[] {
                JTS.transform(points[0], t),
                JTS.transform(points[1], t),
                JTS.transform(points[2], t),
                JTS.transform(points[3], t),
                JTS.transform(points[4], t),
                JTS.transform(points[5], t)
        };
        
        
        for (int i = 0; i < 5; i++) {
            System.out.println(GeometryUtils.findMinY(geoms[i]) - GeometryUtils.findMinY(geoms[i + 1]));
        }
        
        Point bl = builder.createPoint(111, -44);
        Point tr = builder.createPoint(155, -10);
        
        System.out.println(JTS.transform(bl, t) + " " + JTS.transform(tr, t));
    }
}
