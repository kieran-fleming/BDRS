package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.Date;
import java.util.HashMap;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertySetting;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertyType;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;

public class RecordPropertyAttributeFormFieldTest extends AbstractControllerTest{
	
    @Autowired
    private MetadataDAO metadataDAO;
    @Autowired
    private SurveyDAO surveyDAO;
    
    private Survey survey;

	@Before
    public void setUp() throws Exception {
		
	    Survey s = new Survey();
        s.setStartDate(new Date(System.currentTimeMillis()));
        s.setDescription("Test Survey Description");
        s.setName("A test survey");
        survey = surveyDAO.save(s);
	}
	/**
	 * Test the constructor of the <code>RecordPropertyAttributeFormField</code>
	 */
	@Test
	public void testCreateAttributeFormFieldTest(){
		RecordProperty recordProperty = new RecordProperty(survey, RecordPropertyType.SPECIES, metadataDAO);
		HashMap<RecordPropertySetting, String>  mdKeys= RecordProperty.getMetaDataKeys(RecordPropertyType.SPECIES);
		request.setParameter(mdKeys.get(RecordPropertySetting.WEIGHT), "1");
		request.setParameter(mdKeys.get(RecordPropertySetting.DESCRIPTION),"species_label");
		request.setParameter(mdKeys.get(RecordPropertySetting.REQUIRED),Boolean.TRUE.toString());
		request.setParameter(mdKeys.get(RecordPropertySetting.SCOPE),AttributeScope.RECORD.toString());
		request.setParameter(mdKeys.get(RecordPropertySetting.HIDDEN),Boolean.FALSE.toString());
		RecordPropertyAttributeFormField field =  new RecordPropertyAttributeFormField(recordProperty, request.getParameterMap());
		Assert.assertEquals(request.getParameter(mdKeys.get(RecordPropertySetting.DESCRIPTION)), field.getDescription());
	}
	
}
