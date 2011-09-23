package au.com.gaiaresources.bdrs.spatial;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.SchemaException;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.attribute.AttributeDictionaryFactory;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializer;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializerResult;
import au.com.gaiaresources.bdrs.deserialization.record.RecordEntry;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
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
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class ShapefileToRecordTest extends AbstractTransactionalTest {

    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    TaxaDAO taxaDAO;
    @Autowired
    RecordDAO recordDAO;
    @Autowired
    UserDAO userDAO;
    @Autowired
    CensusMethodDAO cmDAO;
    
    User currentUser;
    Survey survey;
    IndicatorSpecies species;
    IndicatorSpecies speciesNotInSurvey;
    TaxonGroup taxonGroup;
    CensusMethod cm;
    CensusMethod taxaCm;
    
    Date surveyStartDate;
    
    private SimpleDateFormat shpDateFormat = new SimpleDateFormat("dd MMM yyyy");
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() {
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
        
        currentUser = userDAO.getUser("admin");
    }
    
    @Test
    public void testShapefileWithSurveyAttributes_Point() throws Exception {
        testShapefileWithSurveyAttributes(ShapefileType.POINT);
    }
    
    @Test
    public void testShapefileWithSurveyAttributes_MultiPolygon() throws Exception {
        testShapefileWithSurveyAttributes(ShapefileType.MULTI_POLYGON);
    }
    
    private void testShapefileWithSurveyAttributes(ShapefileType shpType) throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 6, 22, 14, 30, 00);
        Date recordWhen = cal.getTime();
        
        cal.clear();
        cal.set(2011, 6, 22, 00, 00, 00);
        Date dateOnly = cal.getTime();
        
        cal.clear();
        cal.set(2000, 1, 1);
        Date sattrDate = cal.getTime();
        
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        ShapefileToRecordEntryTransformer transformer = new ShapefileToRecordEntryTransformer(klu);
        
        // create the template:
        ShapeFileWriter writer = new ShapeFileWriter();
        File file = writer.createZipShapefile(survey, null, shpType);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertEquals(survey.getId(), reader.getSurveyIdList().get(0));
        
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(0, reader.getCensusMethodIdList().get(0).intValue());
        
        // manipulate the template:
        GeometryBuilder gb = new GeometryBuilder();
        ShapefileDataStore ds = reader.getDataStore();
        
        List<ShapefileFeature> featureList = new LinkedList<ShapefileFeature>();
        
        {
            Map<String, Object> featureAttr = new HashMap<String, Object>();
            featureAttr.put(klu.getNotesKey(), "comments");
            featureAttr.put(klu.getDateKey(), shpDateFormat.format(dateOnly));
            featureAttr.put(klu.getSpeciesNameKey(), "wootus maxus");
            featureAttr.put(klu.getTimeKey(), "14:30");
            featureAttr.put("sattr_0", 100);
            featureAttr.put("sattr_1", 6);
            featureAttr.put("sattr_2", 6.7d);
            featureAttr.put("sattr_3", "barcode1234");
            featureAttr.put("sattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("sattr_5", "13:33");
            featureAttr.put("sattr_6", "6");
            featureAttr.put("sattr_9", "9");
            featureAttr.put("sattr_7", "7");
            featureAttr.put("sattr_8", "8");
            
            Geometry geomToWrite = gb.createPoint(10, 5);
            
            switch (shpType) {
            case POINT:
                geomToWrite = gb.createPoint(10, 5);
                break;
            case MULTI_POLYGON:
                geomToWrite = gb.createSquare(0, 0, 10);
                break;
                default:
                    Assert.fail("unhandled shapefile type");
            }
            Assert.assertNotNull(geomToWrite);
            
            featureList.add(new ShapefileFeature(geomToWrite, featureAttr));
        }
        
        writeFeatures(ds, featureList);
        
        List<RecordEntry> entries = transformer.shapefileFeatureToRecordEntries(reader.getFeatureIterator(), reader.getSurveyIdList(), reader.getCensusMethodIdList());
        
        Assert.assertNotNull(entries);
        Assert.assertEquals(1, entries.size());
        
        AttributeDictionaryFactory adf = new ShapefileAttributeDictionaryFactory();
        AttributeParser parser = new ShapefileAttributeParser();
        RecordDeserializer rds = new RecordDeserializer(klu, adf, parser);
        List<RecordDeserializerResult> dsResult = rds.deserialize(currentUser, entries);
        
        Assert.assertNotNull(dsResult);
        Assert.assertEquals(1, dsResult.size());
        
        Assert.assertTrue("Should be an empty error map", dsResult.get(0).getErrorMap().isEmpty());
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        Record record = recordDAO.search(null, null, null).getList().get(0);
        
        Assert.assertNotNull("record should not be null", record);
        Assert.assertNotNull("record species should not be null", record.getSpecies());
        Assert.assertEquals(species.getScientificName(), record.getSpecies().getScientificName());
        
        Assert.assertEquals(recordWhen, record.getWhen());
        
        Geometry geom = record.getGeometry();
        
        Assert.assertEquals(4326, geom.getSRID());
        
        switch (shpType) {
        case POINT:
            Assert.assertTrue(geom instanceof Point);
            Point point = (Point)geom;
            Assert.assertEquals(10d, point.getX());
            Assert.assertEquals(5d, point.getY());
            break;
        case MULTI_POLYGON:
            Assert.assertTrue(geom instanceof MultiPolygon);
            Assert.assertEquals(5d, geom.getCentroid().getX());
            Assert.assertEquals(5d, geom.getCentroid().getY());
            break;
            default:
                Assert.fail("unhandled shapefile type");
        }
        
        Set<AttributeValue> avList = record.getAttributes();
        
        assertAttributeValue(avList, "sattr_0", 100);
        assertAttributeValue(avList, "sattr_1", 6);
        assertAttributeValue(avList, "sattr_2", 6.7d);
        assertAttributeValue(avList, "sattr_3", "barcode1234");
        assertAttributeValue(avList, "sattr_4", sattrDate);
        assertAttributeValue(avList, "sattr_5", "13:33");
        assertAttributeValue(avList, "sattr_6", "6");
        assertAttributeValue(avList, "sattr_7", "7");
        assertAttributeValue(avList, "sattr_8", "8");
        assertAttributeValue(avList, "sattr_9", "9");
    }
    
    @Test
    public void testShapefileToRecordWithCensusMethodNonTaxonomic() throws Exception {
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 6, 22, 14, 30, 00);
        Date recordWhen = cal.getTime();
        
        cal.clear();
        cal.set(2011, 6, 22, 00, 00, 00);
        Date dateOnly = cal.getTime();
        
        cal.clear();
        cal.set(2000, 1, 1);
        Date sattrDate = cal.getTime();
        
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        ShapefileToRecordEntryTransformer transformer = new ShapefileToRecordEntryTransformer(klu);
        
        // create the template:
        ShapeFileWriter writer = new ShapeFileWriter();
        File file = writer.createZipShapefile(survey, cm, ShapefileType.POINT);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertEquals(survey.getId().intValue(), reader.getSurveyIdList().get(0).intValue());
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(cm.getId().intValue(), reader.getCensusMethodIdList().get(0).intValue());
        
        // manipulate the template:
        GeometryBuilder gb = new GeometryBuilder();
        ShapefileDataStore ds = reader.getDataStore();
        
        List<ShapefileFeature> featureList = new LinkedList<ShapefileFeature>();
        
        {
            Map<String, Object> featureAttr = new HashMap<String, Object>();
            featureAttr.put(klu.getNotesKey(), "comments");
            featureAttr.put(klu.getDateKey(), shpDateFormat.format(dateOnly));
            //featureAttr.put(klu.getSpeciesNameKey(), "wootus maxus");
            featureAttr.put(klu.getTimeKey(), "14:30");
            featureAttr.put("sattr_0", 100);
            featureAttr.put("sattr_1", 6);
            featureAttr.put("sattr_2", 6.7d);
            featureAttr.put("sattr_3", "barcode1234");
            featureAttr.put("sattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("sattr_5", "13:33");
            featureAttr.put("sattr_6", "6");
            featureAttr.put("sattr_9", "9");
            featureAttr.put("sattr_7", "7");
            featureAttr.put("sattr_8", "8");
            
            featureAttr.put("cattr_0", 100);
            featureAttr.put("cattr_1", 6);
            featureAttr.put("cattr_2", 6.7d);
            featureAttr.put("cattr_3", "barcode1234");
            featureAttr.put("cattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("cattr_5", "13:33");
            featureAttr.put("cattr_6", "6");
            featureAttr.put("cattr_9", "9");
            featureAttr.put("cattr_7", "7");
            featureAttr.put("cattr_8", "8");
            
            Point point = gb.createPoint(10, 5);
            featureList.add(new ShapefileFeature(point, featureAttr));
        }
        
        writeFeatures(ds, featureList);
        
        List<RecordEntry> entries = transformer.shapefileFeatureToRecordEntries(reader.getFeatureIterator(), reader.getSurveyIdList(), reader.getCensusMethodIdList());
        
        Assert.assertNotNull(entries);
        Assert.assertEquals(1, entries.size());
        
        AttributeDictionaryFactory adf = new ShapefileAttributeDictionaryFactory();
        AttributeParser parser = new ShapefileAttributeParser();
        RecordDeserializer rds = new RecordDeserializer(klu, adf, parser);
        List<RecordDeserializerResult> dsResult = rds.deserialize(currentUser, entries);
        
        Assert.assertNotNull(dsResult);
        Assert.assertEquals(1, dsResult.size());
        
        for (Entry<String, String> entry : dsResult.get(0).getErrorMap().entrySet()) {
            log.debug(entry.getKey() + " : " + entry.getValue());
        }
        
        Assert.assertTrue("Should be an empty error map", dsResult.get(0).getErrorMap().isEmpty());
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        Record record = recordDAO.search(null, null, null).getList().get(0);
        
        Assert.assertNotNull(record);
        Assert.assertNull(record.getSpecies());
       
        
        Assert.assertEquals(recordWhen, record.getWhen());
        
        Geometry geom = record.getGeometry();
        
        Assert.assertEquals(4326, geom.getSRID());
        
        Assert.assertTrue(geom instanceof Point);
        Point point = (Point)geom;
        Assert.assertEquals(10d, point.getX());
        Assert.assertEquals(5d, point.getY());
        
        Set<AttributeValue> avList = record.getAttributes();
        
        assertAttributeValue(avList, "sattr_0", 100);
        assertAttributeValue(avList, "sattr_1", 6);
        assertAttributeValue(avList, "sattr_2", 6.7d);
        assertAttributeValue(avList, "sattr_3", "barcode1234");
        assertAttributeValue(avList, "sattr_4", sattrDate);
        assertAttributeValue(avList, "sattr_5", "13:33");
        assertAttributeValue(avList, "sattr_6", "6");
        assertAttributeValue(avList, "sattr_7", "7");
        assertAttributeValue(avList, "sattr_8", "8");
        assertAttributeValue(avList, "sattr_9", "9");
        
        assertAttributeValue(avList, "cattr_0", 100);
        assertAttributeValue(avList, "cattr_1", 6);
        assertAttributeValue(avList, "cattr_2", 6.7d);
        assertAttributeValue(avList, "cattr_3", "barcode1234");
        assertAttributeValue(avList, "cattr_4", shpDateFormat.format(sattrDate));
        assertAttributeValue(avList, "cattr_5", "13:33");
        assertAttributeValue(avList, "cattr_6", "6");
        assertAttributeValue(avList, "cattr_7", "7");
        assertAttributeValue(avList, "cattr_8", "8");
        assertAttributeValue(avList, "cattr_9", "9");
    }
    
    @Test
    public void testShapefileToRecordWithCensusMethodTaxonomic() throws Exception {
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 6, 22, 14, 30, 00);
        Date recordWhen = cal.getTime();
        
        cal.clear();
        cal.set(2011, 6, 22, 00, 00, 00);
        Date dateOnly = cal.getTime();
        
        cal.clear();
        cal.set(2000, 1, 1);
        Date sattrDate = cal.getTime();
        
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        ShapefileToRecordEntryTransformer transformer = new ShapefileToRecordEntryTransformer(klu);
        
        // create the template:
        ShapeFileWriter writer = new ShapeFileWriter();
        File file = writer.createZipShapefile(survey, taxaCm, ShapefileType.POINT);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertEquals(survey.getId().intValue(), reader.getSurveyIdList().get(0).intValue());
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(taxaCm.getId().intValue(), reader.getCensusMethodIdList().get(0).intValue());
        
        // manipulate the template:
        GeometryBuilder gb = new GeometryBuilder();
        ShapefileDataStore ds = reader.getDataStore();
        
        List<ShapefileFeature> featureList = new LinkedList<ShapefileFeature>();
        
        {
            Map<String, Object> featureAttr = new HashMap<String, Object>();
            featureAttr.put(klu.getNotesKey(), "comments");
            featureAttr.put(klu.getDateKey(), shpDateFormat.format(dateOnly));
            featureAttr.put(klu.getSpeciesNameKey(), "wootus maxus");
            featureAttr.put(klu.getTimeKey(), "14:30");
            featureAttr.put("sattr_0", 100);
            featureAttr.put("sattr_1", 6);
            featureAttr.put("sattr_2", 6.7d);
            featureAttr.put("sattr_3", "barcode1234");
            featureAttr.put("sattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("sattr_5", "13:33");
            featureAttr.put("sattr_6", "6");
            featureAttr.put("sattr_9", "9");
            featureAttr.put("sattr_7", "7");
            featureAttr.put("sattr_8", "8");
            
            featureAttr.put("cattr_0", 100);
            featureAttr.put("cattr_1", 6);
            featureAttr.put("cattr_2", 6.7d);
            featureAttr.put("cattr_3", "barcode1234");
            featureAttr.put("cattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("cattr_5", "13:33");
            featureAttr.put("cattr_6", "6");
            featureAttr.put("cattr_9", "9");
            featureAttr.put("cattr_7", "7");
            featureAttr.put("cattr_8", "8");
            
            Point point = gb.createPoint(10, 5);
            featureList.add(new ShapefileFeature(point, featureAttr));
        }
        
        writeFeatures(ds, featureList);
        
        List<RecordEntry> entries = transformer.shapefileFeatureToRecordEntries(reader.getFeatureIterator(), reader.getSurveyIdList(), reader.getCensusMethodIdList());
        
        Assert.assertNotNull(entries);
        Assert.assertEquals(1, entries.size());
        
        AttributeDictionaryFactory adf = new ShapefileAttributeDictionaryFactory();
        AttributeParser parser = new ShapefileAttributeParser();
        RecordDeserializer rds = new RecordDeserializer(klu, adf, parser);
        List<RecordDeserializerResult> dsResult = rds.deserialize(currentUser, entries);
        
        Assert.assertNotNull(dsResult);
        Assert.assertEquals(1, dsResult.size());

        Assert.assertTrue("Should be an empty error map", dsResult.get(0).getErrorMap().isEmpty());
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        Record record = recordDAO.search(null, null, null).getList().get(0);
        
        Assert.assertNotNull(record);
        Assert.assertNotNull(record.getSpecies());
        Assert.assertEquals(species.getScientificName(), record.getSpecies().getScientificName());
       
        
        Assert.assertEquals(recordWhen, record.getWhen());
        
        Geometry geom = record.getGeometry();
        
        Assert.assertEquals(4326, geom.getSRID());
        
        Assert.assertTrue(geom instanceof Point);
        Point point = (Point)geom;
        Assert.assertEquals(10d, point.getX());
        Assert.assertEquals(5d, point.getY());
        
        Set<AttributeValue> avList = record.getAttributes();
        
        assertAttributeValue(avList, "sattr_0", 100);
        assertAttributeValue(avList, "sattr_1", 6);
        assertAttributeValue(avList, "sattr_2", 6.7d);
        assertAttributeValue(avList, "sattr_3", "barcode1234");
        assertAttributeValue(avList, "sattr_4", sattrDate);
        assertAttributeValue(avList, "sattr_5", "13:33");
        assertAttributeValue(avList, "sattr_6", "6");
        assertAttributeValue(avList, "sattr_7", "7");
        assertAttributeValue(avList, "sattr_8", "8");
        assertAttributeValue(avList, "sattr_9", "9");
        
        assertAttributeValue(avList, "cattr_0", 100);
        assertAttributeValue(avList, "cattr_1", 6);
        assertAttributeValue(avList, "cattr_2", 6.7d);
        assertAttributeValue(avList, "cattr_3", "barcode1234");
        assertAttributeValue(avList, "cattr_4", shpDateFormat.format(sattrDate));
        assertAttributeValue(avList, "cattr_5", "13:33");
        assertAttributeValue(avList, "cattr_6", "6");
        assertAttributeValue(avList, "cattr_7", "7");
        assertAttributeValue(avList, "cattr_8", "8");
        assertAttributeValue(avList, "cattr_9", "9");
    }
    
    @Rule
    public ExpectedException exception = ExpectedException.none();
    
    @Test
    public void validationTest() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        
        cal.clear();
        cal.set(2000, 6, 22, 00, 00, 00);
        Date beforeSurveyStartDate = cal.getTime();
        
        cal.clear();
        cal.set(2000, 1, 1);
        Date sattrDate = cal.getTime();
        
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        ShapefileToRecordEntryTransformer transformer = new ShapefileToRecordEntryTransformer(klu);
        
        // create the template:
        ShapeFileWriter writer = new ShapeFileWriter();
        File file = writer.createZipShapefile(survey, null, ShapefileType.POINT);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertEquals(survey.getId(), reader.getSurveyIdList().get(0));
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(0, reader.getCensusMethodIdList().get(0).intValue());
        
        // manipulate the template:
        GeometryBuilder gb = new GeometryBuilder();
        ShapefileDataStore ds = reader.getDataStore();
        
        List<ShapefileFeature> featureList = new LinkedList<ShapefileFeature>();
        
        // set errors in the input here
        {
            Map<String, Object> featureAttr = new HashMap<String, Object>();
            featureAttr.put(klu.getNotesKey(), "comments");
            featureAttr.put(klu.getDateKey(), shpDateFormat.format(beforeSurveyStartDate)); // error, record date is before the survey start date
            featureAttr.put(klu.getSpeciesNameKey(), "wootus maxus");
            featureAttr.put(klu.getTimeKey(), "14:30");
            featureAttr.put("sattr_0", 0);
            featureAttr.put("sattr_1", 3); // error, out of range
            featureAttr.put("sattr_2", 6.7d);
            featureAttr.put("sattr_3", ""); // error, required field
            featureAttr.put("sattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("sattr_5", "13:33");
            featureAttr.put("sattr_6", "");
            featureAttr.put("sattr_9", "");
            featureAttr.put("sattr_7", "");
            featureAttr.put("sattr_8", "");
            Point point = gb.createPoint(10, 5);
            featureList.add(new ShapefileFeature(point, featureAttr));
        }
        
        writeFeatures(ds, featureList);
        
        List<RecordEntry> entries = transformer.shapefileFeatureToRecordEntries(reader.getFeatureIterator(), reader.getSurveyIdList(), reader.getCensusMethodIdList());
        
        Assert.assertNotNull(entries);
        Assert.assertEquals(1, entries.size());
        
        AttributeDictionaryFactory adf = new ShapefileAttributeDictionaryFactory();
        AttributeParser parser = new ShapefileAttributeParser();
        RecordDeserializer rds = new RecordDeserializer(klu, adf, parser);
        List<RecordDeserializerResult> dsResult = rds.deserialize(currentUser, entries);
        
        Assert.assertNotNull(dsResult);
        Assert.assertEquals(1, dsResult.size());
        
        RecordDeserializerResult result = dsResult.get(0);
        
        // make sure we get the expected errors.
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_1"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_3"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_6"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_7"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_8"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_9"));
        Assert.assertTrue(result.getErrorMap().containsKey(klu.getDateKey()));
    }
    
    @Test
    public void testSpeciesNotInSurvey() throws Exception {
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        
        cal.clear();
        cal.set(2000, 6, 22, 00, 00, 00);
        Date beforeSurveyStartDate = cal.getTime();
        
        cal.clear();
        cal.set(2000, 1, 1);
        Date sattrDate = cal.getTime();
        
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        ShapefileToRecordEntryTransformer transformer = new ShapefileToRecordEntryTransformer(klu);
        
        // create the template:
        ShapeFileWriter writer = new ShapeFileWriter();
        File file = writer.createZipShapefile(survey, null, ShapefileType.POINT);
        
        ShapeFileReader reader = new ShapeFileReader(file);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertEquals(survey.getId(), reader.getSurveyIdList().get(0));
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(0, reader.getCensusMethodIdList().get(0).intValue());
        
        // manipulate the template:
        GeometryBuilder gb = new GeometryBuilder();
        ShapefileDataStore ds = reader.getDataStore();
        
        List<ShapefileFeature> featureList = new LinkedList<ShapefileFeature>();
        
        // set errors in the input here
        {
            Map<String, Object> featureAttr = new HashMap<String, Object>();
            featureAttr.put(klu.getNotesKey(), "comments");
            featureAttr.put(klu.getDateKey(), shpDateFormat.format(beforeSurveyStartDate)); // error, record date is before the survey start date
            featureAttr.put(klu.getSpeciesNameKey(), "notus");
            featureAttr.put(klu.getTimeKey(), "14:30");
            featureAttr.put("sattr_0", 0);
            featureAttr.put("sattr_1", 3); // error, out of range
            featureAttr.put("sattr_2", 6.7d);
            featureAttr.put("sattr_3", ""); // error, required field
            featureAttr.put("sattr_4", shpDateFormat.format(sattrDate));
            featureAttr.put("sattr_5", "13:33");
            featureAttr.put("sattr_6", "");
            featureAttr.put("sattr_9", "");
            featureAttr.put("sattr_7", "");
            featureAttr.put("sattr_8", "");
            Point point = gb.createPoint(10, 5);
            featureList.add(new ShapefileFeature(point, featureAttr));
        }
        
        writeFeatures(ds, featureList);
        
        List<RecordEntry> entries = transformer.shapefileFeatureToRecordEntries(reader.getFeatureIterator(), reader.getSurveyIdList(), reader.getCensusMethodIdList());
        
        Assert.assertNotNull(entries);
        Assert.assertEquals(1, entries.size());
        
        AttributeDictionaryFactory adf = new ShapefileAttributeDictionaryFactory();
        AttributeParser parser = new ShapefileAttributeParser();
        RecordDeserializer rds = new RecordDeserializer(klu, adf, parser);
        List<RecordDeserializerResult> dsResult = rds.deserialize(currentUser, entries);
        
        Assert.assertNotNull(dsResult);
        Assert.assertEquals(1, dsResult.size());
        
        RecordDeserializerResult result = dsResult.get(0);
        
        // make sure we get the expected errors.
        Assert.assertTrue(result.getErrorMap().containsKey(klu.getSpeciesNameKey()));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_1"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_3"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_6"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_7"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_8"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_9"));
        Assert.assertTrue(result.getErrorMap().containsKey(klu.getDateKey()));
    }
    
    private Attribute createAttribute(String name, AttributeType type, boolean required) {
        return createAttribute(name, type, required, null);
    }
    
    private Attribute createAttribute(String name, AttributeType type, boolean required, String[] args) {
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
    
    private AttributeValue getByAttributeName(Set<AttributeValue> avList, String attrName) {
        for (AttributeValue av : avList) {
            if (av.getAttribute().getName().equals(attrName)) {
                return av;
            }
        }
        return null;
    }
    
    private void writeFeatures(ShapefileDataStore ds, List<ShapefileFeature> featureList) throws IOException {
        
        org.geotools.data.Transaction shapefileTransaction = new DefaultTransaction("create");
        SimpleFeatureBuilder featureBuilder = new SimpleFeatureBuilder(ds.getSchema());
        ShapefileFeatureStore featureStore = new ShapefileFeatureStore(ds, Collections.EMPTY_SET, ds.getSchema());

        FeatureCollection<SimpleFeatureType, SimpleFeature> collection = FeatureCollections.newCollection();
        for (ShapefileFeature f : featureList) {
            Map<String, Object> attributes = f.getAttributes();
            featureBuilder.reset();
            for (Entry<String, Object> entry : attributes.entrySet()) {
                featureBuilder.set(entry.getKey(), entry.getValue());
                
            }
            featureBuilder.set(ShapefileFields.THE_GEOM, f.getGeometry());
            collection.add(featureBuilder.buildFeature(null));
        }

        featureStore.setTransaction(shapefileTransaction);
        try {
            featureStore.addFeatures(collection);
            shapefileTransaction.commit();
        } catch (Exception ex) {
            log.debug("error while writing shapefile:", ex);
            shapefileTransaction.rollback();
            Assert.fail("Could not manipulate shapefile");
        } finally {
            shapefileTransaction.close();
        }
    }
    
    private void assertAttributeValue(Set<AttributeValue> avList, String attrName, String expectedValue) {
        AttributeValue av = getByAttributeName(avList, attrName);
        Assert.assertNotNull(av);
        Assert.assertEquals(expectedValue, av.getStringValue());
    }
    
    private void assertAttributeValue(Set<AttributeValue> avList, String attrName, Date expectedValue) {
        AttributeValue av = getByAttributeName(avList, attrName);
        Assert.assertNotNull(av);
        Assert.assertEquals(expectedValue, av.getDateValue());
    }
    
    private void assertAttributeValue(Set<AttributeValue> avList, String attrName, int expectedValue) {
        AttributeValue av = getByAttributeName(avList, attrName);
        Assert.assertNotNull(av);
        Assert.assertEquals(expectedValue, av.getNumericValue().intValue());
    }
    
    private void assertAttributeValue(Set<AttributeValue> avList, String attrName, double expectedValue) {
        AttributeValue av = getByAttributeName(avList, attrName);
        Assert.assertNotNull(av);
        Assert.assertEquals(expectedValue, av.getNumericValue().doubleValue());
    }
}
