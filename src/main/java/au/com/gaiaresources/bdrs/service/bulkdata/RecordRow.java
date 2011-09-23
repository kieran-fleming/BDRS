package au.com.gaiaresources.bdrs.service.bulkdata;

import java.text.ParseException;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;

public interface RecordRow extends StyledRow {

    public static final String GPS_LOCATION = "GPS LOCATION";
    
    /**
     * Writes core help text to the specified help sheet.
     * @param helpSheet The sheet where help text shall be written.
     * @param rowIndex The row index where help text shall start to be written.
     * @return The next available row after help text has beeen written.
     */
    public int writeCoreHelp(Sheet helpSheet, int rowIndex);

    public void writeHeader(Row superHeaderRow, Row headerRow, Survey survey);

    public void writeRow(LSIDService lsidService, Row createRow, Record r);

    public void readHeader(Survey survey, Row superHeaderRow, Row row) throws ParseException;

    public RecordUpload readRow(Survey survey, Row row);
    
    public boolean isHeader(Row row);
}
