package au.com.gaiaresources.bdrs.model.theme;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

/**
 * Customisations for a normally unthemable page e.g. 'My Sightings'
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "THEME_PAGE")
@AttributeOverride(name = "id", column = @Column(name = "theme_page_id"))
public class ThemePage extends PortalPersistentImpl {
    
    private String key;
    private String title = null;
    private String description = null;
    private Theme theme;
    
    public static final int MAX_TITLE_LENGTH = 255;
    public static final int MAX_DESCRIPTION_LENGTH = 2047;

    /**
     * The key for the page. Undecided whether to use the 'view' name or the url at the moment.
     * Will probably go for the view name actually, less parsing to worry about.
     * 
     * @return
     */
    @Column(name = "KEY", nullable=false)
    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }
    
    /**
     * The title to display at the top of the page
     * @return
     */
    @Column(name = "TITLE", nullable=true, length=MAX_TITLE_LENGTH)
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }
    
    /**
     * The description which will go right underneth the title.
     * 2k characters should be enough for most apps
     * @return
     */
    @Column(name = "DESCRIPTION", nullable=true, length=MAX_DESCRIPTION_LENGTH)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * The theme that this page customisation is associated with
     * @return
     */
    @ManyToOne
    @JoinColumn(name = "THEME_ID", nullable = false)
    @ForeignKey(name = "THEME_THEME_PAGE_FK")
    public Theme getTheme() {
        return theme;
    }
    
    public void setTheme(Theme value) {
        this.theme = value;
    }
}
