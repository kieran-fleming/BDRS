package au.com.gaiaresources.bdrs.model.detect;

import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;

public interface BDRSWurflCapabilityDAO extends TransactionDAO{
	
	/**
	 * Creates a device capability
	 * @param group to which the capability belongs
	 * @param name of the capability
	 * @param value of the capability
	 * @return the persistent instance of the BDRSWurflCapability.
	 */
	BDRSWurflCapability create(String group, String name, String value);
	BDRSWurflCapability create(Session sesh, String group, String name, String value);
	
	/**
	 * Gets an existing capability
	 * @param id of the capability in the database
	 * @return the persistent instance of the BDRSWurflCapability.
	 */
	BDRSWurflCapability get(Integer id);
	BDRSWurflCapability get(Session sesh, Integer id);
	
	/**
	 * Gets all the capabilities of a particular group
	 * @param groupName of the capabilities group
	 * @return a List of the persistent instance of the BDRSWurflCapability.
	 */
	List<BDRSWurflCapability> getByGroup(String groupName);
	List<BDRSWurflCapability>  getByGroup(Session sesh, String groupName);
	
	/**
	 * Gets all the capabilities with a particular name
	 * @param name of the capabilities that are being requested
	 * @return a List of the persistent instance of the BDRSWurflCapability.
	 */
	List<BDRSWurflCapability> getByName(String name);
	List<BDRSWurflCapability>  getByName(Session sesh, String name);
	
	/**
	 * Gets a capability from the database with a specific key value combination
	 * @param capabilityName of the capability
	 * @param capabilityValue of the capability
	 * @return the persistent instance of the BDRSWurflCapability.
	 */
	BDRSWurflCapability getByNameValue(String capabilityName, String capabilityValue);
	BDRSWurflCapability getByNameValue(Session sesh, String capabilityName, String capabilityValue);

	 /**
     * Gets all device capabilities from the database
     * @return the persistent instances of the BDRSWurflCapabilities as a list.
     */
	List<BDRSWurflCapability> getAll();
	List<BDRSWurflCapability> getAll (Session sesh);
	
	/**
	 * Gets all device capabilities from the database
	 * @return a HasMap that maps the xmlCapabilityIds to a HashMap that maps the xmlCapabilityValue with the persistent instances of BDRSWurflCapability.
	 */
	HashMap<String, HashMap<String, BDRSWurflCapability>> getCapabilitiesMap();
	
}
