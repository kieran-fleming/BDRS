package au.com.gaiaresources.bdrs.controller;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.geometry.GeometryBuilder;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordDAO;
import au.com.gaiaresources.bdrs.model.record.RecordVisibility;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.model.user.UserDAO;
import au.com.gaiaresources.bdrs.service.lsid.LSIDService;


public abstract class AbstractGridControllerTest extends AbstractControllerTest {
    
    @Autowired
    protected SurveyDAO surveyDAO;
    @Autowired
    protected TaxaDAO taxaDAO;
    @Autowired
    protected RecordDAO recordDAO;
    @Autowired
    protected UserDAO userDAO;
    @Autowired
    protected CensusMethodDAO cmDAO;
    @Autowired
    protected MetadataDAO metaDAO;
    @Autowired
    protected LocationService locService;

    protected User currentUser;
    
    protected Survey survey1;
    protected Survey survey2;
    
    protected CensusMethod taxaCm;
    protected CensusMethod nonTaxaCm;
    protected CensusMethod optTaxaCm;
    
    protected Record r1;
    protected Record r2;
    protected Record r3;
    protected Record r4;
    protected Record r5;
    protected Record r6;
    protected Record r7;
    protected Record r8;
    protected Record r9;
    
    protected List<Record> allRecordList;
    
    protected IndicatorSpecies dropBear;
    protected IndicatorSpecies nyanCat;
    protected IndicatorSpecies hoopSnake;
    protected IndicatorSpecies surfingBird;
        
    protected Date now;
    
    protected Calendar cal = Calendar.getInstance();
    
    protected GeometryBuilder geomBuilder = new GeometryBuilder();
    
    protected Logger log = Logger.getLogger(getClass());
    
    protected SimpleDateFormat bdrsDateFormat = new SimpleDateFormat("dd MMM yyyy");
    
    @Autowired
    protected LSIDService lsidService;
    
    @Before
    public void setup() {
        
        taxaCm = createCm("taxa cm", "taxa cm desc", Taxonomic.TAXONOMIC, true);
        nonTaxaCm = createCm("non taxa cm", "non taxa cm desc", Taxonomic.NONTAXONOMIC, true);
        optTaxaCm = createCm("opt taxa cm", "opt taxa cm desc", Taxonomic.OPTIONALLYTAXONOMIC, true);
        
        now = getDate(2010, 9, 20);
        
        currentUser = userDAO.getUser("admin");
        
        TaxonGroup g1 = new TaxonGroup();
        g1.setName("fictionus animus");
        taxaDAO.save(g1);
        
        dropBear = createIndicatorSpecies(g1, "dropus bearus", "drop bear", "jimmy ricard", "guid1231239a8d");
        nyanCat = createIndicatorSpecies(g1, "nyanatic catup", "nyan cat", null, "lsid:sdklsdff:s39er:sdksdf:");
        hoopSnake = createIndicatorSpecies(g1, "circulom reptile", "hoop snake", null, null);
        surfingBird = createIndicatorSpecies(g1, "orthonological waverider", "surfing bird", "a guy who names stuff", null);
        
        List<CensusMethod> survey1_cmList = new ArrayList<CensusMethod>();
        survey1_cmList.add(taxaCm);
        survey1_cmList.add(nonTaxaCm);
        survey1_cmList.add(optTaxaCm);
        survey1 = createSurvey("Fictionay Animal Survey", "A survey of fictionary animals for testing", 
                               now, null, true, survey1_cmList);
        
        survey2 = createSurvey("Generic Survey, no CM", "A survey with no census methods for testing", 
                               getDate(1980, 1, 1), getDate(2000, 1, 1), false, Collections.EMPTY_LIST);
        
        r1 = createRecord(survey1, null, dropBear, currentUser, 1, getDate(2010, 9, 21), 1, 1, RecordVisibility.PUBLIC, true);
        r2 = createRecord(survey1, taxaCm, nyanCat, currentUser, 2, getDate(2010, 9, 22), 2.123456789d, 22.123456789d, RecordVisibility.PUBLIC, true);
        r3 = createRecord(survey1, nonTaxaCm, null, currentUser, null, getDate(2010, 9, 23), 3, 3, RecordVisibility.PUBLIC, true);
        r4 = createRecord(survey1, optTaxaCm, surfingBird, currentUser, 4, getDate(2010, 9, 24), 4, 4, RecordVisibility.PUBLIC, true);
        
        // optional items *without* taxonomic information
        r5 = createRecord(survey1, null, null, currentUser, null, getDate(2010, 9, 25), 5, 5, RecordVisibility.PUBLIC, true);
        r6 = createRecord(survey1, optTaxaCm, null, currentUser, null, getDate(2010, 9, 26), 6, 6, RecordVisibility.PUBLIC, true);
        
        r7 = createRecord(survey1, null, hoopSnake, currentUser, 1, getDate(2010, 9, 27), 1, 1, RecordVisibility.CONTROLLED, true);
        r8 = createRecord(survey1, null, dropBear, currentUser, 1, getDate(2010, 9, 28), 1, 1, RecordVisibility.OWNER_ONLY, true);
        
        r9 = createRecord(survey2, null, hoopSnake, currentUser, 10, getDate(2010, 9, 29), -1, -1, RecordVisibility.PUBLIC, false);
        
        allRecordList = new ArrayList<Record>();
        allRecordList.add(r1);
        allRecordList.add(r2);
        allRecordList.add(r3);
        allRecordList.add(r4);
        allRecordList.add(r5);
        allRecordList.add(r6);
        allRecordList.add(r7);
        allRecordList.add(r8);
        allRecordList.add(r9);
    }
    
    private IndicatorSpecies createIndicatorSpecies(TaxonGroup group, String sciName, String commonName, String author, String guid) {
        IndicatorSpecies species = new IndicatorSpecies();
        species.setTaxonGroup(group);
        species.setScientificName(sciName);
        species.setCommonName(commonName);
        
        if (author != null) {
            species.setScientificNameAndAuthor(sciName + " - " + author);
        }
        if (guid != null) {
            species.setGuid(metaDAO, guid);
        }
        
        species.setRunThreshold(false);
        
        return taxaDAO.save(species);
    }
    
    private Date getDate(int year, int month, int day) {
        Calendar cal = Calendar.getInstance();
        cal.clear();
        cal.set(year, month, day);
        return cal.getTime();
    }
    
    private String getDateAsString(int year, int month, int day) {
        Date d = getDate(year, month, day);
        return bdrsDateFormat.format(d);
    }
    
    private Record createRecord(Survey survey, CensusMethod cm, IndicatorSpecies sp, User u, Integer count, Date now, double lon, double lat, RecordVisibility vis, boolean generateAv) {
        Record r = new Record();
        r.setSurvey(survey);
        r.setCensusMethod(cm);
        r.setSpecies(sp);
        r.setUser(u);
        r.setWhen(now);
        r.setLastDate(now);
        r.setNumber(count);
        r.setRecordVisibility(vis);
        r.setGeometry(geomBuilder.createPoint(lon, lat));
        
        r.setRunThreshold(false);
        
        Set<AttributeValue> avSet = new HashSet<AttributeValue>();
        
        int seed = 0;
        for (Attribute a : survey.getAttributes()) {
            avSet.add(generateAttributeValue(a, seed++, generateAv));
        }
        
        if (cm != null) {
            for (Attribute a : cm.getAttributes()) {
                avSet.add(generateAttributeValue(a, seed++, generateAv));
            }
        }

        r.setAttributes(avSet);
        
        return recordDAO.saveRecord(r);
    }
    
    private Survey createSurvey(String name, String desc, Date startDate, Date endDate, boolean attrRequired, List<CensusMethod> cmList) {
        Survey surv = new Survey();
        surv.setName(name);
        surv.setDescription(desc);
        surv.setStartDate(now);
        
        List<Attribute> attrList = createAttrList("sattr", attrRequired);
        surv.setAttributes(attrList);

        surv.setCensusMethods(cmList);
        
        surv.setRunThreshold(false);
        
        return surveyDAO.save(surv);
    }
   
    
    private List<Attribute> createAttrList(String namePrefix, boolean attrRequired) {
        List<Attribute> attrList = new LinkedList<Attribute>();
        attrList.add(createAttribute(namePrefix + "_0", AttributeType.INTEGER, attrRequired));
        attrList.add(createAttribute(namePrefix + "_1", AttributeType.INTEGER_WITH_RANGE, attrRequired, new String[] { "5", "10" } ));
        attrList.add(createAttribute(namePrefix + "_2", AttributeType.DECIMAL, attrRequired));
        attrList.add(createAttribute(namePrefix + "_3", AttributeType.BARCODE, attrRequired));
        attrList.add(createAttribute(namePrefix + "_4", AttributeType.DATE, attrRequired));
        attrList.add(createAttribute(namePrefix + "_5", AttributeType.TIME, attrRequired));
        attrList.add(createAttribute(namePrefix + "_6", AttributeType.STRING, attrRequired));
        attrList.add(createAttribute(namePrefix + "_7", AttributeType.STRING_AUTOCOMPLETE, attrRequired));
        attrList.add(createAttribute(namePrefix + "_8", AttributeType.TEXT, attrRequired));
        attrList.add(createAttribute(namePrefix + "_9", AttributeType.STRING_WITH_VALID_VALUES, attrRequired, new String[] { "hello", "world", "goodbye"} ));
        attrList.add(createAttribute(namePrefix + "_10", AttributeType.FILE, attrRequired));
        attrList.add(createAttribute(namePrefix + "_11", AttributeType.IMAGE, attrRequired));
        attrList.add(createAttribute(namePrefix + "_12", AttributeType.HTML, attrRequired));
        attrList.add(createAttribute(namePrefix + "_13", AttributeType.HTML_COMMENT, attrRequired));
        attrList.add(createAttribute(namePrefix + "_14", AttributeType.HTML_HORIZONTAL_RULE, attrRequired));
        return attrList;
    }
    
    private CensusMethod createCm(String name, String desc, Taxonomic tax, boolean attrRequired) {
        CensusMethod cm = new CensusMethod();
        cm.setName(name);
        cm.setDescription(desc);
        cm.setTaxonomic(tax);
        
        List<Attribute> attrList = createAttrList("cattr", attrRequired);
        cm.setAttributes(attrList);
        
        cm.setRunThreshold(false);
        
        return cmDAO.save(cm);
    }
    
    protected Attribute createAttribute(String name, AttributeType type, boolean required) {
        return createAttribute(name, type, required, null);
    }
    
    protected Attribute createAttribute(String name, AttributeType type, boolean required, String[] args) {
        Attribute a = new Attribute();
        
        if (args != null) {
            List<AttributeOption> options = new ArrayList<AttributeOption>();
            for (String s : args) {
                AttributeOption attrOpt = new AttributeOption();
                attrOpt.setRunThreshold(false);
                attrOpt.setValue(s);
                options.add(attrOpt);
                
                taxaDAO.save(attrOpt);
            }
            a.setOptions(options);
        }
        
        a.setName(name);
        a.setDescription(name + " desc");
        a.setRequired(required);
        a.setTag(false);
        a.setTypeCode(type.getCode());
        
        a.setRunThreshold(false);
        
        taxaDAO.save(a);
        return a;
    }
    
    /**
     * A method provided to child classes to add test specific attribute values
     * 
     * @param a
     * @param value
     * @return
     * @throws ParseException
     */
    protected AttributeValue createAttributeValue(Attribute a, String value) throws ParseException {
        AttributeValue av = new AttributeValue();
        av.setAttribute(a);
        switch (a.getType()) {
        case INTEGER:
        case INTEGER_WITH_RANGE:
        case DECIMAL:
            av.setNumericValue(new BigDecimal(Double.valueOf(value)));
            break;
        
        case DATE:
            av.setDateValue(bdrsDateFormat.parse(value));
            break;
            
        case BARCODE:
        case TIME:
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case HTML:
        case HTML_COMMENT:
        case HTML_HORIZONTAL_RULE:
        case STRING_WITH_VALID_VALUES:
        case MULTI_CHECKBOX:
        case MULTI_SELECT:
            av.setStringValue(value);
            break;
            
        case SINGLE_CHECKBOX:
            av.setBooleanValue(value);
            break;
            
        case IMAGE:
        case FILE:
            // the string value becomes the file name
            av.setStringValue(value);
            break;
            
        default:
            // not handled. fail the test to notify the test writer
            Assert.fail("Attribute type : " + a.getTypeCode() + " is not handled. Fix it!");
        }
        
        av.setRunThreshold(false);
        
        recordDAO.saveAttributeValue(av);
        
        return av;
    }
    
    /**
     * Just a way to make test data without writing lots of code 
     * 
     * @param a
     * @param seed
     * @return
     */
    private AttributeValue generateAttributeValue(Attribute a, int seed, boolean generateAv) {
        AttributeValue av = new AttributeValue();
        av.setAttribute(a);
        if (generateAv) {
            switch (a.getType()) {
            case INTEGER:
            case DECIMAL:
                av.setNumericValue(new BigDecimal(Double.valueOf(seed)));
                break;
                
            case INTEGER_WITH_RANGE:
            {
                Integer lower = Integer.parseInt(a.getOptions().get(0).getValue());
                Integer upper = Integer.parseInt(a.getOptions().get(1).getValue());
                Integer value = (seed%(upper-lower)) + lower;
                av.setNumericValue(new BigDecimal(value));
            }
                break;
            
            case DATE:
                av.setDateValue(getDate(2010, 10, seed%30));
                break;
                
            case BARCODE:
            case TIME:
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
                av.setStringValue(String.format("seed is : %d", seed));
                break;
                
            case HTML:
            case HTML_COMMENT:
            case HTML_HORIZONTAL_RULE:
                av.setStringValue(String.format("<p>seed is : %d</p>", seed));
                break;
                
            case STRING_WITH_VALID_VALUES:
            case MULTI_CHECKBOX:
            case MULTI_SELECT:
            {
                int listIdx = seed % a.getOptions().size();
                av.setStringValue(a.getOptions().get(listIdx).getValue());
            }
                break;
                
            case SINGLE_CHECKBOX:
                
                av.setBooleanValue(Boolean.toString((seed % 2) == 0));
                break;
                
            case IMAGE:
            case FILE:
                // the string value becomes the file name
                // putting a space in here deliberately
                av.setStringValue("filename " + Integer.toString(seed) + ".bleh");
                break;
                
            default:
                // not handled. fail the test to notify the test writer
                Assert.fail("Attribute type : " + a.getTypeCode() + " is not handled. Fix it!");
            }
        }
        
        av.setRunThreshold(false);
        
        recordDAO.saveAttributeValue(av);
        
        return av;
    }
    
    /**
     * pass in a string representation of the attribute id
     * 
     * @param avSet
     * @param strId
     * @return
     */
    protected AttributeValue getAttributeValueByAttributeId(Set<AttributeValue> avSet, String strId) {
        Integer attrId = Integer.parseInt(strId);
        for (AttributeValue av : avSet) {
            if (av.getAttribute().getId().equals(attrId)) {
                return av;
            }
        }
        return null;
    }
}
