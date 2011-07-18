package au.com.gaiaresources.bdrs.model.record;

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;

public class RecordDAOImplTest extends AbstractControllerTest {
    // testing the nullable fields...
    
    @Autowired
    RecordDAO recDAO;
    @Autowired
    SurveyDAO surveyDAO;
    
    User u;
    
    @Before
    public void setup() {
        // we know admin will always exist so...
        u = userDAO.getUser("admin");
    }
    
    // unfortunately we have to write to the database to conclude this test works properly...
    @After
    public void teardown() {
        
    }
    
    private Record createSimpleRecord(User u) {
        return createSimpleRecord(u, null);
    }
    
    private Record createSimpleRecord(User u, Survey s) {
        Record rec = new Record();
        rec.setUser(u);
        rec.setLastDate(new Date());
        rec.setTime(1000L);
        // made to be nullable...
        rec.setNumber(null);
        rec.setSpecies(null);
        rec.setSurvey(s);
        return rec;
    }
    
    // dont care about the name
    private Survey createSurvey() {
        Survey survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        return surveyDAO.save(survey);
    }
    
    @Test
    public void testSave() {
        Record rec = new Record();
        rec.setUser(u);
        rec.setLastDate(new Date());
        rec.setTime(1000L);
        // made to be nullable...
        rec.setNumber(null);
        rec.setSpecies(null);
        recDAO.saveRecord(rec);
    }
    
    @Test
    public void testRecordsInRecords() {
        Record r1 = createSimpleRecord(u);
        Record r2 = createSimpleRecord(u);
        
        r2.setParentRecord(r1);
        
        recDAO.saveRecord(r1);
        recDAO.saveRecord(r2);
        
        // so the changes are reflected in our object...
        sessionFactory.getCurrentSession().refresh(r1);
        sessionFactory.getCurrentSession().refresh(r2);
        
        Assert.assertEquals(1, r1.getChildRecords().size());
        Assert.assertTrue(r1.getChildRecords().contains(r2));
    }
    
    @Test
    public void testRecordsInRecordsRelationManaging() {
        Record r1 = createSimpleRecord(u);
        Record r2 = createSimpleRecord(u);
        
        // watch as the change is magically ignored!
        // This is because the relationship is mantained by the child record, so adding
        // to the list of child records in the parent record has no effect.
        r1.getChildRecords().add(r2);
        
        recDAO.saveRecord(r1);
        recDAO.saveRecord(r2);
        
        // so the changes are reflected in our object...
        sessionFactory.getCurrentSession().refresh(r1);
        sessionFactory.getCurrentSession().refresh(r2);
        
        Assert.assertEquals(0, r1.getChildRecords().size());
        Assert.assertFalse(r1.getChildRecords().contains(r2));
        Assert.assertNull(r1.getParentRecord());
    }
}
