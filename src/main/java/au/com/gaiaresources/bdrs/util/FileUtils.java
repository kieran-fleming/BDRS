package au.com.gaiaresources.bdrs.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;

import au.com.gaiaresources.bdrs.json.JSON;
import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONException;
import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.json.JSONSerializer;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class FileUtils {
    
    private static final Logger log = Logger.getLogger(FileUtils.class);
    
    private static int MAX_FILENAME_LENGTH = 200;
    
    public static String getContentType(InputStream inputStream) throws FileNotFoundException {
        try {
            ContentHandler contenthandler = new BodyContentHandler();
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            //metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
            parser.parse(inputStream, contenthandler, metadata);
            String contentType = metadata.get(Metadata.CONTENT_TYPE);
            return contentType;
            
        } catch(FileNotFoundException fnfe) {
            throw fnfe;
        } catch(IOException ioe) {
            return "application/octet-stream";
        } catch(TikaException tika) {
            return "application/octet-stream";
        } catch(SAXException se) {
            return "application/octet-stream";
        }
    }
    
    /**
     * Returns the content type of the specified file.
     * 
     * @param file the file to be tested.
     * @return the content type of the file if it can be determined otherwise,
     * returns application/octet-stream.
     * @throws FileNotFoundException thrown if the file cannot be found.
     */
    public static String getContentType(File file) throws FileNotFoundException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            return FileUtils.getContentType(new FileInputStream(file));
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch(IOException ioe) {
                log.error(ioe.getMessage(), ioe);
            }
        }
    }
    
    /**
     * Creates a temp directory with the desired prefix.
     * 
     * @param dirPrefix
     * @return
     * @throws IOException
     */
    public static File createTempDirectory(String dirPrefix) throws IOException {
        if (dirPrefix == null) {
            throw new IllegalArgumentException("String, dirPrefix, cannot be null");
        }
        final File temp;
        temp = File.createTempFile(dirPrefix, Long.toString(System.nanoTime()));
        if(!(temp.delete())) {
            throw new IOException("Could not delete temp file to create temp directory: " + temp.getAbsolutePath());
        }
        if(!(temp.mkdir())) {
            throw new IOException("Could not create temp directory: " + temp.getAbsolutePath());
        }
        temp.deleteOnExit();
        return temp;
    } 
    
    /**
     * creates a new file in the desired directory.
     * 
     * @param directory
     * @param filename
     * @return
     */
    public static File createFileInDir(File directory, String filename) {
        if (directory == null) {
            throw new IllegalArgumentException("File, directory, cannot be null");
        }
        if (filename == null) {
            throw new IllegalArgumentException("String, filename, cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File, directory, must be a directory");
        }
        
        File f = new File(getFilename(directory, filename));
        return f;
    }
    
    /**
     * creates a filename by combining a filename with a path.
     * 
     * @param directory
     * @param filename
     * @return
     */
    public static String getFilename(File directory, String filename) {
        if (directory == null) {
            throw new IllegalArgumentException("File, directory, cannot be null");
        }
        if (filename == null) {
            throw new IllegalArgumentException("String, filename, cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File, directory, must be a directory");
        }
        
        StringBuilder sb = new StringBuilder();
        sb.append(directory.getAbsolutePath());
        sb.append("/");
        sb.append(filename);
        return sb.toString();
    }
    
    public static File getFileFromDir(File directory, String filename) throws IOException {
        if (directory == null) {
            throw new IllegalArgumentException("File, directory, cannot be null");
        }
        if (filename == null) {
            throw new IllegalArgumentException("String, filename, cannot be null");
        }
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("File, directory, must be a directory");
        }
        
        File f = new File(getFilename(directory, filename));
        if (!f.exists()) {
            throw new IOException("File should exist: " + filename);
        }
        return f;
    }
    
    public static void writeBytesToFile(byte[] content, File file) throws IOException {
        InputStream iStream = null; 
        OutputStream oStream = null; 
        try {
            iStream = new ByteArrayInputStream(content);
            oStream = new FileOutputStream(file);
            IOUtils.copy(iStream, oStream);
        } finally {
            if (iStream != null) {
                iStream.close();
            }
            if (oStream != null) {
                oStream.close();
            }
        }
    }
    
    /**
     * Reads an InputStream in JSON format into a {@link JSONArray} or {@link JSONObject}.
     * @param <T> The type of JSON object to create, either {@link JSONArray} or {@link JSONObject}
     * @param fileStream The {@link InputStream} to read
     * @return A {@link JSONArray} or {@link JSONObject} representing the contents of the {@link InputStream}
     * @throws IOException when there is an error reading the {@link InputStream}
     * @throws JSONException when the {@link InputStream} is not properly formatted JSON
     */
    public static JSON readJsonStream(InputStream stream) throws IOException, JSONException {
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(stream));
            StringBuilder configJsonStr = new StringBuilder();
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                configJsonStr.append(line);
            }
            return JSONSerializer.toJSON(configJsonStr.toString());
        } finally {
            if (reader != null) {
                reader.close();
            }
        }
    }
    
    /**
     * Take a string and turn it into a safe filename. Only includes alphanumerics.
     * You must append the extension after using this method.
     * Returns a string of max length 200
     * 
     * @param str string to convert
     * @return the safe filename
     */
    public static String getSafeFilename(String str) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
                // limit the size of the returned string...
                if (sb.length() >= MAX_FILENAME_LENGTH) {
                    break;
                }
            }
        }
        return sb.toString();
    }
}
