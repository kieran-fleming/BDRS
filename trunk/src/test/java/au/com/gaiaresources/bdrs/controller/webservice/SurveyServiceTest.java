package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.org.ala.web.filter.JsonpFilter;

public class SurveyServiceTest extends AbstractControllerTest {
	 private Logger log = Logger.getLogger(getClass());
	 @Autowired
	 private UserDAO userDAO;
	 @Autowired
	 private SurveyDAO surveyDAO;
	 
	 @Before
	 public void setup() throws Exception {
		 //create a user
		 PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	     String emailAddr = "user@mailinator.com";
	     String encodedPassword = passwordEncoder.encodePassword("password", null);
	   
	     User u = userDAO.createUser("user", "fn", "ln", emailAddr, encodedPassword, "usersIdent", new String[] { "ROLE_USER" });
	     Set<User> users = new HashSet<User>();
	     users.add(u);
	     //create a survey for the user
	     Survey survey = new Survey();
	     survey.setActive(true);
	     survey.setName("Test survey");
	     survey.setDescription("This is a survey used for testing");
	     survey.setUsers(users);
	     surveyDAO.save(survey);
		 //log the user in
	     login("user", "password", new String[] { "ROLE_USER" });
	 }
	
	 
	@Test
	public void testSurveysForUser() throws Exception{
	 	request.setMethod("GET");
		request.setRequestURI("/webservice/survey/surveysForUser.htm");
		User user = userDAO.getUserByEmailAddress("user@mailinator.com");
		request.setParameter("ident", user.getRegistrationKey());
		handle(request, response);
		//tests if the registration key in the request is correct
		Assert.assertEquals("Request parameter ident should be user",
				"usersIdent", request.getParameter("ident"));
		//tests if the response is of type json
		Assert.assertEquals("Content type should be application/json",
				"application/json", response.getContentType());
		//tests if the response contains a survey with the name 'Test survey'
		JSONArray responseContent = JSONArray.fromObject(response.getContentAsString());
		JSONObject survey = responseContent.getJSONObject(0);
		Assert.assertEquals("The survey name should be 'Test survey'.","Test survey",survey.getString("name"));
	}

}
