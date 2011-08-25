package au.com.gaiaresources.bdrs.controller.map;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.map.AssignedGeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMap;
import au.com.gaiaresources.bdrs.model.map.GeoMapDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.security.Role;

public class GeoMapLayerController_Delete_Test extends AbstractControllerTest {

    @Autowired
    GeoMapLayerDAO layerDAO;
    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    GeoMapDAO mapDAO;
    @Autowired
    GeoMapFeatureDAO featureDAO;
    
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    
    GeoMapFeature feature1;
    
    Survey survey1;

    GeoMap map;
    
    GeometryBuilder gb = new GeometryBuilder();
    
    Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() throws Exception {
        survey1 = new Survey(); 
        
        surveyDAO.save(survey1);
        
        layer1 = new GeoMapLayer();
        layer1.setName("aaaa");
        layer1.setDescription("zzzz");
        layer1.setSurvey(survey1);
        layer1.setLayerSource(GeoMapLayerSource.SURVEY_KML);
        
        layer2 = new GeoMapLayer();
        layer2.setName("bbbb");
        layer2.setDescription("yyyy");
        layer2.setSurvey(survey1);
        layer2.setLayerSource(GeoMapLayerSource.SURVEY_KML);
        
        layerDAO.save(layer1);
        layerDAO.save(layer2);
        
        map = new GeoMap();
        map.setName("map name");
        map.setDescription("map desc");
       
        mapDAO.save(map);
        
        List<AssignedGeoMapLayer> assignedList = new LinkedList<AssignedGeoMapLayer>();
        assignedList.add(createTestAssignedLayer(map, layer1));
        assignedList.add(createTestAssignedLayer(map, layer1));
        assignedList.add(createTestAssignedLayer(map, layer1));
        assignedList.add(createTestAssignedLayer(map, layer2));
        assignedList.add(createTestAssignedLayer(map, layer2));
        assignedList.add(createTestAssignedLayer(map, layer2));
        
        layerDAO.save(assignedList);
        
        feature1 = new GeoMapFeature();
        feature1.setGeometry(gb.createPoint(1, 1));
        feature1.setLayer(layer1);
        featureDAO.save(feature1);
        
        login("admin", "password", new String[] { Role.ADMIN });
    }
    
    @Test
    public void testGeoMapLayerDelete() throws Exception {
        request.setRequestURI(GeoMapLayerController.DELETE_LAYER_URL);
        request.setMethod("POST");
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_SAVE, layer1.getId().toString());
        
        int deletedLayerId = layer1.getId().intValue();
        // make sure we have all 6 items before hand...
        Assert.assertEquals(6, layerDAO.getForMap(map.getId()).size());
        
        ModelAndView mv = handle(request, response);
        View view = mv.getView();
        Assert.assertTrue(view instanceof RedirectView);
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, ((RedirectView)view).getUrl());
        
        // all of the assigned layers associated with layer 1 will be deleted
        Assert.assertEquals(3, layerDAO.getForMap(map.getId()).size());
        Assert.assertNull(layerDAO.get(deletedLayerId));
    }
    
    private AssignedGeoMapLayer createTestAssignedLayer(GeoMap map, GeoMapLayer layer) {
        AssignedGeoMapLayer asLayer = new AssignedGeoMapLayer();
        asLayer.setLayer(layer);
        asLayer.setMap(map);
        return asLayer;
    }
}
