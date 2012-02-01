package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.LocationAttributeFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link LocationAttributeFacet}s.
 * @author stephanie
 */
public class LocationAttributeFacetBuilder extends AbstractFacetBuilder<LocationAttributeFacet> {
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records to ones with the selected location attribute values.";
    
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "LocationAttributeFacet";
    
    private int facetIndex = 0;
    
    /**
     * Creates an instance of this class which will build an {@link AttributeFacet}.
     */
    public LocationAttributeFacetBuilder() {
        super(LocationAttributeFacet.class);
    }

    @Override
    public Facet createFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        return new LocationAttributeFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams, facetIndex++);
    }

    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
}
