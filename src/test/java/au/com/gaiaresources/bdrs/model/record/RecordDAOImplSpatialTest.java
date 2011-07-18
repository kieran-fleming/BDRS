package au.com.gaiaresources.bdrs.model.record;

import java.util.Calendar;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

import com.vividsolutions.jts.geom.Geometry;

public class RecordDAOImplSpatialTest extends AbstractControllerTest {
    
    private GeometryBuilder geometryBuilder = new GeometryBuilder();
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private GeoMapLayerDAO layerDAO;
    @Autowired
    private UserDAO userDAO;
    
    User admin;
    Survey survey1;
    Survey survey2;
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    
    Record r1;
    Record r2;
    Record r3;
    Record r4;
    
    Geometry g1;
    Geometry g2;
    Geometry g3;
    Geometry g4;
        
    @Before
    public void setup() {
        Calendar cal = Calendar.getInstance();
        cal.set(2000, 10, 10);
        Date now = cal.getTime();
        
        admin = userDAO.getUser("admin");
        
        survey1 = surveyDAO.createSurvey("my survey");
        survey2 = surveyDAO.createSurvey("second survey");
        
        g1 = geometryBuilder.createSquare(0, 0, 10);
        g2 = geometryBuilder.createSquare(5, 0, 10);
        g3 = geometryBuilder.createSquare(0, 5, 10);
        g4 = geometryBuilder.createSquare(5, 5, 10);
        
        r1 = createTestRecord(now, g1, survey1);
        r2 = createTestRecord(now, g2, survey1);
        r3 = createTestRecord(now, g3, survey2);
        r4 = createTestRecord(now, g4, survey2);
                      
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
        
        recordDAO.saveRecord(r1);
        recordDAO.saveRecord(r2);
        recordDAO.saveRecord(r3);
        recordDAO.saveRecord(r4);
        
        surveyDAO.updateSurvey(survey1);
    }
    
    @Test
    public void testPointIntersect() {
        Integer[] layerIds = new Integer[] { layer1.getId(), layer2.getId() };
        
        {
            Geometry point = geometryBuilder.createPoint(2.5, 2.5);
            List<Record> result = recordDAO.find(layerIds, point);
            Assert.assertEquals(1, result.size());
        }
        
        {
            Geometry point = geometryBuilder.createPoint(2.5, 7.5);
            List<Record> result = recordDAO.find(layerIds, point);
            Assert.assertEquals(2, result.size());
        }
        
        {
            Geometry point = geometryBuilder.createPoint(7.5, 7.5);
            List<Record> result = recordDAO.find(layerIds, point);
            Assert.assertEquals(4, result.size());
        }
    }
    
    private Record createTestRecord(Date now, Geometry geom, Survey survey) {
        Record rec = new Record();
        rec.setUser(admin);
        rec.setCensusMethod(null);
        rec.setSurvey(survey);
        rec.setSpecies(null);
        rec.setWhen(now);
        rec.setLastDate(now);
        rec.setGeometry(geom);        
        return rec;
    }
}
