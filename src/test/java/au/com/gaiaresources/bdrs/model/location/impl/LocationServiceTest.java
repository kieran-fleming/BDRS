package au.com.gaiaresources.bdrs.model.location.impl;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.test.AbstractSpringContextTest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class LocationServiceTest extends AbstractSpringContextTest {
    @Autowired
    LocationService locationService;
    
    private Logger log = Logger.getLogger(getClass());
    
    @Test
    public void testLatLongTruncation() {
        double latitude = -29.995307513761;
        double longitude = 120.94335937562;
        Point p = locationService.createPoint(latitude, longitude);
        Assert.assertEquals(-29.995307, p.getY(), 0.0000001);
        Assert.assertEquals(120.943359, p.getX(), 0.0000001);
    }
    
    // just making sure a self intersecting polygon wkt becomes an invalid geometry
    @Test
    public void testFromWktSelfIntersectingPolygon() {
        String wktString = "POLYGON((135.44531250005 -35.427036840141,124.19531250049 -27.957942082073,113.12109375093 -41.477654408167,134.74218750007 -26.55160357215,135.44531250005 -35.427036840141))";
        Geometry geom = locationService.createGeometryFromWKT(wktString);
        Assert.assertFalse("geom should be invalid", geom.isValid());
    }
    
    @Test
    public void testFromWktValidPolygon() {
        String wktString = "POLYGON((106.96875000118 -26.236689530083,110.66015625103 -30.564552846079,128.23828125033 -25.445678126428,126.4804687504 -20.59414374007,119.09765625069 -18.106616919754,112.06640625097 -18.93998203528,104.68359375127 -21.086983557827,98.355468751521 -26.865655064182,100.46484375144 -30.715792305308,103.27734375132 -34.273035505668,106.26562500121 -34.418169134701,106.96875000118 -26.236689530083))";
        Geometry geom = locationService.createGeometryFromWKT(wktString);
        Assert.assertTrue("geom should be valid", geom.isValid());
    }
}
