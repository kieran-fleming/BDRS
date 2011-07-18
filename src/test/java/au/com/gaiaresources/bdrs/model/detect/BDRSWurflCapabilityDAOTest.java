package au.com.gaiaresources.bdrs.model.detect;

import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;

public class BDRSWurflCapabilityDAOTest extends AbstractControllerTest{
	
	@Autowired
	private BDRSWurflCapabilityDAO capabilityDAO;
	
	private BDRSWurflCapability capability1;
	private Integer capabilityId1;
	
	@Before
	public void setup(){
		capability1 =  capabilityDAO.create("product_info", "is_tablet", "true");
		capabilityDAO.create("display", "physical_screen_height", "400");
		capabilityDAO.create("display", "physical_screen_width", "200");
		capabilityId1 = capability1.getId();
	}
	
	@Test
	public void testCreate(){
		BDRSWurflCapability createdCapability = capabilityDAO.get(capabilityId1);
		Assert.assertNotNull(createdCapability);
		Assert.assertTrue(createdCapability.getId() > 0);
		Assert.assertEquals("product_info", createdCapability.getGroup());
		Assert.assertEquals("is_tablet", createdCapability.getName());
		Assert.assertEquals("true", createdCapability.getValue());
	}
	
	@Test
	public void testGet(){
		
	}
	
	@Test
	public void testGetByGroup(){
		List<BDRSWurflCapability> capabilities = capabilityDAO.getByGroup("display");
		
		Assert.assertNotNull(capabilities);
		Assert.assertTrue("Expected 2 BDRSWurflCapabilities", capabilities.size() == 2);
		for(BDRSWurflCapability c : capabilities){
			Assert.assertEquals("display", c.getGroup());
		}
	}
	
	@Test
	public void testGetByName(){
		List<BDRSWurflCapability> capabilities = capabilityDAO.getByName("is_tablet");
		
		Assert.assertNotNull(capabilities);
		Assert.assertTrue("Expected 1 BDRSWurflCapability", capabilities.size() == 1);
		Assert.assertEquals("is_tablet", capabilities.get(0).getName());
	}
	
	@Test
	public void testGetByNameValue(){
		BDRSWurflCapability c = capabilityDAO.getByNameValue("physical_screen_height", "400");
		
		Assert.assertEquals("physical_screen_height", c.getName());
		Assert.assertEquals("400", c.getValue());
	}
	
	@Test
	public void testGetAll(){
		List<BDRSWurflCapability> capabilities = capabilityDAO.getAll();
		
		Assert.assertEquals(3, capabilities.size());
	}

}
