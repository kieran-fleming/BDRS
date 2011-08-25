package au.com.gaiaresources.bdrs.model.taxa.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

/**
 * Implementation of <code>TaxaService</code>.
 * @author Tim Carpenter
 *
 */
@Service
public class TaxaServiceImpl implements TaxaService {
	Logger log = Logger.getLogger(TaxaServiceImpl.class);
	
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private RegionDAO regionDAO;
    @Autowired
    private SpeciesProfileDAO speciesProfileDAO;
    @Autowired
    private AttributeDAO attributeDAO;  
    
    
    @Override
    public TaxonGroup createTaxonGroup(String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                       boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                       boolean includeNumber)
    {
        return taxaDAO.createTaxonGroup(name, includeBehaviour, includeFirstAppearance, includeLastAppearance,
                                        includeHabitat, includeWeather, includeNumber);
    }
    
    @Override
	public TaxonGroup createTaxonGroup(String name, boolean includeBehaviour,
			boolean includeFirstAppearance, boolean includeLastAppearance,
			boolean includeHabitat, boolean includeWeather,
			boolean includeNumber, String image, String thumbNail) {
		return taxaDAO.createTaxonGroup(name, includeBehaviour, includeFirstAppearance, includeLastAppearance, includeHabitat, includeWeather, includeNumber, image, thumbNail);
	}
    
    
    @Override
    public TaxonGroup updateTaxonGroup(Integer id, String name, boolean includeBehaviour, boolean includeFirstAppearance,
                                       boolean includeLastAppearance, boolean includeHabitat, boolean includeWeather,
                                       boolean includeNumber)
    {
        return taxaDAO.updateTaxonGroup(id, name, includeBehaviour, includeFirstAppearance, includeLastAppearance,
                                        includeHabitat, includeWeather, includeNumber);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TaxonGroup getTaxonGroup(Integer id) {
        return taxaDAO.getTaxonGroup(id);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public TaxonGroup getTaxonGroup(String name) {
        return taxaDAO.getTaxonGroup(name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends TaxonGroup> getTaxonGroups() {
        return taxaDAO.getTaxonGroups();
    }
    
    /**
     * {@inheritDoc}
     */
    public Attribute createAttribute(TaxonGroup group, String name, AttributeType type, boolean required) {
        return taxaDAO.createAttribute(group, name, type, required);
    }
    
    /**
     * {@inheritDoc}
     */
    public Attribute createAttribute(TaxonGroup group, String name, AttributeType type, boolean required, boolean isTag) {
        return taxaDAO.createAttribute(group, name, type, required, isTag);
    }
    
    /**
     * {@inheritDoc}
     */
    public AttributeOption createAttributeOption(Attribute attribute, String value) {
    	return taxaDAO.createAttributeOption(attribute, value);
    }
    
    public TypedAttributeValue createIndicatorSpeciesAttribute(IndicatorSpecies species, Attribute attr, String value)
    {
    	return taxaDAO.createIndicatorSpeciesAttribute(species, attr, value);
    }

    public TypedAttributeValue createIndicatorSpeciesAttribute(IndicatorSpecies species, Attribute attr, String value, String desc)
    {
    	return taxaDAO.createIndicatorSpeciesAttribute(species, attr, value, desc);
    }
    
    /**
     * {@inheritDoc}
     */
    public Attribute updateAttribute(Integer id, String name, AttributeType type, boolean required) {
        return taxaDAO.updateAttribute(id, name, type, required);
    }
    
    /**
     * {@inheritDoc}
     */
    public Attribute getAttribute(Integer id) {
        return taxaDAO.getAttribute(id);
    }
    
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IndicatorSpecies createIndicatorSpecies(String scientificName, String commonName, TaxonGroup taxonGroup,
                                                   Collection<String> regionNames, List<SpeciesProfile> infoItems) 
    {
        return taxaDAO.createIndicatorSpecies(scientificName, commonName, taxonGroup, 
                                              regionDAO.getRegions(regionNames.toArray(new String[regionNames.size()])), infoItems);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public IndicatorSpecies updateIndicatorSpecies(Integer id, String scientificName, String commonName, 
                                                   TaxonGroup taxonGroup, Collection<String> regionNames, List<SpeciesProfile> infoItems)
    {
        return taxaDAO.updateIndicatorSpecies(id, scientificName, commonName, taxonGroup, 
                                              regionDAO.getRegions(regionNames.toArray(new String[regionNames.size()])), infoItems);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends IndicatorSpecies> getIndicatorSpecies() {
        return taxaDAO.getIndicatorSpecies();
    }
    
    public Map<TaxonGroup, List<IndicatorSpecies>> getGroupedIndicatorSpecies() {
        Map<TaxonGroup, List<IndicatorSpecies>> grouped = new HashMap<TaxonGroup, List<IndicatorSpecies>>();
        //TODO: change 'getIndicatorSpecies()' to 'getIndicatorSpecies(groupSurveyId)'
        for (IndicatorSpecies i : getIndicatorSpecies()) {
            if (!grouped.containsKey(i.getTaxonGroup())) {
                grouped.put(i.getTaxonGroup(), new ArrayList<IndicatorSpecies>());
            }
            grouped.get(i.getTaxonGroup()).add(i);
        }
        
        return grouped;
    }
    
    public Map<TaxonGroup, List<IndicatorSpecies>> getGroupedIndicatorSpecies(Region region) {
        Map<TaxonGroup, List<IndicatorSpecies>> grouped = new HashMap<TaxonGroup, List<IndicatorSpecies>>();
        
        for (IndicatorSpecies i : getIndicatorSpecies(region)) {
            if (!grouped.containsKey(i.getTaxonGroup())) {
                grouped.put(i.getTaxonGroup(), new ArrayList<IndicatorSpecies>());
            }
            grouped.get(i.getTaxonGroup()).add(i);
        }
        
        return grouped;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends IndicatorSpecies> getIndicatorSpecies(Region region) {
        return taxaDAO.getIndicatorSpecies(region);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<? extends IndicatorSpecies> getIndicatorSpecies(TaxonGroup group) {
        return taxaDAO.getIndicatorSpecies(group);
    }
    
    @Override
    public IndicatorSpecies getIndicatorSpecies(Integer speciesID) {
        return taxaDAO.getIndicatorSpecies(speciesID);
    }
    
    @Override
    public List<? extends IndicatorSpecies> getIndicatorSpecies(String commonName) {
    	return taxaDAO.getIndicatorSpeciesByCommonName(commonName);
    }
    
    public List<String> getGroupedIndicatorSpeciesNames(){
    	return null;
    }

	@Override
	public List<String> getTaxonGroupNames(
			Map<TaxonGroup, List<IndicatorSpecies>> groupData) {
		List<String> names = new ArrayList<String>();
		for(Map.Entry<TaxonGroup, List<IndicatorSpecies>> entry : groupData.entrySet()){
			names.add(entry.getKey().getName());
		}
		return names;
	}
	
	@Override
	public List<? extends IndicatorSpecies> getIndicatorSpeciesByNameSearch(
			String name) {
		return taxaDAO.getIndicatorSpeciesByNameSearch(name);
	}

	@Override
	public SpeciesProfile createSpeciesProfile(String header, String content, String type) {
		return speciesProfileDAO.createSpeciesProfile(header, content, type);
	}
	
	@Override
	public List<SpeciesProfile> getSpeciesProfile(String type, IndicatorSpecies species){
		List <SpeciesProfile> infoItems_subset = new ArrayList<SpeciesProfile>();
		List<SpeciesProfile> infoItems  = species.getInfoItems();
		log.debug("SpeciesProfile_Items:"+infoItems.size());
    	for (SpeciesProfile s : infoItems){
    		if(s.getType().equalsIgnoreCase(type)){
    			infoItems_subset.add(s);
    		}
    	}
    	return infoItems_subset;
	}

	@Override
	public Attribute getFieldNameAttribute() {
	    IndicatorSpecies fieldSpecies = getFieldSpecies();
	    TaxonGroup fieldNamesGroup = fieldSpecies.getTaxonGroup();
	    
	    for(Attribute attr : fieldNamesGroup.getAttributes()) {
	        if(Attribute.FIELD_NAME_NAME.equals(attr.getName()) &&
	                Attribute.FIELD_NAME_DESC.equals(attr.getDescription())) {
	            return attr;
	        }
	    }
	    
	    // if we got here, there isn't a field name attribute yet - so create one.
	    Attribute attr = new Attribute();
	    attr.setDescription(Attribute.FIELD_NAME_DESC);
	    attr.setName(Attribute.FIELD_NAME_NAME);
	    attr.setRequired(false);
	    attr.setTag(false);
	    attr.setTypeCode(AttributeType.STRING.getCode());
	    attr = attributeDAO.save(attr);
	    
	    fieldNamesGroup.getAttributes().add(attr);
	    taxaDAO.save(fieldNamesGroup);
	    
	    return attr;
	}

	@Override
	public IndicatorSpecies getFieldSpecies() {
	    
	    IndicatorSpecies fieldSpecies = taxaDAO.getIndicatorSpeciesByScientificName(IndicatorSpecies.FIELD_SPECIES_NAME);
	    if(fieldSpecies == null) {
	           TaxonGroup fieldNamesGroup = taxaDAO.getTaxonGroup(TaxonGroup.FIELD_NAMES_GROUP_NAME);
	            if(fieldNamesGroup == null){
	                fieldNamesGroup = new TaxonGroup();
	                fieldNamesGroup.setName(TaxonGroup.FIELD_NAMES_GROUP_NAME);
	                fieldNamesGroup.setBehaviourIncluded(false);
	                fieldNamesGroup.setFirstAppearanceIncluded(false);
	                fieldNamesGroup.setHabitatIncluded(false);
	                fieldNamesGroup.setLastAppearanceIncluded(false);
	                fieldNamesGroup.setNumberIncluded(false);
	                fieldNamesGroup.setWeatherIncluded(false);
	                fieldNamesGroup = taxaDAO.save(fieldNamesGroup);
	            }
	            
	            fieldSpecies = new IndicatorSpecies();
	            fieldSpecies.setScientificName(IndicatorSpecies.FIELD_SPECIES_NAME);
                    fieldSpecies.setCommonName(IndicatorSpecies.FIELD_SPECIES_NAME);
	            
	            fieldSpecies.setAuthor("");
	            fieldSpecies.setScientificNameAndAuthor("");
	            fieldSpecies.setTaxonGroup(fieldNamesGroup);
	            fieldSpecies.setTaxonRank(TaxonRank.SPECIES);
	            fieldSpecies.setYear("");
	            
	            fieldSpecies = taxaDAO.save(fieldSpecies);
	    }
	    
	    return fieldSpecies;
	}
	    
    
}
