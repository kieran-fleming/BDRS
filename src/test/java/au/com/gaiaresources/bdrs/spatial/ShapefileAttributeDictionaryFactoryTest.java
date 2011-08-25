package au.com.gaiaresources.bdrs.spatial;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;

public class ShapefileAttributeDictionaryFactoryTest {

    @Test
    public void testNamingCollisionResolving1() {
        ShapefileAttributeDictionaryFactory fact = new ShapefileAttributeDictionaryFactory();
        
        Survey survey = new Survey();
        survey.setName("my survey");
        survey.setDescription("my survey description woooo");
        
        CensusMethod cm = new CensusMethod();
        cm.setName("cm name");
        cm.setDescription("cm description");
        cm.setTaxonomic(Taxonomic.TAXONOMIC);
               
        List<Attribute> surveyAttrList = new ArrayList<Attribute>();
        List<Attribute> cmAttrList = new ArrayList<Attribute>();
        
        Attribute a1 = createAttribute(surveyAttrList, "attributename");       
        Attribute a4 = createAttribute(surveyAttrList, "1attributename");       
        Attribute a5 = createAttribute(surveyAttrList, "1attributenameasd");
        
        Attribute a2 = createAttribute(cmAttrList, "attributename");
        Attribute a3 = createAttribute(cmAttrList, "attributename");
        
        survey.setAttributes(surveyAttrList);
        cm.setAttributes(cmAttrList);
        
        Map<Attribute, String> nameMap = fact.createNameKeyDictionary(survey, null, cm);
        
        Assert.assertEquals("attributen", nameMap.get(a1));
        Assert.assertEquals("attribute1", nameMap.get(a2));
        Assert.assertEquals("attribute2", nameMap.get(a3));
        Assert.assertEquals("_1attribut", nameMap.get(a4));
        Assert.assertEquals("_1attribu1", nameMap.get(a5));
    }
    
    @Test
    public void testNameCollisionResolvingShortNames() {
        ShapefileAttributeDictionaryFactory fact = new ShapefileAttributeDictionaryFactory();
        
        Survey survey = new Survey();
        survey.setName("my survey");
        survey.setDescription("my survey description woooo");
        List<Attribute> surveyAttrList = new ArrayList<Attribute>();
        
        Attribute a1 = createAttribute(surveyAttrList, "a");      
        Attribute a2 = createAttribute(surveyAttrList, "a");
        Attribute a3 = createAttribute(surveyAttrList, "a");
        Attribute a4 = createAttribute(surveyAttrList, "a");
        Attribute a5 = createAttribute(surveyAttrList, "a");
        Attribute a6 = createAttribute(surveyAttrList, "a");
        Attribute a7 = createAttribute(surveyAttrList, "a");
        Attribute a8 = createAttribute(surveyAttrList, "a");
        Attribute a9 = createAttribute(surveyAttrList, "a");
        Attribute a10 = createAttribute(surveyAttrList, "a");
        Attribute a11 = createAttribute(surveyAttrList, "a");
        
        survey.setAttributes(surveyAttrList);
        
        Map<Attribute, String> nameMap = fact.createNameKeyDictionary(survey, null, null);
        
        Assert.assertEquals("a", nameMap.get(a1));
        Assert.assertEquals("a1", nameMap.get(a2));
        Assert.assertEquals("a2", nameMap.get(a3));
        Assert.assertEquals("a3", nameMap.get(a4));
        Assert.assertEquals("a4", nameMap.get(a5));
        Assert.assertEquals("a5", nameMap.get(a6));
        Assert.assertEquals("a6", nameMap.get(a7));
        Assert.assertEquals("a7", nameMap.get(a8));
        Assert.assertEquals("a8", nameMap.get(a9));
        Assert.assertEquals("a9", nameMap.get(a10));
        Assert.assertEquals("a10", nameMap.get(a11));
    }
    
    @Test
    public void testNameCollisionResolvingDarwinCore() {
        ShapefileAttributeDictionaryFactory fact = new ShapefileAttributeDictionaryFactory();
        ShapefileRecordKeyLookup klu = new ShapefileRecordKeyLookup();
        
        Survey survey = new Survey();
        survey.setName("my survey");
        survey.setDescription("my survey description woooo");
        List<Attribute> surveyAttrList = new ArrayList<Attribute>();
        
        Attribute a1 = createAttribute(surveyAttrList, klu.getSpeciesIdKey());      
        Attribute a2 = createAttribute(surveyAttrList, klu.getSpeciesNameKey());
        Attribute a3 = createAttribute(surveyAttrList, klu.getIndividualCountKey());
        Attribute a4 = createAttribute(surveyAttrList, klu.getDateKey());
        Attribute a5 = createAttribute(surveyAttrList, klu.getTimeKey());
        Attribute a6 = createAttribute(surveyAttrList, klu.getNotesKey());
        
        survey.setAttributes(surveyAttrList);
        
        Map<Attribute, String> nameMap = fact.createNameKeyDictionary(survey, null, null);
        
        Assert.assertEquals("species_i1", nameMap.get(a1));
        Assert.assertEquals("species_n1", nameMap.get(a2));
        Assert.assertEquals(klu.getIndividualCountKey() + "1", nameMap.get(a3));
        Assert.assertEquals(klu.getDateKey() + "1", nameMap.get(a4));
        Assert.assertEquals(klu.getTimeKey() + "1", nameMap.get(a5));
        Assert.assertEquals(klu.getNotesKey() + "1", nameMap.get(a6));
    }
    
    private Attribute createAttribute(List<Attribute> list, String name) {
        Attribute a = createAttribute(name);
        list.add(a);
        return a;
    }
    
    private Attribute createAttribute(String name) {
        Attribute a2 = new Attribute();
        a2.setName(name);
        a2.setDescription("attrdescription");
        a2.setTypeCode(AttributeType.STRING.getCode());
        return a2;
    }
}
