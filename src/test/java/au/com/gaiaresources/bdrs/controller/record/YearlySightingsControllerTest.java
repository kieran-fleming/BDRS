package au.com.gaiaresources.bdrs.controller.record;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordAttributeFormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyFormField;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Tests all aspects of the <code>YearlySightingsControllerTest</code>.
 */
public class YearlySightingsControllerTest extends RecordFormTest {

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private RecordDAO recordDAO;
    @Autowired 
    private MetadataDAO metadataDAO;
    @Autowired
    private LocationDAO locationDAO;
    @Autowired
    private LocationService locationService;
    @Autowired
    private RedirectionService redirectionService;

    private Survey survey;
    private TaxonGroup taxonGroup;
    private IndicatorSpecies speciesA;
    private IndicatorSpecies speciesB;
    private Location locationA;
    private Location locationB;

    @Before
    public void setUp() throws Exception {
        taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);
        
        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);
        
        speciesB = new IndicatorSpecies();
        speciesB.setCommonName("Indicator Species B");
        speciesB.setScientificName("Indicator Species B");
        speciesB.setTaxonGroup(taxonGroup);
        speciesB = taxaDAO.save(speciesB);
        
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for(AttributeType attrType : AttributeType.values()) {
            for(AttributeScope scope : new AttributeScope[] { AttributeScope.RECORD, AttributeScope.SURVEY, null }) {
                
                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(scope);
                attr.setTag(false);
                
                if(AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for(int i=0; i<4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        opt = taxaDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }
                
                attr = taxaDAO.save(attr);
                attributeList.add(attr);
            }
        }
        
        HashSet<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
        speciesSet.add(speciesA);

        survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA);
        metadataDAO.save(md);
        survey.setAttributes(attributeList);
        survey.setSpecies(speciesSet);
        survey = surveyDAO.save(survey);
        
        User admin = userDAO.getUser("admin");
        
        locationA = new Location();
        locationA.setName("Location A");        
        locationA.setUser(admin);
        locationA.setLocation(locationService.createPoint(-40.58, 153.1));
        locationDAO.save(locationA);
        
        locationB = new Location();
        locationB.setName("Location B");        
        locationB.setUser(admin);
        locationB.setLocation(locationService.createPoint(-32.58, 154.2));
        locationDAO.save(locationB);
    }
    
    @Test
    public void testAddRecord() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/yearlySightings.htm");
        request.setParameter("surveyId", survey.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "yearlySightings");
        
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "species");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "locations");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "dateMatrix");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "today");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "location");
        Assert.assertNull(mv.getModelMap().get("location"));
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");
    }
    
    @Test
    public void testAddRecordWithRecordId() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Set<RecordAttribute> attributeSet = new HashSet<RecordAttribute>();
        Map<Attribute, RecordAttribute> expectedRecordAttrMap = new HashMap<Attribute, RecordAttribute>();
        for(Attribute attr : survey.getAttributes()) {
            RecordAttribute recAttr = new RecordAttribute();
            recAttr.setAttribute(attr);
            switch (attr.getType()) {
                case INTEGER:
                    Integer i = new Integer(123);
                    recAttr.setNumericValue(new BigDecimal(i));
                    recAttr.setStringValue(i.toString());
                    break;
                case DECIMAL:
                    Double d = new Double(123);
                    recAttr.setNumericValue(new BigDecimal(d));
                    recAttr.setStringValue(d.toString());
                    break;
                case DATE:
                    Date date = new Date(System.currentTimeMillis());
                    recAttr.setDateValue(date);
                    recAttr.setStringValue(dateFormat.format(date));
                    break;
                case STRING_AUTOCOMPLETE:
                case STRING:
                    recAttr.setStringValue("This is a test string record attribute");
                    break;
                case TEXT:
                    recAttr.setStringValue("This is a test text record attribute");
                    break;
                case STRING_WITH_VALID_VALUES:
                    recAttr.setStringValue(attr.getOptions().iterator().next().getValue());
                    break;
                case FILE:
                    recAttr.setStringValue("testDataFile.dat");
                    break;
                case IMAGE:
                    recAttr.setStringValue("testImgFile.png");
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "+attr.getType().toString(), false);
                    break;
            }
            
            recAttr = recordDAO.saveRecordAttribute(recAttr);
            attributeSet.add(recAttr);
            expectedRecordAttrMap.put(attr, recAttr);
        }
        
        // Add a record to the Survey
        Record record = new Record();
        record.setSurvey(survey);
        record.setSpecies(speciesA);
        record.setUser(getRequestContext().getUser());
        record.setLocation(locationA);
        record.setHeld(false);
        Date now = new Date();
        record.setWhen(now);
        record.setTime(now.getTime());
        record.setLastDate(now);
        record.setLastTime(now.getTime());
        record.setNotes("This is a test record");
        record.setFirstAppearance(false);
        record.setLastAppearance(false);
        record.setBehaviour("Eating a muffin");
        record.setHabitat("By my foot");
        record.setNumber(1);
        record.setAttributes(attributeSet);
        // record.setReviewRequests()
        // record.setMetadata()
        record = recordDAO.saveRecord(record);

        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/yearlySightings.htm");
        request.setParameter("surveyId", survey.getId().toString());
        request.setParameter("recordId", record.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "yearlySightings");
        
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "species");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "locations");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "dateMatrix");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "today");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "location");
        Assert.assertEquals(record.getLocation(), mv.getModelMap().get("location"));
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");
        
        for(FormField formField : ((List<FormField>)mv.getModelMap().get("formFieldList"))) {
            if(formField.isAttributeFormField()) {
                RecordAttributeFormField attributeField = (RecordAttributeFormField)formField;
                
                Assert.assertEquals(record, attributeField.getRecord());
                Assert.assertEquals(survey, attributeField.getSurvey());
            }
            else if(formField.isPropertyFormField()) {
                RecordPropertyFormField propertyField = (RecordPropertyFormField)formField;
                Assert.assertEquals(record, propertyField.getRecord());
                Assert.assertEquals(survey, propertyField.getSurvey());
            }
            else {
                Assert.assertTrue(false);
            }
        }
    }
    
    @Test
    public void testSubmitRecord() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/yearlySightings.htm");
        
        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());
        params.put("locationId", locationA.getId().toString());
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        
        Date today = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));

        String key;
        String value;
        for (Attribute attr : survey.getAttributes()) {
            if(AttributeScope.SURVEY.equals(attr.getScope())) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
                value = new String();
    
                switch (attr.getType()) {
                    case INTEGER:
                        value = "123";
                        break;
                    case DECIMAL:
                        value = "456.7";
                        break;
                    case DATE:
                        value = dateFormat.format(today);
                        break;
                    case STRING_AUTOCOMPLETE:
                    case STRING:
                        value = "Test Survey Attr String";
                        break;
                    case TEXT:
                        value = "Test Survey Attr Text";
                        break;
                    case STRING_WITH_VALID_VALUES:
                        value = attr.getOptions().iterator().next().getValue();
                        break;
                    case FILE:
                        String file_filename = String.format("attribute_%d", attr.getId());
                        MockMultipartFile mockFileFile = new MockMultipartFile(key, file_filename, "audio/mpeg", file_filename.getBytes());
                        ((MockMultipartHttpServletRequest)request).addFile(mockFileFile);
                        value = file_filename;
                        break;
                    case IMAGE:
                        String image_filename = String.format("attribute_%d", attr.getId());
                        MockMultipartFile mockImageFile = new MockMultipartFile(key, image_filename, "image/png", image_filename.getBytes());
                        ((MockMultipartHttpServletRequest)request).addFile(mockImageFile);
                        value = image_filename;
                        break;
                    default:
                        Assert.assertTrue("Unknown Attribute Type: "+attr.getType().toString(), false);
                        break;
                }
                params.put(key, value);
            }
        }

        // Fill in 300 out of the possible 365/366 days of the year with the
        // index of that day in the year.
        Calendar cal = new GregorianCalendar();
        cal.setTime(new Date());
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);

        Set<Integer> sightingSet = new HashSet<Integer>();
        for(int i=0; i<300; i++) {
            Integer sighting = new Integer(cal.get(Calendar.DAY_OF_YEAR));
            sightingSet.add(sighting);
            params.put(String.format("date_%d", cal.getTimeInMillis()), sighting.toString());
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        request.setParameters(params);
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(redirectionService.getMySightingsUrl(survey), redirect.getUrl());
        
        Assert.assertEquals(300, recordDAO.countAllRecords().intValue());
        for(Record rec : recordDAO.getRecords(getRequestContext().getUser(), survey, locationA)) {
            // Assert that this was a sighting we added
            Assert.assertTrue(sightingSet.remove(rec.getNumber()));
            cal.setTime(rec.getWhen());
            // Test that the sighting was for the correct day.
            Assert.assertEquals(rec.getNumber().intValue(), cal.get(Calendar.DAY_OF_YEAR));
            
            for(AttributeValue recAttr: rec.getAttributes()) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", recAttr.getAttribute().getId());
                switch (recAttr.getAttribute().getType()) {
                    case INTEGER:
                        Assert.assertEquals(Integer.parseInt(params.get(key)), recAttr.getNumericValue().intValue());
                        break;
                    case DECIMAL:
                        Assert.assertEquals(Double.parseDouble(params.get(key)), recAttr.getNumericValue().doubleValue());
                        break;
                    case DATE:
                        Assert.assertEquals(today, recAttr.getDateValue());
                        break;
                    case STRING_AUTOCOMPLETE:
                    case STRING:
                    case TEXT:
                        Assert.assertEquals(params.get(key), recAttr.getStringValue());
                        break;
                    case STRING_WITH_VALID_VALUES:
                        Assert.assertEquals(params.get(key), recAttr.getStringValue());
                        break;
                    case FILE:
                    case IMAGE:
                        Assert.assertEquals(params.get(key), recAttr.getStringValue());
                        break;
                    default:
                        Assert.assertTrue("Unknown Attribute Type: "+recAttr.getAttribute().getType().toString(), false);
                        break;
                }
            }
        }
        
        // All records accounted for.
        Assert.assertTrue(sightingSet.isEmpty());
    }
    
    @Test
    public void testRecordFormPredefinedLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/yearlySightings.htm", true, SurveyFormRendererType.YEARLY_SIGHTINGS, true);
    }
    
    @Test
    public void testRecordFormLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/yearlySightings.htm", false, SurveyFormRendererType.YEARLY_SIGHTINGS, true);
    }
    
    @Test
    public void testRecordFormPredefinedLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/yearlySightings.htm", true, SurveyFormRendererType.YEARLY_SIGHTINGS, false);
    }
    
    @Test
    public void testRecordFormLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/yearlySightings.htm", false, SurveyFormRendererType.YEARLY_SIGHTINGS, false);
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}