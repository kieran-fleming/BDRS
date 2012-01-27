package au.com.gaiaresources.bdrs.service.facet.builder;

import java.util.Map;

import net.sf.json.JSONObject;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceCategory;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.facet.Facet;

/**
 * The <code>FacetBuilder</code> is responsible for the creation of Facet 
 * instances. 
 */
public interface FacetBuilder {
    
    /**
     * Returns the key for the preference containing configuration values for
     * the facets created by this builder.
     * @return the preference key for the facet represented by this builder.
     */
    public String getPreferenceKey();
    
    /**
     * Returns the description content to be associated with the facet preference.
     * @return the description to be associated with the facet preference.
     */
    public abstract String getPreferenceDescription();
    
    /**
     * Gets the default preference for this facet. The default preference 
     * contains default configuration values for this facet.
     * @param portal the {@link Portal} to be associated with this {@link Preference}.
     * @param category the {@link PreferenceCategory} to be associated with this {@link Preference}.
     * @return the default preference
     */
    public Preference getDefaultPreference(Portal portal, PreferenceCategory category);

    /**
     * Creates a new concrete Facet.
     * 
     * @param recordDAO the dao used to count the applicable records.
     * @param parameterMap a mapping of query parameters.
     * @param user the user requesting the listing of records.
     * @param userParams the user configuration parameters to be passed to the Facet.
     * @return a new instance of a Facet.
     */
    public Facet createFacet(RecordDAO recordDAO,  Map<String, String[]> parameterMap, User user, JSONObject userParams);

    /**
     * Returns the class of the Facet represented by this builder.
     * @return the class of the Facet that is created by this builder.
     */
    public Class<? extends Facet> getFacetClass();
    
    /**
     * Returns the default human readable name of this facet.
     * @return the default human readable name of this facet.
     */
    public String getDefaultDisplayName();
}
