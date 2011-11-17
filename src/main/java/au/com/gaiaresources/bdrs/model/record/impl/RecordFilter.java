package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.hibernate.Query;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * A RecordFilter is an object for creating complex queries for {@link Record} 
 * objects or for counting them.  Implementations of this interface will provide 
 * a way to set parameters for the query and will handle query creation from 
 * the specified parameters.
 * 
 * @author stephanie
 */
public interface RecordFilter {

    /**
     * Get a query for {@link Record} objects based on all set parameters.
     * @param sesh The {@link Session} to create the query in.
     * @return A {@link Query} object that can be used to get results.
     */
    public Query getRecordQuery(Session sesh);

    /**
     * Get a query for {@link Record} objects based on all set parameters.
     * @param sesh The {@link Session} to create the query in.
     * @param sortCriteria A list of {@link SortingCriteria} that specifies columns and thier ordering for the query.
     * @return A {@link Query} object that can be used to get results.
     */
    public Query getRecordQuery(Session session,
            List<SortingCriteria> sortCriteria);

    /**
     * Set the {@link User} who is making the query.  This will filter records based on 
     * record visibility.
     * @param accessor The user who is requesting the data
     */
    public void setAccessor(User accessor);

    /**
     * Set the {@link User} who owns the records.  This will filter records based on 
     * record ownership.
     * @param user The user who owns the records to return
     */
    public void setUser(User user);

    /**
     * Setter for the census method filter.  Will only return records for the given
     * census method.
     * @param censusMethod The census method to filter by
     */
    public void setCensusMethod(CensusMethod censusMethod);

    /**
     * Set the species filter.  Will only return records for the given species.
     * @param species The species to filter by
     */
    public void setSpecies(IndicatorSpecies species);

    /**
     * Set the primary key for the user group to filter on.  Will only return 
     * records for users in the given group.
     * @param groupPk The primary key of a user group to filter on
     */
    public void setGroupPk(int groupPk);

    /**
     * Set the primary key for the taxon group to filter on.  Will only return 
     * records for species in the given group.
     * @param taxonGroupPk The primary key of a taxon group to filter on
     */
    public void setTaxonGroupPk(int taxonGroupPk);

    /**
     * Set the primary key for the survey to filter on.  Will only return records 
     * of the given survey.
     * @param surveyPk The primary key of a survey to filter on
     */
    public void setSurveyPk(int surveyPk);

    /**
     * Set the start date to filter by.  Will only return records made on or after the 
     * given date.
     * @param startDate The date to filter records after
     */
    public void setStartDate(Date startDate);

    /**
     * Set the end date to filter by.  Will only return records made before the given date.
     * @param endDate The date to filter records before
     */
    public void setEndDate(Date endDate);

    /**
     * Set a species string to match on.  Will only return records of species that 
     * match the search term.
     * @param species A string to match species on
     */
    public void setSpeciesSearch(String species);

    /**
     * Optional parameter for paging
     * @return
     */
    public Integer getPageNumber();
    
    /**
     * Optional parameter for paging. AKA 'limit'
     * @param pageNumber
     */
    public Integer getEntriesPerPage();

    /**
     * Optional parameter for paging
     */
    public void setPageNumber(Integer number);

    /**
     * Optional parameter for paging. AKA 'limit'
     * @param number
     */
    public void setEntriesPerPage(Integer number);

    /**
     * Optional parameter - uses left join fetching in query. Defaults to false
     * @param fetch
     */
    public void setFetch(boolean fetch);

    /**
     * Sets the {@link RecordVisibility} to filter by.  Will only return records 
     * matching the given visibility unless the accessor is null (anonymous) or 
     * does not have permission to access records of the given visibility (i.e. is not admin), 
     * in which case the default is {@link RecordVisibility.PUBLIC}.
     * @param visibility The visibility to query for
     */
    public void setRecordVisibility(RecordVisibility visibility);

    /**
     * Gets the select part of the query. This String must be of the form 
     * 'select x, y, z' and must not contain a from clause.
     * @return A String representing the select part of a query
     */
    String getQueryPredicate();

    /**
     * Get the ordering clause for the query.  This string must be of the form 
     * 'order by x, y, z' where x, y, and z are elements from the select clause.
     * It must use the sortCriteria to build the ordering.
     * @param sortCriteria A {@link List} of {@link SortingCriteria} to order the query by
     * @return
     */
    String getOrderingClause(List<SortingCriteria> sortCriteria);

    /**
     * Get joined table clauses.  This string must be of the form 
     * ('[and|or] x = :value_x')* where value_x is added to the given parameter map.
     * @param paramMap A mapping of the clause parameters to be added to the query
     * @return A String representing the criteria for the query that pertains to 
     *         any tables joined in {@link #createTableJoin()}.
     */
    String getJoinedClauses(Map<String, Object> paramMap);

    /**
     * Get the joined tables.  This string must be of the form 
     * '([inner|outer|left|right]+ join x)*'.
     * @return A String representing the tables to join for the query.
     */
    String createTableJoin();
}