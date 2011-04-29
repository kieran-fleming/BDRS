package au.com.gaiaresources.bdrs.controller.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.log4j.Logger;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.BodyContentHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.ModelAndView;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import au.com.gaiaresources.bdrs.db.Persistent;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.servlet.view.FileView;

@Component
public class AbstractDownloadFileController {
    @Autowired
    private FileService fileService;

    Logger log = Logger.getLogger(AbstractDownloadFileController.class);

    protected ModelAndView downloadFile(String className, Integer id, String fileName) {
        return downloadFile(className, id, fileName, null, false);
    }
    
    protected ModelAndView downloadFile(String className, Integer id, String fileName, String contentType) {
    	 return downloadFile(className, id, fileName, null, false);
    }
    
    @SuppressWarnings("unchecked")
    protected ModelAndView downloadFile(String className, Integer id, String fileName, String contentType, Boolean base64) {
        Class<? extends Persistent> persistentClass = null;
        try {
            persistentClass = (Class<? extends Persistent>) Class.forName(className);
            FileDataSource file = fileService.getFile(persistentClass, id, fileName);
            FileView fileView = new FileView(file);
            fileView.setEncoding(base64);
            fileView.setFileType(file.getContentType());
            
            if(contentType == null) {
                contentType = getContentType(file);
            }
            fileView.setContentType(contentType);
                
            return new ModelAndView(fileView);
            
        } catch (ClassNotFoundException cnfe) {
            log.error("Class " + className + " does not exist.");
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        } catch (IllegalArgumentException iae) {
            log.error("Unable to download file : " + fileName, iae);
            throw new HTTPException(HttpServletResponse.SC_NOT_FOUND);
        }
    }

    private String getContentType(FileDataSource fileDataSource) {
        String contentType;
        File file = fileDataSource.getFile();
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            ContentHandler contenthandler = new BodyContentHandler();
            AutoDetectParser parser = new AutoDetectParser();
            Metadata metadata = new Metadata();
            metadata.set(Metadata.RESOURCE_NAME_KEY, file.getName());
            parser.parse(fis, contenthandler, metadata);
            contentType = metadata.get(Metadata.CONTENT_TYPE);
            
        } catch (SAXException se) {
            log.warn(se.getMessage(), se);
            contentType = fileDataSource.getContentType();
        } catch (TikaException te) {
            log.warn(te.getMessage(), te);
            contentType = fileDataSource.getContentType();
        } catch(IOException e) {
            log.warn(e.getMessage(), e);
            contentType = fileDataSource.getContentType();
        } finally {
            try {
                if(fis != null) {
                    fis.close();
                }
            } catch(IOException ioe) {
                log.warn(ioe.getMessage(), ioe);
                contentType = fileDataSource.getContentType();
            }
        }
        return contentType;
    }
}
