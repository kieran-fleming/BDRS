package au.com.gaiaresources.bdrs.service.bulkdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyService;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

public class XlsRecordRow implements RecordRow {

    private Logger log = Logger.getLogger(getClass());

    private LinkedHashMap<String, String> headerMap;

    // must be a tree map so we can order the keys
    private TreeMap<Integer, String> namespaceHeaderIdx = new TreeMap<Integer, String>();
    private List<String> attributeHeader = new ArrayList<String>();

    private List<String> completeHeader = null;

    private Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();
    
    private SurveyService surveyService;
    
    private BulkDataReadWriteService bdrws;
    
    private Set<CensusMethod> censusMethods;

    public XlsRecordRow(PropertyService propertyService, SurveyService surveyService, BulkDataReadWriteService bulkDataReadWriteService) {
        this.surveyService = surveyService;
        this.bdrws = bulkDataReadWriteService;
        
        LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
        
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.id", "ID"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.id", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.parentId", "Parent ID"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.parentId", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.censusMethod", "Census Method"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.CensusMethod", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.scientificName", "SCIENTIFIC NAME"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.scientificName", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.commonName", "COMMON NAME"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.commonName", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.locationName", "LOCATION NAME"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.locationName", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.latitude", "LATITUDE"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.latitude", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.longitude", "LONGITUDE"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.longitude", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.date", "DATE"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.date", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.time", "TIME"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.time", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.numberSeen", "NUMBER SEEN"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.numberSeen", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.notes", "NOTES"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.notes", ""));

        this.setHeaderMap(headerMap);
    }

    protected void setHeaderMap(LinkedHashMap<String, String> headerMap) {
        this.headerMap = headerMap;
    }

    public void writeHeader(Row superHeaderRow, Row row, Survey survey) {
        
        censusMethods = surveyService.catalogCensusMethods(survey);

        Cell headerCell;
        CellStyle recordHeaderStyle = getCellStyleByKey(STYLE_RECORD_HEADER);

        int colIndex = 0;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            if (superHeaderRow.getCell(colIndex) == null) {
                Cell superRowCell = superHeaderRow.createCell(colIndex);
                setCellStyle(superRowCell, recordHeaderStyle);
            }
            
            headerCell = row.createCell(colIndex++);
            headerCell.setCellValue(entry.getKey());
            setCellStyle(headerCell, recordHeaderStyle);
        }
        colIndex = writeAttributeHeader(survey.getAttributes(), superHeaderRow, row, colIndex, recordHeaderStyle);
        
        for (CensusMethod cm : censusMethods) {
            Cell censusMethodTitleCell = superHeaderRow.createCell(colIndex);
            censusMethodTitleCell.setCellValue(bdrws.formatCensusMethodNameId(cm));
            setCellStyle(censusMethodTitleCell, recordHeaderStyle);
            colIndex = writeAttributeHeader(cm.getAttributes(), superHeaderRow, row, colIndex, recordHeaderStyle);
        }
    }
    
    private int writeAttributeHeader(List<Attribute> attrList, Row superHeaderRow, Row row, int colIndex, CellStyle recordHeaderStyle) {
        for (Attribute attrib : attrList) {
            if (!AttributeType.FILE.equals(attrib.getType())
                    && !AttributeType.IMAGE.equals(attrib.getType())) {
                
                if (superHeaderRow.getCell(colIndex) == null) {
                    Cell superRowCell = superHeaderRow.createCell(colIndex);
                    setCellStyle(superRowCell, recordHeaderStyle);
                }
                
                String headerName = attrib.getDescription();
                Cell headerCell = row.createCell(colIndex++);
                headerCell.setCellValue(headerName);
                setCellStyle(headerCell, recordHeaderStyle);
                attributeHeader.add(headerName);
            }
        }
        return colIndex;
    }
    
    private void setCellStyle(Cell cell, CellStyle style) {
        if (style != null) {
            cell.setCellStyle(style);
        }
    }


    public void writeRow(LSIDService lsidService, Row row, Record rec) {
        int colIndex = 0;

        colIndex = writeRowId(lsidService, row, rec, colIndex);
        colIndex = writeRowParentId(lsidService, row, rec, colIndex);
        colIndex = writeRowCensusMethod(row, rec, colIndex);

        if (rec.getSpecies() != null) {
            colIndex = writeRowTaxonomy(row, rec, colIndex);
        } else {
            colIndex = writeRowBlank(row, colIndex);
        }

        // Location, lat, long
        colIndex = writeRowLocation(row, rec, colIndex);
        colIndex = writeRowLatitude(row, rec, colIndex);
        colIndex = writeRowLongitude(row, rec, colIndex);

        // Date Time
        colIndex = writeRowDate(row, rec, colIndex);
        colIndex = writeRowTime(row, rec, colIndex);
        if (rec.getNumber() != null) {
            colIndex = writeRowCount(row, rec, colIndex);
        }
        colIndex = writeRowNotes(row, rec, colIndex);
        colIndex = writeRowAttributes(row, rec, colIndex);
    }

    protected int writeRowNotes(Row row, Record rec, int colIndex) {
        row.createCell(colIndex++).setCellValue(rec.getNotes());
        return colIndex;
    }
    
    protected int writeRowParentId(LSIDService lsidService, Row row, Record rec, int colIndex) {
        String value = rec.getParentRecord() != null ? 
                lsidService.toLSID(rec.getParentRecord()).toString() :
                    "";
        row.createCell(colIndex++).setCellValue(value);
        return colIndex;
    }
    
    protected int writeRowCensusMethod(Row row, Record rec, int colIndex) {
        String value = rec.getCensusMethod() != null ?
                bdrws.formatCensusMethodNameId(rec.getCensusMethod()) :
                    "";
        row.createCell(colIndex++).setCellValue(value);
        return colIndex;
    }

    protected int writeRowCount(Row row, Record rec, int colIndex) {
        if (rec.getNumber() != null) {
            row.createCell(colIndex++).setCellValue(rec.getNumber());
        } else {
            row.createCell(colIndex++);
        }
        return colIndex;
    }

    protected int writeRowTime(Row row, Record rec, int colIndex) {
        Cell time = row.createCell(colIndex++);
        time.setCellStyle(getCellStyleByKey(STYLE_TIME_CELL));
        if (rec.getTimeAsDate() != null) {
            time.setCellValue(rec.getTimeAsDate());
        }
        return colIndex;
    }

    protected int writeRowDate(Row row, Record rec, int colIndex) {
        Cell when = row.createCell(colIndex++);
        when.setCellStyle(getCellStyleByKey(STYLE_DATE_CELL));
        when.setCellValue(rec.getWhen());
        return colIndex;
    }

    protected int writeRowLongitude(Row row, Record rec, int colIndex) {
        double longitude;
        if (rec.getLocation() == null) {
            longitude = rec.getPoint().getX();
        } else {
            longitude = rec.getLocation().getLocation().getX();
        }
        row.createCell(colIndex++).setCellValue(longitude);
        return colIndex;
    }

    protected int writeRowLatitude(Row row, Record rec, int colIndex) {
        double latitude;
        if (rec.getLocation() == null) {
            latitude = rec.getPoint().getY();
        } else {
            latitude = rec.getLocation().getLocation().getY();
        }
        row.createCell(colIndex++).setCellValue(latitude);
        return colIndex;
    }

    protected int writeRowLocation(Row row, Record rec, int colIndex) {
        if (rec.getLocation() == null) {
            row.createCell(colIndex++).setCellValue(GPS_LOCATION);
        } else {
            row.createCell(colIndex++).setCellValue(rec.getLocation().getName());
        }
        return colIndex;
    }

    protected int writeRowTaxonomy(Row row, Record rec, int colIndex) {
        IndicatorSpecies species = rec.getSpecies();
        String commonName = species.getCommonName() == null ? ""
                : species.getCommonName();
        row.createCell(colIndex++).setCellValue(species.getScientificName());
        row.createCell(colIndex++).setCellValue(commonName);
        return colIndex;
    }

    protected int writeRowId(LSIDService lsidService, Row row, Record rec,
            int colIndex) {
        row.createCell(colIndex++).setCellValue(lsidService.toLSID(rec).toString());
        return colIndex;
    }
    
    protected int writeRowBlank(Row row, int colIndex) {
        row.createCell(colIndex++).setCellValue("");
        return colIndex;
    }

    protected int writeRowAttributes(Row row, Record rec, int colIndex) {

        Map<String, AttributeValue> recordAttributeMap = new HashMap<String, AttributeValue>();
        for (AttributeValue attr : rec.getAttributes()) {
            recordAttributeMap.put(attr.getAttribute().getDescription(), attr);
        }

        Cell cell;
        TypedAttributeValue attr;
        for (String header : attributeHeader) {
            cell = row.createCell(colIndex++);
            attr = recordAttributeMap.get(header);
            if (attr != null) {
                //val = attr.getStringValue();
                switch (attr.getAttribute().getType()) {
                case INTEGER:
                case INTEGER_WITH_RANGE:
                case DECIMAL:
                    if (attr.getNumericValue() != null) {
                        cell.setCellValue(attr.getNumericValue().doubleValue());
                    }
                    break;
                case DATE:
                    if (attr.getDateValue() != null) {
                        cell.setCellValue(attr.getDateValue());
                        cell.setCellStyle(getCellStyleByKey(STYLE_DATE_CELL));
                    }
                    break;
                case IMAGE:
                case FILE:
                    throw new UnsupportedOperationException(
                            "Spreadsheet upload of file data is not supported.");
                case TEXT:
                case STRING_WITH_VALID_VALUES:
                case STRING:
                default:
                    if (attr.getStringValue() != null) {
                        cell.setCellValue(attr.getStringValue());
                    }
                    break;
                }
            }
        }
        return colIndex;
    }

    public void readHeader(Survey survey, Row superHeaderRow, Row row) throws ParseException {
        int colIndex = 0;

        censusMethods = surveyService.catalogCensusMethods(survey);
        
        String headerName;
        String cellValue;
        completeHeader = new ArrayList<String>();

        // Check core fields
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            headerName = entry.getKey();

            cellValue = row.getCell(colIndex++).getStringCellValue();
            if (!headerName.equals(cellValue)) {
                log.warn(String.format("Unexpected header value expected=\"%s\" actual=\"%s\"", headerName, cellValue));
                // The header does not exactly match what we expect
                throw new ParseException("Unexpected header value \""
                        + cellValue + "\"", colIndex);
            }
            completeHeader.add(headerName);
        }

        // Check attribute fields
        for (Attribute attrib : survey.getAttributes()) {
            if (!AttributeType.FILE.equals(attrib.getType())
                    && !AttributeType.IMAGE.equals(attrib.getType())) {
                
                Cell namespaceCell = superHeaderRow.getCell(colIndex);
                if (namespaceCell != null) {
                    String value = namespaceCell.getStringCellValue();
                    if (StringUtils.hasLength(value)) {
                        namespaceHeaderIdx.put(colIndex, namespaceCell.getStringCellValue());
                    }
                }

                headerName = attrib.getDescription();
                cellValue = row.getCell(colIndex++).getStringCellValue();
                if (!headerName.equals(cellValue)) {
                    // The header does not exactly match what we expect
                    throw new ParseException("Unexpected header value \""
                            + cellValue + "\"", colIndex);
                }
                attributeHeader.add(headerName);
                completeHeader.add(headerName);
            }
        }
        
        for (CensusMethod cm : censusMethods) {
            for (Attribute attrib : cm.getAttributes()) {
                if (!AttributeType.FILE.equals(attrib.getType())
                        && !AttributeType.IMAGE.equals(attrib.getType())) {
                    
                    Cell namespaceCell = superHeaderRow.getCell(colIndex);
                    if (namespaceCell != null) {
                        namespaceHeaderIdx.put(colIndex, namespaceCell.getStringCellValue());
                    }

                    headerName = attrib.getDescription();
                    cellValue = row.getCell(colIndex++).getStringCellValue();
                    if (!headerName.equals(cellValue)) {
                        // The header does not exactly match what we expect
                        throw new ParseException("Unexpected header value \""
                                + cellValue + "\"", colIndex);
                    }
                    attributeHeader.add(headerName);
                    completeHeader.add(headerName);
                }
            }
        }
    }
    
    
    public boolean isHeader(Row row) {
        Cell cell = row.getCell(0);
        boolean isHeader = false;
        if(Cell.CELL_TYPE_STRING == cell.getCellType()) {
            String firstHeaderCellValue = headerMap.entrySet().iterator().next().getKey();
            log.debug(firstHeaderCellValue +" == "+cell.getStringCellValue());
            isHeader = cell.getStringCellValue().equals(firstHeaderCellValue);
        }
        return isHeader;
    }

    public RecordUpload readRow(Survey survey, Row row) {
        RecordUpload recUpload = new RecordUpload();

        recUpload.setRowNumber(row.getRowNum());
        
        censusMethods = surveyService.catalogCensusMethods(survey);
        
        // Set the defaults
        recUpload.setHeld(false);
        recUpload.setFirstAppearance(false);
        recUpload.setLastAppearance(false);
        recUpload.setBehaviour("");
        recUpload.setHabitat("");

        int colIndex = 0;

        try {
            colIndex = 0;

            colIndex = readRowId(row, recUpload, colIndex);
            colIndex = readRowParentId(row, recUpload, colIndex);
            colIndex = readRowCensusMethodId(row, recUpload, colIndex);
            colIndex = readRowTaxonomy(row, recUpload, colIndex);
            colIndex = readRowLocation(survey, row, recUpload, colIndex);

            colIndex = readRowLatitude(row, recUpload, colIndex);
            colIndex = readRowLongitude(row, recUpload, colIndex);
            if (!recUpload.hasLatitudeLongitude()
                    && recUpload.isGPSLocationName()) {
                throw new IllegalArgumentException(
                        "A latitude and longitude and/or location name is required.");
            }

            colIndex = readRowDate(row, recUpload, colIndex);
            colIndex = readRowTime(row, recUpload, colIndex);
            colIndex = readRowCount(row, recUpload, colIndex);
            colIndex = readRowNotes(row, recUpload, colIndex);
            colIndex = readRowAttributes(survey, row, recUpload, colIndex);

        } catch (Exception e) {
            recUpload.setError(true);
            String msg = e.getMessage() == null ? e.toString() : e.getMessage();
            
            log.debug(" ------ ");
            log.debug(msg);
            log.debug(e.toString());
            
            recUpload.setErrorMessage("Column " + colIndex + " Row " + row.getRowNum() + " [ "
                    + getCompleteHeader().get(colIndex) + " ]: " + msg);
            
            log.debug(recUpload.getErrorMessage());
            
            log.warn(e.toString(), e);
        }
        //log.debug(recUpload);
        log.debug(recUpload.isError());
        return recUpload;
    }

    public static final String SURVEY_ATTR_NAMESPACE = "survey";
    
    protected int readRowAttributes(Survey survey, Row row,
            RecordUpload recUpload, int colIndex) {
        
        Cell cell;
        Map<String, Map<String, Attribute>> namedAttributeMap = new HashMap<String, Map<String, Attribute>>();
        
        //Map<String, Attribute> attributeMap = new HashMap<String, Attribute>();
        namedAttributeMap.put(SURVEY_ATTR_NAMESPACE, new HashMap<String, Attribute>());
        for (Attribute attr : survey.getAttributes()) {
            //attributeMap.put(attr.getDescription(), attr);
            //namedAttributeMap.put()
            namedAttributeMap.get(SURVEY_ATTR_NAMESPACE).put(attr.getDescription(), attr);
        }
        
        for (CensusMethod cm : censusMethods) {
            String namespace = bdrws.formatCensusMethodNameId(cm);
            namedAttributeMap.put(namespace, new HashMap<String, Attribute>());
            for (Attribute attr : cm.getAttributes()) {
                //attributeMap.put(attr.getDescription(), attr);
                namedAttributeMap.get(namespace).put(attr.getDescription(), attr);
            }
        }
        

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        String attrValue;
        Attribute attr;
        
        String attributeNamespace = SURVEY_ATTR_NAMESPACE;
        for (String recordAttributeName : attributeHeader) {
            
            // set namespace
            if (StringUtils.hasLength(namespaceHeaderIdx.get(colIndex))) {
                attributeNamespace = namespaceHeaderIdx.get(colIndex);
            }
            
            attrValue = "";
            cell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
            
            attr = namedAttributeMap.get(attributeNamespace).get(recordAttributeName);
            
            if (Cell.CELL_TYPE_BLANK != cell.getCellType()) {
                switch (attr.getType()) {
                case INTEGER:
                case INTEGER_WITH_RANGE:
                case DECIMAL:
                    attrValue = new Double(cell.getNumericCellValue()).toString();
                    break;
                case DATE:
                    attrValue = dateFormat.format(cell.getDateCellValue());
                    break;
                case IMAGE:
                case FILE:
                    throw new UnsupportedOperationException(
                            "Spreadsheet upload of file data is not supported.");
                case TEXT:
                case STRING_WITH_VALID_VALUES:
                case STRING:
                default:
                    attrValue = cell.getStringCellValue();
                    break;
                }
            }

            //recUpload.setRecordAttribute(recordAttributeName, attrValue);
            recUpload.setNamedAttribute(attributeNamespace, recordAttributeName, attrValue);
        }
        return colIndex;
    }

    protected int readRowNotes(Row row, RecordUpload recUpload, int colIndex) {
        recUpload.setNotes(row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
        return colIndex;
    }

    protected int readRowCount(Row row, RecordUpload recUpload, int colIndex) {
        Cell cell;
        cell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        if (cell.getCellType() == Cell.CELL_TYPE_BLANK) {
            recUpload.setNumberSeen(null);
        } else {
            recUpload.setNumberSeen(new Double(cell.getNumericCellValue()).intValue());
        }
        return colIndex;
    }

    protected int readRowTime(Row row, RecordUpload recUpload, int colIndex) {
        Date time = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getDateCellValue();
        recUpload.setTime(time);
        if (time == null) {
            throw new IllegalArgumentException("This attribute is required.");
        }
        return colIndex;
    }

    protected int readRowDate(Row row, RecordUpload recUpload, int colIndex) {
        Date when = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getDateCellValue();
        recUpload.setWhen(when);
        if (when == null) {
            throw new IllegalArgumentException("This attribute is required.");
        }
        return colIndex;
    }

    protected int readRowLongitude(Row row, RecordUpload recUpload, int colIndex) {
        Cell cell;
        // Longitude
        cell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        double longitude = Double.NaN;
        if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
            longitude = cell.getNumericCellValue();
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException(
                        "Longitude must be between -180 and 180.");
            }
        }
        recUpload.setLongitude(longitude);
        return colIndex;
    }

    protected int readRowLatitude(Row row, RecordUpload recUpload, int colIndex) {
        Cell cell;
        // Latitude
        cell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        double latitude = Double.NaN;
        if (cell.getCellType() != Cell.CELL_TYPE_BLANK) {
            latitude = cell.getNumericCellValue();
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException(
                        "Latitude must be between -90 and 90.");
            }
        }
        recUpload.setLatitude(latitude);
        return colIndex;
    }

    protected int readRowLocation(Survey survey, Row row,
            RecordUpload recUpload, int colIndex) {
        String locationName = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getStringCellValue();
        recUpload.setLocationName(locationName);
        if (survey.isPredefinedLocationsOnly() && recUpload.isGPSLocationName()) {
            throw new IllegalArgumentException(
                    "This attribute must be a predefined location.");
        }
        return colIndex;
    }

    protected int readRowId(Row row, RecordUpload recUpload, int colIndex) {
        recUpload.setId(readStringFromCell(row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK)));
        if (recUpload.getId().isEmpty()) {
            recUpload.setId(String.format("Row %d", row.getRowNum() + 1));
        }
        return colIndex;
    }
    
    
    private String readStringFromCell(Cell cell) {
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_NUMERIC:
                // convert double to int, to Integer, to string lulz...
                return new Integer(((int)cell.getNumericCellValue())).toString();
            // some of these will still throw errors. I was only aiming at catching the
            // numeric case. Note we always cast it to an int.
            case Cell.CELL_TYPE_BLANK:
            case Cell.CELL_TYPE_BOOLEAN:
            case Cell.CELL_TYPE_ERROR:
            case Cell.CELL_TYPE_FORMULA:
            case Cell.CELL_TYPE_STRING:
            default:
            return cell.getStringCellValue();
        }
    }
    
    protected int readRowParentId(Row row, RecordUpload recUpload, int colIndex) {
        recUpload.setParentId(readStringFromCell(row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK)));
        return colIndex;        
    }
    
    protected int readRowCensusMethodId(Row row, RecordUpload recUpload, int colIndex) {
        recUpload.setCensusMethodId(row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
        return colIndex; 
    }

    protected int readRowTaxonomy(Row row, RecordUpload recUpload, int colIndex) {
        recUpload.setScientificName(row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
        recUpload.setCommonName(row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK).getStringCellValue());
        
        CensusMethod cm = findCensusMethod(recUpload.getCensusMethodId(), censusMethods);
        
        if (cm == null || Taxonomic.TAXONOMIC.equals(cm.getTaxonomic())) {
            if (!((recUpload.getScientificName() != null && !recUpload.getScientificName().isEmpty()) || (recUpload.getCommonName() != null && !recUpload.getCommonName().isEmpty()))) {
                throw new IllegalArgumentException(
                        "The scientific name or common name is required.");
            }
        }
        return colIndex;
    }
    
    private CensusMethod findCensusMethod(String censusMethodIdString, Set<CensusMethod> cmSet) {
        for (CensusMethod cm : cmSet) {
            if (StringUtils.hasLength(censusMethodIdString)) {
                if (cm.getId().equals(bdrws.parseCensusMethodId(censusMethodIdString))
                        && cm.getName().equals(bdrws.parseCensusMethodName(censusMethodIdString))) {
                    return cm;
                } 
            }
        }
        if (StringUtils.hasLength(censusMethodIdString)) {
                // no match but the string has length / is not null. invalid name or id
                throw new IllegalArgumentException(
                "The census method identifier: '" + censusMethodIdString + "' is invalid.");
        }
        return null;
    }

    @Override
    public int writeCoreHelp(Sheet helpSheet, int rowIndex) {
        Row row;
        int colIndex = 0;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            row = helpSheet.createRow(rowIndex++);
            colIndex = 0;
            row.createCell(colIndex++).setCellValue(entry.getKey());
            row.createCell(colIndex++).setCellValue(entry.getValue());
        }
        return rowIndex;
    }

    @Override
    public void createCellStyles(Workbook wb) {
        if (wb == null) {
            return;
        }
        CreationHelper createHelper = wb.getCreationHelper();

        // Headers
        CellStyle headerStyle = wb.createCellStyle();
        headerStyle.setFillBackgroundColor(IndexedColors.BLACK.getIndex());
        headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);

        Font font = wb.createFont();
        font.setBoldweight(Font.BOLDWEIGHT_BOLD);
        font.setColor(HSSFColor.WHITE.index);
        headerStyle.setFont(font);

        cellStyleMap.put(STYLE_HELP_HEADER, headerStyle);
        cellStyleMap.put(STYLE_RECORD_HEADER, headerStyle);
        cellStyleMap.put(STYLE_LOCATION_HEADER, headerStyle);
        cellStyleMap.put(STYLE_TAXONOMY_HEADER, headerStyle);

        // Date and Time
        CellStyle dateStyle = wb.createCellStyle();
        dateStyle.setDataFormat(createHelper.createDataFormat().getFormat("D MMM YYYY"));
        cellStyleMap.put(STYLE_DATE_CELL, dateStyle);

        CellStyle timeStyle = wb.createCellStyle();
        timeStyle.setDataFormat(createHelper.createDataFormat().getFormat("HH:MM"));
        cellStyleMap.put(STYLE_TIME_CELL, timeStyle);
    }
    
    protected void setCellStyleByKey(String styleKey, CellStyle style) {
        cellStyleMap.put(styleKey, style);
    }

    @Override
    public CellStyle getCellStyleByKey(String styleKey) {
        return cellStyleMap.get(styleKey);
    }
    
    public List<String> getCompleteHeader() {
        return Collections.unmodifiableList(completeHeader);
    }
}
