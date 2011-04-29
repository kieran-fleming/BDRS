package au.com.gaiaresources.bdrs.model.expert;

import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * An expert can deal with questions regarding one or more taxon groups and
 * regions.
 *
 * @author Tim Carpenter
 *
 */
@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Table(name = "EXPERT")
@AttributeOverride(name = "id", column = @Column(name = "EXPERT_ID"))
public class Expert extends PortalPersistentImpl {

    private User user;
    private Set<Region> regions;
    private Set<TaxonGroup> groups;

    /**
     * The user on the system.
     *
     * @return {@link User}
     */
    @ManyToOne
    @JoinColumn(name = "USER_ID", nullable = false)
    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * The regions that this expert knows about.
     *
     * @return {@link Set} {@link Region}
     */
    @ManyToMany
    @JoinTable(name = "EXPERT_REGION", joinColumns = { @JoinColumn(name = "EXPERT_ID") }, inverseJoinColumns = { @JoinColumn(name = "REGION_ID") })
    @ForeignKey(name = "EXPERT_REGION_TO_EXPERT_FK", inverseName = "EXPERT_REGION_TO_REGION_FK")
    public Set<Region> getRegions() {
        return regions;
    }

    public void setRegions(Set<Region> regions) {
        this.regions = regions;
    }

    /**
     * The taxon groups that this expert know about.
     *
     * @return {@link Set} {@link TaxonGroup}
     */
    @ManyToMany
    @JoinTable(name = "EXPERT_TAXON_GROUP", joinColumns = { @JoinColumn(name = "EXPERT_ID") }, inverseJoinColumns = { @JoinColumn(name = "TAXON_GROUP_ID") })
    @ForeignKey(name = "EXPERT_TAXON_GROUP_TO_EXPERT_FK", inverseName = "EXPERT_TAXON_GROUP_TO_TAXON_GROUP_FK")
    public Set<TaxonGroup> getTaxonGroups() {
        return groups;
    }

    public void setTaxonGroups(Set<TaxonGroup> taxonGroups) {
        this.groups = taxonGroups;
    }
}
