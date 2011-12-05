package au.com.gaiaresources.bdrs.controller.record;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.security.Role;


/**
 * Tests that the {@link ModerationController} holds and releases records properly.
 * @author stephanie
 */
public class ModerationControllerTest extends AbstractGridControllerTest {

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
    }
    
    @Test
    public void testReleaseRecords() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
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
    }
}
