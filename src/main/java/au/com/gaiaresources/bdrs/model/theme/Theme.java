package au.com.gaiaresources.bdrs.model.theme;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

/**
 * Describes a set of images, styles and templates to be applied when
 * rendering views. 
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "THEME")
@AttributeOverride(name = "id", column = @Column(name = "THEME_ID"))
public class Theme extends PortalPersistentImpl {
    public static final String DISABLE_THEME = "disableTheme";
    public static final String THEME_DIR_RAW = "raw";
    public static final String THEME_DIR_PROCESSED = "processed";
    public static final String ASSET_DOWNLOAD_URL_TMPL = "%s/files/download.htm?className=%s&id=%d&fileName=%s/";
    public static final String ASSET_KEY = "asset";
    public static final String CONTENT_KEY = "content(\\.\\w+)+";
    public static final String DEFAULT_THEME_NAME = "Default BDRS Theme (%s)";
    //public static final String THEME_DIR_DEFAULT = "default";
    
    private String name;
    private boolean active = false;
    private boolean isDefault = false;
    /**
     * The UUID of a <code>ManagedFile</code>
     */
    private String themeFileUUID;
    private List<ThemeElement> themeElements = new ArrayList<ThemeElement>();
    
    private String[] cssFiles = new String[]{};
    private String[] jsFiles = new String[]{};

    @Column(name = "ACTIVE", nullable = false)
    public boolean isActive() {
        return active;
    }
    public void setActive(boolean active) {
        this.active = active;
    }

    @Column(name = "ISDEFAULT", nullable = false)
    public boolean isDefault() {
        return this.isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
    
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "THEME_FILE_UUID", nullable = true)
    public String getThemeFileUUID() {
        return themeFileUUID;
    }
    public void setThemeFileUUID(String themeFileUUID) {
        this.themeFileUUID = themeFileUUID;
    }

    @OneToMany
    public List<ThemeElement> getThemeElements() {
        return themeElements;
    }
    public void setThemeElements(List<ThemeElement> themeElements) {
        this.themeElements = themeElements;
    }
    
    @CollectionOfElements
    @JoinTable(name = "THEME_CSS_FILE")
    @Column(name = "CSS_FILE")
    @IndexColumn(name = "ARRAY_INDEX")
    @Fetch(FetchMode.SUBSELECT)
    public String[] getCssFiles() {
        return cssFiles;
    }
    public void setCssFiles(String[] cssFiles) {
        this.cssFiles = cssFiles;
    }
    
    @CollectionOfElements
    @JoinTable(name = "THEME_JS_FILE")
    @Column(name = "JS_FILE")
    @IndexColumn(name = "ARRAY_INDEX")
    @Fetch(FetchMode.SUBSELECT)
    public String[] getJsFiles() {
        return jsFiles;
    }
    public void setJsFiles(String[] jsFiles) {
        this.jsFiles = jsFiles;
    }
}
