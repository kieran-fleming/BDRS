package au.com.gaiaresources.bdrs.controller.map;

import java.math.BigDecimal;
import java.util.Calendar;

import junit.framework.Assert;
import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.map.GeoMapDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayer;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerSource;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.JsonService;

public class GeoMapLayerController_GetFeatureInfo_Test extends
        AbstractControllerTest {

    GeometryBuilder geomBuilder = new GeometryBuilder();
    

    @Autowired
    GeoMapLayerDAO layerDAO;
    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    RecordDAO recDAO;
    @Autowired
    AttributeDAO attrDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    GeoMapFeatureDAO gmfDAO;

    
    Attribute recAttr1;
    Attribute recAttr2;
    Attribute recAttr3;
    
    Attribute layerAttr1;
    Attribute layerAttr2;
    Attribute layerAttr3;
    
    Survey survey;
    GeoMapLayer layer1;
    GeoMapLayer layer2;
    
    GeoMapFeature gmf;
    
    User admin;
    User owner;
    User nonOwner;
    
    Record recordOwnerOnly;
    Record recordPublic;
    Record recordControlled;
    
    Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() throws Exception {
        
        owner = userDAO.createUser("owner", "first", "last", "user@user.com", "password", "regkey", Role.USER);
        nonOwner = userDAO.createUser("nonowner", "nonowner", "nonowner", "nonowner@user.com", "password", "regkey", Role.USER);
        
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 1, 1);
        
        admin = userDAO.getUser("admin");
        
        recAttr1 = createTestAttribute("recattr1", AttributeType.INTEGER.getCode());
        recAttr2 = createTestAttribute("recattr2", AttributeType.STRING.getCode());
        recAttr3 = createTestAttribute("recattr3", AttributeType.STRING.getCode());
        
        layerAttr1 = createTestAttribute("layerattr1", AttributeType.INTEGER.getCode());
        layerAttr2 = createTestAttribute("layerattr2", AttributeType.STRING.getCode());
        layerAttr3 = createTestAttribute("layerattr3", AttributeType.STRING.getCode());
        
        survey = new Survey();
        survey.setName("my survey");
        survey.getAttributes().add(recAttr1);
        survey.getAttributes().add(recAttr2);
        survey.getAttributes().add(recAttr3);
        surveyDAO.save(survey);

        recordOwnerOnly = new Record();
        recordOwnerOnly.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        recordOwnerOnly.setUser(owner);
        recordOwnerOnly.setWhen(cal.getTime());
        recordOwnerOnly.setLastDate(cal.getTime());
        recordOwnerOnly.setGeometry(geomBuilder.createSquare(-10, -10, 10));
        recordOwnerOnly.setSurvey(survey);
        recordOwnerOnly.getAttributes().add(createTestAttrValue(recAttr1, "1"));
        recordOwnerOnly.getAttributes().add(createTestAttrValue(recAttr2, "two"));
        recordOwnerOnly.getAttributes().add(createTestAttrValue(recAttr3, "three"));
        recDAO.saveRecord(recordOwnerOnly);
        
        recordPublic = new Record();
        recordPublic.setRecordVisibility(RecordVisibility.PUBLIC);
        recordPublic.setUser(owner);
        recordPublic.setWhen(cal.getTime());
        recordPublic.setLastDate(cal.getTime());
        recordPublic.setGeometry(geomBuilder.createSquare(-10, -10, 10));
        recordPublic.setSurvey(survey);
        recordPublic.getAttributes().add(createTestAttrValue(recAttr1, "7"));
        recordPublic.getAttributes().add(createTestAttrValue(recAttr2, "eight"));
        recordPublic.getAttributes().add(createTestAttrValue(recAttr3, "nine"));
        recDAO.saveRecord(recordPublic);
        
        recordControlled = new Record();
        recordControlled.setRecordVisibility(RecordVisibility.CONTROLLED);
        recordControlled.setUser(owner);
        recordControlled.setWhen(cal.getTime());
        recordControlled.setLastDate(cal.getTime());
        recordControlled.setGeometry(geomBuilder.createSquare(-10, -10, 10));
        recordControlled.setSurvey(survey);
        recordControlled.getAttributes().add(createTestAttrValue(recAttr1, "10"));
        recordControlled.getAttributes().add(createTestAttrValue(recAttr2, "eleven"));
        recordControlled.getAttributes().add(createTestAttrValue(recAttr3, "twelve"));
        recDAO.saveRecord(recordControlled);
        
        layer1 = new GeoMapLayer();
        layer1.setName("first");
        layer1.setSurvey(survey);
        layer1.setLayerSource(GeoMapLayerSource.SURVEY_MAPSERVER);
        layerDAO.save(layer1);
        
        layer2 = new GeoMapLayer();
        layer2.setName("second");
        layer2.setLayerSource(GeoMapLayerSource.SHAPEFILE);
        layer2.getAttributes().add(layerAttr1);
        layer2.getAttributes().add(layerAttr2);
        layer2.getAttributes().add(layerAttr3);
        layerDAO.save(layer2);
        
        gmf = new GeoMapFeature();
        gmf.setGeometry(geomBuilder.createSquare(-5, -5, 10));
        gmf.setLayer(layer2);
        gmf.getAttributes().add(createTestAttrValue(layerAttr1, "4"));
        gmf.getAttributes().add(createTestAttrValue(layerAttr2, "five"));
        gmf.getAttributes().add(createTestAttrValue(layerAttr3, "six"));
        gmfDAO.save(gmf);
    }
    
    private Attribute createTestAttribute(String name, String typecode) {
        Attribute a = new Attribute();
        a.setName(name);
        a.setDescription(name + " desc");
        a.setTag(false);
        a.setTypeCode(typecode);
        return attrDAO.save(a);
    }
    
    private AttributeValue createTestAttrValue(Attribute attr, String value) {
        AttributeValue av = new AttributeValue();
        av.setAttribute(attr);
        switch (attr.getType()) {
        case INTEGER:
            av.setNumericValue(new BigDecimal(Integer.parseInt(value)));
            break;
        case STRING:
            av.setStringValue(value);
            break;
        default:
            Assert.assertTrue("Tried to create an attribute value of an unsupported type", false);    
        }
        return attrDAO.save(av);
    }
    
    /**
     * Admin expects everything to be returned
     * @throws Exception
     */
    @Test
    public void testWebservice_asAdmin() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        testExpectAllRecordsWithFullDetailReturned();
    }
    
    /**
     * Owner expects everythign to be returned
     * @throws Exception
     */
    @Test
    public void testWebService_asOwner() throws Exception {
        
        login("owner", "password", new String[] { Role.USER });
        testExpectAllRecordsWithFullDetailReturned();
    }
    
    private void testExpectAllRecordsWithFullDetailReturned() throws Exception {
        request.setRequestURI(GeoMapLayerController.GET_FEATURE_SERVICE_URL);
        request.setMethod("GET");
        
        request.setParameter(GeoMapLayerController.PARAM_BUFFER_KM, "0");
        request.setParameter(GeoMapLayerController.PARAM_LATITUDE_Y, "-2.5");
        request.setParameter(GeoMapLayerController.PARAM_LONGITUDE_X, "-2.5");
        request.addParameter(GeoMapLayerController.PARAM_MAP_LAYER_ID, layer1.getId().toString());
        request.addParameter(GeoMapLayerController.PARAM_MAP_LAYER_ID, layer2.getId().toString());
        
        this.handle(request, response);

        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray items = (JSONArray)json.get(GeoMapLayerController.JSON_KEY_ITEMS);
        Assert.assertNotNull(items);
        Assert.assertEquals(4, items.size());
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordOwnerOnly.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals(1, getAttribute(attributes, recAttr1.getDescription()).getLong(recAttr1.getDescription()));
            Assert.assertEquals("two", getAttribute(attributes, recAttr2.getDescription()).getString(recAttr2.getDescription()));
            Assert.assertEquals("three", getAttribute(attributes, recAttr3.getDescription()).getString(recAttr3.getDescription()));
            
            Assert.assertEquals("Standard Taxonomic", obj.getString(JsonService.RECORD_KEY_CENSUS_METHOD));
        }
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordPublic.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals(7, getAttribute(attributes, recAttr1.getDescription()).getLong(recAttr1.getDescription()));
            Assert.assertEquals("eight", getAttribute(attributes, recAttr2.getDescription()).getString(recAttr2.getDescription()));
            Assert.assertEquals("nine", getAttribute(attributes, recAttr3.getDescription()).getString(recAttr3.getDescription()));
            
            Assert.assertEquals("Standard Taxonomic", obj.getString(JsonService.RECORD_KEY_CENSUS_METHOD));
        }
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordControlled.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals(10, getAttribute(attributes, recAttr1.getDescription()).getLong(recAttr1.getDescription()));
            Assert.assertEquals("eleven", getAttribute(attributes, recAttr2.getDescription()).getString(recAttr2.getDescription()));
            Assert.assertEquals("twelve", getAttribute(attributes, recAttr3.getDescription()).getString(recAttr3.getDescription()));
            
            Assert.assertEquals("Standard Taxonomic", obj.getString(JsonService.RECORD_KEY_CENSUS_METHOD));
        }
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_MAP_FEATURE, gmf.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES);
            Assert.assertEquals(4, getAttribute(attributes, layerAttr1.getDescription()).getLong(layerAttr1.getDescription()));
            Assert.assertEquals("five", getAttribute(attributes, layerAttr2.getDescription()).getString(layerAttr2.getDescription()));
            Assert.assertEquals("six", getAttribute(attributes, layerAttr3.getDescription()).getString(layerAttr3.getDescription()));
        }
    }
    
    /**
     * Non owner, non admin expects
     * 1. everything to be returned for public records
     * 2. limited info to be returned for controlled records
     * 3. nothing to be returned for owner only records
     * 
     * @throws Exception
     */
    @Test
    public void testWebservice_asNonOwner() throws Exception {
        login("nonowner", "password", new String[] { Role.USER });
        
        request.setRequestURI(GeoMapLayerController.GET_FEATURE_SERVICE_URL);
        request.setMethod("GET");
        
        request.setParameter(GeoMapLayerController.PARAM_BUFFER_KM, "0");
        request.setParameter(GeoMapLayerController.PARAM_LATITUDE_Y, "-2.5");
        request.setParameter(GeoMapLayerController.PARAM_LONGITUDE_X, "-2.5");
        request.addParameter(GeoMapLayerController.PARAM_MAP_LAYER_ID, layer1.getId().toString());
        request.addParameter(GeoMapLayerController.PARAM_MAP_LAYER_ID, layer2.getId().toString());
        
        this.handle(request, response);

        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray items = (JSONArray)json.get(GeoMapLayerController.JSON_KEY_ITEMS);
        Assert.assertNotNull(items);
        Assert.assertEquals(3, items.size());
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordPublic.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals("expect all attributes to be present", 3, attributes.size());
            
            Assert.assertEquals(7, getAttribute(attributes, recAttr1.getDescription()).getLong(recAttr1.getDescription()));
            Assert.assertEquals("eight", getAttribute(attributes, recAttr2.getDescription()).getString(recAttr2.getDescription()));
            Assert.assertEquals("nine", getAttribute(attributes, recAttr3.getDescription()).getString(recAttr3.getDescription()));
            
            Assert.assertEquals("Standard Taxonomic", obj.getString(JsonService.RECORD_KEY_CENSUS_METHOD));
        }
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordControlled.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals("there should be no attributes", 0, attributes.size());
        }
        {
            // features are unaffected
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_MAP_FEATURE, gmf.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES);
            Assert.assertEquals(4, getAttribute(attributes, layerAttr1.getDescription()).getLong(layerAttr1.getDescription()));
            Assert.assertEquals("five", getAttribute(attributes, layerAttr2.getDescription()).getString(layerAttr2.getDescription()));
            Assert.assertEquals("six", getAttribute(attributes, layerAttr3.getDescription()).getString(layerAttr3.getDescription()));
        }
    }
    
    /**
     * Anonymous expects the same thing as a non owner of a record
     * 
     * @throws Exception
     */
    @Test
    public void testWebservice_asAnonymous() throws Exception {
        request.setRequestURI(GeoMapLayerController.GET_FEATURE_SERVICE_URL);
        request.setMethod("GET");
        
        request.setParameter(GeoMapLayerController.PARAM_BUFFER_KM, "0");
        request.setParameter(GeoMapLayerController.PARAM_LATITUDE_Y, "-2.5");
        request.setParameter(GeoMapLayerController.PARAM_LONGITUDE_X, "-2.5");
        request.addParameter(GeoMapLayerController.PARAM_MAP_LAYER_ID, layer1.getId().toString());
        request.addParameter(GeoMapLayerController.PARAM_MAP_LAYER_ID, layer2.getId().toString());
        
        this.handle(request, response);

        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray items = (JSONArray)json.get(GeoMapLayerController.JSON_KEY_ITEMS);
        Assert.assertNotNull(items);
        Assert.assertEquals(3, items.size());
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordPublic.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals("expect all attributes to be present", 3, attributes.size());
            
            Assert.assertEquals(7, getAttribute(attributes, recAttr1.getDescription()).getLong(recAttr1.getDescription()));
            Assert.assertEquals("eight", getAttribute(attributes, recAttr2.getDescription()).getString(recAttr2.getDescription()));
            Assert.assertEquals("nine", getAttribute(attributes, recAttr3.getDescription()).getString(recAttr3.getDescription()));
            
            Assert.assertEquals("Standard Taxonomic", obj.getString(JsonService.RECORD_KEY_CENSUS_METHOD));
        }
        {
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_RECORD, recordControlled.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES); 
            
            Assert.assertEquals("there should be no attributes", 0, attributes.size());
        }
        {
            // features are unaffected
            JSONObject obj = getFeature(JsonService.JSON_ITEM_TYPE_MAP_FEATURE, gmf.getId().longValue(), items);
            Assert.assertNotNull(obj);
            JSONArray attributes = obj.getJSONArray(JsonService.JSON_KEY_ATTRIBUTES);
            Assert.assertEquals(4, getAttribute(attributes, layerAttr1.getDescription()).getLong(layerAttr1.getDescription()));
            Assert.assertEquals("five", getAttribute(attributes, layerAttr2.getDescription()).getString(layerAttr2.getDescription()));
            Assert.assertEquals("six", getAttribute(attributes, layerAttr3.getDescription()).getString(layerAttr3.getDescription()));
        }
    }
    
    private JSONObject getAttribute(JSONArray featureArray, String description) {
        for (int i=0; i<featureArray.size(); ++i) {
            JSONObject obj = featureArray.getJSONObject(i);
            if (obj.containsKey(description)) {
                return obj;
            }
        }
        return null;
    }
    
    private JSONObject getFeature(String type, long id, JSONArray featureArray) {
        for (int i=0; i<featureArray.size(); ++i) {
            JSONObject obj = featureArray.getJSONObject(i);
            if (obj.getLong(JsonService.JSON_KEY_ID) == id && type.equals(obj.getString(JsonService.JSON_KEY_TYPE))) {
                return obj;
            }
        }
        return null;
    }
}
