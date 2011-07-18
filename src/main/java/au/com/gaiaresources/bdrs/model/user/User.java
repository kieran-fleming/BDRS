package au.com.gaiaresources.bdrs.model.user;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.CollectionOfElements;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.IndexColumn;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.annotation.Sensitive;
import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * A <code>User</code> can signon and access secure areas of the application.
 * 
 * @author Tim Carpenter
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "USER_DEFINITION")
@AttributeOverride(name = "id", column = @Column(name = "USER_DEFINITION_ID"))
public class User extends PortalPersistentImpl implements Comparable<User> {

    Logger log = Logger.getLogger(User.class);
    /**
    *
    */
    private static final long serialVersionUID = 1L;
    private String emailAddress;
    private String firstName;
    private String lastName;
    private String password;
    private String name;
    private String[] roles;
    private Set<Metadata> metadata = new HashSet<Metadata>();
    private Boolean active;
    private String registrationKey;

    /**
     * Get the users e-mail address.
     * 
     * @return <code>String</code>.
     */
    @CompactAttribute
    @Column(name = "EMAIL_ADDRESS", nullable = false)
    public String getEmailAddress() {
        return emailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        this.emailAddress = emailAddress;
    }

    /**
     * Get the users first name.
     * 
     * @return <code>String</code>.
     */
    @CompactAttribute
    @Column(name = "FIRST_NAME", nullable = false)
    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    /**
     * Get the users last name.
     * 
     * @return <code>String</code>.
     */
    @CompactAttribute
    @Column(name = "LAST_NAME", nullable = false)
    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    /**
     * Get the password for the user.
     * 
     * @return <code>String</code>.
     */
    @Sensitive
    @Column(name = "PASSWORD", nullable = false)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Get the name that the <code>User</code> users to sign on with.
     * 
     * @return <code>String</code>.
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
     * Is this user active.
     * 
     * @return <code>Boolean</code>.
     */
    @Column(name = "ACTIVE", nullable = false)
    public Boolean isActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    /**
     * Get the registration key for this user.
     * 
     * @return <code>String</code>.
     */
    @Sensitive
    @Column(name = "REGISTRATION_KEY", nullable = false)
    public String getRegistrationKey() {
        return registrationKey;
    }

    public void setRegistrationKey(String registrationKey) {
        this.registrationKey = registrationKey;
    }

    /**
     * Get the roles that this user has.
     * 
     * @return <code>String[]</code>.
     */
    @CollectionOfElements
    @JoinTable(name = "USER_ROLE", joinColumns = { @JoinColumn(name = "USER_DEFINITION_ID") })
    @ForeignKey(name = "USER_ROLE_USER_FK")
    @Column(name = "ROLE_NAME")
    @IndexColumn(name = "ROLE_ORDER")
    @Fetch(FetchMode.SUBSELECT)
    public String[] getRoles() {
        return roles;
    }

    public void setRoles(String[] roles) {
        this.roles = roles;
    }

    // Many to many is a work around (read hack) to prevent a unique
    // constraint being applied on the metadata id.
    @ManyToMany(fetch = FetchType.LAZY)
    public Set<Metadata> getMetadata() {
        return metadata;
    }

    public void setMetadata(Set<Metadata> metadata) {
        this.metadata = metadata;
    }

    @Transient
    public String getMetadataValue(String key) {
        if (key == null) {
            return "";
        }

        for (Metadata md : this.getMetadata()) {
            if (md.getKey().equals(key)) {
                return md.getValue();
            }
        }

        return "";
    }

    // because it's private it doesn't need the transient annotation.. ?
    @Transient
    public Metadata getMetadataObj(String key) {
        for (Metadata md : this.getMetadata()) {
            if (md.getKey().equals(key)) {
                return md;
            }
        }
        return null;
    }

    @Transient
    public Boolean metadataExists(String key) {
        return this.getMetadataObj(key) != null;
    }

    @Transient
    public void setMetadataValue(String key, String value) {
        if (key == null) {
            return;
        }
        Metadata md = metadataExists(key) ? getMetadataObj(key)
                : new Metadata();
        md.setKey(key);
        md.setValue(value);
    }
    
    @Transient 
    public void addMetaDataObj(Metadata md) {
        this.metadata.add(md);
    }
    
    @Transient
    @Sensitive
    public boolean isUser() {
        return hasRole(Role.USER);
    }

    @Transient
    @Sensitive
    public boolean isStudent() {
        return hasRole(Role.USER);
    }

    @Transient
    @Sensitive
    public boolean isPowerStudent() {
        return hasRole(Role.POWERUSER);
    }

    @Transient
    @Sensitive
    public boolean isTeacher() {
        return hasRole(Role.SUPERVISOR);
    }

    @Transient
    @Sensitive
    public boolean isAdmin() {
        return hasRole(Role.ADMIN);
    }
    
    @Transient
    @Sensitive
    public boolean isRoot() {
        return hasRole(Role.ROOT);
    }

    @Transient
    @Sensitive
    private boolean hasRole(String role) {
        if (role == null) {
            return false;
        }

        String[] roles = this.getRoles();
        if (roles == null || (roles != null && roles.length == 0)) {
            return false;
        } else {
            // Is it quicker to sort and binary search or to iterate?
            for (String r : roles) {
                if (role.equals(r)) {
                    return true;
                }
            }
            return false;
        }
    }

    // Just so we can put it into a sorted set
    @Override
    public int compareTo(User o) {
        return getId() - o.getId();
    }
    
    @Transient
    public String getFullName() {
        if(this.getFirstName() != null && this.getLastName() != null) {
            return String.format("%s %s", this.getFirstName(), this.getLastName());
        } else {
            return this.getName();
        }
    }
}
