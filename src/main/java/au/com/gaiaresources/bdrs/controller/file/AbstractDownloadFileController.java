package au.com.gaiaresources.bdrs.controller.file;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Date;

import javax.activation.FileDataSource;
import javax.servlet.http.HttpServletResponse;
import javax.xml.ws.http.HTTPException;

import org.apache.commons.io.IOUtils;
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

import com.ibm.icu.text.DateFormat;
import com.ibm.icu.text.SimpleDateFormat;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.db.Persistent;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.servlet.view.FileView;
import au.com.gaiaresources.bdrs.util.ZipUtils;

@Component
public class AbstractDownloadFileController extends AbstractController {
    private static final String FILE_DATE_FORMAT = "YYYY-MM-dd-HH-mm";

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

    protected void downloadFile(File file, HttpServletResponse response, String filePrefix, String fileSuffix) throws IOException {
        downloadFile(file, response, filePrefix, fileSuffix, "application/octet-stream");
    }
    
    protected void downloadFile(File file, HttpServletResponse response, String filePrefix, String fileSuffix, String contentType) throws IOException {
        FileInputStream inStream = null;
        try {
            inStream = new FileInputStream(file);
            downloadFileFromStream(inStream, response, filePrefix, fileSuffix, contentType);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
        }
    }

    protected void downloadFileFromStream(InputStream inStream, HttpServletResponse response, String filePrefix, String fileSuffix, String contentType) throws IOException {
        response.setContentType(contentType);
        response.setHeader("Content-Disposition", "attachment;filename=\""+ filePrefix + "_" + 
                           new SimpleDateFormat(FILE_DATE_FORMAT).format(new Date(System.currentTimeMillis())) + "." + fileSuffix + "\"");
        try {
            int length = IOUtils.copy(inStream, response.getOutputStream());
            response.setContentLength(length);
        } finally {
            if (inStream != null) {
                inStream.close();
            }
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
    
    /**
     * Convenience method for zipping an array of directories into one single OutputStream, 
     * copying it to an InputStream and returning it in the response.
     * @param listFiles
     * @param response
     * @param name
     */
    protected void downloadFilesAsZip(File[] listFiles, HttpServletResponse response, String name) {
        ByteArrayOutputStream out = null;
        ByteArrayInputStream in = null;
        try {
            out = new ByteArrayOutputStream();
            ZipUtils.compressToStream(listFiles, out);
            in = new ByteArrayInputStream(out.toByteArray());
            downloadFileFromStream(in, response, name, "zip", "application/octet-stream");
        } catch (IOException e) {
            log.error("Error downloading file: ", e);
            getRequestContext().addMessage("bdrs.file.zip.error", new Object[]{});
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    log.error("Error closing input file", e);
                }
            }
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    log.error(e);
                }
            }
        }
    }
}
