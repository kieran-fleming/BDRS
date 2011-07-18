package au.com.gaiaresources.bdrs.controller.survey;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.security.Role;

public class SurveyBaseControllerTest extends AbstractControllerTest {
    
    @Autowired
    private SurveyDAO surveyDAO;

    @Test
    public void testListSurveys() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/listing.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "surveyListing");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyList");
    }

    @Test
    public void testAddSurvey() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/edit.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "surveyEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "publish");
    }

    
    /**
     * Tests the basic use case of creating a new survey and clicking save.
     */
    @Test
    public void testAddSurveySubmitWideLogo() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/edit.htm");

        BufferedImage wideImage = new BufferedImage(1024, 40, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D)wideImage.getGraphics();
        g2.setColor(Color.GREEN);
        g2.fillRect(0,0,wideImage.getWidth(), wideImage.getHeight());
        File imgTmp = File.createTempFile("SurveyBaseControllerTest.testAddSurveySubmit", ".png");
        ImageIO.write(wideImage, "png", imgTmp);
        
        MockMultipartFile logoFile = new MockMultipartFile(Metadata.SURVEY_LOGO+"_file", imgTmp.getName(), "image/png", new FileInputStream(imgTmp));
        MockMultipartHttpServletRequest multipartRequest = (MockMultipartHttpServletRequest)request;
        multipartRequest.addFile(logoFile);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "Test Survey 1234");
        params.put("description", "This is a test survey");
        params.put("surveyDate", "10 Nov 2010");
        params.put("SurveyLogo", "");
        params.put("rendererType", "DEFAULT");
        params.put(Metadata.SURVEY_LOGO, imgTmp.getName());
        request.setParameters(params);
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/listing.htm", redirect.getUrl());
        
        Survey survey = surveyDAO.getSurveyByName(params.get("name"));
        Assert.assertEquals(survey.getName(), params.get("name"));
        Assert.assertEquals(survey.getDescription(), params.get("description"));
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2010, 10, 10, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(survey.getStartDate().getTime(), cal.getTime().getTime());
        
        Metadata md = survey.getMetadataByKey(Metadata.SURVEY_LOGO);
        Assert.assertEquals(imgTmp.getName(), md.getValue());
    }
    
    /**
     * Tests the basic use case of creating a new survey and clicking saveAndContinue.
     */
    @Test
    public void testAddSurveySubmitTallLogo() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/edit.htm");

        BufferedImage tallImage = new BufferedImage(40, 1024, BufferedImage.TYPE_INT_RGB);
        Graphics2D g2 = (Graphics2D)tallImage.getGraphics();
        g2.setColor(Color.GREEN);
        g2.fillRect(0,0,tallImage.getWidth(), tallImage.getHeight());
        File imgTmp = File.createTempFile("SurveyBaseControllerTest.testAddSurveySubmit", ".png");
        ImageIO.write(tallImage, "png", imgTmp);
        
        MockMultipartFile logoFile = new MockMultipartFile(Metadata.SURVEY_LOGO+"_file", imgTmp.getName(), "image/png", new FileInputStream(imgTmp));
        MockMultipartHttpServletRequest multipartRequest = (MockMultipartHttpServletRequest)request;
        multipartRequest.addFile(logoFile);
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("name", "Test Survey 1234");
        params.put("description", "This is a test survey");
        params.put("surveyDate", "10 Nov 2010");
        params.put("SurveyLogo", "");
        params.put("rendererType", "DEFAULT");
        params.put("saveAndContinue", "saveAndContinue");
        params.put(Metadata.SURVEY_LOGO, imgTmp.getName());
        request.setParameters(params);
        
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/editTaxonomy.htm", redirect.getUrl());
        
        Survey survey = surveyDAO.getSurveyByName(params.get("name"));
        Assert.assertEquals(survey.getName(), params.get("name"));
        Assert.assertEquals(survey.getDescription(), params.get("description"));
        Calendar cal = GregorianCalendar.getInstance();
        cal.set(2010, 10, 10, 0, 0, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Assert.assertEquals(survey.getStartDate().getTime(), cal.getTime().getTime());
        
        Metadata md = survey.getMetadataByKey(Metadata.SURVEY_LOGO);
        Assert.assertEquals(imgTmp.getName(), md.getValue());
    }

    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}
