package au.com.gaiaresources.bdrs.spatial;

import java.io.File;
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
import org.junit.Test;
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

public class ExportRecordToShapefileTest extends AbstractShapefileTest {
    
    private Logger log = Logger.getLogger(getClass());
    
    // the export 'no' record case is the same as exporting a template
    @Test 
    public void testExportSingleRecord() throws Exception {
        User accessor = owner;
        ShapeFileWriter writer = new ShapeFileWriter();
        List<Record> recList = new LinkedList<Record>();
        recList.add(r1);
        File exportedFile = writer.exportRecords(recList, accessor);
        
        ShapeFileReader reader = new ShapeFileReader(exportedFile);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertEquals(survey.getId(), reader.getSurveyIdList().get(0));
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(0, reader.getCensusMethodIdList().get(0).intValue());
        
        assertFeatureCount(reader.getFeatureIterator(), recList.size());

        assertRecord(r1, reader, accessor);
    }
    
    @Test
    public void testExportMultiSurveyNoCensusMethod() throws Exception {
        User accessor = owner;
        ShapeFileWriter writer = new ShapeFileWriter();
        List<Record> recList = new LinkedList<Record>();
        recList.add(r1);
        recList.add(r3);
        File exportedFile = writer.exportRecords(recList, accessor);
        
        ShapeFileReader reader = new ShapeFileReader(exportedFile, true);
        
        Assert.assertEquals(2, reader.getSurveyIdList().size());
        Assert.assertTrue("should contain survey id : " + survey.getId(), reader.getSurveyIdList().contains(survey.getId()));
        Assert.assertTrue("should contain survey id : " + secondSurvey.getId(), reader.getSurveyIdList().contains(secondSurvey.getId()));
        // no census methods...
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertEquals(0, reader.getCensusMethodIdList().get(0).intValue());
        
        assertFeatureCount(reader.getFeatureIterator(), recList.size());
        
        assertRecord(r1, reader, accessor);
        assertRecord(r3, reader, accessor);
    }
    
    @Test
    public void testExportMultiSingleSurveyWithNonTaxaCensusMethod() throws Exception {
        User accessor = owner;
        ShapeFileWriter writer = new ShapeFileWriter();
        List<Record> recList = new LinkedList<Record>();
        recList.add(r1);
        recList.add(r3);
        recList.add(r4);
        File exportedFile = writer.exportRecords(recList, accessor);
        
        ShapeFileReader reader = new ShapeFileReader(exportedFile, true);
        
        Assert.assertEquals(2, reader.getSurveyIdList().size());
        Assert.assertTrue("should contain survey id : " + survey.getId(), reader.getSurveyIdList().contains(survey.getId()));
        Assert.assertTrue("should contain survey id : " + secondSurvey.getId(), reader.getSurveyIdList().contains(secondSurvey.getId()));
        
        // no census methods...
        Assert.assertEquals(1, reader.getCensusMethodIdList().size());
        Assert.assertTrue("should contain censusmethod id : " + cm.getId(), reader.getCensusMethodIdList().contains(cm.getId()));
        
        assertFeatureCount(reader.getFeatureIterator(), recList.size());
        
        assertRecord(r1, reader, accessor);
        assertRecord(r3, reader, accessor);
        assertRecord(r4, reader, accessor);
    }
    
    @Test
    public void testExportSingleSurveyMultiCensusMethod() throws Exception {
        User accessor = owner;
        ShapeFileWriter writer = new ShapeFileWriter();
        List<Record> recList = new LinkedList<Record>();
        recList.add(r1);
        recList.add(r2);
        recList.add(r4);
        File exportedFile = writer.exportRecords(recList, accessor);
        
        ShapeFileReader reader = new ShapeFileReader(exportedFile, true);
        
        Assert.assertEquals(1, reader.getSurveyIdList().size());
        Assert.assertTrue("should contain survey id : " + survey.getId(), reader.getSurveyIdList().contains(survey.getId()));
        
        // no census methods...
        Assert.assertEquals(2, reader.getCensusMethodIdList().size());
        Assert.assertTrue("should contain censusmethod id : " + taxaCm.getId(), reader.getCensusMethodIdList().contains(taxaCm.getId()));
        Assert.assertTrue("should contain censusmethod id : " + cm.getId(), reader.getCensusMethodIdList().contains(cm.getId()));
        
        assertFeatureCount(reader.getFeatureIterator(), recList.size());
        
        assertRecord(r1, reader, accessor);
        assertRecord(r2, reader, accessor);
        assertRecord(r4, reader, accessor);
    }
    
    private void testExportMultiSurveyMultiCensus(User accessor) throws Exception {      
        ShapeFileWriter writer = new ShapeFileWriter();
        List<Record> recList = new LinkedList<Record>();
        recList.add(r1);
        recList.add(r2);
        recList.add(r3);
        recList.add(r4);
        File exportedFile = writer.exportRecords(recList, accessor);
        
        ShapeFileReader reader = new ShapeFileReader(exportedFile, true);
        
        Assert.assertEquals(2, reader.getSurveyIdList().size());
        Assert.assertTrue("should contain survey id : " + survey.getId(), reader.getSurveyIdList().contains(survey.getId()));
        Assert.assertTrue("should contain survey id : " + secondSurvey.getId(), reader.getSurveyIdList().contains(secondSurvey.getId()));
        
        // no census methods...
        Assert.assertEquals(2, reader.getCensusMethodIdList().size());
        Assert.assertTrue("should contain censusmethod id : " + taxaCm.getId(), reader.getCensusMethodIdList().contains(taxaCm.getId()));
        Assert.assertTrue("should contain censusmethod id : " + cm.getId(), reader.getCensusMethodIdList().contains(cm.getId()));
        
        assertFeatureCount(reader.getFeatureIterator(), recList.size());
        
        assertRecord(r1, reader, accessor);
        assertRecord(r2, reader, accessor);
        assertRecord(r3, reader, accessor);
        assertRecord(r4, reader, accessor);
    }
    
    @Test
    public void testExportMultiSurveyMultiCensus_ByOwner() throws Exception {
        testExportMultiSurveyMultiCensus(owner);
    }
    
    @Test
    public void testExportMultiSurveyMultiCensus_ByAdmin() throws Exception {
        testExportMultiSurveyMultiCensus(admin);
    }
    
    @Test
    public void testMultiSurveyMultiCensusExport_ByNonOwner() throws Exception {
        testExportMultiSurveyMultiCensus(nonOwner);
    }
}

