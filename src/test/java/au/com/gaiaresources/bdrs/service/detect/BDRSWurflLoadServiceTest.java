package au.com.gaiaresources.bdrs.service.detect;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDevice;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDeviceDAO;

public class BDRSWurflLoadServiceTest extends AbstractControllerTest{
	
	@Autowired
	private BDRSWurflLoadService loadService;
	
	@Autowired
	private BDRSWurflDeviceDAO deviceDAO;
	
	private Logger log = Logger.getLogger(getClass());
	
	@Test
	public void testLoadWurflXML() throws IOException {
		loadService.loadWurflXML("wurfl.xml");
		loadService.loadWurflXML("wurfl_patch.xml");
		
		List<BDRSWurflDevice> devices =  deviceDAO.getDevices();
		// not sure where the 6 comes from....
		Assert.assertEquals("number of devices should match", 6, devices.size());
		BDRSWurflDevice genericDevice = deviceDAO.getByIdString("generic");
		Assert.assertNotNull("device should not be null", genericDevice);
		Assert.assertEquals("number of capabilities shoudl match", 497, genericDevice.getCapabilities().size());
		BDRSWurflDevice genericXhtmlDevice = deviceDAO.getByIdString("generic_xhtml");
		Assert.assertNotNull("device should not be null", genericXhtmlDevice);
		Assert.assertEquals("number of capabilities shoudl match", 21, genericXhtmlDevice.getCapabilities().size());
	}
	
}
