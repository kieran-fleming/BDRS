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

import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordAttributeFormField;
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
 * Tests all aspects of the <code>TrackerController</code>.
 */
public class TrackerControllerTest extends RecordFormTest {

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
    private TaxonGroup taxonGroupBirds;
    private TaxonGroup taxonGroupFrogs;    
    private IndicatorSpecies speciesA;
    private IndicatorSpecies speciesB;
    private Location locationA;
    private Location locationB;

    @Before
    public void setUp() throws Exception {
        taxonGroupBirds = new TaxonGroup();
        taxonGroupBirds.setName("Birds");
        taxonGroupBirds = taxaDAO.save(taxonGroupBirds);
        
        taxonGroupFrogs = new TaxonGroup();
        taxonGroupFrogs.setName("Frogs");
        taxonGroupFrogs = taxaDAO.save(taxonGroupBirds);
        
        List<Attribute> taxonGroupAttributeList;
        Attribute groupAttr;
        for (TaxonGroup group : new TaxonGroup[] { taxonGroupBirds, taxonGroupFrogs }) {
            taxonGroupAttributeList = new ArrayList<Attribute>();
            for(boolean isTag : new boolean[] { true, false }) {
                for (AttributeType attrType : AttributeType.values()) {
                    groupAttr = new Attribute();
                    groupAttr.setRequired(true);
                    groupAttr.setName(group.getName() + "_" + attrType.toString()+"_isTag"+isTag);
                    groupAttr.setDescription(group.getName() + "_" + attrType.toString()+"_isTag"+isTag);
                    groupAttr.setTypeCode(attrType.getCode());
                    groupAttr.setScope(null);
                    groupAttr.setTag(isTag);
    
                    if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                        List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                        for (int i = 0; i < 4; i++) {
                            AttributeOption opt = new AttributeOption();
                            opt.setValue(String.format("Option %d", i));
                            opt = taxaDAO.save(opt);
                            optionList.add(opt);
                        }
                        groupAttr.setOptions(optionList);
                    }
    
                    groupAttr = taxaDAO.save(groupAttr);
                    taxonGroupAttributeList.add(groupAttr);
                }
            }
            group.setAttributes(taxonGroupAttributeList);
            taxaDAO.save(group);
        }
        
        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroupBirds);
        speciesA = taxaDAO.save(speciesA);
        
        speciesB = new IndicatorSpecies();
        speciesB.setCommonName("Indicator Species B");
        speciesB.setScientificName("Indicator Species B");
        speciesB.setTaxonGroup(taxonGroupBirds);
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
        speciesSet.add(speciesB);

        survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
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
        request.setRequestURI("/bdrs/user/tracker.htm");
        request.setParameter("surveyId", survey.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "tracker");
        
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "record");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyFormFieldList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroupFormFieldList");

        Assert.assertFalse((Boolean)mv.getModelMap().get("preview"));
        Assert.assertEquals(survey.getAttributes().size() + Record.RECORD_PROPERTY_NAMES.length,
                            ((List)mv.getModelMap().get("surveyFormFieldList")).size());
        Assert.assertEquals(0, ((List)mv.getModelMap().get("taxonGroupFormFieldList")).size());
        for(FormField formField : ((List<FormField>)mv.getModelMap().get("surveyFormFieldList"))) {
            if(formField.isPropertyFormField()) {
                Assert.assertNull(((RecordPropertyFormField)formField).getSpecies());
            }
        }
    }
    
    @Test
    public void testAddRecordWithExactTaxonSearch() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/tracker.htm");
        request.setParameter("surveyId", survey.getId().toString());
        request.setParameter("taxonSearch", speciesA.getScientificName());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "tracker");
        
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "record");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyFormFieldList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroupFormFieldList");
        
        Assert.assertFalse((Boolean)mv.getModelMap().get("preview"));
        Assert.assertEquals(survey.getAttributes().size() + Record.RECORD_PROPERTY_NAMES.length,
                            ((List)mv.getModelMap().get("surveyFormFieldList")).size());
        // Half of the taxon group attributes are tags.
        Assert.assertEquals(speciesA.getTaxonGroup().getAttributes().size()/2, 
                            ((List)mv.getModelMap().get("taxonGroupFormFieldList")).size());
        for(FormField formField : ((List<FormField>)mv.getModelMap().get("surveyFormFieldList"))) {
            if(formField.isPropertyFormField()) {
                Assert.assertEquals(speciesA, ((RecordPropertyFormField)formField).getSpecies());
            }
        }
    }
    
    @Test
    public void testAddRecordWithMultipleTaxaSearch() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/tracker.htm");
        request.setParameter("surveyId", survey.getId().toString());
        request.setParameter("taxonSearch", "Indicator Species");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "tracker");
        
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "record");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");

        IndicatorSpecies expectedTaxon = 
            surveyDAO.getSpeciesForSurveySearch(survey.getId(), request.getParameter("taxonSearch")).get(0);
        Assert.assertFalse((Boolean)mv.getModelMap().get("preview"));
        Assert.assertEquals(survey.getAttributes().size() + Record.RECORD_PROPERTY_NAMES.length,
                            ((List)mv.getModelMap().get("surveyFormFieldList")).size());
        // Half of the taxon group attributes are tags.
        Assert.assertEquals(expectedTaxon.getTaxonGroup().getAttributes().size()/2, 
                            ((List)mv.getModelMap().get("taxonGroupFormFieldList")).size());
        // Its an error that gets logged, but nonetheless the first species
        // should be returned
        for(FormField formField : ((List<FormField>)mv.getModelMap().get("surveyFormFieldList"))) {
            if(formField.isPropertyFormField()) {
                Assert.assertEquals(expectedTaxon, ((RecordPropertyFormField)formField).getSpecies());
            }
        }
    }
    
    @Test 
    public void testEditRecord() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
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
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Set<RecordAttribute> attributeList = new HashSet<RecordAttribute>();
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
            attributeList.add(recAttr);
            expectedRecordAttrMap.put(attr, recAttr);
        }
        
        for(Attribute attr : record.getSpecies().getTaxonGroup().getAttributes()) {
            if(!attr.isTag()) {
                RecordAttribute recAttr = new RecordAttribute();
                recAttr.setAttribute(attr);
                switch (attr.getType()) {
                    case INTEGER:
                        Integer i = new Integer(987);
                        recAttr.setNumericValue(new BigDecimal(i));
                        recAttr.setStringValue(i.toString());
                        break;
                    case DECIMAL:
                        Double d = new Double(987);
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
                        recAttr.setStringValue("This is a test string record attribute for groups");
                        break;
                    case TEXT:
                        recAttr.setStringValue("This is a test text record attribute for groups");
                        break;
                    case STRING_WITH_VALID_VALUES:
                        recAttr.setStringValue(attr.getOptions().iterator().next().getValue());
                        break;
                    case FILE:
                        recAttr.setStringValue("testGroupDataFile.dat");
                        break;
                    case IMAGE:
                        recAttr.setStringValue("testGroupImgFile.png");
                        break;
                    default:
                        Assert.assertTrue("Unknown Attribute Type: "+attr.getType().toString(), false);
                        break;
                }
                recAttr = recordDAO.saveRecordAttribute(recAttr);
                attributeList.add(recAttr);
                expectedRecordAttrMap.put(attr, recAttr);
            }
        }

        record.setAttributes(attributeList);
        record = recordDAO.saveRecord(record);
        
        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/tracker.htm");
        request.setParameter("surveyId", survey.getId().toString());
        request.setParameter("recordId", record.getId().toString());
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "tracker");
        
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "record");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyFormFieldList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroupFormFieldList");
        
        List<FormField> allFormFields = new ArrayList<FormField>((List<FormField>)mv.getModelMap().get("surveyFormFieldList"));
        allFormFields.addAll((List<FormField>)mv.getModelMap().get("taxonGroupFormFieldList"));
        for(FormField formField : allFormFields) {
            if(formField.isAttributeFormField()) {
                RecordAttributeFormField attributeField = (RecordAttributeFormField)formField;
                
                Assert.assertEquals(record, attributeField.getRecord());
                Assert.assertEquals(survey, attributeField.getSurvey());
                Assert.assertEquals(expectedRecordAttrMap.get(attributeField.getAttribute()), 
                                    attributeField.getRecordAttribute());
            } else if(formField.isPropertyFormField()) {
                RecordPropertyFormField propertyField = (RecordPropertyFormField)formField;
                Assert.assertEquals(record, propertyField.getRecord());
                Assert.assertEquals(survey, propertyField.getSurvey());
            } else {
                Assert.assertTrue(false);
            }
        }
        
        Assert.assertEquals(record, mv.getModelMap().get("record"));
        Assert.assertEquals(survey, mv.getModelMap().get("survey"));
    }
    
    @Test 
    public void testSaveRecord() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Date today = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/tracker.htm");

        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());
        //params.put("recordId","");
        params.put("species", speciesA.getId().toString());
        params.put("latitude", "-32.546");
        params.put("longitude", "115.488");
        params.put("date", dateFormat.format(today));
        params.put("time_hour", "15");
        params.put("time_minute", "48");
        params.put("number", "29");
        params.put("notes", "This is a test record");
        
        String key;
        String value;
        for (Attribute attr : survey.getAttributes()) {
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
        
        for (Attribute attr : speciesA.getTaxonGroup().getAttributes()) {
            key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX, attr.getId());
            value = new String();
            switch (attr.getType()) {
                case INTEGER:
                    value = "987";
                    break;
                case DECIMAL:
                    value = "654.3";
                    break;
                case DATE:
                    value = dateFormat.format(today);
                    break;
                case STRING_AUTOCOMPLETE:
                case STRING:
                    value = "Test Group Attr String";
                    break;
                case TEXT:
                    value = "Test Group Attr Text";
                    break;
                case STRING_WITH_VALID_VALUES:
                    value = attr.getOptions().iterator().next().getValue();
                    break;
                case FILE:
                    String file_filename = String.format("group_attribute_%d", attr.getId());
                    MockMultipartFile mockFileFile = new MockMultipartFile(key, file_filename, "audio/mpeg", file_filename.getBytes());
                    ((MockMultipartHttpServletRequest)request).addFile(mockFileFile);
                    value = file_filename;
                    break;
                case IMAGE:
                    String image_filename = String.format("group_attribute_%d", attr.getId());
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
        
        request.setParameters(params);
        ModelAndView mv = handle(request, response);
        
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView)mv.getView();
        Assert.assertEquals(redirectionService.getMySightingsUrl(survey), redirect.getUrl());
        
        Assert.assertEquals(1, recordDAO.countRecords(getRequestContext().getUser()).intValue());
        Record rec = recordDAO.getRecords(getRequestContext().getUser()).get(0);
        
        Assert.assertEquals(speciesA, rec.getSpecies());
        Assert.assertEquals(Double.parseDouble(params.get("latitude")), rec.getPoint().getY());
        Assert.assertEquals(Double.parseDouble(params.get("longitude")), rec.getPoint().getX());
        
        Calendar cal = GregorianCalendar.getInstance();
        cal.setTime(today);
        cal.set(Calendar.HOUR_OF_DAY, 15);
        cal.set(Calendar.MINUTE, 48);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        Assert.assertEquals(cal.getTime(), rec.getWhen());
        Assert.assertEquals(cal.getTime().getTime(), rec.getTime().longValue());
        
        Assert.assertEquals(rec.getNotes(), params.get("notes"));
        
        for(AttributeValue recAttr: rec.getAttributes()) {
            Attribute attr = recAttr.getAttribute();
            if(survey.getAttributes().contains(recAttr.getAttribute())) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
            } else if(speciesA.getTaxonGroup().getAttributes().contains(recAttr.getAttribute())) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX, attr.getId());
            } else {
                Assert.assertFalse(true);
                key = null;
            }
            
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
                case STRING:
                case STRING_AUTOCOMPLETE:
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
    
    @Test
    public void testRecordFormPredefinedLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/tracker.htm", true, SurveyFormRendererType.DEFAULT, true);
    }
    
    @Test
    public void testRecordFormLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/tracker.htm", false, SurveyFormRendererType.DEFAULT, true);
    }
    
    @Test
    public void testRecordFormPredefinedLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/tracker.htm", true, SurveyFormRendererType.DEFAULT, false);
    }
    
    @Test
    public void testRecordFormLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/tracker.htm", false, SurveyFormRendererType.DEFAULT, false);
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}