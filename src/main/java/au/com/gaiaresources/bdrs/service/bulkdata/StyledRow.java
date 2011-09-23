package au.com.gaiaresources.bdrs.service.bulkdata;

import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Workbook;

/**
 * A StyledRow is a representation of an XLS row that contains style information.
 */
public interface StyledRow {
    // Cell Styling
    public static final String STYLE_HELP_HEADER = "STYLE_HELP_HEADER";
    public static final String STYLE_LOCATION_HEADER = "STYLE_LOCATION_HEADER";
    public static final String STYLE_RECORD_HEADER = "STYLE_RECORD_HEADER";
    public static final String STYLE_TAXONOMY_HEADER = "STYLE_TAXONOMY_HEADER";
    public static final String STYLE_DATE_CELL = "STYLE_DATE_CELL";
    public static final String STYLE_TIME_CELL = "STYLE_TIME_CELL";
    
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
}
