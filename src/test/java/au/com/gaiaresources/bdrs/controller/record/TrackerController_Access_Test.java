package au.com.gaiaresources.bdrs.controller.record;

import java.math.BigDecimal;
import java.util.Calendar;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.security.access.AccessDeniedException;
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
    SurveyDAO surveyDAO;
    @Autowired
    RecordDAO recDAO;
    @Autowired
    AttributeDAO attrDAO;
    @Autowired
    UserDAO userDAO;

    
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
    
    @Rule
    public ExpectedException expectedEx = ExpectedException.none();
    
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
        request.setParameter(RecordWebFormContext.PARAM_EDIT, Boolean.TRUE.toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, TrackerController.TRACKER_VIEW_NAME);

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
        request.setParameter(RecordWebFormContext.PARAM_EDIT, Boolean.TRUE.toString());

        expectedEx.expect(AccessDeniedException.class);
        expectedEx.expectMessage(RecordWebFormContext.MSG_CODE_EDIT_AUTHFAIL);
        handle(request, response);
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
        request.setParameter(RecordWebFormContext.PARAM_EDIT, Boolean.TRUE.toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, TrackerController.TRACKER_VIEW_NAME);
        
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

        expectedEx.expect(AccessDeniedException.class);
        expectedEx.expectMessage(RecordWebFormContext.MSG_CODE_EDIT_AUTHFAIL);
        handle(request, response);
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
    
    /**
     * Views a record
     * 
     * @throws Exception
     */
    @Test
    public void testFormGet_noUser_newRecord_edit() throws Exception {
        
        // attempting to view a new record results in trying to create
        // a new record (i.e. entering edit mode).
        // since non logged in users can never edit records, access
        // will always fail.
        testGetFormGet(null, null, true, false, true);
        
    }
    
    @Test
    public void testFormGet_noUser_newRecord_view() throws Exception {

        // attempting to view a new record results in trying to create
        // a new record (i.e. entering edit mode).
        // since non logged in users can never edit records, access
        // will always fail.
        testGetFormGet(null, null, false, false, true);
    }
    
    @Test
    public void testFormGet_noUser_existingRecord_view_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(null, record, false, true, false);
    }
    
    @Test
    public void testFormGet_noUser_existingRecord_view_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(null, record, false, false, false);
    }
    
    @Test
    public void testFormGet_noUser_existingRecord_view_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(null, record, false, false, false);
    }
    
    /**
     * parameterized test
     * 
     * @param loginUser - user to login as, can be null
     * @param rec - record to request for the form, can be null
     * @param requestEdit - value to pass to request record editing
     * @param expectedAccessResult - do we expect to be able to see the tracker form?
     * @param expectedFormEditState - should the tracker form's state be editable?
     * @throws Exception
     */
    private void testGetFormGet(User loginUser, Record rec, boolean requestEdit, 
            boolean expectedAccessResult, boolean expectedFormEditState) throws Exception {
        
        if (loginUser != null) {
            login(loginUser.getName(), "password", loginUser.getRoles());    
        }
        
        request.setMethod("GET");
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        
        request.setParameter(RecordWebFormContext.PARAM_EDIT, Boolean.toString(requestEdit));
        
        if (rec != null) {
            request.setParameter(TrackerController.PARAM_RECORD_ID, record.getId().toString());
        }

        if (!expectedAccessResult) {
            expectedEx.expect(AccessDeniedException.class);
            if (expectedFormEditState) {
                // edit mode
                expectedEx.expectMessage(RecordWebFormContext.MSG_CODE_EDIT_AUTHFAIL);
            } else {
                // view mode
                expectedEx.expectMessage(RecordWebFormContext.MSG_CODE_VIEW_AUTHFAIL);
            }
        }
        ModelAndView mv = handle(request, response);
        
        // if access has failed exceptions would have been thrown in the handler and we never reach the
        // following section of code
        
        // if we expect access to be allowed....
        Assert.assertEquals(HttpServletResponse.SC_OK, response.getStatus());
        assertViewName(mv, TrackerController.TRACKER_VIEW_NAME);
        RecordWebFormContext webFormContext = (RecordWebFormContext)mv.getModel().get(RecordWebFormContext.MODEL_WEB_FORM_CONTEXT);
        Assert.assertEquals("editable state does not match expected", requestEdit, webFormContext.isEditable());        
    }
    
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
    }
}
