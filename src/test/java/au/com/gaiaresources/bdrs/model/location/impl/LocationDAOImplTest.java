package au.com.gaiaresources.bdrs.model.location.impl;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Test;

import au.com.gaiaresources.bdrs.controller.AbstractGridControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;

public class LocationDAOImplTest extends AbstractGridControllerTest {
    
    private Logger log = Logger.getLogger(getClass());
    
    @Test
    public void testGetSurveyLocations() {
        User admin = userDAO.getUser("admin");
        // aka survey1
        Survey currentSurvey = surveyDAO.getSurveyByName("Fictionay Animal Survey");
        PagedQueryResult<Location> adminResults = locationDAO.getSurveylocations(null, admin, currentSurvey.getId().intValue());
        
        Assert.assertEquals("wrong number of locations returned", allSurveyLocationList.size() - currentSurvey.getLocations().size(), adminResults.getCount());
    }
    
    @Test
    public void testGetSurveyLocationsRestricted() {
        // we know that this user 'foreverAlone' has NO access to any private surveys.
        
        // aka survey1
        Survey currentSurvey = surveyDAO.getSurveyByName("Fictionay Animal Survey");
        PagedQueryResult<Location> adminResults = locationDAO.getSurveylocations(null, foreverAlone, currentSurvey.getId().intValue());
        
        Assert.assertEquals("wrong number of locations returned", 0, adminResults.getCount());
    }
}
