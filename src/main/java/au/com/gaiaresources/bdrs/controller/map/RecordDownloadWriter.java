package au.com.gaiaresources.bdrs.controller.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.spatial.ShapeFileWriter;
import au.com.gaiaresources.bdrs.util.KMLUtils;

public class RecordDownloadWriter {
    
    private static Logger log = Logger.getLogger(RecordDownloadWriter.class);
    
    public static void write(Session sesh, HttpServletRequest request, HttpServletResponse response, ScrollableRecords sr, RecordDownloadFormat format, User accessingUser) throws Exception {
        
        if (request == null) {
            throw new IllegalArgumentException("HttpServletRequest, request, cannot be null");
        }
        if (response == null) {
            throw new IllegalArgumentException("HttpServletResponse, response, cannot be null");
        }
        if (sr == null) {
            throw new IllegalArgumentException("List<Record>, recordList, cannot be null");
        }
        if (format == null) {
            throw new IllegalArgumentException("RecordDownloadFormat, format, cannot be null");
        }
        
        if (!sr.hasMoreElements()) {
            // a temporary solution to the 'no records' edge case.
            response.getOutputStream().print("There are no records to download");
            return;
        }
        
        if (format == RecordDownloadFormat.KML) {
            response.setContentType(KMLUtils.KML_CONTENT_TYPE);
            response.setHeader("Content-Disposition", "attachment;filename=layer_"+System.currentTimeMillis()+".kml");
            
            try {
                int recordCount = 0;
                List<Record> rList = new ArrayList<Record>(ScrollableRecords.RECORD_BATCH_SIZE);
                while (sr.hasMoreElements()) {
                    rList.add(sr.nextElement());
                    
                    // evict to ensure garbage collection
                    if (++recordCount % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
                        KMLUtils.writeRecordsToKML(accessingUser,
                                                   request.getContextPath(), 
                                                   request.getParameter("placemark_color"), 
                                                   rList, 
                                                   response.getOutputStream());
                        rList.clear();
                        sesh.clear();
                    }
                }
                // Flush the remainder out of the list.
                KMLUtils.writeRecordsToKML(accessingUser,
                                           request.getContextPath(), 
                                           request.getParameter("placemark_color"), 
                                           rList, 
                                           response.getOutputStream());
                sesh.clear();
                
            } catch (JAXBException e) {
                log.error(e);
                throw e;
            } catch (IOException e) {
                log.error(e);
                throw e;
            }
        } else if (format == RecordDownloadFormat.SHAPEFILE) {
            
            // This is not a good thing
            List<Record> recordList = new ArrayList<Record>();
            while(sr.hasMoreElements()) {
                recordList.add(sr.nextElement());
            }
            
            ShapeFileWriter writer = new ShapeFileWriter();
            File zipFile = writer.exportRecords(recordList, accessingUser);
            
            sesh.clear();
            recordList.clear();
            
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
