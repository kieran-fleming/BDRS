package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.YearFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link YearFacet}s.
 */
public class YearFacetBuilder extends AbstractFacetBuilder<YearFacet> {
    
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "Year";
    
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records on a yearly basis.";

    /**
     * Creates a new instance.
     */
    public YearFacetBuilder() {
        super(YearFacet.class);
    }
    
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public Facet createFacet(RecordDAO recordDAO,
            Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        return new YearFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams);
    }
    
    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
}