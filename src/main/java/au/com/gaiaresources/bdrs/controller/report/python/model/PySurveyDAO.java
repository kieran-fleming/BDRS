/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.report.python.model;

import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Represents a facade over the {@link SurveyDAO} ensuring that any data
 * retrieved using this facade is readonly.
 */
public class PySurveyDAO {

    private User user;
    private SurveyDAO surveyDAO;

    /**
     * Creates a new instance.
     * 
     * @param user the user accessing data.
     * @param surveyDAO retrieves survey related data.
     */
    public PySurveyDAO(User user, SurveyDAO surveyDAO) {
        this.user = user;
        this.surveyDAO = surveyDAO;
    }

    /**
     * Retrieves a list of active surveys for the accessing user.
     * @return a JSON encoded string representing all active surveys for the
     * accessing user.
     */
    public String getActiveSurveys() {
        return PyDAOUtil.toJSON(surveyDAO.getActiveSurveysForUser(this.user)).toString();
    }
}
