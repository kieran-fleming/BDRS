package au.com.gaiaresources.bdrs.controller.map;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.map.AssignedGeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMap;
import au.com.gaiaresources.bdrs.model.map.GeoMapDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.security.Role;

public class GeoMapControllerTest extends AbstractControllerTest {

    @Autowired
    GeoMapDAO geoMapDAO;
    @Autowired
    GeoMapLayerDAO layerDAO;
    
    GeoMap map1;
    GeoMap map2;
    GeoMap map3;
    
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    
    @Before
    public void setup() throws Exception {
        map1 = new GeoMap();
        map1.setName("aaaa");
        map1.setAnonymousAccess(true);
        map1.setPublish(false);
        map1.setWeight(100);
        
        map2 = new GeoMap();
        map2.setName("bbbb");
        map2.setAnonymousAccess(true);
        map2.setPublish(true);
        
        map3 = new GeoMap();
        map3.setName("cccc");
        map3.setAnonymousAccess(false);
        map3.setPublish(true);
        
        geoMapDAO.save(map1);
        geoMapDAO.save(map2);
        geoMapDAO.save(map3);
        
        layer1 = new GeoMapLayer();
        layer2 = new GeoMapLayer();
        
        layer1.setLayerSource(GeoMapLayerSource.KML);
        layer2.setLayerSource(GeoMapLayerSource.KML);
        
        layerDAO.save(layer1);
        layerDAO.save(layer2);
    }
    
    @Test
    public void testListing() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.LISTING_URL);
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "geoMapListing");
    }
    
    @Test
    public void testViewExisting() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.EDIT_URL);
        request.setParameter(GeoMapController.GEO_MAP_PK_VIEW, map1.getId().toString());
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "geoMapEdit");
        
        GeoMap gm = (GeoMap)mv.getModel().get("geoMap");
        Assert.assertEquals(map1.getId(), gm.getId());
    }
    
    
    
    @Test
    public void testViewExistingWithAssignedLayers() throws Exception {
        GeoMap map4 = new GeoMap();
        map4.setName("aaaa");
        map4.setAnonymousAccess(true);
        map4.setPublish(false);
        map4.setWeight(100);
        geoMapDAO.save(map4);
        
        List<AssignedGeoMapLayer> layerList = new ArrayList<AssignedGeoMapLayer>();
        layerList.add(createTestAssignedLayer(map4, layer2));
        layerList.add(createTestAssignedLayer(map4, layer1));
        layerDAO.save(layerList);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.EDIT_URL);
        request.setParameter(GeoMapController.GEO_MAP_PK_VIEW, map4.getId().toString());
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "geoMapEdit");
        
        GeoMap gm = (GeoMap)mv.getModel().get(GeoMapController.MAV_GEO_MAP);
        Assert.assertEquals(map4.getId(), gm.getId());
        
        List<AssignedGeoMapLayer> returnedList = (List<AssignedGeoMapLayer>)mv.getModel().get(GeoMapController.MAV_ASSIGNED_LAYERS);
        Assert.assertNotNull(returnedList);
        Assert.assertEquals(2, returnedList.size());
        Assert.assertEquals(layer2, returnedList.get(0).getLayer());
        Assert.assertEquals(layer1, returnedList.get(1).getLayer());
    }
    
    @Test
    public void testViewNew() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.EDIT_URL);
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "geoMapEdit");
        
        GeoMap gm = (GeoMap)mv.getModel().get("geoMap");
        Assert.assertEquals(null, gm.getId());
    }
    
    @Test
    public void testSaveNew() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI(GeoMapController.EDIT_URL);
        
        request.setParameter("name", "new name");
        request.setParameter("description", "new description");
        request.setParameter("anonymousAccess", "on");
        request.setParameter("hidePrivateDetails", "on");
        request.setParameter("publish", "on");
        request.setParameter("weight", "12");
        
        request.addParameter("mapLayerPk", layer1.getId().toString());
        request.addParameter("mapLayerPk", layer2.getId().toString());
        
        request.addParameter(GeoMapController.PARAM_MAP_LAYER_VISIBLE, "false");
        request.addParameter(GeoMapController.PARAM_MAP_LAYER_VISIBLE, "true");
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMap> pagedResult = geoMapDAO.search(null, "new name", null, null, null, null);
        
        Assert.assertEquals(1, pagedResult.getCount());
        
        GeoMap gm = pagedResult.getList().get(0);
        
        Assert.assertEquals("new name", gm.getName());
        Assert.assertEquals("new description", gm.getDescription());
        Assert.assertEquals(true, gm.isAnonymousAccess());
        Assert.assertEquals(true, gm.isHidePrivateDetails());
        Assert.assertEquals(true, gm.isPublish());
        Assert.assertEquals(12, gm.getWeight());
        
        List<AssignedGeoMapLayer> layerList = layerDAO.getForMap(gm.getId());
        Assert.assertEquals(2, layerList.size());
        // we now need to test for order...
        Assert.assertEquals(layerList.get(0).getLayer(), layer1);
        Assert.assertEquals(layerList.get(1).getLayer(), layer2);
        
        Assert.assertEquals(layerList.get(0).isVisible(), false);
        Assert.assertEquals(layerList.get(1).isVisible(), true);
    }
    
    @Test
    public void testEditExisting() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI(GeoMapController.EDIT_URL);
        
        request.setParameter(GeoMapController.GEO_MAP_PK_SAVE, map1.getId().toString());
        request.setParameter("name", "edited name");
        request.setParameter("description", "edited description");
        request.setParameter("anonymousAccess", "on");
        request.setParameter("hidePrivateDetails", "on");
        request.setParameter("publish", "on");
        request.setParameter("weight", "10");
        
        request.addParameter("mapLayerPk", layer1.getId().toString());
        request.addParameter("mapLayerPk", layer2.getId().toString());
        
        request.addParameter(GeoMapController.PARAM_MAP_LAYER_VISIBLE, "true");
        request.addParameter(GeoMapController.PARAM_MAP_LAYER_VISIBLE, "false");
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMap> pagedResult = geoMapDAO.search(null, null, null, map1.getId(), null, null);
        
        Assert.assertEquals(1, pagedResult.getCount());
        
        GeoMap gm = pagedResult.getList().get(0);
        
        Assert.assertEquals("edited name", gm.getName());
        Assert.assertEquals("edited description", gm.getDescription());
        Assert.assertEquals(true, gm.isAnonymousAccess());
        Assert.assertEquals(true, gm.isHidePrivateDetails());
        Assert.assertEquals(true, gm.isPublish());
        Assert.assertEquals(10, gm.getWeight());
        
        List<AssignedGeoMapLayer> layerList = layerDAO.getForMap(gm.getId());
        Assert.assertEquals(2, layerList.size());
        // we now need to test for order...
        Assert.assertEquals(layerList.get(0).getLayer(), layer1);
        Assert.assertEquals(layerList.get(1).getLayer(), layer2);
        
        Assert.assertEquals(layerList.get(0).isVisible(), true);
        Assert.assertEquals(layerList.get(1).isVisible(), false);
    }
    
    @Test
    public void testListingService() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.LIST_SERVICE_URL);
       
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "3");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, GeoMapController.FILTER_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);

        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
        Assert.assertEquals(3, json.getLong("records"));
        Assert.assertEquals("aaaa", ((JSONObject)rowArray.get(0)).getString("name"));      
        Assert.assertEquals("", ((JSONObject)rowArray.get(0)).getString("description"));
        Assert.assertEquals("Yes", ((JSONObject)rowArray.get(0)).getString("anonymousAccess"));
        Assert.assertEquals("No", ((JSONObject)rowArray.get(0)).getString("publish"));
    }
    
    @Test
    public void testAvailableMapServiceAsAnon() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.GET_AVAILABLE_MAP_SERVICE_URL);
       
        this.handle(request,response);
        
        JSONArray array = (JSONArray)JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertEquals(1, array.size());
        JSONObject geoMapJsonObj = (JSONObject)array.get(0);
        Assert.assertEquals("bbbb", geoMapJsonObj.get("name"));
    }
    
    @Test
    public void testAvailableMapServiceAsUser() throws Exception {
        login("admin", "password", new String[] { Role.USER });
        
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.GET_AVAILABLE_MAP_SERVICE_URL);
       
        this.handle(request,response);
        
        JSONArray array = (JSONArray)JSONSerializer.toJSON(response.getContentAsString());
        Assert.assertEquals(2, array.size());
    }
    
    @Test
    public void testDelete() throws Exception {
        List<AssignedGeoMapLayer> layerList = new ArrayList<AssignedGeoMapLayer>();
        layerList.add(createTestAssignedLayer(map1, layer1));
        layerList.add(createTestAssignedLayer(map1, layer2));
        layerDAO.save(layerList);

        geoMapDAO.update(map1);
        
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI(GeoMapController.DELETE_URL);
        request.setParameter(GeoMapController.GEO_MAP_PK_SAVE, map1.getId().toString());
       
        this.handle(request,response);
        
        GeoMap map = geoMapDAO.get(map1.getId());
        Assert.assertNull(map);
    }
    
    @Test
    public void testMapViewing() throws Exception {
        GeoMap map4 = new GeoMap();
        map4.setName("my map for viewing");
        map4.setAnonymousAccess(true);
        map4.setPublish(false);
        map4.setWeight(100);
        geoMapDAO.save(map4);
        
        List<AssignedGeoMapLayer> layerList = new ArrayList<AssignedGeoMapLayer>();
        layerList.add(createTestAssignedLayer(map4, layer2));
        layerList.add(createTestAssignedLayer(map4, layer1));
        layerDAO.save(layerList);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.VIEW_MAP_URL);
        request.setParameter(GeoMapController.GEO_MAP_PK_VIEW, map4.getId().toString());
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "viewMap");
        
        GeoMap gm = (GeoMap)mv.getModel().get(GeoMapController.MAV_GEO_MAP);
        Assert.assertEquals(map4.getId(), gm.getId());
        
        List<AssignedGeoMapLayer> returnedList = (List<AssignedGeoMapLayer>)mv.getModel().get(GeoMapController.MAV_ASSIGNED_LAYERS);
        Assert.assertNotNull(returnedList);
        Assert.assertEquals(2, returnedList.size());
        Assert.assertEquals(layer2, returnedList.get(0).getLayer());
        Assert.assertEquals(layer1, returnedList.get(1).getLayer());
    }
    
    @Test
    public void testMapViewingAccessFail() throws Exception {
        GeoMap map4 = new GeoMap();
        map4.setName("my map for viewing");
        
        // note no anon access
        map4.setAnonymousAccess(false);
        map4.setPublish(false);
        map4.setWeight(100);
        geoMapDAO.save(map4);
        
        List<AssignedGeoMapLayer> layerList = new ArrayList<AssignedGeoMapLayer>();
        layerList.add(createTestAssignedLayer(map4, layer2));
        layerList.add(createTestAssignedLayer(map4, layer1));
        layerDAO.save(layerList);
        
        // no log in
        //login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("GET");
        request.setRequestURI(GeoMapController.VIEW_MAP_URL);
        request.setParameter(GeoMapController.GEO_MAP_PK_VIEW, map4.getId().toString());
        ModelAndView mv = handle(request, response);
        
        Assert.assertEquals(401, response.getStatus());
    }
    
    private AssignedGeoMapLayer createTestAssignedLayer(GeoMap map, GeoMapLayer layer) {
        AssignedGeoMapLayer asLayer = new AssignedGeoMapLayer();
        asLayer.setMap(map);
        asLayer.setLayer(layer);
        return asLayer;
    }
}
