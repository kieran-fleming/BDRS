package au.com.gaiaresources.bdrs.model.threshold;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.ManyToMany;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Filter;
import org.hibernate.annotations.FilterDef;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.ParamDef;

import au.com.gaiaresources.bdrs.db.impl.PortalPersistentImpl;
import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;

@Entity
@FilterDef(name=PortalPersistentImpl.PORTAL_FILTER_NAME, parameters=@ParamDef( name="portalId", type="integer" ) )
@Filter(name=PortalPersistentImpl.PORTAL_FILTER_NAME, condition=":portalId = PORTAL_ID")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "THRESHOLD")
@AttributeOverride(name = "id", column = @Column(name = "ID"))
/**
 * Thresholds represent a series of {@link Condition}s that when met, will
 * execute a set of {@link Action}s.
 */
public class Threshold extends PortalPersistentImpl {
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    private String className;
    private boolean enabled = true;
    private List<Condition> conditions = new ArrayList<Condition>();
    private List<Action> actions = new ArrayList<Action>();

    public Threshold() {
        super();
    }

    @Column(name = "CLASSNAME", nullable = false)
    @Index(name = "threshold_classname_index")
    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }
    
    @Column(name = "ENABLED", nullable = false)
    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    public List<Condition> getConditions() {
        return conditions;
    }

    public void setConditions(List<Condition> conditions) {
        this.conditions = conditions;
    }

    @ManyToMany(fetch = FetchType.LAZY)
    public List<Action> getActions() {
        return actions;
    }

    public void setActions(List<Action> actions) {
        this.actions = actions;
    }
    
    @Transient
    public ActionType[] getPossibleActionTypes() throws ClassNotFoundException {
        return ThresholdService.CLASS_TO_ACTION_MAP.get(Class.forName(this.className));
    }
}
