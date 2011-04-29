package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;

public class ApplicationServiceTest extends AbstractControllerTest {
	
	private Logger log = Logger.getLogger(getClass());
	private Survey frogSurveyInDb, birdAndFrogSurveyInDb;
	
	@Autowired
	private PortalDAO portalDAO;
	
	@Autowired
	private SurveyDAO surveyDAO;
	
	@Autowired
	private LocationDAO locationDAO;
	
	@Autowired
	private TaxaDAO taxaDAO;
	
	@Autowired
	private UserDAO userDAO;
	
	@Autowired
	private AttributeDAO attributeDAO;
	
	@Before
	public void setup(){
		
		 //create a user
		 PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	     String emailAddr = "user@mailinator.com";
	     String encodedPassword = passwordEncoder.encodePassword("password", null);
	     User u = userDAO.createUser("user", "fn", "ln", emailAddr, encodedPassword, "usersIdent", new String[] { "ROLE_USER" });
	     Set<User> users = new HashSet<User>();
	     users.add(u);
	     
	     //create taxa for species
	     TaxonGroup frogTaxon = new TaxonGroup();
	     TaxonGroup birdTaxon = new TaxonGroup();
	     frogTaxon.setName("frogs");
	     birdTaxon.setName("birds");
	     taxaDAO.save(frogTaxon);
	     taxaDAO.save(birdTaxon);
	     
	     //create species
	     Set<IndicatorSpecies> frogSet = new HashSet<IndicatorSpecies>();
	     Set<IndicatorSpecies> birdSet = new HashSet<IndicatorSpecies>();
	     IndicatorSpecies frog1 = new IndicatorSpecies();
	     IndicatorSpecies frog2 = new IndicatorSpecies();
	     IndicatorSpecies bird1 = new IndicatorSpecies();
	     IndicatorSpecies bird2 = new IndicatorSpecies();
	     IndicatorSpecies bird3 = new IndicatorSpecies();
	     frog1.setCommonName("commonNameFrog1");
	     frog2.setCommonName("commonNameFrog2");
	     bird1.setCommonName("commonNamebird1");
	     bird2.setCommonName("commonNamebird2");
	     bird3.setCommonName("commonNamebird3");
	     frog1.setScientificName("scientificNameFrog1");
	     frog2.setScientificName("scientificNameFrog2");
	     bird1.setScientificName("scientificNamebird1");
	     bird2.setScientificName("scientificNamebird2");
	     bird3.setScientificName("scientificNamebird3");
	     frog1.setTaxonGroup(taxaDAO.getTaxonGroup("frogs"));
	     frog2.setTaxonGroup(taxaDAO.getTaxonGroup("frogs"));
	     bird1.setTaxonGroup(taxaDAO.getTaxonGroup("birds"));
	     bird2.setTaxonGroup(taxaDAO.getTaxonGroup("birds"));
	     bird3.setTaxonGroup(taxaDAO.getTaxonGroup("birds"));
	     frogSet.add(taxaDAO.save(frog1));
	     frogSet.add(taxaDAO.save(frog2));
	     birdSet.add(taxaDAO.save(bird1));
	     birdSet.add(taxaDAO.save(bird2));
	     birdSet.add(taxaDAO.save(bird3));
	     
	     //create attributes for survey
	     List<Attribute> attributes = new ArrayList<Attribute>();
	     List<Attribute> birdAndFrogAttributes = new ArrayList<Attribute>();
	     Attribute frogSurveyAttribute1 = new Attribute();
	     Attribute frogSurveyAttribute2 = new Attribute();
	     Attribute birdAndFrogSurveyAttribute1 = new Attribute();
	     Attribute birdAndFrogSurveyAttribute2 = new Attribute();
	     Attribute birdAndFrogSurveyAttribute3 = new Attribute();
	     frogSurveyAttribute1.setName("surveyAttribute1");
	     frogSurveyAttribute2.setName("surveyAttribute2");
	     birdAndFrogSurveyAttribute1.setName("surveyAttribute1");
	     birdAndFrogSurveyAttribute2.setName("surveyAttribute2");
	     birdAndFrogSurveyAttribute3.setName("surveyAttribute3");
	     frogSurveyAttribute1.setRequired(true);
	     frogSurveyAttribute2.setRequired(true);
	     birdAndFrogSurveyAttribute1.setRequired(true);
	     birdAndFrogSurveyAttribute2.setRequired(true);
	     birdAndFrogSurveyAttribute3.setRequired(true);
	     frogSurveyAttribute1.setTypeCode("SV");
	     frogSurveyAttribute2.setTypeCode("SV");
	     birdAndFrogSurveyAttribute1.setTypeCode("SV");
	     birdAndFrogSurveyAttribute2.setTypeCode("SV");
	     birdAndFrogSurveyAttribute3.setTypeCode("SV");
	     frogSurveyAttribute1.setTag(false);
	     frogSurveyAttribute2.setTag(false);
	     birdAndFrogSurveyAttribute1.setTag(false);
	     birdAndFrogSurveyAttribute2.setTag(false);
	     birdAndFrogSurveyAttribute3.setTag(false);
	     attributes.add(attributeDAO.save(frogSurveyAttribute1));
	     attributes.add(attributeDAO.save(frogSurveyAttribute2));
	     birdAndFrogAttributes.add(attributeDAO.save(birdAndFrogSurveyAttribute1));
	     birdAndFrogAttributes.add(attributeDAO.save(birdAndFrogSurveyAttribute2));
	     birdAndFrogAttributes.add(attributeDAO.save(birdAndFrogSurveyAttribute3));
	     
	     //create locations for survey
	     List<Location> locations = new ArrayList<Location>();
	     List<Location> birdSurveyLocations = new ArrayList<Location>();
	     Location l1 = new Location();
	     Location l2 = new Location();
	     Location birdSurveyLocation1 = new Location();
	     Location birdSurveyLocation2 = new Location();
	     Location birdSurveyLocation3 = new Location();
	     l1.setName("location1");
	     l2.setName("location2");
	     birdSurveyLocation1.setName("location1");
	     birdSurveyLocation2.setName("location2");
	     birdSurveyLocation3.setName("location3");
	     
	     //create surveys
	     Survey frogSurvey = new Survey();
	     Survey birdAndFrogSurvey = new Survey();
	     frogSurvey.setActive(true);
	     birdAndFrogSurvey.setActive(true);
	     frogSurvey.setName("frogSurvey");
	     birdAndFrogSurvey.setName("birdAndFrogSurvey");
	     frogSurvey.setDescription("This is a frog survey used for testing");
	     birdAndFrogSurvey.setDescription("This is a birdsAndFrogs survey used for testing");
	     frogSurvey.setUsers(users);
	     birdAndFrogSurvey.setUsers(users);
	     //add attributes
	     frogSurvey.setAttributes(attributes);
	     birdAndFrogSurvey.setAttributes(birdAndFrogAttributes);
	     //add locations
	     locations.add(locationDAO.save(l1));
	     locations.add(locationDAO.save(l2));
	     frogSurvey.setLocations(locations);
	     birdSurveyLocations.add(locationDAO.save(birdSurveyLocation1));
	     birdSurveyLocations.add(locationDAO.save(birdSurveyLocation2));
	     birdSurveyLocations.add(locationDAO.save(birdSurveyLocation3));
	     birdAndFrogSurvey.setLocations(birdSurveyLocations);
	     //add species
	     frogSurvey.setSpecies(frogSet);
	     birdAndFrogSurvey.setSpecies(birdSet);
	     birdAndFrogSurvey.getSpecies().addAll(frogSet);
	     //save the survey
	     frogSurveyInDb = surveyDAO.save(frogSurvey);
	     birdAndFrogSurveyInDb = surveyDAO.save(birdAndFrogSurvey);
	}
	
/*	@Test
	public void testGetApplicationData() throws Exception{
		//Portal portal = portalDAO.getPortal(true);
		
	}*/
	
	/**
	 * Tests getting survey related data from a particular survey.
	 * There is no survey on the device yet.
	 * @throws Exception
	 */
	@Test
	public void testGetSurvey() throws Exception{
		request.setMethod("GET");
		request.setRequestURI("/webservice/application/survey.htm");
		request.setParameter("ident", userDAO.getUser("user").getRegistrationKey());
		request.setParameter("sid", frogSurveyInDb.getId().toString());
		
		handle(request, response);
		
		JSONObject responseContent = JSONObject.fromObject(response.getContentAsString());
		
		//count the survey attributes
		JSONArray attributes = JSONArray.fromObject(responseContent.get("attributesAndOptions"));
		Assert.assertEquals("Expected the attributes size to be " + attributes.size() + " but it was " + frogSurveyInDb.getAttributes().size() , attributes.size(), frogSurveyInDb.getAttributes().size());
		//count the survey locations
		JSONArray locations = JSONArray.fromObject(responseContent.get("locations"));
		Assert.assertEquals("Expected the locations size to be " + locations.size() + " but it was " + frogSurveyInDb.getLocations().size() , locations.size(), frogSurveyInDb.getLocations().size());
		//count the species in the survey
		JSONArray indicatorSpecies = JSONArray.fromObject(responseContent.get("indicatorSpecies"));
		Assert.assertEquals("Expected the indicatorSpecies size to be " + indicatorSpecies.size() + " but it was " + frogSurveyInDb.getSpecies().size() , indicatorSpecies.size(), frogSurveyInDb.getSpecies().size());
		
	}
	
	/**
	 * Tests getting survey related data from a particular survey.
	 * There is already a survey on the device
	 * @throws Exception
	 */
	@Test
	public void testGetSurvey1() throws Exception{
		request.setMethod("GET");
		request.setRequestURI("/webservice/application/survey.htm");
		request.addParameter("ident", userDAO.getUser("user").getRegistrationKey());
		request.addParameter("sid", birdAndFrogSurveyInDb.getId().toString());
		JSONArray surveysOnDevice = new JSONArray();
		surveysOnDevice.add(frogSurveyInDb.getId());
		request.addParameter("surveysOnDevice", surveysOnDevice.toString());
		
		handle(request, response);
		
		JSONObject responseContent = JSONObject.fromObject(response.getContentAsString());
		
		//count the survey attributes
		JSONArray attributes = JSONArray.fromObject(responseContent.get("attributesAndOptions"));
		Assert.assertEquals("Expected the attributes size to be " + attributes.size(), attributes.size(), birdAndFrogSurveyInDb.getAttributes().size());
		//count the survey locations
		JSONArray locations = JSONArray.fromObject(responseContent.get("locations"));
		Assert.assertEquals("Expected the locations size to be " + locations.size(), locations.size(), birdAndFrogSurveyInDb.getLocations().size());
		//count the species in the survey
		JSONArray indicatorSpecies = JSONArray.fromObject(responseContent.get("indicatorSpecies"));
		Assert.assertEquals("IndicatorSpecies size does not match", indicatorSpecies.size(), (birdAndFrogSurveyInDb.getSpecies().size() - frogSurveyInDb.getSpecies().size()));
	}

}
