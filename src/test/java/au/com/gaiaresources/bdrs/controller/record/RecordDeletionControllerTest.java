package au.com.gaiaresources.bdrs.controller.record;

import java.math.BigDecimal;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import junit.framework.Assert;

import org.hibernate.FlushMode;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.encoding.Md5PasswordEncoder;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.location.Location;
import au.com.gaiaresources.bdrs.model.location.LocationDAO;
import au.com.gaiaresources.bdrs.model.location.LocationService;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
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
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.security.Role;

/**
 * Tests all aspects of the <code>TrackerController</code>.
 */
public class RecordDeletionControllerTest extends AbstractControllerTest {
	private DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

	@Autowired
	private SurveyDAO surveyDAO;
	@Autowired
	private TaxaDAO taxaDAO;
	@Autowired
	private MetadataDAO metadataDAO;
	@Autowired
	private LocationDAO locationDAO;
	@Autowired
	private CensusMethodDAO methodDAO;
	@Autowired
	private LocationService locationService;
	@Autowired
	private RecordDAO recordDAO;

	private TaxonGroup taxonGroupBirds;
	private TaxonGroup taxonGroupFrogs;
	private IndicatorSpecies speciesA;
	private IndicatorSpecies speciesB;
	private Location locationA;
	private Location locationB;
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

					if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
						List<AttributeOption> optionList = new ArrayList<AttributeOption>();
						for (int i = 0; i < 4; i++) {
							AttributeOption opt = new AttributeOption();
							opt.setValue(String.format("Option %d", i));
							opt = taxaDAO.save(opt);
							optionList.add(opt);
						}
						groupAttr.setOptions(optionList);
					} else if (AttributeType.INTEGER_WITH_RANGE
							.equals(attrType)) {
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
		String encodedPassword = passwordEncoder.encodePassword("password",
				null);
		String registrationKey = passwordEncoder.encodePassword(
				au.com.gaiaresources.bdrs.util.StringUtils
						.generateRandomString(10, 50), emailAddr);

		user = userDAO.createUser("testuser", "Abigail", "Ambrose", emailAddr,
				encodedPassword, registrationKey, new String[] { Role.USER });

		admin = userDAO.getUser("admin");

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

		int surveyIndex = 1;
		for (CensusMethod method : new CensusMethod[] { methodA, methodB,
				methodC, null }) {
			List<Attribute> attributeList = new ArrayList<Attribute>();
			Attribute attr;
			for (AttributeType attrType : AttributeType.values()) {
				for (AttributeScope scope : new AttributeScope[] {
						AttributeScope.RECORD, AttributeScope.SURVEY, null }) {

					attr = new Attribute();
					attr.setDescription(attrType.toString() + " description");
					attr.setRequired(true);
					attr.setName(attrType.toString());
					attr.setTypeCode(attrType.getCode());
					attr.setScope(scope);
					attr.setTag(false);

					if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
						List<AttributeOption> optionList = new ArrayList<AttributeOption>();
						for (int i = 0; i < 4; i++) {
							AttributeOption opt = new AttributeOption();
							opt.setValue(String.format("Option %d", i));
							opt = taxaDAO.save(opt);
							optionList.add(opt);
						}
						attr.setOptions(optionList);
					} else if (AttributeType.INTEGER_WITH_RANGE
							.equals(attrType)) {
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

			Metadata md = survey
					.setFormRendererType(SurveyFormRendererType.DEFAULT);
			metadataDAO.save(md);

			survey.setAttributes(attributeList);
			survey.setSpecies(new HashSet<IndicatorSpecies>(speciesSet));
			survey.getCensusMethods().add(method);

			survey = surveyDAO.save(survey);

			surveyRecordCount = 0;
			taxonRecordCount = 0;
			methodRecordCount = 0;

			for (IndicatorSpecies species : survey.getSpecies()) {
				for (CensusMethod cm : survey.getCensusMethods()) {
					for (User u : new User[] { admin, user }) {
						Record rec = createRecord(survey, cm, species, u);

						recordCount++;
						surveyRecordCount++;
						methodRecordCount++;
						taxonRecordCount++;
					}
				}
			}
			surveyIndex += 1;
		}

		getRequestContext().getHibernate().flush();
	}

	private Record createRecord(Survey survey, CensusMethod cm,
			IndicatorSpecies species, User user) throws ParseException {
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
		record.setLocation(null);
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

		DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
		dateFormat.setLenient(false);
		Set<AttributeValue> attributeList = new HashSet<AttributeValue>();
		Map<Attribute, AttributeValue> expectedRecordAttrMap = new HashMap<Attribute, AttributeValue>();
		for (Attribute attr : survey.getAttributes()) {
			AttributeValue recAttr = new AttributeValue();
			recAttr.setAttribute(attr);
			switch (attr.getType()) {
			case INTEGER:
				Integer i = new Integer(123);
				recAttr.setNumericValue(new BigDecimal(i));
				recAttr.setStringValue(i.toString());
				break;
			case INTEGER_WITH_RANGE:
				String intStr = attr.getOptions().iterator().next().getValue();
				recAttr
						.setNumericValue(new BigDecimal(Integer
								.parseInt(intStr)));
				recAttr.setStringValue(intStr);
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
				recAttr.setStringValue(attr.getOptions().iterator().next()
						.getValue());
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

		if (record.getSpecies() != null) {
			for (Attribute attr : record.getSpecies().getTaxonGroup()
					.getAttributes()) {
				if (!attr.isTag()) {
					AttributeValue recAttr = new AttributeValue();
					recAttr.setAttribute(attr);
					switch (attr.getType()) {
					case INTEGER:
						Integer i = Integer.valueOf(987);
						recAttr.setNumericValue(new BigDecimal(i));
						recAttr.setStringValue(i.toString());
						break;
					case INTEGER_WITH_RANGE:
						String intStr = attr.getOptions().iterator().next()
								.getValue();
						recAttr.setNumericValue(new BigDecimal(Integer
								.parseInt(intStr)));
						recAttr.setStringValue(intStr);
						break;
					case DECIMAL:
						Double d = Double.valueOf(987);
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
						recAttr
								.setStringValue("This is a test string record attribute for groups");
						break;
					case TEXT:
						recAttr
								.setStringValue("This is a test text record attribute for groups");
						break;
					case STRING_WITH_VALID_VALUES:
						recAttr.setStringValue(attr.getOptions().iterator()
								.next().getValue());
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

	@Test
	public void testAdminRecordDeletion() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/deleteRecord.htm");
        //request.addParameter("recordId", "");
        
        List<Record> records = recordDAO.getRecords(speciesA);
        for (Record record : records) {
        	Integer id = record.getId(); 
        	request.setParameter("recordId", id.toString());
        	ModelAndView mv = handle(request, response);
        	getRequestContext().getHibernate().flush();
            getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
            Assert.assertNull(recordDAO.getRecord(id));
        }
	}
	
	@Test
	public void testUserRecordDeletion() throws Exception {
        login("testuser", "password", new String[] { Role.USER });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/user/deleteRecord.htm");
        //request.addParameter("recordId", "");
        
        List<Record> records = recordDAO.getRecords(user);
        for (Record record : records) {
        	Integer id = record.getId(); 
        	request.setParameter("recordId", id.toString());
        	ModelAndView mv = handle(request, response);
        	getRequestContext().getHibernate().flush();
            getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
            Assert.assertNull(recordDAO.getRecord(id));
        }
        
        records = recordDAO.getRecords(admin);
        for (Record record : records) {
        	Integer id = record.getId(); 
        	request.setParameter("recordId", id.toString());
        	ModelAndView mv = handle(request, response);
        	getRequestContext().getHibernate().flush();
            getRequestContext().getHibernate().setFlushMode(FlushMode.AUTO);
            Assert.assertNotNull(recordDAO.getRecord(id));
        }
	}
}