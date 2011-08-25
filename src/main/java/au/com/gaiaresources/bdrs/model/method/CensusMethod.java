package au.com.gaiaresources.bdrs.model.method;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.MetadataPortalPersistentImpl;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;

/**
 * A CensusMethod, which is essentially a schema for attributes that can be applied to a record.
 * @author 
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "CENSUS_METHOD")
@AttributeOverride(name = "id", column = @Column(name = "CENSUS_METHOD_ID"))
public class CensusMethod extends MetadataPortalPersistentImpl implements Comparable<CensusMethod> {
    private String name;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private Taxonomic taxonomic;
    private List<CensusMethod> censusMethods = new ArrayList<CensusMethod>();
    private String type;
    private String description;
    
    private static final boolean DEFAULT_DRAW_POINT_ENABLED = true;
    private static final boolean DEFAULT_DRAW_LINE_ENABLED = false;
    private static final boolean DEFAULT_DRAW_POLYGON_ENABLED = false;
    
    /**
     * Get the name of this <code>Location</code>.
     * @return <code>String</code>.
     */
    @CompactAttribute
    @Column(name = "name", nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
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
    
    @CompactAttribute
    @Enumerated(EnumType.STRING)
    @Column(name = "taxonomic")
    public Taxonomic getTaxonomic() {
        return taxonomic;
    }

    public void setTaxonomic(Taxonomic taxonomic) {
        this.taxonomic = taxonomic;
    }
    
    @CompactAttribute
    @Column(name = "type")
    public String getType() {
        return type;
    }
    
    public void setType(String value) {
        this.type = value;
    }
    
    @CompactAttribute
    @Column(name = "description", length=1023)
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String value) {
        this.description = value;
    }
    
    @CompactAttribute
    @ManyToMany
    @IndexColumn(name = "pos")
    public List<CensusMethod> getCensusMethods() {
        return this.censusMethods;
    }
    public void setCensusMethods(List<CensusMethod> cmList) {
        this.censusMethods = cmList;
    }
        
    /**
     * Can we enter point data for this census method?
     * @return
     */
    @Transient
    public boolean isDrawPointEnabled() {
        return getBooleanMetadata(Metadata.CENSUS_METHOD_POINT, DEFAULT_DRAW_POINT_ENABLED);
    }
    
    @Transient
    public void setDrawPointEnabled(boolean value, MetadataDAO mdDAO) {
        setBooleanMetadata(Metadata.CENSUS_METHOD_POINT, value, mdDAO);
    }
    
    /**
     * Can we enter line data for this census method?
     * @return
     */
    @Transient
    public boolean isDrawLineEnabled() {
        return getBooleanMetadata(Metadata.CENSUS_METHOD_LINE, DEFAULT_DRAW_LINE_ENABLED);
    }
    
    @Transient
    public void setDrawLineEnabled(boolean value, MetadataDAO mdDAO) {
        setBooleanMetadata(Metadata.CENSUS_METHOD_LINE, value, mdDAO);
    }
    
    /**
     * Can we enter polygon data for this census method?
     * @return
     */
    @Transient
    public boolean isDrawPolygonEnabled() {
        return getBooleanMetadata(Metadata.CENSUS_METHOD_POLYGON, DEFAULT_DRAW_POLYGON_ENABLED);
    }
    
    @Transient
    public void setDrawPolygonEnabled(boolean value, MetadataDAO mdDAO) {
        setBooleanMetadata(Metadata.CENSUS_METHOD_POLYGON, value, mdDAO);
    }
    
    @Override
    public int compareTo(CensusMethod o) {
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
