package au.com.gaiaresources.bdrs.model.detect;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "WURFLCAPABILITY")
@AttributeOverride(name = "id", column = @Column(name = "CAPABILITY_ID"))
public class BDRSWurflCapability extends PersistentImpl{
	
	private String name;
	
	private String value;
	
	private String group;
	
	
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
    
    @Column(name = "VALUE")
    public String getValue() {
        return value;
    }
    public void setValue(String value) {
        this.value = value;
    }
    
    @Column(name = "CAPABILITY_GROUP")
    public String getGroup() {
        return group;
    }
    public void setGroup(String group) {
        this.group = group;
    }

}
