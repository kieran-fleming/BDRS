package au.com.gaiaresources.bdrs.service.bulkdata;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.security.sasl.AuthenticationException;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.ss.util.CellRangeAddress;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.classic.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.record.impl.EmptyScrollableRecords;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyService;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.service.lsid.Lsid;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.util.StringUtils;

public abstract class AbstractBulkDataService {

    // http://support.microsoft.com/kb/120596
    public static final int MAX_EXCEL_ROW_COUNT = 65536;
    // http://wiki.services.openoffice.org/wiki/Documentation/FAQ/Calc/Miscellaneous/What's_the_maximum_number_of_rows_and_cells_for_a_spreadsheet_file%3F
    public static final int MAX_OO_ROW_COUNT = 65536;
    public static final int MAX_ROW_COUNT = Math.min(MAX_EXCEL_ROW_COUNT, MAX_OO_ROW_COUNT);
    public static final int PARSE_ERROR_LIMIT = 50;
    
    public static final String RECORD_SHEET_NAME = "Observations";
    public static final String HELP_SHEET_NAME = "Help";
    public static final String TAXONOMY_SHEET_NAME = "Taxonomy";
    public static final String LOCATION_SHEET_NAME = "Locations";
    /**
     * Header of the column containing Location primary keys.
     */ 
    public static final String LOCATION_SHEET_LOCATION_ID = "Location ID";
    /**
     * Header of the column containing the type (survey or user) of the location.
     */
    public static final String LOCATION_SHEET_LOCATION_TYPE = "Type";
    /**
     * Inserted in the "Type" column of the location row if the location is a survey location.
     */
    public static final String LOCATION_SHEET_SURVEY_LOCATION = "Survey Location";
    /**
     * Inserted in the "Type" column of the location row if the location is a user location.
     */    public static final String LOCATION_SHEET_USER_LOCATION = "User Location";
    public static final String USERS_SHEET_NAME = "Users";

    /**
     * Name of the sheet that lists the Census Methods.
     */
    public static final String CENSUS_METHOD_SHEET_NAME = "CensusMethods";
    /**
     * Name of the header on the census method sheet containing the name of the census method.
     */
    public static final String CENSUS_METHOD_SHEET_CENSUS_METHOD_COL_NAME = "Census Method Name";

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SessionFactory sessionFactory;

    @Autowired
    public SurveyDAO surveyDAO;

    @Autowired
    public GroupDAO groupDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private LocationService locationService;

    @Autowired
    private LocationDAO locationDAO;

    @Autowired
    private TaxaDAO taxaDAO;

    @Autowired
    private RecordDAO recordDAO;

    @Autowired
    private CensusMethodDAO censusMethodDAO;

    @Autowired
    private LSIDService lsidService;

    @Autowired
    protected PropertyService propertyService;

    @Autowired
    protected SurveyService surveyService;

    @Autowired
    protected BulkDataReadWriteService bulkDataReadWriteService;

    private PasswordEncoder passwordEncoder = new Md5PasswordEncoder();

    public void exportSurveyTemplate(Survey survey, OutputStream outputStream)
            throws IOException {
        this.exportSurveyRecords(survey, new EmptyScrollableRecords(), outputStream);
    }
    
    public void exportSurveyRecords(Survey survey, ScrollableRecords scrollableRecords, OutputStream outputStream) throws IOException {
        this.exportSurveyRecords(survey, scrollableRecords, Long.MAX_VALUE, outputStream);
    }

    public void exportSurveyRecords(Survey survey, ScrollableRecords scrollableRecords, long limit,
            OutputStream outputStream) throws IOException {
        if(limit < 0) {
            limit = Long.MAX_VALUE;
        }
        
        RecordRow rowPrinter = getRecordRow();
        Workbook wb = new HSSFWorkbook();
        if (survey != null) {
            rowPrinter.createCellStyles(wb);
            
            // Be careful not to reorder the sheets. This will break the formulas.
            // First create a placeholder sheet for the observations.
            // Then create the census method sheet and location sheet.
            // These sheets will be referenced by the observation sheet.
            Sheet observationSheet = wb.createSheet(RECORD_SHEET_NAME);
            writeCensusMethodSheet(survey, wb, rowPrinter);

            XlsLocationRow locationRow = new XlsLocationRow(
                    bulkDataReadWriteService, survey);
            locationRow.writeLocationHeader(wb);
            locationRow.writeSurveyLocations(wb);

            int rowIndex = 0;
            // Survey Description
            Row surveyDescriptionRow = observationSheet.createRow(rowIndex++);
            Cell surveyDescriptionCell = surveyDescriptionRow.createCell(0);
            surveyDescriptionCell.setCellStyle(rowPrinter.getCellStyleByKey(XlsRecordRow.STYLE_RECORD_HEADER));
            surveyDescriptionCell.setCellValue(String.format("%s: %s", survey.getName(), survey.getDescription()));

            // placeholder for census method names etc
            Row censusMethodTitleRow = observationSheet.createRow(rowIndex++);

            // Record Header
            Row headerRow = observationSheet.createRow(rowIndex++);
            rowPrinter.writeHeader(censusMethodTitleRow, headerRow, survey);

            // Merge the survey description cell to occupy the width of the
            // spreadsheet
            observationSheet.addMergedRegion(new CellRangeAddress(
                    surveyDescriptionRow.getRowNum(), // first row (0-based)
                    surveyDescriptionRow.getRowNum(), // last row (0-based)
                    surveyDescriptionCell.getColumnIndex(), // first column (0-based)
                    headerRow.getLastCellNum() - 1 // last column (0-based)
            ));

            Record r;
            int recordCount = 0;
            Session sesh = sessionFactory.getCurrentSession();
            while(scrollableRecords.hasMoreElements() && recordCount < limit) {
                r = scrollableRecords.nextElement();
                
                if(r.getLocation() != null && !survey.getLocations().contains(r.getLocation())) {
                    locationRow.writeUserLocation(wb, r.getLocation());
                }
                rowPrinter.writeRow(lsidService, observationSheet.createRow(rowIndex++), r);
                
                if (++recordCount % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
                    sesh.clear();
                }
            }

            writeTaxonomySheet(survey, wb, rowPrinter);
            writeHelpSheet(survey, wb, rowPrinter);
        }
        wb.write(outputStream);
    }

    public void exportUsers(List<User> userList, OutputStream outputStream)
            throws IOException {

        RecordRow rowPrinter = getRecordRow();
        Workbook wb = new HSSFWorkbook();

        rowPrinter.createCellStyles(wb);
        Sheet usersSheet = wb.createSheet(USERS_SHEET_NAME);

        Row headerRow = usersSheet.createRow(0);

        Cell headerCell = headerRow.createCell(0);
        headerCell.setCellStyle(rowPrinter.getCellStyleByKey(XlsRecordRow.STYLE_RECORD_HEADER));
        headerCell.setCellValue("Login");

        headerCell = headerRow.createCell(1);
        headerCell.setCellStyle(rowPrinter.getCellStyleByKey(XlsRecordRow.STYLE_RECORD_HEADER));
        headerCell.setCellValue("Given Name");

        headerCell = headerRow.createCell(2);
        headerCell.setCellStyle(rowPrinter.getCellStyleByKey(XlsRecordRow.STYLE_RECORD_HEADER));
        headerCell.setCellValue("Surname");

        headerCell = headerRow.createCell(3);
        headerCell.setCellStyle(rowPrinter.getCellStyleByKey(XlsRecordRow.STYLE_RECORD_HEADER));
        headerCell.setCellValue("Email Address");

        for (int rowIndex = 0; rowIndex < userList.size(); rowIndex++) {

            User u = userList.get(rowIndex);
            Row userDescriptionRow = usersSheet.createRow(rowIndex + 1);

            Cell userLoginCell = userDescriptionRow.createCell(0);
            userLoginCell.setCellValue(String.format("%s", u.getName()));

            Cell userGivenNameCell = userDescriptionRow.createCell(1);
            userGivenNameCell.setCellValue(String.format("%s", u.getFirstName()));

            Cell userSurnameCell = userDescriptionRow.createCell(2);
            userSurnameCell.setCellValue(String.format("%s", u.getLastName()));

            Cell userEmailCell = userDescriptionRow.createCell(3);
            userEmailCell.setCellValue(String.format("%s", u.getEmailAddress()));

        }

        usersSheet.autoSizeColumn(1);
        usersSheet.autoSizeColumn(2);
        usersSheet.autoSizeColumn(3);
        usersSheet.autoSizeColumn(4);

        wb.write(outputStream);
    }

    private void writeTaxonomySheet(Survey survey, Workbook wb,
            RecordRow recordRow) {
        Sheet locSheet = wb.createSheet(TAXONOMY_SHEET_NAME);
        CellStyle headerStyle = recordRow.getCellStyleByKey(XlsRecordRow.STYLE_TAXONOMY_HEADER);

        int rowIndex = 0;
        int colIndex = 0;
        Cell cell;
        Row headerRow = locSheet.createRow(rowIndex++);

        cell = headerRow.createCell(colIndex++);
        cell.setCellValue("Scientific Name");
        cell.setCellStyle(headerStyle);

        cell = headerRow.createCell(colIndex++);
        cell.setCellValue("Common Name");
        cell.setCellStyle(headerStyle);

        cell = headerRow.createCell(colIndex++);
        cell.setCellValue("Group");
        cell.setCellStyle(headerStyle);

        int speciesCount = taxaDAO.countSpeciesForSurvey(survey);

        if (speciesCount > 0 && speciesCount < MAX_ROW_COUNT) {
            for (IndicatorSpecies taxon : survey.getSpecies()) {
                Row taxonRow = locSheet.createRow(rowIndex++);
                colIndex = 0;

                cell = taxonRow.createCell(colIndex++);
                cell.setCellValue(taxon.getScientificName());

                cell = taxonRow.createCell(colIndex++);
                cell.setCellValue(taxon.getCommonName());

                cell = taxonRow.createCell(colIndex++);
                cell.setCellValue(taxon.getTaxonGroup().getName());
            }
        } else {
            Row tooManyRow = locSheet.createRow(rowIndex++);
            colIndex = 0;
            Cell tooManyCell = tooManyRow.createCell(colIndex++);
            tooManyCell.setCellValue("Taxonomy count exceeds maximum row count.");
            locSheet.addMergedRegion(new CellRangeAddress(
                    tooManyRow.getRowNum(), // first
                    // row
                    // (0-based)
                    tooManyRow.getRowNum(), // last row (0-based)
                    0, // first column (0-based)
                    headerRow.getLastCellNum() - 1 // last column (0-based)
            ));

            Row tipRow = locSheet.createRow(rowIndex++);
            colIndex = 0;
            Cell tipCell = tipRow.createCell(colIndex++);
            tipCell.setCellValue("Please download the taxonomy as a CSV file.");
            locSheet.addMergedRegion(new CellRangeAddress(tipRow.getRowNum(), // first
                    // row
                    // (0-based)
                    tipRow.getRowNum(), // last row (0-based)
                    0, // first column (0-based)
                    headerRow.getLastCellNum() - 1 // last column (0-based)
            ));
        }
    }

    private void writeHelpSheet(Survey survey, Workbook wb, RecordRow recordRow) {

        Sheet helpSheet = wb.createSheet(HELP_SHEET_NAME);

        int rowIndex = 0;
        int colIndex = 0;
        Row row;

        // Help Header
        CellStyle helpHeaderStyle = recordRow.getCellStyleByKey(XlsRecordRow.STYLE_HELP_HEADER);
        row = helpSheet.createRow(rowIndex++);
        colIndex = 0;
        Cell columnHeaderCell = row.createCell(colIndex++);
        columnHeaderCell.setCellValue("Column");

        Cell scopeHeaderCell = row.createCell(colIndex++);
        scopeHeaderCell.setCellValue("Scope");

        Cell descriptionHeaderCell = row.createCell(colIndex++);
        descriptionHeaderCell.setCellValue("Description");

        if (helpHeaderStyle != null) {
            columnHeaderCell.setCellStyle(helpHeaderStyle);
            descriptionHeaderCell.setCellStyle(helpHeaderStyle);
            scopeHeaderCell.setCellStyle(helpHeaderStyle);
        }

        // Precanned Darwin Core Values
        rowIndex = recordRow.writeCoreHelp(helpSheet, rowIndex);

        // Attributes
        for (Attribute attr : survey.getAttributes()) {
            row = helpSheet.createRow(rowIndex++);
            colIndex = 0;
            row.createCell(colIndex++).setCellValue(attr.getName());

            String scopeName = attr.getScope() == null ? ""
                    : attr.getScope().getName();
            row.createCell(colIndex++).setCellValue(scopeName);

            row.createCell(colIndex++).setCellValue(attr.getDescription());
        }
    }

    private Sheet writeCensusMethodSheet(Survey survey, Workbook wb,
            RecordRow recordRow) {
        Sheet censusMethodSheet = wb.createSheet(CENSUS_METHOD_SHEET_NAME);

        Set<CensusMethod> censusMethods = surveyService.catalogCensusMethods(survey);

        int rowIndex = 0;
        int colIndex = 0;

        // Help Header
        CellStyle helpHeaderStyle = recordRow.getCellStyleByKey(XlsRecordRow.STYLE_HELP_HEADER);

        Row headerRow = censusMethodSheet.createRow(rowIndex++);
        colIndex = 0;

        createStyledCell(headerRow, "Census Method ID", helpHeaderStyle, colIndex++);
        createStyledCell(headerRow, CENSUS_METHOD_SHEET_CENSUS_METHOD_COL_NAME, helpHeaderStyle, colIndex++);
        createStyledCell(headerRow, "Taxonomic", helpHeaderStyle, colIndex++);
        createStyledCell(headerRow, "Type", helpHeaderStyle, colIndex++);
        createStyledCell(headerRow, "Description", helpHeaderStyle, colIndex++);
        createStyledCell(headerRow, "Valid Child Census Methods", helpHeaderStyle, colIndex++);

        for (CensusMethod cm : censusMethods) {
            Row cmDescRow = censusMethodSheet.createRow(rowIndex++);
            colIndex = 0;

            cmDescRow.createCell(colIndex++).setCellValue(cm.getId().doubleValue());
            createCell(cmDescRow, cm.getName(), colIndex++);
            createCell(cmDescRow, cm.getTaxonomic().getName(), colIndex++);
            createCell(cmDescRow, cm.getType(), colIndex++);
            createCell(cmDescRow, cm.getDescription(), colIndex++);

            StringBuilder sb = new StringBuilder();
            for (CensusMethod childCm : cm.getCensusMethods()) {
                sb.append(childCm.getId());
                sb.append(", ");
            }
            if (sb.length() >= 2) {
                sb.delete(sb.length() - 2, sb.length() - 1);
            }

            createCell(cmDescRow, sb.toString(), colIndex++);
        }

        return censusMethodSheet;
    }

    private void createCell(Row row, String text, int colIndex) {
        createStyledCell(row, text, null, colIndex);
    }

    private void createStyledCell(Row row, String text, CellStyle style,
            int colIndex) {
        Cell cell = row.createCell(colIndex);
        cell.setCellValue(text);
        setCellStyle(cell, style);
    }

    private void setCellStyle(Cell cell, CellStyle style) {
        if (style != null) {
            cell.setCellStyle(style);
        }
    }

    public BulkUpload importBulkData(Survey survey, InputStream inp)
            throws IOException, ParseException {
        try {
            BulkUpload bulkUpload = new BulkUpload();
            Workbook wb = WorkbookFactory.create(inp);

            Sheet recordSheet = null;
            Sheet locationSheet = null;
            for (int sheetIndex = 0; sheetIndex < wb.getNumberOfSheets(); sheetIndex++) {
                Sheet sheet = wb.getSheetAt(sheetIndex);
                String sheetName = sheet.getSheetName();

                if (RECORD_SHEET_NAME.equals(sheetName)) {
                    recordSheet = sheet;
                } else if (LOCATION_SHEET_NAME.equals(sheetName)) {
                    locationSheet = sheet;
                }
            }

            if (recordSheet == null) {
                throw new IllegalArgumentException(
                        "Cannot find record sheet. The sheet must be named: "
                                + RECORD_SHEET_NAME);
            } else if (locationSheet == null) {
                throw new IllegalArgumentException(
                        "Cannot find record sheet. The sheet must be named: "
                                + LOCATION_SHEET_NAME);
            } else {
                // Order is important because the record will attempt to resolve locations.
                importBulkLocations(survey, bulkUpload, locationSheet);
                importBulkRecords(survey, bulkUpload, recordSheet);
            }

            bulkUpload.addSurvey(survey);
            return bulkUpload;

        } catch (InvalidFormatException ife) {
            throw new IllegalArgumentException(ife);
        }
    }

    private void importBulkLocations(Survey survey, BulkUpload bulkUpload,
            Sheet locationSheet) {
        Row row;
        XlsLocationRow locationRow = new XlsLocationRow(
                bulkDataReadWriteService, survey);
        for (Iterator<Row> rowIter = locationSheet.rowIterator(); rowIter.hasNext();) {
            row = rowIter.next();

            // Skip the header row
            if (!locationRow.isHeader(row) && !isBlankRow(row)) {
                LocationUpload locUpload = locationRow.readRow(row);
                bulkUpload.addLocationUpload(locUpload);
            }
        }
    }

    private void importBulkRecords(Survey survey, BulkUpload bulkUpload,
            Sheet recordSheet) throws ParseException {
        boolean headerParsed = false;
        RecordRow recordRow = getRecordRow();
        RecordUpload recordUpload;
        Row row = null;
        Row superRow = null;
        Iterator<Row> rowIterator = recordSheet.rowIterator();
        int errorCount = 0;
        boolean headerReached = false;
        while (rowIterator.hasNext() && errorCount < PARSE_ERROR_LIMIT) {

            superRow = row;
            row = rowIterator.next();

            if (superRow == null) {
                continue;
            }

            // headerParsed means that the header has been loaded
            // by the record row.
            // headerReached means that the header section of the 
            // sheet has been reached and the following rows 
            // are all record.
            if (!headerParsed || !headerReached) {
                // Skip all rows until the header is reached.
                if (recordRow.isHeader(row)) {
                    recordRow.readHeader(survey, superRow, row);
                    headerParsed = true;
                    headerReached = true;
                }
            } else {
                // Skip blank rows. It is not immediately apparent to users
                // that there is a difference between blank rows and
                // the lack of rows in a spreadsheet.
                if (!isBlankRow(row)) {
                    recordUpload = recordRow.readRow(survey, row);
                    recordUpload.setSurveyName(survey.getName());
                    // if the record date is outside the survey date range,
                    // add an error message for that record
                    if ((survey.getStartDate() != null && recordUpload.getWhen().before(survey.getStartDate()))
                            || (survey.getEndDate() != null && recordUpload.getWhen().after(survey.getEndDate()))) {
                        SimpleDateFormat sdf = new SimpleDateFormat(
                                "dd MMM yyy HH:mm");
                        recordUpload.setErrorMessage("Observation date "
                                + sdf.format(recordUpload.getWhen())
                                + " outside survey "
                                + (survey.getStartDate() != null
                                        && survey.getEndDate() != null ? "range ("
                                        + sdf.format(survey.getStartDate())
                                        + " - "
                                        + sdf.format(survey.getEndDate()) + ")"
                                        : "start date ("
                                                + sdf.format(survey.getStartDate())
                                                + ")") + ".");
                        recordUpload.setError(true);
                    }

                    bulkUpload.addRecordUpload(recordUpload);
                    if (recordUpload.isError()) {
                        errorCount += 1;
                    }
                }
            }
        }

        if (!headerParsed) {
            // We still haven't found the header
            throw new ParseException("Unable to find header row.", -1);
        }
    }

    private boolean isBlankRow(Row row) {
        Iterator<Cell> cellIterator = row.cellIterator();
        Cell cell;
        while (cellIterator.hasNext()) {
            cell = cellIterator.next();
            if (Cell.CELL_TYPE_BLANK != cell.getCellType()) {
                return false;
            }
        }
        log.debug(String.format("Row %d is blank. Skipping this row.", row.getRowNum() + 1));
        return true;
    }

    private String formatErrorString(RecordUpload ru, String msg) {
        StringBuilder sb = new StringBuilder();
        sb.append("[Row ");
        sb.append(ru.getRowNumber().toString());
        sb.append("] ");
        sb.append(msg);
        return sb.toString();
    }

    // ------------------------------------------------------
    // Converting BulkUpload to Persisted Objects
    // ------------------------------------------------------

    public List<Record> saveRecords(User owner, BulkUpload bulkUpload,
            boolean createMissingData) throws MissingDataException,
            AuthenticationException, InvalidSurveySpeciesException,
            DataReferenceException {

        if (bulkUpload.hasError()) {
            return null;
        }

        List<Record> records = new ArrayList<Record>();
        // IMPORTANT!!
        // because we are using a new hibernate session here all DAO usage
        // MUST REFER TO THIS NEW SESSION!!  If you don't follow this rule
        // the app may hang / other weirdness may happen. 
        // I assume this has something to do
        // with the sessions deadlocking each other.
        Session sesh = sessionFactory.openSession();
        try {
            Transaction tx = sesh.beginTransaction();

            User ownerForSesh = (User) sesh.merge(owner);

            // Set up all the src data
            bulkUpload.setMissingGroups(populateGroups(sesh, ownerForSesh, bulkUpload, createMissingData));
            bulkUpload.setMissingUsers(populateUsers(sesh, ownerForSesh, bulkUpload, createMissingData));
            bulkUpload.setMissingSurveys(populateSurveys(sesh, ownerForSesh, bulkUpload, createMissingData));
            bulkUpload.setMissingIndicatorSpecies(populateIndicatorSpecies(sesh, bulkUpload));

            if (bulkUpload.isMissingData()) {
                tx.rollback();
                if (createMissingData) {
                    // You were allowed to create missing data and yet you still
                    // have missing data. Must be an authentication issue.
                    throw new AuthenticationException();

                } else {
                    // You need to switch on creation of missing data.
                    throw new MissingDataException();
                }
            }

            // Create the locations
            Map<Integer, Location> locationPkMap = new HashMap<Integer, Location>();
            Map<String, Location> locationNameMap = new HashMap<String, Location>();
            populateLocation(sesh, ownerForSesh, bulkUpload, locationPkMap, locationNameMap);

            // Create the records
            Survey survey;
            Group klass;
            Group group;
            User recordedBy;
            Record rec;
            IndicatorSpecies species;
            Map<String, Record> newRecords = new HashMap<String, Record>();

            int count = 0;
            log.info("Saving Records");
            for (RecordUpload recordUpload : bulkUpload.getRecordUploadList()) {

                // Put the class in the survey
                survey = bulkUpload.getSurveyByName(recordUpload.getSurveyName());
                klass = bulkUpload.getGroupByName(recordUpload.getClassName());
                group = bulkUpload.getGroupByName(recordUpload.getGroupName());

                // If there is no user for this record, then they will be assigned
                // to the person who uploaded the spreadsheet.
                recordedBy = bulkUpload.getUserByUsername(recordUpload.getRecordedByUsername());
                recordedBy = recordedBy == null ? owner : recordedBy;

                if (klass != null) {
                    survey.getGroups().add(klass);
                    survey.getUsers().add(ownerForSesh);
                    surveyDAO.update(sesh, survey);

                    // Put the group in the class
                    if (group != null) {
                        klass.getGroups().add(group);
                        klass.getAdmins().add(ownerForSesh);
                        groupDAO.update(sesh, klass);

                        // Put the user in the group
                        group.getUsers().add(recordedBy);
                        groupDAO.update(sesh, group);
                    }
                }

                try {
                    Lsid lsid = lsidService.fromLSID(recordUpload.getId());
                    rec = recordDAO.getRecord(sesh, lsid.getObjectId());
                } catch (IllegalArgumentException iae) {
                    rec = new Record();
                    if (org.springframework.util.StringUtils.hasLength(recordUpload.getId())) {
                        newRecords.put(recordUpload.getId().trim(), rec);
                    }
                }

                Integer censusMethodId = recordUpload.getCensusMethodId();
                CensusMethod cm = null;
                if (censusMethodId != null) {
                    cm = censusMethodDAO.get(sesh, censusMethodId);
                }
                rec.setCensusMethod(cm);
                
                if (cm == null && recordUpload.getCensusMethodId() != null) {
                    // a census method has been requested but it is invalid!
                    String err = formatErrorString(recordUpload, "You have requested a census method id = "
                            + recordUpload.getCensusMethodId()
                            + " but it is invalid. Only use the ID's exactly as they appear on provided Census Method list bundled with your template");
                    throw new DataReferenceException(err);
                }
                // Set parent record if requested...
                if (org.springframework.util.StringUtils.hasLength(recordUpload.getParentId())) {
                    Record parentRecord;
                    try {
                        Lsid lsid = lsidService.fromLSID(recordUpload.getParentId());
                        parentRecord = recordDAO.getRecord(sesh, lsid.getObjectId());
                    } catch (IllegalArgumentException iae) {
                        // The parent record must be a newly formed record.
                        parentRecord = newRecords.get(recordUpload.getParentId());
                    }
                    if (parentRecord == null) {
                        // error!
                        log.error("Parent record id : "
                                + recordUpload.getParentId()
                                + " was requested but was not found in the list of added records. Can't assign parent record!");
                        throw new DataReferenceException(
                                formatErrorString(recordUpload, "Cannot find the parent id = "
                                        + recordUpload.getParentId()
                                        + ". The ID does not exist in the database nor does it reference a newly created ID in the uploaded data"));
                    }
                    // check if the parent record is a valid one i.e. does it fulfill the census method rules.
                    if (rec.getCensusMethod() == null) {
                        String err = formatErrorString(recordUpload, "The census method of the row is null, unable to assign a parent record. Review the census method listing for valid combinations.");
                        throw new DataReferenceException(err);
                    }
                    if (parentRecord.getCensusMethod() == null) {
                        throw new DataReferenceException(
                                formatErrorString(recordUpload, "The census method of the parent record of the row is null, unable to assign a parent record. Review the census method listing for valid combinations."));
                    }
                    if (!parentRecord.getCensusMethod().getCensusMethods().contains(rec.getCensusMethod())) {
                        throw new DataReferenceException(
                                formatErrorString(recordUpload, "The census method of the child record record is not a valid child census method of the parent record. Review the census method listing for valid combinations."));
                    }
                    rec.setParentRecord(parentRecord);
                } else {
                    rec.setParentRecord(null);
                }

                rec.setSurvey(bulkUpload.getSurveyByName(recordUpload.getSurveyName()));

                if (recordUpload.getScientificName() != null
                        && !recordUpload.getScientificName().isEmpty()) {
                    species = bulkUpload.getIndicatorSpeciesByScientificName(recordUpload.getScientificName());
                } else {
                    species = bulkUpload.getIndicatorSpeciesByCommonName(recordUpload.getCommonName());
                }

                // an survey with no species actually includes everything
                if (survey.getSpecies().isEmpty()
                        || survey.getSpecies().contains(species)) {
                    rec.setSpecies(species);
                } else {
                    bulkUpload.getInvalidSurveySpecies().put(species, survey);
                }

                rec.setUser(recordedBy);

                if (!recordUpload.isGPSLocationName()) {
                    //log.info("Not a gps location");
                    Location loc;
                    if (recordUpload.getLocationId() != null) {
                        loc = locationPkMap.get(recordUpload.getLocationId());
                    } else {
                        loc = locationNameMap.get(recordUpload.getLocationName());
                    }
                    rec.setLocation(loc);
                }
                if (recordUpload.hasLatitudeLongitude()) {
                    rec.setPoint(locationService.createPoint(recordUpload.getLatitude(), recordUpload.getLongitude()));
                }

                rec.setHeld(recordUpload.isHeld());
                rec.setWhen(recordUpload.getWhen());
                rec.setTime(recordUpload.getTime().getTime());
                rec.setLastDate(recordUpload.getLastDate() != null ? recordUpload.getLastDate()
                        : recordUpload.getWhen());
                rec.setLastTime(recordUpload.getLastTime() != null ? recordUpload.getLastTime().getTime()
                        : recordUpload.getTime().getTime());

                rec.setNotes(recordUpload.getNotes());
                rec.setFirstAppearance(recordUpload.getFirstAppearance());
                rec.setLastAppearance(recordUpload.getLastAppearance());
                rec.setBehaviour(recordUpload.getBehaviour());
                rec.setHabitat(recordUpload.getHabitat());
                rec.setNumber(recordUpload.getNumberSeen());

                // Insert the current attributes into a lookup.
                // We will remove attributes that we retain out of this lookup.
                // At the end, any attribute remaining in the lookup will be
                // deleted.
                Map<Attribute, AttributeValue> recordAttributeMap = new HashMap<Attribute, AttributeValue>();
                for (AttributeValue curAttr : rec.getAttributes()) {
                    recordAttributeMap.put(curAttr.getAttribute(), curAttr);
                }

                Set<AttributeValue> recAttrSet = new HashSet<AttributeValue>();
                if (species != null) {
                    for (Attribute taxonAttr : species.getTaxonGroup().getAttributes()) {
                        String recAttrValue = recordUpload.getNamedAttribute(XlsRecordRow.SURVEY_ATTR_NAMESPACE, taxonAttr.getName());
                        if (!taxonAttr.isTag()
                                && org.springframework.util.StringUtils.hasLength(recAttrValue)) {
                            AttributeValue recAttr = createAttributeValue(sesh, recordAttributeMap, taxonAttr, recAttrValue);
                            recAttrSet.add(recAttr);
                        }
                    }
                }

                for (Attribute surveyAttr : survey.getAttributes()) {
                    if (!AttributeScope.LOCATION.equals(surveyAttr)) {
                        String recAttrValue = recordUpload.getNamedAttribute(XlsRecordRow.SURVEY_ATTR_NAMESPACE, surveyAttr.getDescription());
                        if (org.springframework.util.StringUtils.hasLength(recAttrValue)) {
                            AttributeValue recAttr = createAttributeValue(sesh, recordAttributeMap, surveyAttr, recAttrValue);
                            recAttrSet.add(recAttr);
                        }
                    }
                }

                if (cm != null) {
                    for (Attribute censusMethodAttr : cm.getAttributes()) {
                        String cmNamespace = bulkDataReadWriteService.formatCensusMethodNameId(cm);
                        String cmAttrValue = recordUpload.getNamedAttribute(cmNamespace, censusMethodAttr.getDescription());
                        if (org.springframework.util.StringUtils.hasLength(cmAttrValue)) {
                            AttributeValue recAttr = createAttributeValue(sesh, recordAttributeMap, censusMethodAttr, cmAttrValue);
                            recAttrSet.add(recAttr);
                        }
                    }
                }

                rec.setAttributes(recAttrSet);

                // Set record visibility to survey default. Setting via web form not supported.
                // Survey's default record visibility can be set in the 'admin -> projects' interface
                rec.setRecordVisibility(survey.getDefaultRecordVisibility());

                rec = recordDAO.save(sesh, rec);
                records.add(rec);

                // Delete any remaining RecordAttributes
                for (AttributeValue delRecAttr : recordAttributeMap.values()) {
                    recordDAO.delete(sesh, delRecAttr);
                }
                
                count += 1;
                if(count % 100 == 0) {
                    log.info(String.format("%d records saved", count));
                }
            }

            if (bulkUpload.hasInvalidSurveySpecies()) {
                tx.rollback();

                throw new InvalidSurveySpeciesException();
            } else {
                log.info("Committing records...");
                tx.commit();
            }
        } finally {
            sesh.close();
        }
        return records;
    }

    private AttributeValue createAttributeValue(Session sesh,
            Map<Attribute, AttributeValue> existingAttributeMap,
            Attribute attrToAdd, String attributeValueStr) {
        AttributeValue attrVal;
        if (existingAttributeMap.containsKey(attrToAdd)) {
            attrVal = existingAttributeMap.remove(attrToAdd);
        } else {
            attrVal = new AttributeValue();
            attrVal.setAttribute(attrToAdd);
        }
        attrVal.setStringValue(attributeValueStr);
        try {
            switch (attrToAdd.getType()) {
            case INTEGER:
            case INTEGER_WITH_RANGE:
            case DECIMAL:
                attrVal.setNumericValue(new BigDecimal(attributeValueStr));
                break;
            case DATE:
                SimpleDateFormat dateFormat = new SimpleDateFormat(
                        "dd MMM yyyy");
                dateFormat.setLenient(false);
                attrVal.setDateValue(dateFormat.parse(attributeValueStr));
                break;
            case IMAGE:
            case FILE:
                throw new UnsupportedOperationException(
                        "Spreadsheet upload of file data is not supported.");
            case TIME:
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case STRING:
            case HTML:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
            default:
                attrVal.setStringValue(attributeValueStr);
                break;
            }
        } catch (ParseException pe) {
            log.warn("Unable to parse date value \"" + attributeValueStr
                    + "\" for attribute with name \"" + attrToAdd.getName()
                    + "\"", pe);
        } catch (Exception e) {
            String tmpl = "Unable to store value \"%s\" in attribute \"%s:%s\". The error was %s:%s";
            String msg = String.format(tmpl, attributeValueStr, attrToAdd.getName(), attrToAdd.getDescription(), e.getClass().getSimpleName(), e.getMessage());
            throw new IllegalArgumentException(msg, e);
        }
        AttributeValue result = recordDAO.save(sesh, attrVal);
        return result;
    }

    private void populateLocation(Session sesh, User owner,
            BulkUpload bulkUpload, Map<Integer, Location> locationPkMap,
            Map<String, Location> locationNameMap) {

        for (LocationUpload locUpload : bulkUpload.getLocationUploads()) {
            Location location = null;
            Survey survey = null;
            if (locUpload.getLocationName() != null
                    && !RecordRow.GPS_LOCATION.equals(locUpload.getLocationName())
                    && !locUpload.getLocationName().isEmpty()) {

                location = locationDAO.getLocation(sesh, locUpload.getPk());
                if (location == null) {
                    log.debug("Creating location: " + locUpload.getSurveyName()
                            + " - " + locUpload.getLocationName());

                    survey = bulkUpload.getSurveyByName(locUpload.getSurveyName());
                    if (survey != null) {
                        location = new Location();
                        // New uploaded locations are always survey locations.
                        // location.setUser(owner);
                        location.setName(locUpload.getLocationName());
                        location.setLocation(locationService.createPoint(locUpload.getLatitude(), locUpload.getLongitude()));
                        location = locationDAO.save(sesh, location);

                        survey.getLocations().add(location);
                        surveyDAO.update(sesh, survey);

                        locationPkMap.put(location.getId(), location);
                        locationNameMap.put(location.getName(), location);

                    } else {
                        log.debug("Not creating location. Containing Survey \""
                                + locUpload.getSurveyName() + "\" not found.");
                    }
                } else {
                    log.debug("Retrieved Location: "
                            + locUpload.getSurveyName());
                    locationPkMap.put(location.getId(), location);
                    locationNameMap.put(location.getName(), location);
                }

                // Location Attributes
                if (survey != null && location != null) {
                    Map<Attribute, AttributeValue> attrMap = new HashMap<Attribute, AttributeValue>();
                    for (AttributeValue attrVal : location.getAttributes()) {
                        attrMap.put(attrVal.getAttribute(), attrVal);
                    }

                    for (Attribute attr : survey.getAttributes()) {
                        if (AttributeScope.LOCATION.equals(attr.getScope())) {
                            String attrValStr = locUpload.getAttributeValue(attr);
                            if (attrValStr != null) {
                                AttributeValue attrVal = createAttributeValue(sesh, attrMap, attr, attrValStr);
                                location.getAttributes().add(attrVal);
                            }
                        }
                    }
                    location = locationDAO.save(sesh, location);
                }
            } else {
                log.debug("Ignoring GPS Location");
            }
        }
    }

    private List<String> populateSurveys(Session sesh, User owner,
            BulkUpload bulkUpload, boolean createMissingData) {
        boolean canCreate = createMissingData
                && (owner.isTeacher() || owner.isPowerStudent());

        Survey survey;
        List<String> missingItems = new ArrayList<String>();
        for (String surveyName : bulkUpload.getSurveyNames()) {
            survey = surveyDAO.getSurveyByName(sesh, surveyName);
            if (survey == null) {
                if (canCreate) {
                    log.debug("Creating Survey: " + surveyName);
                    survey = new Survey();
                    survey.getUsers().add(owner);
                    survey.setName(surveyName);
                    survey.setActive(true);

                    survey = surveyDAO.save(sesh, survey);
                    bulkUpload.addSurvey(survey);
                } else {
                    log.debug("Not creating survey: " + surveyName
                            + " (createMissingData = false)");
                    missingItems.add(surveyName);
                }
            } else {
                log.debug("Retrieved Survey: " + surveyName);
                bulkUpload.addSurvey(survey);
            }
        }
        return missingItems;
    }

    private List<String> populateUsers(Session sesh, User owner,
            BulkUpload bulkUpload, boolean createMissingData) {
        boolean canCreate = createMissingData && owner.isTeacher();

        User user;
        List<String> missingItems = new ArrayList<String>();
        for (String username : bulkUpload.getUsernames()) {
            user = userDAO.getUser(sesh, username);
            if (user == null) {
                if (canCreate) {
                    log.debug("Creating User: " + username);
                    user = new User();

                    user.setFirstName(username);
                    user.setLastName("");
                    user.setEmailAddress("");
                    user.setName(username);

                    user.setPassword(passwordEncoder.encodePassword(StringUtils.generateRandomString(6, 6), null));
                    user.setRegistrationKey(passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), username));
                    user.setActive(false);

                    user = userDAO.save(sesh, user);
                    bulkUpload.addUser(user);
                } else {
                    log.debug("Not creating user: " + username
                            + " (createMissingData = false)");
                    missingItems.add(username);
                }
            } else {
                log.debug("Retrieved User: " + username);
                bulkUpload.addUser(user);
            }
        }
        return missingItems;
    }

    private List<String> populateGroups(Session sesh, User owner,
            BulkUpload bulkUpload, boolean createMissingData) {
        boolean canCreate = createMissingData && owner.isTeacher();
        Group group;
        List<String> missingItems = new ArrayList<String>();
        for (String groupName : bulkUpload.getGroupNames()) {

            if (groupName != null && !groupName.isEmpty()) {
                group = groupDAO.getGroupByName(sesh, groupName);
                if (group == null) {
                    if (canCreate) {
                        log.debug("Creating Group: " + groupName);
                        group = new Group();
                        group.setName(groupName);
                        group = groupDAO.save(sesh, group);
                        bulkUpload.addGroup(group);
                    } else {
                        log.debug("Not creating group: " + groupName
                                + " (createMissingData = false)");
                        missingItems.add(groupName);
                    }

                } else {
                    log.debug("Retrieved Group: " + groupName);
                    bulkUpload.addGroup(group);
                }
            } else {
                log.debug("Ignoring empty group/class name");
            }
        }
        return missingItems;
    }

    private List<String> populateIndicatorSpecies(Session sesh,
            BulkUpload bulkUpload) {
        IndicatorSpecies species;
        List<String> missingItems = new ArrayList<String>();
        for (String scientificName : bulkUpload.getIndicatorSpeciesScientificName()) {
            species = taxaDAO.getIndicatorSpeciesByScientificName(sesh, scientificName);
            if (species == null) {
                log.debug("Cannot find Indicator Species with scientific name: "
                        + scientificName);
                missingItems.add(scientificName);
            } else {
                log.debug("x Retrieved Indicator Species: " + scientificName);
                bulkUpload.addIndicatorSpecies(species);
            }
        }

        for (String commonName : bulkUpload.getIndicatorSpeciesCommonName()) {
            species = taxaDAO.getIndicatorSpeciesByCommonName(sesh, commonName);
            if (species == null) {
                log.debug("Cannot find Indicator Species with common name: "
                        + commonName);
                missingItems.add(commonName);
            } else {
                log.debug("y Retrieved Indicator Species: " + commonName);
                bulkUpload.addIndicatorSpecies(species);
            }
        }

        return missingItems;
    }

    protected abstract RecordRow getRecordRow();
}
