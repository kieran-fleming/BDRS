package au.com.gaiaresources.bdrs.model.record;

import java.util.Date;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

public class RecordDAOImplTest extends AbstractControllerTest {
    // testing the nullable fields...
    
    @Autowired
    private RecordDAO recDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private CensusMethodDAO cmDAO;
    
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
    
    @Test
    public void testGetChildRecords() {
        Record parent = createSimpleRecord(u);
        Record child1 = createSimpleRecord(u);
        Record child2 = createSimpleRecord(u);
        Record child3 = createSimpleRecord(u);
        
        CensusMethod cm1 = createCensusMethod("cm1");
        CensusMethod cm2 = createCensusMethod("cm2");
        
        child1.setCensusMethod(cm1);
        child2.setCensusMethod(cm2);
        child3.setCensusMethod(cm2);
        
        child1.setParentRecord(parent);
        child2.setParentRecord(parent);
        child3.setParentRecord(parent);
        
        recDAO.saveRecord(parent);
        recDAO.saveRecord(child1);
        recDAO.saveRecord(child2);
        recDAO.saveRecord(child3);
        
        PagedQueryResult<Record> result1 = recDAO.getChildRecords(null, parent.getId(), cm1.getId(), u);
        Assert.assertEquals("mismatch size", 1, result1.getCount());
        Assert.assertTrue("expected record not found", result1.getList().contains(child1));
        
        PagedQueryResult<Record> result2 = recDAO.getChildRecords(null, parent.getId(), cm2.getId(), u);
        Assert.assertEquals("mismatch size", 2, result2.getCount());
        Assert.assertTrue("expected record not found", result2.getList().contains(child2));
        Assert.assertTrue("expected record not found", result2.getList().contains(child3));
    }
    
    @Test
    public void testGetChildRecordPermissions() {
        User bee = userDAO.createUser("bee", "beefirst", "beelast", "bee@beehive.com.au", "password", "regkey", Role.USER);
        User cat = userDAO.createUser("cat", "catfirst", "catlast", "cat@cathouse.com.nz", "password", "regkey", Role.USER);
        
        Record parent = createSimpleRecord(bee);
        Record child1 = createSimpleRecord(bee);
        child1.setRecordVisibility(RecordVisibility.PUBLIC);
        child1.setHeld(false);
        Record child2 = createSimpleRecord(bee);
        child2.setRecordVisibility(RecordVisibility.OWNER_ONLY);
        Record child3 = createSimpleRecord(bee);
        child3.setRecordVisibility(RecordVisibility.PUBLIC);
        child3.setHeld(true);
        
        CensusMethod cm1 = createCensusMethod("cm1");
        
        child1.setCensusMethod(cm1);
        child2.setCensusMethod(cm1);
        child3.setCensusMethod(cm1);
        
        child1.setParentRecord(parent);
        child2.setParentRecord(parent);
        child3.setParentRecord(parent);
        
        recDAO.saveRecord(parent);
        recDAO.saveRecord(child1);
        recDAO.saveRecord(child2);
        recDAO.saveRecord(child3);
        
        // owner case
        {
            PagedQueryResult<Record> result = recDAO.getChildRecords(null, parent.getId(), cm1.getId(), bee);
            Assert.assertEquals("mismatch size", 3, result.getCount());
            Assert.assertTrue("expected record not found", result.getList().contains(child1));
            Assert.assertTrue("expected record not found", result.getList().contains(child2));
            Assert.assertTrue("expected record not found", result.getList().contains(child3));
        }
        
        // non owner case
        {
            PagedQueryResult<Record> result = recDAO.getChildRecords(null, parent.getId(), cm1.getId(), cat);
            Assert.assertEquals("mismatch size", 1, result.getCount());
            Assert.assertTrue("expected record not found", result.getList().contains(child1));
        }
        
        // admin case
        {
            PagedQueryResult<Record> result = recDAO.getChildRecords(null, parent.getId(), cm1.getId(), u);
            Assert.assertEquals("mismatch size", 3, result.getCount());
            Assert.assertTrue("expected record not found", result.getList().contains(child1));
            Assert.assertTrue("expected record not found", result.getList().contains(child2));
            Assert.assertTrue("expected record not found", result.getList().contains(child3));
        }
    }
    
    private CensusMethod createCensusMethod(String name) {
        CensusMethod cm = new CensusMethod();
        cm.setName(name);
        cm.setDescription("desc");
        cm.setTaxonomic(Taxonomic.TAXONOMIC);
        return cmDAO.save(cm);
    }
}
