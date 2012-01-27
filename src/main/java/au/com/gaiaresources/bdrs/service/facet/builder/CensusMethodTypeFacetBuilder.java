package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.CensusMethodTypeFacet;
import au.com.gaiaresources.bdrs.service.facet.Facet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link CensusMethodTypeFacet}s.
 */
public class CensusMethodTypeFacetBuilder extends AbstractFacetBuilder<CensusMethodTypeFacet> {
    
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records to the type of the associated census method or a null census method";
    
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "Record Type";
    
    /**
     * Creaes a new instance.
     */
    public CensusMethodTypeFacetBuilder() {
        super(CensusMethodTypeFacet.class);
    }
    
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public Facet createFacet(RecordDAO recordDAO,
            Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        
        return new CensusMethodTypeFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams);
    }

    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
}