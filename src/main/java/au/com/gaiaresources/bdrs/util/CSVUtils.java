package au.com.gaiaresources.bdrs.util;

import java.io.IOError;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import au.com.bytecode.opencsv.CSVParser;
import au.com.bytecode.opencsv.CSVReader;
import au.com.bytecode.opencsv.CSVWriter;
import edu.emory.mathcs.backport.java.util.Arrays;

public class CSVUtils {
    
    public static String[] fromCSVString(String csvStr) {
        return CSVUtils.fromCSVString(csvStr, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER);
    }
    
    public static String[] fromCSVString(String csvStr, char separator, char quotechar) {
        String[] split = null;
        try {
            CSVReader csvReader = new CSVReader(new StringReader(csvStr), separator, quotechar);
            split = csvReader.readNext();
            csvReader.close();
        } catch(IOException ioe) {
            // This can't happen because we are not doing any file or stream IO.
            throw new IOError(ioe);
        }
                
        if(split == null) {
            split = new String[]{};
        }
        return split;
    }
    
    public static String toCSVString(String[] values, boolean sortValues) {
        return CSVUtils.toCSVString(values, CSVParser.DEFAULT_SEPARATOR, CSVParser.DEFAULT_QUOTE_CHARACTER, sortValues);
    }
    
    public static String toCSVString(String[] values, char separator, char quotechar, boolean sortValues) {
        String stringValue;
        if(values == null) {
                stringValue = "";
        } else {
            try {
                String[] copy = new String[values.length];
                System.arraycopy(values, 0, copy, 0, values.length);
                if (sortValues) {
                    Arrays.sort(copy);
                }
                
                StringWriter writer = new StringWriter();
                CSVWriter csvWriter = new CSVWriter(writer, separator, quotechar);
                csvWriter.writeNext(copy);
                stringValue = writer.toString();
                
                csvWriter.close();
                writer.close();
            } catch(IOException ioe) {
                // This cannot happen
                throw new IOError(ioe);
            }
        }
        return stringValue;
    }
    
    public static boolean hasValue(String[] csvValues, String strToFind) {
        Arrays.sort(csvValues);
        return Arrays.binarySearch(csvValues, strToFind) >= 0;
    }
    
    public static boolean hasValue(String csv, String strToFind) {
        String[] strArray = fromCSVString(csv);
        return hasValue(strArray, strToFind);
    }
}
