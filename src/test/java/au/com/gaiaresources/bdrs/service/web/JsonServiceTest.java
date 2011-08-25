package au.com.gaiaresources.bdrs.service.web;

import java.math.BigDecimal;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import junit.framework.Assert;

import net.sf.json.JSONObject;

import org.junit.Before;
import org.junit.Test;

import au.com.gaiaresources.bdrs.model.record.AccessControlledRecordAdapter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

public class JsonServiceTest {

    private JsonService jsonService;
    private Record record;
    private User owner;
    private User nonOwner;
    private User admin;

    @Before
    public void setup() {
        jsonService = new JsonService();
        
        Survey survey = new Survey();
        survey.setName("survey name");
        survey.setDescription("survey desc");
        
        Attribute sattr1 = new Attribute();
        sattr1.setName("sattr1");
        sattr1.setDescription("sattr1 desc");
        sattr1.setTypeCode(AttributeType.STRING.getCode());
        
        Attribute sattr2 = new Attribute();
        sattr2.setName("sattr2");
        sattr2.setDescription("sattr2 desc");
        sattr2.setTypeCode(AttributeType.INTEGER.getCode());
        
        List<Attribute> sattrList = new LinkedList<Attribute>();
        sattrList.add(sattr1);
        sattrList.add(sattr2);
        survey.setAttributes(sattrList);
        
        owner = createUser(1000, "owner", new String[] { Role.USER });
        nonOwner = createUser(1001, "nonOwner", new String[] { Role.USER });
        admin = createUser(1002, "admin", new String[] { Role.ADMIN });
                
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 8, 5, 13, 30);
        
        record = new Record();
        record.setSurvey(survey);
        record.setUser(owner);
        record.setWhen(cal.getTime());
        record.setAccuracyInMeters(10d);
        record.setNotes("hello world");
        
        TaxonGroup group = new TaxonGroup();
        group.setName("taxonname");
        IndicatorSpecies species = new IndicatorSpecies();
        species.setTaxonGroup(group);
        species.setScientificName("indicatus specius");
        species.setCommonName("a species");
        species.setAuthor("old greg");
        
        record.setSpecies(species);
        record.setNumber(10);
        
        AttributeValue av1 = new AttributeValue();
        av1.setAttribute(sattr1);
        av1.setStringValue("hello world");
        
        AttributeValue av2 = new AttributeValue();
        av2.setAttribute(sattr2);
        av2.setNumericValue(new BigDecimal(4));
        
        Set<AttributeValue> avSet = new HashSet<AttributeValue>();
        avSet.add(av1);
        avSet.add(av2);
        record.setAttributes(avSet);
    }
    
    private User createUser(int id, String name, String[] roles) {
        User u = new User();
        u.setId(id);
        u.setName(name);
        u.setEmailAddress(name + "@" + name + ".com");
        u.setFirstName(name + "first");
        u.setLastName(name + "last");
        u.setRoles(roles);
        return u;
    }
    
    private void testRecordToJson_showAll(User accessor) {
        AccessControlledRecordAdapter recAdapter = new AccessControlledRecordAdapter(record, accessor);
        JSONObject obj = jsonService.toJson(recAdapter);
        
        Assert.assertEquals("indicatus specius", obj.getString(JsonService.RECORD_KEY_SPECIES));
        Assert.assertEquals(owner.getFirstName() + " " + owner.getLastName(), obj.getString(JsonService.RECORD_KEY_USER));
        Assert.assertEquals(owner.getId().intValue(), obj.getInt(JsonService.RECORD_KEY_USER_ID));
        
        Assert.assertTrue(obj.containsKey(JsonService.RECORD_KEY_SPECIES));
        Assert.assertTrue(obj.containsKey(JsonService.RECORD_KEY_BEHAVIOUR));
        Assert.assertTrue(obj.containsKey(JsonService.RECORD_KEY_COMMON_NAME));
        Assert.assertTrue(obj.containsKey(JsonService.RECORD_KEY_HABITAT));
        Assert.assertTrue(obj.containsKey(JsonService.RECORD_KEY_NOTES));
        Assert.assertTrue(obj.containsKey(JsonService.RECORD_KEY_NUMBER));
    }
    
    @Test
    public void testRecordToJson_asOwner() {
        testRecordToJson_showAll(owner);
    }
    
    @Test
    public void testRecordToJson_asAdmin() {
        testRecordToJson_showAll(admin);
    }
    
    @Test
    public void testRecordToJson_asNonOwner() {
        AccessControlledRecordAdapter recAdapter = new AccessControlledRecordAdapter(record, nonOwner);
        JSONObject obj = jsonService.toJson(recAdapter);
        
        Assert.assertEquals(owner.getFirstName() + " " + owner.getLastName(), obj.getString(JsonService.RECORD_KEY_USER));
        Assert.assertEquals(owner.getId().intValue(), obj.getInt(JsonService.RECORD_KEY_USER_ID));
        
        Assert.assertFalse(obj.containsKey(JsonService.RECORD_KEY_SPECIES));
        Assert.assertFalse(obj.containsKey(JsonService.RECORD_KEY_BEHAVIOUR));
        Assert.assertFalse(obj.containsKey(JsonService.RECORD_KEY_COMMON_NAME));
        Assert.assertFalse(obj.containsKey(JsonService.RECORD_KEY_HABITAT));
        Assert.assertFalse(obj.containsKey(JsonService.RECORD_KEY_NOTES));
        Assert.assertFalse(obj.containsKey(JsonService.RECORD_KEY_NUMBER));
    }
}
