package au.com.gaiaresources.bdrs.controller.test;

import java.util.List;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.db.DeleteCascadeHandler;
import au.com.gaiaresources.bdrs.service.db.DeletionService;

@Controller
public class TestDataController extends AbstractController {
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());
    
    @Autowired
    private SpeciesProfileDAO speciesProfileDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private DeletionService deletionService;
    @Autowired
    private UserDAO userDAO;
    
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/testdata/dashboard.htm", method = RequestMethod.GET)
    public ModelAndView setup(HttpServletRequest request,
                            HttpServletResponse response) {
        
        ModelAndView mv = new ModelAndView("testDataDashboard");
        return mv;
    }
    
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/testdata/dashboard.htm", method = RequestMethod.POST)
    public String createTestData(HttpServletRequest request,
                            HttpServletResponse response,
                            @RequestParam(value="taxongroup", required=true) int taxonGroupCount,
                            @RequestParam(value="taxongroup_random", required=true) int taxonGroupRandom,
                            @RequestParam(value="taxongroupattributes", required=false, defaultValue="false") boolean taxonGroupAttributes,
                            @RequestParam(value="taxa", required=true) int taxaCount,
                            @RequestParam(value="taxa_random", required=true) int taxaRandom,
                            @RequestParam(value="taxonprofile", required=false, defaultValue="false") boolean taxonProfile,
                            @RequestParam(value="survey", required=true) int surveyCount,
                            @RequestParam(value="survey_random", required=true) int surveyRandom,
                            @RequestParam(value="testusercount", required=true) int userCount) throws Exception {
        
        TestDataCreator testDataCreator = new TestDataCreator(getRequestContext().getApplicationContext());
        testDataCreator.createTestUsers(userCount, 0);
        testDataCreator.createTaxonGroups(taxonGroupCount, taxonGroupRandom, taxonGroupAttributes);
        testDataCreator.createTaxa(taxaCount, taxaRandom);
        if(taxonProfile) {
            testDataCreator.createTaxonProfile();
        }
        testDataCreator.createSurvey(surveyCount, surveyRandom);
        testDataCreator.createBasicSurvey();
        
        return getRedirectHome();
    }
    
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/testdata/clearData.htm", method = RequestMethod.POST)
    public String clearTestData(HttpServletRequest request,
                            HttpServletResponse response) {
        
        List<SpeciesProfile> profileList = 
            speciesProfileDAO.getSpeciesProfileByType(SpeciesProfile.SPECIES_PROFILE_FILE_TYPE_VALUES);
        for(SpeciesProfile profile : profileList) {
            deletionService.deleteManagedFileByUUID(profile.getContent());
        }

        DeleteCascadeHandler groupHandler = deletionService.getDeleteCascadeHandlerFor(TaxonGroup.class);
        for(TaxonGroup group : taxaDAO.getTaxonGroups()) {
            groupHandler.deleteCascade(group);
        }
        
        DeleteCascadeHandler surveyHandler = deletionService.getDeleteCascadeHandlerFor(Survey.class);
        for(Survey survey : surveyDAO.getSurveys(getRequestContext().getUser())) {
            surveyHandler.deleteCascade(survey);
        }
        
        DeleteCascadeHandler userHandler = deletionService.getDeleteCascadeHandlerFor(User.class);
        List<User> allUsers = userDAO.getUsers();
        for (User u : allUsers) {
            if (!u.getName().equals("root") && !u.getName().equals("admin")) {
                userHandler.deleteCascade(u);
            }
        }
        return getRedirectHome();
    }
}
