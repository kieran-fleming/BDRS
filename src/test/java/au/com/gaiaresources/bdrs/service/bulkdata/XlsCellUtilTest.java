package au.com.gaiaresources.bdrs.service.bulkdata;

import java.util.Calendar;
import java.util.Date;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.apache.poi.ss.formula.FormulaParseException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.Comment;
import org.apache.poi.ss.usermodel.Hyperlink;
import org.apache.poi.ss.usermodel.RichTextString;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.junit.Test;

public class XlsCellUtilTest {

    private Logger log = Logger.getLogger(getClass());
    
    @Test
    public void testTimeParsing() {        
        assertTimeParsing(9, 14);
        assertTimeParsing(15, 30);
        assertTimeParsing(0, 0);
        assertTimeParsing(23, 59);
        assertTimeParsing(0, 1);
    }
    
    /**
     * @param hour (24 hour time)
     * @param min
     */
    private void assertTimeParsing(int hour, int min) {
        // 4 bits used, 16 values inclusive of 0. i.e...
        for (int i=0; i<16; ++i) {
            assertTimeParsing(hour, min, ":", i);
            assertTimeParsing(hour, min, ".", i);
            assertTimeParsing(hour, min, "", i);
            assertTimeParsing(hour, min, " ", i);
            assertTimeParsing(hour, min, "-", i);
            assertTimeParsing(hour, min, "/", i);
        }
    }
    
    // bit masks
    private static final int TWENTY_FOUR_HOUR_MASK = 8;
    private static final int CAPS_MASK = 4;
    private static final int SPACE_MASK = 2;
    private static final int LEADING_ZERO_MASK = 1;
    
    private void assertTimeParsing(int hour, int min, String sep, int switches) {
        Date time = getTime(hour, min);
        boolean twentyFourHour = bitmask(switches, TWENTY_FOUR_HOUR_MASK);
        boolean caps = bitmask(switches, CAPS_MASK);
        boolean space = bitmask(switches, SPACE_MASK);
        boolean leadingZero = bitmask(switches, LEADING_ZERO_MASK);
        Assert.assertEquals("date should be equals", time, XlsCellUtil.cellToTime(getTimeStringCell(hour, min, sep, twentyFourHour, caps, space, leadingZero)));
    }
    
    private boolean bitmask(int value, int mask) {
        return (value & mask) != 0;
    }
    
    private Cell getTimeStringCell(int hour, int min, String separator, boolean twentyfourHourFormat, boolean caps, boolean space, boolean leadingZero) {
        
        StringBuilder sb = new StringBuilder();
        sb.append("  ");  // add some leading space
        if (hour < 9 && !leadingZero) {
            sb.append(String.format("%d", hour));
        } else {
            sb.append(String.format("%02d", hour));
        }
        sb.append(separator);
        sb.append(String.format("%02d", min));
        if (space) {
            sb.append(" ");
        }
        if (!twentyfourHourFormat) {
            if (hour <= 12) {
                if (caps) {
                    sb.append("A.M");
                } else {
                    sb.append("a.m.");
                }
            } else {
                if (caps) {
                    sb.append("PM.");
                } else {
                    sb.append("pm");
                }
            }
        }
        sb.append("  "); // add some trailing space
        return createStringCell(sb.toString());
    }
    
    public Cell createStringCell(String value) {
        Cell c = new MockCell();
        c.setCellType(Cell.CELL_TYPE_STRING);
        c.setCellValue(value);
        return c;
    }
    
    private Date getTime(int hour, int min) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(Calendar.HOUR_OF_DAY, hour);
        cal.set(Calendar.MINUTE, min);
        return cal.getTime();
    }
    
    /**
     * For this test we only call about the string cell type
     *
     */
    private static class MockCell implements Cell {
        
        private int cellType;
        String stringCellValue;

        @Override
        public boolean getBooleanCellValue() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public int getCachedFormulaResultType() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public Comment getCellComment() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public String getCellFormula() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public CellStyle getCellStyle() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public int getCellType() {
            return cellType;
        }

        @Override
        public int getColumnIndex() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public Date getDateCellValue() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public byte getErrorCellValue() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public Hyperlink getHyperlink() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public double getNumericCellValue() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public RichTextString getRichStringCellValue() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public Row getRow() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public int getRowIndex() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public Sheet getSheet() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public String getStringCellValue() {
            return stringCellValue;
        }

        @Override
        public void removeCellComment() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setAsActiveCell() {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellComment(Comment arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellErrorValue(byte arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellFormula(String arg0) throws FormulaParseException {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellStyle(CellStyle arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellType(int arg0) {
            cellType = arg0;
        }

        @Override
        public void setCellValue(double arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellValue(Date arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellValue(Calendar arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellValue(RichTextString arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setCellValue(String arg0) {
            stringCellValue = arg0;
        }

        @Override
        public void setCellValue(boolean arg0) {
            throw new IllegalStateException("not implemented for test");
        }

        @Override
        public void setHyperlink(Hyperlink arg0) {
            throw new IllegalStateException("not implemented for test");
        }
        
    }
}
