package au.com.gaiaresources.bdrs.model.taxa;

import java.util.Collection;
import java.util.List;
import java.util.Set;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.util.Pair;

public interface TaxaDAO {
    TaxonGroup createTaxonGroup(String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                boolean includeNumber);

    TaxonGroup createTaxonGroup(String name, boolean includeBehaviour,
            boolean includeFirstAppearance, boolean includeLastAppearance,
            boolean includeHabitat, boolean includeWeather,
            boolean includeNumber, String image, String thumbNail);

    TaxonGroup updateTaxonGroup(Integer id, String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                boolean includeNumber);
    TaxonGroup updateTaxonGroup(Integer id, String name, boolean includeBehaviour, boolean includeFirstAppearance,
            boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
            boolean includeNumber, String image, String thumbnail);

    TaxonGroup getTaxonGroup(String name);

    TaxonGroup getTaxonGroup(Integer id);

    List<TaxonGroup> getTaxonGroup(Survey survey);

    /**
     * Returns all taxon groups in alphabetical order.
     * @return
     */
    List<? extends TaxonGroup> getTaxonGroups();

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
    Attribute createAttribute(TaxonGroup group, String name, String description, AttributeType type, boolean required, boolean isTag);

    /**
     * Update a <code>TaxonGroupAttribute</code>.
     * @param id The id if the attribute to update.
     * @param name The new name of the attribute.
     * @param type The new data type of the attribute.
     * @param reqired Is this attribute required.
     * @return <code>TaxonGroupAttribute</code>.
     */
    Attribute updateAttribute(Integer id, String name, AttributeType type, boolean required);
    /**
     * Update a <code>TaxonGroupAttribute</code>.
     * @param id The id if the attribute to update.
     * @param name The new name of the attribute.
     * @param description The new description of the attribute.
     * @param type The new data type of the attribute.
     * @param reqired Is this attribute required.
     * @return <code>TaxonGroupAttribute</code>.
     */
    Attribute updateAttribute(Integer id, String name, String description, AttributeType type, boolean required);

    /**
     * Create a TaxonGroupAttributeOption, usually an option for type string_with_valid_options
     * @param attribute the attribute to attach this option to
     * @param option the value for the option
     * @return <code>TaxonGroupAttributeOption</code>
     */
    AttributeOption createAttributeOption(Attribute attribute, String option);

    TypedAttributeValue createIndicatorSpeciesAttribute(IndicatorSpecies species, Attribute attr, String value);
    TypedAttributeValue createIndicatorSpeciesAttribute(IndicatorSpecies species, Attribute attr, String value, String desc);

    /**
     * Deletes a given option.
     * @param id the id of the <code>TaxonGroupAttributeOption</code>
     */
    void deleteTaxonGroupAttributeOption(Integer id);

    AttributeOption getOption(Integer id);

    /**
     * Delete a <code>IndicatorSpeciesAttribute</code>.
     * @param id The id if the attribute to delete.
     */
    void delete(IndicatorSpeciesAttribute attr);
    
    /**
     * Get a taxon group attribute by id.
     * @param id <code>Integer</code>.
     * @return <code>TaxonGroupAttribute</code>.
     */
    Attribute getAttribute(Integer id);
    
    /**
     * Get a taxon group attribute by its values
     * @param taxonGroup
     * @param name
     * @param isTag
     * @return first attribute matching the parameters
     */
    Attribute getAttribute(TaxonGroup taxonGroup, String name, boolean isTag);

    IndicatorSpecies createIndicatorSpecies(String scientificName, String commonName, TaxonGroup taxonGroup,
                                            Collection<Region> regions, List<SpeciesProfile> infoItems);

    IndicatorSpecies updateIndicatorSpecies(Integer id,
            String scientificName, String commonName, TaxonGroup taxonGroup,
            Collection<Region> regions, List<SpeciesProfile> infoItems);
    
    IndicatorSpecies updateIndicatorSpecies(Integer id,
            String scientificName, String commonName, TaxonGroup taxonGroup,
            Collection<Region> regions, List<SpeciesProfile> infoItems, Set<IndicatorSpeciesAttribute> attributes);

    List<IndicatorSpecies> getIndicatorSpecies();

    List<IndicatorSpecies> getIndicatorSpeciesById(Integer[] pks);
    
    List<IndicatorSpecies> getIndicatorSpeciesBySpeciesProfileItem(String type, String content);

    List<IndicatorSpecies> getIndicatorSpecies(Region region);

    List<IndicatorSpecies> getIndicatorSpecies(TaxonGroup group);

    List<IndicatorSpecies> getIndicatorSpeciesByNameSearch(String name);

    IndicatorSpecies getIndicatorSpecies(Integer id);
    
    SpeciesProfile getSpeciesProfileById(Integer id);
    
    /**
     * Returns the indicator species with the given GUID, or null if one could
     * not be found.
     * @param guid
     * @return
     */
    IndicatorSpecies getIndicatorSpeciesByGuid(String guid);

    List<IndicatorSpecies> getIndicatorSpeciesByCommonName(String commonName);

    IndicatorSpecies refresh(IndicatorSpecies s);

    List<IndicatorSpecies> getIndicatorSpecies(
            Integer[] taxonGroupIds);
    
    Integer countIndicatorSpecies(Integer[] taxonGroupIds);

    Integer countAllSpecies();

    IndicatorSpecies getIndicatorSpeciesByScientificName(String scientificName);

    IndicatorSpecies getIndicatorSpeciesByScientificName(Session sesh,
            String scientificName);
    
    IndicatorSpecies getIndicatorSpeciesByScientificNameAndRank(String scientificName, TaxonRank rank);
    
    IndicatorSpecies getIndicatorSpeciesByScientificNameAndRank(Session sesh,
            String scientificName, TaxonRank rank);

    List<TaxonGroup> getTaxonGroupSearch(String nameFragment);

    Attribute save(Attribute attribute);
    
    IndicatorSpecies save(IndicatorSpecies species);
    
    IndicatorSpecies save(Session sesh, IndicatorSpecies species);

    AttributeOption save(AttributeOption opt);
    
    TaxonGroup save(TaxonGroup taxongroup);

    IndicatorSpecies updateIndicatorSpecies(IndicatorSpecies is);

    int countSpeciesForSurvey(Survey survey);

    IndicatorSpecies getIndicatorSpeciesByCommonName(Session sesh,
            String commonName);

    TaxonGroup getTaxonGroup(Session sesh, String name);

    TaxonGroup save(Session sesh, TaxonGroup taxongroup);
    
    IndicatorSpeciesAttribute save(IndicatorSpeciesAttribute taxonAttribute);
    IndicatorSpeciesAttribute save(Session sesh, IndicatorSpeciesAttribute taxonAttribute);

    IndicatorSpecies getIndicatorSpeciesBySourceDataID(Session sesh, String sourceDataId);
    
    /**
     * Returns the indicator species for the specified survey. If the survey
     * does not have any attached indicator species, this function will return
     * all indicator species.
     * @param sesh the session to use to retrieve the IndicatorSpecies
     * @param survey the survey associated with the indicator species.
     * @param start the first indicator species.
     * @param count the maximum number of indicator species to return.
     * @return the indicator species associated with the specified survey.
     */
    List<IndicatorSpecies> getIndicatorSpeciesBySurvey(Session sesh, Survey survey, int start, int maxSize);

    List<IndicatorSpecies> getIndicatorSpeciesListByScientificName(
            String scientificName);
    
    List<Pair<IndicatorSpecies, Integer>> getTopSpecies(int userPk, int limit);

    List<TaxonGroup> getTaxonGroupsSortedByName();

    PagedQueryResult<IndicatorSpecies> getIndicatorSpecies(
            TaxonGroup taxonGroup, PaginationFilter filter);

    List<IndicatorSpecies> getChildTaxa(IndicatorSpecies taxon);

    void delete(IndicatorSpecies taxon);

    void delete(TaxonGroup taxonGroup);
}
