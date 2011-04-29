package au.com.gaiaresources.bdrs.model.taxa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.util.StringUtils;

/**
 * Defines an attribute for in a taxon group.
 * @author Tim Carpenter
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "ATTRIBUTE")
@AttributeOverride(name = "id", column = @Column(name = "ATTRIBUTE_ID"))
public class Attribute extends PortalPersistentImpl {
    private String typeCode;
    private boolean required;
    private String name;
    private String description;
    private boolean tag = false;

    private List<AttributeOption> options = new ArrayList<AttributeOption>();
    private AttributeScope scope;
    
    /**
     * {@inheritDoc}
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
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "DESCRIPTION", nullable = true, columnDefinition="TEXT")
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
    @Column(name = "REQUIRED", nullable = false)
    public boolean isRequired() {
        return required;
    }
    public void setRequired(boolean required) {
        this.required = required;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "TYPE_CODE", nullable = false)
    public String getTypeCode() {
        return typeCode;
    }
    public void setTypeCode(String typeCode) {
        this.typeCode = typeCode;
    }

    @Transient
    public String getPropertyName() {
        return StringUtils.removeNonAlphaNumerics(getName()).toLowerCase();
    }

    @Transient
    public AttributeType getType() {
        return AttributeType.find(getTypeCode(), AttributeType.values());
    }

    @CompactAttribute
    @CollectionOfElements(fetch = FetchType.LAZY)
    @JoinColumn(name = "ATTRIBUTE_ID")
    @IndexColumn(name = "pos")
    public List<AttributeOption> getOptions() {
        return options;
    }
    public void setOptions(List<AttributeOption> options) {
        this.options = options;
    }

    /**
     * {@inheritDoc}
     */
    @CompactAttribute
    @Column(name = "TAG", nullable = false)
    public boolean isTag() {
        return tag;
    }
    public void setTag(boolean tag) {
        this.tag = tag;
    }
    
    @CompactAttribute
    @Enumerated(EnumType.STRING)
    @Column(name = "SCOPE", nullable=true)
    public AttributeScope getScope() {
        return this.scope;
    }
    public void setScope(AttributeScope scope) {
        this.scope = scope;
    }
}
