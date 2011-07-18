package au.com.gaiaresources.bdrs.model.record;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

public class RecordDAOImplSearchTest extends AbstractControllerTest {

    @Autowired
    RecordDAO recDAO;
    @Autowired
    SurveyDAO surveyDAO;
    
    User u;
    User u1 ;
    User u2;
    
    Survey s1;
    Survey s2;
    
    Record rec1;
    Record rec2;
    Record rec3;
    Record rec4;
    
    @Before
    public void setup() {
        // we know admin will always exist so...
        u = userDAO.getUser("admin");
        
        u1 = createUser("u1");
        u2 = createUser("u2");
        
        s1 = createSurvey();
        s2 = createSurvey();
        
        rec1 = createSimpleRecord(u1, s1);
        rec2 = createSimpleRecord(u2, s1);
        rec3 = createSimpleRecord(u1, s2);
        rec4 = createSimpleRecord(u2, s2);
    }
    
    // unfortunately we have to write to the database to conclude this test works properly...
    @After
    public void teardown() {
        
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
        return recDAO.saveRecord(rec);
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
    
    private User createUser(String name) {
        //return userDAO.createUser(name, name, name, name, name, name, [Role.USER]);
        return userDAO.createUser(name, name, name, name, name, name, Role.USER);
    }
    
    @Test 
    public void testSearch1() {
        PagedQueryResult<Record> result = recDAO.search(null, s1.getId(), null);
        
        Assert.assertEquals(2, result.getCount());
        Assert.assertTrue(result.getList().contains(rec1));
        Assert.assertTrue(result.getList().contains(rec2));
    }

    @Test 
    public void testSearch2() {
        List<Integer> userIdList = new ArrayList<Integer>();
        userIdList.add(u1.getId());
        PagedQueryResult<Record> result = recDAO.search(null, s1.getId(), userIdList);
        
        Assert.assertEquals(1, result.getCount());
        Assert.assertTrue(result.getList().contains(rec1));
    }
    
    @Test 
    public void testSearch3() {
        List<Integer> userIdList = new ArrayList<Integer>();
        userIdList.add(u1.getId());
        PagedQueryResult<Record> result = recDAO.search(null, null, userIdList);
        
        Assert.assertEquals(2, result.getCount());
        Assert.assertTrue(result.getList().contains(rec1));
        Assert.assertTrue(result.getList().contains(rec3));
    }
    
    @Test 
    public void testSearch4() {
        PagedQueryResult<Record> result = recDAO.search(null, null, null);
        
        Assert.assertEquals(4, result.getCount());
        Assert.assertTrue(result.getList().contains(rec1));
        Assert.assertTrue(result.getList().contains(rec2));
        Assert.assertTrue(result.getList().contains(rec3));
        Assert.assertTrue(result.getList().contains(rec4));
    }
    
    @Test
    public void testSearch5() {
        List<Integer> userIdList = new ArrayList<Integer>();
        PagedQueryResult<Record> result = recDAO.search(null, null, userIdList);
        
        Assert.assertEquals(0, result.getCount());
    }
}
