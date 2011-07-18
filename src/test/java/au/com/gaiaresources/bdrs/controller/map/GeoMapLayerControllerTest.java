package au.com.gaiaresources.bdrs.controller.map;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.file.ManagedFile;
import au.com.gaiaresources.bdrs.model.file.ManagedFileDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;
import au.com.gaiaresources.bdrs.spatial.ShapeFileReader;

public class GeoMapLayerControllerTest extends AbstractControllerTest {

    @Autowired
    GeoMapLayerDAO layerDAO;
    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    ManagedFileDAO fileDAO;
    @Autowired
    FileService fileService;
    @Autowired 
    AttributeDAO attrDAO;
    @Autowired
    RecordDAO recDAO;
    @Autowired
    GeoMapFeatureDAO featureDAO;
    
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    GeoMapLayer layer3;
    
    Survey survey1;
    Survey survey2;
    Survey survey3;
    ManagedFile mf;
    
    Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() throws Exception {
        survey1 = new Survey();
        survey2 = new Survey();
        survey3 = new Survey();
        
        surveyDAO.save(survey1);
        surveyDAO.save(survey2);
        surveyDAO.save(survey3);
        
        mf = new ManagedFile();
        mf.setFilename("test_document.pdf");
        mf.setContentType("application/pdf");
        mf.setDescription("This is a test document");
        mf.setCredit("Copyright Someone");
        mf.setLicense("Someone");

        fileDAO.save(mf);
        
        layer1 = new GeoMapLayer();
        layer1.setName("aaaa");
        layer1.setDescription("zzzz");
        layer1.setSurvey(survey1);
        layer1.setLayerSource(GeoMapLayerSource.SURVEY_KML);
        
        layer2 = new GeoMapLayer();
        layer2.setName("bbbb");
        layer2.setDescription("yyyy");
        layer2.setManagedFileUUID("uuidwoooo");
        layer2.setLayerSource(GeoMapLayerSource.KML);
        
        layer3 = new GeoMapLayer();
        layer3.setName("cccc");
        layer3.setDescription("xxxx");
        layer3.setLayerSource(GeoMapLayerSource.SHAPEFILE);
        
        layerDAO.save(layer1);
        layerDAO.save(layer2);
        layerDAO.save(layer3);
        
        login("admin", "password", new String[] { Role.ADMIN });
    }
    
    @Test
    public void testList() throws Exception {
        request.setRequestURI(GeoMapLayerController.LISTING_URL);
        request.setMethod("GET");
        ModelAndView mv = this.handle(request, response);
        Assert.assertEquals("geoMapLayerListing", mv.getViewName());
    }
    
    @Test
    public void testViewNew() throws Exception {
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("GET");
        ModelAndView mv = this.handle(request, response);
        Assert.assertEquals("geoMapLayerEdit", mv.getViewName());
        Assert.assertNull(((GeoMapLayer)mv.getModel().get("geoMapLayer")).getId());
        
        assertSurveyList(mv);
    }
    
    @Test
    public void testViewExisting() throws Exception {
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("GET");
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_VIEW, layer1.getId().toString());
        ModelAndView mv = this.handle(request, response);
        Assert.assertEquals("geoMapLayerEdit", mv.getViewName());
        Assert.assertNotNull(((GeoMapLayer)mv.getModel().get("geoMapLayer")).getId());
        
        Assert.assertEquals(layer1.getId(), ((GeoMapLayer)mv.getModel().get("geoMapLayer")).getId());
        
        assertSurveyList(mv);
    }
    
    
    @Test
    public void testSaveNewAndReturn() throws Exception {
        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.PARAM_NAME, "hello world");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "this is the world");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.SURVEY_MAPSERVER.toString());
        request.setParameter(GeoMapLayerController.PARAM_SURVEY_ID, survey1.getId().toString());
        
        setStyleParams(request);
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "hello world", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        Assert.assertEquals("hello world", gml.getName());
        Assert.assertEquals("this is the world", gml.getDescription());
        Assert.assertEquals(GeoMapLayerSource.SURVEY_MAPSERVER, gml.getLayerSource());
        Assert.assertEquals(false, gml.isPublish());
        Assert.assertEquals(false, gml.isHidePrivateDetails());
        
        assertStyleParams(gml);
    }
    
    @Test
    public void testSaveNewAndReturn_InvalidStyleParams() throws Exception {
        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.PARAM_NAME, "hello world");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "this is the world");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.SURVEY_MAPSERVER.toString());
        request.setParameter(GeoMapLayerController.PARAM_SURVEY_ID, survey1.getId().toString());
        
        request.setParameter(GeoMapLayerController.PARAM_STROKE_COLOR, "#AAAAAA");
        request.setParameter(GeoMapLayerController.PARAM_FILL_COLOR, "#BBBBBB");
        request.setParameter(GeoMapLayerController.PARAM_STROKE_WIDTH, "-1");
        request.setParameter(GeoMapLayerController.PARAM_SYMBOL_SIZE, "-2");
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "hello world", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        Assert.assertEquals("hello world", gml.getName());
        Assert.assertEquals("this is the world", gml.getDescription());
        Assert.assertEquals(GeoMapLayerSource.SURVEY_MAPSERVER, gml.getLayerSource());
        Assert.assertEquals(false, gml.isPublish());
        Assert.assertEquals(false, gml.isHidePrivateDetails());
        
        Assert.assertEquals("#AAAAAA", gml.getStrokeColor());
        Assert.assertEquals("#BBBBBB", gml.getFillColor());
        Assert.assertEquals(0, gml.getStrokeWidth());
        Assert.assertEquals(0, gml.getSymbolSize());
    }
    
    @Test
    public void testSaveNewAndReturn_InvalidColor() throws Exception {
        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.PARAM_NAME, "hello world");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "this is the world");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.SURVEY_MAPSERVER.toString());
        request.setParameter(GeoMapLayerController.PARAM_SURVEY_ID, survey1.getId().toString());
        
        request.setParameter(GeoMapLayerController.PARAM_STROKE_COLOR, "AAAAAA");
        request.setParameter(GeoMapLayerController.PARAM_FILL_COLOR, "jkjhkjh");
        request.setParameter(GeoMapLayerController.PARAM_STROKE_WIDTH, "-1");
        request.setParameter(GeoMapLayerController.PARAM_SYMBOL_SIZE, "-2");
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "hello world", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        Assert.assertEquals("hello world", gml.getName());
        Assert.assertEquals("this is the world", gml.getDescription());
        Assert.assertEquals(GeoMapLayerSource.SURVEY_MAPSERVER, gml.getLayerSource());
        Assert.assertEquals(false, gml.isPublish());
        Assert.assertEquals(false, gml.isHidePrivateDetails());
        
        Assert.assertEquals(GeoMapLayer.DEFAULT_STROKE_COLOR, gml.getStrokeColor());
        Assert.assertEquals(GeoMapLayer.DEFAULT_FILL_COLOR, gml.getFillColor());
        Assert.assertEquals(0, gml.getStrokeWidth());
        Assert.assertEquals(0, gml.getSymbolSize());
    }
    
    @Test
    public void testEditExisting() throws Exception {
        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_SAVE, layer1.getId().toString());
        request.setParameter(GeoMapLayerController.PARAM_NAME, "edited name");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "edited description");
        request.setParameter(GeoMapLayerController.PARAM_HIDE_PRIVATE_DETAILS, "on");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, "amanagedfileuuid");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.KML.toString());
        request.setParameter(GeoMapLayerController.PARAM_PUBLISH, "on");
        
        setStyleParams(request);
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "edited name", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        
        Assert.assertEquals(layer1.getId(), gml.getId());
        Assert.assertEquals("edited name", gml.getName());
        Assert.assertEquals("edited description", gml.getDescription());
        Assert.assertEquals(true, gml.isPublish());
        Assert.assertEquals(true, gml.isHidePrivateDetails());
        Assert.assertEquals(GeoMapLayerSource.KML, gml.getLayerSource());
        Assert.assertNull(gml.getSurvey());
        Assert.assertEquals("amanagedfileuuid", gml.getManagedFileUUID());
        
        assertStyleParams(gml);
    }
    
    @Test
    public void testEditExistingFileUUIDKml() throws Exception {
        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_SAVE, layer1.getId().toString());
        request.setParameter(GeoMapLayerController.PARAM_NAME, "edited name");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "edited description");
        request.setParameter(GeoMapLayerController.PARAM_HIDE_PRIVATE_DETAILS, "on");
        request.setParameter(GeoMapLayerController.PARAM_PUBLISH, "on");
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, "managedfileuuid");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.KML.toString());
        
        setStyleParams(request);
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "edited name", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        
        Assert.assertEquals(layer1.getId(), gml.getId());
        Assert.assertEquals("edited name", gml.getName());
        Assert.assertEquals("edited description", gml.getDescription());
        Assert.assertEquals("managedfileuuid", gml.getManagedFileUUID());
        Assert.assertEquals(GeoMapLayerSource.KML, gml.getLayerSource());
        Assert.assertEquals(true, gml.isPublish());
        Assert.assertEquals(true, gml.isHidePrivateDetails());
        Assert.assertNull(gml.getSurvey());
        
        assertStyleParams(gml);
    }
    
    @Test
    public void testLoadShapefileExisting() throws Exception {
        ManagedFile shapefile = prepShapefileZip(); 
        
        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_SAVE, layer1.getId().toString());
        request.setParameter(GeoMapLayerController.PARAM_NAME, "edited name");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "edited description");
        request.setParameter(GeoMapLayerController.PARAM_HIDE_PRIVATE_DETAILS, "on");
        request.setParameter(GeoMapLayerController.PARAM_PUBLISH, "on");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.SHAPEFILE.toString());
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, shapefile.getUuid());
        request.setParameter(GeoMapLayerController.PARAM_SHAPE_TO_DB, "true");
        
        setStyleParams(request);
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "edited name", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        
        Assert.assertEquals(layer1.getId(), gml.getId());
        Assert.assertEquals("edited name", gml.getName());
        Assert.assertEquals("edited description", gml.getDescription());
        Assert.assertEquals(shapefile.getUuid(), gml.getManagedFileUUID());
        Assert.assertEquals(GeoMapLayerSource.SHAPEFILE, gml.getLayerSource());
        Assert.assertEquals(true, gml.isPublish());
        Assert.assertEquals(true, gml.isHidePrivateDetails());
        Assert.assertNull(gml.getSurvey());
        
        assertStyleParams(gml);
        
        List<Attribute> attributeList = gml.getAttributes();
        
        List<GeoMapFeature> featureList = featureDAO.find(gml.getId());
        
        Assert.assertEquals(3, featureList.size());
        Assert.assertEquals(4, attributeList.size());
        
        {
            Attribute a1 = findAttribute(attributeList, "Id");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.INTEGER, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "String");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.STRING, a1.getType());
        }
        {
            Attribute a1 = findAttribute(attributeList, "DateType");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DATE, a1.getType());
        }
        {
            Attribute a1 = findAttribute(attributeList, "Float");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DECIMAL, a1.getType());
        }
    }
    
    @Test
    public void testOverwriteShapefile() throws Exception {
        GeoMapLayer oldShpLayer = prepapreShapefileGeoMapLayer();
        ManagedFile shapefile = prepShapefileZip(); 

        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_SAVE, oldShpLayer.getId().toString());
        request.setParameter(GeoMapLayerController.PARAM_NAME, "edited name");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "edited description");
        request.setParameter(GeoMapLayerController.PARAM_HIDE_PRIVATE_DETAILS, "on");
        request.setParameter(GeoMapLayerController.PARAM_PUBLISH, "on");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.SHAPEFILE.toString());
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, shapefile.getUuid());
        request.setParameter(GeoMapLayerController.PARAM_SHAPE_TO_DB, "true");
        
        setStyleParams(request);
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "edited name", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        
        Assert.assertEquals(oldShpLayer.getId(), gml.getId());
        Assert.assertEquals("edited name", gml.getName());
        Assert.assertEquals("edited description", gml.getDescription());
        Assert.assertEquals(shapefile.getUuid(), gml.getManagedFileUUID());
        Assert.assertEquals(GeoMapLayerSource.SHAPEFILE, gml.getLayerSource());
        Assert.assertEquals(true, gml.isPublish());
        Assert.assertEquals(true, gml.isHidePrivateDetails());
        Assert.assertNull(gml.getSurvey());
        
        assertStyleParams(gml);
        
        List<Attribute> attributeList = gml.getAttributes();
        
        List<GeoMapFeature> featureList = featureDAO.find(gml.getId());
        
        Assert.assertEquals(3, featureList.size());
        Assert.assertEquals(4, attributeList.size());
        
        {
            Attribute a1 = findAttribute(attributeList, "Id");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.INTEGER, a1.getType());
        }
        
        {
            Attribute a1 = findAttribute(attributeList, "String");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.STRING, a1.getType());
        }
        {
            Attribute a1 = findAttribute(attributeList, "DateType");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DATE, a1.getType());
        }
        {
            Attribute a1 = findAttribute(attributeList, "Float");
            Assert.assertNotNull(a1);
            Assert.assertEquals(AttributeType.DECIMAL, a1.getType());
        }
    }
    
    @Test
    public void testOverwriteShapefile_noshapetodb() throws Exception {
        // we expect no shape file DB overwriting to occur
        GeoMapLayer oldShpLayer = prepapreShapefileGeoMapLayer();
        ManagedFile shapefile = prepShapefileZip(); 

        requestDropDatabase();
        commit();
        
        request.setRequestURI(GeoMapLayerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(GeoMapLayerController.GEO_MAP_LAYER_PK_SAVE, oldShpLayer.getId().toString());
        request.setParameter(GeoMapLayerController.PARAM_NAME, "edited name");
        request.setParameter(GeoMapLayerController.PARAM_DESCRIPTION, "edited description");
        request.setParameter(GeoMapLayerController.PARAM_HIDE_PRIVATE_DETAILS, "on");
        request.setParameter(GeoMapLayerController.PARAM_PUBLISH, "on");
        request.setParameter(GeoMapLayerController.PARAM_MAP_LAYER_SRC, GeoMapLayerSource.SHAPEFILE.toString());
        request.setParameter(GeoMapLayerController.PARAM_MANAGED_FILE_UUID, shapefile.getUuid());
        
        setStyleParams(request);
        
        ModelAndView mv = this.handle(request, response);
        
        // return to listing page
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(GeoMapLayerController.LISTING_URL, redirect.getUrl());
        
        PagedQueryResult<GeoMapLayer> result = layerDAO.search(null, "edited name", null);
        
        Assert.assertEquals(1, result.getCount());
        GeoMapLayer gml = result.getList().get(0);
        
        Assert.assertEquals(oldShpLayer.getId(), gml.getId());
        Assert.assertEquals("edited name", gml.getName());
        Assert.assertEquals("edited description", gml.getDescription());
        Assert.assertEquals(shapefile.getUuid(), gml.getManagedFileUUID());
        Assert.assertEquals(GeoMapLayerSource.SHAPEFILE, gml.getLayerSource());
        Assert.assertEquals(true, gml.isPublish());
        Assert.assertEquals(true, gml.isHidePrivateDetails());
        Assert.assertNull(gml.getSurvey());
        
        assertStyleParams(gml);
        
        List<Attribute> attributeList = gml.getAttributes();
        
        List<GeoMapFeature> featureList = featureDAO.find(gml.getId());
        
        Assert.assertEquals(25, featureList.size());
    }
    
    @Test
    public void testListService() throws Exception {
        request.setMethod("GET");
        request.setRequestURI(GeoMapLayerController.LIST_SERVICE_URL);
       
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "3");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, GeoMapLayerController.FILTER_NAME);
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
        Assert.assertEquals(3, json.getLong("records"));
        Assert.assertEquals("aaaa", ((JSONObject)rowArray.get(0)).getString("name"));      
    }
    
    private void assertSurveyList(ModelAndView mv) {
        List<Survey> surveyList = (List<Survey>)mv.getModel().get("surveyList");
        Assert.assertNotNull(surveyList);
        Assert.assertEquals(3, surveyList.size());
        Assert.assertTrue(surveyList.contains(survey1));
        Assert.assertTrue(surveyList.contains(survey2));
        Assert.assertTrue(surveyList.contains(survey3));
    }
    
    private ManagedFile prepShapefileZip() throws IOException {
        String managedFile_filename = "Small_GDA.zip"; 
        String filename = "Small_GDA.zip";
        File file = new File(getClass().getResource(filename).getFile());

        ManagedFile shapefilemf = new ManagedFile();
        shapefilemf.setFilename(managedFile_filename);
        shapefilemf.setContentType("image/jpeg");
        shapefilemf.setWeight(0);
        shapefilemf.setDescription("This is a test image");
        shapefilemf.setCredit("Creative Commons");
        shapefilemf.setLicense("Nobody");
        shapefilemf.setPortal(RequestContextHolder.getContext().getPortal());
        fileDAO.save(shapefilemf);
        
        fileService.createFile(shapefilemf, file, managedFile_filename);
        return shapefilemf;
    }
    
    private GeoMapLayer prepapreShapefileGeoMapLayer() throws IOException {        
        String filename = getClass().getResource("Simple4.zip").getFile();
        File file = new File(filename);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        List<Attribute> attributeList = reader.readAttributes();
        List<GeoMapFeature> featureList = reader.readAsMapFeatures(attributeList);
        
        GeoMapLayer layer = new GeoMapLayer();
        layer.setName("simple4");
        layer.setDescription("simple4 description");
        layer.setLayerSource(GeoMapLayerSource.SHAPEFILE);
        
        layer.setAttributes(attributeList);
        
        for (Attribute a : layer.getAttributes()) {
            attrDAO.save(a);
        }
        layer = layerDAO.save(layer);
        
        for (GeoMapFeature f : featureList) {
            for (AttributeValue ra : f.getAttributes()) {
                recDAO.saveAttributeValue(ra);
            }
            f.setLayer(layer);
            featureDAO.save(f);
        }
        
        // for fault tolerance, make a AttributeValue which is not attached to
        // a geomapfeature...
        AttributeValue orphanValue = new AttributeValue();
        orphanValue.setAttribute(attributeList.get(0));
        orphanValue.setWeight(0);
        recDAO.saveAttributeValue(orphanValue);
        
        return layer;
    }
    
    private Attribute findAttribute(List<Attribute> attributes, String name) {
        for (Attribute a : attributes) {
            if (name.equals(a.getName())) {
                return a;
            }
        }
        return null;
    }
    
    private void setStyleParams(MockHttpServletRequest request) {
        request.setParameter(GeoMapLayerController.PARAM_STROKE_COLOR, "#AAAAAA");
        request.setParameter(GeoMapLayerController.PARAM_FILL_COLOR, "#BBBBBB");
        request.setParameter(GeoMapLayerController.PARAM_STROKE_WIDTH, "10");
        request.setParameter(GeoMapLayerController.PARAM_SYMBOL_SIZE, "20");
    }
    
    private void assertStyleParams(GeoMapLayer gml) {
        Assert.assertEquals("#AAAAAA", gml.getStrokeColor());
        Assert.assertEquals("#BBBBBB", gml.getFillColor());
        Assert.assertEquals(10, gml.getStrokeWidth());
        Assert.assertEquals(20, gml.getSymbolSize());
    }
}
