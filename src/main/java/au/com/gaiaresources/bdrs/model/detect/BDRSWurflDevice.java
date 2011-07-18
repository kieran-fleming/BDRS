package au.com.gaiaresources.bdrs.model.detect;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.AttributeOverride;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Table(name = "WURFLDEVICE")
@AttributeOverride(name = "id", column = @Column(name = "DEVICE_ID"))
public class BDRSWurflDevice extends PersistentImpl {
	
	private String deviceIdString;
	
	private String userAgentString;
	
	private BDRSWurflDevice fallBack;
	
	private Set<BDRSWurflCapability> capabilities  = new HashSet<BDRSWurflCapability>();
	
	
	/**
     * {@inheritDoc}
     */
	// Hack to stop unique constraint being generated.. bad hibernate!
	@ManyToMany
	@JoinTable(name="DEVICE_CAPABILITY")
	public Set<BDRSWurflCapability> getCapabilities() {
		return capabilities;
	}

	public void setCapabilities(Set<BDRSWurflCapability> capabilities) {
		this.capabilities = capabilities;
	}

	@Transient
	public void addCapabilities(Set<BDRSWurflCapability> capabilities) {
		this.capabilities.addAll(capabilities);
	}
	
	@Column(name = "DEVICEIDSTRING")
	public String getDeviceId() {
		return deviceIdString;
	}

	public void setDeviceId(String deviceIdString) {
		this.deviceIdString = deviceIdString;
	}

	@Column(name = "USERAGENT")
	public String getUserAgent() {
		return userAgentString;
	}

	public void setUserAgent(String userAgentString) {
		this.userAgentString = userAgentString;
	}

	
	@ManyToOne
	@JoinColumn(name = "FALLBACK_ID", nullable = true)
	public BDRSWurflDevice getFallBack() {
		return fallBack;
	}

	public void setFallBack(BDRSWurflDevice fallBack) {
		this.fallBack = fallBack;
	}


	
	

}
