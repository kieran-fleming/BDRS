package au.com.gaiaresources.bdrs.model.map.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

public class GeoMapFeatureDAOImplTest extends AbstractTransactionalTest {

    private GeometryBuilder geometryBuilder = new GeometryBuilder();
    @Autowired
    GeoMapFeatureDAO featureDAO;
    @Autowired
    GeoMapLayerDAO layerDAO;
    
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    
    GeoMapFeature f1;
    GeoMapFeature f2;
    GeoMapFeature f3;
    GeoMapFeature f4;
    
    @Before
    public void setup() {
        layer1 = createTestLayer("layer 1");
        layer2 = createTestLayer("layer 2");
        
        f1 = createTestFeature(layer1);
        f2 = createTestFeature(layer1);
        f3 = createTestFeature(layer2);
        f4 = createTestFeature(layer2);
    }
    
    private GeoMapLayer createTestLayer(String name) {
        GeoMapLayer l = new GeoMapLayer();
        l.setName(name);
        l.setDescription(name + " description");
        l.setLayerSource(GeoMapLayerSource.SHAPEFILE);
        return layerDAO.save(l);
    }
    
    private GeoMapFeature createTestFeature(GeoMapLayer layer) {
        GeoMapFeature f = new GeoMapFeature();
        f.setGeometry(geometryBuilder.createSquare(10, 10, 10));
        f.setLayer(layer);
        return featureDAO.save(f);
    }
    
    @Test
    public void testFindByMaplayerId() {
        // make sure our test entries are in there...
        Assert.assertEquals(4, featureDAO.count());       
        {
            List<GeoMapFeature> result = featureDAO.find(layer1.getId());
            Assert.assertEquals(2, result.size());
            Assert.assertTrue(result.contains(f1));
            Assert.assertTrue(result.contains(f2));
        }
        {
            List<GeoMapFeature> result = featureDAO.find(new Integer[] { layer1.getId(), layer2.getId() }, null);
            Assert.assertEquals(4, result.size());
            Assert.assertTrue(result.contains(f1));
            Assert.assertTrue(result.contains(f2));
            Assert.assertTrue(result.contains(f3));
            Assert.assertTrue(result.contains(f4));
        }
    }
}
