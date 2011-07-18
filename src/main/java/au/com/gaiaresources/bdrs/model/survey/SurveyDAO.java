package au.com.gaiaresources.bdrs.model.survey;

import java.util.List;
import java.util.Set;

import org.hibernate.classic.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;

public interface SurveyDAO extends TransactionDAO {

    /**
     * Returns a list of surveys for the given user ordered by survey date.
     * Please note, this include both ACTIVE and INACTIVE surveys.
     * 
     * @param user
     *            the user to search for.
     * @return an ordered list of surveys by date.
     */
    List<Survey> getSurveys(User user);

    /**
     * Returns the survey specified by the primary key
     * 
     * @param pk
     *            the primary key of the survey
     * @return
     */
    Survey getSurvey(int pk);
    
    /**
     * Returns the survey specified by the primary key and pre fetches survey attributes and options.
     * 
     * @param pk
     *            the primary key of the survey
     * @return
     */
    Survey getSurveyData(int pk);

    /**
     * Returns a list of indicator species from a survey that aren't in the 
     * list of survey Id's given.
     * @param pk
     * @param notTheseSurveys
     * @return
     */
    public List<IndicatorSpecies> getSpeciesForSurvey(Survey thisSurvey, List<Survey> notTheseSurveys);

    /**
     * Create a survey and stores it in the database.
     * 
     * @param name
     *            the name of the survey
     * @return the created Survey.
     */
    Survey createSurvey(String name);

    /**
     * Persists the specified survey.
     * 
     * @param survey
     *            The <code>Survey</code> to be updated.
     * @return <code>Survey</code>
     */
    Survey createSurvey(Survey survey);

    /**
     * Updates the specified survey.
     * 
     * @param survey
     *            The <code>Survey</code> to be updated.
     * @return <code>Survey</code>
     */
    Survey updateSurvey(Survey survey);

    /**
     * A convenience function that will create or save a survey depending if the
     * survey has a primary key. Update if the survey has a pk, create
     * otherwise.
     * 
     * @param survey
     *            The <code>Survey</code> to be persisted.
     * @return <code>Survey</code>
     */
    Survey save(Survey survey);

    /**
     * Returns the last survey for the given user or null when there are none
     * this include both ACTIVE and INACTIVE surveys.
     * 
     * @param user
     *            the user to search for.
     * @return a survey or null.
     */
    Survey getLastSurveyForUser(User user);

    /**
     * Returns the requested survey
     * 
     * @param the
     *            id of the survey that is requested.
     * @return the requested Survey.
     */
    Survey get(Integer id);

    /**
     * Deletes the specified survey from the database.
     * 
     * @param id
     *            the primary key of the survey to delete.
     */
    void delete(int id);

    /**
     * @see #getSurveyByName(Session, String)
     */
    Survey getSurveyByName(String surveyName);

    /**
     * Retrieves the survey with the specified name or null if one does not
     * exist.
     * 
     * @param sesh
     *            the session to be used to retrieve the survey or null if the
     *            current session should be used.
     * @param surveyName
     *            the name of the survey.
     * @return the survey with the specified name or null if one does not exist.
     */
    Survey getSurveyByName(Session sesh, String surveyName);

    /**
     * Retrieves the list of Records that have been associated with the
     * specified survey.
     * 
     * @param survey
     *            the survey where the records were created.
     * @return the list of Records associated witht he specified survey.
     */
    List<Record> getRecordsForSurvey(Survey survey);

    /**
     * Find all surveys that are active, and is public or user is in the survey
     * or group
     * 
     * @param user
     *            the user to search for.
     * @return survey
     */
    List<Survey> getActiveSurveysForUser(User user);

    /**
     * Find all species attached to the specified survey where the scientific
     * name or common name contains the 'species' string fragment.
     * 
     * @param surveyPk
     *            the primary key of the survey
     * @param species
     *            the string fragment contained in the scientific name or common
     *            name.
     * @return a list of species matching the criteria above.
     */
    List<IndicatorSpecies> getSpeciesForSurveySearch(int surveyPk,
            String species);
    
    /**
     * Find all species attached to the specified survey where the species
     * is in a taxon group
     * @param surveyId the survey
     * @param taxonGroupId the TaxonGroup that the species are members of
     * @return a list of species matching the criteria above.
     */
	List<IndicatorSpecies> getSurveySpeciesForTaxon(int surveyId,
			int taxonGroupId);

 /**
     * Retrieves all surveys that are active and public.
     * 
     * @param fetchMetadata
     *            true if metadata should be eager fetched.
     * @return all active and public surveys.
     */
    List<Survey> getActivePublicSurveys(boolean fetchMetadata);

 /**
     * Count the species in a survey
     * 
     * @param int surveyPk
     */
    Integer countSpeciesForSurvey(int surveyPk);

    /**
     * Returns the survey with the specified pk using the specified session.
     * 
     * @param sesh
     *            the session to use to retrieve the survey, or the default
     *            session if sesh is null.
     * @param pk
     *            the primary key of the survey.
     * @return the survey with the specified primary key.
     */
    Survey getSurvey(Session sesh, int pk);
    
    /**
     * Overriding this because I may break existing code by changing the
     * interface...
     * 
     * @param sesh
     * @param pk
     * @return
     */
    Survey getSurvey(org.hibernate.Session sesh, int pk);

    /**
     * Returns a set of species that have distributions which overlap the survey's locations
     * @param s
     * @return species which overlap the survey's locations
     */
    Set<IndicatorSpecies> getSpeciesWithinSurveyLocations(Survey s);

    /**
     * Expires the cache entry for a particular survey
     * @param s
     */
    void expireCache(Survey s);

    /**
     * Gets all surveys containing the specified taxon.
     * @param taxon the taxon in the survey
     * @return all surveys containing the specified taxon.
     */
    List<Survey> getSurveys(IndicatorSpecies taxon);
    
    PagedQueryResult<Survey> search(PaginationFilter filter);
}
