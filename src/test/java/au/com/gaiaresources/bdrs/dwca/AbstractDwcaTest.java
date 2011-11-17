package au.com.gaiaresources.bdrs.dwca;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Iterator;
import java.util.List;

import junit.framework.Assert;

import org.gbif.dwc.record.DarwinCoreRecord;
import org.gbif.dwc.terms.DwcTerm;
import org.gbif.dwc.text.Archive;
import org.gbif.dwc.text.StarRecord;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Holds common setup / helper methods for DwcA related tests 
 * 
 * @author aaron
 *
 */
public abstract class AbstractDwcaTest extends AbstractGridControllerTest {
       
    protected final DateFormat ISO8601Local = new SimpleDateFormat(RecordDwcaWriter.ISO_DATE_FORMAT);
    
    protected static final String APPLICATION_URL = REQUEST_SCHEME + "://" + REQUEST_SERVER_NAME + ":" + REQUEST_SERVER_PORT + "/" + REQUEST_CONTEXT_PATH;
    
    protected RedirectionService rdService = new RedirectionService(APPLICATION_URL);
    
    protected void assertDwcRecord(DarwinCoreRecord dwcr, StarRecord starRecord, List<Record> allRecords) {
        
        String catalogNumber = dwcr.getCatalogNumber();
        Record rec = getRecord(allRecords, catalogNumber);
        Assert.assertNotNull("rec cannot be null", rec);
        
        if (rec.getSpecies() != null) {
            if (rec.getSpecies().getScientificNameAndAuthor() != null) {
                Assert.assertEquals("species and author should match", rec.getSpecies().getScientificNameAndAuthor(), dwcr.getScientificName());    
            } else {
                Assert.assertEquals("species should match", rec.getSpecies().getScientificName(), dwcr.getScientificName());    
            }
            if (rec.getSpecies().getGuid() != null) {
                Assert.assertEquals("guid should match", rec.getSpecies().getGuid(), dwcr.getTaxonID());
                Assert.assertEquals("guid should match", rec.getSpecies().getGuid(), dwcr.getScientificNameID());
            } else {
                Assert.assertEquals("lsid should match", lsidService.toLSID(rec.getSpecies()).toString(), dwcr.getTaxonID());
                Assert.assertEquals("lsid should match", lsidService.toLSID(rec.getSpecies()).toString(), dwcr.getScientificNameID());
            }
        } else {
            Assert.assertFalse("scientific name should be empty or null", StringUtils.hasLength(dwcr.getScientificName()));
            Assert.assertFalse("taxon id lsid should be null", StringUtils.hasLength(dwcr.getTaxonID()));
            Assert.assertFalse("sci name id lsid should be null", StringUtils.hasLength(dwcr.getScientificNameID()));
        }
        
        if (rec.getLatitude() != null) {
            String expectedLat = String.format("%f", locService.truncate(rec.getLatitude().doubleValue()));
            Assert.assertEquals("lat should match", expectedLat, dwcr.getDecimalLatitude());    
        } else {
            Assert.assertFalse("lat should be empty", StringUtils.hasLength(dwcr.getDecimalLatitude()));
        }
        
        if (rec.getLongitude() != null) {
            String expectedLon = String.format("%f", locService.truncate(rec.getLongitude().doubleValue()));
            Assert.assertEquals("lon should match", expectedLon, dwcr.getDecimalLongitude());    
        } else {
            Assert.assertFalse("lon should be empty", StringUtils.hasLength(dwcr.getDecimalLongitude()));
        }
        
        Assert.assertEquals("survey lsid should match", lsidService.toLSID(rec.getSurvey()).toString(), dwcr.getDatasetID());
        Assert.assertEquals("recording user shoul match", rec.getUser().getFullName(), dwcr.getRecordedBy());
        if (rec.getNumber() != null) {
            Assert.assertEquals("individual count should match", rec.getNumber().toString(), dwcr.getIndividualCount());
        } else {
            Assert.assertFalse("individual count be empty", StringUtils.hasLength(dwcr.getIndividualCount()));
        }
        
        if (rec.getWhen() != null) {
            Assert.assertEquals("date should match", ISO8601Local.format(rec.getWhen()), dwcr.getEventDate());    
        } else {
            Assert.assertFalse("date should be empty", StringUtils.hasLength(dwcr.getEventDate()));
        }        
        
        Iterator<org.gbif.dwc.record.Record> starRecIter = starRecord.iterator();
        while (starRecIter.hasNext()) {
            org.gbif.dwc.record.Record starRecItem = starRecIter.next();
            
            Assert.assertEquals("rec ids should match", rec.getId().intValue(), Integer.parseInt(starRecItem.id()));
            
            AttributeValue av = getAttributeValueByAttributeId(rec.getAttributes(), starRecItem.value(DwcTerm.measurementID));
            Assert.assertNotNull("attribute value cannot be null", av);
            
            Assert.assertTrue("only populated attribute values should be in the star record", av.isPopulated());
            
            if (rec.getCensusMethod() != null) {
                Assert.assertEquals("census method name must match", rec.getCensusMethod().getName(), starRecItem.value(DwcTerm.measurementMethod));
            }
            
            Assert.assertEquals("description must match", av.getAttribute().getDescription(), starRecItem.value(DwcTerm.measurementType));
            
            StringBuilder remarkBuilder = new StringBuilder();
            remarkBuilder.append("survey: ");
            remarkBuilder.append(rec.getSurvey().getName());
            if (rec.getCensusMethod() != null) {
                remarkBuilder.append(", census method: ");
                remarkBuilder.append(rec.getCensusMethod().getName());
            }
            Assert.assertEquals("remarks must match", remarkBuilder.toString(), starRecItem.value(DwcTerm.measurementRemarks));

            Assert.assertEquals("values must match", getExpectedValue(av), starRecItem.value(DwcTerm.measurementValue));
        }
    }
    
    protected String getExpectedValue(AttributeValue av) {           
        Attribute a = av.getAttribute();
        switch (a.getType()) {
        case INTEGER:
        case INTEGER_WITH_RANGE:
            return av.getNumericValue() != null ? Integer.toString(av.getNumericValue().intValue()) : "";
            
        case DECIMAL:
            return av.getNumericValue() != null ? av.getNumericValue().toString() : "";
        
        case DATE:
            return av.getDateValue() != null ? av.getDateValue().toString() : "";
        case REGEX:
        case BARCODE:
        case TIME:
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case STRING_WITH_VALID_VALUES:
        case MULTI_CHECKBOX:
        case MULTI_SELECT:
        case SINGLE_CHECKBOX:
            return av.getStringValue() != null ? av.getStringValue() : "";
            
        case HTML:
        case HTML_COMMENT:
        case HTML_HORIZONTAL_RULE:
            Assert.fail("HTML elements should not be exported in DwC-A");
            
        case IMAGE:
        case FILE:
            return APPLICATION_URL + "/files/download.htm?" + av.getFileURL();
            
        default:
            throw new IllegalStateException("Type not handled : " + a.getTypeCode());
        }
    }
    
    private Record getRecord(List<Record> recList, String catalogNumber) {
        // catalog number is mapped to record Id so...
        int id = Integer.parseInt(catalogNumber);
        return recordDAO.getRecord(id);
    }
    
    protected int countArchivedRecords(List<Record> recList) {
        int count = 0;
        for (Record rec : recList) {
            if (isArchived(rec)) {
                ++count;
            }
        }
        return count;
    }
    
    /**
     * records must be taxonomic AND public
     * 
     * @param rec
     * @return
     */
    protected boolean isArchived(Record rec) {
        return rec.getRecordVisibility() == RecordVisibility.PUBLIC;
    }
    
    /**
     * Gets the extension records associated with a row in the core file
     * 
     * @param archive
     * @param coreId
     * @return
     */
    protected StarRecord getStarRecord(Archive archive, String coreId) {
        Iterator<StarRecord> starIter = archive.iterator();
        while (starIter.hasNext()) {
            StarRecord sr = starIter.next();
            if (sr.core().id().equals(coreId)) {
                return sr;
            }
        }
        return null;
    }
}
