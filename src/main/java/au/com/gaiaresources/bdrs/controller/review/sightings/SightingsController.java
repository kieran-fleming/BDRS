package au.com.gaiaresources.bdrs.controller.review.sightings;

import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.bind.JAXBException;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.map.RecordDownloadFormat;
import au.com.gaiaresources.bdrs.controller.map.RecordDownloadWriter;
import au.com.gaiaresources.bdrs.kml.KMLWriter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.bulkdata.BulkDataService;
import au.com.gaiaresources.bdrs.util.FileUtils;
import au.com.gaiaresources.bdrs.util.KMLUtils;

public abstract class SightingsController extends AbstractController {
    
    private Logger log = Logger.getLogger(getClass());

    public static final String QUERY_PARAM_DOWNLOAD_FORMAT = "download_format";
    private static final String KML_FILENAME = "Records.kml";
    private static final String SHAPEFILE_ZIP_ENTRY_FORMAT = "shp/Survey%d_%s.zip";
    private static final String XLS_ZIP_ENTRY_FORMAT = "xls/Survey%d_%s.xls";
    
    public static final String SIGHTINGS_DOWNLOAD_CONTENT_TYPE = "application/zip";

    @Autowired
    protected SurveyDAO surveyDAO;
    
    @Autowired
    private BulkDataService bulkDataService;
    
    /**
     * For some scrollable records, create files in the requested download format and
     * zip them up
     * 
     * @param request - the http request object.
     * @param response - the http response object.
     * @param downloadFormat - array containing the download formats.
     * @param sr - the scrollable records object.
     * @param surveyList - the list of surveys to download.
     * @throws Exception
     */
    protected void downloadSightings(HttpServletRequest request, 
            HttpServletResponse response, 
            String[] downloadFormat, 
            ScrollableRecords sr, 
            List<Survey> surveyList) throws Exception {
        
        User user = getRequestContext().getUser();

        if (response.isCommitted()) {
            return;
        }

        response.setContentType(SIGHTINGS_DOWNLOAD_CONTENT_TYPE);
        response.setHeader("Content-Disposition", "attachment;filename=sightings_"
                + System.currentTimeMillis() + ".zip");
        ZipOutputStream zos = new ZipOutputStream(response.getOutputStream());
        try {
            if (downloadFormat != null) {
                Session sesh = getRequestContext().getHibernate();
                String contextPath = request.getContextPath();

                for (String format : downloadFormat) {

                    RecordDownloadFormat rdf = RecordDownloadFormat.valueOf(format);
                    switch (rdf) {
                    case KML: {
                        // make sure scrollable records is rewound and ready to go!
                        sr.rewind();
                        ZipEntry kmlEntry = new ZipEntry(KML_FILENAME);
                        zos.putNextEntry(kmlEntry);
                        writeKML(zos, sesh, contextPath, user, sr);
                        zos.closeEntry();
                        break;
                    }
                    case SHAPEFILE: {
                        for (Survey survey : surveyList) {
                            
                            // make sure scrollable records is rewound and ready to go!
                            sr.rewind();
                            
                            ZipEntry shpEntry = new ZipEntry(
                                    String.format(SHAPEFILE_ZIP_ENTRY_FORMAT, survey.getId(), FileUtils.getSafeFilename(survey.getName())));
                            zos.putNextEntry(shpEntry);

                            // The writer impl will flush the session and disconnect the survey.
                            survey = (Survey) sesh.merge(survey);
                            RecordDownloadWriter.write(bulkDataService, rdf, zos, sesh, contextPath, survey, user, sr);
                            zos.closeEntry();
                        }
                        break;
                    }
                    case XLS: {
                        for (Survey survey : surveyList) {
                            
                            // make sure scrollable records is rewound and ready to go!
                            sr.rewind();
                            
                            ZipEntry shpEntry = new ZipEntry(
                                    String.format(XLS_ZIP_ENTRY_FORMAT, survey.getId(), FileUtils.getSafeFilename(survey.getName())));
                            zos.putNextEntry(shpEntry);

                            // The writer impl will flush the session and disconnect the survey.
                            survey = (Survey) sesh.merge(survey);
                            RecordDownloadWriter.write(bulkDataService, rdf, zos, sesh, contextPath, survey, user, sr);
                            zos.closeEntry();
                        }
                        break;
                    }
                    default:
                        // Do Nothing
                        break;
                    }
                }
            }

        } finally {
            zos.flush();
            zos.close();
        }
    }
    
    private static void writeKML(ZipOutputStream zos, Session sesh, String contextPath, User user, ScrollableRecords sr) throws JAXBException {
        int recordCount = 0;
        List<Record> rList = new ArrayList<Record>(ScrollableRecords.RECORD_BATCH_SIZE);
        KMLWriter writer = KMLUtils.createKMLWriter(contextPath, null);
        while (sr.hasMoreElements()) {
            rList.add(sr.nextElement());
            // evict to ensure garbage collection
            if (++recordCount % ScrollableRecords.RECORD_BATCH_SIZE == 0) {
                
                KMLUtils.writeRecords(writer, user, contextPath, rList);
                rList.clear();
                sesh.clear();
            }
        }
        
        // Flush the remainder out of the list.
        KMLUtils.writeRecords(writer, user, contextPath, rList);
        sesh.clear();
        
        writer.write(false, zos);
    }
}
