package au.com.gaiaresources.bdrs.model.region;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

import com.vividsolutions.jts.geom.MultiPolygon;

/**
 * A <code>Region</code> is an area of interest.
 * @author Tim Carpenter
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "REGION")
@AttributeOverride(name = "id", column = @Column(name = "REGION_ID"))
public class Region extends PortalPersistentImpl {
    private String regionName;
    private MultiPolygon boundary;

    /**
     * {@inheritDoc}
     */
    @Column(name = "NAME")
    /**
     * Get the name of the Region.
     * @return <code>String</code>.
     */
    public String getRegionName() {
        return regionName;
    }
    public void setRegionName(String name) {
        this.regionName = name;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "BOUNDARY")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    /**
     * Get the boundary of the <code>Region</code>.
     * @return JTS <code>Polygon</code>.
     */
    public MultiPolygon getBoundary() {
        return boundary;
    }
    public void setBoundary(MultiPolygon boundary) {
        this.boundary = boundary;
    }

    public boolean equals(Object other) {
        if (other instanceof Region) {
            Region o = (Region) other;
            return o.getId().equals(getId());
        }
        return false;
    }
}
