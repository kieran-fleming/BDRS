package au.com.gaiaresources.bdrs.controller.review.sightings.facet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *  Provides basic accessor and mutator functions for {@link Facet} implementations.
 */
public abstract class AbstractFacet implements Facet {

    private String queryParamName;
    private String displayName;
    private boolean containsSelected;

    private List<FacetOption> facetOptions = new ArrayList<FacetOption>();

    public AbstractFacet(String queryParamName, String displayName, boolean containsSelected) {
        super();
        this.queryParamName = queryParamName;
        this.displayName = displayName;
        this.containsSelected = containsSelected;
    }
    
    @Override
    public boolean isContainsSelected() {
        return containsSelected;
    }

    @Override
    public void setContainsSelected(boolean containsSelected) {
        this.containsSelected = containsSelected;
    }

    @Override
    public String getQueryParamName() {
        return queryParamName;
    }

    @Override
    public void setQueryParamName(String queryParamName) {
        this.queryParamName = queryParamName;
    }

    @Override
    public String getDisplayName() {
        return displayName;
    }

    @Override
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    @Override
    public List<FacetOption> getFacetOptions() {
        return Collections.unmodifiableList(facetOptions);
    }

    @Override
    public void setFacetOptions(List<FacetOption> facetOptions) {
        if (facetOptions == null) {
            throw new NullPointerException();
        }
        this.facetOptions = facetOptions;
    }

    public void addFacetOption(FacetOption opt) {
        if (opt == null) {
            throw new NullPointerException();
        }

        this.facetOptions.add(opt);
    }
}
