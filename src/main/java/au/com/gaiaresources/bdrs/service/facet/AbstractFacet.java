package au.com.gaiaresources.bdrs.service.facet;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery;
import au.com.gaiaresources.bdrs.db.impl.Predicate;

/**
 *  Provides basic accessor and mutator functions for {@link Facet} implementations.
 */
public abstract class AbstractFacet implements Facet {

    private String queryParamName;
    private String displayName;
    private boolean containsSelected;

    private int weight = DEFAULT_WEIGHT_CONFIG;
    private boolean isActive = DEFAULT_ACTIVE_CONFIG;
    private String prefix = DEFAULT_PREFIX_CONFIG;
    
    private List<FacetOption> facetOptions = new ArrayList<FacetOption>();
    
    /**
     * Creates a new instance of this class.
     * 
     * @param queryParamName the base name of query parameters
     * @param displayName the human readable name of this facet.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public AbstractFacet(String queryParamName, String displayName, JSONObject userParams) {
        this.queryParamName = queryParamName;
        this.displayName = displayName;
        
        weight = userParams.optInt(JSON_WEIGHT_KEY, DEFAULT_WEIGHT_CONFIG);
        isActive = userParams.optBoolean(JSON_ACTIVE_KEY, DEFAULT_ACTIVE_CONFIG);
        prefix = userParams.optString(JSON_PREFIX_KEY, DEFAULT_PREFIX_CONFIG);
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
            throw new IllegalArgumentException();
        }
        this.facetOptions = facetOptions;
    }

    /**
     * Adds a new search option to this facet.
     * @param opt the option to be added to this facet.
     */
    public void addFacetOption(FacetOption opt) {
        if (opt == null) {
            throw new IllegalArgumentException();
        }

        this.facetOptions.add(opt);
    }

    @Override
    public boolean isActive() {
        return isActive;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    /**
     * This operation is not supported. The prefix is set automatically by the
     * {@link FacetService}.
     * @param prefix 
     */
    public void setPrefix(String prefix) {
        throw new UnsupportedOperationException();
    }

    /**
     * Sets the active state of this facet. Active facets are applied to search queries.
     * @param active true if this facet should be applied, false otherwise.
     */
    protected void setActive(boolean active) {
        this.isActive = active;
    }
    
    @Override
    public void applyPredicate(HqlQuery q) {
        Predicate facetPredicate = null;
        for(FacetOption opt : getFacetOptions()) {
            if(opt.isSelected()) {
                Predicate optPredicate = opt.getPredicate(); 
                facetPredicate = facetPredicate == null ? optPredicate : facetPredicate.or(optPredicate);
            }
        }
        
        if(facetPredicate != null) {
            q.and(facetPredicate);
        }
    }
    
    @Override
    public int getWeight() {
        return weight;
    }
    
    // -------------------------------------
    // Utility Functions
    // -------------------------------------
    @Override
    public String getInputName() {
        return String.format("%s_%s", getPrefix(), getQueryParamName());
    }
}
