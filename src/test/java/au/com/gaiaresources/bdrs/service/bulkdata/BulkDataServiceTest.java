package au.com.gaiaresources.bdrs.service.bulkdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.hibernate.SQLQuery;
import org.hibernate.Transaction;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.transaction.AfterTransaction;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;

import com.vividsolutions.jts.geom.Point;

public class BulkDataServiceTest extends AbstractControllerTest {
    @Autowired
    private BulkDataService bulkDataService;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private CensusMethodDAO cmDAO;
    @Autowired
    private RecordDAO recDAO;
    @Autowired 
    private AttributeDAO attrDAO;
    @Autowired
    private BulkDataReadWriteService bdrws;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private LocationService locationService;
    @Autowired
    private LSIDService lsidService;
    
    Logger log = Logger.getLogger(getClass());
    
    Survey survey;
    CensusMethod cm;
    Attribute testAttr1;
    Attribute testAttr2;
    CensusMethod cm2;
    Attribute testAttr3;
    Attribute testAttr4;
    
    Attribute surveyAttr1;
    Attribute surveyAttr2;
    
    User user;
    
    TaxonGroup taxongroup;
    IndicatorSpecies species;
    
    @Before
    public void setup() {
        user = userDAO.getUser("admin");
        survey = surveyDAO.createSurvey("my super survey");
        survey.setDescription("a really great survey");
        
        cm = createCensusMethod("c:m:1:", Taxonomic.NONTAXONOMIC);
        testAttr1 = createAttribute("attribute1", "desc1", true, AttributeScope.RECORD, false, "IN");
        testAttr2 = createAttribute("attribute2", "desc2", true, AttributeScope.RECORD, false, "ST");
        cm.getAttributes().add(testAttr1);
        cm.getAttributes().add(testAttr2);
        survey.getCensusMethods().add(cm);
        
        cm2 = createCensusMethod("cm2", Taxonomic.NONTAXONOMIC);
        testAttr3 = createAttribute("attribute3", "desc3", true, AttributeScope.RECORD, false, "IN");
        testAttr4 = createAttribute("attribute4", "desc4", true, AttributeScope.RECORD, false, "ST");
        cm2.getAttributes().add(testAttr3);
        cm2.getAttributes().add(testAttr4);
        survey.getCensusMethods().add(cm2);
        
        survey.getCensusMethods().add(cm2);
        
        // make cm1 and cm2 do the recursion thing
        cm.getCensusMethods().add(cm2);
        cm2.getCensusMethods().add(cm);
        
        cmDAO.update(cm);
        cmDAO.update(cm2);
        
        
        surveyAttr1 = createAttribute("surv1", "sdesc1", true, AttributeScope.RECORD, false, "IN");
        surveyAttr2 = createAttribute("surv2", "sdesc2", true, AttributeScope.RECORD, false, "ST");
        
        survey.getAttributes().add(surveyAttr1);
        survey.getAttributes().add(surveyAttr2);
        
        taxongroup = taxaDAO.createTaxonGroup("a taxon group", false, false, false, false, false, false);
        species = taxaDAO.createIndicatorSpecies("hectus workus", "argh pirate", taxongroup, new ArrayList<Region>(), new ArrayList<SpeciesProfile>());
        
        surveyDAO.updateSurvey(survey);
        
        requestDropDatabase();
    }
    
    @Test
    public void testImportSurvey() throws Exception, ParseException {
        InputStream stream = getClass().getResourceAsStream("basic_upload.xls");
        Survey survey = new Survey();

        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, stream);
        
        Assert.assertEquals(1, bulkUpload.getRecordUploadList().size());
        RecordUpload recUpload = bulkUpload.getRecordUploadList().get(0);

        Calendar cal = Calendar.getInstance();
        cal.set(2011, 2, 27, 14, 42, 0);
        
        //Assert.assertEquals(cal.getTime(), recUpload.getWhen());
        // there's some messed up rounding going on here... accurate to within the second (1000 ms)
        Assert.assertTrue(Math.abs(cal.getTime().getTime() - recUpload.getWhen().getTime()) < 1000);
    }
    
    @Test
    public void testExportSurveyTemplateCensusMethod() throws IOException {
        File spreadSheetTmp = File.createTempFile("BulkDataServiceTest.testExportSurveyTemplateCensusMethod", ".xls");
        FileOutputStream outStream = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream);
        
        bulkDataService.exportSurveyTemplate(survey, outStream);
        
        InputStream inStream = new FileInputStream(spreadSheetTmp);
        registerStream(inStream);

        Workbook wb = new HSSFWorkbook(inStream);
        
        Sheet obSheet = wb.getSheet(AbstractBulkDataService.RECORD_SHEET_NAME);
        Assert.assertNotNull(obSheet);
        
        // The title
        Assert.assertEquals("my super survey: a really great survey", obSheet.getRow(0).getCell(0).getStringCellValue());
        
        
        // The header - contains the attribute names
        Assert.assertNotNull(obSheet.getRow(2));
        
        int colIdx = 0;
        Assert.assertEquals("ID", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Parent ID", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Census Method", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Scientific Name", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Common Name", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Location Name", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Latitude", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Longitude", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Date", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Time", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Number Seen", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Notes", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        Assert.assertEquals("sdesc1", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("sdesc2", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        // cm1
        Assert.assertEquals(bdrws.formatCensusMethodNameId(cm), obSheet.getRow(1).getCell(colIdx).getStringCellValue());
        Assert.assertEquals("desc1", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        Assert.assertEquals("desc2", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        // cm2
        Assert.assertEquals(bdrws.formatCensusMethodNameId(cm2), obSheet.getRow(1).getCell(colIdx).getStringCellValue());
        Assert.assertEquals("desc3", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        Assert.assertEquals("desc4", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
    }
    
    @Test
    public void testExportSurveyTemplateNoCensusMethod() throws IOException {
        survey.setCensusMethods(new ArrayList<CensusMethod>());
        surveyDAO.updateSurvey(survey);
        
        File spreadSheetTmp = File.createTempFile("BulkDataServiceTest.testExportSurveyTemplateNoCensusMethod", ".xls");
        FileOutputStream outStream = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream);
        
        bulkDataService.exportSurveyTemplate(survey, outStream);
        
        InputStream inStream = new FileInputStream(spreadSheetTmp);
        registerStream(inStream);

        Workbook wb = new HSSFWorkbook(inStream);
        
        Sheet obSheet = wb.getSheet(AbstractBulkDataService.RECORD_SHEET_NAME);
        Assert.assertNotNull(obSheet);
        
        // The title
        Assert.assertEquals("my super survey: a really great survey", obSheet.getRow(0).getCell(0).getStringCellValue());
        
        
        // The header - contains the attribute names
        Assert.assertNotNull(obSheet.getRow(2));
        
        int colIdx = 0;
        Assert.assertEquals("ID", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Parent ID", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Census Method", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Scientific Name", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Common Name", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Location Name", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Latitude", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Longitude", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Date", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Time", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Number Seen", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("Notes", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        Assert.assertEquals("sdesc1", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        Assert.assertEquals("sdesc2", obSheet.getRow(2).getCell(colIdx++).getStringCellValue());
        
        Assert.assertNull(obSheet.getRow(1).getCell(colIdx));
        Assert.assertNull(obSheet.getRow(2).getCell(colIdx++));
    }
    
    @Test
    public void testImportSurveyNumericIds() throws IOException, ParseException, MissingDataException, InvalidSurveySpeciesException, DataReferenceException {
        surveyDAO.updateSurvey(survey);
        
        File spreadSheetTmp = File.createTempFile("BulkDataServiceTest.testImportSurveyNoCensusMethod", ".xls");
        FileOutputStream outStream = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream);
        
        bulkDataService.exportSurveyTemplate(survey, outStream);
        
        InputStream inStream = new FileInputStream(spreadSheetTmp);
        registerStream(inStream);

        Workbook wb = new HSSFWorkbook(inStream);
        
        Sheet obSheet = wb.getSheet(AbstractBulkDataService.RECORD_SHEET_NAME);
        Assert.assertNotNull(obSheet);
        
        // The title
        Assert.assertEquals("my super survey: a really great survey", obSheet.getRow(0).getCell(0).getStringCellValue());
        
        String ssParentId = "1";
        
        MyTestRow parentRow = new MyTestRow();
        parentRow.setIdAsNumber(true);
        // test with leading / trailing white space
        parentRow.setId(ssParentId);
        parentRow.setCmId(bdrws.formatCensusMethodNameId(cm));
        parentRow.setScientificName("hectus workus");
        parentRow.setSurveyAttr1(1);
        parentRow.setSurveyAttr2("4");
        parentRow.setCm1Attr1(2);
        parentRow.setCm1Attr2("5");
        parentRow.setCm2Attr1(3);
        parentRow.setCm2Attr2("6");
        parentRow.createRow(obSheet, 3);
        
        MyTestRow childRow = new MyTestRow();
        childRow.setIdAsNumber(true);
        childRow.setId("");
        childRow.setParentId(ssParentId);
        childRow.setCmId(bdrws.formatCensusMethodNameId(cm2));
        childRow.setScientificName("hectus workus");
        childRow.setSurveyAttr1(11);
        childRow.setNumberSeen(1);
        childRow.setNotes("7");
        childRow.setSurveyAttr2("8");
        childRow.setCm1Attr1(2);
        childRow.setCm1Attr2("9");
        childRow.setCm2Attr1(33);
        childRow.setCm2Attr2("10");
        childRow.createRow(obSheet, 4);
        
        FileOutputStream outStream2 = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream2);
        wb.write(outStream2);
        
        FileInputStream inStream2 = new FileInputStream(spreadSheetTmp);
        registerStream(inStream2);
        
        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, inStream2);
        
        Assert.assertEquals(2, bulkUpload.getRecordUploadList().size());
 
        sessionFactory.getCurrentSession().getTransaction().commit();
        sessionFactory.getCurrentSession().beginTransaction();
        bulkDataService.saveRecords(user, bulkUpload, true);
        
        Assert.assertEquals(2, recDAO.countAllRecords().intValue());
        
        //Record r = recDAO.getLatestRecord();
        List<Record> recList = recDAO.getRecords(user);
        {
            Record r = getRecordByCensusMethod(recList, cm);
           
            Assert.assertNotNull(r);
            Assert.assertEquals(cm.getId(), r.getCensusMethod().getId());
            
            // Assert the parent is correct
            Assert.assertNotNull(getRecAttr(r, survey, "sdesc1"));
            Assert.assertEquals(1, getRecAttr(r, survey, "sdesc1").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(r, survey, "sdesc2"));
            Assert.assertEquals("4", getRecAttr(r, survey, "sdesc2").getStringValue());
            
            Assert.assertNotNull(getRecAttr(r, cm, "desc1"));
            Assert.assertEquals(2, getRecAttr(r, cm, "desc1").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(r, cm, "desc2"));
            Assert.assertEquals("5", getRecAttr(r, cm, "desc2").getStringValue());
            
            Assert.assertNull(getRecAttr(r, cm2, "desc3"));
            Assert.assertNull(getRecAttr(r, cm2, "desc4"));
        }
        
        // Assert the child is correct
        {
            Record parentRecord = getRecordByCensusMethod(recList, cm);
            Record rChild = getRecordByCensusMethod(recList, cm2);
            Assert.assertNotNull(rChild);
            Assert.assertEquals(cm2.getId(), rChild.getCensusMethod().getId());
            
            Assert.assertEquals(parentRecord, rChild.getParentRecord());
        
            Assert.assertNotNull(getRecAttr(rChild, survey, "sdesc1"));
            Assert.assertEquals(11, getRecAttr(rChild, survey, "sdesc1").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(rChild, survey, "sdesc2"));
            Assert.assertEquals("8", getRecAttr(rChild, survey, "sdesc2").getStringValue());
            
            Assert.assertNull(getRecAttr(rChild, cm, "desc1"));
            Assert.assertNull(getRecAttr(rChild, cm, "desc2"));
            
            Assert.assertNotNull(getRecAttr(rChild, cm2, "desc3"));
            Assert.assertEquals(33, getRecAttr(rChild, cm2, "desc3").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(rChild, cm2, "desc4"));
            Assert.assertEquals("10", getRecAttr(rChild, cm2, "desc4").getStringValue());
        }
    }
    
    @Test(expected=DataReferenceException.class)
    public void testImportSurveyNoCensusMethod() throws IOException, ParseException, MissingDataException, InvalidSurveySpeciesException, DataReferenceException {
        // Will throw an exception
        survey.setCensusMethods(new ArrayList<CensusMethod>());
        surveyDAO.updateSurvey(survey);
        
        File spreadSheetTmp = File.createTempFile("BulkDataServiceTest.testImportSurveyNoCensusMethod", ".xls");
        FileOutputStream outStream = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream);
        
        bulkDataService.exportSurveyTemplate(survey, outStream);
        
        InputStream inStream = new FileInputStream(spreadSheetTmp);
        registerStream(inStream);

        Workbook wb = new HSSFWorkbook(inStream);
        
        Sheet obSheet = wb.getSheet(AbstractBulkDataService.RECORD_SHEET_NAME);
        Assert.assertNotNull(obSheet);
        
        // The title
        Assert.assertEquals("my super survey: a really great survey", obSheet.getRow(0).getCell(0).getStringCellValue());
        
        String ssParentId = "parentid";
        
        MyTestRow parentRow = new MyTestRow();
        // test with leading / trailing white space
        parentRow.setId(" " + ssParentId + " ");
        parentRow.setCmId("");
        parentRow.setScientificName("hectus workus");
        parentRow.setSurveyAttr1(1);
        parentRow.setSurveyAttr2("string1");
        parentRow.setCm1Attr1(2);
        parentRow.setCm1Attr2("string2");
        parentRow.setCm2Attr1(3);
        parentRow.setCm2Attr2("string3");
        parentRow.createRow(obSheet, 3);
        
        MyTestRow childRow = new MyTestRow();
        childRow.setId("");
        childRow.setParentId(ssParentId);
        childRow.setCmId("");
        childRow.setScientificName("hectus workus");
        childRow.setSurveyAttr1(11);
        childRow.setNumberSeen(1);
        childRow.setNotes("child entry notes");
        childRow.setSurveyAttr2("survey attr string");
        childRow.setCm1Attr1(2);
        childRow.setCm1Attr2("string2");
        childRow.setCm2Attr1(33);
        childRow.setCm2Attr2("choo choo");
        childRow.createRow(obSheet, 4);
        
        FileOutputStream outStream2 = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream2);
        wb.write(outStream2);
        
        FileInputStream inStream2 = new FileInputStream(spreadSheetTmp);
        registerStream(inStream2);
        
        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, inStream2);
        
        Assert.assertEquals(2, bulkUpload.getRecordUploadList().size());
 
        sessionFactory.getCurrentSession().getTransaction().commit();
        sessionFactory.getCurrentSession().beginTransaction();
        bulkDataService.saveRecords(user, bulkUpload, true);
    }
    
    private void createCensusMethodRecordAttributes(Record record) throws ParseException {
        if (record.getCensusMethod() == null) {
            return;
        }
        for (Attribute cmAttr : record.getCensusMethod().getAttributes()) {
            AttributeValue recAttr = new AttributeValue();
            recAttr.setAttribute(cmAttr);
            switch (cmAttr.getType()) {
            case INTEGER:
            case DECIMAL:
                recAttr.setNumericValue(new BigDecimal(1000));
                break;
            case DATE:
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "dd MMM yyyy");
                dateFormat.setLenient(false);
                recAttr.setDateValue(dateFormat.parse("11 may 2011"));
                break;
            case IMAGE:
            case FILE:
                throw new UnsupportedOperationException(
                        "Spreadsheet upload of file data is not supported.");
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case STRING:
            default:
                recAttr.setStringValue("cm attr");
                break;
            }
            recDAO.saveAttributeValue(recAttr);
            record.getAttributes().add(recAttr);
        }
    }
   
    @Test
    public void testExportEditXls() throws IOException, ParseException, MissingDataException, InvalidSurveySpeciesException, DataReferenceException {
        
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 2, 27, 14, 42, 0);
        Point point = locationService.createPoint(-40, 120);
        Record rec = recDAO.createRecord(survey, point, user, species, cal.getTime(), cal.getTime().getTime(), "", false, false, "", "", 200, new HashMap<Attribute, Object>());
        rec.setCensusMethod(cm);
        createCensusMethodRecordAttributes(rec);
        
        //cm.get
        
        Record recChild = recDAO.createRecord(survey, point, user, species, cal.getTime(), cal.getTime().getTime(), "", false, false, "", "", 200, new HashMap<Attribute, Object>());
        recChild.setParentRecord(rec);
        recChild.setCensusMethod(cm2);
        createCensusMethodRecordAttributes(recChild);
        
        List<Record> recListToDownload = new ArrayList<Record>();
        recListToDownload.add(rec);
        recListToDownload.add(recChild);
        
        File spreadSheetTmp = File.createTempFile("BulkDataServiceTest.testExportEditXls", ".xls");
        FileOutputStream outStream = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream);
        
        bulkDataService.exportSurveyRecords(survey, recListToDownload, outStream);
        
        InputStream inStream = new FileInputStream(spreadSheetTmp);
        registerStream(inStream);
        Workbook wb = new HSSFWorkbook(inStream);
        
        Sheet obSheet = wb.getSheet(AbstractBulkDataService.RECORD_SHEET_NAME);
        
        {
            Row parentXlsRow = obSheet.getRow(3);
            int colIdx = 0;
            Assert.assertEquals(lsidService.toLSID(rec).toString(), parentXlsRow.getCell(colIdx++).getStringCellValue());  // ID
            Assert.assertFalse(StringUtils.hasLength(parentXlsRow.getCell(colIdx++).getStringCellValue())); // parentId
            Assert.assertEquals(bdrws.formatCensusMethodNameId(rec.getCensusMethod()), parentXlsRow.getCell(colIdx++).getStringCellValue());
            
            Assert.assertEquals(-40, ((Double)parentXlsRow.getCell(6).getNumericCellValue()).intValue());
            Assert.assertEquals(120, ((Double)parentXlsRow.getCell(7).getNumericCellValue()).intValue());
            
            // Yes I know this makes for a fragile and hard to read test, at least you get to have fun looking
            // at the code now that you've broken it
            // p.s. pls don't remove these asserts, move them to whatever cell index is correct for your
            // new column arrangement!
            Assert.assertEquals(1000, ((Double)parentXlsRow.getCell(14).getNumericCellValue()).intValue());
            Assert.assertEquals("cm attr", parentXlsRow.getCell(15).getStringCellValue());
            
            Assert.assertFalse(StringUtils.hasLength(parentXlsRow.getCell(16).getStringCellValue()));
            Assert.assertFalse(StringUtils.hasLength(parentXlsRow.getCell(17).getStringCellValue()));
        }
        
        {
            Row childXlsRow = obSheet.getRow(4);
            int colIdx = 0;
            Assert.assertEquals(lsidService.toLSID(recChild).toString(), childXlsRow.getCell(colIdx++).getStringCellValue());
            Assert.assertEquals(lsidService.toLSID(rec).toString(), childXlsRow.getCell(colIdx++).getStringCellValue());
            Assert.assertEquals(bdrws.formatCensusMethodNameId(recChild.getCensusMethod()), childXlsRow.getCell(colIdx++).getStringCellValue());
            
            Assert.assertFalse(StringUtils.hasLength(childXlsRow.getCell(14).getStringCellValue()));
            Assert.assertFalse(StringUtils.hasLength(childXlsRow.getCell(15).getStringCellValue()));
            
            Assert.assertEquals(1000, ((Double)childXlsRow.getCell(16).getNumericCellValue()).intValue());
            Assert.assertEquals("cm attr", childXlsRow.getCell(17).getStringCellValue());
        }
        
        // now that it came out right, lets edit it!
        {
            Row childXlsRow = obSheet.getRow(4);
            childXlsRow.getCell(1).setCellValue("");
            
            childXlsRow.getCell(16).setCellValue(10);
            childXlsRow.getCell(17).setCellValue("new string");
        }
        
        // add a new row, make it a child of our original parent record
        {
            MyTestRow childRow = new MyTestRow();
            childRow.setId("weeeee");
            childRow.setParentId(lsidService.toLSID(rec).toString());
            childRow.setCmId(bdrws.formatCensusMethodNameId(cm2));
            childRow.setSurveyAttr1(11);
            childRow.setNumberSeen(1);
            childRow.setNotes("child entry notes");
            childRow.setSurveyAttr2("survey attr string");
            childRow.setCm1Attr1(2);
            childRow.setCm1Attr2("string2");
            childRow.setCm2Attr1(33);
            childRow.setCm2Attr2("choo choo");
            childRow.createRow(obSheet, 5);
        }
        
        FileOutputStream outStream2 = new FileOutputStream(spreadSheetTmp);
        registerStream(outStream2);
        wb.write(outStream2);
        
        FileInputStream inStream2 = new FileInputStream(spreadSheetTmp);
        registerStream(inStream2);
        
        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, inStream2);
        
        Assert.assertEquals(3, bulkUpload.getRecordUploadList().size());
 
        sessionFactory.getCurrentSession().getTransaction().commit();
        bulkDataService.saveRecords(user, bulkUpload, true);
        // save Records will end the current transaction one way or another....so we 
        // need a new one to use our DAOs.
        sessionFactory.getCurrentSession().beginTransaction();
        
        Assert.assertEquals(3, recDAO.countAllRecords().intValue());
        
        sessionFactory.getCurrentSession().refresh(rec);
        sessionFactory.getCurrentSession().refresh(recChild);
        
        Assert.assertNull(recChild.getParentRecord());
        
        Assert.assertEquals(1, rec.getChildRecords().size());
    }
    
    @Test
    public void testImportSurveyCensusMethodRecordInRecord() throws IOException, ParseException, MissingDataException, InvalidSurveySpeciesException, DataReferenceException {
        File spreadSheetTmp = File.createTempFile("BulkDataServiceTest.testImportSurveyCensusMethodRecordInRecord", ".xls");
        FileOutputStream outStream = new FileOutputStream(spreadSheetTmp);
        bulkDataService.exportSurveyTemplate(survey, outStream);
        registerStream(outStream);
        
        InputStream inStream = new FileInputStream(spreadSheetTmp);
        Workbook wb = new HSSFWorkbook(inStream);
        registerStream(inStream);
        
        Sheet obSheet = wb.getSheet(AbstractBulkDataService.RECORD_SHEET_NAME);
        // enter records starting at row 3
        
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 2, 27, 14, 42, 0);
        
        // parent id can be any string so...
        String ssParentId = "yodawg!";
        
        MyTestRow parentRow = new MyTestRow();
        // test with leading / trailing white space
        parentRow.setId(" " + ssParentId + " ");
        parentRow.setCmId(bdrws.formatCensusMethodNameId(cm));
        parentRow.setSurveyAttr1(1);
        parentRow.setSurveyAttr2("string1");
        parentRow.setCm1Attr1(2);
        parentRow.setCm1Attr2("string2");
        parentRow.setCm2Attr1(3);
        parentRow.setCm2Attr2("string3");
        parentRow.createRow(obSheet, 3);
        
        MyTestRow childRow = new MyTestRow();
        childRow.setId("");
        childRow.setParentId(ssParentId);
        childRow.setCmId(bdrws.formatCensusMethodNameId(cm2));
        childRow.setSurveyAttr1(11);
        childRow.setNumberSeen(1);
        childRow.setNotes("child entry notes");
        childRow.setSurveyAttr2("survey attr string");
        childRow.setCm1Attr1(2);
        childRow.setCm1Attr2("string2");
        childRow.setCm2Attr1(33);
        childRow.setCm2Attr2("choo choo");
        childRow.createRow(obSheet, 4);
        
        FileOutputStream outStream2 = new FileOutputStream(spreadSheetTmp);
        wb.write(outStream2);
        registerStream(outStream2);
        
        FileInputStream inStream2 = new FileInputStream(spreadSheetTmp);
        registerStream(inStream2);
        
        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, inStream2);
        
        Assert.assertEquals(2, bulkUpload.getRecordUploadList().size());
 
        sessionFactory.getCurrentSession().getTransaction().commit();
        // we need a new one to use our DAOs.
        sessionFactory.getCurrentSession().beginTransaction();
        
        bulkDataService.saveRecords(user, bulkUpload, true);

        Assert.assertEquals(2, recDAO.countAllRecords().intValue());
        
        //Record r = recDAO.getLatestRecord();
        List<Record> recList = recDAO.getRecords(user);
        {
            Record r = getRecordByCensusMethod(recList, cm);
           
            Assert.assertNotNull(r);
            Assert.assertEquals(cm.getId(), r.getCensusMethod().getId());
            
            // Assert the parent is correct
            Assert.assertNotNull(getRecAttr(r, survey, "sdesc1"));
            Assert.assertEquals(1, getRecAttr(r, survey, "sdesc1").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(r, survey, "sdesc2"));
            Assert.assertEquals("string1", getRecAttr(r, survey, "sdesc2").getStringValue());
            
            Assert.assertNotNull(getRecAttr(r, cm, "desc1"));
            Assert.assertEquals(2, getRecAttr(r, cm, "desc1").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(r, cm, "desc2"));
            Assert.assertEquals("string2", getRecAttr(r, cm, "desc2").getStringValue());
            
            Assert.assertNull(getRecAttr(r, cm2, "desc3"));
            Assert.assertNull(getRecAttr(r, cm2, "desc4"));
        }
        
        // Assert the child is correct
        {
            Record parentRecord = getRecordByCensusMethod(recList, cm);
            Record rChild = getRecordByCensusMethod(recList, cm2);
            Assert.assertNotNull(rChild);
            Assert.assertEquals(cm2.getId(), rChild.getCensusMethod().getId());
            
            Assert.assertEquals(parentRecord, rChild.getParentRecord());
        
            Assert.assertNotNull(getRecAttr(rChild, survey, "sdesc1"));
            Assert.assertEquals(11, getRecAttr(rChild, survey, "sdesc1").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(rChild, survey, "sdesc2"));
            Assert.assertEquals("survey attr string", getRecAttr(rChild, survey, "sdesc2").getStringValue());
            
            Assert.assertNull(getRecAttr(rChild, cm, "desc1"));
            Assert.assertNull(getRecAttr(rChild, cm, "desc2"));
            
            Assert.assertNotNull(getRecAttr(rChild, cm2, "desc3"));
            Assert.assertEquals(33, getRecAttr(rChild, cm2, "desc3").getNumericValue().intValue());
            
            Assert.assertNotNull(getRecAttr(rChild, cm2, "desc4"));
            Assert.assertEquals("choo choo", getRecAttr(rChild, cm2, "desc4").getStringValue());
        }
    }
    
    @Test
    public void testImportSurveyInvalidDateRange() throws Exception, ParseException {
        InputStream stream = getClass().getResourceAsStream("invalid_daterange.xls");
        this.registerStream(stream);
        Survey survey = new Survey();
        
        // set the survey start date and end date after the one in the file
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 6, 5, 0, 0, 0);
        survey.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 1);
        survey.setEndDate(cal.getTime());
        
        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, stream);
        
        // should be one correct and one incorrect
        Assert.assertEquals(1, bulkUpload.getRecordUploadList().size());
        Assert.assertEquals(1, bulkUpload.getErrorRecordUploadList().size());
        
    }
    
    @Test
    public void testImportSurveyValidDateRange() throws Exception, ParseException {
        InputStream stream = getClass().getResourceAsStream("invalid_daterange.xls");
        this.registerStream(stream);
        Survey survey = new Survey();
        
        // set the survey start date and end date before and after the ones in the file
        Calendar cal = Calendar.getInstance();
        cal.set(2011, 6, 1, 0, 0, 0);
        survey.setStartDate(cal.getTime());
        cal.add(Calendar.DAY_OF_MONTH, 7);
        survey.setEndDate(cal.getTime());
        
        BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, stream);
        List<RecordUpload> errors = bulkUpload.getErrorRecordUploadList();
        
        Assert.assertEquals(0, errors.size());
        Assert.assertEquals(2, bulkUpload.getRecordUploadList().size());
    }
    
    private class MyTestRow {
        
        boolean idAsNumber = false;
        String id = "";
        String parentId = "";
        String cmId = "";
        String scientificName = "";
        String commonName = "";
        String locationName = "";
        double latitude = -31;
        double longitude = 115;
        Date date = new Date();
        Date time = new Date();
        double numberSeen = 100;
        String notes = "notes";
        double surveyAttr1 = 1;
        String surveyAttr2 = "string1";
        double cm1Attr1 = 2;
        String cm1Attr2 = "string2";
        double cm2Attr1 = 3;
        String cm2Attr2 = "string3";
        
        public Row createRow(Sheet sheet, int rowIndex) {
            Row result = sheet.createRow(rowIndex);
            int colIdx = 0;

            if (!idAsNumber) {
                result.createCell(colIdx++).setCellValue(id); // id
                result.createCell(colIdx++).setCellValue(parentId); // parent id
                result.createCell(colIdx++).setCellValue(cmId); // census method
                result.createCell(colIdx++).setCellValue(scientificName); // scientific name
                result.createCell(colIdx++).setCellValue(commonName); // common name
                result.createCell(colIdx++).setCellValue(locationName); // location name
                result.createCell(colIdx++).setCellValue(latitude); // latitude
                result.createCell(colIdx++).setCellValue(longitude); // longitude
                result.createCell(colIdx++).setCellValue(date); // date
                result.createCell(colIdx++).setCellValue(time); // time
                result.createCell(colIdx++).setCellValue(numberSeen); // number seen
                result.createCell(colIdx++).setCellValue(notes); // notes
                result.createCell(colIdx++).setCellValue(surveyAttr1); // sdesc1
                result.createCell(colIdx++).setCellValue(surveyAttr2); // sdesc2
                result.createCell(colIdx++).setCellValue(cm1Attr1); // cm1 - desc1
                result.createCell(colIdx++).setCellValue(cm1Attr2); // cm1 - desc2
                result.createCell(colIdx++).setCellValue(cm2Attr1); // cm2 - desc3
                result.createCell(colIdx++).setCellValue(cm2Attr2); // cm2 - desc4
            } else {
                if (StringUtils.hasLength(id)) {
                    result.createCell(colIdx++).setCellValue(Double.parseDouble(id)); // id
                } else {
                    result.createCell(colIdx++).setCellValue(""); // id
                }
                if (StringUtils.hasLength(parentId)) {
                    result.createCell(colIdx++).setCellValue(Double.parseDouble(parentId)); // parent id
                } else {
                    result.createCell(colIdx++).setCellValue(""); // parent id
                }
                //result.createCell(colIdx++).setCellValue(StringUtils.hasLength(id) ? Double.parseDouble(id) : ""); // id
                //result.createCell(colIdx++).setCellValue(StringUtils.hasLength(parentId) ? Double.parseDouble(parentId) : ""); // parent id
                result.createCell(colIdx++).setCellValue(cmId); // census method
                result.createCell(colIdx++).setCellValue(scientificName); // scientific name
                result.createCell(colIdx++).setCellValue(commonName); // common name
                result.createCell(colIdx++).setCellValue(locationName); // location name
                result.createCell(colIdx++).setCellValue(latitude); // latitude
                result.createCell(colIdx++).setCellValue(longitude); // longitude
                result.createCell(colIdx++).setCellValue(date); // date
                result.createCell(colIdx++).setCellValue(time); // time
                result.createCell(colIdx++).setCellValue(numberSeen); // number seen
                result.createCell(colIdx++).setCellValue(notes); // notes
                result.createCell(colIdx++).setCellValue(surveyAttr1); // sdesc1
                result.createCell(colIdx++).setCellValue(surveyAttr2); // sdesc2
                result.createCell(colIdx++).setCellValue(cm1Attr1); // cm1 - desc1
                result.createCell(colIdx++).setCellValue(cm1Attr2); // cm1 - desc2
                result.createCell(colIdx++).setCellValue(cm2Attr1); // cm2 - desc3
                result.createCell(colIdx++).setCellValue(cm2Attr2); // cm2 - desc4
            }
            return result;
        }
        
        public String getId() {
            return id;
        }
        public void setId(String id) {
            this.id = id;
        }
        public String getParentId() {
            return parentId;
        }
        public void setParentId(String parentId) {
            this.parentId = parentId;
        }
        public String getCmId() {
            return cmId;
        }
        public void setCmId(String cmId) {
            this.cmId = cmId;
        }
        public String getScientificName() {
            return scientificName;
        }
        public void setScientificName(String scientificName) {
            this.scientificName = scientificName;
        }
        public String getCommonName() {
            return commonName;
        }
        public void setCommonName(String commonName) {
            this.commonName = commonName;
        }
        public double getLatitude() {
            return latitude;
        }
        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }
        public double getLongitude() {
            return longitude;
        }
        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }
        public Date getDate() {
            return date;
        }
        public void setDate(Date date) {
            this.date = date;
        }
        public Date getTime() {
            return time;
        }
        public void setTime(Date time) {
            this.time = time;
        }
        public double getNumberSeen() {
            return numberSeen;
        }
        public void setNumberSeen(double numberSeen) {
            this.numberSeen = numberSeen;
        }
        public String getNotes() {
            return notes;
        }
        public void setNotes(String notes) {
            this.notes = notes;
        }
        public double getSurveyAttr1() {
            return surveyAttr1;
        }
        public void setSurveyAttr1(double surveyAttr1) {
            this.surveyAttr1 = surveyAttr1;
        }
        public String getSurveyAttr2() {
            return surveyAttr2;
        }
        public void setSurveyAttr2(String surveyAttr2) {
            this.surveyAttr2 = surveyAttr2;
        }
        public double getCm1Attr1() {
            return cm1Attr1;
        }
        public void setCm1Attr1(double cm1Attr1) {
            this.cm1Attr1 = cm1Attr1;
        }
        public String getCm1Attr2() {
            return cm1Attr2;
        }
        public void setCm1Attr2(String val) {
            this.cm1Attr2 = val;
        }
        public double getCm2Attr1() {
            return cm2Attr1;
        }
        public void setCm2Attr1(double cm2Attr1) {
            this.cm2Attr1 = cm2Attr1;
        }
        public String getCm2Attr2() {
            return cm2Attr2;
        }
        public void setCm2Attr2(String cm2Attr) {
            this.cm2Attr2 = cm2Attr;
        }
        
        public boolean getIdAsNumber() {
            return this.idAsNumber;
        }
        public void setIdAsNumber(boolean value) {
            this.idAsNumber = value;
        }
    }
    
    // for debugging obviously...
    private void logRecordAttributes(List<AttributeValue> recAttrList) {
        log.debug("actual record attributes:");
        for (AttributeValue recAttr : recAttrList) {
            Attribute attr = recAttr.getAttribute();
            switch (attr.getType()) {
            case INTEGER:
            case DECIMAL:
                //recAttr.setNumericValue(new BigDecimal(attributeValue));
                log.debug(recAttr.getAttribute().getDescription() + " : " + recAttr.getNumericValue());
                break;
            case DATE:
                //SimpleDateFormat dateFormat = new SimpleDateFormat(
                 //       "dd MMM yyyy");
                //dateFormat.setLenient(false);
                //recAttr.setDateValue(dateFormat.parse(attributeValue));
                log.debug(recAttr.getAttribute().getDescription() + " : " + recAttr.getDateValue());
                break;
            case IMAGE:
            case FILE:
                throw new UnsupportedOperationException(
                        "Spreadsheet upload of file data is not supported.");
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case STRING:
            default:
                //recAttr.setStringValue(attributeValue);
                log.debug(recAttr.getAttribute().getDescription() + " : " + recAttr.getStringValue());
                break;
            }
        }
        log.debug("end actual record attributes");
    }
    
    private Record getRecordByCensusMethod(List<Record> recList, CensusMethod cm) {
        for (Record r : recList) {
            if (r.getCensusMethod().equals(cm)) {
                return r;
            }
        }
        return null;
    }
    
    private AttributeValue getRecAttr(Record rec, CensusMethod cm, String desc) {
        return getRecAttr(rec, getAttributeByDesc(cm, desc));
    }
    
    private AttributeValue getRecAttr(Record rec, Survey survey, String desc) {
        return getRecAttr(rec, getAttributeByDesc(survey, desc));
    }
    
    private AttributeValue getRecAttr(Record rec, Attribute attr) {
        for (AttributeValue ra : rec.getAttributes()) {
            if (ra.getAttribute().equals(attr)) {
                return ra;
            }
        }
        return null;
    }
    
    private Attribute getAttributeByDesc(CensusMethod cm, String desc) {
        return getAttributeByDesc(cm.getAttributes(), desc);
    }
    
    private Attribute getAttributeByDesc(Survey survey, String desc) {
        return getAttributeByDesc(survey.getAttributes(), desc);
    }
    
    private Attribute getAttributeByDesc(List<Attribute> attrList, String desc) {
        for (Attribute a : attrList) {
            if (desc.equals(a.getDescription())) {
                return a;
            }
        }
        return null;
    }
    
    private CensusMethod createCensusMethod(String name, Taxonomic taxonomic) {
        CensusMethod cm = new CensusMethod();
        cm.setName(name);
        cm.setTaxonomic(taxonomic);
        cm.setDescription("census method description");
        cm.setType("census method type");
        return cmDAO.save(cm);
    }
    
    private Attribute createAttribute(String name, String desc, boolean required, AttributeScope scope, boolean tag, String typecode) {
        Attribute a = new Attribute();
        a.setName(name);
        a.setDescription(desc);
        a.setRequired(required);
        a.setScope(scope);
        a.setTag(tag);
        a.setTypeCode(typecode);
        return attrDAO.save(a);
    }
}
