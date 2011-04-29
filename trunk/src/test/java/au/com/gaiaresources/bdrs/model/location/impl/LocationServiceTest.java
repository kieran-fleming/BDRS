package au.com.gaiaresources.bdrs.model.location.impl;

import junit.framework.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import com.vividsolutions.jts.geom.Point;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.location.LocationService;

public class LocationServiceTest extends AbstractControllerTest {
    @Autowired
    LocationService locationService;
    
    @Test
    public void testLatLongTruncation() {
        double latitude = -29.995307513761;
        double longitude = 120.94335937562;
        Point p = locationService.createPoint(latitude, longitude);
        Assert.assertEquals(-29.995307, p.getY(), 0.0000001);
        Assert.assertEquals(120.943359, p.getX(), 0.0000001);
    }
}
