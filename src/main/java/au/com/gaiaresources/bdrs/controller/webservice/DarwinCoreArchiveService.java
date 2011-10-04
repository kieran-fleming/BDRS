package au.com.gaiaresources.bdrs.controller.webservice;

import java.io.File;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;
import au.com.gaiaresources.bdrs.dwca.RecordDwcaWriter;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.record.ScrollableRecords;
import au.com.gaiaresources.bdrs.model.record.impl.RecordFilter;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

@Controller
public class DarwinCoreArchiveService extends AbstractDownloadFileController {

    public static final String DOWNLOAD_ARCHIVE_URL = "/webservice/application/downloadDwca.htm";
    
    @Autowired
    private LSIDService lsidService;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private LocationService locService;
    
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * only returns fully public records, thus it will be a public webservice (no ident check)
     * 
     * @param request
     * @param response
     * @throws IOException
     */
    @RequestMapping(value = DOWNLOAD_ARCHIVE_URL, method = RequestMethod.GET)
    public void downloadArchive(HttpServletRequest request, HttpServletResponse response) throws IOException {
        
        RedirectionService redirService = new RedirectionService(ContentService.getRequestURL(request));
        RecordFilter recFilter = new RecordFilter();
        recFilter.setRecordVisibility(RecordVisibility.PUBLIC);
        
        ScrollableRecords scrollableRec = recordDAO.getScrollableRecords(recFilter);
        
        RecordDwcaWriter recordDwcaWriter = new RecordDwcaWriter(lsidService, locService, redirService);
        File zip = recordDwcaWriter.writeArchive(scrollableRec);
        
        downloadFile(zip, response, "bdrs_dwca", "zip");
    }
}
