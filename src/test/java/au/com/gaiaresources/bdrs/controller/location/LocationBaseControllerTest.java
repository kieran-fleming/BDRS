package au.com.gaiaresources.bdrs.controller.location;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.LocationAttributeFormField;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.security.Role;

public class LocationBaseControllerTest extends AbstractControllerTest {
    
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private AttributeDAO attributeDAO;
    @Autowired
    private MetadataDAO metadataDAO;

    private Survey simpleSurvey;
    private Survey locAttSurvey;
    private List<Attribute> locationAttributes = new ArrayList<Attribute>();
    
    @Before
    public void setUp() throws Exception {
        simpleSurvey = new Survey();
        simpleSurvey.setName("Simple Survey");
        simpleSurvey.setActive(true);
        simpleSurvey.setStartDate(new Date());
        simpleSurvey.setDescription("Simple Test Survey");
        Metadata md2 = simpleSurvey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md2);
        simpleSurvey = surveyDAO.save(simpleSurvey);
        
        locAttSurvey = new Survey();
        locAttSurvey.setName("Location Attributes Survey");
        locAttSurvey.setActive(true);
        locAttSurvey.setStartDate(new Date());
        locAttSurvey.setDescription("Location Attributes Test Survey");
        Metadata md3 = locAttSurvey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md3);
        
        // create location attributes for the survey
        locAttSurvey.setAttributes(createLocationAttributes());
        
        locAttSurvey = surveyDAO.save(locAttSurvey);
    }

    @Test
    public void testListSurveyLocations() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/locationListing.htm");
        request.setParameter("surveyId", String.valueOf(simpleSurvey.getId()));

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "locationListing");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
    }

    @Test
    public void testAddLocation() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/editLocation.htm");
        request.setParameter("surveyId", String.valueOf(simpleSurvey.getId()));
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "surveyEditLocation");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
    }

    /**
     * Tests the basic use case of creating a new location and clicking save.
     */
    @Test
    public void testAddLocationSubmit() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/editLocation.htm");
        request.setParameter("surveyId", String.valueOf(simpleSurvey.getId()));
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("survey", String.valueOf(simpleSurvey.getId()));
        params.put("locationName", "Test Location 1234");
        params.put("locationDescription", "This is a test location");
        params.put("location_WKT", "POINT(115.77240371813 -31.945572001881)");
        params.put("latitude", "-31.945572001881");
        params.put("longitude", "115.77240371813");

        request.setParameters(params);
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/locationListing.htm", redirect.getUrl());
        
        Location location = locationDAO.getLocationByName(simpleSurvey.getName(), params.get("locationName"));
        Assert.assertEquals(location.getName(), params.get("locationName"));
        Assert.assertEquals(location.getDescription(), params.get("locationDescription"));
        // ignore the wkt right now because going in it is POINT[](x,y), but coming out it is POINT[ ](x,y)
        //Assert.assertEquals(location.getLocation().toText(), params.get("location_WKT"));
        Assert.assertEquals(String.valueOf(location.getY()), params.get("latitude"));
        Assert.assertEquals(String.valueOf(location.getX()), params.get("longitude"));
    }
    
    @Test
    public void testAddLocationAttributes() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/editLocation.htm");
        request.setParameter("surveyId", String.valueOf(locAttSurvey.getId()));
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "surveyEditLocation");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "locationFormFieldList");

        int curWeight = Integer.MIN_VALUE;
        Assert.assertEquals(locAttSurvey, mv.getModelMap().get("survey"));
        List<LocationAttributeFormField> formFieldList = (List<LocationAttributeFormField>) mv.getModelMap().get("locationFormFieldList");
        Assert.assertEquals(locAttSurvey.getAttributes().size(), formFieldList.size());
        for (LocationAttributeFormField formField : formFieldList) {
            // Test attributes are sorted by weight
            Assert.assertTrue(formField.getWeight() >= curWeight);
            curWeight = formField.getWeight();
            Assert.assertTrue(formField.isAttributeFormField());
        }
    }
    
    /**
     * Tests the basic use case of creating a new location and clicking save.
     */
    @Test
    public void testAddLocationAttributesSubmit() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/editLocation.htm");
        request.setParameter("surveyId", String.valueOf(locAttSurvey.getId()));
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("survey", String.valueOf(locAttSurvey.getId()));
        params.put("locationName", "Test Location 1234");
        params.put("locationDescription", "This is a test location");
        params.put("location_WKT", "POINT(115.77240371813 -31.945572001881)");
        params.put("latitude", "-31.945572001881");
        params.put("longitude", "115.77240371813");
        params.putAll(createAttributes(locAttSurvey));
        
        request.setParameters(params);
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/locationListing.htm", redirect.getUrl());
        
        Location location = locationDAO.getLocationByName(locAttSurvey.getName(), params.get("locationName"));
        Assert.assertEquals(location.getName(), params.get("locationName"));
        Assert.assertEquals(location.getDescription(), params.get("locationDescription"));
        // ignore the wkt right now because going in it is POINT[](x,y), but coming out it is POINT[ ](x,y)
        //Assert.assertEquals(location.getLocation().toText(), params.get("location_WKT"));
        Assert.assertEquals(String.valueOf(location.getY()), params.get("latitude"));
        Assert.assertEquals(String.valueOf(location.getX()), params.get("longitude"));

        String key;
        for (TypedAttributeValue recAttr : location.getAttributes()) {
            Attribute attr = recAttr.getAttribute();
            if (locAttSurvey.getAttributes().contains(recAttr.getAttribute())) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
            } else {
                Assert.assertFalse(true);
                key = null;
            }

            switch (recAttr.getAttribute().getType()) {
            case INTEGER:
            case INTEGER_WITH_RANGE:
                Assert.assertEquals(Integer.parseInt(params.get(key)), recAttr.getNumericValue().intValue());
                break;
            case DECIMAL:
                Assert.assertEquals(Double.parseDouble(params.get(key)), recAttr.getNumericValue().doubleValue());
                break;
            case DATE:
                Assert.assertEquals(new Date(), recAttr.getDateValue());
                break;
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
            case BARCODE:
            case REGEX:
            case TIME:
            case HTML:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
                Assert.assertEquals(params.get(key), recAttr.getStringValue());
                break;
            case STRING_WITH_VALID_VALUES:
                Assert.assertEquals(params.get(key), recAttr.getStringValue());
                break;
            case MULTI_CHECKBOX:
                {
                    // make sure the correct data got posted to the server correctly
                    Assert.assertEquals(2, request.getParameterValues(key).length);
                    Set<String> optionSet = new HashSet<String>();
                    for(AttributeOption opt : recAttr.getAttribute().getOptions()) {
                        optionSet.add(opt.getValue());
                    }
                    for(String val : recAttr.getMultiCheckboxValue()){
                        Assert.assertTrue(optionSet.contains(val));
                    }
                }
                break;
            case MULTI_SELECT:
                {
                    // make sure the correct data got posted to the server correctly
                    Assert.assertEquals(2, request.getParameterValues(key).length);
                    Set<String> optionSet = new HashSet<String>();
                    for(AttributeOption opt : recAttr.getAttribute().getOptions()) {
                        optionSet.add(opt.getValue());
                    }
                    for(String val : recAttr.getMultiSelectValue()){
                        Assert.assertTrue(optionSet.contains(val));
                    }
                }
                break;
            case SINGLE_CHECKBOX:
                Assert.assertEquals(recAttr.getStringValue() + " should be 'true'!", 
                                    Boolean.parseBoolean(params.get(key)), 
                                    Boolean.parseBoolean(recAttr.getStringValue()));
                break;  
            case FILE:
            case IMAGE:
                Assert.assertEquals(params.get(key), recAttr.getStringValue());
                break;
            default:
                Assert.assertTrue("Unknown Attribute Type: "
                        + recAttr.getAttribute().getType().toString(), false);
                break;
            }
        }
    }

    private List<Attribute> createLocationAttributes() {
        Attribute attr;
        for (AttributeType attrType : AttributeType.values()) {
            for (AttributeScope scope : new AttributeScope[] {
                    AttributeScope.LOCATION }) {

                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(scope);
                attr.setTag(false);

                if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for (int i = 0; i < 4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        opt = attributeDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }

                attr = attributeDAO.save(attr);
                locationAttributes.add(attr);
            }
        }
        return locationAttributes;
    }
    
    private Map<String, String> createAttributes(Survey survey) {
        return createLocationAttributeValues(survey, "123", new SimpleDateFormat("dd MMM yyyy"), new Date());
    }
    
    private Map<String, String> createLocationAttributeValues(Survey survey, String intWithRangeValue, DateFormat dateFormat, Date today) {
        Map<String, String> params = new HashMap<String,String>();
        String key;
        String value;
        for (Attribute attr : survey.getAttributes()) {
            if(!AttributeScope.LOCATION.equals(attr.getScope())) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
                value = "";
                switch (attr.getType()) {
                case INTEGER:
                    value = "123";
                    break;
                case INTEGER_WITH_RANGE:
                    value = intWithRangeValue;
                    break;
                case DECIMAL:
                    value = "456.7";
                    break;
                case DATE:
                    value = dateFormat.format(today);
                    break;
                case STRING_AUTOCOMPLETE:
                case STRING:
                    value = "Test Survey Attr String";
                    break;
                case REGEX:
                    value = "";
                    break;
                case BARCODE:
                    value = "#123456";
                    break;
                case TIME:
                    value = "12:34";
                    break;
                case TEXT:
                    value = "Test Survey Attr Text";
                    break;
                case STRING_WITH_VALID_VALUES:
                    value = attr.getOptions().iterator().next().getValue();
                    break;
                case MULTI_CHECKBOX:
                case MULTI_SELECT:
                    List<AttributeOption> opts = attr.getOptions();
                    request.addParameter(key, opts.get(0).getValue());
                    request.addParameter(key, opts.get(1).getValue());
                    value = null;
                    break;
                case SINGLE_CHECKBOX:
                    value = String.valueOf(true);
                    break;
                case FILE:
                    String file_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockFileFile = new MockMultipartFile(key,
                            file_filename, "audio/mpeg", file_filename.getBytes());
                    ((MockMultipartHttpServletRequest) request).addFile(mockFileFile);
                    value = file_filename;
                    break;
                case IMAGE:
                    String image_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockImageFile = new MockMultipartFile(key,
                            image_filename, "image/png", image_filename.getBytes());
                    ((MockMultipartHttpServletRequest) request).addFile(mockImageFile);
                    value = image_filename;
                    break;
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    value = "<hr/>";
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + attr.getType().toString(), false);
                    break;
                }
                if(value != null) {
                    params.put(key, value);
                }
                // Otherwise value added directly to the request parameters
                // this is to support adding an "array" of values.
            }
        }
        return params;
    }

    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
    }
}
