package au.com.gaiaresources.bdrs.controller.record;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.map.GeoMapFeatureDAO;
import au.com.gaiaresources.bdrs.model.map.GeoMapLayerDAO;
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

public class TrackerController_Access_Test extends AbstractControllerTest {
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
    
    User admin;
    User owner;
    User nonOwner;
    
    Record record;
    
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

        record = new Record();
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        record.setUser(owner);
        record.setWhen(cal.getTime());
        record.setLastDate(cal.getTime());
        record.setGeometry(geomBuilder.createSquare(-10, -10, 10));
        record.setSurvey(survey);
        record.getAttributes().add(createTestAttrValue(recAttr1, "1"));
        record.getAttributes().add(createTestAttrValue(recAttr2, "two"));
        record.getAttributes().add(createTestAttrValue(recAttr3, "three"));
        recDAO.saveRecord(record);
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
     * has access
     * @throws Exception 
     */
    @Test
    public void testGetAccessBy_Owner() throws Exception {
        login(owner.getName(), "password", new String[] { Role.USER });
        
        request.setMethod("GET");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "tracker");

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    /**
     * no access
     * @throws Exception 
     */
    @Test
    public void testGetAccessBy_NonOwner_NonAdmin() throws Exception {
        login(nonOwner.getName(), "password", new String[] { Role.USER });
        
        request.setMethod("GET");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());

        ModelAndView mv = handle(request, response);
        Assert.assertNull(mv);
        
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
    
    /**
     * has access
     * @throws Exception 
     */
    @Test
    public void testGetAccessBy_NonOwner_Admin() throws Exception {
        login(admin.getName(), "password", new String[] { Role.ADMIN });
        
        request.setMethod("GET");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "tracker");
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    
    /**
     * has access but will fail due to lack of params
     * @throws Exception 
     */
    @Test
    public void testPostAccessBy_Owner() throws Exception {
        login(owner.getName(), "password", new String[] { Role.USER });
        
        request.setMethod("POST");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());

        ModelAndView mv = handle(request, response);
        View view = mv.getView();
        Assert.assertTrue(view instanceof RedirectView);

        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    /**
     * no access - will get 401
     * @throws Exception 
     */
    @Test
    public void testPostAccessBy_NonOwner_NonAdmin() throws Exception {
        login(nonOwner.getName(), "password", new String[] { Role.USER });
        
        request.setMethod("POST");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());

        ModelAndView mv = handle(request, response);
        Assert.assertNull(mv);
        
        Assert.assertEquals(HttpServletResponse.SC_FORBIDDEN, response.getStatus());
    }
    
    /**
     * has access but will fail due to lack of params
     * @throws Exception 
     */
    @Test
    public void testPostAccessBy_NonOwner_Admin() throws Exception {
        login(admin.getName(), "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());

        ModelAndView mv = handle(request, response);
        View view = mv.getView();
        Assert.assertTrue(view instanceof RedirectView);
        
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}
