package au.com.gaiaresources.bdrs.controller.bulkdata;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;
import java.util.LinkedList;
import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;
import org.hibernate.HibernateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.attribute.AttributeDictionaryFactory;
import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializer;
import au.com.gaiaresources.bdrs.deserialization.record.RecordDeserializerResult;
import au.com.gaiaresources.bdrs.deserialization.record.RecordEntry;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.bulkdata.BulkDataService;
import au.com.gaiaresources.bdrs.service.bulkdata.BulkUpload;
import au.com.gaiaresources.bdrs.service.bulkdata.DataReferenceException;
import au.com.gaiaresources.bdrs.service.bulkdata.InvalidSurveySpeciesException;
import au.com.gaiaresources.bdrs.service.bulkdata.MissingDataException;
import au.com.gaiaresources.bdrs.spatial.ShapeFileReader;
import au.com.gaiaresources.bdrs.spatial.ShapeFileWriter;
import au.com.gaiaresources.bdrs.spatial.ShapefileAttributeDictionaryFactory;
import au.com.gaiaresources.bdrs.spatial.ShapefileAttributeParser;
import au.com.gaiaresources.bdrs.spatial.ShapefileRecordKeyLookup;
import au.com.gaiaresources.bdrs.spatial.ShapefileToRecordEntryTransformer;
import au.com.gaiaresources.bdrs.spatial.ShapefileType;

@RolesAllowed( {Role.USER,Role.POWERUSER,Role.SUPERVISOR,Role.ADMIN} )
@Controller
public class BulkDataController extends AbstractController {

    public static final String CONTENT_TYPE_XLS = "application/vnd.ms-excel";
    public static final String SHAPEFILE_UPLOAD_URL = "/bulkdata/uploadShapefile.htm";
    public static final String SHAPEFILE_TEMPLATE_URL = "/bulkdata/shapefileTemplate.htm";
    public static final String SPREADSHEET_TEMPLATE_URL = "/bulkdata/spreadsheetTemplate.htm";
    
    public static final String SHAPEFILE_IMPORT_SUMMARY_VIEW = "shapefileImportSummary";
    public static final String PARAM_SHAPEFILE_FILE = "shapefile";
    
    public static final String MV_PARAM_RESULTS_IN_ERROR = "errors";
    public static final String MV_PARAM_WRITE_COUNT = "writeCount";
    
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;
    
    @Autowired
    private CensusMethodDAO cmDAO;

    @Autowired
    private BulkDataService bulkDataService;

    @RequestMapping(value = "/bulkdata/bulkdata.htm", method = RequestMethod.GET)
    public ModelAndView bulkdata(HttpServletRequest request,
            HttpServletResponse response) {

        User user = getRequestContext().getUser();

        ModelAndView mv = new ModelAndView("bulkdata");
        mv.addObject("surveyList", surveyDAO.getActiveSurveysForUser(user));
        return mv;
    }

    @RequestMapping(value = SPREADSHEET_TEMPLATE_URL, method = RequestMethod.GET)
    public void spreadsheetTemplate(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "surveyPk", defaultValue = "0", required = true) int surveyPk)
            throws IOException {

        Survey survey = surveyDAO.getSurvey(surveyPk);

        // Make the survey name a safe filename.
        StringBuilder filename = new StringBuilder();
        for (char c : survey.getName().toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                filename.append(c);
            }
        }
        filename.append("_template_");
        filename.append(System.currentTimeMillis());
        filename.append(".xls");

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename="
                + filename);

        bulkDataService.exportSurveyTemplate(survey, response.getOutputStream());
    }

    @RequestMapping(value = "/bulkdata/upload.htm", method = RequestMethod.POST)
    public ModelAndView upload(MultipartHttpServletRequest req,
                                HttpServletResponse res,
                                @RequestParam(value="surveyPk", required=true) int surveyPk) {

        User user = getRequestContext().getUser();
        Survey survey = surveyDAO.getSurvey(surveyPk);

        ModelAndView view = new ModelAndView("importSummary");
        // The error message is a one line description of the error.
        String errorMessage = "";
        // The error description is a more in depth description of the error.
        String errorDescription = "";
        boolean dataError = false;
        boolean fileError = false;
        boolean parseError = false;
        boolean databaseError = false;

        try {
            MultipartFile uploadedFile = req.getFile("spreadsheet");

            if(uploadedFile != null) {
                if(CONTENT_TYPE_XLS.equals(uploadedFile.getContentType())) {

                    InputStream inp = uploadedFile.getInputStream();
                    boolean createMissing = req.getParameter("createMissing") != null;
                    BulkUpload bulkUpload = bulkDataService.importBulkData(survey, inp);
                    log.debug("1103 BulkDataControler[/bulkdata/upload.htm, POST]: Received Bulkupload");

                    view.addObject("bulkUpload", bulkUpload);

                    if(bulkUpload.hasError()) {
                        parseError = true;
                        if(bulkUpload.getErrorCount() < BulkDataService.PARSE_ERROR_LIMIT) {
                            errorMessage = "There has been an error while parsing the spreadsheet.";
                            errorDescription = "Please correct the errors below and retry your upload";

                        } else {
                            // Too many errors occured. Is this even the right spreadsheet?
                            errorMessage = "More than "+BulkDataService.PARSE_ERROR_LIMIT+" errors were encountered while parsing the spreadsheet.";
                            errorDescription = "Please ensure the spreadsheet is in the correct format and retry your upload";
                        }
                        log.warn(errorMessage);

                    } else {
                        bulkDataService.saveRecords(getRequestContext().getUser(),
                                bulkUpload, createMissing);
                    }

                } else {
                    // Failed to have the right content type
                    fileError = true;
                    errorMessage = "The uploaded file was not an XLS file.";
                    errorDescription = "Please retry your upload with a XLS file";
                    log.warn(errorMessage);
                }
            } else {
                // Failed to actually upload a file
                errorMessage = "Spreadsheet file is required.";
                errorDescription = "Please retry your upload";
                fileError = true;
                log.error(errorMessage);
            }
        }
        catch(MissingDataException mde) {
            // Thrown if additional data is required and create missing data is false.
            dataError = true;
            //String msg = mde.getMessage() == null ? mde.toString() : mde.getMessage();
            errorMessage = "The records could not be imported because there is missing data";
            errorDescription = "Errors are shown in the table below.";
            log.warn(mde.toString(), mde);
        }
        catch(InvalidSurveySpeciesException ise) {
            // Thrown if an attempt was made to add an invalid species to a survey.
            dataError = true;
            errorMessage = "Cannot add sighting records for a species that is not in the survey";
            errorDescription = "Please correct the spreadsheet and retry the upload";
            log.warn(ise.toString(), ise);
        }
        catch(AuthenticationException ae) {
            // Thrown if the owner is not allowed to create a particular type of
            // missing data.
            dataError = true;
            errorMessage = "You do not have the permissions required to create the missing data";
            errorDescription = "Please correct the spreadsheet and retry the upload";
            log.warn(ae.toString(), ae);
        }
        catch(IllegalArgumentException iae) {
            // Thrown if the uploaded file is not an xls file.
            fileError = true;
            String msg = iae.getMessage() == null ? iae.toString() : iae.getMessage();
            errorMessage = "The uploaded file could not be read. The internal error was: "+msg;
            errorDescription = "Please retry your upload";
            log.warn(iae.toString(), iae);
        }
        catch(ParseException pe) {
            // Thrown if the header does not match what we expect exactly, or
            // the header cannot be found.
            dataError = true;
            errorMessage = pe.getMessage();
            if(pe.getErrorOffset() > 0) {
                errorDescription = "Please correct the header in column: "+pe.getErrorOffset();
            }
            else {
                errorDescription = "Please retry your upload";
            }
            log.warn(pe.toString(), pe);
        }
        catch(DataReferenceException dre) {
            // Thrown if the header does not match what we expect exactly, or
            // the header cannot be found.
            dataError = true;
            errorMessage = dre.getMessage();
            errorDescription = "Please correct the row in error and retry your upload";
            // unfortunately no offset information...
            log.warn(dre.toString(), dre);
        }
        catch(IOException ioe) {
            // Should never be thrown really. Just part of Java IO.
            fileError = true;
            String msg = ioe.getMessage() == null ? ioe.toString() : ioe.getMessage();
            errorMessage = "The upload file could not be read. The internal error was: "+msg;
            errorDescription = "Please retry your upload";
            log.error(ioe.toString(), ioe);
        }
        catch(HibernateException he) {
            databaseError = true;
            String msg = he.getMessage() == null ? he.toString() : he.getMessage();
            errorMessage = "A database error has occured. The internal error was: "+msg;
            errorDescription = "Please correct your data and retry the upload.";
            log.error(he.toString(), he);
        }

        view.addObject("survey", survey);
        view.addObject("surveyList", surveyDAO.getActiveSurveysForUser(user));
        view.addObject("errorMessage", errorMessage);
        view.addObject("errorDescription", errorDescription);
        view.addObject("dataError", dataError);
        view.addObject("fileError", fileError);
        view.addObject("parseError", parseError);
        view.addObject("databaseError", databaseError);

        return view;
    }
    
    @RequestMapping(value=SHAPEFILE_TEMPLATE_URL, method=RequestMethod.GET) 
    public void getShapefileTemplate(HttpServletRequest request, HttpServletResponse response,
            @RequestParam(value="surveyPk", required=true) int surveyPk,
            @RequestParam(value="censusMethodPk", required=false, defaultValue="0") int censusMethodPk,
            @RequestParam(value="shapefileType", required=true) String shapefileType) throws Exception {
        
        Survey survey = surveyDAO.getSurvey(surveyPk);
        CensusMethod cm = cmDAO.get(censusMethodPk);
        
        ShapefileType shpType;
        if (ShapefileType.POINT.toString().equals(shapefileType)) {
            shpType = ShapefileType.POINT;
        } else if (ShapefileType.MULTI_POLYGON.toString().equals(shapefileType)) {
            shpType = ShapefileType.MULTI_POLYGON;
        } else {
            throw new IllegalArgumentException("shapefileType value not supported: " + shapefileType);
        }

        // Make the survey name a safe filename.
        StringBuilder filename = new StringBuilder();
        filename.append(sanitizeString(survey.getName()));
        if (cm != null) {
            filename.append("_");
            filename.append(sanitizeString(cm.getName()));
            filename.append("_");
        }
        filename.append("_shpTemplate_");
        filename.append(System.currentTimeMillis());
        filename.append(".zip");

        response.setContentType("application/vnd.ms-excel");
        response.setHeader("Content-Disposition", "attachment;filename="
                + filename);

        ShapeFileWriter shpWriter = new ShapeFileWriter();
        File zipFile = shpWriter.createZipShapefile(survey, cm, shpType);
        
        InputStream in = null;
        try {
            in = new FileInputStream(zipFile);
            IOUtils.copy(in, response.getOutputStream());
        } finally {
            if (in != null) {
                in.close();
            }
        }
    }
    
    private String sanitizeString(String s) {
        StringBuilder sb = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (Character.isLetterOrDigit(c)) {
                sb.append(c);
            }
        }
        return sb.toString();
    }
    
    @RequestMapping(value=SHAPEFILE_UPLOAD_URL, method=RequestMethod.POST)
    public ModelAndView uploadShapefile(MultipartHttpServletRequest request, HttpServletResponse response) throws Exception {
        
        MultipartFile uploadedFile = request.getFile(PARAM_SHAPEFILE_FILE);
        File tempFile = File.createTempFile("shapefileupload", Long.toString(System.nanoTime()));
        uploadedFile.transferTo(tempFile);
        
        ShapeFileReader reader = new ShapeFileReader(tempFile);
        
        RecordKeyLookup klu = new ShapefileRecordKeyLookup();
        ShapefileToRecordEntryTransformer transformer = new ShapefileToRecordEntryTransformer(klu);
        User currentUser = getRequestContext().getUser();
        
        List<RecordEntry> entries = transformer.shapefileFeatureToRecordEntries(reader.getFeatureIterator(), reader.getSurveyIdList(), reader.getCensusMethodIdList());
                
        AttributeDictionaryFactory adf = new ShapefileAttributeDictionaryFactory();
        AttributeParser parser = new ShapefileAttributeParser();
        RecordDeserializer rds = new RecordDeserializer(klu, adf, parser);
        List<RecordDeserializerResult> dsResult = rds.deserialize(currentUser, entries);
        
        List<RecordDeserializerResult> resultsInError = new LinkedList<RecordDeserializerResult>();
        
        for (RecordDeserializerResult rdr : dsResult) {
            if (!rdr.getErrorMap().isEmpty()) {
                resultsInError.add(rdr);
            }
        }

        ModelAndView mv = new ModelAndView(SHAPEFILE_IMPORT_SUMMARY_VIEW);
        
        if (!resultsInError.isEmpty()) {
            // we have errors, rollback the transaction
            requestRollback(request);
            mv.addObject(MV_PARAM_RESULTS_IN_ERROR, resultsInError);
        } else {
            mv.addObject(MV_PARAM_WRITE_COUNT, dsResult.size());
        }
        return mv;
    }
}
