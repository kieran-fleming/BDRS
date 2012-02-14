package au.com.gaiaresources.bdrs.controller.report.python;

import java.io.File;
import java.io.IOException;

import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.commons.io.FilenameUtils;
import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.controller.report.python.model.PyCensusMethodDAO;
import au.com.gaiaresources.bdrs.controller.report.python.model.PyRecordDAO;
import au.com.gaiaresources.bdrs.controller.report.python.model.PySurveyDAO;
import au.com.gaiaresources.bdrs.controller.report.python.model.PyTaxaDAO;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.report.Report;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 *  Represents the bridge between the Java Virtual Machine and the 
 *  Python Virtual Machine. It is the responsibility of the bridge to ensure
 *  that the Python Report only has read-access to application data. This is
 *  generally achieved by ensuring that only JSON encoded strings are passed
 *  from the bridge back to the Python report.
 */
public class PyBDRS {
    private Logger log = Logger.getLogger(getClass());
    
    private Report report;      
    private User user;
    private PyResponse response;
    
    private PySurveyDAO pySurveyDAO;
    private PyTaxaDAO pyTaxaDAO;
    private PyRecordDAO pyRecordDAO;
    private PyCensusMethodDAO pyCensusMethodDAO;
    
    private FileService fileService;
    
    /**
     * Creates a new instance.
     * 
     * @param fileService retrieves files from the files store.
     * @param report the report that will be using this bridge.
     * @param user the user accessing data. 
     * @param surveyDAO retrieves survey related data.
     * @param taxaDAO retrieves taxon and taxon group related data.
     * @param recordDAO retrieves record related data.
     */
    public PyBDRS(FileService fileService, Report report, User user, SurveyDAO surveyDAO, CensusMethodDAO censusMethodDAO, TaxaDAO taxaDAO, RecordDAO recordDAO) {
        this.fileService = fileService;
        this.report = report;
        
        this.user = user;
        
        this.response  = new PyResponse();
        
        this.pySurveyDAO = new PySurveyDAO(user, surveyDAO);
        this.pyTaxaDAO = new PyTaxaDAO(user, surveyDAO, taxaDAO);
        this.pyRecordDAO = new PyRecordDAO(user, recordDAO);
        this.pyCensusMethodDAO = new PyCensusMethodDAO(censusMethodDAO);
    }
    
    /**
     * Returns the python wrapped for the {@link SurveyDAO}
     * @return the pySurveyDAO the python wrapped {@link SurveyDAO}
     */
    public PySurveyDAO getSurveyDAO() {
        return pySurveyDAO;
    }

    /**
     * Returns the python wrapped {@link TaxaDAO}
     * @return the pyTaxaDAO the python wrapped {@link TaxaDAO}
     */
    public PyTaxaDAO getTaxaDAO() {
        return pyTaxaDAO;
    }
    
    /**
     * Returns the python wrapped {@link RecordDAO}
     * @return the pyRecordDAO the python wrapped {@link RecordDAO}
     */
    public PyRecordDAO getRecordDAO() {
        return pyRecordDAO;
    }
    
    /**
     * Returns the python wrapped {@link CensusMethodDAO}
     * @return the user the python wrapped {@link CensusMethodDAO}
     */
    public PyCensusMethodDAO getCensusMethodDAO() {
        return pyCensusMethodDAO;
    }
    
    /**
     * Returns a json serialized representation of the logged in {@link User}
     * @return the user a json serialized representation of the logged in {@link User}
     */
    public String getUser() {
        return JSONObject.fromMapToString(user.flatten());
    }
    
    /**
     * Returns a representation of the server response to the client. 
     * @return the response a representation of the server response to the browser.
     */
    public PyResponse getResponse() {
        return response;
    }
    
    /**
     * Returns an absolute path to a file specified by the relative path assuming
     * that the path was relative to the base report directory. 
     * 
     * Intended to be used by reports that need to retrieve files from the
     * report directory such as templates.
     * 
     * @param relativePath the relative path to the desired file from the report
     * directory.
     * @return an absolute path to the file specified by the relative path.
     */
    public String toAbsolutePath(String relativePath) {
        try {
            File reportDir = fileService.getTargetDirectory(report, Report.REPORT_DIR, false);
            return FilenameUtils.concat(reportDir.getAbsolutePath(), relativePath);
        } catch (IOException ioe) {
            // This cannot happen
            log.error("Unable to resolve absolute path to report.", ioe);
            throw new IllegalStateException(ioe);
        }
    }
    
    /**
     * Exposes the logger to the python report
     * 
     * @return
     */
    public Logger getLogger() {
        return log;
    }
}
