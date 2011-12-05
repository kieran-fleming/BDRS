package au.com.gaiaresources.bdrs.controller.report;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONObject;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.model.report.Report;
import au.com.gaiaresources.bdrs.model.report.ReportDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.ZipUtils;

/**
 * Tests all aspects of the <code>ReportController</code>.
 */
public class ReportControllerTest extends AbstractGridControllerTest {
    
    /**
     * Relative path to the directory containing the site species matrix report.
     */
    public static final String SITE_SPECIES_MATRIX_REPORT_DIR = "reports/SiteSpeciesMatrix/";
    /**
     * Relative path to the directory containing the species list report.
     */
    public static final String SPECIES_LIST_REPORT_DIR = "reports/SpeciesList/";
    
    @Autowired
    private ReportDAO reportDAO;

    /**
     * Tests that the listing view handler.
     * 
     * @throws Exception
     */
    @Test
    public void testReportListing() throws Exception {
        login("user", "password", new String[] { Role.USER });
        
        request.setMethod("GET");
        request.setRequestURI(ReportController.REPORT_LISTING_URL);

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, ReportController.REPORT_LISTING_VIEW);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "reports");
        Assert.assertEquals(reportDAO.getReports().size(), ((List<Report>)mv.getModel().get("reports")).size());
    }
    
    /**
     * Tests that a valid report can be uploaded.
     * 
     * @throws Exception
     */
    @Test
    public void testAddReportValidReport() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        String testReportName = "MinimalReport";
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);
        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        ModelAndView mv = handle(request, response);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(ReportController.REPORT_LISTING_URL, redirect.getUrl());

        JSONObject config = getConfigFile(testReportName);
        String reportName = config.getString(ReportController.JSON_CONFIG_NAME);
        Report report = getReportByName(reportName);
        
        Assert.assertEquals(config.getString(ReportController.JSON_CONFIG_NAME), report.getName());
        Assert.assertEquals(config.getString(ReportController.JSON_CONFIG_DESCRIPTION), report.getDescription());
        
        String baseIconName = FilenameUtils.getBaseName(config.getString(ReportController.JSON_CONFIG_ICON));
        String expectedIconFilename = String.format("%s.%s", baseIconName, ReportController.ICON_FORMAT);
        Assert.assertEquals(expectedIconFilename, report.getIconFilename());
    }
    
    /**
     * Tests that a report missing the config file shows the appropriate error.
     * 
     * @throws Exception
     */
    @Test
    public void testAddReportMissingConfig() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        String testReportName = "MissingConfigReport";
        int origReportCount = reportDAO.getReports().size();
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);
        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        ModelAndView mv = handle(request, response);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(ReportController.REPORT_LISTING_URL, redirect.getUrl());

        // We have the correct number of error message
        Assert.assertEquals(1, getRequestContext().getMessages().size());
        Assert.assertEquals("bdrs.report.add.missing_config", getRequestContext().getMessages().get(0).getCode());
        
        // No reports added
        Assert.assertEquals(origReportCount, reportDAO.getReports().size());
    }
    
    /**
     * Tests that a report with a malformed config JSON file shows the 
     * appropriate error.
     * 
     * @throws Exception
     */
    @Test
    public void testAddReportMalformedConfigJSON() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        String testReportName = "MalformedJSONConfig";
        int origReportCount = reportDAO.getReports().size();
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);
        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        ModelAndView mv = handle(request, response);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(ReportController.REPORT_LISTING_URL, redirect.getUrl());

        // We have the correct number of error message
        Assert.assertEquals(1, getRequestContext().getMessages().size());
        Assert.assertEquals("bdrs.report.add.malformed_config", getRequestContext().getMessages().get(0).getCode());
        
        // No reports added
        Assert.assertEquals(origReportCount, reportDAO.getReports().size());
    }
    
    /**
     * Tests that a report with a malformed config file shows the 
     * appropriate error.
     * 
     * @throws Exception
     */
    @Test
    public void testAddReportMalformedConfig() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        String testReportName = "MalformedConfig";
        int origReportCount = reportDAO.getReports().size();
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);
        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        ModelAndView mv = handle(request, response);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(ReportController.REPORT_LISTING_URL, redirect.getUrl());

        // We have the correct number of error message
        Assert.assertEquals(1, getRequestContext().getMessages().size());
        Assert.assertEquals("bdrs.report.add.error", getRequestContext().getMessages().get(0).getCode());
        
        // No reports added
        Assert.assertEquals(origReportCount, reportDAO.getReports().size());
    }
    
    /**
     * Tests that a report with a missing report directory shows the 
     * appropriate error.
     * 
     * @throws Exception
     */
    @Test
    public void testAddReportMissingReport() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        String testReportName = "MissingReport";
        int origReportCount = reportDAO.getReports().size();
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);
        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        ModelAndView mv = handle(request, response);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(ReportController.REPORT_LISTING_URL, redirect.getUrl());

        // We have the correct number of error message
        Assert.assertEquals(1, getRequestContext().getMessages().size());
        Assert.assertEquals("bdrs.report.add.error", getRequestContext().getMessages().get(0).getCode());
        
        // No reports added
        Assert.assertEquals(origReportCount, reportDAO.getReports().size());
    }
    
    /**
     * Tests that a report with a missing icon shows the 
     * appropriate error.
     * 
     * @throws Exception
     */
    @Test
    public void testAddMissingIconReport() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        String testReportName = "MissingReport";
        int origReportCount = reportDAO.getReports().size();
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);
        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        ModelAndView mv = handle(request, response);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(ReportController.REPORT_LISTING_URL, redirect.getUrl());

        // We have the correct number of error message
        Assert.assertEquals(1, getRequestContext().getMessages().size());
        Assert.assertEquals("bdrs.report.add.error", getRequestContext().getMessages().get(0).getCode());
        
        // No reports added
        Assert.assertEquals(origReportCount, reportDAO.getReports().size());
    }
    
    /**
     * Tests that a report can be deleted.
     *
     * @throws Exception
     */
    @Test
    public void testDeleteReport() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        String testReportName = "MinimalReport";

        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);

        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest)request;
        req.addFile(getTestReport(testReportName));

        handle(request, response);
        Assert.assertFalse(reportDAO.getReports().isEmpty());

        JSONObject config = getConfigFile(testReportName);
        String reportName = config.getString(ReportController.JSON_CONFIG_NAME);
        Report report = getReportByName(reportName);
        
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_DELETE_URL);
        request.addParameter(ReportController.REPORT_ID_PATH_VAR, 
                             String.valueOf(report.getId()));
        handle(request, response);
        Assert.assertTrue(reportDAO.getReports().isEmpty());
    }
    
    /**
     * Tests SiteSpeciesMatrixReport
     *
     * @throws Exception
     */
    @Test
    public void testSiteSpeciesMatrixReport() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        String testReportName = "SiteSpeciesMatrixReport";

        // Upload the Report
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);

        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest) request;
        File reportDir = new File(SITE_SPECIES_MATRIX_REPORT_DIR);
        req.addFile(getTestReport(reportDir, testReportName));

        handle(request, response);
        Assert.assertFalse(reportDAO.getReports().isEmpty());
        Assert.assertEquals(1, getRequestContext().getMessageContents().size());
        
        JSONObject config = getConfigFile(reportDir);
        String reportName = config.getString(ReportController.JSON_CONFIG_NAME);
        Report report = getReportByName(reportName);
        
        String renderURL = getReportRenderURL(report);
        
        // Render the report start page
        request.setMethod("GET");
        request.setRequestURI(renderURL);
        handle(request, response);
        // If everything works as desired, there should be no messages
        Assert.assertTrue(getRequestContext().getMessageContents().isEmpty());
        
        // Test the report on each individual survey
        for(Survey s : surveyDAO.getActiveSurveysForUser(currentUser)) {
            request.setMethod("GET");
            request.setRequestURI(renderURL);
            request.setParameter("surveyId", String.valueOf(s.getId()));
            handle(request, response);
            Assert.assertTrue(getRequestContext().getMessageContents().isEmpty());
        }
        
        // Test all reports
        request.setMethod("GET");
        request.setRequestURI(renderURL);
        for(Survey s : surveyDAO.getActiveSurveysForUser(currentUser)) {
            request.addParameter("surveyId", String.valueOf(s.getId()));
        }
        handle(request, response);
        Assert.assertTrue(getRequestContext().getMessageContents().isEmpty());
    }
    
    /**
     * Tests SpeciesListReport
     *
     * @throws Exception
     */
    @Test
    public void testSpeciesListReport() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        String testReportName = "SpeciesListReport";

        // Upload the Report
        request.setMethod("POST");
        request.setRequestURI(ReportController.REPORT_ADD_URL);

        MockMultipartHttpServletRequest req = (MockMultipartHttpServletRequest) request;
        File reportDir = new File(SPECIES_LIST_REPORT_DIR);
        req.addFile(getTestReport(reportDir, testReportName));

        handle(request, response);
        Assert.assertFalse(reportDAO.getReports().isEmpty());
        Assert.assertEquals(1, getRequestContext().getMessageContents().size());
        
        JSONObject config = getConfigFile(reportDir);
        String reportName = config.getString(ReportController.JSON_CONFIG_NAME);
        Report report = getReportByName(reportName);
        
        String renderURL = getReportRenderURL(report);
        
        // Render the report start page
        request.setMethod("GET");
        request.setRequestURI(renderURL);
        handle(request, response);
        // If everything works as desired, there should be no messages
        Assert.assertTrue(getRequestContext().getMessageContents().isEmpty());
        
        // Test the report on each individual survey
        for(Survey s : surveyDAO.getActiveSurveysForUser(currentUser)) {
            request.setMethod("GET");
            request.setRequestURI(renderURL);
            request.setParameter("surveyId", String.valueOf(s.getId()));
            handle(request, response);
            Assert.assertTrue(getRequestContext().getMessageContents().isEmpty());
        }
        
        // Test all reports
        request.setMethod("GET");
        request.setRequestURI(renderURL);
        for(Survey s : surveyDAO.getActiveSurveysForUser(currentUser)) {
            request.addParameter("surveyId", String.valueOf(s.getId()));
        }
        handle(request, response);
        Assert.assertTrue(getRequestContext().getMessageContents().isEmpty());
    }
    
    private String getReportRenderURL(Report report) {
        return ReportController.REPORT_RENDER_URL.replace("{reportId}", String.valueOf(report.getId()));
    }

    private Report getReportByName(String reportName) {
        for(Report report : reportDAO.getReports()) {
            if(report.getName().equals(reportName)) {
                return report;
            }
        }
        return null;
    }
    
    private MockMultipartFile getTestReport(String reportName) throws URISyntaxException, IOException {
        File dir = new File(getClass().getResource(reportName).toURI());
        return this.getTestReport(dir, reportName);
    }
    
    private MockMultipartFile getTestReport(File dir, String reportName) throws URISyntaxException, IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ZipUtils.compressToStream(dir.listFiles(), baos);
        
        return new MockMultipartFile(ReportController.POST_KEY_ADD_REPORT_FILE, 
                                     String.format("%s.zip", reportName), 
                                     "application/zip", 
                                     baos.toByteArray());
    }
    
    private JSONObject getConfigFile(String reportName) throws IOException, URISyntaxException {
        File dir = new File(getClass().getResource(reportName).toURI());
        return this.getConfigFile(dir);
    }
    
    private JSONObject getConfigFile(File reportDir) throws IOException, URISyntaxException {
        File config = new File(reportDir, ReportController.REPORT_CONFIG_FILENAME);
        return JSONObject.fromObject(readFileAsString(config.getAbsolutePath()));
    }
    
    private String readFileAsString(String filePath) throws java.io.IOException {
        byte[] buffer = new byte[(int) new File(filePath).length()];
        BufferedInputStream f = null;
        try {
            f = new BufferedInputStream(new FileInputStream(filePath));
            f.read(buffer);
        } finally {
            if (f != null)
                try {
                    f.close();
                } catch (IOException ignored) {
                }
        }
        return new String(buffer);
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
    }
}
