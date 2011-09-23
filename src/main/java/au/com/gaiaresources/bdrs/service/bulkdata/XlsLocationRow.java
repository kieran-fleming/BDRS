package au.com.gaiaresources.bdrs.service.bulkdata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import au.com.gaiaresources.bdrs.controller.insecure.taxa.ComparePersistentImplByWeight;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

/**
 * Performs the reading and writing of XLS rows representing {@link Location}s.
 */
public class XlsLocationRow extends StyledRowImpl {
    
    private Logger log = Logger.getLogger(getClass());
    
    private BulkDataReadWriteService bulkDataReadWriteService;
    private Survey survey;
    
    private List<Attribute> locationScopeAttrList = null;

    private Cell currentReadCell;
    
    /**
     * Create a new instance.
     * @param bulkDataReadWriteService used for writing attribute values to cells.
     * @param survey the survey containing the locations to be stored. User locations
     * referenced by Records contained by this survey shall also be stored.
     */
    public XlsLocationRow(BulkDataReadWriteService bulkDataReadWriteService, Survey survey) {
        this.bulkDataReadWriteService = bulkDataReadWriteService;
        this.survey = survey;
    }
    
    // ---------------------------
    // Writing Methods
    // ---------------------------
    /**
     * Creates and populates the location sheet in the specified workbook.
     * @param recordList the list of records containing user locations to be stored.
     * @param wb the workbook where the location sheet shall be created.
     */
    public void writeLocationSheet(List<Record> recordList, Workbook wb) {
        
        super.createCellStyles(wb);
        Sheet locSheet = wb.createSheet(AbstractBulkDataService.LOCATION_SHEET_NAME);

        int rowIndex = 0; 
        List<Attribute> locationScopeAttrs = getLocationScopeAttributes(survey);
        rowIndex = writeLocationHeader(locSheet, rowIndex, locationScopeAttrs);
        
        for (Location loc : survey.getLocations()) {
            Row row = locSheet.createRow(rowIndex++);
            int colIndex = 0;

            row.createCell(colIndex++).setCellValue(loc.getId());
            row.createCell(colIndex++).setCellValue(AbstractBulkDataService.LOCATION_SHEET_SURVEY_LOCATION);
            row.createCell(colIndex++).setCellValue(loc.getName());
            row.createCell(colIndex++).setCellValue(loc.getLocation().getCentroid().getY());
            row.createCell(colIndex++).setCellValue(loc.getLocation().getCentroid().getX());
            
            // Location Attribute Values
            Map<Attribute, AttributeValue> locAttrValMap = new HashMap<Attribute, AttributeValue>();
            for(AttributeValue attrVal : loc.getAttributes()) {
                locAttrValMap.put(attrVal.getAttribute(), attrVal);
            }
            
            for(Attribute attr : locationScopeAttrs) {
                AttributeValue attrVal = locAttrValMap.get(attr);
                bulkDataReadWriteService.writeTypedAttributeValueCell(this,
                                                                      row.createCell(colIndex++), 
                                                                      attrVal);
            }
        }
        
        // Now add user locations
        for (Record rec : recordList) {
            // If the record has a location
            if(rec.getLocation() != null) {
                Location loc = rec.getLocation();
                // and the location is not a survey location
                if(!survey.getLocations().contains(loc)) {
                    Row row = locSheet.createRow(rowIndex++);
                    int colIndex = 0;
        
                    row.createCell(colIndex++).setCellValue(loc.getId());
                    row.createCell(colIndex++).setCellValue(AbstractBulkDataService.LOCATION_SHEET_USER_LOCATION);
                    row.createCell(colIndex++).setCellValue(loc.getName());
                    row.createCell(colIndex++).setCellValue(loc.getLocation().getCentroid().getY());
                    row.createCell(colIndex++).setCellValue(loc.getLocation().getCentroid().getX());
                    
                    // user locations do not have any location attribute values.
                    // if that changes. add that stuff here.
                }
            }
        }
    }
    
    private int writeLocationHeader(Sheet locSheet, int rowIndex, List<Attribute> locationScopeAttrs) {
        CellStyle headerStyle = getCellStyleByKey(STYLE_LOCATION_HEADER);
        int colIndex = 0;
        Cell cell;
        Row row = locSheet.createRow(rowIndex++);
        
        cell = row.createCell(colIndex++);
        cell.setCellValue(AbstractBulkDataService.LOCATION_SHEET_LOCATION_ID);
        cell.setCellStyle(headerStyle);

        cell = row.createCell(colIndex++);
        cell.setCellValue(AbstractBulkDataService.LOCATION_SHEET_LOCATION_TYPE);
        cell.setCellStyle(headerStyle);
        
        cell = row.createCell(colIndex++);
        cell.setCellValue("Location Name");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(colIndex++);
        cell.setCellValue("Latitude");
        cell.setCellStyle(headerStyle);

        cell = row.createCell(colIndex++);
        cell.setCellValue("Longitude");
        cell.setCellStyle(headerStyle);
        
        for(Attribute attr : locationScopeAttrs) {
            cell = row.createCell(colIndex++);
            cell.setCellValue(attr.getDescription());
            cell.setCellStyle(headerStyle);
        }
        
        return rowIndex;
    }
    
    // ---------------------------
    // Reading Methods
    // ---------------------------
    /**
     * Parses a single location row returning a representation of the 
     * contained {@link Location} as a {@link LocationUpload}.
     * @param row the xls row containing the representation of the location.
     * @return a LocationUpload representing the location in the row.
     */
    public LocationUpload readRow(Row row) {
        
        currentReadCell = null;
        LocationUpload locationUpload = new LocationUpload();
        
        try {
            
            int colIndex = 0;
            currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
            if(Cell.CELL_TYPE_BLANK != currentReadCell.getCellType()) {
                locationUpload.setPk((int)XlsCellUtil.cellToDouble(currentReadCell));
            }
            
            // Skip over the location type. We don't need it.
            colIndex++;
            
            currentReadCell = row.getCell(colIndex++);
            locationUpload.setLocationName(XlsCellUtil.cellToString(currentReadCell));
            
            currentReadCell = row.getCell(colIndex++);
            locationUpload.setLatitude(XlsCellUtil.cellToDouble(currentReadCell));
            
            currentReadCell = row.getCell(colIndex++);
            locationUpload.setLongitude(XlsCellUtil.cellToDouble(currentReadCell));
            
            locationUpload.setSurveyName(survey.getName());
            
            colIndex = readLocationAttributeValue(locationUpload, row, colIndex);
                                                      
        } catch (Exception e) {
            
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            
            // While cell **could** be null, if it is null, we haven't managed
            // to read anything ever. Which almost defeats the purpose of this
            // error handler.
            String value = currentReadCell.toString();
            String cellColRef = CellReference.convertNumToColString(currentReadCell.getColumnIndex());
            String sheetName = currentReadCell.getSheet().getSheetName();
            
            String errMsg = String.format("Cell %s!%s%d[ value=\"%s\" ]: %s %s", sheetName, cellColRef, currentReadCell.getRowIndex() + 1, value, e.getClass().getSimpleName(), msg);

            locationUpload.setError(true);
            locationUpload.setErrorMessage(errMsg);
            
            log.debug(errMsg);
            log.warn(e.toString(), e);
        }
        return locationUpload;
    }
    
    private int readLocationAttributeValue(LocationUpload locationUpload, Row row, int colIndex) {
        for (Attribute attr : getLocationScopeAttributes(survey)) {
            String attrValue = null;
            currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
            
            if(Cell.CELL_TYPE_BLANK == currentReadCell.getCellType()) {
                // Ideally we would delete the attribute value if there was one.
                attrValue = "";
            } else {
                switch (attr.getType()) {
                    case INTEGER:
                    case INTEGER_WITH_RANGE:
                    case DECIMAL:
                        attrValue = String.valueOf(XlsCellUtil.cellToDouble(currentReadCell));
                        break;
                    case DATE:
                        // Perhaps we shouldn't be using that particular date format.
                        // It should be referring to the 'canonical' way that we 
                        // convert dates to strings.
                        Date d = XlsCellUtil.cellToDate(currentReadCell);
                        attrValue = XlsCellUtil.DATE_FORMATTER.format(d);
                        break;
                    case IMAGE:
                    case FILE:
                        throw new UnsupportedOperationException(
                                "Spreadsheet upload of file data is not supported.");
                    case TEXT:
                    case STRING_WITH_VALID_VALUES:
                    case STRING:
                    case HTML:
                    case HTML_COMMENT:
                    case HTML_HORIZONTAL_RULE:
                    default:
                        attrValue = XlsCellUtil.cellToString(currentReadCell);
                        break;
                }
            }
            
            locationUpload.setAttributeValue(attr, attrValue);
        }
        return colIndex;
    }
    
    // ---------------------------
    // Utility Methods
    // ---------------------------
    /** 
     * Returns true if the specified row is a header row, otherwise false.
     * @Row row the row to be interrogated.
     * @returns true if the specified row is a header row, false otherwise.
     */
    public boolean isHeader(Row row) {
        return row.getRowNum() == 0;
    }
    
    private List<Attribute> getLocationScopeAttributes(Survey survey) {
        
        if(locationScopeAttrList == null) {
            List<Attribute> attrList = new ArrayList<Attribute>(survey.getAttributes());
            Collections.sort(attrList, new ComparePersistentImplByWeight());
            
            List<Attribute> locationScopeAttrs = new ArrayList<Attribute>();
            for(Attribute attr : attrList) {
                if(AttributeScope.LOCATION.equals(attr.getScope())) {
                    locationScopeAttrs.add(attr);
                }
            }
            
            locationScopeAttrList = Collections.unmodifiableList(locationScopeAttrs);
        }
        
        return locationScopeAttrList;
    }
}