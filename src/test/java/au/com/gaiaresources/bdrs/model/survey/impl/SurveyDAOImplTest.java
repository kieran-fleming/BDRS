/**
 * 
 */
package au.com.gaiaresources.bdrs.model.survey.impl;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.hibernate.Query;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.portal.PortalDAO;
import au.com.gaiaresources.bdrs.model.region.Region;
import au.com.gaiaresources.bdrs.model.region.RegionDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.servlet.RequestContext;
import au.com.gaiaresources.bdrs.servlet.RequestContextHolder;

import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Polygon;
/**
 * @author kehan
 *
 */
public class SurveyDAOImplTest extends AbstractControllerTest {
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private RegionDAO regionDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private UserDAO userDAO;
    @Autowired
    private PortalDAO portalDAO;
    
    private Logger log = Logger.getLogger(getClass());
   

    private Survey survey;
    private TaxonGroup taxonGroup;
    private IndicatorSpecies speciesA;
    private IndicatorSpecies speciesB;
    private IndicatorSpecies speciesC;
    private Region regionA;
    private Region regionB;
    private GeometryBuilder geometryBuilder = new GeometryBuilder();
    private List<IndicatorSpecies> species;
    private User user;
    ArrayList<Location> locations;
    
    /**
     * Set up a few test species etc that we can test against
     * Copied from SingleSiteMultiTaxaControllerTest.java
     */
    @Before
    public void setUp() {
        user = userDAO.getUser("admin");
        taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);
        
        
        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);
        
        speciesB = new IndicatorSpecies();
        speciesB.setCommonName("Indicator Species B");
        speciesB.setScientificName("Indicator Species B");
        speciesB.setTaxonGroup(taxonGroup);
        speciesB = taxaDAO.save(speciesB);
        speciesC = new IndicatorSpecies();
        speciesC.setCommonName("Indicator Species B");
        speciesC.setScientificName("Indicator Species B");
        speciesC.setTaxonGroup(taxonGroup);
        speciesC = taxaDAO.save(speciesB);
        species = new ArrayList<IndicatorSpecies>(2);
        species.add(speciesA);
        species.add(speciesB);
           
        
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for(AttributeType attrType : AttributeType.values()) {  
            attr = new Attribute();
            attr.setRequired(true);
            attr.setName(attrType.toString());
            attr.setTypeCode(attrType.getCode());
            attr.setTag(false);
            
            if(AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                for(int i=0; i<4; i++) {
                    AttributeOption opt = new AttributeOption();
                    opt.setValue(String.format("Option %d", i));
                    opt = taxaDAO.save(opt);
                    optionList.add(opt);
                }
                attr.setOptions(optionList);
            }
            
            attr = taxaDAO.save(attr);
            attributeList.add(attr);
        }
        //Create a Survey
        survey = new Survey();
        survey.setName("SurveyDAOTest");
        survey.setActive(true);
        survey.setDate(new Date());
        survey.setDescription("Survey To Test Survey DAO with");
        survey.setAttributes(attributeList);
        
        //Add The species to the survey
        survey.setSpecies(new HashSet<IndicatorSpecies>(species));
        survey = surveyDAO.save(survey);
        
        //Create some regions
        Set<Polygon> polygons = new HashSet<Polygon>();
        polygons.add(geometryBuilder.createSquare(-50, -50, 50));
        MultiPolygon multiPolygon = new MultiPolygon((Polygon[])polygons.toArray(new Polygon[polygons.size()]), geometryBuilder.getFactory());
        regionA = regionDAO.createRegion(speciesA.getScientificName(), multiPolygon);
        log.debug("Region A has definition: " + regionA.getBoundary().toText());
        polygons = new HashSet<Polygon>();
        polygons.add(geometryBuilder.createSquare(10, 10, 50));
        multiPolygon = new MultiPolygon((Polygon[])polygons.toArray(new Polygon[polygons.size()]), geometryBuilder.getFactory());
        regionB = regionDAO.createRegion(speciesB.getScientificName(), multiPolygon);
        log.debug("Region B has definition: " + regionB.getBoundary().toText());

        //Add them to the species
        speciesA.getRegions().add(regionA);
        speciesA = taxaDAO.save(speciesA);
        speciesB.getRegions().add(regionB);
        speciesB = taxaDAO.save(speciesB);
        
        //Create some Locations
        Location loc;
        locations = new ArrayList<Location>(2);
        loc = new Location();
        loc.setName("A1");
        loc.setLocation(geometryBuilder.createPoint(-20, -20));
        //Why do we have to have a user in a location?
        loc.setUser(user);
        locations.add(locationDAO.createLocation(loc));
        loc = new Location();
        loc.setName("A2");
        loc.setLocation(geometryBuilder.createPoint(-10, -10));
        loc.setUser(user);
        locations.add(locationDAO.createLocation(loc));
        
        //Add the locations to the survey
        survey.setLocations(locations);
        survey = surveyDAO.save(survey);
        
    }
    
    /**
     * Test survey creation
     */
    @Test
    public void testCreateSurvey() throws Exception {
        String surveyName = "testSurvey";
        Survey s = surveyDAO.createSurvey(surveyName);
        Assert.assertEquals(surveyName, s.getName());
    }
    /**
     * Make sure we can load the survey created in the @Before above
     * @throws Exception
     */
    @Test
    public void testCreatedSurveysExist() throws Exception{
        Survey s = surveyDAO.getSurveyByName(survey.getName());
        Assert.assertNotNull(s);
        Assert.assertEquals(survey.getId(), s.getId());
    }
    
    /**
     * Retrieve species that fall within the points that make up a survey
     * @throws Exception
     */
    @Test
    public void testGetSpeciesWithinSurveyLocations() throws Exception{
        Set<IndicatorSpecies> retrievedSpecies = surveyDAO.getSpeciesWithinSurveyLocations(survey);
        //We have only added one species to the region, there should only be a single result
        Assert.assertEquals(1, retrievedSpecies.size());
        for(IndicatorSpecies sp: retrievedSpecies){
            //And this shoudl be speciesA
            Assert.assertEquals(speciesA, sp);
        }
    }
    /**
     * Runs the previous test, but retrieves the cached species from the survey.
     * @throws Exception
     */
    @Test
    public void testGetSpeciesWithinSurveyLocationsCache() throws Exception{
      //And now we do it all over again. The survey species will be cached so this covers the 
      //species from the cache
        this.testGetSpeciesWithinSurveyLocations();
    }
    
    @Test
    public void testSurveySpeciesCacheExpiry() throws Exception{
        //Get species from the cache
        Set<IndicatorSpecies> cachedSpecies = surveyDAO.getSpeciesWithinSurveyLocations(survey);
        //Make sure we're loading from the cache
        cachedSpecies = surveyDAO.getSpeciesWithinSurveyLocations(survey);
        Assert.assertEquals(1, cachedSpecies.size());
        for(IndicatorSpecies sp: cachedSpecies){
            //And this shoudl be speciesA
            Assert.assertEquals(speciesA, sp);
        }
        speciesC.getRegions().add(regionA);
        taxaDAO.save(speciesC);
        surveyDAO.save(survey);
//        surveyDAO.expireCache(survey);
        //Now the cache should have expired
        Set<IndicatorSpecies> refreshedSpecies = surveyDAO.getSpeciesWithinSurveyLocations(survey);
        Assert.assertEquals(2, refreshedSpecies.size());
        Assert.assertTrue("Returned set should contain species A", refreshedSpecies.contains(speciesA));
        Assert.assertTrue("Returned set should contain species C", refreshedSpecies.contains(speciesC));
    }
}
