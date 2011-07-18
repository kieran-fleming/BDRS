package au.com.gaiaresources.bdrs.model.survey;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
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
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.util.DateUtils;
import au.com.gaiaresources.bdrs.util.StringUtils;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "SURVEY")
@AttributeOverride(name = "id", column = @Column(name = "SURVEY_ID"))
public class Survey extends PortalPersistentImpl {
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
    @Column(name = "DESCRIPTION")
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
        if(metadataLookup == null) {
            metadataLookup = new HashMap<String, Metadata>(metadata.size());
            for(Metadata md : metadata) {
                metadataLookup.put(md.getKey(), md);
            }    
        }
        
        return metadataLookup.get(key);
    }
}
