package au.com.gaiaresources.bdrs.controller.report;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipInputStream;

import javax.annotation.security.RolesAllowed;
import javax.imageio.ImageIO;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import jep.Jep;
import jep.JepException;
import net.sf.json.JSON;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.file.DownloadFileController;
import au.com.gaiaresources.bdrs.controller.report.python.PyBDRS;
import au.com.gaiaresources.bdrs.controller.report.python.PyResponse;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.report.Report;
import au.com.gaiaresources.bdrs.model.report.ReportDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.FileUtils;
import au.com.gaiaresources.bdrs.util.ImageUtil;
import au.com.gaiaresources.bdrs.util.ZipUtils;

/**
 * This controller handles all requests for adding, listing, deleting and
 * generation of reports.
 */
@Controller
public class ReportController extends AbstractController {

    /**
     * Tile definition name for listing reports.
     */
    public static final String REPORT_LISTING_VIEW = "reportListing";
    /**
     * Tile definition name for rendering reports.
     */
    public static final String REPORT_RENDER_VIEW = "reportRender";
    
    /**
     * The URL to list all active reports.
     */
    public static final String REPORT_LISTING_URL = "/report/listing.htm";
    /**
     * The URL that accepts POST requests to add reports.
     */
    public static final String REPORT_ADD_URL = "/report/add.htm";
    /**
     * The URL to that accepts POST requests to remove reports.
     */
    public static final String REPORT_DELETE_URL = "/report/delete.htm";
    /**
     * The URL that GETs rendered reports.
     */
    public static final String REPORT_RENDER_URL = "/report/{reportId}/render.htm";
    /**
     * The URL to GET static report files.
     */
    public static final String REPORT_STATIC_URL = "/report/{reportId}/static.htm";
    
    public static final String POST_KEY_ADD_REPORT_FILE = "report_file";
    /**
     * The (mandatory) name of the configuration file containing the report
     * name and description.
     */
    public static final String REPORT_CONFIG_FILENAME = "config.json";  
    
    /**
     * The JSON configuration attribute for the report name.
     */
    public static final String JSON_CONFIG_NAME = "name";
    /**
     * The JSON configuration attribute for the report description.
     */
    public static final String JSON_CONFIG_DESCRIPTION = "description";
    /**
     * The JSON configuration attribute for the report icon file path.
     */
    public static final String JSON_CONFIG_ICON = "icon";
    /**
     * The JSON configuration attribute for the report directory.
     */
    public static final String JSON_CONFIG_REPORT = "report";
    /**
     * The resized width of the report icon. This is the icon that is 
     * displayed on the report listing screen.
     */
    public static final int ICON_WIDTH = 128;
    /**
     * The resized height of the report icon. This is the icon that is 
     * displayed on the report listing screen.
     */
    public static final int ICON_HEIGHT = 128;
    /**
     * The target image format of the resized report icon.
     */
    public static final String ICON_FORMAT = "png";
    
    /**
     * The (mandatory) filename of the python report.
     */
    public static final String PYTHON_REPORT = "report.py";
    /**
     * The path variable name used to extract the primary key of the 
     * current report.
     */
    public static final String REPORT_ID_PATH_VAR = "reportId";
    /**
     * The query parameter name containing the file path of a static report file.
     */
    public static final String FILENAME_QUERY_PARAM = "fileName";
    /**
     * A Python snippet that executes a report and catches any errors and logging
     * them appropriately. 
     */
    public static final String REPORT_EXEC_TMPL;
    
    static {
        StringBuilder builder = new StringBuilder();
        builder.append("try:\n");
        builder.append("    Report().content(\"\"\"%s\"\"\")\n");
        builder.append("except Exception, e:\n");
        builder.append("    import sys, traceback\n");
        builder.append("    response = bdrs.getResponse()\n");
        builder.append("    response.setError(True)\n");
        builder.append("    response.setContent(traceback.format_exc())\n");
        
        REPORT_EXEC_TMPL = builder.toString();
    }
    
    @Autowired
    private ReportDAO reportDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private CensusMethodDAO censusMethodDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired
    private FileService fileService;

    private Logger log = Logger.getLogger(getClass());

    /**
     * Lists all reports currently in the system.
     * 
     * @param request the browser request.
     * @param response the server response.
     * @return a list of all reports currently in the system.
     */
    @RolesAllowed({  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = REPORT_LISTING_URL, method = RequestMethod.GET)
    public ModelAndView listReports(HttpServletRequest request,
            HttpServletResponse response) {

        ModelAndView mv = new ModelAndView(REPORT_LISTING_VIEW);
        mv.addObject("reports", reportDAO.getReports());
        return mv;
    }
    
    /**
     * Adds a report to the system sourcing the report name, description and 
     * icon from the configuration file.
     * 
     * @param request the browser request
     * @param response the server response
     * @return redirects to the report listing page.
     */
    @RolesAllowed({  Role.ADMIN })
    @RequestMapping(value = REPORT_ADD_URL, method = RequestMethod.POST)
    public ModelAndView addReports(MultipartHttpServletRequest request,
            HttpServletResponse response) {
        
        ZipInputStream zis = null;
        File tempReportDir = null;
        InputStream configInputStream = null;
        try {
            tempReportDir = extractUploadedReport(request.getFile(POST_KEY_ADD_REPORT_FILE));

            // Begin extraction of the report.
            File configFile = new File(tempReportDir, REPORT_CONFIG_FILENAME);
            if(configFile.exists()) {
                configInputStream = new FileInputStream(configFile);
                JSON json = FileUtils.readJsonStream(configInputStream);
                if(!json.isArray()) {
                    JSONObject config = (JSONObject) json;
                    
                    String reportName = config.optString(JSON_CONFIG_NAME, null);
                    String reportDescription = config.optString(JSON_CONFIG_DESCRIPTION, null);
                    String iconFilename = config.optString(JSON_CONFIG_ICON);
                    String reportDirName = config.optString(JSON_CONFIG_REPORT);
                    
                    File iconFile = new File(tempReportDir, iconFilename);
                    File reportDir = new File(tempReportDir, reportDirName);
                    File reportPy = new File(reportDir, PYTHON_REPORT);
                    
                    boolean isReportValid = reportName != null && !reportName.isEmpty() && 
                                            reportDescription != null && !reportDescription.isEmpty();
                    boolean isFilesValid = iconFile.exists() && reportDir.exists() && reportPy.exists(); 
                    
                    if(isReportValid && isFilesValid) {
                        
                        String targetIconFilename = String.format("%s.%s", FilenameUtils.removeExtension(iconFilename), ICON_FORMAT);
                        Report report = new Report(reportName, reportDescription, targetIconFilename, true);
                        report = reportDAO.save(report);
                        
                        // Scale the icon file.
                        scaleReportIcon(report, iconFile);
                        boolean deleteSuccess = iconFile.delete();
                        if(!deleteSuccess) {
                            log.warn("Failed to delete icon file at: "+iconFile.getAbsolutePath());
                        }
                        
                        // Move the python report code to the target directory.
                        File targetReportDir = fileService.getTargetDirectory(report, Report.REPORT_DIR, true);
                        if (targetReportDir.exists()) {
                            log.warn("Target directory exists, attempting to delete : " + targetReportDir.getAbsolutePath());
                            try {
                                org.apache.commons.io.FileUtils.deleteDirectory(targetReportDir);    
                            } catch (IOException ioe) {
                                log.error("Failed to delete target report directory", ioe);
                            }
                        }
                        
                        boolean renameSuccess = reportDir.renameTo(targetReportDir);
                        if(!renameSuccess) {
                            log.warn("Failed to rename report directory: "+reportDir.getAbsolutePath());
                        }
                        
                        // Success!
                        getRequestContext().addMessage("bdrs.report.add.success", new Object[]{ report.getName() });
                        
                    } else {
                        // Invalid Report Config or Content
                        log.error("Failed to add report because the name or description is null or the report or icon cannot be found.");
                        getRequestContext().addMessage("bdrs.report.add.error");
                    }
                } else {
                    // Malformed JSON
                    getRequestContext().addMessage("bdrs.report.add.malformed_config");
                }
            } else {
                // Missing config file
                getRequestContext().addMessage("bdrs.report.add.missing_config");
            }
        } catch(SecurityException se) {
            // This cannot happen because we should have the rights to remove
            // a file that we created.
            getRequestContext().addMessage("bdrs.report.add.error");
            log.error("Unable to read the report file.", se);
        } catch(IOException ioe) {
            getRequestContext().addMessage("bdrs.report.add.error");
            log.error("Unable to read the report file.", ioe);
        } catch(JSONException je) {
            getRequestContext().addMessage("bdrs.report.add.malformed_config");
            log.error("Unable to parse the report config file.", je);
        } finally {
            try {
                if(zis != null) {
                    zis.close();
                }
                if(configInputStream != null) {
                    configInputStream.close();
                }
                if(tempReportDir != null) {
                    org.apache.commons.io.FileUtils.deleteDirectory(tempReportDir);
                }
            } catch(IOException ioe) {
                log.error(ioe.getMessage(), ioe);
            }
        }
        
        return new ModelAndView(new RedirectView(REPORT_LISTING_URL, true));
    }
    
    /**
     * Deletes the specified report from the database.
     * 
     * @param request the browser request
     * @param response the server response
     * @param reportId the primary key of the report to be deleted
     * @return redirects to the report listing page
     */
    @RolesAllowed({  Role.ADMIN })
    @RequestMapping(value = REPORT_DELETE_URL, method = RequestMethod.POST)
    public ModelAndView deleteReport(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @RequestParam(required = true, value=REPORT_ID_PATH_VAR) int reportId) {
        
        Report report = reportDAO.getReport(reportId);
        if(report != null) {
            reportDAO.delete(report);
            getRequestContext().addMessage("bdrs.report.delete.success", new Object[]{ report.getName() });
        } else {
            getRequestContext().addMessage("bdrs.report.delete.not_found");
        }
        
        return new ModelAndView(new RedirectView(REPORT_LISTING_URL, true));
    }
    
    /**
     * Renders the specified report.
     * 
     * @param request the browser request.
     * @param response the server response.
     * @param reportId the primary key of the report to be rendered.
     */
    @RolesAllowed({  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = REPORT_RENDER_URL, method = RequestMethod.GET)
    public ModelAndView renderReport(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable(REPORT_ID_PATH_VAR) int reportId) {
        
        try {
            // Get the report and find the Python report code.
            Report report = reportDAO.getReport(reportId);
            File reportDir = fileService.getTargetDirectory(report, Report.REPORT_DIR, true);
            
            // Setup the parameters to send to the Python report.
            PyBDRS bdrs = new PyBDRS(fileService, report, getRequestContext().getUser(), surveyDAO, censusMethodDAO, taxaDAO, recordDAO);
            JSONObject jsonParams = toJSONParams(request);
            
            // Fire up a new Python interpreter
            Jep jep = new Jep(false, reportDir.getAbsolutePath(), Thread.currentThread().getContextClassLoader());
            // Set the Python bdrs global variable
            jep.set("bdrs", bdrs);
            // Load and execute the report
            jep.runScript(new File(reportDir, PYTHON_REPORT).getAbsolutePath());
            jep.eval(String.format(REPORT_EXEC_TMPL, jsonParams.toString()));
            // Terminate the interpreter
            jep.close();
            
            // Examine the report response
            PyResponse pyResponse = bdrs.getResponse();
            if(pyResponse.isError()) {
                // The report had some sort of error.
                log.error(new String(pyResponse.getContent()));
                // Let the user know that an error has occured.
                getRequestContext().addMessage("bdrs.report.render.error");
                // We can't render the page, so redirect back to the listing page.
                return new ModelAndView(new RedirectView(REPORT_LISTING_URL, true));
            } else {
                // Set the header of the Python report if there is one.
                // This allows the python report to provide file downloads if
                // necessary.
                updateHeader(response, pyResponse);
                // Set the content type of the python report. This is HTML 
                // by default
                response.setContentType(pyResponse.getContentType());
                
                // If the content type is HTML then we can treat it as text,
                // otherwise we simply treat it as raw bytes.
                if(PyResponse.HTML_CONTENT_TYPE.equals(pyResponse.getContentType())) {
                    // If the report is standalone, then do not render the
                    // report with the usual header and footer.
                    if(pyResponse.isStandalone()) {
                        response.getWriter().write(new String(pyResponse.getContent()));
                    } else {
                        // Embed the report in a model and view so that it will
                        // receive the usual header, menu and footer.
                        ModelAndView mv = new ModelAndView(REPORT_RENDER_VIEW);
                        mv.addObject("reportContent", new String(pyResponse.getContent()));
                        return mv;
                    }
                } else {
                    // Treat the data as a byte array and simply squirt them
                    // down the pipe.
                    byte[] content = pyResponse.getContent();
                    response.setContentLength(content.length);
                    ServletOutputStream outputStream = response.getOutputStream();
                    outputStream.write(content);
                    outputStream.flush();
                }
            }
            return null;
            
        } catch(JepException je) {
            // Jep Exceptions will occur if there has been a problem on the 
            // Python side of the fence.
            log.error("Unable to render report with PK: "+reportId, je);
            getRequestContext().addMessage("bdrs.report.render.error");
            return new ModelAndView(new RedirectView(REPORT_LISTING_URL, true));
        } catch (IOException e) {
            // Occurs when there has been a problem reading/writing files.
            log.error("Unable to render report with PK: "+reportId, e);
            getRequestContext().addMessage("bdrs.report.render.error");
            return new ModelAndView(new RedirectView(REPORT_LISTING_URL, true));
        }
    }
    
    /**
     * Allows reports to provide static files such as media or javascript
     * without the need to create a Python interpreter. This handler will
     * rewrite the URL and delegate the servicing of the request to the 
     * {@link DownloadFileController}.
     * 
     * @param request the browser request.
     * @param response the server response.
     * @param reportId the primary key of the report containing the static file.
     * @param fileName the relative path of the file to retrieve.
     */
    @RolesAllowed({  Role.USER, Role.POWERUSER, Role.SUPERVISOR, Role.ADMIN })
    @RequestMapping(value = REPORT_STATIC_URL, method = RequestMethod.GET)
    public ModelAndView downloadStaticReportFile(HttpServletRequest request,
                                     HttpServletResponse response,
                                     @PathVariable(REPORT_ID_PATH_VAR) int reportId,
                                     @RequestParam(required = true, value=FILENAME_QUERY_PARAM) String fileName) {
        ModelAndView mv = null;
        Report report = reportDAO.getReport(reportId);
        if(report == null) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
        } else {
            File target = new File(Report.REPORT_DIR, fileName);
            mv = new ModelAndView(new RedirectView(DownloadFileController.FILE_DOWNLOAD_URL, true));
            mv.addObject(DownloadFileController.CLASS_NAME_QUERY_PARAM, report.getClass().getCanonicalName());
            mv.addObject(DownloadFileController.INSTANCE_ID_QUERY_PARAM, report.getId());
            mv.addObject(DownloadFileController.FILENAME_QUERY_PARAM, target.getPath());
        }
        return mv;
    }

    /**
     *  Sets the header of the server response if a header was provided by the
     *  Python report. Setting the header allows a Python report to generate
     *  a dynamically created file download.
     *  
     *  @param response the server response.
     *  @param pyResponse the resposne object that contains the header (if set)
     *  desired by the report.
     */    
    private void updateHeader(HttpServletResponse response, PyResponse pyResponse) {
        if(pyResponse.getHeaderName() != null && pyResponse.getHeaderValue() != null) {
            response.setHeader(pyResponse.getHeaderName(), pyResponse.getHeaderValue());
        }
    }

    /**
     * JSON encodes all query parameters. The JSON object will take the form
     * { string : [string, string, string, ...}  
     * 
     * @param request the browser request
     * @return 
     */
    private JSONObject toJSONParams(HttpServletRequest request) {
        JSONObject params = new JSONObject();
        params.accumulateAll(request.getParameterMap());
        return params;
    }

    /**
     * Scales the specified icon to the size required by the report listing view.
     * 
     * @param report the report instance where the file will be saved.
     * @param iconFile the file to be resized.
     * @throws FileNotFoundException
     * @throws IOException
     */
    private void scaleReportIcon(Report report, File iconFile) throws IOException {
        InputStream iconInputStream = new FileInputStream(iconFile);
        BufferedImage resizedIcon = ImageUtil.resizeImage(iconInputStream, ICON_WIDTH, ICON_HEIGHT);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(resizedIcon, ICON_FORMAT, baos);
        baos.flush();
        fileService.createFile(report.getClass(), report.getId(), report.getIconFilename(), baos.toByteArray());
    }
    
    /**
     * Saves and decompresses the uploaded report to a temporary directory.
     * 
     * @param uploadedReport the uploaded report.
     * @return the directory containing the decompressed report
     * @throws IOException thrown if there is a problem reading or writing the files.
     */
    private File extractUploadedReport(MultipartFile uploadedReport) throws IOException {
        File tempReportZip = File.createTempFile("report", "zip");
        tempReportZip.deleteOnExit();
        FileUtils.writeBytesToFile(uploadedReport.getBytes(), tempReportZip);
        
        // Decompress the report to a temporary location
        File tempReportDir = FileUtils.createTempDirectory(String.valueOf(System.currentTimeMillis()));
        tempReportDir.deleteOnExit();
        ZipUtils.decompressToDir(tempReportZip, tempReportDir);
        
        boolean deleteSuccess = tempReportZip.delete();
        if(!deleteSuccess) {
            log.warn("Failed to delete temporary report zip file: " + tempReportZip.getAbsolutePath());
        }
        
        return tempReportDir;
    }
}
