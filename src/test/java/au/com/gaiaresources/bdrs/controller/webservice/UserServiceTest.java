/**
 * 
 */
package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.Collection;

import au.com.gaiaresources.bdrs.json.JSON;
import au.com.gaiaresources.bdrs.json.JSONArray;
import au.com.gaiaresources.bdrs.json.JSONObject;
import au.com.gaiaresources.bdrs.json.JSONSerializer;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.group.Group;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * @author timo
 * 
 */
public class UserServiceTest extends AbstractControllerTest {
	
	Logger log = Logger.getLogger(UserServiceTest.class);
	
	@Before
	public void setup() throws Exception{
		 //create a user
		 PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
	     String emailAddr = "user@mailinator.com";
	     String encodedPassword = passwordEncoder.encodePassword("password", null);
	     userDAO.createUser("user", "fn", "ln", emailAddr, encodedPassword, "usersIdent", new String[] { "ROLE_USER" });
	}
	
	
	@Test
	public void testPing() throws Exception {
		request.setMethod("POST");
		request.setRequestURI("/webservice/user/ping.htm");
		handle(request, response);
		Assert.assertEquals("Content type should be text/javascript",
				"text/javascript", response.getContentType());
		Assert.assertEquals("Response from ping should be null({0:1});",
				"null({0:1});", response.getContentAsString());
	}
	
	/**
	 * Tests when user is trying to validate with an existing username and password.
	 * @throws Exception
	 */
	@Test
	public void testValidateUser() throws Exception{
		
		request.setMethod("POST");
		request.setRequestURI("/webservice/user/validate.htm");
		request.addParameter("username", "user");
		request.addParameter("password", "password");
		
		handle(request, response);
		JSONObject validUser = new JSONObject();
		JSONObject validationResponse = JSONObject.fromStringToJSONObject(response.getContentAsString());
		
		if(validationResponse.containsKey("user")){
			validUser = validationResponse.getJSONObject("user");
		}
		Assert.assertEquals("Content type should be application/json", "application/json", response.getContentType());
		Assert.assertEquals("The name of the user should be 'user' and it is not.", "user", validUser.getString("name"));
		Assert.assertEquals("The lastName of the user should be 'ln' and it is not.", "ln", validUser.getString("lastName"));
	}
	
	/**
	 * Tests when user is trying to validate with a username but no password.
	 * @throws Exception
	 */
	@Test
	public void testValidateUser1() throws Exception{
		request.setMethod("POST");
		request.setRequestURI("/webservice/user/validate.htm");
		request.addParameter("username", "user");
		request.addParameter("password", "");
		
		handle(request, response);
		
		// validation error, shouldn't get anything back
		Assert.assertEquals("", response.getContentAsString());	}
	
	/**
	 * Tests when user is trying to validate with a password but no username.
	 * @throws Exception
	 */
	@Test
	public void testValidateUser2() throws Exception{
		request.setMethod("POST");
		request.setRequestURI("/webservice/user/validate.htm");
		request.addParameter("username", "");
		request.addParameter("password", "password");
		
		handle(request, response);
		// validation error, shouldn't get anything back
		Assert.assertEquals("", response.getContentAsString());
	}
	
	//private static final String expectedJson = "{\"total\":\"2\",\"page\":\"1\",\"records\":\"5\",\"rows\":[{\"cellValues\":[\"one\",\"two\",\"three\"],\"id\":1},{\"cellValues\":[\"four\",\"five\",\"six\"],\"id\":2},{\"cellValues\":[\"seven\",\"eight\",\"nine\"],\"id\":3}]}";
	@Test
	public void testJqGridDataBuilder() {
	    JqGridDataBuilder builder = new JqGridDataBuilder(3, 5, 1);
	    builder.addRow(new JqGridDataRow(1).addValue("field1", "1").addValue("field2", "2").addValue("field3", "3"));
	    builder.addRow(new JqGridDataRow(2).addValue("field1", "4").addValue("field2", "4").addValue("field3", "4"));
	    builder.addRow(new JqGridDataRow(3).addValue("field1", "5").addValue("field2", "5").addValue("field3", "5"));
	    //Assert.assertEquals(expectedJson, builder.toJson());
	    String json = builder.toJson();
	    JSONObject obj = JSONObject.fromStringToJSONObject(json);
	    Assert.assertEquals("5", obj.get("records"));
	    JSONArray rows = (JSONArray)obj.get("rows");
	    Assert.assertEquals(3, rows.size());
	    Assert.assertEquals("1", ((JSONObject)rows.get(0)).get("id"));
	    Assert.assertEquals("1", ((JSONObject)rows.get(0)).get("field1"));
	    Assert.assertEquals("2", ((JSONObject)rows.get(0)).get("field2"));
	    Assert.assertEquals("3", ((JSONObject)rows.get(0)).get("field3"));
	    
	    Assert.assertEquals("2", ((JSONObject)rows.get(1)).get("id"));
	    Assert.assertEquals("4", ((JSONObject)rows.get(1)).get("field1"));
	    Assert.assertEquals("4", ((JSONObject)rows.get(1)).get("field2"));
	    Assert.assertEquals("4", ((JSONObject)rows.get(1)).get("field3"));
	}
	
//	@Test
//	public void testGetAllUsers() throws Exception {
//	    request.setMethod("GET");
//            request.setRequestURI("/webservice/user/getUsers.htm");
//            request.addParameter("queryType", "allUsers");
//            
//            handle(request, response);
//            
//            // should get a JSON array of User objects
//            JSON userArr = JSONSerializer.toJSON(response.getContentAsString());
//            Assert.assertTrue(userArr.size() > 0);
//            // convert the JSON to Java Object to ensure it is a list of Users
//            Object javaObj = JSONSerializer.toJava(userArr);
//            Assert.assertTrue(javaObj instanceof Collection<?>);
//            
//            // test for appropriate object type
//            // fails because it is type 'net.sf.ezmorph.bean.MorphDynaBean'
//            // so abandoning this assertion
//            /*Assert.assertTrue(("First value in collection is not instance of '" +
//            		className + 
//            		"' not 'User'!"), ((Collection<?>)javaObj).toArray()[0] instanceof User);
//            */
//	}
//	
//       @Test
//        public void testGetGroupUsers() throws Exception {
//            request.setMethod("GET");
//            request.setRequestURI("/webservice/user/getUsers.htm");
//            request.addParameter("queryType", "group");
//            
//            handle(request, response);
//            
//            // should get a JSON array of Group objects
//            JSON groupArr = JSONSerializer.toJSON(response.getContentAsString());
//            if (groupArr.size() > 0) {
//                // convert the JSON to Java Object to ensure it is a list of Groups
//                Object javaObj = JSONSerializer.toJava(groupArr);
//                Assert.assertTrue(javaObj instanceof Collection<?>);
//                // test for appropriate object type
//                // fails because it is type 'net.sf.ezmorph.bean.MorphDynaBean'
//                // so abandoning this assertion
//                //Assert.assertTrue(((Collection<?>)javaObj).toArray()[0] instanceof Group);
//            }
//        }
//       
//       @Test
//       public void testGetProjectUsers() throws Exception {
//           request.setMethod("GET");
//           request.setRequestURI("/webservice/user/getUsers.htm");
//           request.addParameter("queryType", "project");
//           
//           handle(request, response);
//           
//           // should get a JSON array of Survey objects
//           JSON surveyArr = JSONSerializer.toJSON(response.getContentAsString());
//           if (surveyArr.size() > 0) {
//               // convert the JSON to Java Object to ensure it is a list of Surveys
//               Object javaObj = JSONSerializer.toJava(surveyArr);
//               Assert.assertTrue(javaObj instanceof Collection<?>);
//               // test for appropriate object type
//               // fails because it is type 'net.sf.ezmorph.bean.MorphDynaBean'
//               // so abandoning this assertion
//               //Assert.assertTrue(((Collection<?>)javaObj).toArray()[0] instanceof Survey);
//           }
//       }
}
