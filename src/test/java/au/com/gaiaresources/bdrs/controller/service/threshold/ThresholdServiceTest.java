package au.com.gaiaresources.bdrs.controller.service.threshold;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.email.impl.MockEmail;
import au.com.gaiaresources.bdrs.email.impl.MockEmailService;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
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
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.ActionType;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Operator;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.content.ContentService;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.threshold.ActionHandler;
import au.com.gaiaresources.bdrs.service.threshold.ComplexTypeOperator;
import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.EmailActionHandler;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.HoldRecordHandler;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.ModerationEmailActionHandler;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

public class ThresholdServiceTest extends AbstractControllerTest {

    @Autowired
    private RecordDAO recordDAO;

    @Autowired
    private TaxaDAO taxaDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private SurveyDAO surveyDAO;
    
    @Autowired
    private MetadataDAO metadataDAO;
    
    @Autowired
    private ThresholdService thresholdService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private ContentService contentService;
    
    @Autowired
    private RedirectionService redirService;
    
    @Test
    public void testEqualsStringCondition() throws Exception {

        Survey testSurveyMatch = new Survey();
        testSurveyMatch.setName("Test Survey 123");

        Survey testSurveyNoMatch = new Survey();
        testSurveyNoMatch.setName("Test Survey Fail");

        Condition condition = new Condition();
        condition.setPropertyPath("name");
        condition.setClassName(Survey.class.getCanonicalName());
        condition.setValueOperator(Operator.EQUALS);
        condition.setValue(testSurveyMatch.getName());

        Assert.assertTrue(condition.isSimplePropertyType());

        boolean result = condition.applyCondition(getRequestContext().getHibernate(), testSurveyMatch, thresholdService);
        Assert.assertTrue(result);

        result = condition.applyCondition(getRequestContext().getHibernate(), testSurveyNoMatch, thresholdService);
        Assert.assertFalse(result);
    }

    @Test
    public void testEqualsIntegerCondition() throws Exception {

        Record testRecordMatch = new Record();
        testRecordMatch.setNumber(123);

        Record testRecordNoMatch = new Record();
        testRecordNoMatch.setNumber(124);

        Condition condition = new Condition();
        condition.setPropertyPath("number");
        condition.setClassName(Record.class.getCanonicalName());
        condition.setValueOperator(Operator.EQUALS);
        condition.setValue(testRecordMatch.getNumber());

        Assert.assertTrue(condition.isSimplePropertyType());

        boolean result = condition.applyCondition(getRequestContext().getHibernate(), testRecordMatch, thresholdService);
        Assert.assertTrue(result);

        result = condition.applyCondition(getRequestContext().getHibernate(), testRecordNoMatch, thresholdService);
        Assert.assertFalse(result);
    }

    @Test
    public void testEqualsBooleanCondition() throws Exception {

        Record testRecordMatch = new Record();
        testRecordMatch.setHeld(true);

        Record testRecordNoMatch = new Record();
        testRecordNoMatch.setHeld(false);

        Condition condition = new Condition();
        condition.setPropertyPath("held");
        condition.setClassName(Record.class.getCanonicalName());
        condition.setValueOperator(Operator.EQUALS);
        condition.setValue(testRecordMatch.getHeld());

        Assert.assertTrue(condition.isSimplePropertyType());

        boolean result = condition.applyCondition(getRequestContext().getHibernate(), testRecordMatch, thresholdService);
        Assert.assertTrue(result);

        result = condition.applyCondition(getRequestContext().getHibernate(), testRecordNoMatch, thresholdService);
        Assert.assertFalse(result);
    }

    @Test
    public void testEqualsDateCondition() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

        Record testRecordMatch = new Record();
        testRecordMatch.setWhen(dateFormat.parse("08 Dec 2010"));

        Record testRecordNoMatch = new Record();
        testRecordNoMatch.setWhen(dateFormat.parse("09 Dec 2010"));

        Condition condition = new Condition();
        condition.setPropertyPath("when");
        condition.setClassName(Record.class.getCanonicalName());
        condition.setValueOperator(Operator.EQUALS);
        condition.setValue(testRecordMatch.getWhen());

        Assert.assertTrue(condition.isSimplePropertyType());

        boolean result = condition.applyCondition(getRequestContext().getHibernate(), testRecordMatch, thresholdService);
        Assert.assertTrue(result);

        result = condition.applyCondition(getRequestContext().getHibernate(), testRecordNoMatch, thresholdService);
        Assert.assertFalse(result);
    }

    @Test
    public void testContainsCondition() throws Exception {
        String contain = "hippie";
        Survey testSurveyMatch = new Survey();
        testSurveyMatch.setName(String.format("Test Survey %s 123", contain));

        Survey testSurveyNoMatch = new Survey();
        testSurveyNoMatch.setName("Test Survey Fail");

        Condition condition = new Condition();
        condition.setPropertyPath("name");
        condition.setClassName(Survey.class.getCanonicalName());
        condition.setValueOperator(Operator.CONTAINS);
        condition.setValue(contain);

        Assert.assertTrue(condition.isSimplePropertyType());

        boolean result = condition.applyCondition(getRequestContext().getHibernate(), testSurveyMatch, thresholdService);
        Assert.assertTrue(result);

        result = condition.applyCondition(getRequestContext().getHibernate(), testSurveyNoMatch, thresholdService);
        Assert.assertFalse(result);
    }

    @Test 
    public void testRecordAttributeConditionLowerLimitOutside() throws Exception{
    	testRecordAttributeCondition("99");
    }
    
    @Test 
    public void testRecordAttributeConditionLowerLimitEdge() throws Exception{
    	testRecordAttributeCondition("100");
    }
    
    @Test 
    public void testRecordAttributeConditionInRange() throws Exception{
    	testRecordAttributeCondition("101");
    }
    
    @Test 
    public void testRecordAttributeConditionUpperLimitEdge() throws Exception{
    	testRecordAttributeCondition("200");
    }
    
    @Test 
    public void testRecordAttributeConditionUpperLimitOutside() throws Exception{
    	testRecordAttributeCondition("201");
    }
    
    public void testRecordAttributeCondition(String intWithRangeValue) throws Exception {
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Map<AttributeType, Object> attributeTypeValueLookup = new HashMap<AttributeType, Object>();
        for (AttributeType at : AttributeType.values()) {
            switch (at) {
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
            case STRING_WITH_VALID_VALUES:
            case IMAGE:
            case FILE:
            case HTML:
            case HTML_NO_VALIDATION:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
                attributeTypeValueLookup.put(at, at.getCode());
                break;
            case REGEX:
            case BARCODE:
                attributeTypeValueLookup.put(at, "#999999");
                break;
            case TIME:
                attributeTypeValueLookup.put(at, "12:34");
                break;
            case INTEGER:
                attributeTypeValueLookup.put(at, new BigDecimal(101));
                break;
            case MULTI_CHECKBOX:
            case MULTI_SELECT:
            	attributeTypeValueLookup.put(at, new String[]{at.getCode()});
            	break;
            case SINGLE_CHECKBOX:
            	attributeTypeValueLookup.put(at, Boolean.TRUE.toString());
            	break;
            case INTEGER_WITH_RANGE:
            	attributeTypeValueLookup.put(at, new BigDecimal(intWithRangeValue));
                break;
            case DECIMAL:
                attributeTypeValueLookup.put(at, new BigDecimal(123.4567));
                break;
            case DATE:
                attributeTypeValueLookup.put(at, dateFormat.parse("08 Dec 2010"));
                break;
            default:
                break;
            }
        }

        TaxonGroup taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        //taxonGroup = taxaDAO.save(taxonGroup);

        IndicatorSpecies speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        //speciesA = taxaDAO.save(speciesA);

        IndicatorSpecies speciesB = new IndicatorSpecies();
        speciesB.setCommonName("Indicator Species B");
        speciesB.setScientificName("Indicator Species B");
        speciesB.setTaxonGroup(taxonGroup);
        //speciesB = taxaDAO.save(speciesB);

        List<AttributeValue> recAttrList = new ArrayList<AttributeValue>();
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for (AttributeType attrType : AttributeType.values()) {
            for (AttributeScope scope : new AttributeScope[] {
                    AttributeScope.RECORD, AttributeScope.SURVEY,
                    AttributeScope.RECORD_MODERATION, AttributeScope.SURVEY_MODERATION }) {

                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(String.format("%s_%s", attrType.toString(), scope.getName()));
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
                        //opt = taxaDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }else if(AttributeType.INTEGER_WITH_RANGE.equals(attrType)){
                	List<AttributeOption> rangeList = new ArrayList<AttributeOption>();
                	AttributeOption upper = new AttributeOption();
                	AttributeOption lower = new AttributeOption();
                	lower.setValue("100");
                	upper.setValue("200");
                	rangeList.add(taxaDAO.save(lower));
                	rangeList.add(taxaDAO.save(upper));
                	attr.setOptions(rangeList);
                }else if(AttributeType.BARCODE.equals(attrType)){
                	List<AttributeOption> regExpList = new ArrayList<AttributeOption>();
                	AttributeOption regExp = new AttributeOption();
                	regExp.setValue("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
                	regExpList.add(taxaDAO.save(regExp));
                	attr.setOptions(regExpList);
                }else if(AttributeType.REGEX.equals(attrType)){
                    List<AttributeOption> regExpList = new ArrayList<AttributeOption>();
                    AttributeOption regExp = new AttributeOption();
                    regExp.setValue("^#([A-Fa-f0-9]{6}|[A-Fa-f0-9]{3})$");
                    regExpList.add(taxaDAO.save(regExp));
                    attr.setOptions(regExpList);
                }

                //attr = taxaDAO.save(attr);
                attributeList.add(attr);

                AttributeValue recAttr = new AttributeValue();
                recAttr.setAttribute(attr);
                switch (attrType) {
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                case STRING_WITH_VALID_VALUES:
                case IMAGE:
                case FILE:
                case REGEX:
                case BARCODE:
                case TIME:
                case HTML:
                case HTML_NO_VALIDATION:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    recAttr.setStringValue((String) attributeTypeValueLookup.get(attrType));
                    break;
                case INTEGER:
                case INTEGER_WITH_RANGE:
                    recAttr.setNumericValue((BigDecimal) attributeTypeValueLookup.get(attrType));
                    break;
                case SINGLE_CHECKBOX:
                	recAttr.setBooleanValue(attributeTypeValueLookup.get(attrType).toString());
                	break;
                case MULTI_CHECKBOX:
                	recAttr.setMultiCheckboxValue((String[])attributeTypeValueLookup.get(attrType));
                	break;
                case MULTI_SELECT:
                	recAttr.setMultiSelectValue((String[])attributeTypeValueLookup.get(attrType));
                	break;
                case DECIMAL:
                    recAttr.setNumericValue((BigDecimal) attributeTypeValueLookup.get(attrType));
                    break;
                case DATE:
                    recAttr.setDateValue((Date) attributeTypeValueLookup.get(attrType));
                    break;
                default:
                    Assert.assertTrue(false);
                    break;
                }
                recAttrList.add(recAttr);
            }
        }

        HashSet<IndicatorSpecies> speciesSet = new HashSet<IndicatorSpecies>();
        speciesSet.add(speciesA);
        speciesSet.add(speciesB);

        Survey survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        //metadataDAO.save(md);
        survey.setAttributes(attributeList);
        survey.setSpecies(speciesSet);
        //survey = surveyDAO.save(survey);

        User admin = userDAO.getUser("admin");

        Location locationA = new Location();
        locationA.setName("Location A");
        locationA.setUser(admin);
        locationA.setLocation(locationService.createPoint(-40.58, 153.1));
        //locationDAO.save(locationA);

        Location locationB = new Location();
        locationB.setName("Location B");
        locationB.setUser(admin);
        locationB.setLocation(locationService.createPoint(-32.58, 154.2));
        //locationDAO.save(locationB);

        // ----------------------------------------

        Record rec = new Record();
        rec.getAttributes().addAll(recAttrList);

        Condition condition = new Condition();
        condition.setClassName(Record.class.getCanonicalName());
        condition.setPropertyPath("attributes");

        Assert.assertEquals(AttributeValue.class, condition.getTargetIterableTypeForPath());
        ComplexTypeOperator operator = ThresholdService.COMPLEX_TYPE_TO_OPERATOR_MAP.get(condition.getTargetIterableTypeForPath());
        for (Operator keyOperator : operator.getKeyOperators()) {
            for (Operator valueOperator : operator.getValueOperators()) {
                for (AttributeType attrType : AttributeType.values()) {
                    for (AttributeScope scope : new AttributeScope[] {
                            AttributeScope.RECORD, AttributeScope.SURVEY,
                            AttributeScope.RECORD_MODERATION, AttributeScope.SURVEY_MODERATION }) {

                        condition.setKeyOperator(keyOperator);
                        condition.setKey(String.format("%s_%s", attrType.toString(), scope.getName()));
                        condition.setValueOperator(valueOperator);
                        String falseValue = "";
                        switch (attrType) {
                        case STRING:
                        case STRING_AUTOCOMPLETE:
                        case TEXT:
                        case STRING_WITH_VALID_VALUES:
                        case IMAGE:
                        case FILE:
                            condition.setValue((String) attributeTypeValueLookup.get(attrType));
                            falseValue = condition.getValue() + "Wrong";
                            break;
                        case TIME:
                            condition.setValue((String) attributeTypeValueLookup.get(attrType));
                            falseValue = condition.getValue() + "Wrong";
                            break;
                        case INTEGER:
                        case INTEGER_WITH_RANGE:
                            condition.setValue(((BigDecimal) attributeTypeValueLookup.get(attrType)).intValue());
                            falseValue = condition.getValue() + "1";
                            break;
                        case MULTI_CHECKBOX:
                        	condition.setValue(new String[]{"a","b","c"});
                        	falseValue = condition.getValue();
                        	condition.setValue((String[])attributeTypeValueLookup.get(attrType));
                        	break;
                        case MULTI_SELECT:
                        	condition.setValue(new String[]{"a","b","c"});
                        	falseValue = condition.getValue();
                        	condition.setValue((String[])attributeTypeValueLookup.get(attrType));
                        	break;
                        case SINGLE_CHECKBOX:
                        	condition.setValue((String)attributeTypeValueLookup.get(attrType));
                        	falseValue = Boolean.FALSE.toString();
                        	break;
                        case DECIMAL:
                            condition.setValue(((BigDecimal) attributeTypeValueLookup.get(attrType)).doubleValue());
                            falseValue = condition.getValue() + "1";
                            break;
                        case DATE:
                            condition.setValue((Date) attributeTypeValueLookup.get(attrType));
                            falseValue = "09 Dec 2010";
                            break;
                        case REGEX:
                        case BARCODE:
                            condition.setValue((String)attributeTypeValueLookup.get(attrType));
                            falseValue = "012t6589#";
                            break;
                        case HTML:
                        case HTML_NO_VALIDATION:
                        case HTML_COMMENT:
                        case HTML_HORIZONTAL_RULE:
                            // html attributes have no attribute value, therefore the conditions can never match
                            // ignore this case
                            continue;
                        default:
                            Assert.assertTrue(false);
                            break;
                        }

                        Assert.assertFalse(condition.isSimplePropertyType());

                        boolean result = condition.applyCondition(getRequestContext().getHibernate(), rec, thresholdService);
                        Assert.assertTrue("Expected true but was "+result, result);

                        condition.setValue(falseValue);
                        result = condition.applyCondition(getRequestContext().getHibernate(), rec, thresholdService);
                        Assert.assertFalse(result);
                    }
                }
            }
        }
    }

    @Test
    public void testHoldRecordHandler() throws Exception {

        TaxonGroup taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);

        IndicatorSpecies speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);

        User admin = userDAO.getUser("admin");

        Record record = new Record();
        record.setNumber(1);
        record.setLastDate(new Date());
        record.setWhen(new Date());
        record.setHeld(false);
        record.setSpecies(speciesA);
        record.setUser(admin);
        record.setPoint(locationService.createPoint(-32.58, 154.2));
        record = recordDAO.saveRecord(record);
        int recordPk = record.getId();

        Action action = new Action();
        action.setActionType(ActionType.HOLD_RECORD);
        action.setValue(null);

        Threshold threshold = new Threshold();
        threshold.getActions().add(action);

        ActionHandler handler = new HoldRecordHandler();
        handler.executeAction(getRequestContext().getHibernate(), threshold, record, action);
        record = recordDAO.getRecord(recordPk);
        Assert.assertTrue(record.getHeld());

        record.setHeld(true);
        record = recordDAO.saveRecord(record);

        handler.executeAction(getRequestContext().getHibernate(), threshold, record, action);
        record = recordDAO.getRecord(recordPk);
        Assert.assertTrue(record.getHeld());
    }

    @Test
    public void testEmailActionHandler() throws Exception {

        TaxonGroup taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);

        IndicatorSpecies speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);

        User admin = userDAO.getUser("admin");

        Record record = new Record();
        record.setNumber(1);
        record.setLastDate(new Date());
        record.setWhen(new Date());
        record.setHeld(false);
        record.setSpecies(speciesA);
        record.setUser(admin);
        record.setPoint(locationService.createPoint(-32.58, 154.2));

        record = recordDAO.saveRecord(record);

        Action action = new Action();
        action.setActionType(ActionType.HOLD_RECORD);
        action.setValue("person@fakemail.com");

        Threshold threshold = new Threshold();
        threshold.getActions().add(action);

        MockEmailService emailService = new MockEmailService();
        ActionHandler handler = new EmailActionHandler(emailService,
                propertyService);
        handler.executeAction(getRequestContext().getHibernate(), threshold, record, action);
        
        List<MockEmail> messageQueue = emailService.getMockEmailList();
        Assert.assertEquals(1, messageQueue.size());
        
        MockEmail message = messageQueue.get(0);
        Assert.assertEquals(action.getValue(), message.getTo());
    }

    @Test
    public void testModerationEmailActionHandler() throws Exception {
        String email = "user@gaiabdrs.com";
        PasswordEncoder passwordEncoder = new Md5PasswordEncoder();
        String encodedPassword = passwordEncoder.encodePassword("password", null);
        String registrationKey = passwordEncoder.encodePassword(au.com.gaiaresources.bdrs.util.StringUtils.generateRandomString(10, 50), email);

        User user = userDAO.createUser("user", "Abigail", "Ambrose", email, encodedPassword, registrationKey, new String[] { Role.USER });
        login("user", "password", new String[]{Role.USER});
        
        TaxonGroup taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);

        IndicatorSpecies speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroup);
        speciesA = taxaDAO.save(speciesA);

        User admin = userDAO.getUser("admin");
        
        Survey survey = new Survey();
        survey.setName("Test Moderation Survey");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md);
        
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute a = new Attribute();
        a.setName("survey_moderation");
        a.setScope(AttributeScope.SURVEY_MODERATION);
        a.setDescription("desc");
        a.setRequired(false);
        a.setTag(false);
        a.setTypeCode(AttributeType.TEXT.toString());
        
        survey.setAttributes(attributeList);
        survey = surveyDAO.save(survey);
        
        Record record = new Record();
        record.setNumber(1);
        record.setLastDate(new Date());
        record.setWhen(new Date());
        record.setHeld(false);
        record.setSpecies(speciesA);
        record.setUser(user);
        record.setPoint(locationService.createPoint(-32.58, 154.2));
        record.setSurvey(survey);
        record = recordDAO.saveRecord(record);

        Action action = new Action();
        action.setActionType(ActionType.MODERATION_EMAIL_NOTIFICATION);
        
        Threshold threshold = new Threshold();
        threshold.getActions().add(action);

        // test user creation of a new record causes an email to be sent to the admin
        MockEmailService emailService = new MockEmailService();
        ActionHandler handler = new ModerationEmailActionHandler(emailService,
                propertyService, contentService, redirService, userDAO);
        handler.executeAction(getRequestContext().getHibernate(), threshold, record, action);
        
        List<MockEmail> messageQueue = emailService.getMockEmailList();
        Assert.assertTrue("At least one message should have been sent", messageQueue.size() > 0);
        
        for (MockEmail message : messageQueue) {
            Assert.assertEquals(user.getEmailAddress(), message.getFrom());
        }
        
        messageQueue.clear();
        
        // save the record as the admin user and test that the user gets an email that their record was modified
        login("admin", "password", new String[]{Role.ADMIN});
        record = recordDAO.saveRecord(record);
        
        handler.executeAction(getRequestContext().getHibernate(), threshold, record, action);
        // test that it sends a message to the record owner that their record has been moderated
        messageQueue = emailService.getMockEmailList();
        Assert.assertTrue("At least one message should have been sent", messageQueue.size() > 0);
        
        for (MockEmail message : messageQueue) {
            Assert.assertEquals(admin.getEmailAddress(), message.getFrom());
            Assert.assertEquals(user.getEmailAddress(), message.getTo());
        }
    }
}
