package au.com.gaiaresources.bdrs.model.file;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.commons.lang.StringEscapeUtils;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Type;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.file.FileService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;

/**
 * The ManagedFile represents a user uploaded file. Managed files can be 
 * refered to by other persistent objects such as {@code SpeciesProfile}
 * using its UUID. 
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "MANAGEDFILE")
@AttributeOverride(name = "id", column = @Column(name = "MANAGED_FILE_ID"))
public class ManagedFile extends PortalPersistentImpl {

    private String uuid = UUID.randomUUID().toString();
    
    private String filename;
    private String description;
    private String contentType;
    
    private String credit;
    private String license;
    
    private Set<Metadata> metadata = new HashSet<Metadata>();

    /**
     * The universally unique identifier (uuid) for this file. The uuid is used
     * to identify this managed file by other persistent objects.
     * @return the unique identifier that can be used to refer to this file
     * by other persistent objects.
     */
    @Column(name = "UUID", nullable=false, unique=true)
    @Type(type = "text")
    @Index(name="managedfile_uuid_index")
    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * The name of the file being managed.
     * @return the name of the file being managed.
     */
    @Column(name = "FILENAME", nullable=false)
    @Type(type = "text")
    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    /**
     * The description of the content of the file being managed.
     * @return a description of the content of the file being managed.
     */
    @Column(name = "DESCRIPTION", nullable=false)
    @Type(type = "text")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The content type (MIME type) of the file being managed.
     * @return the content type of the file being managed.
     */
    @Column(name = "CONTENTTYPE", nullable=false)
    @Type(type = "text")
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    /**
     * Acknoweledgements to the creators of the file being managed.
     * @return acknowledgements to the creators of the file being managed.
     */
    @Column(name = "CREDIT", nullable=false)
    @Type(type = "text")
    public String getCredit() {
        return credit;
    }

    public void setCredit(String credit) {
        this.credit = credit;
    }

    /**
     * License requirements or license type of the file being managed.
     * @return license requirements or license type of the file being managed.
     */
    @Column(name = "LICENSE", nullable=false)
    @Type(type = "text")
    public String getLicense() {
        return license;
    }

    public void setLicense(String license) {
        this.license = license;
    }

    /**
     * Additional information about the file being managed.
     * @return
     */
    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the metadata id.
    @ManyToMany
    public Set<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        this.metadata = metadata;
    }
    
    @Transient
    public String getFileURL() {
        try {
            return String.format(FileService.FILE_URL_TMPL, URLEncoder.encode(getClass()
                    .getCanonicalName(), "UTF-8"), getId(), URLEncoder.encode(
                    getFilename(), "UTF-8"));
        } catch (UnsupportedEncodingException e) {
            return String.format(FileService.FILE_URL_TMPL, StringEscapeUtils
                    .escapeHtml(getClass().getCanonicalName()), getId(),
                    StringEscapeUtils.escapeHtml(getFilename()));
        }
    }
}
