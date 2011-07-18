package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.List;

import au.com.gaiaresources.bdrs.db.impl.HqlQuery;

/**
 * The <code>Facet</code> represents a selection criteria to be applied to 
 * the set of records. Facets contain a list of {@link FacetOption}s that
 * represents values to be applied to the selection criteria.  
 */
public interface Facet {

    public String getQueryParamName();

    public void setQueryParamName(String queryParamName);

    public String getDisplayName();

    public void setDisplayName(String displayName);

    public List<FacetOption> getFacetOptions();

    public void setFacetOptions(List<FacetOption> facetOptions);

    public void applyPredicate(HqlQuery q);

    void setContainsSelected(boolean containsSelected);

    boolean isContainsSelected();
}
