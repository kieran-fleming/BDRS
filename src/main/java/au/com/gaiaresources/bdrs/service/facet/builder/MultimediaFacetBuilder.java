package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.MultimediaFacet;

/**
 * The concrete implementation of the {@link AbstractFacetBuilder} that creates
 * {@link MultimediaFacet}s.
 */
public class MultimediaFacetBuilder extends AbstractFacetBuilder {
    
    /**
     * Describes the function of this facet that will be used in the preference description.
     */
    public static final String FACET_DESCRIPTION = "Restricts records depending if it contains a non-empty file or image record attribute.";
    
    /**
     * Creaes a new instance.
     */
    public MultimediaFacetBuilder() {
        super(MultimediaFacet.class);
    }
    
    @Override
    public String getPreferenceDescription() {
        return buildPreferenceDescription(FACET_DESCRIPTION, getFacetParameterDescription());
    }

    @Override
    public Facet createFacet(RecordDAO recordDAO,
            Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        
        return new MultimediaFacet(recordDAO, parameterMap, user, userParams);
    }
}