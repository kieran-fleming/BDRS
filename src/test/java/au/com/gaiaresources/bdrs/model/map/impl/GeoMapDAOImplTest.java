package au.com.gaiaresources.bdrs.model.map.impl;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.map.GeoMap;
import au.com.gaiaresources.bdrs.model.map.GeoMapDAO;

public class GeoMapDAOImplTest extends AbstractControllerTest {

    @Autowired
    GeoMapDAO geoMapDAO;
    
    GeoMap map1;
    GeoMap map2;
    GeoMap map3;
    
    @Before
    public void setup() {
        map1 = new GeoMap();
        map1.setAnonymousAccess(true);
        map1.setPublish(false);
        
        map2 = new GeoMap();
        map2.setAnonymousAccess(true);
        map2.setPublish(true);
        
        map3 = new GeoMap();
        map3.setAnonymousAccess(false);
        map3.setPublish(true);
        
        geoMapDAO.save(map1);
        geoMapDAO.save(map2);
        geoMapDAO.save(map3);
    }
    
    @Test
    public void testAutowire() {
        Assert.assertNotNull(geoMapDAO);
    }
    
    @Test
    public void testAnonPublish() {
        PagedQueryResult<GeoMap> result = geoMapDAO.search(null, null, null, null, true, true);
        Assert.assertEquals(1, result.getCount());
        Assert.assertEquals(map2, result.getList().get(0));
    }
}
