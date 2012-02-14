package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONObject;
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
     * A string that describes what the 'attributeName' user configuration parameter will do.
     */
    public static final String ATTRIBUTE_NAME_CONFIG_DESCRIPTION = String.format("<dd><code>%s</code> - the name of the attribute, also the name of the facet.</dd>", "attributeName");
    /**
     * A string that describes what the 'optionCount' user configuration parameter will do.
     */
    public static final String OPTION_COUNT_CONFIG_DESCRIPTION = String.format("<dd><code>%s</code> - the number of options to show in the facet for the attribute</dd>", "optionCount");
    
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
    
    protected List<String> getFacetParameterDescription() {
        List<String> list = new ArrayList<String>();
        list.add(Facet.ACTIVE_CONFIG_DESCRIPTION);
        list.add(Facet.WEIGHT_CONFIG_DESCRIPTION);
        list.add(ATTRIBUTE_NAME_CONFIG_DESCRIPTION);
        list.add(OPTION_COUNT_CONFIG_DESCRIPTION);
        return list;
    }
    
    @Override
    public String getDefaultDisplayName() {
        return DEFAULT_DISPLAY_NAME;
    }
}
