/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.report.python.model;

import java.util.Collection;
import java.util.List;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Represents a facade over the {@link TaxaDAO} ensuring that any data
 * retrieved using this facade is readonly.
 */
public class PyTaxaDAO {
    private Logger log = Logger.getLogger(getClass());
    private TaxaDAO taxaDAO;
    private SurveyDAO surveyDAO;
    
    /**
     * Creates a new instance.
     * 
     * @param user the user accessing data.
     * @param surveyDAO retrieves survey related data.
     * @param taxaDAO retrieves taxon and taxon group related data.
     */
    public PyTaxaDAO(User user, SurveyDAO surveyDAO, TaxaDAO taxaDAO) {
        this.surveyDAO = surveyDAO;
        this.taxaDAO = taxaDAO;
    }
    
    /**
     * Returns a JSON serialized taxon with the specified primary key. 
     * 
     * @param id the primary key of the taxon to be returned.
     * @return a JSON serialized taxon with the specified primary key. 
     */
    public String getTaxon(int id) {
        return PyDAOUtil.toJSON(taxaDAO.getIndicatorSpecies(id)).toString();
    }
    
    /**
     * Returns a JSON serialized array of all taxon groups associated
     * with the specified survey.
     * 
     * @param surveyId the primary key of the survey containing the desired groups.
     * @return a JSON serialized array of all taxon groups in the survey.
     */
    public String getTaxonGroupsForSurvey(int surveyId) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        if(survey == null) {
            return PyDAOUtil.EMPTY_JSON_OBJECT_STR;
        }
        
        return PyDAOUtil.toJSON(taxaDAO.getTaxonGroup(survey)).toString();
    }
    
    /**
     * Returns all taxa associated with the survey with the specified 
     * primary key.
     * 
     * @param surveyId the primary key of the survey containing the desired taxa.
     * @return all taxa associated with the specified survey.
     */
    public String getTaxaForSurvey(int surveyId) {
        Survey survey = surveyDAO.getSurvey(surveyId);
        if(survey == null) {
            return PyDAOUtil.EMPTY_JSON_OBJECT_STR;
        } 
        
        Collection<IndicatorSpecies> speciesList = survey.getSpecies();
        if(speciesList.isEmpty()) {
            // All Species
            speciesList = taxaDAO.getIndicatorSpecies();
        }
        
        return PyDAOUtil.toJSON(speciesList).toString();
    }
    
    /**
     * Returns all distinct taxa that have been recorded in the specified survey.
     * @param surveyId the primary key of the survey containing the records.
     * @return all distinct taxa that have been recorded in the specified survey.
     */
    public String getDistinctRecordedTaxaForSurvey(int surveyId) {
        List<IndicatorSpecies> taxaList = taxaDAO.getDistinctRecordedTaxaForSurvey(surveyId);
        return PyDAOUtil.toJSON(taxaList).toString();
    }
}
