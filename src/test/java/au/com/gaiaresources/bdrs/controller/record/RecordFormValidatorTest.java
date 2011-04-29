package au.com.gaiaresources.bdrs.controller.record;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
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
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { dateFormat.format(earliest) });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { dateFormat.format(latest) });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { dateFormat.format(invalidEarly) });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { dateFormat.format(invalidLate) });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
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
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { min.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { max.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { invalidMin.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { invalidMax.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testDoubleValidator() throws Exception {

        String key = "test";
        Validator validator = new DoubleRangeValidator(propertyService, true,
                false);

        paramMap.put(key, new String[] { "1.0" });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
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
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { dateFormat.format(past) });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { dateFormat.format(future) });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
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
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { min.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { max.toString() });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { invalidMin.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { invalidMax.toString() });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testIntValidator() throws Exception {

        String key = "test";
        Validator validator = new IntRangeValidator(propertyService, true,
                false);

        paramMap.put(key, new String[] { "1" });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        // Parse Error
        paramMap.put(key, new String[] { "Spam" });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

    @Test
    public void testStringValidator() throws Exception {

        String key = "test";
        Validator reqValidator = new StringValidator(propertyService, true,
                false);

        // Must be required
        Assert.assertFalse(reqValidator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { "Spam" });
        Assert.assertTrue(reqValidator.validate(paramMap, key, errorMap));

        Validator blankValidator = new StringValidator(propertyService, false,
                true);

        // Is not required
        paramMap.clear();
        Assert.assertTrue(blankValidator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { "Spam" });
        Assert.assertTrue(blankValidator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { "" });
        Assert.assertTrue(blankValidator.validate(paramMap, key, errorMap));
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
        String invalidLower = valid.toLowerCase();
        String invalidUpper = valid.toUpperCase();
        String invalid = valid.substring(1, valid.length());

        paramMap.put(key, new String[] { valid });
        Assert.assertTrue(validator.validate(paramMap, key, errorMap));

        paramMap.put(key, new String[] { invalidLower });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { invalidUpper });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();

        paramMap.put(key, new String[] { invalid });
        Assert.assertFalse(validator.validate(paramMap, key, errorMap));
        Assert.assertTrue(errorMap.containsKey(key));
        errorMap.clear();
    }

}