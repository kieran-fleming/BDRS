package au.com.gaiaresources.bdrs.model.detect;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;

public class BDRSWurflDeviceDAOTest extends AbstractControllerTest{
	
	@Autowired
	private BDRSWurflDeviceDAO deviceDAO;
	@Autowired
	private BDRSWurflCapabilityDAO capabilityDAO;
	
	private BDRSWurflDevice device1;
	private Integer deviceId1;
	private BDRSWurflCapability capability1, capability2, capability3;
	private Map<String, String> capabilitiesTestMap;
	private String userAgent = "Mozilla/5.0 (Linux; U; Android 2.2; en-au; GT-P1000T Build/FROYO) AppleWebKit/533.1 (KHTML, like Gecko) Version/4.0 Mobile Safari/533.1";
	private Set<BDRSWurflCapability> capabilities;
	
	@Before
	public void setup(){
		device1 = deviceDAO.createDevice("samsung_galaxy_tab_ver1", userAgent);
		deviceId1 = device1.getId();
		
		capability1 =  capabilityDAO.create("product_info", "is_tablet", "true");
		capability2 =  capabilityDAO.create("display", "physical_screen_height", "400");
		capability3 =  capabilityDAO.create("display", "physical_screen_width", "200");
		
		capabilities = new HashSet<BDRSWurflCapability>();
		capabilities.add(capability1);
		capabilities.add(capability2);
		capabilities.add(capability3);
		
		capabilitiesTestMap = new HashMap<String, String>();
		capabilitiesTestMap.put("is_tablet", "true");
		capabilitiesTestMap.put("physical_screen_height", "400");
		capabilitiesTestMap.put("physical_screen_width", "200");
	}
	
	@Test
	public void testCreate(){
		BDRSWurflDevice createdDevice = deviceDAO.get(deviceId1);
		
		Assert.assertNotNull(createdDevice);
		Assert.assertEquals(userAgent, createdDevice.getUserAgent());
		Assert.assertEquals("samsung_galaxy_tab_ver1", createdDevice.getDeviceId());
	}
	
	@Test
	public void testGet(){
		BDRSWurflDevice createdDevice = deviceDAO.get(deviceId1);
		Assert.assertNotNull(createdDevice);
		Assert.assertEquals(userAgent, createdDevice.getUserAgent());
		Assert.assertEquals("samsung_galaxy_tab_ver1", createdDevice.getDeviceId());	
	}
	
	@Test
	public void testGetByUserAgent(){
		BDRSWurflDevice createdDevice = deviceDAO.getByUserAgent(userAgent);
		createdDevice.setCapabilities(capabilities);
		Assert.assertNotNull(createdDevice);
		Assert.assertEquals(userAgent, createdDevice.getUserAgent());
		Assert.assertEquals("samsung_galaxy_tab_ver1", createdDevice.getDeviceId());		
	}
	
	@Test
	public void testUpdateDevice(){
		BDRSWurflDevice createdDevice = deviceDAO.get(deviceId1);
		createdDevice.setDeviceId("generic");
		createdDevice = deviceDAO.updateDevice(createdDevice);
		Assert.assertEquals("generic", createdDevice.getDeviceId());
	}
	
	@Test
	public void testGetDevices(){
		List<BDRSWurflDevice> devices = deviceDAO.getDevices();
		Assert.assertEquals(1, devices.size());
	}
	
	@Test
	public void testGetCapabilityValue(){
		BDRSWurflDevice createdDevice = deviceDAO.getByUserAgent(userAgent);
		createdDevice.addCapabilities(capabilities);
		Assert.assertEquals(capability1.getValue(), deviceDAO.getCapabilityValue(userAgent, capability1.getName()));
	}
	
	@Test
	public void getCapabilitiesByUserAgent(){
		BDRSWurflDevice createdDevice = deviceDAO.getByUserAgent(userAgent);
		createdDevice.addCapabilities(capabilities);
		List<BDRSWurflCapability> capabilitiesList = deviceDAO.getCapabilitiesByUserAgent(userAgent);
		Assert.assertEquals(3, capabilitiesList.size());
	}
	
	@Test
	public void testGetByIdString(){
		BDRSWurflDevice existingDevice = deviceDAO.getByIdString("samsung_galaxy_tab_ver1");
		Assert.assertEquals("samsung_galaxy_tab_ver1", existingDevice.getDeviceId());
	}
	
	@Test
	public void testGetDevicesMap(){
		HashMap<String, BDRSWurflDevice> devices = deviceDAO.getDevicesMap();
		Assert.assertEquals(1, devices.size());
	}
	
}
