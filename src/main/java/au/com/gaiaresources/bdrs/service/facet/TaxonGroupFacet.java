package au.com.gaiaresources.bdrs.service.facet;

import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.Pair;
import edu.emory.mathcs.backport.java.util.Arrays;

/**
 * Represents taxonomic records based on the {@link TaxonGroup} of the associated
 * @link {@link IndicatorSpecies}.
 */
public class TaxonGroupFacet extends AbstractFacet {
    /**
     * The base name of the query parameter.
     */
    public static final String QUERY_PARAM_NAME = "taxonGroup";

    /**
     * Creates a new instance.
     * 
     * @param defaultDisplayName the default human readable name of this facet.
     * @param recordDAO used for retrieving the count of matching records.
     * @param parameterMap the map of query parameters from the browser.
     * @param user the user that is accessing the records.
     * @param userParams user configurable parameters provided in via the {@link Preference)}.
     */
    public TaxonGroupFacet(String defaultDisplayName, RecordDAO recordDAO, Map<String, String[]> parameterMap, User user, JSONObject userParams) {
        super(QUERY_PARAM_NAME, defaultDisplayName, userParams);
        setContainsSelected(parameterMap.containsKey(getInputName()));
        
        String[] selectedOptions = parameterMap.get(getInputName());
        if(selectedOptions == null) {
            selectedOptions = new String[]{};
        }
        Arrays.sort(selectedOptions);
        
        List<Pair<TaxonGroup, Long>> pairs = recordDAO.getDistinctTaxonGroups(null, user);
        for(Pair<TaxonGroup, Long> pair : pairs) {
            super.addFacetOption(new TaxonGroupFacetOption(pair.getFirst(), pair.getSecond(), selectedOptions));
        }
    }
}
