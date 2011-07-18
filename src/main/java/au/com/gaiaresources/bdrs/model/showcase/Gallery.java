package au.com.gaiaresources.bdrs.model.showcase;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.Table;

import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "GALLERY")
@AttributeOverride(name = "id", column = @Column(name = "GALLERY_ID"))
public class Gallery extends PortalPersistentImpl {
    
    List<String> fileUUIDS = new ArrayList<String>();
    String name = "";
    String description = "";
    
    /**
     * The name of this gallery
     * {@inheritDoc}
     */
    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    
    /**
     * The description of this gallery
     * {@inheritDoc}
     */
    @Column(name = "DESCRIPTION", length=1023, nullable = false)
    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }
    
    /**
     * Get the UUIDs of the items in this gallery
     * 
     * @return <code>String[]</code>.
     */
    @CollectionOfElements
    @JoinTable(name = "GALLERY_ITEMS", joinColumns = { @JoinColumn(name = "GALLERY_ID") })
    @ForeignKey(name = "GALLERY_GALLERY_ITEMS_PK")
    @Column(name = "MANAGED_FILE_UUID")
    @IndexColumn(name = "MANAGED_FILE_ORDER")
    @Fetch(FetchMode.JOIN)
    public List<String> getFileUUIDS() {
        return fileUUIDS;
    }

    public void setFileUUIDS(List<String> list) {
        this.fileUUIDS = list;
    }
}
