package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.attribute.AttributeFormField;
import au.com.gaiaresources.bdrs.controller.attribute.AttributeInstanceFormField;
import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.security.Role;

public class SurveyAttributeBaseControllerTest extends AbstractControllerTest {

    @Autowired
    private SurveyDAO surveyDAO;
    @Autowired
    private MetadataDAO metadataDAO;
    @Autowired
    private AttributeDAO attributeDAO;

    private Survey survey;

    @Before
    public void setUp() throws Exception {
        survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md);
        
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            md = new Metadata(String.format("property_weight_%s", propertyName),String.valueOf(PersistentImpl.DEFAULT_WEIGHT));
            md = metadataDAO.save(md);
            survey.getMetadata().add(md);
        }
        //survey.setAttributes();
        //survey.setSpecies(null);
        survey = surveyDAO.save(survey);
    }

    @Test
    public void testEditSurveyAttributes() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/editAttributes.htm");
        request.setParameter("surveyId", survey.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "surveyEditAttributes");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");

        int curWeight = Integer.MIN_VALUE;
        Assert.assertEquals(survey, mv.getModelMap().get("survey"));
        List<AttributeFormField> formFieldList = (List<AttributeFormField>) mv.getModelMap().get("formFieldList");
        Assert.assertEquals(Record.RECORD_PROPERTY_NAMES.length, formFieldList.size());
        for (AttributeFormField formField : formFieldList) {
            // Test attributes are sorted by weight
            Assert.assertTrue(formField.getWeight() >= curWeight);
            curWeight = formField.getWeight();

            // Since the survey initially has no attributes, all attributes in
            // the list must be property attributes.
            Assert.assertTrue(formField.isPropertyField());
        }
    }

    @Test
    public void testEditingSurveyAttributes() throws Exception {
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

                if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for (int i = 0; i < 4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        opt = attributeDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }

                attr = attributeDAO.save(attr);
                attributeList.add(attr);
            }
        }
        survey.setAttributes(attributeList);
        survey = surveyDAO.save(survey);

        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/survey/editAttributes.htm");
        request.setParameter("surveyId", survey.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "surveyEditAttributes");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "survey");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");

        int curWeight = Integer.MIN_VALUE;
        Assert.assertEquals(survey, mv.getModelMap().get("survey"));
        List<AttributeFormField> formFieldList = (List<AttributeFormField>) mv.getModelMap().get("formFieldList");
        // Test all attributes and properties are represented.
        Assert.assertEquals(Record.RECORD_PROPERTY_NAMES.length
                + attributeList.size(), formFieldList.size());
        for (AttributeFormField formField : formFieldList) {
            // Test attributes are sorted by weight
            Assert.assertTrue(formField.getWeight() >= curWeight);
            curWeight = formField.getWeight();

            if (formField.isAttributeField()) {
                Assert.assertTrue(attributeList.contains(((AttributeInstanceFormField) formField).getAttribute()));
            } else if (formField.isPropertyField()) {
                // Nothing to test
            } else {
                Assert.assertTrue(false);
            }
        }
    }

    @Test
    public void testSubmitSurveyAttributesAdding() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/editAttributes.htm");

        int curWeight = 0;
        Map<String, String> params = new HashMap<String, String>();
        params.put("surveyId", survey.getId().toString());

        String attributeOptions = "Option A,Option B,Option C,Option D";

        int index = 0;
        for (AttributeType attrType : AttributeType.values()) {
            for (AttributeScope scope : AttributeScope.values()) {

                request.addParameter("add_attribute", String.valueOf(index));
                params.put(String.format("add_weight_%d", index), String.valueOf(curWeight));
                String name = String.format("%s_%s_%d", attrType.toString(), scope.toString(), index);
                params.put(String.format("add_name_%d", index), name);
                params.put(String.format("add_description_%d", index), name
                        + "_description");
                params.put(String.format("add_typeCode_%d", index), attrType.getCode());
                params.put(String.format("add_scope_%d", index), scope.toString());

                if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    params.put(String.format("add_option_%d", index), attributeOptions);
                }

                index = index + 1;
                curWeight = curWeight + 100;
            }
        }
        
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            params.put(String.format("property_weight_%s", propertyName), String.valueOf(curWeight));
            curWeight = curWeight + 100;
        }
                
        request.addParameters(params);

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/listing.htm", redirect.getUrl());

        Survey actualSurvey = surveyDAO.get(new Integer(params.get("surveyId")));
        Assert.assertEquals(survey.getId(), actualSurvey.getId());

        int expectedAttrCount = AttributeType.values().length
                * AttributeScope.values().length;
        Assert.assertEquals(expectedAttrCount, actualSurvey.getAttributes().size());

        index = 0;
        for (Attribute attribute : actualSurvey.getAttributes()) {
            Assert.assertEquals(Integer.parseInt(params.get(String.format("add_weight_%d", index))), attribute.getWeight());
            Assert.assertEquals(params.get(String.format("add_name_%d", index)), attribute.getName());
            Assert.assertEquals(params.get(String.format("add_description_%d", index)), attribute.getDescription());
            Assert.assertEquals(params.get(String.format("add_typeCode_%d", index)), attribute.getTypeCode());
            Assert.assertEquals(AttributeScope.valueOf(params.get(String.format("add_scope_%d", index))), attribute.getScope());

            if (AttributeType.STRING_WITH_VALID_VALUES.getCode().equals(attribute.getTypeCode())) {
                String[] options = attributeOptions.split(",");
                for (int i = 0; i < options.length; i++) {
                    Assert.assertEquals(options[i], attribute.getOptions().get(i).getValue());
                }
            }

            index = index + 1;
        }
    }

    @Test
    public void testSubmitSurveyAttributesEditing() throws Exception {
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for(AttributeType attrType : AttributeType.values()) {
            for(AttributeScope scope : AttributeScope.values()) {
                
                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(scope);
                attr.setTag(false);
                
                if(AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for(int i=0; i<4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        opt = attributeDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }
                
                attr = attributeDAO.save(attr);
                attributeList.add(attr);
            }
        }
        survey.setAttributes(attributeList);
        survey = surveyDAO.save(survey);
        
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/editAttributes.htm");
        request.setParameter("surveyId", survey.getId().toString());
        
        int curWeight = 0;
        Map<String, String> params = new HashMap<String, String> ();
        params.put("surveyId", survey.getId().toString());
        for(Attribute attribute : survey.getAttributes()){
            params.put(String.format("weight_%d", attribute.getId()), String.valueOf(curWeight));
            params.put(String.format("description_%d", attribute.getId()), attribute.getDescription() + " Edited");
            params.put(String.format("name_%d", attribute.getId()), attribute.getName() + " Edited");
            params.put(String.format("typeCode_%d", attribute.getId()), attribute.getTypeCode());
            params.put(String.format("scope_%d", attribute.getId()), attribute.getScope().toString());
            request.addParameter("attribute", attribute.getId().toString());
            
            curWeight = curWeight + 100;
        }
        
        curWeight = 0;
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            params.put(String.format("property_weight_%s", propertyName), String.valueOf(Integer.MAX_VALUE - curWeight));
            
            curWeight = curWeight + 1; 
        }
        
        request.addParameters(params);

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/listing.htm", redirect.getUrl());

        Survey actualSurvey = surveyDAO.get(new Integer(params.get("surveyId")));
        Assert.assertEquals(survey.getId(), actualSurvey.getId());

        for (Attribute attribute : actualSurvey.getAttributes()) {
            Assert.assertEquals(Integer.parseInt(params.get(String.format("weight_%d", attribute.getId()))), attribute.getWeight());
            Assert.assertEquals(params.get(String.format("name_%d", attribute.getId())), attribute.getName());
            Assert.assertEquals(params.get(String.format("description_%d", attribute.getId())), attribute.getDescription());
            Assert.assertEquals(params.get(String.format("typeCode_%d", attribute.getId())), attribute.getTypeCode());
            Assert.assertEquals(AttributeScope.valueOf(params.get(String.format("scope_%d", attribute.getId()))), attribute.getScope());
        }
        
        Metadata md;
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            md = actualSurvey.getMetadataByKey(String.format(Metadata.RECORD_PROPERTY_FIELD_METADATA_KEY_TEMPLATE, propertyName));
            Assert.assertEquals(params.get(String.format("property_weight_%s", propertyName)), md.getValue());
        }
    }

    @Test
    public void testSubmitSurveyAttributesSaveAndContinue() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/editAttributes.htm");
        request.setParameter("surveyId", survey.getId().toString());
        int curWeight = 0;
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            request.setParameter(String.format("property_weight_%s", propertyName), String.valueOf(curWeight));
            curWeight = curWeight + 100;
        }
        request.setParameter("saveAndContinue", "saveAndContinue");

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/survey/editLocations.htm", redirect.getUrl());
    }

    @Test
    public void testSubmitSurveyAttributesPreview() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/survey/editAttributes.htm");
        request.setParameter("surveyId", survey.getId().toString());
        int curWeight = 0;
        for(String propertyName : Record.RECORD_PROPERTY_NAMES) {
            request.setParameter(String.format("property_weight_%s", propertyName), String.valueOf(curWeight));
            curWeight = curWeight + 100;
        }
        request.setParameter("saveAndPreview", "saveAndPreview");

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/user/surveyRenderRedirect.htm", redirect.getUrl());
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "surveyId");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "preview");
    }
}
