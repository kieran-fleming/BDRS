package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import java.util.Date;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.util.StringUtils;

public class RecordPropertyTest extends AbstractControllerTest{
	
	@Autowired
	public SurveyDAO surveyDAO;
	@Autowired
	public MetadataDAO metadataDAO;
	
	HashMap<RecordPropertyType, RecordProperty> recordProperties;
	private Survey survey;
	
	@Before
    public void setUp() throws Exception {
		recordProperties = new HashMap<RecordPropertyType, RecordProperty>();
		Survey s = new Survey();
        s.setStartDate(new Date(System.currentTimeMillis()));
        s.setDescription("Test Survey Description");
        s.setName("A test survey");
        survey = surveyDAO.save(s);
        for (RecordPropertyType type : RecordPropertyType.values()) {
        	recordProperties.put(type, new RecordProperty(survey,type, metadataDAO));
        }
	}
	
	@Test
	public void createRecordPropertyTest() {
		 for (RecordPropertyType type : RecordPropertyType.values()) {
			 Assert.assertNotNull(recordProperties.get(type));
		 }
	}
	
	@Test
	public void testGetMetadataKeys() {
			for (RecordPropertyType type : RecordPropertyType.values()) {
				RecordProperty recordProperty = recordProperties.get(type);
				for (RecordPropertyType type1 : RecordPropertyType.values()) {
					HashMap<RecordPropertySetting, String> metadataKeys = recordProperty.getMetaDataKeys(type1);
					Assert.assertNotNull(metadataKeys);
					for (RecordPropertySetting setting : RecordPropertySetting.values()) {
						//Assert. assertEquals("RECORD." + type1.getName() + "." + setting.toString(), metadataKeys.get(setting));
						Assert. assertEquals(String.format(RecordProperty.METADATA_KEY_TEMPLATE, type1.getName(), setting), metadataKeys.get(setting));
					}
				}
				
			}
	}
	
	@Test
	public void testGetRecordPropertyType () {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			RecordProperty recordProperty = recordProperties.get(type);
			RecordPropertyType actualType = recordProperty.getRecordPropertyType();
			Assert.assertEquals(type, actualType);
		}
	}
	
	@Test
	public void testGetMetadataDefaultValue() {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			RecordProperty recordProperty = recordProperties.get(type);
			for (RecordPropertySetting setting : RecordPropertySetting.values()) {
				String value = recordProperty.getMetadataValue(setting);
				if (setting == RecordPropertySetting.DESCRIPTION) {
					Assert.assertEquals(type.getDefaultDescription(), value);
				} else if (setting == RecordPropertySetting.HIDDEN) {
					Assert.assertEquals("false", value);
				} else if (setting == RecordPropertySetting.REQUIRED) {
					if(type.equals(RecordPropertyType.ACCURACY)) {
						Assert.assertEquals("false", value);
					} else {
						Assert.assertEquals("true", value);
					}
				} else if (setting == RecordPropertySetting.SCOPE) {
					if(type == RecordPropertyType.SPECIES || type == RecordPropertyType.NUMBER) {
						Assert.assertEquals(AttributeScope.RECORD.toString(), value);
					} else {
						Assert.assertEquals(AttributeScope.SURVEY.toString(), value);
					}
				} else if (setting == RecordPropertySetting.WEIGHT) {
					Assert.assertEquals("0", value);
				} else {
					Assert.fail("RecordPropertySetting " + setting + " has no test yet.");
				}
			}
		}
	}
	
	@Test
	public void testSetAndGetScope() {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 RecordProperty recordProperty =  recordProperties.get(type);
			 recordProperty.setScope(AttributeScope.SURVEY);
			 Assert.assertEquals(AttributeScope.SURVEY, recordProperty.getScope());
			 recordProperty.setScope(AttributeScope.RECORD);
			 Assert.assertEquals(AttributeScope.RECORD, recordProperty.getScope());
			 
		 }
	}
	
	@Test
	public void testSetAndIsHidden() {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 RecordProperty recordProperty =  recordProperties.get(type);
			 recordProperty.setHidden(true);
			 Assert.assertEquals(true, recordProperty.isHidden());
			 recordProperty.setHidden(false);
			 Assert.assertEquals(false, recordProperty.isHidden());
			 
		 }
	}
	
	@Test
	public void testSetAndIsRequired() {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 RecordProperty recordProperty =  recordProperties.get(type);
			 recordProperty.setRequired(true);
			 Assert.assertEquals(true, recordProperty.isRequired());
			 recordProperty.setRequired(false);
			 Assert.assertEquals(false, recordProperty.isRequired());
			 
		 }
	}
	
	@Test
	public void testSetAndGetDescription() {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 RecordProperty recordProperty =  recordProperties.get(type);
			 recordProperty.setDescription("FooDescription");
			 Assert.assertEquals("FooDescription", recordProperty.getDescription());
		 }
	}
	
	@Test
	public void testSetAndGetWeight() {
		for (RecordPropertyType type : RecordPropertyType.values()) {
			 RecordProperty recordProperty =  recordProperties.get(type);
			 recordProperty.setWeight(5);
			 Assert.assertEquals(5, recordProperty.getWeight());
		 }
	}

}
