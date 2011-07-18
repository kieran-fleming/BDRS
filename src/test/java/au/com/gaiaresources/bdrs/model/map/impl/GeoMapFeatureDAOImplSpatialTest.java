package au.com.gaiaresources.bdrs.model.map.impl;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

public class GeoMapFeatureDAOImplSpatialTest extends AbstractTransactionalTest {
    private GeometryBuilder geometryBuilder = new GeometryBuilder();

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private GeoMapLayerDAO layerDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private GeoMapFeatureDAO featureDAO;
    
    User admin;
    Survey survey1;
    Survey survey2;
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    
    GeoMapFeature f1;
    GeoMapFeature f2;
    GeoMapFeature f3;
    GeoMapFeature f4;
    
    Geometry g1;
    Geometry g2;
    Geometry g3;
    Geometry g4;
    
    @Before
    public void setup() {
        admin = userDAO.getUser("admin");
        
        survey1 = surveyDAO.createSurvey("my survey");
        survey2 = surveyDAO.createSurvey("second survey");
        
        g1 = geometryBuilder.createSquare(0, 0, 10);
        g2 = geometryBuilder.createSquare(5, 0, 10);
        g3 = geometryBuilder.createSquare(0, 5, 10);
        g4 = geometryBuilder.createSquare(5, 5, 10);
                      
        layer1 = new GeoMapLayer();
        layer1.setName("aaaa");
        layer1.setDescription("zzzz");
        layer1.setSurvey(survey1);
        layer1.setLayerSource(GeoMapLayerSource.SURVEY_KML);

        layer2 = new GeoMapLayer();
        layer2.setName("cccc");
        layer2.setDescription("xxxx");
        layer2.setLayerSource(GeoMapLayerSource.SHAPEFILE);
        layer2.setSurvey(survey2);
                
        layerDAO.save(layer1);
        layerDAO.save(layer2);
        
        f1 = createTestFeature(g1, layer1);
        f2 = createTestFeature(g2, layer1);
        f3 = createTestFeature(g3, layer2);
        f4 = createTestFeature(g4, layer2);
        
        surveyDAO.updateSurvey(survey1);
    }
    
    private GeoMapFeature createTestFeature(Geometry geom, GeoMapLayer layer) {
        GeoMapFeature feature = new GeoMapFeature();
        feature.setLayer(layer);
        feature.setGeometry(geom);
        return featureDAO.save(feature);
    }
    
    @Test
    public void testPointIntersect() {
        Integer[] ids = new Integer[] { layer1.getId(), layer2.getId() };
        {
            Point point = geometryBuilder.createPoint(2.5, 2.5);
            List<GeoMapFeature> features = featureDAO.find(ids, point);
            Assert.assertEquals(1, features.size());
        }
        
        {
            Point point = geometryBuilder.createPoint(2.5, 7.5);
            List<GeoMapFeature> features = featureDAO.find(ids, point);
            Assert.assertEquals(2, features.size());
        }
        
        {
            Point point = geometryBuilder.createPoint(7.5, 7.5);
            List<GeoMapFeature> features = featureDAO.find(ids, point);
            Assert.assertEquals(4, features.size());
        }
    }
    
    @Test
    public void testGeomIntersect() {
        Integer[] ids = new Integer[] { layer1.getId(), layer2.getId() };
        {
            Point point = geometryBuilder.createPoint(2.5, 2.5);
            Geometry geom = geometryBuilder.bufferInM(point, 1d);
            List<GeoMapFeature> features = featureDAO.find(ids, geom);
            Assert.assertEquals(1, features.size());
        }
        
        {
            Point point = geometryBuilder.createPoint(2.5, 7.5);
            Geometry geom = geometryBuilder.bufferInM(point, 1d);
            List<GeoMapFeature> features = featureDAO.find(ids, geom);
            Assert.assertEquals(2, features.size());
        }
        
        {
            Point point = geometryBuilder.createPoint(7.5, 7.5);
            Geometry geom = geometryBuilder.bufferInM(point, 1d);
            List<GeoMapFeature> features = featureDAO.find(ids, geom);
            Assert.assertEquals(4, features.size());
        }
    }
}
