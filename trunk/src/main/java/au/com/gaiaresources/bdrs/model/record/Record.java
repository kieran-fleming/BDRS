package au.com.gaiaresources.bdrs.model.record;

import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;
import java.util.TimeZone;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.expert.ReviewRequest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.user.User;

import com.vividsolutions.jts.geom.Point;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "RECORD")
@AttributeOverride(name = "id", column = @Column(name = "RECORD_ID"))
public class Record extends PortalPersistentImpl {
    
    public static final String RECORD_PROPERTY_SPECIES = "species";
    public static final String RECORD_PROPERTY_LOCATION = "location";
    public static final String RECORD_PROPERTY_POINT = "point";
    public static final String RECORD_PROPERTY_WHEN = "when";
    public static final String RECORD_PROPERTY_TIME = "time";
    public static final String RECORD_PROPERTY_NOTES = "notes";
    public static final String RECORD_PROPERTY_NUMBER = "number";

    public static final String[] RECORD_PROPERTY_NAMES = new String[] {
            RECORD_PROPERTY_SPECIES, RECORD_PROPERTY_LOCATION,
            RECORD_PROPERTY_POINT, RECORD_PROPERTY_WHEN, RECORD_PROPERTY_TIME,
            RECORD_PROPERTY_NOTES, RECORD_PROPERTY_NUMBER };
    
    private Survey survey;
    private IndicatorSpecies species;
    private User user;
    private Location location;
    private Point point;
    private Boolean held;
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

    private Set<RecordAttribute> attributes = new HashSet<RecordAttribute>();
    private Set<ReviewRequest> reviewRequests = new HashSet<ReviewRequest>();

    private Set<Metadata> metadata = new HashSet<Metadata>();

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "INDICATOR_SPECIES_ID", nullable = false)
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
     * {@inheritDoc}
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "LOCATION_ID")
    @ForeignKey(name = "RECORD_LOCATION_FK")
    /**
     * Get the location that the user saw the species.
     * @return {@link Location}
     */
    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "POINT")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    /**
     * Get the Point coordinates that the user saw the species.
     * @return {@link Location}
     */
    public Point getPoint() {
        return point;
    }

    public void setPoint(Point location) {
        this.point = location;
    }

    @CompactAttribute
    @Column(name = "HELD")
    /**
     * Return whether the record is held or not.
     */
    public Boolean isHeld() {
        return held;
    }


    public void setHeld(Boolean held) {
        this.held = held;
    }
    @CompactAttribute
    @Transient
    public Boolean getHeld(){
        return held;
    }

    @CompactAttribute
    @Column(name = "WHEN_DATE", nullable = false)
    /**
     * Get the date that the sighting occured.
     * @return {@link Date}
     */
    public Date getWhen() {
        return when;
    }

    public void setWhen(Date when) {
        this.when = when;
        this.time = when.getTime();
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
        this.when = new Date(time);
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "LAST_DATE", nullable = false)
    /**
     * Get the date that the sighting occured.
     * @return {@link Date}
     */
    public Date getLastDate() {
        return lastDate;
    }

    public void setLastDate(Date when) {
        this.lastDate = when;
        this.lastTime = when.getTime();
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
        this.lastDate = new Date(time);
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


    @CompactAttribute
    @OneToMany
    /**
     * Get the set of attributes that were recorded for the species.
     * @return {@link Set} of {@link RecordAttribute}
     */
    public Set<RecordAttribute> getAttributes() {
        return attributes;
    }

    public void setAttributes(Set<RecordAttribute> attributes) {
        this.attributes = attributes;
    }

    @CompactAttribute
    @Column(name = "NOTES")
    @Type(type = "text")
    /**
     * Any notes that the user might have about the sighting.
     * @return {@link String}
     */
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
    @Column(name = "NUMBER_SEEN")
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
            return new String();
        }

        for(Metadata md : this.getMetadata()) {
            if(md.getKey().equals(key)) {
                return md.getValue();
            }
        }

     return new String();
    }
    @Transient
    public Double getLatitude() {
        if(this.location != null && this.location.getLocation() != null){
            return this.location.getLocation().getY();
        }
        else if(this.point != null) {
            return this.point.getY();
        }
        else {
            return null;
        }
    }

    @Transient
    public Double getLongitude() {
        if(this.location != null && this.location.getLocation() != null){
            return this.location.getLocation().getX();
        }
        else if(this.point != null) {
            return this.point.getX();
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
}
