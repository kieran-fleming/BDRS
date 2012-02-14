package au.com.gaiaresources.bdrs.controller.webservice;

import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.group.GroupDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;

public class SurveyServiceTest extends AbstractControllerTest {
	 private Logger log = Logger.getLogger(getClass());
	 @Autowired
	 private UserDAO userDAO;
	 @Autowired
	 private SurveyDAO surveyDAO;
	 @Autowired
	 private GroupDAO groupDAO;
	 private static final String GROUP_NAME = "Group Name";
	 private static final String SURVEY_1_NAME = "Survey 1";
	 private static final String SURVEY_2_NAME = "Survey 2";
	 
	 private Group group;
	 private Survey survey1;
	 private Survey survey2;
	 private User u;
	 
	 @Before
	 public void setup() throws Exception {
		 //create a user
		 PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	     String emailAddr = "user@mailinator.com";
	     String encodedPassword = passwordEncoder.encodePassword("password", null);
	     u = userDAO.createUser("user", "fn", "ln", emailAddr, encodedPassword, "usersIdent", new String[] { "ROLE_USER" });
	     //create a survey for the user
	     survey1 = surveyDAO.createSurvey(SURVEY_1_NAME);
	     survey1.setDescription("This is a survey used for testing");
	     survey1.getUsers().add(u);
	     surveyDAO.save(survey1);
		 //log the user in
	     login("user", "password", new String[] { Role.USER});
	     group = groupDAO.createGroup(GROUP_NAME);
	     group.getUsers().add(u);
	     groupDAO.updateGroup(group);
	     survey1.getGroups().add(group);
	     surveyDAO.updateSurvey(survey1);
	     survey2 = surveyDAO.createSurvey(SURVEY_2_NAME);
	     survey2.setPublic(true);
	     survey2 = surveyDAO.updateSurvey(survey2);
	 }
	
	 
	@Test
	public void testSurveysForUser() throws Exception{
	 	request.setMethod("GET");
		request.setRequestURI("/webservice/survey/surveysForUser.htm");
		User user = userDAO.getUserByEmailAddress("user@mailinator.com");
		request.setParameter(SurveyService.AUTHORISATION_PARAMETER, user.getRegistrationKey());
		handle(request, response);
		//tests if the registration key in the request is correct
		Assert.assertEquals("Request parameter" + SurveyService.AUTHORISATION_PARAMETER + " ident should be user",
				"usersIdent", request.getParameter(SurveyService.AUTHORISATION_PARAMETER));
		//tests if the response is of type json
		Assert.assertEquals("Content type should be application/json",
				"application/json", response.getContentType());
		//tests if the response contains a survey with the name 'Test survey'
		JSONArray responseContent = JSONArray.fromString(response.getContentAsString());
		//We should get both users surveys back
		Assert.assertEquals("Returned json array size should match. We should get all the surveys created for the user back", 2, responseContent.size());
		JSONObject survey = responseContent.getJSONObject(0);
		Assert.assertEquals("The survey name should be equal.", SURVEY_1_NAME, survey.getString("name"));
	}
	
        @Test
        public void testSurveysForUserWithGroupId() throws Exception{
                request.setMethod("GET");
                request.setRequestURI("/webservice/survey/surveysForUser.htm");
                User user = userDAO.getUserByEmailAddress("user@mailinator.com");
                request.setParameter(SurveyService.AUTHORISATION_PARAMETER, user.getRegistrationKey());
                request.setParameter(SurveyService.GROUP_ID_PARAMETER, group.getId().toString());
                handle(request, response);
                // tests if the registration key in the request is correct
                Assert.assertEquals("Request authorisation parameter should be should match",
                                "usersIdent", request.getParameter(SurveyService.AUTHORISATION_PARAMETER));
                // tests if the response is of type json
                Assert.assertEquals("Content type should be application/json",
                                "application/json", response.getContentType());
                // Tests if the groupId parameter was passed
                Assert.assertEquals("groupId should be the value passed", group.getId().toString(), request.getParameter(SurveyService.GROUP_ID_PARAMETER));
                // tests if the response contains a survey with the name 'Test survey'
                JSONArray responseContent = JSONArray.fromString(response.getContentAsString());
                // We should get both users surveys back
                Assert.assertEquals("Returned json array size should match", 1, responseContent.size());
                JSONObject survey = responseContent.getJSONObject(0);
                Assert.assertEquals("The survey name should be equal",SURVEY_1_NAME, survey.getString("name"));
        }
}
