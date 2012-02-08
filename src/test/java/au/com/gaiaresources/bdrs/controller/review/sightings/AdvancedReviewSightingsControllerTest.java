package au.com.gaiaresources.bdrs.controller.review.sightings;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
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
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.hibernate.FlushMode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.map.RecordDownloadFormat;
import au.com.gaiaresources.bdrs.db.impl.HqlQuery.SortOrder;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.preference.Preference;
import au.com.gaiaresources.bdrs.model.preference.PreferenceDAO;
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
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.facet.CensusMethodTypeFacet;
import au.com.gaiaresources.bdrs.service.facet.CensusMethodTypeFacetOption;
import au.com.gaiaresources.bdrs.service.facet.Facet;
import au.com.gaiaresources.bdrs.service.facet.FacetService;
import au.com.gaiaresources.bdrs.service.facet.LocationAttributeFacet;
import au.com.gaiaresources.bdrs.service.facet.MonthFacet;
import au.com.gaiaresources.bdrs.service.facet.SurveyFacet;
import au.com.gaiaresources.bdrs.service.facet.TaxonGroupFacet;
import au.com.gaiaresources.bdrs.service.facet.UserFacet;
import au.com.gaiaresources.bdrs.util.KMLUtils;

/**
 * Tests all aspects of the <code>AdvancedReviewSightingsController</code>.
 */
public class AdvancedReviewSightingsControllerTest extends
        AbstractControllerTest {
    private DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

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
    private PreferenceDAO prefDAO;
    @Autowired
    private CensusMethodDAO methodDAO;
    @Autowired
    private LocationService locationService;
    @Autowired
    private FacetService facetService;
    @Autowired
    private AdvancedReviewSightingsController controller;

    private TaxonGroup taxonGroupBirds;
    private TaxonGroup taxonGroupFrogs;
    private IndicatorSpecies speciesA;
    private IndicatorSpecies speciesB;
    private IndicatorSpecies speciesC;
    private CensusMethod methodA;
    private CensusMethod methodC;
    private CensusMethod methodB;
    private User user;
    private User admin;
    private Date dateA;
    private Date dateB;
    /**
     * Total number of records created by setup.
     */
    private int recordCount;
    /**
     * Total number of records created for each survey by setup.
     */
    private int surveyRecordCount;
    private Map<User, Integer> userRecordCount = new HashMap<User, Integer>();
    /**
     * Total number of records created for each census method.
     */
    private int methodRecordCount;
    /**
     * Total number of records created for each indicator species.
     */
    private int taxonRecordCount;

    @Before
    public void setUp() throws Exception {
        System.err.println("setUp");
        
        dateA = dateFormat.parse("27 Jun 2004");
        dateB = dateFormat.parse("02 Oct 2005");

        taxonGroupBirds = new TaxonGroup();
        taxonGroupBirds.setName("Birds");
        taxonGroupBirds = taxaDAO.save(taxonGroupBirds);

        taxonGroupFrogs = new TaxonGroup();
        taxonGroupFrogs.setName("Frogs");
        taxonGroupFrogs = taxaDAO.save(taxonGroupFrogs);

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

                    if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)
                            || AttributeType.MULTI_CHECKBOX.equals(attrType)
                            || AttributeType.MULTI_SELECT.equals(attrType)) {
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

        speciesC = new IndicatorSpecies();
        speciesC.setCommonName("Indicator Species C");
        speciesC.setScientificName("Indicator Species C");
        speciesC.setTaxonGroup(taxonGroupFrogs);
        speciesC = taxaDAO.save(speciesC);

        HashSet<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
        speciesSet.add(speciesA);
        speciesSet.add(speciesB);
        speciesSet.add(speciesC);

        methodA = new CensusMethod();
        methodA.setName("Method A");
        methodA.setTaxonomic(Taxonomic.TAXONOMIC);
        methodA.setType("Type X");
        methodA = methodDAO.save(methodA);

        methodB = new CensusMethod();
        methodB.setName("Method B");
        methodB.setTaxonomic(Taxonomic.OPTIONALLYTAXONOMIC);
        methodB.setType("Type X");
        methodB = methodDAO.save(methodB);

        methodC = new CensusMethod();
        methodC.setName("Method C");
        methodC.setTaxonomic(Taxonomic.NONTAXONOMIC);
        methodC.setType("Type Y");
        methodC = methodDAO.save(methodC);

        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String emailAddr = "abigail.ambrose@example.com";
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(au.com.gaiaresources.bdrs.util.StringUtils.generateRandomString(10, 50), emailAddr);

        user = userDAO.createUser("testuser", "Abigail", "Ambrose", emailAddr, encodedPassword, registrationKey, new String[] { Role.USER });

        admin = userDAO.getUser("admin");

        int surveyIndex = 1;
        for (CensusMethod method : new CensusMethod[] { methodA, methodB,
                methodC, null }) {
            List<Attribute> attributeList = new ArrayList<Attribute>();
            Attribute attr;
            for (AttributeType attrType : AttributeType.values()) {
                for (AttributeScope scope : new AttributeScope[] {
                        AttributeScope.RECORD, AttributeScope.SURVEY,
                        AttributeScope.LOCATION,
                        AttributeScope.RECORD_MODERATION,
                        AttributeScope.SURVEY_MODERATION, null }) {

                    attr = new Attribute();
                    String scopeName = scope == null ? "null" : scope.toString();
                    attr.setDescription(scopeName + " "+ attrType.toString() + " description");
                    attr.setRequired(true);
                    attr.setName(attrType.toString());
                    attr.setTypeCode(attrType.getCode());
                    attr.setScope(scope);
                    attr.setTag(false);

                    if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)
                            || AttributeType.MULTI_CHECKBOX.equals(attrType)
                            || AttributeType.MULTI_SELECT.equals(attrType)) {
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
                    }

                    attr = taxaDAO.save(attr);
                    attributeList.add(attr);
                }
            }

            Survey survey = new Survey();
            survey.setName(String.format("Survey %d", surveyIndex));
            survey.setActive(true);
            survey.setStartDate(new Date());
            survey.setDescription(String.format("Survey %d", surveyIndex)
                    + " Description");

            Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
            metadataDAO.save(md);

            survey.setAttributes(attributeList);
            survey.setSpecies(new HashSet<IndicatorSpecies>(speciesSet));
            survey.getCensusMethods().add(method);

            survey = surveyDAO.save(survey);
            survey.setLocations(createLocations(survey));
            survey = surveyDAO.save(survey);
            surveyRecordCount = 0;
            taxonRecordCount = 0;
            methodRecordCount = 0;

            for (Location loc : survey.getLocations()) {
                for (IndicatorSpecies species : survey.getSpecies()) {
                    for (CensusMethod cm : survey.getCensusMethods()) {
                        for (User u : new User[] { admin, user }) {
                            createRecord(survey, cm, loc, species, u);

                            recordCount++;
                            surveyRecordCount++;

                            int thisUserRecordCount = 1;
                            if (userRecordCount.containsKey(u)) {
                                thisUserRecordCount = userRecordCount.get(u) + 1;
                            }
                            userRecordCount.put(u, thisUserRecordCount);

                            methodRecordCount++;
                            taxonRecordCount++;
                        }
                    }
                }
            }
            surveyIndex += 1;
        }

        getRequestContext().getHibernate().flush();
    }

    private List<Location> createLocations(Survey survey) {

        List<Location> locList = new ArrayList<Location>();
        for (int i = 0; i < 3; i++) {
            locList.add(createLocation(survey, i));
        }
        return locList;
    }

    private Location createLocation(Survey survey, int index) {
        Location loc = new Location();
        loc.setName(String.format("Location %d", index));
        loc.setLocation(locationService.createPoint(-40.58, 153.1));
        for (Attribute attr : survey.getAttributes()) {
            if (AttributeScope.LOCATION.equals(attr.getScope())) {
                List<AttributeOption> opts = attr.getOptions();
                AttributeValue attrVal = new AttributeValue();
                attrVal.setAttribute(attr);
                switch (attr.getType()) {
                case INTEGER:
                    Integer i = Integer.valueOf(123);
                    attrVal.setNumericValue(new BigDecimal(i));
                    attrVal.setStringValue(i.toString());
                    break;
                case INTEGER_WITH_RANGE:
                    String intStr = attr.getOptions().iterator().next().getValue();
                    attrVal.setNumericValue(new BigDecimal(
                            Integer.parseInt(intStr)));
                    attrVal.setStringValue(intStr);
                    break;
                case DECIMAL:
                    Double d = new Double(123);
                    attrVal.setNumericValue(new BigDecimal(d));
                    attrVal.setStringValue(d.toString());
                    break;
                case DATE:
                    Date date = new Date(System.currentTimeMillis());
                    attrVal.setDateValue(date);
                    attrVal.setStringValue(dateFormat.format(date));
                    break;
                case STRING_AUTOCOMPLETE:
                case STRING:
                    attrVal.setStringValue("This is a test string record attribute");
                    break;
                case TEXT:
                    attrVal.setStringValue("This is a test text record attribute");
                    break;
                case REGEX:
                case BARCODE:
                    attrVal.setStringValue("#454545");
                    break;
                case TIME:
                    attrVal.setStringValue("12:34");
                    break;
                case HTML:
                case HTML_NO_VALIDATION:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    attrVal.setStringValue("<hr/>");
                    break;
                case STRING_WITH_VALID_VALUES:
                    attrVal.setStringValue(opts.iterator().next().getValue());
                    break;
                case MULTI_CHECKBOX:
                    attrVal.setMultiCheckboxValue(new String[] {
                            opts.get(0).getValue(), opts.get(1).getValue() });
                    break;
                case MULTI_SELECT:
                    attrVal.setMultiCheckboxValue(new String[] {
                            opts.get(0).getValue(), opts.get(1).getValue() });
                    break;
                case SINGLE_CHECKBOX:
                    attrVal.setBooleanValue(Boolean.TRUE.toString());
                    break;
                case FILE:
                    attrVal.setStringValue("testDataFile.dat");
                    break;
                case IMAGE:
                    attrVal.setStringValue("testImgFile.png");
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "
                            + attr.getType().toString(), false);
                    break;
                }
                attrVal = recordDAO.saveAttributeValue(attrVal);
                loc.getAttributes().add(attrVal);
            }
        }

        loc = locationDAO.save(loc);
        return loc;
    }

    private Record createRecord(Survey survey, CensusMethod cm, Location loc,
            IndicatorSpecies species, User user) throws ParseException {
        //        if(cm == null) {
        //            System.err.println("Null Census Method Record in "+survey.getName()+" for "+species.getScientificName()+" by "+user.getFullName());
        //        } else {
        //            System.err.println(cm.getTaxonomic().getName()+":"+cm.getType()+" record in "+survey.getName()+" for "+species.getScientificName()+" by "+user.getFullName());
        //        }

        Date recDate = admin.equals(user) ? dateA : dateB;

        Record record = new Record();
        record.setSurvey(survey);
        if (cm != null && Taxonomic.NONTAXONOMIC.equals(cm.getTaxonomic())) {
            record.setSpecies(null);
        } else {
            record.setSpecies(species);
        }
        record.setCensusMethod(cm);
        record.setUser(user);
        record.setLocation(loc);
        record.setPoint(locationService.createPoint(-32.42, 154.15));
        record.setHeld(false);
        record.setWhen(recDate);
        record.setTime(recDate.getTime());
        record.setLastDate(recDate);
        record.setLastTime(recDate.getTime());
        record.setNotes("This is a test record");
        record.setFirstAppearance(false);
        record.setLastAppearance(false);
        record.setBehaviour("Behaviour notes");
        record.setHabitat("Habitat Notes");
        record.setNumber(1);
        // records need to be public for the tests to work as written
        record.setRecordVisibility(RecordVisibility.PUBLIC);

        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Set<AttributeValue> attributeList = new HashSet<AttributeValue>();
        Map<Attribute, AttributeValue> expectedRecordAttrMap = new HashMap<Attribute, AttributeValue>();
        for (Attribute attr : survey.getAttributes()) {
            if (!AttributeScope.LOCATION.equals(attr.getScope())) {
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
                    String intStr = attr.getOptions().iterator().next().getValue();
                    recAttr.setNumericValue(new BigDecimal(
                            Integer.parseInt(intStr)));
                    recAttr.setStringValue(intStr);
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
                case REGEX:
                case BARCODE:
                    recAttr.setStringValue("#454545");
                    break;
                case TIME:
                    recAttr.setStringValue("12:34");
                    break;
                case HTML:
                case HTML_NO_VALIDATION:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    recAttr.setStringValue("<hr/>");
                    break;
                case STRING_WITH_VALID_VALUES:
                    recAttr.setStringValue(opts.iterator().next().getValue());
                    break;
                case MULTI_CHECKBOX:
                    recAttr.setMultiCheckboxValue(new String[] {
                            opts.get(0).getValue(), opts.get(1).getValue() });
                    break;
                case MULTI_SELECT:
                    recAttr.setMultiCheckboxValue(new String[] {
                            opts.get(0).getValue(), opts.get(1).getValue() });
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

        if (record.getSpecies() != null) {
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
                        String intStr = attr.getOptions().iterator().next().getValue();
                        recAttr.setNumericValue(new BigDecimal(
                                Integer.parseInt(intStr)));
                        recAttr.setStringValue(intStr);
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
                    case BARCODE:
                        recAttr.setStringValue("#454545");
                        break;
                    case TIME:
                        recAttr.setStringValue("12:34");
                        break;
                    case HTML:
                    case HTML_NO_VALIDATION:
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
                    case MULTI_CHECKBOX: {
                        List<AttributeOption> opts = attr.getOptions();
                        recAttr.setMultiCheckboxValue(new String[] {
                                opts.get(0).getValue(), opts.get(1).getValue() });
                    }
                        break;
                    case MULTI_SELECT: {
                        List<AttributeOption> opts = attr.getOptions();
                        recAttr.setMultiSelectValue(new String[] {
                                opts.get(0).getValue(), opts.get(1).getValue() });
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
        }

        record.setAttributes(attributeList);
        return recordDAO.saveRecord(record);
    }
    
    private void resetRequest() {
        request.removeAllParameters();
        response = new MockHttpServletResponse();
    }
    
    @Test
    public void testAdvancedReviewSightings() throws Exception {
        testAdminUserFacet();
        resetRequest();

        testAdvancedReviewDownload();
        resetRequest();
        
        testAllPublicRecordsUserFacetSelection();
        resetRequest();
        
        testAnonymousView();
        resetRequest();
        
        testCensusMethodObservationType();
        resetRequest();
        
        testCensusMethodOneType();
        resetRequest();

        testCensusMethodTwoTypes();
        resetRequest();

        testJSONSightings();
        resetRequest();

        testKMLSightings();
        resetRequest();

        testMapSightings();
        resetRequest();

        testMultipleUserFacet();
        resetRequest();

        testNoSurveyFacetWithSurveyID();
        resetRequest();

        testOneMonthFacet();
        resetRequest();

        testOneTaxonGroupFacet();
        resetRequest();

        testSightingsSearchText();
        resetRequest();

        testSightingsSorting();
        resetRequest();

        testSurveyFacetNoneSelected();
        resetRequest();

        testSurveyFacetTwoSelected();
        resetRequest();

        testTableSightings();
        resetRequest();

        testTwoMonthFacet();
        resetRequest();

        testTwoTaxonGroupFacet();
        resetRequest();

        testUserUserFacet();
        resetRequest();

        testUserViewUserFacet();
        resetRequest();

        // Saves a preference so do this test last
        testOneLocationAttributeFacet();
        resetRequest();
    }

    //@Test
    public void testSurveyFacetNoneSelected() throws Exception {
        Survey survey = surveyDAO.getSurveys(admin).get(0);
        List<Survey> surveyList = new ArrayList<Survey>();
        surveyList.add(survey);

        testSurveyFacetSelection(surveyList, surveyRecordCount);
    }

    //@Test
    public void testSurveyFacetTwoSelected() throws Exception {
        List<Survey> allSurveys = surveyDAO.getSurveys(admin);
        List<Survey> surveyList = new ArrayList<Survey>();
        surveyList.add(allSurveys.get(0));
        surveyList.add(allSurveys.get(1));

        testSurveyFacetSelection(surveyList, 2 * surveyRecordCount);
    }

    //@Test
    public void testNoSurveyFacetWithSurveyID() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        Survey survey = surveyDAO.getSurveys(admin).get(0);

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        request.addParameter(SurveyFacet.SURVEY_ID_QUERY_PARAM_NAME, survey.getId().toString());
        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        for (Facet facet : facetList) {
            if (facet instanceof SurveyFacet) {
                Assert.assertFalse(facet.isActive());
            }
        }

        Integer surveyId = new Integer(
                mv.getModel().get(SurveyFacet.SURVEY_ID_QUERY_PARAM_NAME).toString());
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, surveyId, null, null, null, null, null);
        Assert.assertEquals(surveyRecordCount, recordList.size());
    }

    //@Test
    public void testMapSightings() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        request.addParameter("viewType", "map");

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");
    }

    //@Test
    public void testTableSightings() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        request.addParameter("viewType", "table");

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeValue(mv, AdvancedReviewSightingsController.MODEL_TABLE_VIEW_SELECTED, true);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");
    }

    //@Test
    public void testKMLSightings() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReviewKMLSightings.htm");
        request.addParameter("viewType", "map");

        handle(request, response);
        Assert.assertEquals(KMLUtils.KML_CONTENT_TYPE, response.getContentType());
        Assert.assertTrue(response.getContentAsString().length() > 0);
    }

    //@Test
    public void testJSONSightings() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReviewJSONSightings.htm");
        request.addParameter("viewType", "table");

        handle(request, response);
        Assert.assertEquals("application/json", response.getContentType());
        Assert.assertTrue(response.getContentAsString().length() > 0);
        Assert.assertTrue(JSONArray.fromObject(response.getContentAsString()).size() > 0);
    }

    //@Test
    public void testAdvancedReviewDownload() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        Survey survey = surveyDAO.getSurveys(admin).get(0);
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReviewDownload.htm");
        request.addParameter(SurveyFacet.SURVEY_ID_QUERY_PARAM_NAME, survey.getId().toString());
        request.addParameter(AdvancedReviewSightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.KML.toString());
        request.addParameter(AdvancedReviewSightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.XLS.toString());
        request.addParameter(AdvancedReviewSightingsController.QUERY_PARAM_DOWNLOAD_FORMAT, RecordDownloadFormat.SHAPEFILE.toString());

        handle(request, response);
        Assert.assertEquals(AdvancedReviewSightingsController.SIGHTINGS_DOWNLOAD_CONTENT_TYPE, response.getContentType());
        Assert.assertTrue(response.getContentAsByteArray().length > 0);
    }

    //@Test
    public void testSightingsSorting() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");

        for (String sortProperty : AdvancedReviewSightingsController.VALID_SORT_PROPERTIES) {
            for (SortOrder sortOrder : SortOrder.values()) {
                List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, sortProperty, sortOrder.toString(), null, null, null);
                Assert.assertEquals(recordCount, recordList.size());
            }
        }
    }

    //@Test
    public void testSightingsSearchText() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        request.addParameter(AdvancedReviewSightingsController.SEARCH_QUERY_PARAM_NAME, speciesA.getScientificName());
        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");

        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, request.getParameter(AdvancedReviewSightingsController.SEARCH_QUERY_PARAM_NAME), null, null);
        // Some of the records are non taxonomic
        Assert.assertEquals((methodRecordCount * 3) / 3, recordList.size());
    }

    //@Test
    public void testCensusMethodOneType() throws Exception {
        List<CensusMethod> methodList = new ArrayList<CensusMethod>();
        methodList.add(methodA);
        methodList.add(methodB);

        testCensusMethodSelection(methodList, 2 * methodRecordCount);
    }

    //@Test
    public void testCensusMethodTwoTypes() throws Exception {
        List<CensusMethod> methodList = new ArrayList<CensusMethod>();
        methodList.add(methodA);
        methodList.add(methodB);
        methodList.add(methodC);

        testCensusMethodSelection(methodList, 3 * methodRecordCount);
    }

    //@Test
    public void testCensusMethodObservationType() throws Exception {
        List<CensusMethod> methodList = new ArrayList<CensusMethod>();
        methodList.add(methodA);
        methodList.add(methodB);
        methodList.add(methodC);
        methodList.add(null);

        testCensusMethodSelection(methodList, 4 * methodRecordCount);
    }

    //@Test
    public void testOneTaxonGroupFacet() throws Exception {

        List<TaxonGroup> groupList = new ArrayList<TaxonGroup>();
        groupList.add(taxonGroupBirds);

        testTaxonGroupFacetSelection(groupList, 2 * taxonRecordCount);
    }

    //@Test
    public void testTwoTaxonGroupFacet() throws Exception {

        List<TaxonGroup> groupList = new ArrayList<TaxonGroup>();
        groupList.add(taxonGroupBirds);
        groupList.add(taxonGroupFrogs);

        testTaxonGroupFacetSelection(groupList, 3 * taxonRecordCount);
    }

    //@Test
    public void testOneMonthFacet() throws Exception {
        List<Long> dateList = new ArrayList<Long>();
        Calendar cal = new GregorianCalendar();
        for (Date d : new Date[] { dateA }) {
            cal.setTime(d);
            dateList.add(Integer.valueOf(cal.get(Calendar.MONTH)).longValue() + 1);
        }

        testMonthFacetSelection(dateList, recordCount / 2);
    }

    //@Test
    public void testTwoMonthFacet() throws Exception {

        List<Long> dateList = new ArrayList<Long>();
        Calendar cal = new GregorianCalendar();
        for (Date d : new Date[] { dateA, dateB }) {
            cal.setTime(d);
            dateList.add(Integer.valueOf(cal.get(Calendar.MONTH)).longValue() + 1);
        }

        testMonthFacetSelection(dateList, recordCount);
    }

    //@Test
    public void testAdminUserFacet() throws Exception {

        List<User> userList = new ArrayList<User>();
        userList.add(admin);

        testUserFacetSelection(userList, admin, recordCount / 2);
    }

    //@Test
    public void testUserUserFacet() throws Exception {

        List<User> userList = new ArrayList<User>();
        userList.add(user);

        testUserFacetSelection(userList, admin, userRecordCount.get(user));
    }

    //@Test
    public void testMultipleUserFacet() throws Exception {

        List<User> userList = new ArrayList<User>();
        userList.add(user);
        userList.add(admin);

        testUserFacetSelection(userList, admin, userRecordCount.get(user)
                + userRecordCount.get(admin));
    }

    //@Test
    public void testUserViewUserFacet() throws Exception {
        List<User> userList = new ArrayList<User>();
        userList.add(user);

        testUserFacetSelection(userList, user, userRecordCount.get(user));
    }

    private void testCensusMethodSelection(List<CensusMethod> methodList,
            int expectedCount) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        Set<String> methodTypes = new HashSet<String>();
        for (CensusMethod method : methodList) {
            if (method == null) {
                methodTypes.add(CensusMethodTypeFacetOption.NOCENSUSMETHODVALUE);
            } else {
                methodTypes.add(method.getType());
            }
        }

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");

        List<Facet> facets = getFacetInstancesByType(CensusMethodTypeFacet.class);
        for (Facet facet : facets) {
            for (String type : methodTypes) {
                request.addParameter(facet.getInputName(), type);
            }
        }
        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(expectedCount, recordList.size());

        for (Record rec : recordList) {
            if (rec.getCensusMethod() != null) {
                Assert.assertTrue(methodTypes.contains(rec.getCensusMethod().getType()));
            }
        }
    }

    private void testSurveyFacetSelection(List<Survey> surveyList,
            int expectedRecordCount) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");

        List<Facet> facets = getFacetInstancesByType(SurveyFacet.class);
        for (Facet facet : facets) {
            for (Survey survey : surveyList) {
                request.addParameter(facet.getInputName(), survey.getId().toString());
            }
        }
        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(expectedRecordCount, recordList.size());

        for (Record rec : recordList) {
            Assert.assertTrue(surveyList.contains(rec.getSurvey()));
        }
    }

    private void deselectMyRecordsOnly() {
        List<Facet> facets = getFacetInstancesByType(UserFacet.class);
        for (Facet facet : facets) {
            request.addParameter(facet.getInputName(), String.valueOf(-1));
        }
    }

    //@Test
    public void testOneLocationAttributeFacet() throws Exception {

        Survey survey = surveyDAO.getSurveyByName("Survey 1");
        Location loc = survey.getLocations().get(0);
        List<AttributeValue> locAttrList = new ArrayList<AttributeValue>();
        locAttrList.add(loc.getAttributes().iterator().next());

        testLocationAttributeFacetSelection(locAttrList);
    }

    private void testLocationAttributeFacetSelection(List<AttributeValue> locAttrValueList) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");

        Preference pref = prefDAO.getPreferenceByKey(LocationAttributeFacet.class.getCanonicalName());
        JSONArray jsonArray = new JSONArray();
        for (AttributeValue attrVal : locAttrValueList) {
            Attribute attr = attrVal.getAttribute();
            JSONObject prefValue = new JSONObject();
            prefValue.put(Facet.JSON_ACTIVE_KEY, Facet.DEFAULT_ACTIVE_CONFIG);
            prefValue.put(Facet.JSON_WEIGHT_KEY, Facet.DEFAULT_WEIGHT_CONFIG);
            prefValue.put(Facet.JSON_NAME_KEY, attr.getDescription());
            prefValue.put(LocationAttributeFacet.JSON_ATTRIBUTE_NAME_KEY, attr.getDescription());

            jsonArray.add(prefValue);
        }
        pref.setValue(jsonArray.toString());
        pref = prefDAO.save(pref);

        List<String> attrNames = new ArrayList<String>();
        List<Facet> facets = getFacetInstancesByType(LocationAttributeFacet.class);
        for (Facet facet : facets) {
            for (AttributeValue attrVal : locAttrValueList) {
                request.addParameter(facet.getInputName(), attrVal.getStringValue());
                attrNames.add(attrVal.getStringValue());
            }
        }

        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);

        for (Record rec : recordList) {
            Location loc = rec.getLocation();
            Assert.assertNotNull(loc);

            boolean isFound = false;
            for (AttributeValue attrVal : loc.getAttributes()) {
                isFound = isFound || attrNames.contains(attrVal.getStringValue());
            }
            Assert.assertTrue(isFound);
        }
    }

    private void testTaxonGroupFacetSelection(List<TaxonGroup> groupList,
            int expectedRecordCount) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");

        List<Facet> facets = getFacetInstancesByType(TaxonGroupFacet.class);
        for (Facet facet : facets) {
            for (TaxonGroup group : groupList) {
                request.addParameter(facet.getInputName(), group.getId().toString());
            }
        }
        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(expectedRecordCount, recordList.size());

        for (Record rec : recordList) {
            if (rec.getSpecies() != null) {
                Assert.assertTrue(groupList.contains(rec.getSpecies().getTaxonGroup()));
            }
        }
    }

    private void testUserFacetSelection(List<User> userList, User loginUser,
            int expectedRecordCount) throws Exception {
        login(loginUser.getName(), loginUser.getPassword(), loginUser.getRoles());

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        
        List<Facet> facets = getFacetInstancesByType(UserFacet.class);
        for (Facet facet : facets) {
            for (User user : userList) {
                request.addParameter(facet.getInputName(), user.getId().toString());
            }
        }

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(expectedRecordCount, recordList.size());

        for (Record rec : recordList) {
            Assert.assertTrue(userList.contains(rec.getUser()));
        }
    }

    private void testMonthFacetSelection(List<Long> monthlist,
            int expectedRecordCount) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");

        Calendar cal = new GregorianCalendar();

        Set<Integer> monthSet = new HashSet<Integer>(monthlist.size());
        List<Facet> facets = getFacetInstancesByType(MonthFacet.class);
        for (Facet facet : facets) {
            for (Long date : monthlist) {
                request.addParameter(facet.getInputName(), date.toString());
                monthSet.add(date.intValue());
            }
        }

        deselectMyRecordsOnly();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(expectedRecordCount, recordList.size());

        for (Record rec : recordList) {
            cal.setTime(rec.getWhen());
            Integer monthInt = cal.get(Calendar.MONTH);
            Assert.assertTrue(monthSet.contains(Integer.valueOf(monthInt + 1)));
        }
    }

    private List<Facet> getFacetInstancesByType(Class<? extends Facet> klass) {
        List<Facet> facetList = new ArrayList<Facet>();
        User user = userDAO.getUser("admin");
        for (Facet facet : facetService.getFacetList(user, new HashMap<String, String[]>())) {
            if (facet.getClass().equals(klass)) {
                facetList.add(facet);
            }
        }

        return facetList;
    }

    //@Test
    public void testAnonymousView() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");
        deselectMyRecordsOnly();

        Calendar cal = new GregorianCalendar();

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(recordCount, recordList.size());
    }

    //@Test
    public void testAllPublicRecordsUserFacetSelection() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/review/sightings/advancedReview.htm");

        List<Facet> facets = getFacetInstancesByType(UserFacet.class);
        for (Facet facet : facets) {
            request.addParameter(facet.getInputName(), String.valueOf(-1));
        }

        ModelAndView mv = handle(request, response);
        getRequestContext().getHibernate().flush();
        getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
        ModelAndViewAssert.assertViewName(mv, "advancedReview");

        ModelAndViewAssert.assertModelAttributeAvailable(mv, "mapViewSelected");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortBy");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "sortOrder");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "searchText");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "facetList");

        List<Facet> facetList = (List<Facet>) mv.getModel().get("facetList");
        List<Record> recordList = controller.getMatchingRecordsAsList(facetList, null, null, null, null, null, null);
        Assert.assertEquals(recordCount, recordList.size());
    }
}
