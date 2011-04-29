package au.com.gaiaresources.bdrs.model.theme;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

/**
 * Describes a single value (such as a color) that can be configured within
 * the theme. 
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "THEME_ELEMENT")
@AttributeOverride(name = "id", column = @Column(name = "THEME_ELEMENT_ID"))
public class ThemeElement extends PortalPersistentImpl {
    
    private String key;
    private ThemeElementType type;
    private String defaultValue;
    private String customValue;
    
    public ThemeElement(String key, ThemeElementType type, String defaultValue,
            String customValue) {
        super();
        this.key = key;
        this.type = type;
        this.defaultValue = defaultValue;
        this.customValue = customValue;
    }
    
    public ThemeElement() {
    }

    @Column(name = "KEY", nullable = false)
    @Index(name="theme_element_key_index")
    public String getKey() {
        return key;
    }
    public void setKey(String key) {
        this.key = key;
    }
    
    @Enumerated(EnumType.STRING)
    @Column(name = "TYPE", nullable = false)
    public ThemeElementType getType() {
        return type;
    }
    public void setType(ThemeElementType type) {
        this.type = type;
    }
    
    @Column(name = "DEFAULT_VALUE", nullable = false)
    public String getDefaultValue() {
        return defaultValue;
    }
    public void setDefaultValue(String defaultValue) {
        this.defaultValue = defaultValue;
    }
    
    @Column(name = "CUSTOM_VALUE", nullable = false)
    public String getCustomValue() {
        return customValue;
    }
    public void setCustomValue(String customValue) {
        this.customValue = customValue;
    }
}
