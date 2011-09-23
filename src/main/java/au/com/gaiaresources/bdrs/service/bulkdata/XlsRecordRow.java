package au.com.gaiaresources.bdrs.service.bulkdata;

import java.text.ParseException;
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
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.controller.insecure.taxa.ComparePersistentImplByWeight;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyService;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.service.property.PropertyService;

public class XlsRecordRow extends StyledRowImpl implements RecordRow {

    private Logger log = Logger.getLogger(getClass());
    
    private LinkedHashMap<String, String> headerMap;

    // must be a tree map so we can order the keys
    private TreeMap<Integer, String> namespaceHeaderIdx = new TreeMap<Integer, String>();
    private List<Attribute> headerAttributes = new ArrayList<Attribute>();

    private List<String> completeHeader = null;

    private SurveyService surveyService;
    
    private BulkDataReadWriteService bdrws;
    
    private Set<CensusMethod> censusMethods;

    private CensusMethodDAO censusMethodDAO;
    
    // The cell that is currently being processed when loading data
    private Cell currentReadCell;

    public XlsRecordRow(PropertyService propertyService, SurveyService surveyService, CensusMethodDAO censusMethodDAO,
                        BulkDataReadWriteService bulkDataReadWriteService) {
        this.surveyService = surveyService;
        this.censusMethodDAO = censusMethodDAO;
        this.bdrws = bulkDataReadWriteService;
        
        LinkedHashMap<String, String> headerMap = new LinkedHashMap<String, String>();
        
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.id", "ID"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.id", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.parentId", "Parent ID"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.parentId", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.censusMethod.id", "CENSUS METHOD ID"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.censusMethod.id", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.censusMethod", "CENSUS METHOD"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.censusMethod", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.scientificName", "SCIENTIFIC NAME"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.scientificName", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.commonName", "COMMON NAME"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.commonName", ""));
        headerMap.put(propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.location.id", "LOCATION ID"), propertyService.getPropertyValue(PropertyService.BULKDATA, "xls.row.header.bdrs.help.location.id", ""));
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
        List<Attribute> attributesForHeader = new ArrayList<Attribute>();
        for(Attribute a : survey.getAttributes()) {
            if(!AttributeScope.LOCATION.equals(a.getScope())) {
                attributesForHeader.add(a);
            }
        }
        colIndex = writeAttributeHeader(attributesForHeader, superHeaderRow, row, colIndex, recordHeaderStyle);
        
        for (CensusMethod cm : censusMethods) {
            if(!cm.getAttributes().isEmpty()) {
                Cell censusMethodTitleCell = superHeaderRow.createCell(colIndex);
                censusMethodTitleCell.setCellValue(bdrws.formatCensusMethodNameId(cm));
                setCellStyle(censusMethodTitleCell, recordHeaderStyle);
                colIndex = writeAttributeHeader(cm.getAttributes(), superHeaderRow, row, colIndex, recordHeaderStyle);
            }
        }
    }
    
    private int writeAttributeHeader(List<Attribute> attrList, Row superHeaderRow, Row row, int colIndex, CellStyle recordHeaderStyle) {
        
        List<Attribute> sortedAttributes = new ArrayList<Attribute>(attrList);
        Collections.sort(attrList, new ComparePersistentImplByWeight());
        
        for (Attribute attrib : sortedAttributes) {
            if (!AttributeType.FILE.equals(attrib.getType())
                    && !AttributeType.IMAGE.equals(attrib.getType())
                    && !AttributeType.HTML.equals(attrib.getType())
                    && !AttributeType.HTML_COMMENT.equals(attrib.getType())
                    && !AttributeType.HTML_HORIZONTAL_RULE.equals(attrib.getType())) {
                
                if (superHeaderRow.getCell(colIndex) == null) {
                    Cell superRowCell = superHeaderRow.createCell(colIndex);
                    setCellStyle(superRowCell, recordHeaderStyle);
                }
                
                String headerName = attrib.getDescription();
                Cell headerCell = row.createCell(colIndex++);
                headerCell.setCellValue(headerName);
                setCellStyle(headerCell, recordHeaderStyle);
                
                headerAttributes.add(attrib);
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
            // Two blank cells. One for the scientific name the other for the common name.
            colIndex = writeRowBlank(row, colIndex);
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
        } else {
            colIndex = writeRowBlank(row, colIndex);
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
        CensusMethod cm = rec.getCensusMethod();
        if(cm == null) {
            
            // Census Method ID
            colIndex = writeRowBlank(row, colIndex);
            
            // Census Method Name
            colIndex = writeRowBlank(row, colIndex);
            
        } else {
            // Census Method ID
            Cell methodIDCell = row.createCell(colIndex++);
            methodIDCell.setCellValue(cm.getId().doubleValue());
            String methodCellRefStr = new CellReference(methodIDCell.getRowIndex(),
                                                        methodIDCell.getColumnIndex()).formatAsString();
            
            Sheet cmSheet = row.getSheet().getWorkbook().getSheet(AbstractBulkDataService.CENSUS_METHOD_SHEET_NAME);
            
            Cell topLeftCell = XlsCellUtil.getTopLeftCell(cmSheet, 1);
            String topLeft = XlsCellUtil.cellToCellReferenceString(topLeftCell);
            
            Cell bottomRightCell = XlsCellUtil.getBottomRightCell(cmSheet);
            String bottomRight = XlsCellUtil.cellToCellReferenceString(bottomRightCell);
            
            String formula = String.format("VLOOKUP(%s,%s!%s:%s,%d,0)",
                                           methodCellRefStr, 
                                           AbstractBulkDataService.CENSUS_METHOD_SHEET_NAME, 
                                           topLeft, 
                                           bottomRight,
                                           2);
            row.createCell(colIndex++).setCellFormula(formula);
        }
        
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
            longitude = rec.getLocation().getLocation().getCentroid().getX();
        }
        row.createCell(colIndex++).setCellValue(longitude);
        return colIndex;
    }

    protected int writeRowLatitude(Row row, Record rec, int colIndex) {
        double latitude;
        if (rec.getLocation() == null) {
            latitude = rec.getPoint().getY();
        } else {
            latitude = rec.getLocation().getLocation().getCentroid().getY();
        }
        row.createCell(colIndex++).setCellValue(latitude);
        return colIndex;
    }

    protected int writeRowLocation(Row row, Record rec, int colIndex) {
        if (rec.getLocation() == null) {
            colIndex = writeRowBlank(row, colIndex);
            row.createCell(colIndex++).setCellValue(GPS_LOCATION);
        } else {
            Cell locIdCell = row.createCell(colIndex++);
            locIdCell.setCellValue(rec.getLocation().getId());

            String locCellRefStr = new CellReference(locIdCell.getRowIndex(),
                                                     locIdCell.getColumnIndex()).formatAsString();
            
            Sheet locSheet = row.getSheet().getWorkbook().getSheet(AbstractBulkDataService.LOCATION_SHEET_NAME);
            Cell topLeftCell = XlsCellUtil.getTopLeftCell(locSheet, 1);
            String topLeft = XlsCellUtil.cellToCellReferenceString(topLeftCell);
            
            Cell bottomRightCell = XlsCellUtil.getBottomRightCell(locSheet);
            String bottomRight = XlsCellUtil.cellToCellReferenceString(bottomRightCell);
            
            String formula = String.format("VLOOKUP(%s,%s!%s:%s,%d,0)",
                                           locCellRefStr, 
                                           AbstractBulkDataService.LOCATION_SHEET_NAME, 
                                           topLeft, 
                                           bottomRight,
                                           3);
            row.createCell(colIndex++).setCellFormula(formula);
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
        row.createCell(colIndex++);
        return colIndex;
    }

    protected int writeRowAttributes(Row row, Record rec, int colIndex) {

        Map<Attribute, AttributeValue> recordAttributeMap = new HashMap<Attribute, AttributeValue>();
        for (AttributeValue attrVal : rec.getAttributes()) {
            recordAttributeMap.put(attrVal.getAttribute(), attrVal);
        }

        Cell cell;
        TypedAttributeValue attr;
        for (Attribute headerAttr : headerAttributes) {
            cell = row.createCell(colIndex++);
            attr = recordAttributeMap.get(headerAttr);
            bdrws.writeTypedAttributeValueCell(this, cell, attr);
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
            if(!AttributeScope.LOCATION.equals(attrib.getScope())) {
                if (!AttributeType.FILE.equals(attrib.getType())
                        && !AttributeType.IMAGE.equals(attrib.getType())
                        && !AttributeType.HTML.equals(attrib.getType())
                        && !AttributeType.HTML_COMMENT.equals(attrib.getType())
                        && !AttributeType.HTML_HORIZONTAL_RULE.equals(attrib.getType())) {

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
                    headerAttributes.add(attrib);
                    completeHeader.add(headerName);
                }
            }
        }
        
        for (CensusMethod cm : censusMethods) {
            if(!cm.getAttributes().isEmpty()) {
                for (Attribute attrib : cm.getAttributes()) {
                    if (!AttributeType.FILE.equals(attrib.getType())
                            && !AttributeType.IMAGE.equals(attrib.getType())
                            && !AttributeType.HTML.equals(attrib.getType())
                            && !AttributeType.HTML_COMMENT.equals(attrib.getType())
                            && !AttributeType.HTML_HORIZONTAL_RULE.equals(attrib.getType())) {
                        
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
                        headerAttributes.add(attrib);
                        completeHeader.add(headerName);
                    }
                }
            }
        }
    }
    
    public boolean isHeader(Row row) {
        Cell cell = row.getCell(0, Row.CREATE_NULL_AS_BLANK);
        boolean isHeader = false;
        if(Cell.CELL_TYPE_STRING == cell.getCellType()) {
            String firstHeaderCellValue = headerMap.entrySet().iterator().next().getKey();
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

            // Skip over the census method name
            colIndex += 1;
            
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
            
            String value = currentReadCell.toString();
            String cellColRef = CellReference.convertNumToColString(currentReadCell.getColumnIndex());
            
            String errMsg = String.format("Cell %s%d[ value=\"%s\" ]: %s %s", cellColRef, currentReadCell.getRowIndex() + 1, value, e.getClass().getSimpleName(), msg);
            recUpload.setErrorMessage(errMsg);
            
            log.debug(recUpload.getErrorMessage());
            
            log.warn(e.toString(), e);
        }
        //log.debug(recUpload.isError());
        return recUpload;
    }

    public static final String SURVEY_ATTR_NAMESPACE = "survey";
    
    protected int readRowAttributes(Survey survey, Row row,
            RecordUpload recUpload, int colIndex) {
        
        Map<String, Map<String, Attribute>> namedAttributeMap = new HashMap<String, Map<String, Attribute>>();
        
        namedAttributeMap.put(SURVEY_ATTR_NAMESPACE, new HashMap<String, Attribute>());
        for (Attribute attr : survey.getAttributes()) {
            if(!AttributeScope.LOCATION.equals(attr.getScope())) {
                namedAttributeMap.get(SURVEY_ATTR_NAMESPACE).put(attr.getDescription(), attr);
            }
        }
        
        for (CensusMethod cm : censusMethods) {
            String namespace = bdrws.formatCensusMethodNameId(cm);
            namedAttributeMap.put(namespace, new HashMap<String, Attribute>());
            for (Attribute attr : cm.getAttributes()) {
                namedAttributeMap.get(namespace).put(attr.getDescription(), attr);
            }
        }
        
        String attrValue;
        Attribute attr;
        
        String attributeNamespace = SURVEY_ATTR_NAMESPACE;
        for (Attribute attrHeader : headerAttributes) {
            String recordAttributeName = attrHeader.getDescription();
            // set namespace
            if (StringUtils.hasLength(namespaceHeaderIdx.get(colIndex))) {
                attributeNamespace = namespaceHeaderIdx.get(colIndex);
            }
            
            attrValue = "";
            currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
            attr = namedAttributeMap.get(attributeNamespace).get(recordAttributeName);
            
            if (Cell.CELL_TYPE_BLANK != currentReadCell.getCellType()) {
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
                        //attrValue = cell.getStringCellValue();
                        attrValue = XlsCellUtil.cellToString(currentReadCell);
                        break;
                }
            }

            recUpload.setNamedAttribute(attributeNamespace, recordAttributeName, attrValue);
        }
        return colIndex;
    }

    protected int readRowNotes(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        recUpload.setNotes(currentReadCell.getStringCellValue());
        return colIndex;
    }

    protected int readRowCount(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        if (currentReadCell.getCellType() == Cell.CELL_TYPE_BLANK) {
            recUpload.setNumberSeen(null);
        } else {
            recUpload.setNumberSeen(new Double(XlsCellUtil.cellToDouble(currentReadCell)).intValue());
        }
        return colIndex;
    }

    protected int readRowTime(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        Date time = XlsCellUtil.cellToTime(currentReadCell);
        if (time == null) {
            throw new IllegalArgumentException("This attribute is required.");
        }
        recUpload.setTime(time);
        return colIndex;
    }

    protected int readRowDate(Row row, RecordUpload recUpload, int colIndex) {
        
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        Date when = XlsCellUtil.cellToDate(currentReadCell); 
        if (when == null) {
            throw new IllegalArgumentException("This attribute is required.");
        }
        recUpload.setWhen(when);
        return colIndex;
    }

    protected int readRowLongitude(Row row, RecordUpload recUpload, int colIndex) {
        // Longitude
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        double longitude = Double.NaN;
        if (currentReadCell.getCellType() != Cell.CELL_TYPE_BLANK) {
            longitude = XlsCellUtil.cellToDouble(currentReadCell);
            if (longitude < -180 || longitude > 180) {
                throw new IllegalArgumentException(
                        "Longitude must be between -180 and 180.");
            }
        }
        recUpload.setLongitude(longitude);
        return colIndex;
    }

    protected int readRowLatitude(Row row, RecordUpload recUpload, int colIndex) {
        // Latitude
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        double latitude = Double.NaN;
        if (currentReadCell.getCellType() != Cell.CELL_TYPE_BLANK) {
            latitude = XlsCellUtil.cellToDouble(currentReadCell);
            if (latitude < -90 || latitude > 90) {
                throw new IllegalArgumentException(
                        "Latitude must be between -90 and 90.");
            }
        }
        recUpload.setLatitude(latitude);
        return colIndex;
    }

    protected int readRowLocation(Survey survey, Row row, RecordUpload recUpload, int colIndex) {
        
        // Location ID
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        if (currentReadCell.getCellType() != Cell.CELL_TYPE_BLANK) {
            int locationId = (int)XlsCellUtil.cellToDouble(currentReadCell);
            recUpload.setLocationId(locationId);
        }
        
        // Location Name
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        String locationName = XlsCellUtil.cellToString(currentReadCell);
        recUpload.setLocationName(locationName);
        if (survey.isPredefinedLocationsOnly() && recUpload.isGPSLocationName()) {
            throw new IllegalArgumentException(
                    "This attribute must be a predefined location.");
        }
        
        return colIndex;
    }

    protected int readRowId(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        recUpload.setId(XlsCellUtil.cellToString(currentReadCell));
        if (recUpload.getId().isEmpty()) {
            recUpload.setId(String.format("Row %d", row.getRowNum() + 1));
        }
        return colIndex;
    }
    
    protected int readRowParentId(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        recUpload.setParentId(XlsCellUtil.cellToString(currentReadCell));
        return colIndex;        
    }
    
    protected int readRowCensusMethodId(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        if(Cell.CELL_TYPE_BLANK != currentReadCell.getCellType()) {
            double censusMethodId = XlsCellUtil.cellToDouble(currentReadCell);
            recUpload.setCensusMethodId((int)censusMethodId);
        }
        return colIndex; 
    }

    protected int readRowTaxonomy(Row row, RecordUpload recUpload, int colIndex) {
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        recUpload.setScientificName(XlsCellUtil.cellToString(currentReadCell));
        
        currentReadCell = row.getCell(colIndex++, Row.CREATE_NULL_AS_BLANK);
        recUpload.setCommonName(XlsCellUtil.cellToString(currentReadCell));
        
        CensusMethod cm = null;
        if(recUpload.getCensusMethodId() != null) {
            cm = censusMethodDAO.get(recUpload.getCensusMethodId());
        }
        
        if (cm != null && Taxonomic.TAXONOMIC.equals(cm.getTaxonomic())) {
            if (!((recUpload.getScientificName() != null && !recUpload.getScientificName().isEmpty()) || (recUpload.getCommonName() != null && !recUpload.getCommonName().isEmpty()))) {
                throw new IllegalArgumentException(
                        "The scientific name or common name is required.");
            }
        }
        return colIndex;
    }
    
    @Override
    public int writeCoreHelp(Sheet helpSheet, int rowIndex) {
        Row row;
        int colIndex = 0;
        for (Map.Entry<String, String> entry : headerMap.entrySet()) {
            row = helpSheet.createRow(rowIndex++);
            colIndex = 0;
            row.createCell(colIndex++).setCellValue(entry.getKey());
            colIndex = writeRowBlank(row, colIndex);
            row.createCell(colIndex++).setCellValue(entry.getValue());
        }
        return rowIndex;
    }

    public List<String> getCompleteHeader() {
        return Collections.unmodifiableList(completeHeader);
    }
}
