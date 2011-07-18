package au.com.gaiaresources.bdrs.controller.record;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.util.StringUtils;

import com.vividsolutions.jts.util.Assert;

public abstract class RecordFormTest extends AbstractControllerTest {
    
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private MetadataDAO metadataDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    
    @Autowired
    private LocationService locationService;    
    
    protected void testRecordLocations(String requestURI,
                            boolean predefinedLocationsOnly,
                            SurveyFormRendererType rendererType,
                            boolean loginAsSurveyOwner) throws Exception {

        TaxonGroup taxonGroup = new TaxonGroup();
        taxonGroup.setName("Test Taxon Group");
        taxonGroup = taxaDAO.save(taxonGroup);
        
        Set<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();

        IndicatorSpecies species = new IndicatorSpecies();
        species.setCommonName("testRecordLocations species");
        species.setScientificName("testRecordLocations species");
        species.setTaxonGroup(taxonGroup);
        species = taxaDAO.save(species);
        speciesSet.add(species);
        
        User admin = userDAO.getUser("admin");
        Survey survey = new Survey();
        survey.setName("testRecordLocations");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription("RecordFormTest.testRecordLocations Description");
        
        Metadata md = survey.setFormRendererType(rendererType);
        metadataDAO.save(md);
        
        if(predefinedLocationsOnly) {
            Metadata predefinedLocMetadataData = new Metadata();
            predefinedLocMetadataData.setKey(Metadata.PREDEFINED_LOCATIONS_ONLY);
            predefinedLocMetadataData.setValue(Boolean.TRUE.toString());
            metadataDAO.save(predefinedLocMetadataData);
            survey.getMetadata().add(predefinedLocMetadataData);
        }
        
        survey.setSpecies(speciesSet);
        
        Location loc;
        for(int i=0; i<10; i++) {
            loc = new Location();
            loc.setName(String.format("Location %d", i));        
            loc.setUser(admin);
            loc.setLocation(locationService.createPoint(-40.58+(0.1*i), 153.1+(0.1*i)));
            loc = locationDAO.save(loc);
            survey.getLocations().add(loc);
        }
            
        survey = surveyDAO.save(survey);
        
        // Create User and the user's Locations
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(StringUtils.generateRandomString(10, 50), emailAddr);
        User user = userDAO.createUser("testuser", "Abigail", "Ambrose", 
                                       emailAddr, encodedPassword, 
                                       registrationKey, 
                                       new String[]{"ROLE_USER"});
        
        // Notice that there is an overlap between the user location name and 
        // the survey location name.
        List<Location> userLocList = new ArrayList<Location>();
        for(int i=5; i<15; i++) {
            loc = new Location();
            loc.setName(String.format("Location %d", i));        
            loc.setUser(user);
            loc.setLocation(locationService.createPoint(-40.58+(0.1*i), 153.1+(0.1*i)));
            loc = locationDAO.save(loc);
            userLocList.add(loc);
        }
        
        // Now the test
        if(loginAsSurveyOwner) {
            login("admin", "password", new String[] { Role.ADMIN });
        } else {
            login("testuser", "password", new String[] { "ROLE_USER" });
        }
        request.setMethod("GET");
        request.setRequestURI(requestURI);
        request.setParameter("surveyId", survey.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "locations");
        
        Collection<Location> locCollection = (Collection<Location>)mv.getModel().get("locations");
        if(predefinedLocationsOnly) {
            Assert.equals(survey.getLocations().size(), locCollection.size());
            for(Location surveyLoc : survey.getLocations()) {
                Assert.isTrue(locCollection.contains(surveyLoc));
            }
        } else {
            // Expected Locations - No Duplicates
            Set<Location> expectedLoc = new HashSet<Location>();
            expectedLoc.addAll(survey.getLocations());
            
            List<Location> locList = loginAsSurveyOwner ? locationDAO.getUserLocations(admin) : userLocList;
            expectedLoc.addAll(locList);
            
            Assert.equals(expectedLoc.size(), locCollection.size());

            // Contains all survey locations and user locations
            for(Location surveyLoc : survey.getLocations()) {
                Assert.isTrue(locCollection.contains(surveyLoc));
            }
            for(Location userLoc : locList) {
                Assert.isTrue(locCollection.contains(userLoc));   
            }
        }
    }
}
