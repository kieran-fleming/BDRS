package au.com.gaiaresources.bdrs.controller.record;

import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.email.impl.MockEmail;
import au.com.gaiaresources.bdrs.email.impl.MockEmailService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentService;


/**
 * Tests that the {@link ModerationController} holds and releases records properly.
 * @author stephanie
 */
public class ModerationControllerTest extends AbstractGridControllerTest {
    
    // MockEmailService
    @Autowired
    private EmailService emailService;
    // assigned via casting
    private MockEmailService mockEmailService;
    @Autowired
    private ContentService contentService;
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() {
        mockEmailService = (MockEmailService)emailService;
        mockEmailService.clearEmails();
    }

    @Test
    public void testHoldRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        Survey survey = this.singleSiteMultiTaxaSurvey;
        List<Record> surveyRecList = this.getInitialRecordList(survey);
        
        Record refRecord1 = surveyRecList.get(0);
        
        request.setRequestURI(ModerationController.RECORD_MODERATE_URL);
        request.setMethod("POST");
        request.setParameter(ModerationController.PARAM_RECORD_ID, refRecord1.getId().toString());
        request.setParameter(ModerationController.PARAM_HOLD, String.valueOf(true));
        
        this.handle(request, response);
        
        // check that the record is held
        Record actualRecord = recordDAO.getRecord(refRecord1.getId());
        Assert.assertTrue("Record should be held", actualRecord.isHeld());
        
        // emails not sent out when holding records.
        Assert.assertEquals("Mismatch email count", 0, mockEmailService.getMockEmailList().size());
    }
    
    @Test
    public void testReleaseRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        User moderator = userDAO.getUser("admin");
        
        Survey survey = this.singleSiteMultiTaxaSurvey;
        List<Record> surveyRecList = this.getInitialRecordList(survey);
        
        Record refRecord1 = surveyRecList.get(0);
        refRecord1.setHeld(true);
        recordDAO.saveRecord(refRecord1);
        
        request.setRequestURI(ModerationController.RECORD_MODERATE_URL);
        request.setMethod("POST");
        request.setParameter(ModerationController.PARAM_RECORD_ID, refRecord1.getId().toString());
        request.setParameter(ModerationController.PARAM_HOLD, String.valueOf(false));
        
        this.handle(request, response);
        
        // check that the record is held
        Record actualRecord = recordDAO.getRecord(refRecord1.getId());
        Assert.assertFalse("Record should not be held", actualRecord.isHeld());
        
        Assert.assertEquals("Mismatch email count", 1, mockEmailService.getMockEmailList().size());
        MockEmail mockEmail = mockEmailService.getMockEmailList().get(0);
        Assert.assertEquals("Template mismatch", contentService.getContent(sesh, "email/RecordReleased"), mockEmail.getMessage());
        Assert.assertEquals("Email 'to' mismatch", refRecord1.getUser().getEmailAddress(), mockEmail.getTo());
        Assert.assertEquals("Email 'from' mismatch'", moderator.getEmailAddress(), mockEmail.getFrom());
        
        Map<String, Object> vmParam = mockEmail.getParams();
        
        Assert.assertEquals("Mod first name mismatch", moderator.getFirstName(), vmParam.get(ModerationController.VM_PARAM_KEY_MOD_FIRST_NAME));
        Assert.assertEquals("Mod last name mismatch", moderator.getLastName(), vmParam.get(ModerationController.VM_PARAM_KEY_MOD_LAST_NAME));
        Assert.assertEquals("Owner first name mismatch", refRecord1.getUser().getFirstName(), vmParam.get(ModerationController.VM_PARAM_KEY_OWNER_FIRST_NAME));
        Assert.assertEquals("Owner last name mismatch", refRecord1.getUser().getLastName(), vmParam.get(ModerationController.VM_PARAM_KEY_OWNER_LAST_NAME));
        List<String> recUrlList = (List<String>)vmParam.get(ModerationController.VM_PARAM_KEY_RECORD_URL_LIST);
        Assert.assertEquals("Rec url count mismatch", 1, recUrlList.size());
    }
    
    @Test
    public void testReleaseMultipleRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        Survey survey = this.singleSiteMultiTaxaSurvey;
        List<Record> surveyRecList = this.getInitialRecordList(survey);
        
        Record refRecord1 = surveyRecList.get(0);
        refRecord1.setHeld(true);
        recordDAO.saveRecord(refRecord1);
        
        Record refRecord2 = new Record();
        refRecord2.setUser(user);
        refRecord2.setSurvey(survey);
        refRecord2.setHeld(true);
        recordDAO.saveRecord(refRecord2);
        
        request.setRequestURI(ModerationController.RECORD_MODERATE_URL);
        request.setMethod("POST");
        request.setParameter(ModerationController.PARAM_RECORD_ID, refRecord1.getId().toString());
        request.addParameter(ModerationController.PARAM_RECORD_ID, refRecord2.getId().toString());
        request.setParameter(ModerationController.PARAM_HOLD, String.valueOf(false));
        
        this.handle(request, response);
        
        // check that the record is held
        Record actualRecord = recordDAO.getRecord(refRecord1.getId());
        Assert.assertFalse("Record should not be held", actualRecord.isHeld());
        
        Assert.assertEquals("Mismatch email count", 2, mockEmailService.getMockEmailList().size());
    }
}
