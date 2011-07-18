package au.com.gaiaresources.bdrs.service.detect;

import java.io.IOException;
import java.util.List;

import junit.framework.Assert;

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
	
	@Test
	public void testLoadWurflXML() throws IOException {
		loadService.loadWurflXML("wurfl.xml");
		loadService.loadWurflXML("wurfl_patch.xml");
		List<BDRSWurflDevice> devices =  deviceDAO.getDevices();
		
		Assert.assertEquals(6, devices.size());
		Assert.assertEquals(497, devices.get(0).getCapabilities().size());
		Assert.assertEquals(21, devices.get(1).getCapabilities().size());
	}
	
}
