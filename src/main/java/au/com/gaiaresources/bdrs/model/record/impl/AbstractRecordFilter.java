package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.impl.SortingCriteria;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Provides a filtering system for making record queries.
 * 
 * @author stephanie
 */
public abstract class AbstractRecordFilter implements RecordFilter {
    private Logger log = Logger.getLogger(getClass());
    private User user = null;
    private User accessor = null;
    private int groupPk = 0;
    private int surveyPk = 0;
    private int taxonGroupPk = 0;
    private Date startDate = null;
    private Date endDate = null;
    private String speciesSearch = null;
    private Integer pageNumber = null;
    private Integer entriesPerPage = null;
    private RecordVisibility visibility = null;
    private Boolean taxonomic = null;
    private boolean fetch = false;
    private Boolean held = null;
    /**
     * The {@link CensusMethod} to query for.  This property is ignored unless 
     * explicitly set.  To query for null census methods, either explicitly call 
     * {@link #setCensusMethod(CensusMethod)} method with a null parameter or set 
     * the {@link #useCensusMethodFilter} flag to true.
     */
    private CensusMethod censusMethod = null;
    /**
     * Boolean flag indicating whether or not to use the {@link #censusMethod} 
     * property in the query.  This allows you to specify a query for null 
     * census method values.
     */
    private boolean useCensusMethodFilter = false;
    private IndicatorSpecies species = null;

    public AbstractRecordFilter() {
        super();
    }

    /**
     * Getter for species.  Will only return records for the given species.
     * @return the species 
     */
    public IndicatorSpecies getSpecies() {
        return species;
    }

    /**
     * Setter for species.  Will filter on the given species
     * @param species the species to filter on
     */
    public void setSpecies(IndicatorSpecies species) {
        this.species = species;
    }

    /**
     * Getter for boolean flag determining whether or not to use the census method filter.
     * Defaults to false, which will not filter by census method even if a census method 
     * filter has been set.  When set to true, will filter on the census method, 
     * even if that method is null.  Note that this is set to true in the 
     * {@link #setCensusMethod(CensusMethod)} method.
     * @return Flag indicating whether or not to use the census method filter.
     */
    public boolean useCensusMethodFilter() {
        return useCensusMethodFilter;
    }


    /**
     * Setter for boolean flag determining whether or not to use the census method filter.
     * Defaults to false, which will not filter by census method even if a census method 
     * filter has been set.  When set to true, will filter on the census method, 
     * even if that method is null.  Note that this is set to true in the 
     * {@link #setCensusMethod(CensusMethod)} method.
     * @param useCensusMethodFilter Flag indicating whether or not to use the census method filter.
     */
    public void setUseCensusMethodFilter(boolean useCensusMethodFilter) {
        this.useCensusMethodFilter = useCensusMethodFilter;
    }
    
    /**
     * Getter for the census method filter.  Will only return records for the 
     * given census method.  Note that this field is ignored by default and only 
     * used when the {@link #useCensusMethodFilter} field is set.
     * @return The census method filter
     */
    public CensusMethod getCensusMethod() {
        return censusMethod;
    }

    /**
     * Setter for the census method filter.  Will only return records for the given
     * census method.  Note that this sets the {@link #useCensusMethodFilter} field
     * to true so it will be used in the query.
     * @param censusMethod The census method to filter by
     */
    public void setCensusMethod(CensusMethod censusMethod) {
        this.censusMethod = censusMethod;
        this.useCensusMethodFilter = false;
    }
    /**
     * Getter for the {@link #held} property.  Will only return records that match the {@link #held} property.
     * @return The held filter
     */
    public Boolean isHeld() {
        return held;
    }

    /**
     * Setter for the {@link #held} property.  Will only return records that match the {@link #held} property.
     * @param held Boolean indicating whether to return held (true) records or unheld (false) records.
     */
    public void setHeld(Boolean held) {
        this.held = held;
    }
    /**
     * Getter for the user filter.  Will only return records owned by the specified user
     * @return The user filter
     */
    public User getUser() {
        return user;
    }
    /**
     * Setter for the user filter.  Will only return records owned by the specified user
     * @param user
     */
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Getter for the accessor filter.  Will only return records that the accessor can access.
     * @return
     */
    public User getAccessor() {
        return accessor;
    }

    /**
     * Setter for the accessor filter.  Will only return records that the accessor has access to.
     * @param accessor The account making the request
     */
    public void setAccessor(User accessor) {
        this.accessor = accessor;
    }
    /**
     * Getter for the {@link #groupPk} property.  Will only return records for specific group pk
     * 
     * @return
     */
    public int getGroupPk() {
        return groupPk;
    }

    /**
     * Setter for the {@link #groupPk} property.  Will only return records for specific group pk
     * @param groupPk
     */
    public void setGroupPk(int groupPk) {
        this.groupPk = groupPk;
    }

    /**
     * Only return records for specified survey pk
     * 
     * @return
     */
    public int getSurveyPk() {
        return surveyPk;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setSurveyPk(int)
     */
    public void setSurveyPk(int surveyPk) {
        this.surveyPk = surveyPk;
    }

    /**
     * Only return records that contain a species which is part of the specified taxon group
     * 
     * @return
     */
    public int getTaxonGroupPk() {
        return taxonGroupPk;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setTaxonGroupPk(int)
     */
    public void setTaxonGroupPk(int taxonGroupPk) {
        this.taxonGroupPk = taxonGroupPk;
    }

    /**
     * Only return records lodged after this date
     * 
     * @return
     */
    public Date getStartDate() {
        return startDate != null ? new Date(startDate.getTime()) : null;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setStartDate(java.util.Date)
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate != null ? new Date(startDate.getTime()) : null;
    }

    /**
     * Only return records lodged before this date
     * @return
     */
    public Date getEndDate() {
        return endDate != null ? new Date(endDate.getTime()) : null;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setEndDate(java.util.Date)
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate != null ? new Date(endDate.getTime()) : null;
    }

    /**
     * Only return records containing the following species - the search string is
     * the scientific name of the species.
     * @return
     */
    public String getSpeciesSearch() {
        return speciesSearch;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setSpeciesSearch(java.lang.String)
     */
    public void setSpeciesSearch(String speciesSearch) {
        this.speciesSearch = speciesSearch;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getPageNumber()
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setPageNumber(java.lang.Integer)
     */
    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getEntriesPerPage()
     */
    public Integer getEntriesPerPage() {
        return entriesPerPage;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setEntriesPerPage(java.lang.Integer)
     */
    public void setEntriesPerPage(Integer entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }

    /** 
     * Only return records with visibility set to this
     * @return
     */
    public RecordVisibility getRecordVisibility() {
        return this.visibility;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setRecordVisibility(au.com.gaiaresources.bdrs.model.record.RecordVisibility)
     */
    public void setRecordVisibility(RecordVisibility vis) {
        this.visibility = vis;
    }

    /**
     * Only return records with a non null species field
     * @return
     */
    public Boolean isTaxonomic() {
        return this.taxonomic;
    }

    public void setTaxonomic(Boolean taxonomic) {
        this.taxonomic = taxonomic;
    }

    /**
     * Optional parameter - uses left join fetching in query. Defaults to false
     * @return
     */
    public boolean isFetch() {
        return fetch;
    }
    
    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#setFetch(boolean)
     */
    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getRecordQuery(org.hibernate.Session)
     */
    @Override
    public Query getRecordQuery(Session sesh) {
        List<SortingCriteria> sortCriteria = Collections.emptyList();
        return getRecordQuery(sesh, sortCriteria);
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getRecordQuery(org.hibernate.Session, java.util.List)
     */
    @Override
    public Query getRecordQuery(Session sesh, List<SortingCriteria> sortCriteria) {
    
        Map<String, Object> paramMap = new HashMap<String, Object>();
        StringBuilder builder = new StringBuilder(getQueryPredicate());
        // append the record table for all queries
        builder.append(" from Record record ");
        
        builder.append(createTableJoin());
        builder.append(" where record.id > 0");
    
        // since we now have records with a null 'when' field, the where
        // clause should only be included when no start/end date has been requested.
        // If this is not done, records with null 'when' fields will never
        // be retrieved by this query generator.
        if (getStartDate() != null || getEndDate() != null) {
            
            Calendar cal = new GregorianCalendar();
            
            if (getStartDate() == null) {
                setStartDate(new Date(1l));
            }
        
            cal.setTime(getStartDate());
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            setStartDate(cal.getTime());
        
            if (getEndDate() == null) {
                setEndDate(new Date(System.currentTimeMillis()));
            }
            
            cal.clear();
            cal.setTime(getEndDate());
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.MILLISECOND, 999);
            setEndDate(cal.getTime());
            
            builder.append(" and record.time >= :startTime and record.time <= :endTime");
            paramMap.put("startTime", getStartDate().getTime());
            paramMap.put("endTime", getEndDate().getTime());
        }
    
        if (getUser() != null) {
            // only show the user's own records when user is specified
            builder.append(" and record.user = :user");
            paramMap.put("user", getUser());
        } else if (getAccessor() == null || !getAccessor().isAdmin()) {
            // if user is not specified and accessor is not specified or accessor is not admin,
            // only return public and unheld records
            setRecordVisibility(RecordVisibility.PUBLIC);
            // only use held as a false condition for the filter if 
            // it hasn't been set by the requestor
            if (isHeld() == null) {
                setHeld(false);
            }
        }

        // make sure there is a space before the joined clauses
        builder.append(" ");
        builder.append(getJoinedClauses(paramMap));
        
        if (getSpecies() != null) {
            builder.append(" and record.species = :species");
            paramMap.put("species", getSpecies());
        }
       
        if (getRecordVisibility() != null) {
            builder.append(" and record.recordVisibility = :recordVisibility");
            paramMap.put("recordVisibility", getRecordVisibility());
        }
        if (isTaxonomic() != null) {
            builder.append(" and record.species is not null");
        }
        if (isHeld() != null) {
            builder.append(" and (record.held = :held");
            if (!isHeld()) {
                // null is the same as not held since the field is nullable
                builder.append(" or record.held is null)");
            } else {
                builder.append(")");
            }
            paramMap.put("held", isHeld());
        }
        
        if (useCensusMethodFilter()) {
            if (getCensusMethod() != null) {
                builder.append(" and record.censusMethod = :censusMethod");
                paramMap.put("censusMethod", getCensusMethod());
            } else {
                // a check for null census methods
                builder.append(" and record.censusMethod is null");
            }
        }
        
        // make sure there is a space before the ordering clause
        builder.append(" ");
        builder.append(getOrderingClause(sortCriteria));
        
        Query q = sesh.createQuery(builder.toString());
        for (Map.Entry<String, Object> entry : paramMap.entrySet()) {
            q.setParameter(entry.getKey(), entry.getValue());
        }
    
        if (getEntriesPerPage() != null) {
            q.setMaxResults(getEntriesPerPage());
        }
        
        return q;
    }
    
    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getQueryPredicate()
     */
    public abstract String getQueryPredicate();

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getOrderingClause(java.util.List)
     */
    public abstract String getOrderingClause(List<SortingCriteria> sortCriteria);

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#getJoinedClauses(java.util.Map)
     */
    public abstract String getJoinedClauses(Map<String, Object> paramMap);

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.model.record.impl.RecordFilter#createTableJoin()
     */
    public abstract String createTableJoin();
}