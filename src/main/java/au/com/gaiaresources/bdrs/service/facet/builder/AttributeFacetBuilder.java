package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.AttributeFacet;
import au.com.gaiaresources.bdrs.service.facet.Facet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link AttributeFacet}s.
 * @author stephanie
 */
public class AttributeFacetBuilder extends AbstractFacetBuilder<AttributeFacet> {
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records to ones with the selected attribute values.";
    
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "AttributeFacet";
    
    private int facetIndex = 0;
    
    /**
     * Creates an instance of this class which will build an {@link AttributeFacet}.
     */
    public AttributeFacetBuilder() {
        super(AttributeFacet.class);
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder#createFacet(au.com.gaiaresources.bdrs.model.record.RecordDAO, java.util.Map, au.com.gaiaresources.bdrs.model.user.User, net.sf.json.JSONObject)
     */
    @Override
    public Facet createFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        return new AttributeFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams, facetIndex++);
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder#getPreferenceDescription()
     */
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
}
