package au.com.gaiaresources.bdrs.service.bulkdata;

import java.util.HashMap;
import java.util.Map;

import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Workbook;

public class StyledRowImpl implements StyledRow {
    
    private Map<String, CellStyle> cellStyleMap = new HashMap<String, CellStyle>();

    public StyledRowImpl() {
        super();
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
    
    
}