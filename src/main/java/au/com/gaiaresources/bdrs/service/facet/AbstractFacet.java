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
     * @param defaultDisplayName the human readable name of this facet.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public AbstractFacet(String queryParamName, String defaultDisplayName, JSONObject userParams) {
        this.queryParamName = queryParamName;
        
        this.weight = userParams.optInt(JSON_WEIGHT_KEY, DEFAULT_WEIGHT_CONFIG);
        this.isActive = userParams.optBoolean(JSON_ACTIVE_KEY, DEFAULT_ACTIVE_CONFIG);
        this.prefix = userParams.optString(JSON_PREFIX_KEY, DEFAULT_PREFIX_CONFIG);
        this.displayName = userParams.optString(JSON_NAME_KEY, defaultDisplayName);
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
    public boolean isAllSelected() {
        if(facetOptions.isEmpty()) {
            return false;
        }
        
        for(FacetOption opt : facetOptions) {
            if(!opt.isSelected()) {
                return false;
            }
        }
        return true;
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

    /**
     * Inserts a new search option to this facet.
     * @param opt the option to be added to this facet.
     * @param index the index at which to insert this facet
     */
    public void insertFacetOption(FacetOption opt, int index) {
        if (opt == null) {
            throw new IllegalArgumentException();
        }

        this.facetOptions.add(index, opt);
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
    
    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.Facet#getPredicate()
     */
    @Override
    public Predicate getPredicate() {
        Predicate facetPredicate = null;
        for(FacetOption opt : getFacetOptions()) {
            if(opt.isSelected()) {
                Predicate optPredicate = opt.getPredicate(); 
                if (optPredicate != null) {
                    facetPredicate = facetPredicate == null ? optPredicate : facetPredicate.or(optPredicate);
                }
            }
        }
        
        return facetPredicate;
    }
    
    @Override
    public int getWeight() {
        return weight;
    }
    
    @Override
    public void applyCustomJoins(HqlQuery query) {
        // Do nothing. This is a placeholder for decendents to override if needed.
    }
    
    // -------------------------------------
    // Utility Functions
    // -------------------------------------
    @Override
    public String getInputName() {
        return String.format("%s_%s", getPrefix(), getQueryParamName());
    }
}
