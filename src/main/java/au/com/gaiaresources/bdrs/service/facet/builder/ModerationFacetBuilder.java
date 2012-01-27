package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.ModerationFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link ModerationFacet}s.
 * @author stephanie
 */
public class ModerationFacetBuilder extends AbstractFacetBuilder<ModerationFacet> {
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records to ones that are moderated.";
    
    /**
     * The human readable name of this facet.
     */
    public static final String DEFAULT_DISPLAY_NAME = "Moderation Status";
    
    /**
     * Creates an instance of this class which will build an {@link AttributeFacet}.
     */
    public ModerationFacetBuilder() {
        super(ModerationFacet.class);
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.service.facet.builder.FacetBuilder#createFacet(au.com.gaiaresources.bdrs.model.record.RecordDAO, java.util.Map, au.com.gaiaresources.bdrs.model.user.User, net.sf.json.JSONObject)
     */
    @Override
    public Facet createFacet(RecordDAO recordDAO, Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        return new ModerationFacet(DEFAULT_DISPLAY_NAME, recordDAO, parameterMap, user, userParams);
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
