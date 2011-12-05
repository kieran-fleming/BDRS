package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.LocationFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link LocationFacet}s.
 * @author stephanie
 */
public class LocationFacetBuilder extends AbstractFacetBuilder {
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records by location.";

    public LocationFacetBuilder() {
        super(LocationFacet.class);
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder#getPreferenceDescription()
     */
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder#createFacet(au.com.gaiaresources.bdrs.model.record.RecordDAO, java.util.Map, au.com.gaiaresources.bdrs.model.user.User, net.sf.json.JSONObject)
     */
    @Override
    public Facet createFacet(RecordDAO recordDAO,
            Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        return new LocationFacet(recordDAO, parameterMap, user, userParams);
    }
}
