package au.com.gaiaresources.bdrs.model.survey.impl;

import java.util.Calendar;

import junit.framework.Assert;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormSubmitAction;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

public class SurveyTest extends AbstractTransactionalTest {

    @Autowired
    private MetadataDAO metaDAO;
    @Autowired 
    private SurveyDAO surveyDAO;
    
    private Survey survey;
    
    private Logger log = Logger.getLogger(getClass());
    
    @Before
    public void setup() {
        survey = new Survey();
        survey.setName("survey name");
        survey.setDescription("survey description");
        
        Calendar cal = Calendar.getInstance();
        cal.set(2007, 10, 10, 10, 10, 10);
        survey.setStartDate(cal.getTime());
        
        survey = surveyDAO.save(survey);
    }
    
    @Test
    public void testSurveyFormActionType() {
        Assert.assertEquals("expected value mismatch", Survey.DEFAULT_SURVEY_FORM_SUBMIT_ACTION, survey.getFormSubmitAction());
        
        survey.setFormSubmitAction(SurveyFormSubmitAction.STAY_ON_FORM, metaDAO);
        surveyDAO.save(survey);
        
        Assert.assertEquals("expected value mismatch", SurveyFormSubmitAction.STAY_ON_FORM, survey.getFormSubmitAction());
    }
}
