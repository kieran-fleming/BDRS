package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.MonthFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link MonthFacet}s.
 */
public class MonthFacetBuilder extends AbstractFacetBuilder<MonthFacet> {
    
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records on a per monthly basis.";
    
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "Month";

    /**
     * Creates a new instance.
     */
    public MonthFacetBuilder() {
        super(MonthFacet.class);
    }
    
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public Facet createFacet(RecordDAO recordDAO,
            Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        return new MonthFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams);
    }

    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
    
}