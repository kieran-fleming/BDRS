package au.com.gaiaresources.bdrs.controller.map;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.kml.KMLWriter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.bulkdata.BulkDataService;
import au.com.gaiaresources.bdrs.spatial.ShapeFileWriter;
import au.com.gaiaresources.bdrs.util.KMLUtils;

public class RecordDownloadWriter {
    
    private static Logger log = Logger.getLogger(RecordDownloadWriter.class);
    

    /**
     * Writes out the records using the specified format to the response. This
     * function primarily serves the use case where records exist in multiple
     * surveys or when the survey itself is irrelevant (e.g KML and Shapefile)
     * 
     * @param sesh the database session to retrieve the records.
     * @param request the browser request for encoded records.
     * @param response the server response to the browser.
     * @param sr the records to encode.
     * @param format the encoding format. Note that this function does not support XLS format. Use {@link #write(BulkDataService, RecordDownloadFormat, OutputStream, Session, String, Survey, User, ScrollableRecords)}
     * @param accessingUser the user requesting the encoding of records.
     * @throws Exception
     */
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
        
        switch (format) {
        case KML:
            response.setContentType(KMLUtils.KML_CONTENT_TYPE);
            response.setHeader("Content-Disposition", "attachment;filename=layer_"+System.currentTimeMillis()+".kml");
            break;
        case SHAPEFILE:
            response.setContentType("application/octet-stream");
            response.setHeader("Content-Disposition", "attachment;filename=record_export_"+System.currentTimeMillis()+".zip");
            break;
        case XLS:
            throw new IllegalArgumentException("Cannot write XLS Records without a Survey.");
        default:
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.error("Records were requested to be downloaded in an invalid format : " + format);
            return;
        }
        
        try {
            write(null, format, response.getOutputStream(), sesh, request.getContextPath(), null, accessingUser, sr);
        } catch (IOException ioe) {
            // This may occur if the user has 
            // switched tabs before the kml has been provided.
        }
    }
    
    /**
     * Writes out the records using the specified format to the output stream.
     * 
     * @param bulkDataService only used for XLS encoding records. Can be null for other formats.
     * @param format the format to encode the records.
     * @param out the output stream where encoded records will be written.
     * @param sesh the database session to retrieve the records.
     * @param contextPath the context path of the web application.
     * @param survey the survey containing the records. Only used for XLS formay. Can be null for other formats.
     * @param accessingUser the user requesting the encoding of records.
     * @param sr the records to be encoded.
     * @throws Exception
     */
    public static void write(BulkDataService bulkDataService, RecordDownloadFormat format, OutputStream out,
            Session sesh, String contextPath, Survey survey, User accessingUser,
            ScrollableRecords sr) throws Exception {
        
        switch (format) {
        case KML:
            writeKMLRecords(out, sesh, contextPath, accessingUser, sr);
            break;
        case SHAPEFILE:
            writeSHPRecords(out, sesh, accessingUser, survey, sr);
            break;
        case XLS:
            writeXLSRecords(bulkDataService, out, survey, sr, sesh);
            break;
        default:
            // Do nothing
            log.error("Unknown RecordDownloadFormat: "+format);
            break;
        }
    }
    
    private static void writeXLSRecords(BulkDataService bulkDataService,
            OutputStream out, Survey survey,
            ScrollableRecords sr, Session sesh) throws Exception {
        bulkDataService.exportSurveyRecords(sesh, survey, sr, out);
    }

    private static void writeSHPRecords(OutputStream out, Session sesh,
            User accessingUser, Survey survey, ScrollableRecords sr) throws Exception,
            FileNotFoundException, IOException {
        // This is not a good thing
        // -----
        // Yes, I believe I just made it worse
        List<Record> recordList = new ArrayList<Record>();

        while(sr.hasMoreElements()) {
            Record r = sr.nextElement();
            if (survey == null || r.getSurvey() == survey) {
                recordList.add(r);
            }
        }
        
        if (!recordList.isEmpty()) {
            
            // no point writing a non empty shapefile since the user is not
            // expecting a template in this download but a populated shapefile
            ShapeFileWriter writer = new ShapeFileWriter();
            File zipFile = writer.exportRecords(recordList, accessingUser);
            
            sesh.clear();
            recordList.clear();
            
            FileInputStream inStream = null;
            try {
                inStream = new FileInputStream(zipFile);
                IOUtils.copy(inStream, out);
            } finally {
                if (inStream != null) {
                    inStream.close();
                }
            }    
        }
    }

    private static void writeKMLRecords(OutputStream out, Session sesh,
            String contextPath, User accessingUser, ScrollableRecords sr)
            throws JAXBException {
        int recordCount = 0;
        List<Record> rList = new ArrayList<Record>(ScrollableRecords.RECORD_BATCH_SIZE);
        KMLWriter writer = KMLUtils.createKMLWriter(contextPath, null);
        while (sr.hasMoreElements()) {
            rList.add(sr.nextElement());
            
            // evict to ensure garbage collection
            if (++recordCount % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
                
                KMLUtils.writeRecords(writer, accessingUser, contextPath, rList);
                rList.clear();
                sesh.clear();
            }
        }
        // Flush the remainder out of the list.
        KMLUtils.writeRecords(writer, accessingUser, contextPath, rList);
        sesh.clear();
        writer.write(false, out);
    }
}
