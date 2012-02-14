package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.TaxonGroupFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link TaxonGroupFacet}s.
 */
public class TaxonGroupFacetBuilder extends AbstractFacetBuilder<TaxonGroupFacet> {
    
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Represents taxonomic records based on the group of the associated organism.";
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "Species Group";
    
    /**
     * Creaes a new instance.
     */
    public TaxonGroupFacetBuilder() {
        super(TaxonGroupFacet.class);
    }
    
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public Facet createFacet(RecordDAO recordDAO,
            Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        
        return new TaxonGroupFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams);
    }

    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
    
    
}