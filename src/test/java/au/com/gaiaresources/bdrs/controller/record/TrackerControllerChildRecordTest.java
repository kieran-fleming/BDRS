package au.com.gaiaresources.bdrs.controller.record;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
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
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * For testing the cm / cm and rec / rec, parent / child relationship.
 * 
 * @author aaron
 *
 */
public class TrackerControllerChildRecordTest extends AbstractControllerTest {

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private RecordDAO recDAO;
    @Autowired
    private CensusMethodDAO cmDAO;
    @Autowired
    private MetadataDAO metaDAO;
    
    private Survey survey;
    
    private CensusMethod parentCensusMethod;
    private CensusMethod childCensusMethod;
    
    private Record parentRecord;
    private Record childRecord;
    
    private User user;
    
    @Before
    public void setup() {
        
        user = userDAO.getUser("admin");
        
        Calendar cal = Calendar.getInstance();
        cal.set(2009, 2, 3, 4, 40);
        
        childCensusMethod = new CensusMethod();
        childCensusMethod.setName("child census method");
        childCensusMethod.setDescription("child census method desc");
        childCensusMethod.setTaxonomic(Taxonomic.TAXONOMIC);
        cmDAO.save(childCensusMethod);
        
        parentCensusMethod = new CensusMethod();
        parentCensusMethod.setName("parent census method");
        parentCensusMethod.setDescription("parent census method desc");
        parentCensusMethod.setTaxonomic(Taxonomic.TAXONOMIC);
        
        List<CensusMethod> cmList = new ArrayList<CensusMethod>();
        cmList.add(childCensusMethod);
        parentCensusMethod.setCensusMethods(cmList);
        
        cmDAO.save(parentCensusMethod);
        
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
        
        parentRecord = new Record();
        parentRecord.setWhen(cal.getTime());
        parentRecord.setUser(user);
        parentRecord.setSurvey(survey);
        recDAO.saveRecord(parentRecord);
        
        childRecord = new Record();
        childRecord.setWhen(cal.getTime());
        childRecord.setCensusMethod(childCensusMethod);
        childRecord.setUser(user);
        childRecord.setSurvey(survey);
        childRecord.setParentRecord(parentRecord);
        recDAO.saveRecord(childRecord);
    }
    
    @Test
    public void testCreateChildRecord() throws Exception {
        
        this.login("admin", "password", new String[] { Role.ADMIN } );
        
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");
        
        request.setParameter(TrackerController.PARAM_CENSUS_METHOD_ID, childCensusMethod.getId().toString());
        request.setParameter(TrackerController.PARAM_PARENT_RECORD_ID, parentRecord.getId().toString());
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        Integer newRecId = (Integer)mv.getModel().get(RecordWebFormContext.MODEL_RECORD_ID);
        
        Assert.assertNotNull("new record id cannot be null", newRecId);
        
        Record newRec = recDAO.getRecord(newRecId);
        
        Assert.assertNotNull("new record must exist", newRec);
        
        Assert.assertEquals("record does not have expected parent", parentRecord, newRec.getParentRecord());
        Assert.assertEquals("record does not have expected census method", childCensusMethod, newRec.getCensusMethod());
    }
    
    @Test
    public void testGetFormOfChildRecord() throws Exception {
        this.login("admin", "password", new String[] { Role.ADMIN } );
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("GET");
        request.setParameter(TrackerController.PARAM_RECORD_ID, childRecord.getId().toString());
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        
        ModelAndView mv = handle(request, response);
        
        this.assertViewName(mv, TrackerController.TRACKER_VIEW_NAME);
        
        List<Record> parentRecList = (List<Record>)mv.getModel().get(TrackerController.MV_PARENT_RECORD_LIST);
        Assert.assertEquals("size mismatch", 1, parentRecList.size());
        Assert.assertTrue("expected record not contained in list", parentRecList.contains(parentRecord));
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
