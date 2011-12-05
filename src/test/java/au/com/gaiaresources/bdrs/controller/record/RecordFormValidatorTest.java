package au.com.gaiaresources.bdrs.controller.record;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.record.validator.DateValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.DoubleRangeValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.HistoricalDateValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.IntRangeValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.StringValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.TaxonValidator;
import au.com.gaiaresources.bdrs.controller.record.validator.Validator;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

public class RecordFormValidatorTest extends AbstractControllerTest {

    @Autowired
    private PropertyService propertyService;

    @Autowired
    private TaxaDAO taxaDAO;

    private Map<String, String[]> paramMap;
    private Map<String, String> errorMap;

    @Before
    public void setUp() throws Exception {
        paramMap = new HashMap<String, String[]>();
        errorMap = new HashMap<String, String>();
    }

    @Test
    public void testDateValidator() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Calendar cal = new GregorianCalendar();

        Date valid = dateFormat.parse("14 Dec 2010");

        cal.setTime(valid);
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date earliest = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, -1);
        Date invalidEarly = cal.getTime();

        cal.setTime(valid);
        cal.add(Calendar.DAY_OF_WEEK, 7);
        Date latest = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date invalidLate = cal.getTime();

        String key = "test";
        Validator validator = new DateValidator(propertyService, true, false,
                earliest, latest);

        // Bounadary Test
        paramMap.put(key, new String[] { dateFormat.format(valid) });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { dateFormat.format(earliest) });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { dateFormat.format(latest) });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { dateFormat.format(invalidEarly) });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { dateFormat.format(invalidLate) });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testDoubleRangeValidator() throws Exception {

        Double valid = new Double(0.0);
        Double min = new Double(-100.0);
        Double invalidMin = new Double(-100.1);
        Double max = new Double(100.0);
        Double invalidMax = new Double(100.1);

        String key = "test";
        Validator validator = new DoubleRangeValidator(propertyService, true,
                false, min, max);

        // Boundary Test
        paramMap.put(key, new String[] { valid.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, null, errorMap));

        paramMap.put(key, new String[] { min.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, null, errorMap));

        paramMap.put(key, new String[] { max.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, null, errorMap));

        paramMap.put(key, new String[] { invalidMin.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, null, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { invalidMax.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, null, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testDoubleValidator() throws Exception {

        String key = "test";
        Validator validator = new DoubleRangeValidator(propertyService, true,
                false);

        paramMap.put(key, new String[] { "1.0" });
        Assert.assertTrue(validator.validate(paramMap, key, null, errorMap));

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, null, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testHistoricalDateValidator() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Calendar cal = new GregorianCalendar();

        cal.setTimeInMillis(System.currentTimeMillis());
        Date valid = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, -1);
        Date past = cal.getTime();

        cal.setTime(valid);
        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date future = cal.getTime();

        String key = "test";
        Validator validator = new HistoricalDateValidator(propertyService,
                true, false);

        // Bounadary Test
        paramMap.put(key, new String[] { dateFormat.format(valid) });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { dateFormat.format(past) });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { dateFormat.format(future) });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testIntRangeValidator() throws Exception {

        Integer valid = new Integer(0);
        Integer min = new Integer(-100);
        Integer invalidMin = new Integer(-101);
        Integer max = new Integer(100);
        Integer invalidMax = new Integer(101);

        String key = "test";
        Validator validator = new IntRangeValidator(propertyService, true,
                false, min, max);

        // Boundary Test
        paramMap.put(key, new String[] { valid.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { min.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { max.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { invalidMin.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { invalidMax.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testIntValidator() throws Exception {

        String key = "test";
        Validator validator = new IntRangeValidator(propertyService, true,
                false);

        paramMap.put(key, new String[] { "1" });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testStringValidator() throws Exception {

        String key = "test";
        Validator reqValidator = new StringValidator(propertyService, true,
                false);

        // Must be required
        Assert.assertFalse(reqValidator.validate(paramMap, key, null,  errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { "Spam" });
        Assert.assertTrue(reqValidator.validate(paramMap, key, null,  errorMap));

        Validator blankValidator = new StringValidator(propertyService, false,
                true);

        // Is not required
        paramMap.clear();
        Assert.assertTrue(blankValidator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { "Spam" });
        Assert.assertTrue(blankValidator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { "" });
        Assert.assertTrue(blankValidator.validate(paramMap, key, null,  errorMap));
    }

    @Test
    public void testTaxonValidator() throws Exception {
        TaxonGroup taxonGroup = new TaxonGroup();
        taxonGroup.setName("Birds");
        taxonGroup = taxaDAO.save(taxonGroup);

        IndicatorSpecies species = new IndicatorSpecies();
        species.setCommonName("Indicator Species A");
        species.setScientificName("Indicator Species A");
        species.setTaxonGroup(taxonGroup);
        species = taxaDAO.save(species);

        String key = "test";
        Validator validator = new TaxonValidator(propertyService, true, false,
                taxaDAO);

        String valid = species.getScientificName();
        String allLower = valid.toLowerCase();
        String allUpper = valid.toUpperCase();
        String shortenedName = valid.substring(1, valid.length());

        paramMap.put(key, new String[] { valid });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));

        paramMap.put(key, new String[] { allLower });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));
        errorMap.clear();

        paramMap.put(key, new String[] { allUpper });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));
        errorMap.clear();

        paramMap.put(key, new String[] { shortenedName });
        Assert.assertTrue(validator.validate(paramMap, key, null,  errorMap));
        errorMap.clear();
    }
    
    @Test
    public void testValidateDateRange() throws Exception {

        SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        Calendar cal = new GregorianCalendar();

        Date valid = dateFormat.parse("14 Dec 2010");

        cal.setTime(valid);
        cal.add(Calendar.DAY_OF_WEEK, -7);
        Date earliest = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, -1);
        Date invalidEarly = cal.getTime();

        cal.setTime(valid);
        cal.add(Calendar.DAY_OF_WEEK, 7);
        Date latest = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 1);
        Date invalidLate = cal.getTime();

        String key = "date";
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);

        // Boundary Test
        paramMap.put(key, new String[] { dateFormat.format(valid) });
        paramMap.put("dateRange", new String[] { dateFormat.format(earliest) , dateFormat.format(latest) });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));

        paramMap.put(key, new String[] { dateFormat.format(earliest) });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));

        paramMap.put(key, new String[] { dateFormat.format(latest) });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));

        paramMap.put(key, new String[] { dateFormat.format(invalidEarly) });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();

        paramMap.put(key, new String[] { dateFormat.format(invalidLate) });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();

        // Test with no lower bound
        paramMap.put(key, new String[] { dateFormat.format(valid) });
        paramMap.put("dateRange", new String[] { dateFormat.format(earliest) , null });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));

        paramMap.put(key, new String[] { dateFormat.format(earliest) });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));

        paramMap.put(key, new String[] { dateFormat.format(latest) });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));

        paramMap.put(key, new String[] { dateFormat.format(invalidEarly) });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();

        paramMap.put(key, new String[] { dateFormat.format(invalidLate) });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));
        
        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.DATE_WITHIN_RANGE, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
    }
    
    @Test
    public void testValidateRequiredTime() throws Exception {
        String key = "time";
        String value = "";
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);

        // Boundary Test
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REQUIRED_TIME, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();

        value = "12:00";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REQUIRED_TIME, key, null));

        value = "anyString";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REQUIRED_TIME, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
    }
    
    @Test
    public void testValidateTime() throws Exception {
        String key = "time";
        String value = "";
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);

        // Boundary Test
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.TIME, key, null));

        value = "12:00";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.TIME, key, null));
        
        value = "32:00";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.TIME, key, null));

        value = "anyString";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.TIME, key, null));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
    }
    
    @Test
    public void testValidateHtml() throws Exception {
        String key = "test";
        String value = "";
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);

        // Boundary Test
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.HTML, key, null));

        value = "<html><body></body></html>";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.HTML, key, null));

        value = "<br>";
        paramMap.put(key, new String[] { value });
        boolean validated = validator.validate(paramMap, ValidationType.HTML, key, null);
        Assert.assertTrue(validated);
        
        value = "</html";
        paramMap.put(key, new String[] { value });
        validated = validator.validate(paramMap, ValidationType.HTML, key, null);
        Assert.assertFalse(validated);
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
    }
    
    @Test
    public void testValidateRegex() {
        String key = "test";
        String value = "";
        String regex = "\\d+(\\.?\\d+)?"; // regex for numbers
        RecordFormValidator validator = new RecordFormValidator(propertyService, taxaDAO);

        Attribute att = new Attribute();
        att.setName(key+"_attribute");
        List<AttributeOption> options = new ArrayList<AttributeOption>(1);
        AttributeOption opt = new AttributeOption();
        opt.setValue(regex);
        options.add(opt);
        att.setOptions(options);
        
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REGEX, key, att));
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REQUIRED_REGEX, key, att));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
        
        value = ".45";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REGEX, key, att));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
        
        value = "0.45";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REGEX, key, att));

        value = "1,200";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REGEX, key, att));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
        
        value = "1";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REGEX, key, att));

        // test word characters
        regex = "[A-Z](\\w*\\s*)*\\."; // matches a sentence
        att.getOptions().remove(opt);
        opt.setValue(regex);
        att.getOptions().add(opt);
        att.setOptions(options);
        
        value = "I";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REGEX, key, att));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
        
        value = "I.";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REGEX, key, att));
        
        value = "I think this should be valid.";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REGEX, key, att));
        
        value = "this will not be valid.";
        paramMap.put(key, new String[] { value });
        Assert.assertFalse(validator.validate(paramMap, ValidationType.REGEX, key, att));
        Assert.assertTrue(validator.getErrorMap().containsKey(key));
        validator.getErrorMap().clear();
        
        value = "I can even use numb3r5 and _und3r5c0r35 and CAPITALS.";
        paramMap.put(key, new String[] { value });
        Assert.assertTrue(validator.validate(paramMap, ValidationType.REGEX, key, att));
    }
}