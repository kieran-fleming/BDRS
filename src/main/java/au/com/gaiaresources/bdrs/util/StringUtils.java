package au.com.gaiaresources.bdrs.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.List;
import java.util.Random;

import org.apache.log4j.Logger;
import org.w3c.tidy.Tidy;

import au.com.gaiaresources.bdrs.controller.record.validator.HtmlValidator.MyTidyMessageListener;

/**
 * String utilities.
 * @author Tim Carpenter
 *
 */
public final class StringUtils {
    /**
     * All alpha numeric characters.
     */
    public static final String ALPHA_NUMERIC_CHARACTERS = 
                                            "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
    
    private static Logger log = Logger.getLogger(StringUtils.class);
    
    /**
     * Generate a random string containing characters from <code>ALPHA_NUMERIC_CHARACTERS</code>.
     * @param minLength <code>int</code> the min length of the string.
     * @param maxLength <code>int</code> the max length of the string.
     * @return <code>String</code>.
     */
    public static String generateRandomString(int minLength, int maxLength) {
        Random generator = new Random();
        int length = Math.max(minLength, generator.nextInt(maxLength));
        StringBuffer randomString = new StringBuffer();
        for (int i = 0; i < length; i++) {
            int charIndex = generator.nextInt(ALPHA_NUMERIC_CHARACTERS.length() - 1);
            randomString.append(ALPHA_NUMERIC_CHARACTERS.charAt(charIndex));
        }
        return randomString.toString();
    }
    
    /**
     * Check if the given string is null or has zero length.
     * @param s <code>String</code>.
     * @return <code>boolean</code>.
     */
    public static boolean nullOrEmpty(String s) {
        return s == null || s.length() == 0;
    }
    
    /**
     * Check if the given string has a non-zero length.
     * @param s <code>String</code>.
     * @return <code>boolean</code>.
     */
    public static boolean notEmpty(String s) {
        return !nullOrEmpty(s);
    }
    
    /**
     * Convert a Throwable to a pretty string.
     * @param t <code>Throwable</code>.
     * @return The formatted string.
     */
    public static String prettyPrintThrowable(Throwable t) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw, true);
        t.printStackTrace(pw);
        pw.flush();
        sw.flush();
        return sw.toString();
    }
    
    /**
     * Null safe toString returning an empty string for nulls.
     * @param o <code>Object</code>.
     * @return <code>String</code>.
     */
    public static String toString(Object o) {
        if (o == null) {
            return "";
        }
        return o.toString();
    }
    
    public static String substitution(String source, String indexer, String ... substitutes) {
        String substituted = source;
        if (substitutes != null) {
            for (int i = 0; i < substitutes.length; i++) {
                substituted = substituted.replaceAll(indexer + i, substitutes[i]);
            }
        }
        return substituted;
    }
    
    /**
     * Concatentate some String together with a given delimiter. 
     * Optionally, delimit where the array entry is null or empty.
     * For example:
     * <br/><br/>
     * <code>StringUtils.buildDelimitedConcatenation(new String[] {"A", null, "C"}, ":", false);</code>
     * <br/><br/>
     * gives<br/><br/>
     * <code>A:C</code><br/><br/>
     * whereas<br/><br/>
     * <code>StringUtils.buildDelimitedConcatenation(new String[] {"A", null, "C"}, ":", true);</code><br/><br/>
     * gives<br/><br/>
     * <code>A::C</code><br/><br/>
     * and<br/><br/>
     * <code>StringUtils.buildDelimitedConcatenation(new String[] {"A", "", "C"}, ":", false);</code><br/><br/>
     * gives<br/><br/>
     * <code>A:C</code>
     * @param components <code>String[]</code>.
     * @param delimiter <code>String</code>.
     * @param delimitEmpty <code>boolean</code>.
     * @return <code>String</code>.
     */
    public static String buildDelimitedConcatenation(String[] components, String delimiter, boolean delimitEmpty) {
        StringBuffer buffer = new StringBuffer();
        for (String s : components) {
            if (buffer.length() > 0) {
                if (notEmpty(s) || delimitEmpty) {
                    buffer.append(delimiter);
                }
            }
            if (s != null) {
                buffer.append(s);
            }
        }
        return buffer.toString();
    }
    
    /**
     * Removes all non-alphanumeric characters from a string.
     * @param s <code>String</code> source.
     * @return <code>String</code>.
     */
    public static String removeNonAlphaNumerics(String s) {
        return s.replaceAll("[^\\w]", "");
    }
    
    /**
     * Validates and "tidies" an HTML String using Tidy.
     * @param html The HTML String to validate and "tidy"
     * @return The "tidied" HTML String or null if the String was invalid
     */
    public static String validateHtml(String html) {
        return validateHtml(html, null);
    }
    
    public static String validateHtml(String html, MyTidyMessageListener listener) {
        if (nullOrEmpty(html)) {
            return null;
        }
        
        StringWriter sw = new StringWriter();
        PrintWriter pw = null;
        try {
            Tidy tidy = new Tidy();
            // create a listener to handle errors
            if (listener == null) {
                listener = new MyTidyMessageListener();
            }
            tidy.setMessageListener(listener);
            // create an output stream to redirect output from Tidy to the log file
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            pw = new PrintWriter(out);
            tidy.setErrout(pw);
            tidy.setXHTML(false);
            
            tidy.parse(new StringReader(html), sw);
            
            log.warn(out.toString());
            // the listener only stores error messages, so if it has a message,
            // there was an error in the validation
            return listener.getErrors().size() > 0 ? null : sw.toString();
        } finally {
            if (pw != null) {
                pw.close();
            }
            try {
                sw.close();
            } catch (IOException e) {
                
            }
        }
    }

    /**
     * Replaces all the '\' in the regex with '\\'.
     * @param regex The regular expression to escape
     * @return The escaped regular expression
     */
    public static String escapeRegex(String regex) {
        return regex.replaceAll("\\\\", "\\\\\\\\");
    }

    /**
     * Returns a String of the list elements separated by the delimiter.
     * @param list The list of elements to join into a String
     * @param delimiter The delimiter by which to separate the list items
     * @return A String of list items separated by a delimiter
     */
    public static String joinList(List<? extends Object> list, String delimiter) {
        StringBuilder sb = new StringBuilder();
        for (Object object : list) {
            sb.append(object.toString());
            sb.append(delimiter);
        }
        // remove trailing delimiter
        if (sb.length() > 0) {
            sb.setLength(sb.length()-1);
        }
        return sb.toString();
    };
}
