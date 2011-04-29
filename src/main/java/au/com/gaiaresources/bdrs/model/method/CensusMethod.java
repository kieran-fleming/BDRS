package au.com.gaiaresources.bdrs.model.method;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
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
public class CensusMethod extends PortalPersistentImpl {
    private String name;
    private List<Attribute> attributes = new ArrayList<Attribute>();
    private boolean taxonomic;
    
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
    @Column(name = "taxonomic")
    public boolean isTaxonomic() {
        return taxonomic;
    }

    public void setTaxonomic(boolean taxonomic) {
        this.taxonomic = taxonomic;
    }

}
