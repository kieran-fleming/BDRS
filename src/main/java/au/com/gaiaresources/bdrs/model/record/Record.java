package au.com.gaiaresources.bdrs.model.record;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.attribute.Attributable;
import au.com.gaiaresources.bdrs.model.expert.ReviewRequest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValueUtil;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "RECORD")
@AttributeOverride(name = "id", column = @Column(name = "RECORD_ID"))
public class Record extends PortalPersistentImpl implements ReadOnlyRecord, Attributable<AttributeValue> {
    
    // no species and number seen
    public static final RecordPropertyType[] NON_TAXONOMIC_RECORD_PROPERTY_NAMES = new RecordPropertyType[] {
    	RecordPropertyType.LOCATION,
    	RecordPropertyType.POINT,
    	RecordPropertyType.ACCURACY,
    	RecordPropertyType.WHEN,
    	RecordPropertyType.TIME,
    	RecordPropertyType.NOTES};

    private Logger log = Logger.getLogger(getClass());
    
    private Survey survey;
    private IndicatorSpecies species;
    private User user;
    private Location location;
    private Geometry geometry;
    private Double AccuracyInMeters;
    private Boolean held = false;
    private RecordVisibility recordVisibility = RecordVisibility.OWNER_ONLY;

    private Date when;
    private Long time;
    private Date lastDate;
    private Long lastTime;
    private String notes;

    private Boolean firstAppearance;
    private Boolean lastAppearance;
    private String behaviour = "";
    private String habitat = "";
    private Integer number;
    private CensusMethod censusMethod;
    
    private Record parentRecord;
    private Set<Record> childRecords = new HashSet<Record>();

    private Set<AttributeValue> attributes = new HashSet<AttributeValue>();
    private Set<ReviewRequest> reviewRequests = new HashSet<ReviewRequest>();

    private Set<Metadata> metadata = new HashSet<Metadata>();

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "INDICATOR_SPECIES_ID", nullable = true)
    @ForeignKey(name = "RECORD_SPECIES_FK")
    @Index(name = "RECORD_N1")
    /**
     * Get the species that the record relates to.
     * @return {@link IndicatorSpecies}
     */
    public IndicatorSpecies getSpecies() {
        return species;
    }

    public void setSpecies(IndicatorSpecies species) {
        this.species = species;
    }
    
    @ManyToOne
    @JoinColumn(name = "PARENT_RECORD_ID", nullable = true)
    @ForeignKey(name = "PARENT_RECORD_TO_RECORD_FK")
    public Record getParentRecord() {
        return this.parentRecord;
    }
    
    public void setParentRecord(Record value) {
        this.parentRecord = value;
    }
    
    /**
     * DO NO UPDATE THIS LIST! the relationship is managed by the
     * get/setParentRecord! Any changes to the list will be ignored
     * See RecordDAOImplTest.java
     *  
     * @return
     */
    @OneToMany(mappedBy="parentRecord", fetch = FetchType.LAZY)
    public Set<Record> getChildRecords() {
        return childRecords;
    }

    /**
     * DO NO UPDATE THIS LIST! the relationship is managed by the
     * get/setParentRecord! Any changes to the list will be ignored
     * See RecordDAOImplTest.java
     * 
     * @param value
     */
    public void setChildRecords(Set<Record> value) {
        childRecords = value;
    }

    /**
     * Gets the survey that the record belongs to
     * 
     * Should this really be nullable ?
     */
    @CompactAttribute
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "INDICATOR_SURVEY_ID", nullable = true)
    @ForeignKey(name = "RECORD_SURVEY_FK")
    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }
    
    @ManyToOne(fetch =  FetchType.LAZY)
    @JoinColumn(name = "INDICATOR_CENSUSMETHOD_ID", nullable = true)
    @ForeignKey(name = "RECORD_CENSUSMETHOD_FK")
    public CensusMethod getCensusMethod() {
        return this.censusMethod;
    }
    
    public void setCensusMethod(CensusMethod value) {
        this.censusMethod = value;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "INDICATOR_USER_ID", nullable = false)
    @ForeignKey(name = "RECORD_USER_FK")
    /**
     * Get the user that owns the record.
     * @return {@link User}
     */
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }


    /**
     * Get the location that the user saw the species.
     * @return {@link Location}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "LOCATION_ID")
    @ForeignKey(name = "RECORD_LOCATION_FK")
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    
    @Transient
    public Point getPoint() {
        return geometry == null ? null : geometry.getCentroid();
    }

    public void setPoint(Point location) {
        this.geometry = location;
    }
    
    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "GEOM")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    /**
     * Get the Point coordinates that the user saw the species.
     * @return {@link Location}
     */
    public Geometry getGeometry() {
        return geometry;
    }

    public void setGeometry(Geometry value) {
        this.geometry = value;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "ACCURACY")
    /**
     * Get the Point coordinates that the user saw the species.
     * @return {@link Location}
     */
    public Double getAccuracyInMeters() {
        return AccuracyInMeters;
    }

    public void setAccuracyInMeters(Double accuracy) {
        this.AccuracyInMeters = accuracy;
    }
    
    @CompactAttribute
    @Column(name = "HELD", nullable = false)
    /**
     * Return whether the record is held or not.
     */
    public Boolean isHeld() {
        return held;
    }

    public void setHeld(boolean held) {
        this.held = held;
    }
    @CompactAttribute
    @Transient
    public Boolean getHeld(){
        return isHeld();
    }

    @CompactAttribute
    @Column(name = "WHEN_DATE")
    /**
     * Get the date that the sighting occured.
     * @return {@link Date}
     */
    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
        if (when == null) {
        	this.time = null;
        } else {
        	this.time = when.getTime();
        }
    }

    @CompactAttribute
    @Column(name = "TIME")
    /**
     * Get the time that the sighting occured. Can be null. Stored as a long
     * so as to avoid time zone issues. Stores the number of milliseconds passed midnight
     * on 1/1/70 GMT.
     * @return {@link Long}
     */
    public Long getTime() {
        return time;
    }

    public void setTime(Long time) {
        this.time = time;
        if (time == null) {
        	this.when = null;
        } else {
        	this.when = new Date(time);
        }
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "LAST_DATE")
    /**
     * Get the date that the sighting occured.
     * @return {@link Date}
     */
    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date when) {
        this.lastDate = when;
        if (when == null) {
        	this.lastTime = null;
        } else {
        	this.lastTime = when.getTime();
        }
    }

    @CompactAttribute
    @Column(name = "LAST_TIME")
    /**
     * Get the time that the sighting occured. Can be null. Stored as a long
     * so as to avoid time zone issues. Stores the number of milliseconds passed midnight
     * on 1/1/70 GMT.
     * @return {@link Long}
     */
    public Long getLastTime() {
        return lastTime;
    }

    public void setLastTime(Long time) {
        this.lastTime = time;
        if  (time == null) {
        	this.lastDate = null;
        } else {
        	this.lastDate = new Date(time);
        }
        
    }

    @Transient
    /**
     * Get the time value as a date object.
     * @see Record#getTime()
     * @return {@link Date}
     */
    public Date getTimeAsDate() {
        if(time == null) {
            return null;
        }
        return convertTimeToDate(time);
    }

    @Transient
    /**
     * Get the time value as a date object.
     * @see Record#getTime()
     * @return {@link Date}
     */
    public Date getLastTimeAsDate() {
        if(lastTime == null) {
            return null;
        }
        return convertTimeToDate(lastTime);
    }

    private Date convertTimeToDate(long time) {
        if (time > 0) {
            Calendar c = Calendar.getInstance();
            c.setTimeZone(TimeZone.getTimeZone("GMT"));
            c.setTimeInMillis(time);
            return c.getTime();
        }
        return null;
    }

    /**
     * Get the set of attributes that were recorded for the species.
     * @return {@link Set} of {@link RecordAttribute}
     */
    @CompactAttribute
    @OneToMany
    @Override
    public Set<AttributeValue> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<AttributeValue> attributes) {
        this.attributes = attributes;
    }

    /**
     * Any notes that the user might have about the sighting.
     * @return {@link String}
     */
    @CompactAttribute
    @Column(name = "NOTES")
    @Type(type = "text")
    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }

    @Column(name = "FIRST_APPEARANCE")
    /**
     * Was this the first appearance of the season.
     * @return {@link Boolean}
     */
    public Boolean isFirstAppearance() {
        return firstAppearance;
    }

    public void setFirstAppearance(Boolean firstAppearance) {
        this.firstAppearance = firstAppearance;
    }

    @Column(name = "LAST_APPEARANCE")
    /**
     * Was this the last appearance of the season.
     * @return {@link Boolean}
     */
    public Boolean isLastAppearance() {
        return lastAppearance;
    }

    public void setLastAppearance(Boolean lastAppearance) {
        this.lastAppearance = lastAppearance;
    }

    @Column(name = "BEHAVIOUR")
    @Type(type = "text")
    /**
     * What behaviour was observed.
     * @return {@link String}
     */
    public String getBehaviour() {
        return behaviour;
    }

    public void setBehaviour(String behaviour) {
        this.behaviour = behaviour;
    }

    @Column(name = "HABITAT")
    @Type(type = "text")
    /**
     * What was the habitat like.
     * @return {@link String}
     */
    public String getHabitat() {
        return habitat;
    }

    public void setHabitat(String habitat) {
        this.habitat = habitat;
    }

    @CompactAttribute
    @Column(name = "NUMBER_SEEN", nullable=true)
    /**
     * How many were seen?
     * @return {@link Integer}
     */
    public Integer getNumber() {
        return number;
    }

    public void setNumber(Integer number) {
        this.number = number;
    }

    @OneToMany(mappedBy = "record")
    /**
     * Get the review requests.
     * @return {@link Set} of {@link ReviewRequest}
     */
    public Set<ReviewRequest> getReviewRequests() {
        return reviewRequests;
    }

    public void setReviewRequests(Set<ReviewRequest> reviewRequests) {
        this.reviewRequests = reviewRequests;
    }

    /**
     * The visibility level of the record. Defaults to 'owner only' 
     * 
     * @return
     */
    @CompactAttribute
    @Enumerated(EnumType.STRING)
    @Column(name = "record_visibility")
    public RecordVisibility getRecordVisibility() {
        return recordVisibility;
    }

    public void setRecordVisibility(RecordVisibility value) {
        this.recordVisibility = value;
    }

    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the metadata id.
    @ManyToMany
    public Set<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        this.metadata = metadata;
    }
    @Transient
    public String getMetadataValue(String key) {
        if(key == null) {
            return "";
        }

        for(Metadata md : this.getMetadata()) {
            if(md.getKey().equals(key)) {
                return md.getValue();
            }
        }

     return "";
    }
    @Transient
    public Double getLatitude() {
    	Location loc = this.getLocation();
        if(this.getPoint() != null) {
            return this.getPoint().getY();
        }
        else if(loc != null && loc.getLocation() != null){
            return loc.getLocation().getCentroid().getY();
        }
        else {
            return null;
        }
    }

    @Transient
    public Double getLongitude() {
    	Location loc = this.getLocation();
        if(this.getPoint() != null) {
            return this.getPoint().getX();
        }
        else if(loc != null && loc.getLocation() != null){
            return loc.getLocation().getCentroid().getX();
        }
        else {
            return null;
        }
    }
    @Transient
    /**
     * If the record has a Metadata value for Metadata.RECORD_NOT_DUPLICATE and this is true, returns true,
     * otherwise false 
     */
    public boolean isNotDuplicate(){
        return Boolean.parseBoolean(this.getMetadataValue(Metadata.RECORD_NOT_DUPLICATE));
    }
    
        /**
     * Returns a list of the AttributeValues ordered by Attribute weight
     * @return
     */
    @Transient
    public List<AttributeValue> getOrderedAttributes() {
        return AttributeValueUtil.orderAttributeValues(attributes);
    }

    /**
     * Whether or not the details of this record should be hidden when outputing to json,
     * or any other means.
     * 
     * @param accessor
     * @return
     */
    @Transient
    public boolean hideDetails(User accessor) {
        boolean isPublic = this.getRecordVisibility() == RecordVisibility.PUBLIC;
        if (accessor == null) {
            // ignore accessing user and show only public records that aren't held
            return !isPublic || isHeld();
        }
        if (accessor.getId() == null) {
            log.warn("Attempting to access record with a non null user with a null id. This _probably_ should not happen");
            // ignore accessing user and show only public records that aren't held
            return !isPublic || isHeld();
        }
        if (this.getUser() == null || this.getUser().getId() == null) {
            // record is not yet properly created (user field cannot be null in database).
            log.warn("Attempting to determine whether a record should have hidden details but the record has no owner");
            return false;
        }

        boolean isOwner = accessor.getId().intValue() == this.getUser().getId().intValue();
        // if you aren't the owner or admin and the record isn't public or is held, hide the details
        if (isOwner || accessor.isAdmin()) {
            // the owner and the admin can always see their records
            return false;
        }
        // everyone besided the owner and admin can only see public and unheld records
        return !isPublic || isHeld();
    }
    
    /**
     * Whether or not the user attempting to write to this record actually has
     * write access
     * 
     * @param writer - the user attempting to write to this record
     * @return true if writing allowed, false otherwise
     */
    @Transient
    public boolean canWrite(User writer) {
        
        if (writer == null) {
            // we can't write a record with no writer!
            return false;
        }
        if (writer.getId() == null) {
            log.warn("Attempting to write to record with a non null user with a null id. This _probably_ should not happen");
            // user does not exist in database - cannot write.
            return false;
        }
        if (this.getId() == null) {
            // this is a new record. anyone should be able to write to it.
            return true;
        }
        // at this point we know the record already exists in the database (non null record id).
        
        if (this.getUser() == null || this.getUser().getId() == null) {
            // record is not yet properly created (user field cannot be null in database).
            log.warn("Attempting to determine whether a record should have hidden details but the record has no owner");
            return false;
        }
        boolean isOwner = writer.getId().intValue() == this.getUser().getId().intValue();
        return isOwner || Role.isRoleHigherThanOrEqualTo(Role.getHighestRole(writer.getRoles()), Role.SUPERVISOR);
    }
    
    /**
     * Contains the logic for if a user can view (not edit) this record
     * 
     * @param viewer - the user attempting to view this record. can be null (i.e. not logged in)
     * @return true if viewing allowed, false otherwise
     */
    @Transient
    public boolean canView(User viewer) {

        if (viewer != null && viewer.getId() == null) {
            throw new IllegalStateException("viewer does not have an id - the user object is not persisted");
        }
        if (this.getId() == null) {
            // we probably should not be attempting to view a non persisted record. throw an exception
            throw new IllegalStateException("Cannot view non persisted record");
        }
        // at this point we know the record already exists in the database (non null record id).
        if (this.getUser() == null || this.getUser().getId() == null) {
            throw new IllegalStateException("The owner of the record is invalid");
        }
        
        boolean hasPrivilege = viewer != null ? Role.isRoleHigherThanOrEqualTo(Role.getHighestRole(viewer.getRoles()), Role.SUPERVISOR) : false;
        boolean isOwner = viewer != null ? viewer.getId().equals(this.getUser().getId()) : false;
        
        switch (this.recordVisibility) {
        // only the owner or admin can view an OWNER_ONLY record
        case OWNER_ONLY:
        // CONTROLLED records are a bit strange. alot of the information is hidden so rendering
        // forms could be a little tricky. For now handle controlled records the same as
        // owner only records
        case CONTROLLED:
            return isOwner || hasPrivilege;
        // anyone can view a public record
        case PUBLIC:
            return true;
            
            default:
                throw new IllegalStateException("record visibility type not handled : " + this.recordVisibility);
        }
    }

    @Override
    @Transient
    public AttributeValue createAttribute() {
        return new AttributeValue();
    }
}
