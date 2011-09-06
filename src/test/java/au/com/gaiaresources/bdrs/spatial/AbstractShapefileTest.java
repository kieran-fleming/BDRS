package au.com.gaiaresources.bdrs.spatial;

import java.io.IOException;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.opengis.feature.Property;
import org.opengis.feature.simple.SimpleFeature;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

import com.vividsolutions.jts.geom.Geometry;

// has to extend abstract controller test as sometimes we will be using it in a controller
// context, sometimes we wont. the performance hit is minimal.
public abstract class AbstractShapefileTest extends AbstractControllerTest {
    @Autowired
    protected SurveyDAO surveyDAO;
    @Autowired
    protected TaxaDAO taxaDAO;
    @Autowired
    protected RecordDAO recordDAO;
    @Autowired
    protected UserDAO userDAO;
    @Autowired
    protected CensusMethodDAO cmDAO;
    @Autowired
    protected LocationService locService;
    
    protected User owner;
    protected User admin;
    protected User nonOwner;
    
    protected Survey survey;
    protected Survey secondSurvey;
    
    protected IndicatorSpecies species;
    protected IndicatorSpecies speciesNotInSurvey;
    protected TaxonGroup taxonGroup;
    protected CensusMethod cm;
    protected CensusMethod taxaCm;
    
    protected Date surveyStartDate;
    
    protected Record r1;
    protected Record r2;
    protected Record r3;
    protected Record r4;
    
    protected GeometryBuilder geomBuilder;
    
    protected RecordKeyLookup klu = new ShapefileRecordKeyLookup();
    
    protected SimpleDateFormat shpDateFormat = new SimpleDateFormat("dd MMM yyyy");
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() throws ParseException {
        
        owner = userDAO.createUser("owner", "ownerfirst", "ownerlast", "owner@owner.com", "password", "regkey", Role.USER);
        nonOwner = userDAO.createUser("nonOwner", "nonOwnerFirst", "nonOwnerLast", "nonowner@nonowner.com", "password", "regkey", Role.USER);
        admin = userDAO.getUser("admin");
        
        geomBuilder = new GeometryBuilder();
        
        taxonGroup = new TaxonGroup();
        taxonGroup.setRunThreshold(false);
        taxonGroup.setName("my taxon group");
        
        species = new IndicatorSpecies();
        species.setRunThreshold(false);
        
        // for case insensitive name searching
        species.setScientificName("wOotuS Maxus");
        species.setCommonName("red cup");
        species.setTaxonGroup(taxonGroup);
        
        speciesNotInSurvey = new IndicatorSpecies();
        speciesNotInSurvey.setRunThreshold(false);
        speciesNotInSurvey.setScientificName("notus surveyus");
        speciesNotInSurvey.setCommonName("ixnay on the survey");
        speciesNotInSurvey.setTaxonGroup(taxonGroup);
        
        taxaDAO.save(taxonGroup);
        taxaDAO.save(species);
        taxaDAO.save(speciesNotInSurvey);
        
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 6, 20, 14, 30, 00);
        
        survey = new Survey();
        survey.setName("my survey");
        survey.setDescription("my survey description woooo");
        
        Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
        speciesSet.add(species);
        
        survey.setSpecies(speciesSet);
        
        taxaCm = new CensusMethod();
        taxaCm.setName("taxa cm");
        taxaCm.setDescription("taxa cm desc");
        taxaCm.setTaxonomic(Taxonomic.TAXONOMIC);
        List<Attribute> taxaCmList = new LinkedList<Attribute>();
        taxaCmList.add(createAttribute("cattr_0", AttributeType.INTEGER, true));
        taxaCmList.add(createAttribute("cattr_1", AttributeType.INTEGER_WITH_RANGE, true, new String[] { "5", "10" } ));
        taxaCmList.add(createAttribute("cattr_2", AttributeType.DECIMAL, true));
        taxaCmList.add(createAttribute("cattr_3", AttributeType.BARCODE, true));
        taxaCmList.add(createAttribute("cattr_4", AttributeType.DATE, true));
        taxaCmList.add(createAttribute("cattr_5", AttributeType.TIME, true));
        taxaCmList.add(createAttribute("cattr_6", AttributeType.STRING, true));
        taxaCmList.add(createAttribute("cattr_7", AttributeType.STRING_AUTOCOMPLETE, true));
        taxaCmList.add(createAttribute("cattr_8", AttributeType.TEXT, true));
        taxaCmList.add(createAttribute("cattr_9", AttributeType.STRING_WITH_VALID_VALUES, true, new String[] { "hello", "world", "goodbye"} ));
        taxaCm.setAttributes(taxaCmList);
        cmDAO.save(taxaCm);
        
        cm = new CensusMethod();
        cm.setName("census method name");
        cm.setDescription("census method description");
        cm.setTaxonomic(Taxonomic.NONTAXONOMIC);
        
        List<Attribute> cmAttrList = new LinkedList<Attribute>();
        cmAttrList.add(createAttribute("cattr_0", AttributeType.INTEGER, true));
        cmAttrList.add(createAttribute("cattr_1", AttributeType.INTEGER_WITH_RANGE, true, new String[] { "5", "10" } ));
        cmAttrList.add(createAttribute("cattr_2", AttributeType.DECIMAL, true));
        cmAttrList.add(createAttribute("cattr_3", AttributeType.BARCODE, true));
        cmAttrList.add(createAttribute("cattr_4", AttributeType.DATE, true));
        cmAttrList.add(createAttribute("cattr_5", AttributeType.TIME, true));
        cmAttrList.add(createAttribute("cattr_6", AttributeType.STRING, true));
        cmAttrList.add(createAttribute("cattr_7", AttributeType.STRING_AUTOCOMPLETE, true));
        cmAttrList.add(createAttribute("cattr_8", AttributeType.TEXT, true));
        cmAttrList.add(createAttribute("cattr_9", AttributeType.STRING_WITH_VALID_VALUES, true, new String[] { "hello", "world", "goodbye"} ));
        cm.setAttributes(cmAttrList);
        cmDAO.save(cm);
        
        survey.getCensusMethods().add(cm);
        survey.getCensusMethods().add(taxaCm);
        
        List<Attribute> surveyAttrList = new LinkedList<Attribute>();
        survey.setRunThreshold(false);
        surveyAttrList.add(createAttribute("sattr_0", AttributeType.INTEGER, true));
        surveyAttrList.add(createAttribute("sattr_1", AttributeType.INTEGER_WITH_RANGE, true, new String[] { "5", "10" } ));
        surveyAttrList.add(createAttribute("sattr_2", AttributeType.DECIMAL, true));
        surveyAttrList.add(createAttribute("sattr_3", AttributeType.BARCODE, true));
        surveyAttrList.add(createAttribute("sattr_4", AttributeType.DATE, true));
        surveyAttrList.add(createAttribute("sattr_5", AttributeType.TIME, true));
        surveyAttrList.add(createAttribute("sattr_6", AttributeType.STRING, true));
        surveyAttrList.add(createAttribute("sattr_7", AttributeType.STRING_AUTOCOMPLETE, true));
        surveyAttrList.add(createAttribute("sattr_8", AttributeType.TEXT, true));
        surveyAttrList.add(createAttribute("sattr_9", AttributeType.STRING_WITH_VALID_VALUES, true, new String[] { "hello", "world", "goodbye"} ));
        survey.setAttributes(surveyAttrList);
        
        surveyStartDate = cal.getTime();
        
        survey.setStartDate(surveyStartDate);
        surveyDAO.save(survey);
        
        secondSurvey = new Survey();
        secondSurvey.setName("second survey");
        secondSurvey.setDescription("description of second survey");
        List<Attribute> surveyAttrList2 = new LinkedList<Attribute>();
        secondSurvey.setRunThreshold(false);
        surveyAttrList2.add(createAttribute("sattr_0", AttributeType.INTEGER, true));
        surveyAttrList2.add(createAttribute("sattr_1", AttributeType.INTEGER_WITH_RANGE, true, new String[] { "5", "10" } ));
        surveyAttrList2.add(createAttribute("sattr_2", AttributeType.DECIMAL, true));
        surveyAttrList2.add(createAttribute("sattr_3", AttributeType.BARCODE, true));
        surveyAttrList2.add(createAttribute("sattr_4", AttributeType.DATE, true));
        surveyAttrList2.add(createAttribute("sattr_5", AttributeType.TIME, true));
        surveyAttrList2.add(createAttribute("sattr_6", AttributeType.STRING, true));
        surveyAttrList2.add(createAttribute("sattr_7", AttributeType.STRING_AUTOCOMPLETE, true));
        surveyAttrList2.add(createAttribute("sattr_8", AttributeType.TEXT, true));
        surveyAttrList2.add(createAttribute("sattr_9", AttributeType.STRING_WITH_VALID_VALUES, true, new String[] { "hello", "world", "goodbye"} ));
        secondSurvey.setAttributes(surveyAttrList2);
        
        surveyStartDate = cal.getTime();
        
        secondSurvey.setStartDate(surveyStartDate);
        surveyDAO.save(secondSurvey);
        
        r1 = createRecord(survey, null, locService.convertToMultiGeom(geomBuilder.createRectangle(0, 0, 10, 10)), species, cal.getTime(), owner, 9001, null, 1d, RecordVisibility.CONTROLLED);
        r2 = createRecord(survey, taxaCm, locService.convertToMultiGeom(geomBuilder.createPoint(20, 20)), species, cal.getTime(), owner, 9002, null, 2d, RecordVisibility.CONTROLLED);
        r3 = createRecord(secondSurvey, null, locService.convertToMultiGeom(geomBuilder.createLine(-10, -10, 0, 0)), null, cal.getTime(), owner, null, null, null, RecordVisibility.CONTROLLED);        
        r4 = createRecord(survey, cm, locService.convertToMultiGeom(geomBuilder.createPoint(15, 15)), null, cal.getTime(), owner, null, null, null, RecordVisibility.CONTROLLED);
    }
    
    protected Record createRecord(Survey survey, CensusMethod cm, Geometry geom, IndicatorSpecies species, 
            Date when, User user, Integer number, String notes, Double accuracy, RecordVisibility recVis) throws ParseException {
        Record record = new Record();
        
        if (survey == null) {
            throw new IllegalArgumentException("survey cannot be null");
        }
        
        Set<AttributeValue> avSet = new HashSet<AttributeValue>();
        // lets add some records to the surveys!
        record.setAccuracyInMeters(1d);
        record.setBehaviour("jumping");
        record.setSurvey(survey);
        record.setCensusMethod(taxaCm);
        record.setSpecies(species);
        record.setGeometry(geom);
        record.setWhen(when);
        record.setLastDate(when);
        record.setRecordVisibility(recVis);
        record.setUser(owner);
        record.setCensusMethod(cm);
        record.setNotes(notes);
        record.setNumber(number);
        
        List<Attribute> surveyAttrList = survey.getAttributes();
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_0"), "1"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_1"), "6"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_2"), "5.5"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_3"), "barcode"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_4"), "16 Aug 2011"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_5"), "08:27"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_6"), "my string value"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_7"), "autocomplete"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_8"), "text"));
        avSet.add(createAttributeValue(getAttributeByName(surveyAttrList, "sattr_9"), "string with valid values"));
    
        if (cm != null) {
            List<Attribute> cmAttrList = cm.getAttributes();
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_0"), "1"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_1"), "6"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_2"), "5.5"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_3"), "barcode"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_4"), "16 Aug 2011"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_5"), "08:27"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_6"), "my string value"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_7"), "autocomplete"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_8"), "text"));
            avSet.add(createAttributeValue(getAttributeByName(cmAttrList, "cattr_9"), "string with valid values"));
        }
        record.setAttributes(avSet);
        
        return recordDAO.saveRecord(record);
    }
    
    protected void assertRecord(Record record, ShapeFileReader reader, User accessor) throws IOException {
        
        boolean hideDetails = record.hideDetails(accessor);
        
        Map<Integer, String> attrIdNameMap = reader.getAttributeIdNameMap();
        
        SimpleFeature feature = getFeatureByRecordId(reader.getFeatureIterator(), record.getId());
        Assert.assertNotNull("feature should not be null for rec id = " + record.getId().toString(), feature);
        
        if (!hideDetails) {
            // assert everything is there...
            assertFeatureIntegerValue(feature, klu.getRecordIdKey(), record.getId());
            
            assertFeatureStringValue(feature, ShapeFileWriter.KEY_RECORD_OWNER, record.getUser().getFirstName() + " " + record.getUser().getLastName());
            
            if (record.getNumber() != null) {
                assertFeatureIntegerValue(feature, klu.getIndividualCountKey(), record.getNumber());
            }
            
            assertFeatureStringValue(feature, klu.getDateKey(), shpDateFormat.format(record.getWhen()));
            
            if (record.getSpecies() != null) {
                assertFeatureStringValue(feature, klu.getSpeciesNameKey(), record.getSpecies().getScientificName());
            }
            
            assertFeatureStringValue(feature, klu.getNotesKey(), record.getNotes());
            assertFeatureDoubleValue(feature, klu.getAccuracyKey(), record.getAccuracyInMeters());
            
            for (AttributeValue av : record.getAttributes()) {
                assertAttributeValue(av, feature, attrIdNameMap);
            }
        } else {
            // you'll note that fields we would expect to be 'null' are 0 for numeric types.
            // this is due to SHP format not supporting null fields. numeric types default to 0
            // when no data is entered.
            
            // only a few items should be shown if the record has hidden details...
            assertFeatureIntegerValue(feature, klu.getRecordIdKey(), record.getId());
            assertFeatureStringValue(feature, ShapeFileWriter.KEY_RECORD_OWNER, record.getUser().getFirstName() + " " + record.getUser().getLastName());
            assertFeatureStringValue(feature, klu.getDateKey(), shpDateFormat.format(record.getWhen()));
            assertFeatureStringValue(feature, klu.getSpeciesNameKey(), null);
            assertFeatureIntegerValue(feature, klu.getIndividualCountKey(), 0);
            assertFeatureStringValue(feature, klu.getNotesKey(), null);
            assertFeatureDoubleValue(feature, klu.getAccuracyKey(), 0d);
            
            for (AttributeValue av : record.getAttributes()) {
                assertAttributeValueEmpty(av, feature, attrIdNameMap);
            }
        }
    }
    
    // yes i know this is awful but i can't be bothered working out the ShapefileDataStore
    // querying API. Feel free to change the implementation
    protected SimpleFeature getFeatureByRecordId(Iterator<SimpleFeature> iter, int recordId) throws IOException {

        SimpleFeature result = null;
        
        while (iter.hasNext()) {
            
            SimpleFeature feature = iter.next();
            // continue iterating until end of list. This unlocks the underlying datastore object.
            if (result != null) {
                continue;
            } else {
                Property prop = feature.getProperty(klu.getRecordIdKey());
                Assert.assertNotNull(prop);
                Assert.assertTrue("value must be an instance of integer", prop.getValue() instanceof Integer);
                Integer value = (Integer)prop.getValue();
                if (value.intValue() == recordId) {
                    result = feature;
                }    
            }
        }        
        return result;
    }
    
    protected void assertFeatureCount(Iterator<SimpleFeature> iter, int expectedCount) throws IOException {
        int count = 0;
        while (iter.hasNext()) {
            iter.next();
            ++count;
        }
        Assert.assertEquals("wrong number of features in shapefile", expectedCount, count);
    }
    
    protected void assertFeatureIntegerValue(SimpleFeature feature, String name, Integer expectedValue) {
        Property prop = feature.getProperty(name);
        Assert.assertNotNull(prop);
        Assert.assertTrue("value must be an instance of integer", prop.getValue() instanceof Integer);
        Integer value = (Integer)prop.getValue();
        
        // because shapefiles don't have null fields, when the expected string value is null we need
        // to relax our assertion so...
        if (expectedValue == null) {
            expectedValue = 0;
        }
        
        Assert.assertEquals("expected value not found for " + name, expectedValue, value);
    }
    
    protected void assertFeatureDoubleValue(SimpleFeature feature, String name, Double expectedValue) {
        Property prop = feature.getProperty(name);
        Assert.assertNotNull(prop);
        Assert.assertTrue("value must be an instance of integer", prop.getValue() instanceof Double);
        Double value = (Double)prop.getValue();
        Assert.assertEquals("expected value not found for " + name, expectedValue, value);
    }
    
    protected void assertFeatureStringValue(SimpleFeature feature, String name, String expectedValue) {
        Property prop = feature.getProperty(name);
        Assert.assertNotNull("property is null. name = " + name, prop);
        Assert.assertTrue("value must be an instance of string. name = " + name, prop.getValue() instanceof String);
        String value = (String)prop.getValue();
        
        // because shapefiles don't have null fields, when the expected string value is null we need
        // to relax our assertion so...
        value = value.trim();
        if (expectedValue == null) {
            expectedValue = "";
        }
        
        Assert.assertEquals("expected value not found for " + name, expectedValue, value);
    }
    
    protected Attribute createAttribute(String name, AttributeType type, boolean required) {
        return createAttribute(name, type, required, null);
    }
    
    protected Attribute createAttribute(String name, AttributeType type, boolean required, String[] args) {
        Attribute a = new Attribute();
        a.setRunThreshold(false);
        
        if (args != null) {
            List<AttributeOption> options = new ArrayList<AttributeOption>();
            for (String s : args) {
                AttributeOption attrOpt = new AttributeOption();
                attrOpt.setRunThreshold(false);
                attrOpt.setValue(s);
                options.add(attrOpt);
                taxaDAO.save(attrOpt);
            }
            a.setOptions(options);
        }
        
        a.setName(name);
        a.setDescription(name + " desc");
        a.setRequired(required);
        a.setTag(false);
        a.setTypeCode(type.getCode());
        taxaDAO.save(a);
        return a;
    }
    
    protected AttributeValue createAttributeValue(Attribute a, String value) throws ParseException {
        AttributeValue av = new AttributeValue();
        av.setAttribute(a);
        switch (a.getType()) {
        case INTEGER:
        case INTEGER_WITH_RANGE:
        case DECIMAL:
            av.setNumericValue(new BigDecimal(Double.valueOf(value)));
            break;
        
        case DATE:    
            av.setDateValue(shpDateFormat.parse(value));
            break;
            
        case BARCODE:
        case TIME:
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case HTML:
        case HTML_COMMENT:
        case HTML_HORIZONTAL_RULE:
        case STRING_WITH_VALID_VALUES:
        case MULTI_CHECKBOX:
        case MULTI_SELECT:
            av.setStringValue(value);
            break;
            
        case SINGLE_CHECKBOX:
            av.setBooleanValue(value);
            break;
            
        case IMAGE:
        case FILE:
            // ignored
            break;
        }
        
        recordDAO.saveAttributeValue(av);
        
        return av;
    }
    
    protected Attribute getAttributeByName(List<Attribute> attrList, String name) {
        for (Attribute a : attrList) {
            if (name.equals(a.getName())) {
                return a;
            }
        }
        return null;
    }

    protected void assertAttributeValue(AttributeValue av, SimpleFeature feature, Map<Integer, String> attrIdNameMap) {
        Attribute a = av.getAttribute();
        
        String shpAttrName = attrIdNameMap.get(a.getId());
        
        switch (a.getType()) {
        case INTEGER:
        case INTEGER_WITH_RANGE:
            if (av.getNumericValue() == null) {
                assertFeatureIntegerValue(feature, shpAttrName, null);
            } else {
                assertFeatureIntegerValue(feature, shpAttrName, av.getNumericValue().intValue());    
            }
            break;
        case DECIMAL:
            if (av.getNumericValue() == null) {
                assertFeatureDoubleValue(feature, shpAttrName, null);
            } else {
                assertFeatureDoubleValue(feature, shpAttrName, av.getNumericValue().doubleValue());                
            }
            break;
        
        case DATE:    
        {
            if (av.getDateValue() == null) {
                assertFeatureStringValue(feature, shpAttrName, null);
            } else {
                assertFeatureStringValue(feature, shpAttrName, shpDateFormat.format(av.getDateValue()));
            }
        }
            break;
            
        case BARCODE:
        case TIME:
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case HTML:
        case HTML_COMMENT:
        case HTML_HORIZONTAL_RULE:
        case STRING_WITH_VALID_VALUES:
        case MULTI_CHECKBOX:
        case MULTI_SELECT:
            assertFeatureStringValue(feature, shpAttrName, av.getStringValue());
            break;
            
        case SINGLE_CHECKBOX:
        {
            if (av.getBooleanValue() == null) {
                assertFeatureStringValue(feature, shpAttrName, null);
            } else {
                assertFeatureStringValue(feature, shpAttrName, av.getBooleanValue().toString()); 
            }
        }
            break;
            
        case IMAGE:
        case FILE:
            Assert.fail("Cannot properly assert this attribute type : " + a.getTypeCode());
            // ignored
            break;
        }
    }
    
    protected void assertAttributeValueEmpty(AttributeValue av, SimpleFeature feature, Map<Integer, String> attrIdNameMap) {
        Attribute a = av.getAttribute();
        
        String shpAttrName = attrIdNameMap.get(a.getId());
        
        switch (a.getType()) {
        case INTEGER:
        case INTEGER_WITH_RANGE:
            assertFeatureIntegerValue(feature, shpAttrName, 0);    
            break;
        case DECIMAL:
            assertFeatureDoubleValue(feature, shpAttrName, 0d);                
            break;
        
        case DATE:    
            assertFeatureStringValue(feature, shpAttrName, "");
            break;
            
        case BARCODE:
        case TIME:
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case HTML:
        case HTML_COMMENT:
        case HTML_HORIZONTAL_RULE:
        case STRING_WITH_VALID_VALUES:
        case MULTI_CHECKBOX:
        case MULTI_SELECT:
            assertFeatureStringValue(feature, shpAttrName, "");
            break;
            
        case SINGLE_CHECKBOX:
            assertFeatureStringValue(feature, shpAttrName, ""); 
            break;
            
        case IMAGE:
        case FILE:
            Assert.fail("Cannot properly assert this attribute type : " + a.getTypeCode());
            // ignored
            break;
        }
    }
}
