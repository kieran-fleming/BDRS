package au.com.gaiaresources.bdrs.service.detect;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import au.com.gaiaresources.bdrs.db.SessionFactory;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapability;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflCapabilityDAO;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDevice;
import au.com.gaiaresources.bdrs.model.detect.BDRSWurflDeviceDAO;

@Service
public class BDRSWurflLoadService {
	Logger log = Logger.getLogger(BDRSWurflLoadService.class);
	
	@Autowired
	private BDRSWurflDeviceDAO deviceDAO;
	
	@Autowired
	private BDRSWurflCapabilityDAO capabilityDAO;
	
	@Autowired
	private SessionFactory sessionFactory;
	
	public void loadWurflXML(String fileName) throws IOException{
		
		Session sesh = sessionFactory.getCurrentSession();
		sesh.setFlushMode(FlushMode.MANUAL);
	    
		NodeList deviceNodeLst;
		
		HashMap<String, BDRSWurflDevice> devices = deviceDAO.getDevicesMap();
		HashMap<String, HashMap<String, BDRSWurflCapability>> capabilities = capabilityDAO.getCapabilitiesMap();

		InputStream inputStream = this.getClass().getResourceAsStream(fileName);
		try {
			if(inputStream != null){
				
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		        DocumentBuilder db;
				db = dbf.newDocumentBuilder();
		        Document doc = db.parse(inputStream);
		        
		        doc.getDocumentElement().normalize();
		        
		        deviceNodeLst = doc.getElementsByTagName("device");
		        
		        for (int i = 0; i < deviceNodeLst.getLength(); i++) {
		        	Node deviceNode = deviceNodeLst.item(i);
					 if (deviceNode.getNodeType() == Node.ELEMENT_NODE) {
						 Element deviceElement = (Element) deviceNode;
						 String userAgentAttr = deviceElement.getAttribute("user_agent");
						 String fallBackIdAttr = deviceElement.getAttribute("fall_back");
						 String deviceIdAttr = deviceElement.getAttribute("id");
						 
						 //get device if exists
						 BDRSWurflDevice device = devices.get(deviceIdAttr);
						 if(device != null){
							 // update device it's user agent
							 device.setUserAgent(userAgentAttr);
						 }else{
							 //create new device
							 device = deviceDAO.createDevice(deviceIdAttr, userAgentAttr);
						 }
						 
						 //get device it's fallback if exists
						 BDRSWurflDevice fallbackDevice = devices.get(fallBackIdAttr);
						 if(fallbackDevice != null){
							 //update device it's fallback
							 device.setFallBack(fallbackDevice);
						 }else{
							 if(!fallBackIdAttr.equalsIgnoreCase("root")){
								 //create a new fallback
								 fallbackDevice = deviceDAO.createDevice(fallBackIdAttr); 
								 //update device it's fallback
								 device.setFallBack(fallbackDevice);
								 devices.put(fallbackDevice.getDeviceId(), fallbackDevice);
							 }
						 }
						
						 //Add capabilities to the device
						 NodeList groupNodeLst = deviceNode.getChildNodes();
						 for (int j = 0; j < groupNodeLst.getLength(); j++){
							 Node groupNode = groupNodeLst.item(j);
							 if (groupNode.getNodeType() == Node.ELEMENT_NODE){
								 Element groupElement = (Element) groupNode;
								 String groupIdAttr = groupElement.getAttribute("id");
								 
								 NodeList capabilitiesNodeLst = groupNode.getChildNodes();
								 for (int k = 0; k < capabilitiesNodeLst.getLength(); k++){
									 Node capabilityNode = capabilitiesNodeLst.item(k);
									 if (capabilityNode.getNodeType() == Node.ELEMENT_NODE){
										Element capabilityElement = (Element) capabilityNode;
										String capabilityName = capabilityElement.getAttribute("name");
										String capabilityValue = capabilityElement.getAttribute("value");
										
										//get device it's capability if it exists
										HashMap<String, BDRSWurflCapability> valueToCapability = capabilities.get(capabilityName);
										BDRSWurflCapability c;
										if (valueToCapability != null){
											if(valueToCapability.get(capabilityValue) != null){
												//it exists don't do anything
												c = valueToCapability.get(capabilityValue);
											}else{
												//create capability (capability does not exist with the current capabilityValue)
												c = capabilityDAO.create(groupIdAttr, capabilityName, capabilityValue);
												valueToCapability.put(c.getValue(), c);
												capabilities.put(c.getName(), valueToCapability);
											}
										}else{
											//create capability
											c = capabilityDAO.create(groupIdAttr, capabilityName, capabilityValue);
											valueToCapability = new HashMap<String, BDRSWurflCapability>();
											valueToCapability.put(c.getValue(), c);
											capabilities.put(c.getName(), valueToCapability);
										}
										device.getCapabilities().add(c);
									 }
								 }
								 
								 BDRSWurflDevice updatedDevice = deviceDAO.updateDevice(device);
								 devices.put(updatedDevice.getDeviceId(), updatedDevice);
							 }
						 }
					 }
		        }
		        
		        
		        // save to the database
		        sesh.flush();
		        sesh.clear();
		        
		      }else{
		    	  log.warn("InputStream is null. Xml file probably does not exist.");
		      }

			} catch (ParserConfigurationException e) {
				e.printStackTrace();
			} catch (SAXException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}finally{
				//close InputSTream
				if(inputStream != null){
					inputStream.close();
				}
				//commit transaction
		        sesh.setFlushMode(FlushMode.AUTO);
			}
			
	}
	
	
}
