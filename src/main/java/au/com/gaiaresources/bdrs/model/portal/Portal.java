package au.com.gaiaresources.bdrs.model.portal;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.apache.log4j.Logger;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "PORTAL")
@AttributeOverride(name = "id", column = @Column(name = "PORTAL_IDENTIFIER"))
public class Portal extends PersistentImpl {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    private String name;
    private boolean isDefault = false;

    public Portal() {
        super();
    }

    @Column(name = "NAME", nullable = false)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    
    @Column(name = "ISDEFAULT", nullable = false)
    public boolean isDefault() {
        return this.isDefault;
    }
    
    public void setDefault(boolean isDefault) {
        this.isDefault = isDefault;
    }
}
