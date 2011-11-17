package au.com.gaiaresources.bdrs.controller.record;

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

import org.junit.Assert;
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
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
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
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Tests all aspects of the <code>SingleSiteMultiTaxaController</code>.
 */
public class SingleSiteMultiTaxaControllerTest extends RecordFormTest {

    @Autowired
    protected SurveyDAO surveyDAO;
    @Autowired
    protected TaxaDAO taxaDAO;
    @Autowired
    protected MetadataDAO metadataDAO;
    @Autowired
    protected RecordDAO recordDAO;
    @Autowired
    protected RedirectionService redirectionService;

    protected Survey survey;
    protected TaxonGroup taxonGroup;
    protected IndicatorSpecies speciesA;
    protected IndicatorSpecies speciesB;

    @Before
    public void setUp() throws Exception {
        setup(SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA);
    }
    
    protected void setup(SurveyFormRendererType renderType) {
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
        for (AttributeType attrType : AttributeType.values()) {
            for (AttributeScope scope : new AttributeScope[] {
                    AttributeScope.RECORD, AttributeScope.SURVEY, null }) {

                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(scope);
                attr.setTag(false);

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
                    attr.setOptions(optionList);
                }

                attr = taxaDAO.save(attr);
                attributeList.add(attr);
            }
        }

        survey = new Survey();
        // make sure that the survey's record visibility is applied...
        survey.setDefaultRecordVisibility(RecordVisibility.CONTROLLED, metadataDAO);
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setName(renderType.getName()+" 1234");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription(renderType.getName()+" Survey Description");
        Metadata md = survey.setFormRendererType(renderType);
        metadataDAO.save(md);
        survey.setAttributes(attributeList);
        survey = surveyDAO.save(survey);
    }

    /**
     * Tests that a blank form can be retrieved.
     * 
     * @throws Exception
     */
    @Test
    public void testAddRecord() throws Exception {
        testAddRecord("/bdrs/user/singleSiteMultiTaxa.htm", "singleSiteMultiTaxa");
    }
    
    protected void testAddRecord(String URI, String viewName) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(URI);
        request.setParameter("surveyId", survey.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, viewName);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sightingRowFormFieldList");

        for (FormField formField : ((List<FormField>) mv.getModelMap().get("formFieldList"))) {
            if (formField.isAttributeFormField()) {
                Assert.assertEquals(AttributeScope.SURVEY, ((RecordAttributeFormField) formField).getAttribute().getScope());
            } else if (formField.isPropertyFormField()) {
                String typeName = ((RecordPropertyFormField) formField).getPropertyName();
                // It should not be either the species or the number
                Assert.assertFalse(RecordPropertyType.SPECIES.getName().equals(typeName));
                Assert.assertFalse(RecordPropertyType.NUMBER.getName().equals(typeName));
            } else {
                Assert.assertTrue(false);
            }
        }

        for (FormField formField : ((List<FormField>) mv.getModelMap().get("sightingRowFormFieldList"))) {
            if (formField.isAttributeFormField()) {
                Assert.assertFalse(AttributeScope.SURVEY.equals(((RecordAttributeFormField) formField).getAttribute().getScope()));
            } else if (formField.isPropertyFormField()) {
            	String typeName = ((RecordPropertyFormField) formField).getPropertyName();
                // It should not be either the species or the number
                Assert.assertTrue(RecordPropertyType.SPECIES.getName().equals(typeName)
                        || RecordPropertyType.NUMBER.getName().equals(typeName));
            } else {
                Assert.assertTrue(false);
            }
        }
    }

    /**
     * Tests that additional rows can be retrieved. This is normally done via
     * ajax.
     * 
     * @throws Exception
     */
    @Test
    public void testAjaxAddSightingRow() throws Exception {
        testAjaxAddSightingRow("/bdrs/user/singleSiteMultiTaxa/sightingRow.htm", "singleSiteMultiTaxaRow");
    }
    
    public void testAjaxAddSightingRow(String URI, String viewName) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI(URI);

        Map<String, String> param = new HashMap<String, String>();
        param.put("surveyId", survey.getId().toString());
        // Try 3 requests
        for (int i = 0; i < 3; i++) {
            param.put("sightingIndex", Integer.valueOf(i).toString());

            request.setParameters(param);

            ModelAndView mv = handle(request, response);
            ModelAndViewAssert.assertViewName(mv, viewName);

            String expectedPrefix = String.format(SingleSiteMultiTaxaController.PREFIX_TEMPLATE, i);
            ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");
            for (FormField formField : ((List<FormField>) mv.getModelMap().get("formFieldList"))) {
                if (formField.isAttributeFormField()) {

                    RecordAttributeFormField attributeField = (RecordAttributeFormField) formField;
                    Assert.assertEquals(expectedPrefix, attributeField.getPrefix());
                    Assert.assertNull(attributeField.getRecord().getId());
                    Assert.assertEquals(survey, attributeField.getSurvey());

                    Assert.assertFalse(AttributeScope.SURVEY.equals(attributeField.getAttribute().getScope()));

                } else if (formField.isPropertyFormField()) {

                    RecordPropertyFormField propertyField = (RecordPropertyFormField) formField;
                    Assert.assertEquals(expectedPrefix, propertyField.getPrefix());
                    Assert.assertNull(propertyField.getRecord().getId());
                    Assert.assertEquals(survey, propertyField.getSurvey());

                    Assert.assertTrue(RecordPropertyType.SPECIES.getName().equals(propertyField.getPropertyName())
                            || RecordPropertyType.NUMBER.getName().equals(propertyField.getPropertyName()));

                } else {

                    Assert.assertTrue(false);

                }
            }
        }
    }

    /**
     * Tests that multiple records can be saved.
     * 
     * @throws Exception
     */
    @Test 
    public void testSaveRecordLowerLimitOutside() throws Exception{
    	testSaveRecord("99");
    }
    
    @Test 
    public void testSaveRecordLowerLimitEdge() throws Exception{
    	testSaveRecord("100");
    }
    
    @Test 
    public void testSaveRecordInRange() throws Exception{
    	testSaveRecord("101");
    }
    
    @Test 
    public void testSaveRecordUpperLimitEdge() throws Exception{
    	testSaveRecord("200");
    }
    
    @Test 
    public void testSaveRecordUpperLimitOutside() throws Exception{
    	testSaveRecord("201");
    }
    public void testSaveRecord(String intWithRangeValue) throws Exception {
        testSaveRecord(intWithRangeValue, "/bdrs/user/singleSiteMultiTaxa.htm");
    }
    
    protected void testSaveRecord(String intWithRangeValue, String URI) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI(URI);

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);

        GregorianCalendar cal = new GregorianCalendar();
        cal.set(2010, 10, 12, 15, 30);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date sightingDate = cal.getTime();

        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());
        params.put("latitude", "-36.879620605027");
        params.put("longitude", "126.650390625");
        params.put("date", dateFormat.format(sightingDate));
        params.put("time_hour", new Integer(cal.get(Calendar.HOUR_OF_DAY)).toString());
        params.put("time_minute", new Integer(cal.get(Calendar.MINUTE)).toString());
        params.put("notes", "This is a test record");
        params.put("sightingIndex", "2");

        Map<Attribute, Object> surveyScopeAttributeValueMapping = new HashMap<Attribute, Object>();
        Map<IndicatorSpecies, Map<Attribute, Object>> recordScopeAttributeValueMapping = new HashMap<IndicatorSpecies, Map<Attribute, Object>>(
                2);
        Map<Attribute, Object> attributeValueMapping;

        // We have 2 species set up so lets save them both
        int sightingIndex = 0;
        String surveyPrefix = "";
        for (IndicatorSpecies taxon : new IndicatorSpecies[] { speciesA,
                speciesB }) {
            params.put(String.format("%d_survey_species_search", sightingIndex), taxon.getScientificName());
            params.put(String.format("%d_species", sightingIndex), taxon.getId().toString());
            params.put(String.format("%d_number", sightingIndex), Integer.valueOf(sightingIndex + 21).toString());

            String recordPrefix = String.format("%d_", sightingIndex);
            String prefix;
            String key;
            String value; // The value in the post dict
            attributeValueMapping = new HashMap<Attribute, Object>();
            Map<Attribute, Object> valueMap;
            recordScopeAttributeValueMapping.put(taxon, attributeValueMapping);
            for (Attribute attr : survey.getAttributes()) {
                if(!AttributeScope.LOCATION.equals(attr.getScope())) {
                    if (AttributeScope.SURVEY.equals(attr.getScope())) {
                        prefix = surveyPrefix;
                        valueMap = surveyScopeAttributeValueMapping;
                    } else {
                        prefix = recordPrefix;
                        valueMap = attributeValueMapping;
                    }

                    key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, prefix, attr.getId());
                    value = "";

                    switch (attr.getType()) {
                    case INTEGER:
                        Integer val = Integer.valueOf(sightingIndex + 30);
                        value = val.toString();
                        valueMap.put(attr, val);
                        break;
                    case INTEGER_WITH_RANGE:
                        valueMap.put(attr, intWithRangeValue);
                        break;
                    case DECIMAL:
                        value = String.format("50.%d", sightingIndex);
                        valueMap.put(attr, Double.parseDouble(value));
                        break;
                    case DATE:
                        Date date = new Date(System.currentTimeMillis());
                        value = dateFormat.format(date);
                        // Reparsing the date strips out the hours, minutes and seconds
                        valueMap.put(attr, dateFormat.parse(value));
                        break;
                    case REGEX:
                    case STRING_AUTOCOMPLETE:
                    case STRING:
                    case BARCODE:
                    case TIME:
                    case HTML:
                    case HTML_COMMENT:
                    case HTML_HORIZONTAL_RULE:
                        value = String.format("String %d", sightingIndex);
                        valueMap.put(attr, value);
                        break;
                    case TEXT:
                        value = String.format("Text %d", sightingIndex);
                        valueMap.put(attr, value);
                        break;
                    case STRING_WITH_VALID_VALUES:
                        value = attr.getOptions().get(sightingIndex).getValue();
                        valueMap.put(attr, value);
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
                        valueMap.put(attr, value);
                        break;
                    case FILE:
                        String file_filename = String.format("attribute_%d", attr.getId());
                        MockMultipartFile mockFileFile = new MockMultipartFile(key,
                                file_filename, "audio/mpeg",
                                file_filename.getBytes());
                        ((MockMultipartHttpServletRequest) request).addFile(mockFileFile);
                        valueMap.put(attr, mockFileFile);
                        break;
                    case IMAGE:
                        String image_filename = String.format("attribute_%d", attr.getId());
                        MockMultipartFile mockImageFile = new MockMultipartFile(
                                key, image_filename, "image/png",
                                image_filename.getBytes());
                        ((MockMultipartHttpServletRequest) request).addFile(mockImageFile);
                        valueMap.put(attr, mockImageFile);
                        break;
                    default:
                        Assert.assertTrue("Unknown Attribute Type: "
                                + attr.getType().toString(), false);
                        break;
                    }
                    if(value != null) {
                        params.put(key, value);
                    }
                }
            }
            sightingIndex += 1;
        }

        request.setParameters(params);
        ModelAndView mv = handle(request, response);
        Assert.assertEquals(2, recordDAO.countAllRecords().intValue());

        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals(redirectionService.getMySightingsUrl(survey), redirect.getUrl());

        sightingIndex = 0;
        for (IndicatorSpecies taxon : new IndicatorSpecies[] { speciesA,
                speciesB }) {
            List<Record> records = recordDAO.getRecords(taxon);
            Assert.assertEquals(1, records.size());
            Record record = records.get(0);

            Assert.assertEquals(survey.getId(), record.getSurvey().getId());
            // Coordinates are truncates to 6 decimal points
            Assert.assertEquals(new Double(params.get("latitude")).doubleValue(), record.getPoint().getY(), Math.pow(10, -6));
            Assert.assertEquals(new Double(params.get("longitude")).doubleValue(), record.getPoint().getX(), Math.pow(10, -6));
            Assert.assertEquals(sightingDate, record.getWhen());
            Assert.assertEquals(sightingDate.getTime(), record.getTime().longValue());
            Assert.assertEquals(params.get("notes"), record.getNotes());

            Assert.assertEquals(taxon, record.getSpecies());
            Assert.assertEquals(sightingIndex + 21, record.getNumber().intValue());

            Map<Attribute, Object> attributeValueMap = recordScopeAttributeValueMapping.get(taxon);
            Object expected;
            for (TypedAttributeValue recAttr : record.getAttributes()) {
                if (AttributeScope.SURVEY.equals(recAttr.getAttribute().getScope())) {
                    expected = surveyScopeAttributeValueMapping.get(recAttr.getAttribute());
                } else {
                    expected = attributeValueMap.get(recAttr.getAttribute());
                }

                switch (recAttr.getAttribute().getType()) {
                case INTEGER:
                case INTEGER_WITH_RANGE:
                    Assert.assertEquals(expected, recAttr.getNumericValue().intValue());
                    break;
                case DECIMAL:
                    Assert.assertEquals(expected, recAttr.getNumericValue().doubleValue());
                    break;
                case DATE:
                    Assert.assertEquals(expected, recAttr.getDateValue());
                    break;
                case REGEX:
                case STRING_AUTOCOMPLETE:
                case STRING:
                case TEXT:
                case BARCODE:
                case TIME:
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    Assert.assertEquals(expected, recAttr.getStringValue());
                    break;
                case STRING_WITH_VALID_VALUES:
                    Assert.assertEquals(expected, recAttr.getStringValue());
                    break;
                case MULTI_CHECKBOX:
	                {
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
	            	Assert.assertEquals(Boolean.parseBoolean(expected.toString()), 
	            			Boolean.parseBoolean(recAttr.getStringValue()));
	            	break;  
                case FILE:
                case IMAGE:
                    String filename = ((MockMultipartFile) expected).getOriginalFilename();
                    Assert.assertEquals(filename, recAttr.getStringValue());
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + recAttr.getAttribute().getType().toString(), false);
                    break;
                }
            }
            sightingIndex += 1;
        }

        // Test Save and Add Another 
        request.setParameter("submitAndAddAnother", "submitAndAddAnother");
        mv = handle(request, response);
        Assert.assertEquals(4, recordDAO.countAllRecords().intValue());
        
        // get all the records.
        List<Record> recList = recordDAO.search(null, null, null).getList();
        for (Record r : recList) {
            Assert.assertEquals("record should be set to same visibility as the survey default record visibility", survey.getDefaultRecordVisibility(), r.getRecordVisibility());
        }

        Assert.assertTrue(mv.getView() instanceof RedirectView);
        redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/user/surveyRenderRedirect.htm", redirect.getUrl());
        
        this.assertMessageCode(SingleSiteController.MSG_CODE_SUCCESS_ADD_ANOTHER);
    }
    
    @Test
    public void testRecordFormPredefinedLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteMultiTaxa.htm", true, SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA, true);
    }
    
    @Test
    public void testRecordFormLocationsAsSurveyOwner() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteMultiTaxa.htm", false, SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA, true);
    }
    
    @Test
    public void testRecordFormPredefinedLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteMultiTaxa.htm", true, SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA, false);
    }
    
    @Test
    public void testRecordFormLocations() throws Exception {
        super.testRecordLocations("/bdrs/user/singleSiteMultiTaxa.htm", false, SurveyFormRendererType.SINGLE_SITE_MULTI_TAXA, false);
    }

    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return super.createUploadRequest();
    }
}
