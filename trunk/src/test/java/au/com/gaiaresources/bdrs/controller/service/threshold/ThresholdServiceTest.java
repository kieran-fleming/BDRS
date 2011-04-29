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
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.threshold.ActionHandler;
import au.com.gaiaresources.bdrs.service.threshold.ComplexTypeOperator;
import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.EmailActionHandler;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.HoldRecordHandler;
import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
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

public class ThresholdServiceTest extends AbstractControllerTest {

    @Autowired
    private RecordDAO recordDAO;

    @Autowired
    private TaxaDAO taxaDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private ThresholdService thresholdService;

    @Autowired
    private LocationService locationService;

    @Autowired
    private PropertyService propertyService;

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
    public void testRecordAttributeCondition() throws Exception {
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
                attributeTypeValueLookup.put(at, at.getCode());
                break;
            case INTEGER:
                attributeTypeValueLookup.put(at, new BigDecimal(101));
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

        List<RecordAttribute> recAttrList = new ArrayList<RecordAttribute>();
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for (AttributeType attrType : AttributeType.values()) {
            for (AttributeScope scope : new AttributeScope[] {
                    AttributeScope.RECORD, AttributeScope.SURVEY }) {

                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(String.format("%s_%s", attrType.toString(), scope.getName()));
                attr.setTypeCode(attrType.getCode());
                attr.setScope(scope);
                attr.setTag(false);

                if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for (int i = 0; i < 4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        //opt = taxaDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }

                //attr = taxaDAO.save(attr);
                attributeList.add(attr);

                RecordAttribute recAttr = new RecordAttribute();
                recAttr.setAttribute(attr);
                switch (attrType) {
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                case STRING_WITH_VALID_VALUES:
                case IMAGE:
                case FILE:
                    recAttr.setStringValue((String) attributeTypeValueLookup.get(attrType));
                    break;
                case INTEGER:
                    recAttr.setNumericValue((BigDecimal) attributeTypeValueLookup.get(attrType));
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
        survey.setDate(new Date());
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

        Assert.assertEquals(RecordAttribute.class, condition.getTargetIterableTypeForPath());
        ComplexTypeOperator operator = ThresholdService.COMPLEX_TYPE_TO_OPERATOR_MAP.get(condition.getTargetIterableTypeForPath());
        for (Operator keyOperator : operator.getKeyOperators()) {
            for (Operator valueOperator : operator.getValueOperators()) {
                for (AttributeType attrType : AttributeType.values()) {
                    for (AttributeScope scope : new AttributeScope[] {
                            AttributeScope.RECORD, AttributeScope.SURVEY }) {

                        condition.setKeyOperator(keyOperator);
                        condition.setKey(String.format("%s_%s", attrType.toString(), scope.getName()));
                        condition.setValueOperator(valueOperator);
                        String falseValue = new String();
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
                        case INTEGER:
                            condition.setValue(((BigDecimal) attributeTypeValueLookup.get(attrType)).intValue());
                            falseValue = condition.getValue() + "1";
                            break;
                        case DECIMAL:
                            condition.setValue(((BigDecimal) attributeTypeValueLookup.get(attrType)).doubleValue());
                            falseValue = condition.getValue() + "1";
                            break;
                        case DATE:
                            condition.setValue((Date) attributeTypeValueLookup.get(attrType));
                            falseValue = "09 Dec 2010";
                            break;
                        default:
                            Assert.assertTrue(false);
                            break;
                        }

                        Assert.assertFalse(condition.isSimplePropertyType());

                        boolean result = condition.applyCondition(getRequestContext().getHibernate(), rec, thresholdService);
                        Assert.assertTrue(result);

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
        
        List<MockEmailMessage> messageQueue = emailService.getMessageQueue();
        Assert.assertEquals(1, messageQueue.size());
        
        MockEmailMessage message = messageQueue.get(0);
        Assert.assertEquals(action.getValue(), message.to);
    }

    private class MockEmailService implements EmailService {

        private List<MockEmailMessage> messageQueue = new ArrayList<MockEmailMessage>();

        @Override
        public String getErrorToAddress() {
            return "error@fakemail.com";
        }

        @Override
        public void sendMessage(String to, String subject, String templateName,
                Map<String, Object> subsitutionParams) {
            messageQueue.add(new MockEmailMessage(to, subject, templateName,
                    subsitutionParams));
        }

        @Override
        public void sendMessage(String to, String from, String subject,
                String message) {
            messageQueue.add(new MockEmailMessage(to, from, subject, message));
        }

        public List<MockEmailMessage> getMessageQueue() {
            return messageQueue;
        }
    }
    
    @SuppressWarnings("unused")
    private class MockEmailMessage {
        String to;
        String from;
        String subject;
        String templateName;
        Map<String, Object> substitutionParams;
        String message;

        MockEmailMessage(String to, String subject, String templateName,
                Map<String, Object> substitutionParams) {
            this.to = to;
            this.subject = subject;
            this.templateName = templateName;
            this.substitutionParams = substitutionParams;
        }

        MockEmailMessage(String to, String from, String subject, String message) {

            this.to = to;
            this.from = from;
            this.subject = subject;
            this.message = message;
        }
    }
}
