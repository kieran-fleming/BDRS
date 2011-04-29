package au.com.gaiaresources.bdrs.model.taxa;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import au.com.gaiaresources.bdrs.model.region.Region;

/**
 * Service for dealing with taxa related things. <code>TaxonGroups</code> and <code>IndicatorSpecies</code>.
 * @author Tim Carpenter
 *
 */
public interface TaxaService {

    ////////// TAXON GROUPS //////////

    /**
     * Create a new taxon group.
     * @param name The name of the group.
     * @param includeBehaviour boolean
     * @param includeFirstAppearance boolean
     * @param includeLastAppearance boolean
     * @param includeHabitat boolean
     * @param includeWeather boolean
     * @param includeNumber boolean
     * @return <code>TaxonGroup</code>.
     */
    TaxonGroup createTaxonGroup(String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                boolean includeNumber);

    /**
     * Create a new taxon group.
     * @param name The name of the group.
     * @param includeBehaviour boolean
     * @param includeFirstAppearance boolean
     * @param includeLastAppearance boolean
     * @param includeHabitat boolean
     * @param includeWeather boolean
     * @param includeNumber boolean
     * @param image String
     * @param thumbNail String
     * @return <code>TaxonGroup</code>.
     */
    TaxonGroup createTaxonGroup(String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                boolean includeNumber, String image, String thumbNail);

    /**
     * Create a new taxon group.
     * @param name The name of the group.
     * @param includeBehaviour boolean
     * @param includeFirstAppearance boolean
     * @param includeLastAppearance boolean
     * @param includeHabitat boolean
     * @param includeWeather boolean
     * @param includeNumber boolean
     * @return <code>TaxonGroup</code>.
     */
    TaxonGroup updateTaxonGroup(Integer id, String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                boolean includeNumber);

    /**
     * Retrieve a taxon group by id.
     * @param id <code>Integer</code>.
     * @return <code>TaxonGroup</code>.
     */
    TaxonGroup getTaxonGroup(Integer id);

    /**
     * Retrieve a taxon group by name.
     * @param name <code>String</code>.
     * @return <code>TaxonGroup</code>.
     */
    TaxonGroup getTaxonGroup(String name);

    /**
     * Get all of the taxon groups that are defined.
     * @return <code>List</code> of <code>TaxonGroup</code>s.
     */
    List<? extends TaxonGroup> getTaxonGroups();

    ///////////// TAXON GROUP ATTRIBUTES /////////////////

    /**
     * Create a <code>TaxonGroupAttribute</code>.
     * @param group <code>TaxonGroup</code> to which the attribute will belong.
     * @param name The name of the attribute.
     * @param type The data type of the attribute.
     * @param required Is this attribute required.
     * @return <code>TaxonGroupAttribute</code>.
     */
    Attribute createAttribute(TaxonGroup group, String name, AttributeType type, boolean required);

    Attribute createAttribute(TaxonGroup group, String name, AttributeType type, boolean required, boolean isTag);

    /**
     * Create a <code> TaxonGroupAttributeOption</code>.
     * @param attr <code>TaxonGroupAttribute</code> that this is an option for.
     * @param value the value of the option.
     * @return
     */
    AttributeOption createAttributeOption(Attribute attr, String value);

    AttributeValue createIndicatorSpeciesAttribute(IndicatorSpecies species, Attribute attr, String value);
    
    AttributeValue createIndicatorSpeciesAttribute(IndicatorSpecies species, Attribute attr, String value, String desc);
    
    /**
     * Update a <code>TaxonGroupAttribute</code>.
     * @param id The id if the attribute to update.
     * @param name The new name of the attribute.
     * @param type The new data type of the attribute.
     * @param required Is this attribute required.
     * @return <code>TaxonGroupAttribute</code>.
     */
    Attribute updateAttribute(Integer id, String name, AttributeType type, boolean required);

    /**
     * Get a taxon group attribute by id.
     * @param id <code>Integer</code>.
     * @return <code>TaxonGroupAttribute</code>.
     */
    Attribute getAttribute(Integer id);

    ////////// INDICATOR SPECIES //////////

    IndicatorSpecies createIndicatorSpecies(String scientificName, String commonName, TaxonGroup taxonGroup,
                                            Collection<String> regionNames, List<SpeciesProfile> infoItems);

    IndicatorSpecies updateIndicatorSpecies(Integer id, String scientificName, String commonName, TaxonGroup taxonGroup,
                                            Collection<String> regionNames, List<SpeciesProfile> infoItems);

    SpeciesProfile createSpeciesProfile(String header, String content,
			String type);

    List<SpeciesProfile> getSpeciesProfile(String type, IndicatorSpecies species);

    List<? extends IndicatorSpecies> getIndicatorSpecies();

    List<? extends IndicatorSpecies> getIndicatorSpeciesByNameSearch(String name);

    Map<TaxonGroup, List<IndicatorSpecies>> getGroupedIndicatorSpecies();

    Map<TaxonGroup, List<IndicatorSpecies>> getGroupedIndicatorSpecies(Region region);

    List<? extends IndicatorSpecies> getIndicatorSpecies(Region region);

    List<? extends IndicatorSpecies> getIndicatorSpecies(TaxonGroup group);

    IndicatorSpecies getIndicatorSpecies(Integer id);

    List<? extends IndicatorSpecies> getIndicatorSpecies(String commonName);

    List<String> getGroupedIndicatorSpeciesNames();

    /*Extracts TaxonGoupNames from Map and returns them as a List*/
	List<String> getTaxonGroupNames(
			Map<TaxonGroup, List<IndicatorSpecies>> groupData);

}
