package au.com.gaiaresources.bdrs.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

import org.apache.log4j.Logger;

/**
 * Ensures thread safe access to DateFormat instances.
 * @author Tim Carpenter
 */
public class DateFormatter {
    private static ThreadLocal<Map<String, DateFormat>> threadFormatters = new ThreadLocal<Map<String, DateFormat>>();
    
    /** Date format string for 01 Jan 2007. */
    public static final String DAY_MONTH_YEAR = "dd MMM yyyy";
    /** Date format string to 01 Jan 2007 12:34. */
    public static final String DAY_MONTH_YEAR_TIME = "dd MMM yyyy HH:mm";
    /** Date format string to 01st January 2007. */
    public static final String LONG_DAY_MONTH_YEAR = "dd MMMMM yyyy";
    /** Date format string to 12:12. */
    public static final String TIME = "HH:mm";
    
    private static Logger logger = Logger.getLogger(DateFormatter.class);

    /**
     * Format a date.
     * @param date <code>Date</code>.
     * @param format The format <code>String</code>.
     * @param useGMT Should GMT be used or should the timezone be taken into account.
     * @return <code>String</code>.
     */
    public static String format(Date date, String format, boolean useGMT) {
        if (useGMT) {
            DateFormat f = new SimpleDateFormat(format);
            f.setTimeZone(TimeZone.getTimeZone("GMT"));
            return f.format(date);
        } else {
            return format(date, format);
        }
    }
    
    /**
     * Format a date.
     * @param date <code>Date</code>.
     * @param format The format <code>String</code>.
     * @return <code>String</code>.
     */
    public static String format(Date date, String format) {
        return getFormatter(format).format(date);
    }
    
    /**
     * Parse a string into a date.
     * @param date The date string.
     * @param format The format string.
     * @return <code>Date</code>.
     */
    public static Date parse(String date, String format) {
        try {
            return getFormatter(format).parse(date);
        } catch (ParseException pe) {
            logger.error("Failed to parse " + date + " into format " + format, pe);
        }
        return null;
    }
    
    /**
     * Get a <code>DateFormat</code>.
     * @param format <code>String</code>.
     * @return <code>DateFormat</code>.
     */
    public static DateFormat getFormatter(String format) {
        Map<String, DateFormat> threadFormats = threadFormatters.get();
        if (threadFormats == null) {
            threadFormats = new HashMap<String, DateFormat>();
            threadFormatters.set(threadFormats);
        }
        DateFormat formatter = null;
        if (threadFormats.containsKey(format)) {
            formatter = threadFormats.get(format);
        } else {
            formatter = new SimpleDateFormat(format);
            threadFormats.put(format, formatter);
        }
        return formatter;
    }
}

