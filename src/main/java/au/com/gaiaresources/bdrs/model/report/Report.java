package au.com.gaiaresources.bdrs.model.report;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Lob;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

/**
 * Describes a readonly view of the data in the system.
 */
@Entity
@FilterDef(name = PortalPersistentImpl.PORTAL_FILTER_NAME, parameters = @ParamDef(name = "portalId", type = "integer"))
@Filter(name = PortalPersistentImpl.PORTAL_FILTER_NAME, condition = ":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "REPORT")
@AttributeOverride(name = "id", column = @Column(name = "REPORT_ID"))
public class Report extends PortalPersistentImpl {
    /**
     * The target directory that will contain the report after extraction.
     */
    public static final String REPORT_DIR = "report";
    
    private String name;
    private String description;
    private String iconFilename;
    private boolean active = true;
    
    /**
     * Creates a new blank (and invalid) report.
     */
    public Report() {
    }
    
    /**
     * Creates a new report.
     * 
     * @param name the name of the new report.
     * @param description a short description of the report.
     * @param iconFilename the relative path to the report icon.
     * @param active true if this report is active, false otherwise.
     */
    public Report(String name, String description, String iconFilename,
            boolean active) {
        super();
        this.name = name;
        this.description = description;
        this.iconFilename = iconFilename;
        this.active = active;
    }

    /**
     * @return the name
     */
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    /**
     * @param name
     *            the name to set
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return the description
     */
    @Column(name = "DESCRIPTION", nullable = false)
    @Lob
    public String getDescription() {
        return description;
    }

    /**
     * @param description
     *            the description to set
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * @return the active
     */
    @Column(name = "ACTIVE", nullable = false)
    public boolean isActive() {
        return active;
    }

    /**
     * @param active
     *            the active to set
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * @return the iconFilename
     */
    @Column(name = "ICONFILENAME", nullable = false)
    public String getIconFilename() {
        return iconFilename;
    }

    /**
     * @param iconFilename the iconFilename to set
     */
    public void setIconFilename(String iconFilename) {
        this.iconFilename = iconFilename;
    }
}
