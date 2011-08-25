package au.com.gaiaresources.bdrs.controller.record;

import java.util.Calendar;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.deserialization.record.RecordKeyLookup;
import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.security.Role;

import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;

public class TrackerController_Wkt_Test extends AbstractControllerTest {

    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    CensusMethodDAO cmDAO;
    @Autowired
    RecordDAO recDAO;
    @Autowired
    MetadataDAO metadataDAO;
    
    GeometryBuilder gb;
    
    Survey survey;
    CensusMethod cm;
    
    @Rule
    public ExpectedException thrown = ExpectedException.none();
    
    @Before
    public void setup() throws Exception {
        gb = new GeometryBuilder();
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 6, 2);
        
        cm = new CensusMethod();
        cm.setTaxonomic(Taxonomic.NONTAXONOMIC);
        cm.setName("cm name");
        cm.setDescription("cm desc");
        
        cmDAO.save(cm);
        
        survey = new Survey();
        survey.setName("my survey");
        survey.setDescription("survey desc");
        survey.setStartDate(cal.getTime());
        
        // piggy backing another test here...
        survey.setDefaultRecordVisibility(RecordVisibility.CONTROLLED, metadataDAO);
        
        survey.getCensusMethods().add(cm);
        
        surveyDAO.save(survey);
        
        login("admin", "password", new String[] { Role.ADMIN });
    }
    
    @Test
    public void testPointWkt() throws Exception {
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 7, 2);
        
        Geometry geom = gb.createPoint(-10, -20);
        
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_CENSUS_METHOD_ID, cm.getId().toString());
        request.setParameter(TrackerController.PARAM_DATE, "02 Aug 2011 12:45");
        request.setParameter(TrackerController.PARAM_TIME_HOUR, "15");
        request.setParameter(TrackerController.PARAM_TIME_MINUTE, "48");
        request.setParameter(TrackerController.PARAM_NOTES, "my notes");
        request.setParameter(TrackerController.PARAM_WKT, geom.toText());
        
        handle(request, response);
        
        Assert.assertEquals(1, recDAO.countAllRecords().intValue());
        Record rec = recDAO.search(null, null, null).getList().get(0);
        
        Assert.assertTrue("should be a point", rec.getGeometry() instanceof Point);
        Assert.assertEquals(-10d, rec.getGeometry().getCentroid().getX());
        Assert.assertEquals(-20d, rec.getGeometry().getCentroid().getY());
        Assert.assertEquals(RecordVisibility.CONTROLLED, rec.getRecordVisibility());
    }
    
    @Test
    public void testPolygonWkt() throws Exception {
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 7, 2);
        
        Geometry geom = gb.createSquare(-10, -10, 10);
        
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_CENSUS_METHOD_ID, cm.getId().toString());
        request.setParameter(TrackerController.PARAM_DATE, "02 Aug 2011 12:45");
        request.setParameter(TrackerController.PARAM_TIME_HOUR, "15");
        request.setParameter(TrackerController.PARAM_TIME_MINUTE, "48");
        request.setParameter(TrackerController.PARAM_NOTES, "my notes");
        request.setParameter(TrackerController.PARAM_WKT, geom.toText());
        request.setParameter(TrackerController.PARAM_RECORD_VISIBILITY, RecordVisibility.OWNER_ONLY.toString());
        
        handle(request, response);
        
        Assert.assertEquals(1, recDAO.countAllRecords().intValue());
        Record rec = recDAO.search(null, null, null).getList().get(0);
        
        Assert.assertTrue("should be a multi polygon", rec.getGeometry() instanceof MultiPolygon);
        
        Assert.assertEquals(-5d, rec.getGeometry().getCentroid().getX());
        Assert.assertEquals(-5d, rec.getGeometry().getCentroid().getY());
        Assert.assertEquals(RecordVisibility.OWNER_ONLY, rec.getRecordVisibility());
    }
    
    @Test
    public void testLineWkt() throws Exception {
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 7, 2);
        
        Geometry geom = gb.createLine(1,2,3,4);
        
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_CENSUS_METHOD_ID, cm.getId().toString());
        request.setParameter(TrackerController.PARAM_DATE, "02 Aug 2011 12:45");
        request.setParameter(TrackerController.PARAM_TIME_HOUR, "15");
        request.setParameter(TrackerController.PARAM_TIME_MINUTE, "48");
        request.setParameter(TrackerController.PARAM_NOTES, "my notes");
        request.setParameter(TrackerController.PARAM_WKT, geom.toText());
        request.setParameter(TrackerController.PARAM_RECORD_VISIBILITY, RecordVisibility.PUBLIC.toString());
        
        handle(request, response);
        
        Assert.assertEquals(1, recDAO.countAllRecords().intValue());
        Record rec = recDAO.search(null, null, null).getList().get(0);
        
        Assert.assertTrue("should be a multi line string", rec.getGeometry() instanceof MultiLineString);
        Assert.assertEquals(RecordVisibility.PUBLIC, rec.getRecordVisibility());
    }
    
    @Test
    public void testInvalidPolyWkt() throws Exception {
        request.setRequestURI(TrackerController.EDIT_URL);
        request.setMethod("POST");
        
        // a self intersecting wkt string
        String wktString = "POLYGON((135.44531250005 -35.427036840141,124.19531250049 -27.957942082073,113.12109375093 -41.477654408167,134.74218750007 -26.55160357215,135.44531250005 -35.427036840141))";
        
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(2011, 7, 2);
        
        request.setParameter(TrackerController.PARAM_SURVEY_ID, survey.getId().toString());
        request.setParameter(TrackerController.PARAM_CENSUS_METHOD_ID, cm.getId().toString());
        request.setParameter(TrackerController.PARAM_DATE, "02 Aug 2011 12:45");
        request.setParameter(TrackerController.PARAM_TIME_HOUR, "15");
        request.setParameter(TrackerController.PARAM_TIME_MINUTE, "48");
        request.setParameter(TrackerController.PARAM_NOTES, "my notes");
        request.setParameter(TrackerController.PARAM_WKT, wktString);

        ModelAndView mv = handle(request, response);
        
        Map<String, String> errorMap = (Map<String, String>)mv.getModel().get(TrackerController.MV_ERROR_MAP);
        
        Assert.assertNotNull("errorMap should not be null", errorMap);
        
        RecordKeyLookup klu = new TrackerFormRecordKeyLookup();
        
        Assert.assertTrue("Error for lat should not be empty", StringUtils.hasLength(errorMap.get(klu.getLatitudeKey())));
        Assert.assertTrue("Error for long should not be empty", StringUtils.hasLength(errorMap.get(klu.getLongitudeKey())));
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}
