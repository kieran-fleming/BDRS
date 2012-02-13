package au.com.gaiaresources.bdrs.model.taxa;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Lob;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OrderBy;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.attribute.Attributable;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.util.CollectionUtils;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "INDICATOR_SPECIES")
@AttributeOverride(name = "id", column = @Column(name = "INDICATOR_SPECIES_ID"))
public class IndicatorSpecies extends PortalPersistentImpl implements Attributable<IndicatorSpeciesAttribute> {
    
    public static final String FIELD_SPECIES_NAME = "Field Species";
    
    private String scientificNameAndAuthor;
    private String scientificName;
    private String commonName;
    private TaxonGroup taxonGroup;
    private Set<Region> regions = new HashSet<Region>();
    private Set<String> regionNames = new HashSet<String>();
    private Set<IndicatorSpeciesAttribute> attributes = new HashSet<IndicatorSpeciesAttribute>();
    private List<SpeciesProfile> infoItems = new ArrayList<SpeciesProfile>();
    private IndicatorSpecies parent;
    private TaxonRank rank;
    private String author;
    private String year;
    private String source;
    private String sourceId;
    
    private Set<Metadata> metadata = new HashSet<Metadata>();
    // Cache of metadata mapped against the key. This is not a database 
    // column or relation.
    private Map<String, Metadata> metadataLookup = null;

    @CompactAttribute
    @CollectionOfElements(fetch = FetchType.LAZY)
    @JoinColumn(name = "INDICATOR_SPECIES_ID")
    @OrderBy("weight")
    public List<SpeciesProfile> getInfoItems() {
        return infoItems;
    }

    public void setInfoItems(List<SpeciesProfile> infoItems) {
        this.infoItems = infoItems;
    }

    @CompactAttribute
    @OneToMany
//    @JoinColumn(name = "INDICATOR_SPECIES_ID")
    @Override
    public Set<IndicatorSpeciesAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<IndicatorSpeciesAttribute> attributes) {
        this.attributes = attributes;
    }
    
    @CompactAttribute
    @Column(name = "SCIENTIFIC_NAME_AND_AUTHOR", nullable = true)
    public String getScientificNameAndAuthor() {
        return scientificNameAndAuthor;
    }

    public void setScientificNameAndAuthor(String scientificNameAndAuthor) {
        this.scientificNameAndAuthor = scientificNameAndAuthor;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "SCIENTIFIC_NAME", nullable = false)
    @Index(name="species_scientific_name_index")
    public String getScientificName() {
        return scientificName;
    }

    public void setScientificName(String scientificName) {
        this.scientificName = scientificName;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "COMMON_NAME", nullable = false)
    @Index(name="species_common_name_index")
    public String getCommonName() {
        return commonName;
    }

    public void setCommonName(String commonName) {
        this.commonName = commonName;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "TAXON_GROUP_ID", nullable = false)
    @ForeignKey(name = "IND_SPECIES_TAXON_GROUP_FK")
    public TaxonGroup getTaxonGroup() {
        return taxonGroup;
    }

    public void setTaxonGroup(TaxonGroup taxonGroup) {
        this.taxonGroup = taxonGroup;
    }

    /**
     * {@inheritDoc}
     */
    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "INDICATOR_SPECIES_REGION", joinColumns = { @JoinColumn(name = "INDICATOR_SPECIES_ID") }, inverseJoinColumns = { @JoinColumn(name = "REGION_ID") })
    @ForeignKey(name = "INDICATOR_SPECIES_REGION_SPEICES_FK", inverseName = "INDICATOR_SPECIES_REGION_REGION_FK")
    // Hibernate FAIL.
    // https://forum.hibernate.org/viewtopic.php?f=1&t=1007658
    // https://forum.hibernate.org/viewtopic.php?f=1&t=998294
    // http://opensource.atlassian.com/projects/hibernate/browse/HHH-530?focusedCommentId=17890&page=com.atlassian.jira.plugin.system.issuetabpanels%3Acomment-tabpanel#action_17890
    //@Fetch(FetchMode.SUBSELECT)
    public Set<Region> getRegions() {
        return regions;
    }

    public void setRegions(Set<Region> regions) {
        this.regions = regions;
    }

    @ManyToOne
    @JoinColumn(name = "PARENT_ID")
    public IndicatorSpecies getParent() {
        return parent;
    }

    public void setParent(IndicatorSpecies parent) {
        this.parent = parent;
    }

    @CompactAttribute
    @Enumerated(EnumType.STRING)
    @Column(name = "RANK", nullable=true)
    public TaxonRank getTaxonRank() {
        return this.rank;
    }
    public void setTaxonRank(TaxonRank rank) {
        this.rank = rank;
    }

    @CompactAttribute
    @Column(name = "AUTHOR", nullable = true)
    public String getAuthor() {
        return author;
    }
    public void setAuthor(String author) {
        this.author = author;
    }
    
    @CompactAttribute
    @Column(name = "YEAR", nullable = true)
    public String getYear() {
        return year;
    }
    public void setYear(String year) {
        this.year = year;
    }  
    
    /**
     * The data source of this taxon. For example, ALA, Max, NSW Flora
     * 
     * @return the name of the data source
     */
    @Column(name="source", nullable=true)
    @Lob
    public String getSource() {
        return source;
    }

    /**
     * The data source of this taxon. For example, ALA, Max, NSW Flora
     * 
     * @param source the name of the source to set
     */
    public void setSource(String source) {
        this.source = source;
    }

    /**
     * The identifier for this taxon. It should either be globally unique or
     * locally unique within the data source of this taxon, see get/setSource()
     * If the identifier is locally unique within the data source - it is highly
     * recommended that the source for this taxon is non null!
     * 
     * @return identifier for this taxon
     */
    @Column(name="source_id", nullable=true)
    @Lob
    @Index(name="indicator_species_source_id_index")
    public String getSourceId() {
        return sourceId;
    }

    /**
     * The identifier for this taxon. It should either be globally unique or
     * locally unique within the data source of this taxon, see get/setSource()
     * If the identifier is locally unique within the data source - it is highly
     * recommended that the source for this taxon is non null!
     * 
     * @param sourceId identifier for this taxon
     */
    public void setSourceId(String sourceId) {
        this.sourceId = sourceId;
    }

    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the metadata id.
    /**
     * Sets the Metadata associated with this IndicatorSpecies
     * 
     * @return Metadata associated with this IndicatorSpecies
     */
    @ManyToMany(fetch = FetchType.LAZY, cascade = { CascadeType.REMOVE })
    @Cascade(org.hibernate.annotations.CascadeType.DELETE)
    public Set<Metadata> getMetadata() {
        return metadata;
    }

    /**
     * Sets the Metadata associated with this IndicatorSpecies
     * 
     * @param metadata Metadata associated with this IndicatorSpecies.
     */
    public void setMetadata(Set<Metadata> metadata) {
        this.metadata = metadata;
    }
    
    /**
     * Gets a Metadata for this species by key
     * 
     * @param key key used to search for Metadata
     * @return Metadata with the requested key if it exists, null otherwise
     */
    @Transient
    public Metadata getMetadataByKey(String key) {
        if(key == null) {
            return null;
        }
        
        // If it has not been initialised or the metadata has since been changed,
        if(metadataLookup == null || (metadataLookup!= null && metadataLookup.size() != metadata.size())) {
            metadataLookup = new HashMap<String, Metadata>(metadata.size());
            for(Metadata md : metadata) {
                metadataLookup.put(md.getKey(), md);
            }    
        }
        
        return metadataLookup.get(key);
    }

    @Transient
    public Set<String> getRegionNames() {
        // Lazily load the names...
        if (this.regionNames == null) {
            this.regionNames = new HashSet<String>();
            for (Region r : CollectionUtils.nullSafeFor(this.getRegions())) {
                regionNames.add(r.getRegionName());
            }
        }
        return regionNames;
    }

    @Override
    @Transient
    public IndicatorSpeciesAttribute createAttribute() {
        return new IndicatorSpeciesAttribute();
    }
}
