package au.com.gaiaresources.bdrs.controller.admin.setup;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.controller.file.AbstractDownloadFileController;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.region.RegionService;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.user.RegistrationService;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.PrecisionModel;

@Controller
public class SetupController extends AbstractController {

    Logger log = Logger.getLogger(AbstractDownloadFileController.class);

    @Autowired
    private TaxaService taxaService;

    @Autowired
    private RegionService regionService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private RegistrationService registrationService;

    @Autowired
	private SurveyDAO surveyDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private GroupDAO groupDAO;

    @Autowired
    private LocationDAO locationDAO;

    @Autowired
    private TaxaDAO taxaDAO;

    @Autowired
    private RecordDAO recordDAO;


	@RequestMapping(value = "/admin/setup.htm", method = RequestMethod.GET)
    public String render() {

		// region
		ArrayList<BigDecimal[]> points = new ArrayList<BigDecimal[]>();
		BigDecimal[] points1 = { new BigDecimal(-31), new BigDecimal(112) };
		BigDecimal[] points2 = { new BigDecimal(-32), new BigDecimal(115) };
		BigDecimal[] points3 = { new BigDecimal(-33), new BigDecimal(110) };
		points.add(points1);
		points.add(points2);
		points.add(points3);

		regionService.createRegion("Australia", points);
		ArrayList<String> regions = new ArrayList<String>();
		regions.add("Australia");

		List<String> places = new ArrayList<String>();
		places.add("Australia");

    	{
            Survey s = surveyDAO.createSurvey("Test Survey");
            List<Location> locationSet = new ArrayList<Location>();
            Location loc = new Location();
            loc.setUser(getRequestContext().getUser());
            loc.setName("big tree");
            GeometryFactory geomFactory = new GeometryFactory(new PrecisionModel(), 4326);
            loc.setLocation(geomFactory.createPoint(new Coordinate(113, -32)));
            locationSet.add(loc);
            locationDAO.createLocation(loc);
            Location loc1 = new Location();
            loc1.setUser(getRequestContext().getUser());
            loc1.setName("pond");
            loc1.setLocation(geomFactory.createPoint(new Coordinate(152, -41)));
            locationSet.add(loc1);
            locationDAO.createLocation(loc1);
            s.setLocations(locationSet);
            Set<User> users = new HashSet<User>();
            users.addAll(userDAO.getUsers());
            s.setUsers(users);
            Set<IndicatorSpecies> species = new HashSet<IndicatorSpecies>();
            species.addAll(taxaService.getIndicatorSpecies());
            s.setSpecies(species);
            surveyDAO.updateSurvey(s);
    	}

    	{
    	    // Groups
    	    List<User> studentList = new ArrayList<User>();
    	    Set<Group> classSet = new HashSet<Group>();
    	    Set<User> adminSet = new HashSet<User>();
    	    adminSet.add(getRequestContext().getUser());
    	    Group c;
    	    Group g;
    	    User u;
    	    for(String classIndex : new String[]{"A","B","C"}) {

    	        Set<Group> groupSet = new TreeSet<Group>();
    	        for(String groupIndex: new String[]{"P","Q","R"}) {

        	        Set<User> userSet = new HashSet<User>();
        	        for(String userIndex : new String[]{"X","Y","Z"}) {
        	            String lastname = classIndex+groupIndex+userIndex;
        	            String username = "user."+lastname;
        	            u = registrationService.signUp(username,
        	                                           username+"@example.com",
        	                                           "User", lastname, "password");
        	            registrationService.completeRegistration(u.getRegistrationKey());
        	            u.setRoles(new String[]{  Role.USER });
        	            u.setActive(Boolean.TRUE);
        	            userDAO.updateUser(u);
        	            userSet.add(u);
        	            studentList.add(u);
        	        }

        	        g = groupDAO.createGroup("Group "+classIndex+groupIndex);
        	        g.setUsers(userSet);
        	        groupDAO.updateGroup(g);
           	        groupSet.add(g);
    	        }

    	        c = groupDAO.createGroup("Class "+classIndex);
    	        c.setGroups(groupSet);
    	        c.setAdmins(adminSet);
    	        groupDAO.updateGroup(c);
    	        classSet.add(c);
    	    }

    	    // Locations
    	    Location loc = new Location();
            loc.setLocation(locationService.getGeometryFactory().createPoint(new Coordinate(-32, 113)));
            loc.setName("Alpha Site");
            loc.setUser(getRequestContext().getUser());
            locationDAO.save(loc);

            Location loc2 = new Location();
            loc2.setLocation(locationService.getGeometryFactory().createPoint(new Coordinate(-41, 152)));
            loc2.setName("Beta Site");
            loc2.setUser(getRequestContext().getUser());
            locationDAO.save(loc2);

            List<Location> locationSet = new ArrayList<Location>();
            locationSet.add(loc);
            locationSet.add(loc2);

            // Survey
            Set<User> userSet = new HashSet<User>(studentList);
            userSet.add(getRequestContext().getUser());

    	    Survey s = new Survey();
            s.setStartDate(new Date(System.currentTimeMillis()));
            s.setDescription("Test Survey Description");
            s.setName("Test Roundtrip Survey");
            s.setLocations(locationSet);
            s.setUsers(userSet);
            s.setSpecies(new HashSet<IndicatorSpecies>(taxaDAO.getIndicatorSpecies()));
            s.setGroups(classSet);

            surveyDAO.save(s);

            // Records
            Location[] locationArray = locationSet.toArray(new Location[locationSet.size()]);
            for(User student : studentList) {
                for(IndicatorSpecies species : taxaDAO.getIndicatorSpecies()) {
                    int locationIndex = Math.round((float)Math.random() * (float)(locationArray.length-1));
                    Record rec = new Record();
                    rec.setSurvey(s);
                    rec.setSpecies(species);
                    rec.setUser(student);
                    rec.setLocation(locationArray[locationIndex]);
                    rec.setPoint(null);
                    rec.setHeld(true);
                    rec.setWhen(new Date());
                    rec.setTime(rec.getWhen().getTime());
                    rec.setLastDate(new Date());
                    rec.setLastTime(rec.getLastDate().getTime());
                    rec.setNotes("Test Record Notes");
                    rec.setFirstAppearance(false);
                    rec.setLastAppearance(false);
                    rec.setBehaviour("Test Record Behaviour");
                    rec.setHabitat("Test Record Habitat");
                    rec.setNumber(Math.round((float)Math.random() * 5.0f)+1);
                    recordDAO.saveRecord(rec);
                }
            }
    	}

        return "setupData";
    }
}
