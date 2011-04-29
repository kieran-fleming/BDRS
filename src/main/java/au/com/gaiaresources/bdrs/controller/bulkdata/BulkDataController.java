package au.com.gaiaresources.bdrs.controller.bulkdata;

import java.io.IOException;
import java.io.InputStream;
import java.text.ParseException;

import javax.security.sasl.AuthenticationException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

import au.com.gaiaresources.bdrs.service.bulkdata.BulkDataService;
import au.com.gaiaresources.bdrs.service.bulkdata.BulkUpload;
import au.com.gaiaresources.bdrs.service.bulkdata.InvalidSurveySpeciesException;
import au.com.gaiaresources.bdrs.service.bulkdata.MissingDataException;
import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;

@Controller
public class BulkDataController extends AbstractController {

    public static final String CONTENT_TYPE_XLS = "application/vnd.ms-excel";

    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private SurveyDAO surveyDAO;

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

    @RequestMapping(value = "/bulkdata/spreadsheetTemplate.htm", method = RequestMethod.GET)
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
                    BulkUpload bulkUpload = bulkDataService.importSurveyRecords(survey, inp);

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
}
