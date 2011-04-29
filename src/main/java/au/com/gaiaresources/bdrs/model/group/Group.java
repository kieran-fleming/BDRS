package au.com.gaiaresources.bdrs.model.group;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.OrderBy;
import org.hibernate.annotations.ParamDef;
import org.hibernate.annotations.Sort;
import org.hibernate.annotations.SortType;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.user.User;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "USERGROUP")
@AttributeOverride(name = "id", column = @Column(name = "GROUP_ID"))
public class Group extends PortalPersistentImpl implements Comparable<Group> {

    private String name;
    private String description;

    private Set<User> users = new HashSet<User>();
    private Set<User> admins  = new HashSet<User>();

    @OrderBy(clause="created_at asc")
    private Set<Group> groups  = new TreeSet<Group>();


    /**
     * {@inheritDoc}
     */
    @Column(name = "NAME")
    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }

    /**
     * {@inheritDoc}
     */
    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }
    public void setDescription(String desc) {
        this.description = desc;
    }

    @ManyToMany
    @Sort(type=SortType.NATURAL)
    public Set<Group> getGroups() {
        return groups;
    }
    @SuppressWarnings("unchecked")
    public void setGroups(Set<? extends Group> groups) {
        this.groups = (Set<Group>)groups;
    }

    @ManyToMany
    @JoinTable(name="GROUP_USERS")
    public Set<User> getUsers() {
        return users;
    }
    public void setUsers(Set<User> users) {
        this.users = users;
    }

    @ManyToMany
    @JoinTable(name="GROUP_ADMINS")
    public Set<User> getAdmins() {
        return admins;
    }

    @SuppressWarnings("unchecked")
    public void setAdmins(Set<? extends User> admins) {
        this.admins = (Set<User>)admins;
    }

    @Transient
    @Override
    public int compareTo(Group other) {
        // handle null ID stuff...
        if (this.getId() == null && other.getId() == null) {
            if (this == other) {
                return 0;
            } else {
                return 1;
            }
        } else if (this.getId() == null || other.getId() == null) {
            return 1;
        }
        // we're good, compare the ID's as usual
        return this.getId() - other.getId();
    }
}
