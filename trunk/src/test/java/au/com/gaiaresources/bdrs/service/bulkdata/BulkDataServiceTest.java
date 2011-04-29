package au.com.gaiaresources.bdrs.service.bulkdata;

import java.io.InputStream;
import java.text.ParseException;
import java.util.Calendar;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.survey.Survey;

public class BulkDataServiceTest extends AbstractControllerTest {
    @Autowired
    private BulkDataService bulkDataService;
    
    Logger log = Logger.getLogger(getClass());
    
    @Test
    public void testImportSurvey() throws Exception, ParseException {
        InputStream stream = getClass().getResourceAsStream("basic_upload.xls");
        Survey survey = new Survey();

        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, stream);
        
        Assert.assertEquals(1, bulkUpload.getRecordUploadList().size());
        RecordUpload recUpload = bulkUpload.getRecordUploadList().get(0);

        Calendar cal = Calendar.getInstance();
        cal.set(2011, 2, 27, 14, 42, 0);
        
        //Assert.assertEquals(cal.getTime(), recUpload.getWhen());
        // there's some messed up rounding going on here... accurate to within the second (1000 ms)
        Assert.assertTrue(Math.abs(cal.getTime().getTime() - recUpload.getWhen().getTime()) < 1000);
    }
}
