package au.com.gaiaresources.bdrs.kml;

import java.awt.Color;
import java.io.File;
import java.io.FileOutputStream;

import org.junit.Test;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class TestKMLWriter {
    @Test
    public void testEmptyStyle() throws Exception {
        KMLWriter writer = new KMLWriter();
        writer.createStyle("test");
        writer.write(true, System.out);
    }
    
    @Test
    public void testIconStyle() throws Exception {
        KMLWriter writer = new KMLWriter();
        writer.createStyleIcon("test", "http://www.example.com/icon.png");
        writer.write(true, System.out);
    }
    
    @Test
    public void testPolyStyle() throws Exception {
        KMLWriter writer = new KMLWriter();
        writer.createStylePoly("pstyle", "aa0000ff".toCharArray());
        writer.write(true, System.out);
    }
    
    @Test
    public void testPolyStyleColor() throws Exception {
        KMLWriter writer = new KMLWriter();
        writer.createStylePoly("pstyle", new Color(255, 255, 255, 100));
        writer.write(true, System.out);
        
        writer = new KMLWriter();
        writer.createStylePoly("pstyle", new Color(255, 0, 0));
        writer.write(true, System.out);
    }
    
    
    @Test
    public void testPolygonPlacemark() throws Exception {
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        LinearRing ring = factory.createLinearRing(new Coordinate[] {
                                     new Coordinate(10, 10),
                                     new Coordinate(20, 10),
                                     new Coordinate(20, 20),
                                     new Coordinate(10, 20),
                                     new Coordinate(10, 10)
                                 });
        
        Polygon p = factory.createPolygon(ring, null);
        KMLWriter writer = new KMLWriter();
        writer.createFolder("PolygonTest");
        writer.createPlacemark("PolygonTest", "poly", p);
        
        writer.write(true, System.out);
    }
    
    @Test
    public void testHalfDegreeKML() throws Exception {
        int minX = 111;
        int maxX = 155;
        int minY = -44;
        int maxY = -10;
        
        GeometryFactory factory = new GeometryFactory(new PrecisionModel(), 4326);
        
        KMLWriter writer = new KMLWriter();
        writer.createFolder("PolygonTest");
        
        for (double d = minX; d < maxX; d = d + 0.5) {
            for (double e = minY; e < maxY; e = e + 0.5) {
                LinearRing r = factory.createLinearRing(new Coordinate[] {
                                                new Coordinate(d, e),
                                                new Coordinate(d + 0.49, e),
                                                new Coordinate(d + 0.49, e + 0.49),
                                                new Coordinate(d, e + 0.49),
                                                new Coordinate(d, e)
                                            });
                Polygon p = factory.createPolygon(r, null);
                writer.createPlacemark("PolygonTest", "" + e + d, p);
            }
        }
        
        writer.write(false, new FileOutputStream(new File("C:\\outputkml.kml")));
    }
}
