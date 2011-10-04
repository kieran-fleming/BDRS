package au.com.gaiaresources.bdrs.service.lsid;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;

public class LSIDServiceTest extends AbstractGridControllerTest {
    
    @Autowired
    private LSIDService lsidService;
    
    private Logger log = Logger.getLogger(getClass());
    
    @Test
    public void testSurveyLsid() {
        Lsid lsid = lsidService.toLSID(survey1);
        Assert.assertTrue( lsid.toString().endsWith("Survey:" + survey1.getId().toString()));
    }
}
