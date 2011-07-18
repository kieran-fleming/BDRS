package au.com.gaiaresources.bdrs.model.detect;

import java.util.HashMap;
import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.db.TransactionDAO;

public interface BDRSWurflDeviceDAO extends TransactionDAO{
	
	
	/**
     * Creates a new device
     * @param deviceIdString for the new BDRSWurflDevice
     * @param userAgentString for the new BDRSWurflDevice
     * @return the persistent instance of the BDRSWurflDevice.
     */
    public BDRSWurflDevice createDevice(String deviceIdString, String userAgentString);
    
	/**
     * Creates a new device
     * @param deviceIdString for the new BDRSWurflDevice
     * @return the persistent instance of the BDRSWurflDevice.
     */
    public BDRSWurflDevice createDevice(String deviceIdString);
    
    /**
     * Gets a device from the database
     * @param id of the device in the database
     * @return the persistent instance of the BDRSWurflDevice.
     */
    public BDRSWurflDevice get(Integer id);
    
    /**
     * Gets the device that matches the userAgent.
     * @param userAgent of the device we are looking for
     * @return
     */
	public BDRSWurflDevice getByUserAgent(String userAgent);
	public BDRSWurflDevice getByUserAgent(Session sesh, String userAgent);
	
	/**
	 * Updates changes to the device to the database
	 * @param device that is to be updated
	 * @return the persistent instance of the updated BDRSWurfldevice.
	 */
	public BDRSWurflDevice updateDevice(BDRSWurflDevice device);

	/**
	 * Gets all the devices in the database.
	 * @return a list of the persistent instances of the BDRSWurfldevice that exist in the database.
	 */
	public List<BDRSWurflDevice> getDevices();
	public List<BDRSWurflDevice> getDevices(Session sesh);
	
	/**
	 * Gets all the devices in the database.
	 * @return a HasMap that maps the xmlDeviceIds to the persistent instances of BDRSWurfldevice.
	 */
	public HashMap<String, BDRSWurflDevice> getDevicesMap();
	public HashMap<String, BDRSWurflDevice> getDevicesMap(Session sesh);

	/**
	 * Gets the value of a capability for a particular User-Agent.
	 * @param userAgent of a particular device
	 * @param capabilityName of which the value is requested
	 * @return the string value of the capability
	 */
	String getCapabilityValue(String userAgent,String capabilityName);
	String getCapabilityValue(Session sesh, String userAgent,String capabilityName);
	
	/**
	 * 
	 * @param device
	 * @param capabilityName
	 * @return
	 */
	String getCapabilityValue(BDRSWurflDevice device, String capabilityName);

	/**
	 * Gets the capabilities of a device.
	 * @param userAgent of the device we want the capabilities from
	 * @return  a list of the persistent instance of the BDRSWurflCapability
	 */
	List<BDRSWurflCapability> getCapabilitiesByUserAgent(String userAgent);
	List<BDRSWurflCapability> getCapabilitiesByUserAgent(Session sesh, String userAgent);

	/**
	 * Gets a device
	 * @param deviceIdString of the device we are requesting
	 * @return the persistent instance of the updated BDRSWurfldevice.
	 */
	public BDRSWurflDevice getByIdString(String deviceIdString);
	public BDRSWurflDevice getByIdString(Session sesh, String deviceIdString);
	
	
	

   

}
