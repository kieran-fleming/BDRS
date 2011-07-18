package au.com.gaiaresources.bdrs.model.detect.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapability;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDevice;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDeviceDAO;

@Repository
public class BDRSWurflDeviceDAOImpl extends AbstractDAOImpl implements BDRSWurflDeviceDAO {
	
	Logger log = Logger.getLogger(this.getClass());

	@Override
	public BDRSWurflDevice createDevice(String deviceIdString, String userAgentString) {		
		BDRSWurflDevice device = new BDRSWurflDevice();
		device.setDeviceId(deviceIdString);
		device.setUserAgent(userAgentString);
		return save(device);
	}
	
	@Override
	public BDRSWurflDevice createDevice(String deviceIdString) {
		BDRSWurflDevice device = new BDRSWurflDevice();
		device.setDeviceId(deviceIdString);
		return save(device);
	}

	
	@Override
	public BDRSWurflDevice get(Integer id) {
		return getByID(BDRSWurflDevice.class, id);
	}

	@Override
	public BDRSWurflDevice getByUserAgent(String userAgent) {
		return this.getByUserAgent(null, userAgent);
	}

	@Override
	public BDRSWurflDevice getByUserAgent(Session sesh, String userAgent) {
		
		if (sesh == null){
			sesh = getSession();
		}
		
		List<BDRSWurflDevice> devices = find(sesh, "select d from BDRSWurflDevice d where d.userAgent = ?", userAgent);
		if(devices.isEmpty()){
			return null;
		}else{
			return devices.get(0);
		}
		
	}
	
	@Override
	public List<BDRSWurflCapability> getCapabilitiesByUserAgent(String userAgent) {
		return this.getCapabilitiesByUserAgent(null, userAgent);
	}
	
	@Override
	public List<BDRSWurflCapability> getCapabilitiesByUserAgent(Session sesh, String userAgent) {
		
		
		if (sesh == null){
			sesh = getSession();
		}
		
		return find(sesh, "select c from BDRSWurflDevice d join d.capabilities c where d.userAgent = ?", userAgent);
	}
	
	@Override
	public String getCapabilityValue(String userAgent, String capabilityName) {
		return this.getCapabilityValue(null, userAgent, capabilityName);
	}
	
	@Override
	public String getCapabilityValue(Session sesh, String userAgent, String capabilityName) {
		
		String query = "select c.value from BDRSWurflDevice d join d.capabilities c where d.userAgent = :ua and c.name = :name";
		Query q;
		
		if (sesh == null){
			q = getSession().createQuery(query);
		}else{
			q = sesh.createQuery(query);
		}
		
		q.setParameter("ua", userAgent);
		q.setParameter("name", capabilityName);
		
		List<String> capList = q.list();
		if (capList.size() > 0) {
			if (capList.size() > 1) {
				log.warn("Found Multiple values for capability " + capabilityName + " in combination with userAgent " + userAgent);
			}
			return capList.get(0);
		}
		return null;
		
	}
	

	@Override
	public BDRSWurflDevice updateDevice(BDRSWurflDevice device) {
		 Object o = merge(device);
	     update((BDRSWurflDevice) o);
	     return (BDRSWurflDevice) o;
	}

	@Override
	public List<BDRSWurflDevice> getDevices() {
		return newQueryCriteria(BDRSWurflDevice.class).run();
	}

	@Override
	public List<BDRSWurflDevice> getDevices(Session sesh) {
		String query = "from BDRSWurflDevice";
		Query q;
		if (sesh == null) {
			q = getSession().createQuery(query);
		} else {
			q = sesh.createQuery(query);
		}
		return q.list();
	}
	
	@Override
	public BDRSWurflDevice getByIdString(String deviceIdString) {
		return this.getByIdString(null, deviceIdString);
	}

	@Override
	public BDRSWurflDevice getByIdString(Session sesh, String deviceIdString) {
		String query = "select d from BDRSWurflDevice d where d.deviceId = :deviceId";
		Query q;
		
		if (sesh == null){
			q = getSession().createQuery(query);
		}else{
			q = sesh.createQuery(query);
		}
		
		q.setParameter("deviceId", deviceIdString);
		
		List<BDRSWurflDevice> deviceList = q.list();
		if (deviceList.size() > 0) {
			if (deviceList.size() > 1) {
				log.warn("Found Multiple devices for deviceIdString = " + deviceIdString);
			}
			return deviceList.get(0);
		}
		return null;
	}

	@Override
	public HashMap<String, BDRSWurflDevice> getDevicesMap(Session sesh) {
		HashMap<String, BDRSWurflDevice> devicesMap = new HashMap<String, BDRSWurflDevice>();
		List<BDRSWurflDevice> devices = this.getDevices(sesh);
		for (BDRSWurflDevice d : devices){
			devicesMap.put(d.getDeviceId(), d);
		}
		return devicesMap;
	}
	
	@Override
	public HashMap<String, BDRSWurflDevice> getDevicesMap() {
		return getDevicesMap(null);
	}

	@Override
	public String getCapabilityValue(BDRSWurflDevice device,
			String capabilityName) {
		if(device == null){
			return null;
		}else{
			Set<BDRSWurflCapability> capabilities = device.getCapabilities();
			for (BDRSWurflCapability c : capabilities){
				if(c.getName().equalsIgnoreCase(capabilityName)){
					return c.getValue();
				}
			}
			return this.getCapabilityValue(device.getFallBack(), capabilityName);
		}
	}

	
}
