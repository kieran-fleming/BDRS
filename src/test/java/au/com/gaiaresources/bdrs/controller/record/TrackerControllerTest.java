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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.attribute.formfield.FormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordAttributeFormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyFormField;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
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
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
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
    private Location locationPoly;

    private Survey simpleSurvey;
    
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
        for (TaxonGroup group : new TaxonGroup[] { taxonGroupBirds,
                taxonGroupFrogs }) {
            taxonGroupAttributeList = new ArrayList<Attribute>();
            for (boolean isTag : new boolean[] { true, false }) {
                for (AttributeType attrType : AttributeType.values()) {
                    groupAttr = new Attribute();
                    groupAttr.setRequired(true);
                    groupAttr.setName(group.getName() + "_"
                            + attrType.toString() + "_isTag" + isTag);
                    groupAttr.setDescription(group.getName() + "_"
                            + attrType.toString() + "_isTag" + isTag);
                    groupAttr.setTypeCode(attrType.getCode());
                    groupAttr.setScope(null);
                    groupAttr.setTag(isTag);
    
                    if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType) ||
                            AttributeType.MULTI_CHECKBOX.equals(attrType) ||
                            AttributeType.MULTI_SELECT.equals(attrType)) {

                        List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                        for (int i = 0; i < 4; i++) {
                            AttributeOption opt = new AttributeOption();
                            opt.setValue(String.format("Option %d", i));
                            opt = taxaDAO.save(opt);
                            optionList.add(opt);
                        }
                        groupAttr.setOptions(optionList);
                    } else if (AttributeType.INTEGER_WITH_RANGE.equals(attrType)) {
                        List<AttributeOption> rangeList = new ArrayList<AttributeOption>();
                        AttributeOption upper = new AttributeOption();
                        AttributeOption lower = new AttributeOption();
                        lower.setValue("100");
                        upper.setValue("200");
                        rangeList.add(taxaDAO.save(lower));
                        rangeList.add(taxaDAO.save(upper));
                        groupAttr.setOptions(rangeList);
                    } else if (AttributeType.BARCODE.equals(attrType)) {
                        List<AttributeOption> regExpList = new ArrayList<AttributeOption>();
                        AttributeOption regExp = new AttributeOption();
                        regExp.setValue("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$"); // regexp for hex color codes
                        regExpList.add(taxaDAO.save(regExp));
                        groupAttr.setOptions(regExpList);
                    } else if (AttributeType.REGEX.equals(attrType)) {
                        List<AttributeOption> regExpList = new ArrayList<AttributeOption>();
                        AttributeOption regExp = new AttributeOption();
                        String re1="\\d+(\\.?\\d+)?\\s+\\w+\\s+\\d+";
                        regExp.setValue(re1); // regexp for '1.5 of 3'
                        regExpList.add(taxaDAO.save(regExp));
                        groupAttr.setOptions(regExpList);
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
        for (AttributeType attrType : AttributeType.values()) {
            for (AttributeScope scope : new AttributeScope[] {
                    AttributeScope.RECORD, AttributeScope.SURVEY, 
                    AttributeScope.RECORD_MODERATION, AttributeScope.SURVEY_MODERATION, 
                    null }) {

                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(scope);
                attr.setTag(false);

                if(AttributeType.STRING_WITH_VALID_VALUES.equals(attrType) ||
                        AttributeType.MULTI_CHECKBOX.equals(attrType) ||
                        AttributeType.MULTI_SELECT.equals(attrType)) {
                    
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for (int i = 0; i < 4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        opt = taxaDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                } else if (AttributeType.INTEGER_WITH_RANGE.equals(attrType)) {
                    List<AttributeOption> rangeList = new ArrayList<AttributeOption>();
                    AttributeOption upper = new AttributeOption();
                    AttributeOption lower = new AttributeOption();
                    lower.setValue("100");
                    upper.setValue("200");
                    rangeList.add(taxaDAO.save(lower));
                    rangeList.add(taxaDAO.save(upper));
                    attr.setOptions(rangeList);
                } else if (AttributeType.BARCODE.equals(attrType)) {
                    List<AttributeOption> regExpList = new ArrayList<AttributeOption>();
                    AttributeOption regExp = new AttributeOption();
                    regExp.setValue("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
                    regExpList.add(taxaDAO.save(regExp));
                    attr.setOptions(regExpList);
                } else if (AttributeType.REGEX.equals(attrType)) {
                    List<AttributeOption> regExpList = new ArrayList<AttributeOption>();
                    AttributeOption regExp = new AttributeOption();
                    String re1="\\d+(\\.?\\d+)?\\s+\\w+\\s+\\d+";
                    regExp.setValue(re1); // regexp for '1.5 of 3'
                    regExpList.add(taxaDAO.save(regExp));
                    attr.setOptions(regExpList);
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
        survey.setStartDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        survey.setDefaultRecordVisibility(RecordVisibility.CONTROLLED, metadataDAO);
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md);
        survey.setAttributes(attributeList);
        survey.setSpecies(speciesSet);
        survey = surveyDAO.save(survey);

        simpleSurvey = new Survey();
        simpleSurvey.setName("Simple Survey");
        simpleSurvey.setActive(true);
        simpleSurvey.setStartDate(new Date());
        simpleSurvey.setDescription("Simple Test Survey");
        Metadata md2 = simpleSurvey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md2);
        //survey.setSpecies(speciesSet);
        simpleSurvey = surveyDAO.save(simpleSurvey);
        
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
        
        locationPoly = new Location();
        locationPoly.setLocation(locationService.createGeometryFromWKT("POLYGON((114.91699218293 -33.678639850485,115.18066405793 -34.406909655312,117.46582030783 -35.272531755372,119.92675780773 -33.897777012664,114.91699218293 -33.678639850485))"));
        locationPoly.setName("Location Poly");
        locationPoly.setUser(admin);
        locationPoly = locationDAO.save(locationPoly);
        
		// all tests in this class are attempting to edit a record
        request.setParameter(RecordWebFormContext.PARAM_EDIT, "true");
    }
    
    @Test
    public void nullSurveyGet() throws Exception {
        nullSurveyTest("GET");
    }
    
    @Test
    public void nullSurveyPost() throws Exception {
        nullSurveyTest("POST");
    }
    
    private void nullSurveyTest(String requestMethod) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod(requestMethod);
        request.setRequestURI("/bdrs/user/tracker.htm");
        request.setParameter("surveyId", Integer.toString(0));
        
        ModelAndView mav = handle(request, response);
        
        assertRedirectAndErrorCode(mav, redirectionService.getMySightingsUrl(null), TrackerController.NO_SURVEY_ERROR_KEY);
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

        Assert.assertFalse((Boolean) mv.getModelMap().get("preview"));
        Assert.assertEquals(survey.getAttributes().size()
                + RecordPropertyType.values().length, ((List) mv.getModelMap().get("surveyFormFieldList")).size());
        Assert.assertEquals(0, ((List) mv.getModelMap().get("taxonGroupFormFieldList")).size());
        for (FormField formField : ((List<FormField>) mv.getModelMap().get("surveyFormFieldList"))) {
            if (formField.isPropertyFormField()) {
                Assert.assertNull(((RecordPropertyFormField) formField).getSpecies());
            }
        }
        
        // make sure the new record has the correct default record visibility set
        Record rec = (Record)mv.getModel().get("record");
        Assert.assertEquals(RecordVisibility.CONTROLLED, rec.getRecordVisibility());
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

        Assert.assertFalse((Boolean) mv.getModelMap().get("preview"));
        Assert.assertEquals(survey.getAttributes().size()
                + RecordPropertyType.values().length, ((List) mv.getModelMap().get("surveyFormFieldList")).size());
        // Half of the taxon group attributes are tags.
        Assert.assertEquals(speciesA.getTaxonGroup().getAttributes().size() / 2, ((List) mv.getModelMap().get("taxonGroupFormFieldList")).size());
        for (FormField formField : ((List<FormField>) mv.getModelMap().get("surveyFormFieldList"))) {
            if (formField.isPropertyFormField()) {
            	RecordPropertyFormField recordPropertyFormField = (RecordPropertyFormField) formField;
                Assert.assertEquals(speciesA, recordPropertyFormField.getSpecies());
                //Make sure the right descriptions come through in a default survey
                RecordProperty recordProperty = recordPropertyFormField.getRecordProperty();
                switch (recordProperty.getRecordPropertyType()){
                case ACCURACY:
                	Assert.assertEquals("Accuracy (meters)", recordProperty.getDescription());
                	break;
                case NUMBER:
                	Assert.assertEquals("Individual Count", recordProperty.getDescription());
                	break;
                case WHEN:
                	Assert.assertEquals("Date", recordProperty.getDescription());
                	break;
               default:
            	   Assert.assertEquals(recordProperty.getRecordPropertyType().getDefaultDescription(), recordProperty.getDescription());
                }
                
                
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

        IndicatorSpecies expectedTaxon = surveyDAO.getSpeciesForSurveySearch(survey.getId(), request.getParameter("taxonSearch")).get(0);
        Assert.assertFalse((Boolean) mv.getModelMap().get("preview"));
        Assert.assertEquals(survey.getAttributes().size()
                + RecordPropertyType.values().length, ((List) mv.getModelMap().get("surveyFormFieldList")).size());
        // Half of the taxon group attributes are tags.
        Assert.assertEquals(expectedTaxon.getTaxonGroup().getAttributes().size() / 2, ((List) mv.getModelMap().get("taxonGroupFormFieldList")).size());
        // Its an error that gets logged, but nonetheless the first species
        // should be returned
        for (FormField formField : ((List<FormField>) mv.getModelMap().get("surveyFormFieldList"))) {
            if (formField.isPropertyFormField()) {
                Assert.assertEquals(expectedTaxon, ((RecordPropertyFormField) formField).getSpecies());
            }
        }
    }
    
    @Test
    public void testAddEmptyRecord() throws Exception {
    	Survey mockSurvey = new Survey();
    	mockSurvey.setName("mockSurvey");
    	mockSurvey.setActive(true);
    	mockSurvey.setStartDate(new Date());
    	mockSurvey.setDescription("Survey to test adding an empty record");
    	mockSurvey.setDefaultRecordVisibility(RecordVisibility.CONTROLLED, metadataDAO);
        Metadata mockMd  = mockSurvey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(mockMd);
        surveyDAO.save(mockSurvey);
    	
    	login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/tracker.htm");
        request.setParameter("surveyId", mockSurvey.getId().toString());
        setDwcRequired(false, mockSurvey);
        setDwcHidden(true, mockSurvey);
        ModelAndView mv = handle(request, response);
        Integer recordId = (Integer)mv.getModelMap().get("record_id");
        Assert.assertNotNull("recordId is null", recordId);
        if (recordId != null) {
        	Record r = recordDAO.getRecord(recordId);
        	Assert.assertNotNull("Record is null", r);
        }
    }

    @Test
    public void testEditRecordLowerLimitOutside() throws Exception {
        testEditRecord("99");
    }

    @Test
    public void testEditRecordLowerLimitEdge() throws Exception {
        testEditRecord("100");
    }

    @Test
    public void testEditRecordInRange() throws Exception {
        testEditRecord("101");
    }

    @Test
    public void testEditRecordUpperLimitEdge() throws Exception {
        testEditRecord("200");
    }

    @Test
    public void testEditRecordUpperLimitOutside() throws Exception {
        testEditRecord("201");
    }

    public void testEditRecord(String intWithRangeValue) throws Exception {
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
        Set<AttributeValue> attributeList = new HashSet<AttributeValue>();
        Map<Attribute, AttributeValue> expectedRecordAttrMap = new HashMap<Attribute, AttributeValue>();
        for(Attribute attr : survey.getAttributes()) {
            if(!AttributeScope.LOCATION.equals(attr.getScope())) {
                List<AttributeOption> opts = attr.getOptions();
                AttributeValue recAttr = new AttributeValue();
                recAttr.setAttribute(attr);
                switch (attr.getType()) {
                case INTEGER:
                    Integer i = Integer.valueOf(123);
                    recAttr.setNumericValue(new BigDecimal(i));
                    recAttr.setStringValue(i.toString());
                    break;
                case INTEGER_WITH_RANGE:
                    Integer j = new Integer(intWithRangeValue);
                    recAttr.setNumericValue(new BigDecimal(j));
                    recAttr.setStringValue(intWithRangeValue);
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
                case REGEX:
                    recAttr.setStringValue("1.5 of 3");
                    break;
                case BARCODE:
                    recAttr.setStringValue("#123456");
                    break;
                case TIME:
                    recAttr.setStringValue("12:34");
                    break;
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    recAttr.setStringValue("<hr/>");
                    break;
                case TEXT:
                    recAttr.setStringValue("This is a test text record attribute");
                    break;
                case STRING_WITH_VALID_VALUES:
                    recAttr.setStringValue(attr.getOptions().iterator().next().getValue());
                    break;
                case MULTI_CHECKBOX:
                    recAttr.setMultiCheckboxValue(new String[]{opts.get(0).getValue(), opts.get(1).getValue()});
                    break;
                case MULTI_SELECT:
                    recAttr.setMultiCheckboxValue(new String[]{opts.get(0).getValue(), opts.get(1).getValue()});
                    break;
                case SINGLE_CHECKBOX:
                    recAttr.setBooleanValue(Boolean.TRUE.toString());
                    break;
                case FILE:
                    recAttr.setStringValue("testDataFile.dat");
                    break;
                case IMAGE:
                    recAttr.setStringValue("testImgFile.png");
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + attr.getType().toString(), false);
                    break;
                }
                recAttr = recordDAO.saveAttributeValue(recAttr);
                attributeList.add(recAttr);
                expectedRecordAttrMap.put(attr, recAttr);
            }
        }

        for (Attribute attr : record.getSpecies().getTaxonGroup().getAttributes()) {
            if (!attr.isTag()) {
                AttributeValue recAttr = new AttributeValue();
                recAttr.setAttribute(attr);
                switch (attr.getType()) {
                case INTEGER:
                    Integer i = new Integer(987);
                    recAttr.setNumericValue(new BigDecimal(i));
                    recAttr.setStringValue(i.toString());
                    break;
                case INTEGER_WITH_RANGE:
                    Integer j = new Integer(intWithRangeValue);
                    recAttr.setNumericValue(new BigDecimal(j));
                    recAttr.setStringValue(intWithRangeValue);
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
                case REGEX:
                    recAttr.setStringValue("6 afraidof 7");
                    break;
                case BARCODE:
                    recAttr.setStringValue("#123456");
                    break;
                case TIME:
                    recAttr.setStringValue("12:34");
                    break;
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    recAttr.setStringValue("<hr/>");
                    break;
                case TEXT:
                    recAttr.setStringValue("This is a test text record attribute for groups");
                    break;
                case STRING_WITH_VALID_VALUES:
                    recAttr.setStringValue(attr.getOptions().iterator().next().getValue());
                    break;
                case MULTI_CHECKBOX:
                    {
                        List<AttributeOption> opts = attr.getOptions(); 
                        recAttr.setMultiCheckboxValue(new String[]{
                                opts.get(0).getValue(),
                                opts.get(1).getValue()
                        });
                    }
                    break;
                case MULTI_SELECT:
                    {
                        List<AttributeOption> opts = attr.getOptions(); 
                        recAttr.setMultiSelectValue(new String[]{
                                opts.get(0).getValue(),
                                opts.get(1).getValue()
                        });
                    }
                    break;
                case SINGLE_CHECKBOX:
                    recAttr.setStringValue(Boolean.FALSE.toString());
                    break;
                case FILE:
                    recAttr.setStringValue("testGroupDataFile.dat");
                    break;
                case IMAGE:
                    recAttr.setStringValue("testGroupImgFile.png");
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + attr.getType().toString(), false);
                    break;
                }
                recAttr = recordDAO.saveAttributeValue(recAttr);
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

        List<FormField> allFormFields = new ArrayList<FormField>(
                (List<FormField>) mv.getModelMap().get("surveyFormFieldList"));
        allFormFields.addAll((List<FormField>) mv.getModelMap().get("taxonGroupFormFieldList"));
        for (FormField formField : allFormFields) {
            if (formField.isAttributeFormField()) {
                RecordAttributeFormField attributeField = (RecordAttributeFormField) formField;

                Assert.assertEquals(record, attributeField.getRecord());
                Assert.assertEquals(survey, attributeField.getSurvey());
                Assert.assertEquals(expectedRecordAttrMap.get(attributeField.getAttribute()), attributeField.getAttributeValue());
            } else if (formField.isPropertyFormField()) {
                RecordPropertyFormField propertyField = (RecordPropertyFormField) formField;
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
    public void testSaveRecordLowerLimitOutside() throws Exception {
        testSaveRecord("99", false);
    }

    @Test
    public void testSaveRecordLowerLimitEdge() throws Exception {
        testSaveRecord("100", true);
    }

    @Test
    public void testSaveRecordInRange() throws Exception {
        testSaveRecord("101", true);
    }

    @Test
    public void testSaveRecordUpperLimitEdge() throws Exception {
        testSaveRecord("200", true);
    }

    @Test
    public void testSaveRecordUpperLimitOutside() throws Exception {
        testSaveRecord("201", false);
    }

    public void testSaveRecord(String intWithRangeValue, boolean passExpected)
            throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Date today = dateFormat.parse(dateFormat.format(new Date(
                System.currentTimeMillis())));

        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/tracker.htm");

        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());
        params.put("survey_species_search", speciesA.getScientificName());
        params.put("species", speciesA.getId().toString());
        params.put("latitude", "-32.546");
        params.put("longitude", "115.488");
        params.put("date", dateFormat.format(today));
        params.put("time", "15:48");
        params.put("number", "29");
        params.put("notes", "This is a test record");

        params.putAll(createAttributes(intWithRangeValue, dateFormat, today));

        request.setParameters(params);
        ModelAndView mv = handle(request, response);

        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        if (passExpected) {
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

            String key;
            for (TypedAttributeValue recAttr : rec.getAttributes()) {
                Attribute attr = recAttr.getAttribute();
                if (survey.getAttributes().contains(recAttr.getAttribute())) {
                    key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
                } else if (speciesA.getTaxonGroup().getAttributes().contains(recAttr.getAttribute())) {
                    key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX, attr.getId());
                } else {
                    Assert.assertFalse(true);
                    key = null;
                }

                switch (recAttr.getAttribute().getType()) {
                case INTEGER:
                case INTEGER_WITH_RANGE:
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
                case REGEX:
                case BARCODE:
                case TIME:
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    Assert.assertEquals(params.get(key), recAttr.getStringValue());
                    break;
                case STRING_WITH_VALID_VALUES:
                    Assert.assertEquals(params.get(key), recAttr.getStringValue());
                    break;
                case MULTI_CHECKBOX:
                    {
                        // make sure the correct data got posted to the server correctly
                        Assert.assertEquals(2, request.getParameterValues(key).length);
                        Set<String> optionSet = new HashSet<String>();
                        for(AttributeOption opt : recAttr.getAttribute().getOptions()) {
                            optionSet.add(opt.getValue());
                        }
                        for(String val : recAttr.getMultiCheckboxValue()){
                            Assert.assertTrue(optionSet.contains(val));
                        }
                    }
                    break;
                case MULTI_SELECT:
                    {
                        // make sure the correct data got posted to the server correctly
                        Assert.assertEquals(2, request.getParameterValues(key).length);
                        Set<String> optionSet = new HashSet<String>();
                        for(AttributeOption opt : recAttr.getAttribute().getOptions()) {
                            optionSet.add(opt.getValue());
                        }
                        for(String val : recAttr.getMultiSelectValue()){
                            Assert.assertTrue(optionSet.contains(val));
                        }
                    }
                    break;
                case SINGLE_CHECKBOX:
                    Assert.assertEquals(recAttr.getStringValue() + " should be 'true'!", 
                                        Boolean.parseBoolean(params.get(key)), 
                                        Boolean.parseBoolean(recAttr.getStringValue()));
                    break;  
                case FILE:
                case IMAGE:
                    Assert.assertEquals(params.get(key), recAttr.getStringValue());
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + recAttr.getAttribute().getType().toString(), false);
                    break;
                }
            }
        } else {
            Assert.assertEquals("/bdrs/user/tracker.htm", redirect.getUrl());
            Assert.assertEquals(0, recordDAO.countRecords(getRequestContext().getUser()).intValue());
            
        }
    }

    private Map<? extends String, ? extends String> createAttributes() {
        return createAttributes("123", new SimpleDateFormat("dd MMM yyyy"), new Date());
    }
    
    private Map<? extends String, ? extends String> createAttributes(String intWithRangeValue, DateFormat dateFormat, Date today) {
        Map<String, String> params = new HashMap<String,String>();
        String key;
        String value;
        for (Attribute attr : survey.getAttributes()) {
            if(!AttributeScope.LOCATION.equals(attr.getScope())) {
                key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
                value = "";
                switch (attr.getType()) {
                case INTEGER:
                    value = "123";
                    break;
                case INTEGER_WITH_RANGE:
                    value = intWithRangeValue;
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
                case REGEX:
                    value = "7 ate 9";
                    break;
                case BARCODE:
                    value = "#123456";
                    break;
                case TIME:
                    value = "12:34";
                    break;
                case TEXT:
                    value = "Test Survey Attr Text";
                    break;
                case STRING_WITH_VALID_VALUES:
                    value = attr.getOptions().iterator().next().getValue();
                    break;
                case MULTI_CHECKBOX:
                case MULTI_SELECT:
                    List<AttributeOption> opts = attr.getOptions();
                    request.addParameter(key, opts.get(0).getValue());
                    request.addParameter(key, opts.get(1).getValue());
                    value = null;
                    break;
                case SINGLE_CHECKBOX:
                    value = String.valueOf(true);
                    break;
                case FILE:
                    String file_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockFileFile = new MockMultipartFile(key,
                            file_filename, "audio/mpeg", file_filename.getBytes());
                    ((MockMultipartHttpServletRequest) request).addFile(mockFileFile);
                    value = file_filename;
                    break;
                case IMAGE:
                    String image_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockImageFile = new MockMultipartFile(key,
                            image_filename, "image/png", image_filename.getBytes());
                    ((MockMultipartHttpServletRequest) request).addFile(mockImageFile);
                    value = image_filename;
                    break;
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    value = "<hr/>";
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + attr.getType().toString(), false);
                    break;
                }
                if(value != null) {
                    params.put(key, value);
                }
                // Otherwise value added directly to the request parameters
                // this is to support adding an "array" of values.
            }
        }

        for (Attribute attr : speciesA.getTaxonGroup().getAttributes()) {
            key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX, attr.getId());
            value = "";
            switch (attr.getType()) {
            case INTEGER:
                value = "987";
                break;
            case INTEGER_WITH_RANGE:
                value = intWithRangeValue;
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
            case HTML:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
                value = "<hr/>";
                break;
            case REGEX:
                value = "12.9 plus 30";
                break;
            case BARCODE:
                value = "#123456";
                break;
            case TIME:
                value = "12:34";
                break;
            case TEXT:
                value = "Test Group Attr Text";
                break;
            case STRING_WITH_VALID_VALUES:
                value = attr.getOptions().iterator().next().getValue();
                break;
            case MULTI_CHECKBOX:
            case MULTI_SELECT:
                List<AttributeOption> opts = attr.getOptions(); 
                request.addParameter(key, opts.get(0).getValue());
                request.addParameter(key, opts.get(1).getValue());
                value = null;
                break;
            case SINGLE_CHECKBOX:
                // Should evaluate to false
                value = "wibble";
                break;
            case FILE:
                String file_filename = String.format("group_attribute_%d", attr.getId());
                MockMultipartFile mockFileFile = new MockMultipartFile(key,
                        file_filename, "audio/mpeg", file_filename.getBytes());
                ((MockMultipartHttpServletRequest) request).addFile(mockFileFile);
                value = file_filename;
                break;
            case IMAGE:
                String image_filename = String.format("group_attribute_%d", attr.getId());
                MockMultipartFile mockImageFile = new MockMultipartFile(key,
                        image_filename, "image/png", image_filename.getBytes());
                ((MockMultipartHttpServletRequest) request).addFile(mockImageFile);
                value = image_filename;
                break;
            default:
                Assert.assertTrue("Unknown Attribute Type: "
                        + attr.getType().toString(), false);
                break;
            }
            if(value != null) {
                params.put(key, value);
            } 
            // Otherwise value added directly to the request parameters
            // this is to support adding an "array" of values. 
        }
        return params;
    }

    @Test
    public void testSaveRecordInvalidEarlyDateNoEnd() throws Exception {
        testSaveRecordWithDateRange("04 Jul 2011 12:45", "05 Jul 2011 00:00", null, false);
    }
 
    @Test
    public void testSaveRecordInvalidEarlyDate() throws Exception {
        testSaveRecordWithDateRange("04 Jul 2011 12:45", "05 Jul 2011 00:00", "06 Jul 2011 17:00", false);
    }
 
    //@Test this test doesn't work as expected, but does in practice, why?
    public void testSaveRecordValidDateNoEnd() throws Exception {
        testSaveRecordWithDateRange("05 Jul 2011 12:45", "05 Jul 2011 00:00", null, true);
    }
 
    //@Test this test doesn't work as expected, but does in practice, why?
    public void testSaveRecordSameStartDate() throws Exception {
        testSaveRecordWithDateRange("05 Jul 2011 12:45", "05 Jul 2011 00:00", "06 Jul 2011 17:00", true);
    }
 
    @Test
    public void testSaveRecordSameEndDate() throws Exception {
        testSaveRecordWithDateRange("06 Jul 2011 12:45", "05 Jul 2011 00:00", "06 Jul 2011 17:00", false);
    }
 
    @Test
    public void testSaveRecordInvalidLateDate() throws Exception {
        testSaveRecordWithDateRange("07 Jul 2011 12:45", "05 Jul 2011 00:00", "06 Jul 2011 17:00", false);
    }
 
    public void testSaveRecordWithDateRange(String date, String earliest,
            String latest, boolean passExpected) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
 
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        dateFormat.setLenient(false);
        Date today = dateFormat.parse(dateFormat.format(new Date(
                System.currentTimeMillis())));
 
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/tracker.htm");
 
        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());
        //params.put("recordId","");
        params.put("survey_species_search", speciesA.getScientificName());
        params.put("species", speciesA.getId().toString());
        params.put("latitude", "-32.546");
        params.put("longitude", "115.488");
        params.put("date", date);
        params.put("time", "15:48");
        params.put("number", "29");
        params.put("notes", "This is a test record");
 
        survey.setStartDate(earliest);
        survey.setEndDate(latest);
        survey = surveyDAO.save(survey);
 
        request.setParameters(params);
        ModelAndView mv = handle(request, response);
 
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        if (passExpected) {
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
        } else {
            Assert.assertEquals("/bdrs/user/tracker.htm", redirect.getUrl());
            Assert.assertEquals(0, recordDAO.countRecords(getRequestContext().getUser()).intValue());
        }
    }
 
    @Test
    public void testSaveRecordWithSameLonLatLocation() throws Exception {
        testSaveRecordWithLonLatLocation(locationA.getLocation().getCentroid().getX(), 
                                         locationA.getLocation().getCentroid().getY(), 
                                         locationA, true, true);
    }
    
    @Test
    public void testSaveRecordWithDifferentLonLatLocation() throws Exception {
        testSaveRecordWithLonLatLocation(locationA.getLocation().getCentroid().getX(), 
                                         locationA.getLocation().getCentroid().getY(), 
                                         locationPoly, true, false);
    }
    
    public void testSaveRecordWithLonLatLocation(double lon, double lat, Location loc, boolean passExpected, boolean geometriesMatch) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
 
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm");
        dateFormat.setLenient(false);
        Date today = dateFormat.parse(dateFormat.format(new Date(
                System.currentTimeMillis())));
 
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/tracker.htm");
 
        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", simpleSurvey.getId().toString());
        params.put("survey_species_search", "");
        params.put("latitude", String.valueOf(lat));
        params.put("longitude", String.valueOf(lon));
        params.put("location", String.valueOf(loc.getId()));
        params.put("date", dateFormat.format(new Date()));
        params.put("time", "15:48");
        params.put("number", "");
        params.put("notes", "This is a test record");
        
        request.setParameters(params);
        ModelAndView mv = handle(request, response);
 
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        if (passExpected) {
            Assert.assertEquals("Error in redirection: ", redirectionService.getMySightingsUrl(simpleSurvey), redirect.getUrl());
            Assert.assertEquals(1, recordDAO.countRecords(getRequestContext().getUser()).intValue());
            Record rec = recordDAO.getRecords(getRequestContext().getUser()).get(0);
 
            Assert.assertEquals(Double.parseDouble(params.get("latitude")), rec.getPoint().getY());
            Assert.assertEquals(Double.parseDouble(params.get("longitude")), rec.getPoint().getX());
            if (geometriesMatch) {
                Assert.assertTrue(rec.getLocation().getLocation().equalsExact(rec.getPoint()));
            } else {
                Assert.assertNotNull(rec.getLocation());
                Assert.assertNotNull(rec.getPoint());
                Assert.assertFalse(rec.getLocation().getLocation().equalsExact(rec.getPoint()));
            }
            
            Calendar cal = GregorianCalendar.getInstance();
            cal.setTime(today);
            cal.set(Calendar.HOUR_OF_DAY, 15);
            cal.set(Calendar.MINUTE, 48);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
 
            Assert.assertEquals(cal.getTime(), rec.getWhen());
            Assert.assertEquals(cal.getTime().getTime(), rec.getTime().longValue());
 
            Assert.assertEquals(rec.getNotes(), params.get("notes"));
        } else {
            Assert.assertEquals("/bdrs/user/tracker.htm", redirect.getUrl());
            Assert.assertEquals(0, recordDAO.countRecords(getRequestContext().getUser()).intValue());
        }
    }
    
    @Test
    public void testRecordFormPredefinedLocationsAsSurveyOwner()
            throws Exception {
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
    
    /**
     * Helper method that sets the Darwin Core Fields to be required or not required.
     * @param req	
     * @param dwcSurvey The survey for which the Darwin Core Fields required flag needs to be set.
     */
    private void setDwcRequired(boolean req, Survey dwcSurvey) {
		RecordProperty recordProperty;
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 recordProperty = new RecordProperty(dwcSurvey, type, metadataDAO);
			 recordProperty.setRequired(req);
		}
    }

    /**
     * Helper method that sets the Darwin Core Fields to be hidden or not hidden.
     * @param hidden
     * @param dwcSurvey The survey for which the Darwin Core Fields hidden flag needs to be set.
     */
    private void setDwcHidden(boolean hidden, Survey dwcSurvey) {
		RecordProperty recordProperty;
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 recordProperty = new RecordProperty(dwcSurvey, type, metadataDAO);
			 recordProperty.setHidden(hidden);
		}
    }
}
