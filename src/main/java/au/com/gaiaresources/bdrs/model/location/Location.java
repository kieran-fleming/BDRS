package au.com.gaiaresources.bdrs.model.location;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.*;

import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.attribute.Attributable;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.user.User;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.Point;

/**
 * A User defined location for where they are collecting information.
 * @author Tim Carpenter
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "LOCATION")
@AttributeOverride(name = "id", column = @Column(name = "LOCATION_ID"))
public class Location extends PortalPersistentImpl implements Attributable<AttributeValue> {
    private Geometry location;
    private User user;
    private String name;
    private Set<Region> regions = new HashSet<Region>();
    private Set<AttributeValue> attributes = new HashSet<AttributeValue>();
    private String description;
    
    /**
     * Get the coordinate of the <code>Location</code>.
     * @return Java Topology Suite <code>Point</code>.
     */
    @CompactAttribute
    @Column(name = "LOCATION")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    public Geometry getLocation() {
        return location;
    }
    public void setLocation(Geometry location) {
        this.location = location;
    }

    /**
     * Get the <code>User</code> that owns this <code>Location</code>.
     * @return <code>User</code>.
     */
    @CompactAttribute
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = true)
    @ForeignKey(name = "LOCATION_USER_FK")
    public User getUser() {
        return user;
    }
    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Get the name of this <code>Location</code>.
     * @return <code>String</code>.
     */
    @CompactAttribute
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Get the description of this <code>Location</code>.
     * @return <code>String</code>.
     */
    @CompactAttribute
    @Column(name = "DESCRIPTION", nullable = true)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the regions that this location is within. Most of the time
     * there will only be one. But in the case of border locations
     * there might be more than one.
     * @return <code>Set</code> of <code>Region</code>.
     */
    @ManyToMany
    @JoinTable(name = "LOCATION_REGION",
               joinColumns = { @JoinColumn(name = "LOCATION_ID") },
               inverseJoinColumns = { @JoinColumn(name = "REGION_ID") })
    @ForeignKey(name = "LOCATION_REGION_LOC_FK",
                inverseName = "LOCATION_REGION_REGION_FK")
    @Fetch(FetchMode.JOIN)
    public Set<Region> getRegions() {
        // Changing to FetchMode.JOIN as FetchMode.SUBSELECT breaks when:
        // survey A has a record in it, no survey locations or user locations used.
        // survey B has a record in it that uses a user location. Survey B also
        // has survey locations.
        // Attempting to load the table view in the advanced review screen and selecting
        // viewing records for survey A. It causes a 500 error with the following exception: 
        // org.hibernate.exception.DataException: could not load collection by subselect: [au.com.gaiaresources.bdrs.model.location.Location.regions#<22, 28>]
        // 
        // where 22 and 28 are id's of locations used in survey B.
        // 
        // This may not be the minimum test case.
        return regions;
    }
    
    public void setRegions(Set<Region> regions) {
        this.regions = regions;
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

    @Override
    public void setAttributes(Set<AttributeValue> attributes) {
        this.attributes = attributes;
    }
    
    @Transient
    public Point getPoint() {
        if (getLocation() == null) {
            return null;
        }
        return getLocation().getCentroid();
    }

    @Transient
    public Double getX() {
        if (getPoint() == null) {
            return null;
        }
        return getPoint().getX();
    }

    @Transient
    public Double getY() {
        if (getPoint() == null) {
            return null;
        }
        return getPoint().getY();
    }

    @Transient
    public Double getArea() {
        if (getLocation() == null) {
            return null;
        }
        return getLocation().getArea();
    }
    
    @Override
    @Transient
    public AttributeValue createAttribute() {
        return new AttributeValue();
    }
}
