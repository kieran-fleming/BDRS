package au.com.gaiaresources.bdrs.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;


public class FileUtils {
    
    private static final Logger log = Logger.getLogger(FileUtils.class);
    
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
}
