package au.com.gaiaresources.bdrs.model.record.impl;

import java.util.Date;

import au.com.gaiaresources.bdrs.model.record.RecordVisibility;

/**
 * Class for passing parameters to filter records
 * 
 * for ints, 0 = dont care
 * for Objects, null = dont care
 * Everything is 'dont care' by default
 * 
 * @author aaron
 *
 */
public class RecordFilter {
    
    private int userPk = 0;
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

    /**
     * only return records for specified user pk
     * @return
     */
    public int getUserPk() {
        return userPk;
    }

    public void setUserPk(int userPk) {
        this.userPk = userPk;
    }

    /**
     * only return records for specific group pk
     * 
     * @return
     */
    public int getGroupPk() {
        return groupPk;
    }

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

    public void setSpeciesSearch(String speciesSearch) {
        this.speciesSearch = speciesSearch;
    }

    /**
     * optional parameter for paging
     * @return
     */
    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    /**
     * optional parameter for paging. AKA 'limit'
     * @param pageNumber
     */
    public Integer getEntriesPerPage() {
        return entriesPerPage;
    }

    public void setEntriesPerPage(Integer entriesPerPage) {
        this.entriesPerPage = entriesPerPage;
    }

    /** 
     * only return records with visibility set to this
     * @return
     */
    public RecordVisibility getRecordVisibility() {
        return this.visibility;
    }
    
    public void setRecordVisibility(RecordVisibility vis) {
        this.visibility = vis;
    }
    
    /**
     * only return records with a non null species field
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

    public void setFetch(boolean fetch) {
        this.fetch = fetch;
    }
}
