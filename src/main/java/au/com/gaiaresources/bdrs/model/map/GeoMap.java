package au.com.gaiaresources.bdrs.model.map;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "GEO_MAP")
@AttributeOverride(name = "id", column = @Column(name = "GEO_MAP_ID"))
public class GeoMap extends PortalPersistentImpl {
    
    String name = "";
    String description = "";
    boolean hidePrivateDetails = true; // if any...
    // Will probably do this using hierarchical roles.
    String roleRequired = ""; // can be null / empty string
    boolean publish = false;
    boolean anonymousAccess = false;
    
    /**
     * The name of this map
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
     * The description of this map
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
     * whether to hide private details on this map. Mainly user specific stuff.
     * Can increase the granularity of the privacy later if required
     * {@inheritDoc}
     */
    @Column(name = "HIDE_PRIVATE_DETAILS", nullable = false)
    public boolean isHidePrivateDetails() {
        return hidePrivateDetails;
    }
    public void setHidePrivateDetails(boolean hidePrivateDetails) {
        this.hidePrivateDetails = hidePrivateDetails;
    }
    
    /**
     * The minimum role level required to view this map. relies on 
     * hierarchical roles.
     * {@inheritDoc}
     */
    @Column(name = "ROLE_REQUIRED", nullable = false)
    public String getRoleRequired() {
        return roleRequired;
    }
    public void setRoleRequired(String roleRequired) {
        this.roleRequired = roleRequired;
    }
    
    /**
     * whether this map is published or not
     * {@inheritDoc}
     */
    @Column(name = "PUBLISH", nullable = false)
    public boolean isPublish() {
        return publish;
    }
    public void setPublish(boolean publish) {
        this.publish = publish;
    }    
    
    /**
     * Indicates whether the map can be seen without logging in
     */
    @Column(name = "ANONYMOUS_ACCESS", nullable = false) 
    public boolean isAnonymousAccess() {
        return this.anonymousAccess;
    }
    public void setAnonymousAccess(boolean value) {
        this.anonymousAccess = value;
    }
}
