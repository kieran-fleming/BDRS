package au.com.gaiaresources.bdrs.controller.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.spatial.ShapeFileWriter;
import au.com.gaiaresources.bdrs.util.KMLUtils;

public class RecordDownloadWriter {
    
    private static Logger log = Logger.getLogger(RecordDownloadWriter.class);
    
    public static void write(HttpServletRequest request, HttpServletResponse response, List<Record> recordList, RecordDownloadFormat format, User accessingUser) throws Exception {
        
        if (request == null) {
            throw new IllegalArgumentException("HttpServletRequest, request, cannot be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("HttpServletResponse, response, cannot be null");
        }
        if (recordList == null) {
            throw new IllegalArgumentException("List<Record>, recordList, cannot be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("RecordDownloadFormat, format, cannot be null");
        }
        
        if (recordList.isEmpty()) {
            // a temporary solution to the 'no records' edge case.
            response.getOutputStream().print("There are no records to download");
            return;
        }
        
        if (format == RecordDownloadFormat.KML) {
            response.setContentType(KMLUtils.KML_CONTENT_TYPE);
            response.setHeader("Content-Disposition", "attachment;filename=layer_"+System.currentTimeMillis()+".kml");
            
            try {
                KMLUtils.writeRecordsToKML(accessingUser,
                                       request.getContextPath(), 
                                       request.getParameter("placemark_color"), 
                                       recordList, 
                                       response.getOutputStream());
            } catch (JAXBException e) {
                log.error(e);
                throw e;
            } catch (IOException e) {
                log.error(e);
                throw e;
            }
        } else if (format == RecordDownloadFormat.SHAPEFILE) {
            ShapeFileWriter writer = new ShapeFileWriter();
            File zipFile = writer.exportRecords(recordList, accessingUser);
            
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=record_export_"+System.currentTimeMillis()+".zip");
            
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(zipFile);
                IOUtils.copy(inStream, response.getOutputStream());
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }
        } else {
            // invalid format requested...
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.error("Records were requested to be downloaded in an invalid format : " + format);
            return;
        }
    }
}
