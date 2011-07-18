package au.com.gaiaresources.bdrs.model.map;

import java.util.LinkedHashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

import com.vividsolutions.jts.geom.Geometry;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "GEO_MAP_FEATURE")
@AttributeOverride(name = "id", column = @Column(name = "GEO_MAP_FEATURE_ID"))
public class GeoMapFeature extends PortalPersistentImpl {
    
    private Geometry geometry;
    private Set<AttributeValue> attributes = new LinkedHashSet<AttributeValue>();
    private GeoMapLayer layer;
    
    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "GEOM")
    @Type(type = "org.hibernatespatial.GeometryUserType")
    /**
     * Get the geometry object for this map feature
     * @return {@link Location}
     */
    public Geometry getGeometry() {
        return geometry;
    }
    public void setGeometry(Geometry geometry) {
        this.geometry = geometry;
    }
    
    @OneToMany
    @JoinTable(name="GEO_MAP_FEATURE_ATTRIBUTE_VALUE")
    /**
     * Get the set of attributes that were recorded for the species.
     * @return {@link Set} of {@link RecordAttribute}
     */
    public Set<AttributeValue> getAttributes() {
        return attributes;
    }
    
    public void setAttributes(Set<AttributeValue> attributes) {
        this.attributes = attributes;
    }
    
    @ManyToOne
    @JoinColumn(name = "GEO_MAP_LAYER_ID", nullable = false)
    @ForeignKey(name = "GEO_MAP_FEATURE_TO_GEO_MAP_LAYER_FK")
    public GeoMapLayer getLayer() {
        return layer;
    }
    public void setLayer(GeoMapLayer layer) {
        this.layer = layer;
    }
}
