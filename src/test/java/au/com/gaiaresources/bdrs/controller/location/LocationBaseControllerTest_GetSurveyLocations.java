package au.com.gaiaresources.bdrs.controller.location;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Test;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.security.Role;

public class LocationBaseControllerTest_GetSurveyLocations extends
        AbstractGridControllerTest {

    @Test
    public void testGetSurveyLocationsForUserService() throws Exception {
        // aka survey1
        Survey currentSurvey = surveyDAO.getSurveyByName("Fictionay Animal Survey");
        
        request.setMethod("GET");
        request.setRequestURI(LocationBaseController.GET_SURVEY_LOCATIONS_FOR_USER);
        request.setParameter(LocationBaseController.PARAM_SURVEY_ID, currentSurvey.getId().toString());
        this.login("admin", "password", new String[] { Role.ADMIN });
        
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        
        // we expect 5 items back. see LocationDAOImplTest.testGetSurveyLocations
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
    }
}
