package au.com.gaiaresources.bdrs.controller.record;

import java.util.Calendar;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletResponse;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import edu.emory.mathcs.backport.java.util.Collections;

public abstract class AbstractRecordFormAccessTest extends AbstractControllerTest {

    private GeometryBuilder geomBuilder = new GeometryBuilder();
    
    @Autowired
    protected SurveyDAO surveyDAO;
    @Autowired
    protected RecordDAO recDAO;
    @Autowired
    protected AttributeDAO attrDAO;
    @Autowired
    protected UserDAO userDAO;
    @Autowired
    protected TaxaDAO taxaDAO;

    protected IndicatorSpecies species;
    
    protected Survey survey;
    
    protected User admin;
    protected User owner;
    protected User nonOwner;
    
    protected Record record;
    
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
        
        TaxonGroup g1 = new TaxonGroup();
        g1.setName("fictionus animus");
        g1 = taxaDAO.save(g1);
        
        species = new IndicatorSpecies();
        species.setTaxonGroup(g1);
        species.setScientificName("sci name placeholder");
        species.setCommonName("common name placeholder");
        species = taxaDAO.save(species);
        
        survey = new Survey();
        survey.setName("my survey");
        
        Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
        speciesSet.add(species);
        survey.setSpecies(speciesSet);
        
        surveyDAO.save(survey);

        record = new Record();
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        record.setUser(owner);
        record.setWhen(cal.getTime());
        record.setLastDate(cal.getTime());
        record.setGeometry(geomBuilder.createSquare(-10, -10, 10));
        record.setSurvey(survey);
        
        recDAO.saveRecord(record);
    }
    
    
    @Test
    public void testFormGet_noUser_existingRecord_view_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), null, record, false, true, false);
    }
    
    @Test
    public void testFormGet_noUser_existingRecord_view_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), null, record, false, false, false);
    }
    
    @Test
    public void testFormGet_noUser_existingRecord_view_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), null, record, false, false, false);
    }
    
    // view access by non owner user - role admin
    
    @Test
    public void testFormGet_adminUser_existingRecord_view_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), admin, record, false, true, false);
    }
    
    @Test
    public void testFormGet_adminUser_existingRecord_view_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), admin, record, false, true, false);
    }
    
    @Test
    public void testFormGet_adminUser_existingRecord_view_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), admin, record, false, true, false);
    }
    
    // view access by non owner user - role user
    
    @Test
    public void testFormGet_nonOwnerUser_existingRecord_view_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), nonOwner, record, false, true, false);
    }
    
    @Test
    public void testFormGet_nonOwnerUser_existingRecord_view_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), nonOwner, record, false, false, false);
    }
    
    @Test
    public void testFormGet_nonOwnerUser_existingRecord_view_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), nonOwner, record, false, false, false);
    }
    
    
    // view access by owning user - role user
    
    @Test
    public void testFormGet_ownerUser_existingRecord_view_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), owner, record, false, true, false);
    }
    
    @Test
    public void testFormGet_ownerUser_existingRecord_view_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), owner, record, false, true, false);
    }
    
    @Test
    public void testFormGet_ownerUser_existingRecord_view_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), owner, record, false, true, false);
    }
    
    // edit section follows
    
    // edit access by non owner user - role admin
    
    @Test
    public void testFormGet_adminUser_existingRecord_edit_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), admin, record, true, true, true);
    }
    
    @Test
    public void testFormGet_adminUser_existingRecord_edit_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), admin, record, true, true, true);
    }
    
    @Test
    public void testFormGet_adminUser_existingRecord_edit_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), admin, record, true, true, true);
    }
    
    // edit access by non owner user - role user
    
    @Test
    public void testFormGet_nonOwnerUser_existingRecord_edit_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), nonOwner, record, true, false, true);
    }
    
    @Test
    public void testFormGet_nonOwnerUser_existingRecord_edit_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), nonOwner, record, true, false, true);
    }
    
    @Test
    public void testFormGet_nonOwnerUser_existingRecord_edit_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), nonOwner, record, true, false, true);
    }
    
    // edit access by owning user - role user
    
    @Test
    public void testFormGet_ownerUser_existingRecord_edit_public() throws Exception {
        record.setRecordVisibility(RecordVisibility.PUBLIC);
        testGetFormGet(getGetUri(), owner, record, true, true, true);
    }
    
    @Test
    public void testFormGet_ownerUser_existingRecord_edit_controlled() throws Exception {
        record.setRecordVisibility(RecordVisibility.CONTROLLED);
        testGetFormGet(getGetUri(), owner, record, true, true, true);
    }
    
    @Test
    public void testFormGet_ownerUser_existingRecord_edit_ownerOnly() throws Exception {
        record.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        testGetFormGet(getGetUri(), owner, record, true, true, true);
    }
    
    /**
     * Because non logged in users can now view forms in 'read only' mode, we have had to
     * remove the RolesAllowed annotations. As such we still need to control access on posts
     * 
     * Test not runnable on continuous integration server as RolesAllowed annotations do not
     * work, hence the exception we expect to be thrown never occurs.
     * 
     * @throws Exception
     */
    public void testPostWithNoLoggedInUser() throws Exception {
        
        request = super.createUploadRequest();
        
        request.setMethod("POST");
        request.setRequestURI(getPostUri());
        request.addParameters(getPostMap());
        
        // expect no user to be logged in...
        expectedEx.expect(AuthenticationCredentialsNotFoundException.class);
        // expect no message - this exception is thrown by spring  
        handle(request, response);
    }
    
    /**
     * @return the uri to GET the record form
     */
    abstract protected String getGetUri();
    
    /**
     * @return the uri to POST the record form
     */
    abstract protected String getPostUri();
    
    /**
     * @return The key to use when putting the survey id in the query dictionary
     */
    abstract protected String getSurveyIdKey();
    
    /**
     * @return The key to use when putting the record id in the query dictionary
     */
    abstract protected String getRecordIdKey();
    
    /**
     * @return the expected view name
     */
    abstract protected String getExpectedViewName();
    
    /**
     * @return the map to add to the mock request in order to fulfill the minimum requirements
     * to post a form. This is just to get past the param annotation checks - immediately following
     * these checks the handlers should be checking for a logged in user and throwing
     * AccessDeniedExceptions.
     * 
     * This map does not need to include the request method and request uri
     */
    abstract protected Map<String, String> getPostMap();
    
    /**
     * 
     * @return a map to add to the mock request in order to fulfill the min requirements to
     * GET a form.
     * 
     * This map does not need to include the request method and request uri.
     */
    @SuppressWarnings("unchecked")
    protected Map<String, String> getGetMap() {
        return Collections.emptyMap();
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
    protected void testGetFormGet(String uri, User loginUser, Record rec, boolean requestEdit, 
            boolean expectedAccessResult, boolean expectedFormEditState) throws Exception {
        
        if (loginUser != null) {
            login(loginUser.getName(), "password", loginUser.getRoles());    
        }
        
        request.setMethod("GET");
        request.setRequestURI(uri);
        request.setParameter(getSurveyIdKey(), survey.getId().toString());
        
        request.setParameter(RecordWebFormContext.PARAM_EDIT, Boolean.toString(requestEdit));
        
        request.addParameters(getGetMap());
        
        if (rec != null) {
            request.setParameter(getRecordIdKey(), record.getId().toString());
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
        assertViewName(mv, getExpectedViewName());
        RecordWebFormContext webFormContext = (RecordWebFormContext)mv.getModel().get(RecordWebFormContext.MODEL_WEB_FORM_CONTEXT);
        Assert.assertEquals("editable state does not match expected", requestEdit, webFormContext.isEditable());    
    }
}
