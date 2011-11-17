package au.com.gaiaresources.bdrs.controller.bulkdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import junit.framework.Assert;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.geotools.data.DefaultTransaction;
import org.geotools.data.shapefile.ShapefileDataStore;
import org.geotools.data.shapefile.ShapefileFeatureStore;
import org.geotools.feature.FeatureCollection;
import org.geotools.feature.FeatureCollections;
import org.geotools.feature.simple.SimpleFeatureBuilder;
import org.junit.Before;
import org.junit.Test;
import org.opengis.feature.simple.SimpleFeature;
import org.opengis.feature.simple.SimpleFeatureType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializerResult;
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
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.servlet.Interceptor;
import au.com.gaiaresources.bdrs.spatial.ShapeFileReader;
import au.com.gaiaresources.bdrs.spatial.ShapeFileWriter;
import au.com.gaiaresources.bdrs.spatial.ShapefileFields;
import au.com.gaiaresources.bdrs.spatial.ShapefileRecordKeyLookup;
import au.com.gaiaresources.bdrs.spatial.ShapefileType;
import au.com.gaiaresources.bdrs.util.ZipUtils;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;


public class BulkDataController_Shapefile_Test extends AbstractControllerTest {
    
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
    TaxonGroup taxonGroup;
    CensusMethod cm;
    
    Date surveyStartDate;
    
    private SimpleDateFormat shpDateFormat = new SimpleDateFormat("dd MMM yyyy");
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() throws Exception {
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 6, 20, 14, 30, 00);
        
        survey = new Survey();
        survey.setName("my survey");
        survey.setDescription("my survey description woooo");
        
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
        
        taxonGroup = new TaxonGroup();
        taxonGroup.setRunThreshold(false);
        taxonGroup.setName("my taxon group");
        
        species = new IndicatorSpecies();
        species.setRunThreshold(false);
        species.setScientificName("wootus maxus");
        species.setCommonName("red cup");
        species.setTaxonGroup(taxonGroup);
        
        taxaDAO.save(taxonGroup);
        taxaDAO.save(species);
        
        currentUser = userDAO.getUser("admin");
        
        login("admin", "password", new String[] { Role.ADMIN } );
    }
    
    @Test
    public void testShapefileToRecordBasic() throws Exception {
        
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
            Point point = gb.createPoint(10, 5);
            featureList.add(new ShapefileFeature(point, featureAttr));
        }
        
        writeFeatures(ds, featureList);
        
        MockMultipartFile mockShapefile = prepareMockShapefile(reader.getUnzipDir());
        ((MockMultipartHttpServletRequest)request).addFile(mockShapefile);
        
        request.setRequestURI(BulkDataController.SHAPEFILE_UPLOAD_URL);
        request.setMethod("POST");
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertNull(request.getAttribute(Interceptor.REQUEST_ROLLBACK));
        
        Assert.assertEquals(BulkDataController.SHAPEFILE_IMPORT_SUMMARY_VIEW, mv.getViewName());
        Assert.assertNull(mv.getModel().get(BulkDataController.MV_PARAM_RESULTS_IN_ERROR));
        Assert.assertEquals(1, ((Integer)mv.getModel().get(BulkDataController.MV_PARAM_WRITE_COUNT)).intValue());
        
        Assert.assertEquals(1, recordDAO.countAllRecords().intValue());
        
        Record record = recordDAO.search(null, null, null).getList().get(0);
        
        Assert.assertNotNull(record);
        Assert.assertNotNull(record.getSpecies());
        Assert.assertEquals("wootus maxus", record.getSpecies().getScientificName());
        
        Assert.assertEquals(recordWhen, record.getWhen());
        
        Geometry geom = record.getGeometry();
        Assert.assertTrue(geom instanceof Point);
        Point point = (Point)geom;
        Assert.assertEquals(10d, point.getX());
        Assert.assertEquals(5d, point.getY());
        
        Set<AttributeValue> avList = record.getAttributes();
        
        assertAttributeValue(avList, "sattr_0", 100);
        assertAttributeValue(avList, "sattr_1", 6);
        assertAttributeValue(avList, "sattr_2", 6.7d);
        assertAttributeValue(avList, "sattr_3", "barcode1234");
        assertAttributeValue(avList, "sattr_4", shpDateFormat.format(sattrDate));
        assertAttributeValue(avList, "sattr_5", "13:33");
        assertAttributeValue(avList, "sattr_6", "6");
        assertAttributeValue(avList, "sattr_7", "7");
        assertAttributeValue(avList, "sattr_8", "8");
        assertAttributeValue(avList, "sattr_9", "9");
    }
    
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
        
        MockMultipartFile mockShapefile = prepareMockShapefile(reader.getUnzipDir());
        ((MockMultipartHttpServletRequest)request).addFile(mockShapefile);
        
        request.setRequestURI(BulkDataController.SHAPEFILE_UPLOAD_URL);
        request.setMethod("POST");
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertNotNull(request.getAttribute(Interceptor.REQUEST_ROLLBACK));
        
        Assert.assertEquals(BulkDataController.SHAPEFILE_IMPORT_SUMMARY_VIEW, mv.getViewName());
        Assert.assertNotNull(mv.getModel().get(BulkDataController.MV_PARAM_RESULTS_IN_ERROR));
        
        List<RecordDeserializerResult> resultsInError = (List<RecordDeserializerResult>)mv.getModel().get(BulkDataController.MV_PARAM_RESULTS_IN_ERROR);
        
        Assert.assertEquals(1, resultsInError.size());
        RecordDeserializerResult result = resultsInError.get(0);
        
        // make sure we get the expected errors.
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_1"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_3"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_6"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_7"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_8"));
        Assert.assertTrue(result.getErrorMap().containsKey("sattr_9"));
        Assert.assertTrue(result.getErrorMap().containsKey(klu.getDateKey()));
    }
    
    private MockMultipartFile prepareMockShapefile(File unzipDir) throws IOException {
        File zipFileToUpload = File.createTempFile("zipshapefiletoupload", ".zip");
        ZipUtils.compress(unzipDir.listFiles(), zipFileToUpload);
        InputStream fileIn = null;
        byte[] fileBytes = null;
        try {
            fileIn = new FileInputStream(zipFileToUpload);
            fileBytes = IOUtils.toByteArray(fileIn);
        } finally {
            if (fileIn != null) {
                fileIn.close();
            }
        }
        Assert.assertNotNull(fileBytes);
        return new MockMultipartFile(BulkDataController.PARAM_SHAPEFILE_FILE, zipFileToUpload.getName(), "application/octet-stream", fileBytes);
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
    
    private class ShapefileFeature {
        
        private Geometry geom;
        private Map<String, Object> attributes;
        
        public ShapefileFeature(Geometry geom, Map<String, Object> attributes) {
            this.geom = geom;
            this.attributes = attributes;
        }
        
        public Geometry getGeometry() {
            return geom;
        }
        
        public Map<String, Object> getAttributes() {
            return attributes;
        }
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
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
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
