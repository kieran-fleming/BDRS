package au.com.gaiaresources.bdrs.model.detect.impl;

import java.util.HashMap;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.springframework.stereotype.Repository;

import au.com.gaiaresources.bdrs.db.impl.AbstractDAOImpl;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapability;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapabilityDAO;

@Repository
public class BDRSWurflCapabilityDAOImpl extends AbstractDAOImpl implements BDRSWurflCapabilityDAO {
	
	Logger log = Logger.getLogger(getClass());

	@Override
	public BDRSWurflCapability create(String group, String name, String value) {
		return create (null, group, name, value);
	}
	
	@Override
	public BDRSWurflCapability create(Session sesh, String group, String name, String value) {
		if (sesh == null) {
			sesh = getSession();
		}
		
		BDRSWurflCapability capability = new BDRSWurflCapability();
		capability.setGroup(group);
		capability.setName(name);
		capability.setValue(value);
		
		return save(sesh, capability);
	}
	
	@Override
	public BDRSWurflCapability get(Integer id) {
		return getByID(BDRSWurflCapability.class, id);
	}

	@Override
	public BDRSWurflCapability get(Session sesh, Integer id) {
		return getByID(BDRSWurflCapability.class, id);
	}
	
    @Override
    public List<BDRSWurflCapability> getByName(Session sesh, String name) {
    	
    	 if(sesh == null) {
             sesh = getSession();
         }
         
         List<BDRSWurflCapability> capabilities = find(sesh, "select c from BDRSWurflCapability c where c.name = ?", name);
         if(capabilities.isEmpty()){
        	 return null;
         }else{
        	
             return capabilities;
         }
    }
	
	@Override
	public List<BDRSWurflCapability> getByName(String name) {
		return this.getByName(null, name);
	}

    @Override
    public List<BDRSWurflCapability> getByGroup(Session sesh, String groupName) {
    	
    	 if(sesh == null) {
             sesh = getSession();
         }
         
         List<BDRSWurflCapability> capabilities = find(sesh, "select c from BDRSWurflCapability c where c.group = ?", groupName);
         if(capabilities.isEmpty()){
        	 return null;
         }else{
        	
             return capabilities;
         }
    }
    
	@Override
	public  List<BDRSWurflCapability> getByGroup(String groupName) {
		return this.getByGroup(null, groupName);
	}

	@Override
	public BDRSWurflCapability getByNameValue(String capabilityName, String capabilityValue) {
		return this.getByNameValue(null, capabilityName, capabilityValue);
	}
	
	@Override
	@SuppressWarnings("unchecked")
	public BDRSWurflCapability getByNameValue(Session sesh, String capabilityName, String capabilityValue) {
		String query = "select c from BDRSWurflCapability c where c.value = :value and c.name = :name";
		Query q;
		if (sesh == null) {
			q = getSession().createQuery(query);
		} else {
			q = sesh.createQuery(query);
		}
		
		q.setParameter("value", capabilityValue);
		q.setParameter("name", capabilityName);
		
		List<BDRSWurflCapability> capList = q.list();
		if (capList.size() > 0) {
			if (capList.size() > 1) {
				log.warn("Found Multiple BDRSWurflCapabilities for values : " + capabilityName + ", " + capabilityValue);
			}
			return capList.get(0);
		}
		return null;
	}

	@Override
	public List<BDRSWurflCapability> getAll() {
	   return this.getAll(null);
	}
	
	@Override
	public List<BDRSWurflCapability> getAll (Session sesh){
		
		String query = "from BDRSWurflCapability";
		Query q;
		
		if (sesh == null) {
			q = getSession().createQuery(query);
		} else {
			q = sesh.createQuery(query);
		}
		 
		return q.list();
	}

	@Override
	public HashMap<String, HashMap<String, BDRSWurflCapability>> getCapabilitiesMap() {
		HashMap<String, HashMap<String, BDRSWurflCapability>> capabilitiesMap = new HashMap<String, HashMap<String,BDRSWurflCapability>>();
		List<BDRSWurflCapability> capabilities = this.getAll();
		
		for (BDRSWurflCapability c : capabilities){
			if(capabilitiesMap.get(c.getName()) != null){
				capabilitiesMap.get(c.getName()).put(c.getValue(), c);
			}else{
				HashMap<String, BDRSWurflCapability> valueToCapabability = new HashMap<String, BDRSWurflCapability>();
				valueToCapabability.put(c.getValue(), c);
				capabilitiesMap.put(c.getName(), valueToCapabability);
			}
		}
		
		return capabilitiesMap;
	}
	
	

	
	

}
