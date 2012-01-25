package au.com.gaiaresources.bdrs.controller.record;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormSubmitAction;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

public class TrackerControllerFormSubmitActionTest extends
        AbstractControllerTest {

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private RedirectionService redirService;
    @Autowired
    private MetadataDAO metaDAO;
    @Autowired
    private RecordDAO recDAO;
    
    private Survey survey;
    
    private Record existingRecord;
            
    @Before
    public void setup() {
        
        User user = userDAO.getUser("admin");
        
        Calendar cal = Calendar.getInstance();
        cal.set(2009, 2, 3, 4, 40);
        
        survey = new Survey();
        survey.setName("test survey 1");
        survey.setDescription("test survey description");
        survey.setStartDate(cal.getTime());
        surveyDAO.save(survey);
        
        // Make everything not required for easier record creation
        for (RecordPropertyType rpt : RecordPropertyType.values()) {
            RecordProperty prop = new RecordProperty(survey, rpt, metaDAO);
            prop.setRequired(false);
        }
        
        existingRecord = new Record();
        existingRecord = new Record();
        existingRecord.setWhen(cal.getTime());
        existingRecord.setUser(user);
        existingRecord.setSurvey(survey);
        recDAO.saveRecord(existingRecord);
    }
    
    @Test
    public void testNewRecordMySightingsRedirect() throws Exception {
        
        survey.setFormSubmitAction(SurveyFormSubmitAction.MY_SIGHTINGS, metaDAO);
        
        this.login("admin", "password", new String[] { Role.ADMIN } );
        
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");

        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, redirService.getMySightingsUrl(survey));
        this.assertMessageCode(TrackerController.MSG_CODE_SAVE_NEW_SUCCESS_MY_SIGHTINGS);
    }
    
    @Test
    public void testNewRecordStayOnFormRedirect() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.STAY_ON_FORM, metaDAO);
        
        this.login("admin", "password", new String[] { Role.ADMIN } );
        
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");

        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        Integer newRecId = (Integer)mv.getModel().get(RecordWebFormContext.MODEL_RECORD_ID);
        Assert.assertNotNull("new record id cannot be null", newRecId);
        Record newRec = recDAO.getRecord(newRecId);
        
        this.assertRedirect(mv, redirService.getViewRecordUrl(newRec));
        this.assertMessageCode(TrackerController.MSG_CODE_SAVE_NEW_SUCCESS_STAY_ON_FORM);
    }
    
    @Test
    public void testExistingRecordMySightingsRedirect() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.MY_SIGHTINGS, metaDAO);
        
        this.login("admin", "password", new String[] { Role.ADMIN } );
        
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");

        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, existingRecord.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertRedirect(mv, redirService.getMySightingsUrl(survey));
        this.assertMessageCode(TrackerController.MSG_CODE_SAVE_EXISTING_SUCCESS_MY_SIGHTINGS);
    }

    @Test
    public void testExistingRecordStayOnFormRedirect() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.STAY_ON_FORM, metaDAO);
        
        this.login("admin", "password", new String[] { Role.ADMIN } );
        
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");

        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_RECORD_ID, existingRecord.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        Integer newRecId = (Integer)mv.getModel().get(RecordWebFormContext.MODEL_RECORD_ID);
        Assert.assertNotNull("new record id cannot be null", newRecId);
        Record newRec = recDAO.getRecord(newRecId);
        
        this.assertRedirect(mv, redirService.getViewRecordUrl(newRec));
        this.assertMessageCode(TrackerController.MSG_CODE_SAVE_EXISTING_SUCCESS_STAY_ON_FORM);
    }
    
    @Test
    public void testNewRecordAddAnother() throws Exception {
        survey.setFormSubmitAction(SurveyFormSubmitAction.STAY_ON_FORM, metaDAO);
        
        this.login("admin", "password", new String[] { Role.ADMIN } );
        
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");

        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(RecordWebFormContext.PARAM_SUBMIT_AND_ADD_ANOTHER, "true");
        
        ModelAndView mv = handle(request, response);
        
        Integer newRecId = (Integer)mv.getModel().get(RecordWebFormContext.MODEL_RECORD_ID);
        Assert.assertNotNull("new record id cannot be null", newRecId);
        
        this.assertRedirect(mv, RecordWebFormContext.SURVEY_RENDER_REDIRECT_URL);
        this.assertMessageCode(TrackerController.MSG_CODE_SAVE_SUCCESS_ADD_ANOTHER);
    }
    
    
    /**
     * Override mock http creation
     * 
     * @return
     */
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return this.createUploadRequest();
    }
}
