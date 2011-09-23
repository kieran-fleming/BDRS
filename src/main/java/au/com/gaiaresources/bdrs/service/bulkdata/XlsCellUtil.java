package au.com.gaiaresources.bdrs.service.bulkdata;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.CellReference;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;

/**
 * Provides utility functions when working with XLS cells.
 */
public class XlsCellUtil {
    
    /**
     *  The expected format for dates coming from XLS.
     */
    public static final SimpleDateFormat DATE_FORMATTER;
    /**
     * The expected format for time coming from XLS.
     */
    public static final SimpleDateFormat TIME_FORMATTER;
    
    static {
        DATE_FORMATTER = new SimpleDateFormat("dd MMM yyyy");
        DATE_FORMATTER.setLenient(false);
        
        TIME_FORMATTER = new SimpleDateFormat("HH:mm");
    }
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * Returns the cell in the first row and first column of the specified sheet,
     * not including the header.
     *
     * @param sheet the sheet containing the cell to be returned.
     * @param headerRowCount the number of rows in the header.
     */
    public static Cell getTopLeftCell(Sheet sheet, int headerRowCount) {
        Row firstDataRow = sheet.getRow(sheet.getFirstRowNum() + headerRowCount);
        return firstDataRow.getCell(firstDataRow.getFirstCellNum());
    }
    
    /**
     * Returns the cell in the last row and last column of the specified sheet.
     *
     * @param sheet the sheet containing the cell to be returned.
     */
    public static Cell getBottomRightCell(Sheet sheet) {
        Row cmLastDataRow = sheet.getRow(sheet.getLastRowNum());
        return cmLastDataRow.getCell(cmLastDataRow.getLastCellNum()-1);
    }

    /**
     * Returns the string representation of this cell coordinate. For example,
     * A1, B2 and C3. Note that this method does not include the sheet name when
     * returning the coordinate.
     */
    public static String cellToCellReferenceString(Cell cell) {
        return new CellReference(cell.getRowIndex(),
                                 cell.getColumnIndex()).formatAsString();
    }
    
    /**
     * Converts the content of the specified cell to a String.
     * <ul>
     *     <li>Blank cells are converted to empty string</li>
     *     <li>Boolean cells are converted using {@link String#valueOf(boolean)}</li>
     *     <li>Error cells throw an {@link IllegalStateException}</li>
     *     <li>Formula Cells are evaluated and the stringified result returned.</li>
     *     <li>Numeric Cells are converted using {@link String#valueOf(double)}</li>
     *     <li>String cells are returned as is</li>
     * </ul>
     *
     * @param cell the cell containing the content to be returned.
     */
    public static String cellToString(Cell cell) {
        String ret = null;
        switch(cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                ret = "";
                break;
            case Cell.CELL_TYPE_BOOLEAN:
                ret = String.valueOf(cell.getBooleanCellValue());
                break;
            case Cell.CELL_TYPE_ERROR:
                throw new IllegalStateException("Cannot convert an error cell to a string.");
            case Cell.CELL_TYPE_FORMULA:
                ret = cellToString(evaluateFormulaCell(cell));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                ret = String.valueOf(cell.getNumericCellValue());
                break;
            case Cell.CELL_TYPE_STRING:
                ret = cell.getStringCellValue();
                break;
            default:
                throw new IllegalStateException("Unknown Cell Type: "+cell.getCellType());
        }
        return ret;
    }
    
    /**
     * Converts the content of the specified cell to a Date.
     * <ul>
     *     <li>Blank cells throw an {@link IllegalStateException}</li>
     *     <li>Boolean cells throw an {@link IllegalStateException}</li>
     *     <li>Error cells throw an {@link IllegalStateException}</li>
     *     <li>Formula Cells are evaluated and the date result returned</li>
     *     <li>Numeric Cells are converted to a date by the cell and returned</li>
     *     <li>String cells are parsed using the {@value #DATE_FORMATTER} and returned</li>
     * </ul>
     *
     * @param cell the cell containing the content to be returned.
     */
    public static Date cellToDate(Cell cell) {
        Date ret = null;
        switch(cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                throw new IllegalStateException("Cannot convert a blank cell to a date.");
            case Cell.CELL_TYPE_BOOLEAN:
                throw new IllegalStateException("Cannot convert a boolean value to a date.");
            case Cell.CELL_TYPE_ERROR:
                throw new IllegalStateException("Cannot convert an error cell to a date.");
            case Cell.CELL_TYPE_FORMULA:
                ret = cellToDate(evaluateFormulaCell(cell));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                ret = cell.getDateCellValue();
                break;
            case Cell.CELL_TYPE_STRING:
                try {
                    ret = DATE_FORMATTER.parse(cell.getStringCellValue());
                } catch(ParseException pe) {
                    throw new IllegalStateException(pe);
                }
                break;
            default:
                throw new IllegalStateException("Unknown Cell Type: "+cell.getCellType());
        }
        return ret;
    }
    
    /**
     * Converts the content of the specified cell to a double.
     * <ul>
     *     <li>Blank cells throw an {@link IllegalStateException}</li>
     *     <li>Boolean cells throw an {@link IllegalStateException}</li>
     *     <li>Error cells throw an {@link IllegalStateException}</li>
     *     <li>Formula Cells are evaluated and the double result returned.</li>
     *     <li>Numeric Cells are returned as is</li>
     *     <li>String cells are parsed using {@link Double#parseDouble(String)} and returned</li>
     * </ul>
     *
     * @param cell the cell containing the content to be returned.
     */
    public static double cellToDouble(Cell cell) {
        double ret;
        switch(cell.getCellType()) {
            case Cell.CELL_TYPE_BLANK:
                throw new IllegalStateException("Cannot convert a blank cell to a number.");
            case Cell.CELL_TYPE_BOOLEAN:
                throw new IllegalStateException("Cannot convert a boolean value to a number.");
            case Cell.CELL_TYPE_ERROR:
                throw new IllegalStateException("Cannot convert an error cell to a number.");
            case Cell.CELL_TYPE_FORMULA:
                ret = cellToDouble(evaluateFormulaCell(cell));
                break;
            case Cell.CELL_TYPE_NUMERIC:
                ret = cell.getNumericCellValue();
                break;
            case Cell.CELL_TYPE_STRING:
                ret = Double.parseDouble(cell.getStringCellValue());
                break;
            default:
                throw new IllegalStateException("Unknown Cell Type: "+cell.getCellType());
        }
        return ret;
    }
    
    /**
     * Converts the content of the specified cell to a Date. This method does
     * the same thing as {@link #cellToDate(Cell)} except that if a string is
     * received, this method will use a {@link #TIME_FORMATTER} to parse the 
     * text.
     *
     * @param cell the cell containing the content to be returned.
     */
    public static Date cellToTime(Cell cell) {
        Date ret = null;
        if(cell.getCellType() == Cell.CELL_TYPE_STRING) {
            try {
                ret = TIME_FORMATTER.parse(cell.getStringCellValue());
            } catch(ParseException pe) {
                throw new IllegalStateException(pe);
            }
        } else {
            ret = cellToDate(cell);
        }
        return ret;
    }
    
    private static Cell evaluateFormulaCell(Cell cell) {
        FormulaEvaluator evaluator = cell.getSheet().getWorkbook().getCreationHelper().createFormulaEvaluator();
        return evaluator.evaluateInCell(cell);
    }
}