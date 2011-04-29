package au.com.gaiaresources.bdrs.wms;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.imageio.ImageIO;

import org.geotools.data.DataUtilities;
import org.geotools.data.memory.MemoryFeatureCollection;
import org.geotools.feature.simple.SimpleFeatureImpl;
import org.geotools.filter.identity.FeatureIdImpl;
import org.geotools.map.DefaultMapContext;
import org.geotools.map.MapContext;
import org.geotools.referencing.CRS;
import org.geotools.renderer.lite.StreamingRenderer;
import org.geotools.styling.Style;
import org.geotools.styling.StyleBuilder;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Polygon;
import com.vividsolutions.jts.geom.PrecisionModel;

public class TestWMS {
    private static Random r = new Random(System.currentTimeMillis());
    @Test
    public void test1() throws Exception {
        Logger.getLogger("org.geotools.styling").setLevel(Level.FINEST);
        GeometryFactory geomFac = new GeometryFactory(new PrecisionModel(), 4326);
        //WebMapServer s = new WebMapServer()
        
        
        SimpleFeatureType featureType = DataUtilities.createType("Feature", "*geom:Polygon:srid=4326,count:0");
        MemoryFeatureCollection coll = new MemoryFeatureCollection(featureType);
        
        
        coll.add(createPoly1(featureType, geomFac));
        coll.add(createPoly2(featureType, geomFac));
        coll.add(createPoly3(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        coll.add(createRandomPoly(featureType, geomFac));
        
        
//        FilterFactory filterFactory = CommonFactoryFinder.getFilterFactory(null);
//        Literal noughtPointFive = filterFactory.literal(0.5);
        
        StyleBuilder styleBuilder = new StyleBuilder();
//        StyleFactory styleFactory = styleBuilder.getStyleFactory();
//        Expression red = styleBuilder.colorExpression(Color.RED);
        
        
        //Function func = filterFactory.function("hello", red);
        
        String[] colours = new String[] {"aa0000", "bb0000", "cc0000", "dd0000", "ee0000", "ff0000"};
        Style cs = styleBuilder.buildClassifiedStyle(coll, "count", colours, featureType);
        //cs.
        
//        Style s = new BasicPolygonStyle(styleFactory.createFill(red), 
//                                        styleFactory.createStroke(red, noughtPointFive));
        
        MapContext mapContext = new DefaultMapContext(CRS.decode("EPSG:4326"));
        mapContext.addLayer(coll, cs);
        
        
        
        StreamingRenderer renderer = new StreamingRenderer();
        renderer.setContext(mapContext);
        
        BufferedImage image = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = image.createGraphics();
        renderer.paint(g, new Rectangle(100, 100), mapContext.getLayerBounds());
        
        ImageIO.write(image, "png", new File("C:\\outputfile.png"));
        
        g.dispose();
        
        
        //renderer.
        //FeatureType featureType = new SimpleFeatureType(null, null, null, null, null);
        //FeatureCollection featureCollection = new MemoryFeatureCollection(null);
    }
    
    private SimpleFeature createPoly1(SimpleFeatureType featureType, GeometryFactory geomFac) {
     // Poly 1
        LinearRing r = geomFac.createLinearRing(new Coordinate[] {
                new Coordinate(0, 0),
                new Coordinate(0, 10),
                new Coordinate(10, 10),
                new Coordinate(0, 0)
        });
        
        Polygon p = geomFac.createPolygon(r, null);
        List<Object> atts = new ArrayList<Object>();
        //atts.add(10);
        atts.add(p);
        atts.add(1);
        return new SimpleFeatureImpl(atts, featureType, new FeatureIdImpl("19"));
    }
    
    private SimpleFeature createPoly2(SimpleFeatureType featureType, GeometryFactory geomFac) {
     // Poly 2
        LinearRing r2 = geomFac.createLinearRing(new Coordinate[] {
                new Coordinate(11, 11),
                new Coordinate(11, 5),
                new Coordinate(7, 10),
                new Coordinate(11, 11)
        });
        
        Polygon p2 = geomFac.createPolygon(r2, null);
        List<Object> atts2 = new ArrayList<Object>();
        //atts2.add(8);
        atts2.add(p2);
        atts2.add(5);
        return new SimpleFeatureImpl(atts2, featureType, new FeatureIdImpl("20"));
    }
    
    private SimpleFeature createPoly3(SimpleFeatureType featureType, GeometryFactory geomFac) {
        // Poly 2
           LinearRing r2 = geomFac.createLinearRing(new Coordinate[] {
                   new Coordinate(11, 0),
                   new Coordinate(11, 4),
                   new Coordinate(7, 4),
                   new Coordinate(11, 0)
           });
           
           Polygon p2 = geomFac.createPolygon(r2, null);
           List<Object> atts2 = new ArrayList<Object>();
           //atts2.add(7);
           atts2.add(p2);
           atts2.add(10);
           return new SimpleFeatureImpl(atts2, featureType, new FeatureIdImpl("21"));
       }
    
    private SimpleFeature createRandomPoly(SimpleFeatureType featureType, GeometryFactory geomFac) {
        int x = r.nextInt(15);
        int y = r.nextInt(15);
        // Poly 2
           LinearRing r2 = geomFac.createLinearRing(new Coordinate[] {
                   new Coordinate(x, y),
                   new Coordinate(x + r.nextInt(10), y),
                   new Coordinate(x + r.nextInt(10), y + r.nextInt(15)),
                   new Coordinate(x, y)
           });
           
           Polygon p2 = geomFac.createPolygon(r2, null);
           List<Object> atts2 = new ArrayList<Object>();
           //atts2.add(7);
           atts2.add(p2);
           atts2.add(r.nextInt(150));
           return new SimpleFeatureImpl(atts2, featureType, new FeatureIdImpl(Integer.toString(r.nextInt(5000))));
       }
    
    @Test
    public void colTest() {
        gen("ff0000");
        gen("00ff00");
    }
    
    private void gen(String code) {
        int i = Integer.decode("0x" + code).intValue();
        System.out.println(i);
        
        Color c = Color.decode("" + i);
        System.out.println(c.getRed() + ", " + c.getGreen() + ", " + c.getBlue() + ", " + c.getAlpha());
    }
    
    
}
