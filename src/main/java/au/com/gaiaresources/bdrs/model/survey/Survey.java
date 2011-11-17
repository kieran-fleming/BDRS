package au.com.gaiaresources.bdrs.model.survey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.DateUtils;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "SURVEY")
@AttributeOverride(name = "id", column = @Column(name = "SURVEY_ID"))
public class Survey extends PortalPersistentImpl implements Comparable<Survey> {
	private String name;
	private String description;
    private boolean active;
    private Date startDate;
    private Date endDate;
    private boolean publik;

    private List<Location> locations = new ArrayList<Location>();
    private Set<User> users = new HashSet<User>();
    private Set<Group> groups = new HashSet<Group>();
    private Set<IndicatorSpecies> species = new HashSet<IndicatorSpecies>();

    private List<Attribute> attributes = new ArrayList<Attribute>();
    private List<CensusMethod> censusMethods = new ArrayList<CensusMethod>();

    private Set<Metadata> metadata = new HashSet<Metadata>();
    
    // Cache of metadata mapped against the key. This is not a database 
    // column or relation.
    private Map<String, Metadata> metadataLookup = null;
    
    // the default record publish level
    // Whatever this is set to will by the default settings of all new records
    // made for this survey.
    private static final RecordVisibility DEFAULT_RECORD_VISIBILITY = RecordVisibility.PUBLIC;
    // the default record publish modifable setting - true means users can alter their 
    // record publish settings to whatever they choose. False means the records
    // will have the same publish level as the default record publish level for
    // the survey.
    private static final boolean DEFAULT_RECORD_VISIBILITY_MODIFIABLE = true;
    
    private static final boolean DEFAULT_CENSUS_METHOD_PROVIDED_FOR_SURVEY = true;

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "NAME")
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "DESCRIPTION", length=1023)
    public String getDescription() {
        return description;
    }

    public void setDescription(String desc) {
        this.description = desc;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "ACTIVE")
    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "SURVEYDATE")
    public Date getStartDate() {
        return startDate != null ? (Date) startDate.clone() : null;
    }

    public void setStartDate(Date date) {
        this.startDate = date != null ? (Date) date.clone() : null;
    }

    public void setStartDate(String date) {
        setStartDate(DateUtils.getDate(date));
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "SURVEYENDDATE")
    public Date getEndDate() {
        if (this.endDate != null) {
            // make the end date inclusive by setting the HH:mm to 23:59
            Calendar cal = Calendar.getInstance();
            cal.setTime(this.endDate);
            cal.set(Calendar.HOUR_OF_DAY, 11);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            cal.set(Calendar.AM_PM, Calendar.PM);
            this.endDate = cal.getTime();
        }

        return endDate != null ? (Date) endDate.clone() : null;
    }

    public void setEndDate(Date date) {
        this.endDate = date != null ? (Date) date.clone() : null;
    }
    
    public void setEndDate(String date) {
        setEndDate(DateUtils.getDate(date));
    }

    @CompactAttribute
    @ManyToMany(fetch = FetchType.LAZY)
    @IndexColumn(name = "pos")
    public List<Location> getLocations() {
        return locations;
    }

    public void setLocations(List<Location> locations) {
        this.locations = locations;
    }

    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the user_id.
    @ManyToMany
    public Set<User> getUsers() {
        return users;
    }

    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @CompactAttribute
    @ManyToMany(fetch=FetchType.LAZY)
    public Set<IndicatorSpecies> getSpecies() {
        return species;
    }

    public void setSpecies(Set<IndicatorSpecies> species) {
        this.species = species;
    }

    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the user_id.
    @ManyToMany
    public Set<Group> getGroups() {
        return groups;
    }

    public void setGroups(Set<Group> groups) {
        this.groups = groups;
    }

    @CompactAttribute
    @OneToMany
    @IndexColumn(name = "pos")
    public List<Attribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(List<Attribute> attributes) {
        this.attributes = attributes;
    }

    @Column(name = "PUBLIC")
    public boolean isPublic() {
        return this.publik;
    }

    public void setPublic(boolean publik) {
        this.publik = publik;
    }

    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the metadata id.
    @ManyToMany(fetch = FetchType.LAZY)
    public Set<Metadata> getMetadata() {
        metadataLookup = null;
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        metadataLookup = null;
        this.metadata = metadata;
    }
    
    /**
     * {@inheritDoc}
     */
    @ManyToMany
    @IndexColumn(name = "pos")
    public List<CensusMethod> getCensusMethods() {
        return this.censusMethods;
    }
    public void setCensusMethods(List<CensusMethod> cmList) {
        this.censusMethods = cmList;
    }

    @Transient
    public Metadata setFormRendererType(SurveyFormRendererType rendererType) {
        Metadata md = getMetadataByKey(Metadata.FORM_RENDERER_TYPE);

        // Find the metadata or create it.
        if(md == null) {
            md = new Metadata();
            md.setKey(Metadata.FORM_RENDERER_TYPE);
        }

        if(rendererType == null){
            // Setting it to null so remove it from the set (if present).
            metadata.remove(md);
        } else {
            // Set the value and add it to the set.
            md.setValue(rendererType.toString());
            metadata.add(md);
        }
        return md;
    }

    @Transient
    public SurveyFormRendererType getFormRendererType() {
        Metadata md = getMetadataByKey(Metadata.FORM_RENDERER_TYPE);
        return md == null ? null : SurveyFormRendererType.valueOf(md.getValue());
    }
    
    @Transient
    public boolean isPredefinedLocationsOnly() {
        Metadata md = getMetadataByKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
        return md != null && Boolean.parseBoolean(md.getValue());
    }

    @Transient
    public Metadata getMetadataByKey(String key) {
        if(key == null) {
            return null;
        }
        for (Metadata m : metadata) {
            if (key.equals(m.getKey())) {
                return m;
            }
        }
        // not found. return null
        return null;
    }
    
    /**
     * The record visibility level the form is set to when creating
     * a new record
     * @return
     */
    @Transient
    public RecordVisibility getDefaultRecordVisibility() {
        Metadata md = getMetadataByKey(Metadata.DEFAULT_RECORD_VIS);
        return md == null ? DEFAULT_RECORD_VISIBILITY : RecordVisibility.parse(md.getValue());
    }
    
    /**
     * Set the default default record visibility setting as a metadata object.
     * Requires the metadata DAO object to save the metadata. Makes the client
     * code cleaner.
     * 
     * @param value
     * @param mdDAO - the metadata dao, required to save the metadata
     * @return
     */
    @Transient
    public Metadata setDefaultRecordVisibility(RecordVisibility value, MetadataDAO mdDAO) {
		if (value == null) {
            throw new IllegalArgumentException("value cannot be null");
        } 

        if (mdDAO == null) {
            throw new IllegalArgumentException("mdDAO cannot be null");
        }
        
        Metadata md = getMetadataByKey(Metadata.DEFAULT_RECORD_VIS);

        // Find the metadata or create it.
        if(md == null) {
            md = new Metadata();
            md.setKey(Metadata.DEFAULT_RECORD_VIS);
            // default value: full public (as it is for the Atlas).
            md.setValue(DEFAULT_RECORD_VISIBILITY.toString());
        }

        // Set the value and add it to the set.
        md.setValue(value.toString());
        
        if (!metadataContainsKey(md)) {
            metadata.add(md);
        }
        // save it!
        return mdDAO.save(md);
    }
    
    /**
     * Can users change the record publish level to whatever they like
     * 
     * @return
     */
    @Transient 
    public boolean isRecordVisibilityModifiable() {
        Metadata md = getMetadataByKey(Metadata.RECORD_VIS_MODIFIABLE);
        return md == null ? DEFAULT_RECORD_VISIBILITY_MODIFIABLE : Boolean.parseBoolean(md.getValue());
    }
    
    /**
     * Set the default record visibility modifiable flag as a metadata object.
     * Requires the metadata DAO object to save the metadata. Makes the client
     * code cleaner.
     * 
     * @param value 
     * @param mdDAO - the metadata dao, required to save the metadata
     * @return
     */
    @Transient
    public Metadata setRecordVisibilityModifiable(boolean value, MetadataDAO mdDAO) {
        if (mdDAO == null) {
            throw new IllegalArgumentException("mdDAO cannot be null");
        }
        
        Metadata md = getMetadataByKey(Metadata.RECORD_VIS_MODIFIABLE);

        // Find the metadata or create it.
        if(md == null) {
            md = new Metadata();
            md.setKey(Metadata.RECORD_VIS_MODIFIABLE);
            // default value: full public (as it is for the Atlas).
            md.setValue(Boolean.valueOf(DEFAULT_RECORD_VISIBILITY_MODIFIABLE).toString());
        }

        // Set the value and add it to the set.
        md.setValue(value ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        
        if (!metadataContainsKey(md)) {
            metadata.add(md);
        }
        // save it!
        return mdDAO.save(md);
    }
    
    /**
     * Whether or not the survey will have the default census method. Really this corresponds to
     * NO census method. It will appear as the 'standard taxonomic' form when contributing to the
     * survey. If you do NOT want this to appear then set this option to false!
     * @return
     */
    @Transient
    public boolean isDefaultCensusMethodProvided() {
        Metadata md = getMetadataByKey(Metadata.DEFAULT_CENSUS_METHOD_FOR_SURVEY);
        return md == null ? DEFAULT_CENSUS_METHOD_PROVIDED_FOR_SURVEY : Boolean.parseBoolean(md.getValue());
    }
    
    @Transient
    public Metadata setDefaultCensusMethodProvided(boolean value, MetadataDAO mdDAO) {
        if (mdDAO == null) {
            throw new IllegalArgumentException("mdDAO cannot be null");
        }
        
        Metadata md = getMetadataByKey(Metadata.DEFAULT_CENSUS_METHOD_FOR_SURVEY);

        // Find the metadata or create it.
        if(md == null) {
            md = new Metadata();
            md.setKey(Metadata.DEFAULT_CENSUS_METHOD_FOR_SURVEY);
            // default value: full public (as it is for the Atlas).
            md.setValue(Boolean.valueOf(DEFAULT_CENSUS_METHOD_PROVIDED_FOR_SURVEY).toString());
        }

        // Set the value and add it to the set.
        md.setValue(value ? Boolean.TRUE.toString() : Boolean.FALSE.toString());
        
        if (!metadataContainsKey(md)) {
            metadata.add(md);
        }
        
        return mdDAO.save(md);
    }
    
    // We can't rely on the set to detect whether the metadata item has already been added to it.
    // I assume it has something to do with the equals() implementation in PortalPersistentImpl.
    // It can lead to having a duplicate in the set which is of course, bad. I don't want to
    // override the behaviour of equals() incase something else is relying on it. Check for
    // existince in the set via metadata key!
    private boolean metadataContainsKey(Metadata md) {
        if (md == null) {
            return false;
        }

		if (md.getKey() == null) {
			throw new IllegalArgumentException("Metadata key cannot be null.");
		}

        for (Metadata m : metadata) {
            if (md.getKey().equals(m.getKey())) {
                return true;
            }
        }
        // not found. return null
        return false;
    }

    @Override
    public int compareTo(Survey o) {
        if (o == null) {
            return 1;
        }
        if (this.getId() == null) {
            return 1;
        }
        if (o.getId() == null) {
            return -1;
        }
        // it's not clear whether compareTo does null checks so leave
        // the above null checks in.
        return this.getId().compareTo(o.getId());
    }
	
}
