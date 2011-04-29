package au.com.gaiaresources.bdrs.service.bulkdata;

import java.text.ParseException;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;

public interface RecordRow {

    public static final String GPS_LOCATION = "GPS LOCATION";
    
    // Cell Styling
    public static final String STYLE_HELP_HEADER = "STYLE_HELP_HEADER";
    public static final String STYLE_LOCATION_HEADER = "STYLE_LOCATION_HEADER";
    public static final String STYLE_RECORD_HEADER = "STYLE_RECORD_HEADER";
    public static final String STYLE_TAXONOMY_HEADER = "STYLE_TAXONOMY_HEADER";
    public static final String STYLE_DATE_CELL = "STYLE_DATE_CELL";
    public static final String STYLE_TIME_CELL = "STYLE_TIME_CELL";
    
    /**
     * Writes core help text to the specified help sheet.
     * @param helpSheet The sheet where help text shall be written.
     * @param rowIndex The row index where help text shall start to be written.
     * @return The next available row after help text has beeen written.
     */
    public int writeCoreHelp(Sheet helpSheet, int rowIndex);
    
    /**
     * Creates all cell styles for this implementation and registers it with
     * the style table of the workbook.
     *  
     * @param wb the workbook where the styles shall be used.
     */
    public void createCellStyles(Workbook wb);
    
    /**
     * Returns the style mapped against the specified key.
     * @param styleKey the unique name of the style.
     * @return the cell style for the specified key or null if one does not
     * exist.
     */
    public CellStyle getCellStyleByKey(String styleKey);

    public void writeHeader(Row headerRow, Survey survey);

    public void writeRow(LSIDService lsidService, Row createRow, Record r);

    public void readHeader(Survey survey, Row row) throws ParseException;

    public RecordUpload readRow(Survey survey, Row row);
    
    public boolean isHeader(Row row);
}
