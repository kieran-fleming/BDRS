package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
import net.sf.json.JSONSerializer;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.insecure.taxa.ComparePersistentImplByWeight;
import au.com.gaiaresources.bdrs.controller.webservice.JqGridDataHelper;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.method.CensusMethodDAO;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.survey.SurveyDAO;
import au.com.gaiaresources.bdrs.model.survey.SurveyFormRendererType;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;

public class CensusMethodControllerTest extends AbstractControllerTest {
    
    @Autowired
    private CensusMethodDAO cmDAO;
    @Autowired
    private AttributeDAO attrDAO;
    @Autowired
    SurveyDAO surveyDAO;
    @Autowired
    MetadataDAO metadataDAO;
    
    CensusMethod m1;
    CensusMethod m2;
    CensusMethod m3;
    private Survey survey;
    
    @Before
    public void setup() {
        // worst test data generation ever!
        m1 = new CensusMethod();
        m1.setName("apple");
        m1.setTaxonomic(Taxonomic.NONTAXONOMIC);
        Attribute testAttr1 = new Attribute();
        testAttr1.setName("an_attribute");
        testAttr1.setDescription("attribute description");
        testAttr1.setRequired(true);
        testAttr1.setScope(AttributeScope.RECORD);
        testAttr1.setTag(false);
        testAttr1.setTypeCode("IN");
        attrDAO.save(testAttr1);
        
        Attribute testAttr2 = new Attribute();
        testAttr2.setName("anotherattr");
        testAttr2.setDescription("attribsdfsute desgfdsdfcription");
        testAttr2.setRequired(true);
        testAttr2.setScope(AttributeScope.RECORD);
        testAttr2.setTag(false);
        testAttr2.setTypeCode("ST");
        attrDAO.save(testAttr2);
        
        m1.getAttributes().add(testAttr1);
        m1.getAttributes().add(testAttr2);
        m1 = cmDAO.save(m1);
        
        
        m2 = new CensusMethod();
        m2.setName("banana");
        m2.setTaxonomic(Taxonomic.TAXONOMIC);
        Attribute testAttr3 = new Attribute();
        testAttr3.setName("an_attribute_22");
        testAttr3.setDescription("attribute description 22");
        testAttr3.setRequired(true);
        testAttr3.setScope(AttributeScope.RECORD);
        testAttr3.setTag(false);
        testAttr3.setTypeCode("IN 22");
        m2.getAttributes().add(testAttr3);
        m2 = cmDAO.save(m2);
        attrDAO.save(testAttr3);
        
        m3 = new CensusMethod();
        m3.setName("chicken");
        m3.setTaxonomic(Taxonomic.TAXONOMIC);
        Attribute testAttr4 = new Attribute();
        testAttr4.setName("an_attribute_22");
        testAttr4.setDescription("attribute description 22");
        testAttr4.setRequired(true);
        testAttr4.setScope(AttributeScope.RECORD);
        testAttr4.setTag(false);
        testAttr4.setTypeCode("INasd22");
        m3.getAttributes().add(testAttr4);
        m3 = cmDAO.save(m3);
        attrDAO.save(testAttr4);
        
        survey = new Survey();
        survey.setName("SingleSiteMultiTaxaSurvey 1234");
        survey.setActive(true);
        survey.setStartDate(new Date());
        survey.setDescription("Single Site Multi Taxa Survey Description");
        Metadata md = survey.setFormRendererType(SurveyFormRendererType.DEFAULT);
        metadataDAO.save(md);
        // no attributes
        //survey.setAttributes(attributeList);
        // no species which infact means all species
        //survey.setSpecies(speciesSet);
        survey.getCensusMethods().add(m1);
        survey.getCensusMethods().add(m2);
        survey = surveyDAO.save(survey);
    }
    
    @Test
    public void testListing() {

    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testViewNew() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");
        
        ModelAndView mv = this.handle(request, response);
        
        CensusMethod newMethod = (CensusMethod)mv.getModel().get("censusMethod");
        Assert.assertNull(newMethod.getId());
         
        List<AttributeFormField> attrList = (List<AttributeFormField>)mv.getModel().get("attributeFormFieldList");
        Assert.assertEquals(0, attrList.size());
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void testViewExisting() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");        
        request.setParameter("censusMethodId", m1.getId().toString());
        
        ModelAndView mv = this.handle(request, response);
        
        CensusMethod newMethod = (CensusMethod)mv.getModel().get("censusMethod");
        Assert.assertNotNull(newMethod.getId());
        Assert.assertEquals(m1.getId().intValue(), newMethod.getId().intValue());
         
        List<AttributeFormField> attrList = (List<AttributeFormField>)mv.getModel().get("attributeFormFieldList");
        Assert.assertEquals(m1.getAttributes().size(), attrList.size());
    }
    
    // will test the add functionality of course...
    // always test with adding 2 items or more.
    @Test
    public void testEditNew() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");
        
        testEditing("new census method", null);
    }
    
    @Test
    public void testEditExistingAdd() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");
        request.setParameter("censusMethodId", m1.getId().toString());
        
        testEditing("mama mia", m1.getId());
    }
    
    @Test
    public void testSetTaxonomic1() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");
        request.setParameter("censusMethodId", m1.getId().toString());
        request.setParameter("censusMethodName", m1.getName());
        request.setParameter("taxonomic", "TAXONOMIC");
        
        handle(request, response);
        
        List<CensusMethod> censusMethodList = cmDAO.search(null, m1.getName(), null).getList();
        Assert.assertEquals(censusMethodList.size(), 1);
        CensusMethod cmUnderTest = censusMethodList.get(0);
        Assert.assertTrue(Taxonomic.TAXONOMIC.equals(cmUnderTest.getTaxonomic()));
    }
    
    @Test
    public void testSetTaxonomic2() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");
        request.setParameter("censusMethodId", m1.getId().toString());
        request.setParameter("censusMethodName", m1.getName());
        request.setParameter("taxonomic", "NONTAXONOMIC");
        
        handle(request, response);
        
        List<CensusMethod> censusMethodList = cmDAO.search(null, m1.getName(), null).getList();
        Assert.assertEquals(censusMethodList.size(), 1);
        CensusMethod cmUnderTest = censusMethodList.get(0);
        Assert.assertFalse(!Taxonomic.NONTAXONOMIC.equals(cmUnderTest.getTaxonomic()));
    }
    
    @Test
    public void testSetTaxonomic3() throws Exception {
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/censusMethod/edit.htm");
        request.setParameter("censusMethodId", m1.getId().toString());
        request.setParameter("censusMethodName", m1.getName());
        request.setParameter("taxonomic", "OPTIONALLYTAXONOMIC");
        
        handle(request, response);
        
        List<CensusMethod> censusMethodList = cmDAO.search(null, m1.getName(), null).getList();
        Assert.assertEquals(censusMethodList.size(), 1);
        CensusMethod cmUnderTest = censusMethodList.get(0);
        Assert.assertFalse(!Taxonomic.OPTIONALLYTAXONOMIC.equals(cmUnderTest.getTaxonomic()));
    }
    
    private void testEditing(String newName, Integer expectedId) throws Exception {
     // Attributes
        int curWeight = 0;
        String attributeOptions = "Option A,Option B,Option C,Option D";
        int index = 0;
        
        request.addParameter("censusMethodName", newName);
        request.setParameter("taxonomic", "TAXONOMIC");
        request.addParameter("childCensusMethod", m2.getId().toString());
        request.addParameter("childCensusMethod", m3.getId().toString());
        
        Boolean isTag = false;
        for (AttributeType attrType : AttributeType.values()) {

            request.addParameter("add_attribute", String.valueOf(index));
            request.setParameter(String.format("add_weight_%d", index), String.valueOf(curWeight));
            request.setParameter(String.format("add_name_%d", index), "test name isTag="+isTag.toString());
            request.setParameter(String.format("add_description_%d", index), "test description isTag="+isTag.toString());
            request.setParameter(String.format("add_typeCode_%d", index), attrType.getCode());
            request.setParameter(String.format("add_tag_%d", index), isTag.toString().toLowerCase());
            //request.setParameter(String.format("add_scope_%d", index), scope.toString());
            if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                request.setParameter(String.format("add_option_%d", index), attributeOptions);
            }

            index = index + 1;
            curWeight = curWeight + 100;
        }
        
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/censusMethod/listing.htm", redirect.getUrl());
        
        List<CensusMethod> censusMethodList = cmDAO.search(null, newName, null).getList();
        Assert.assertEquals(censusMethodList.size(), 1);
        CensusMethod cmUnderTest = censusMethodList.get(0);
        
        if (expectedId != null) {
            Assert.assertEquals(expectedId, cmUnderTest.getId());
        }
        Assert.assertEquals(newName, cmUnderTest.getName());
        
        int expectedAttrCount = AttributeType.values().length;
        Assert.assertEquals(expectedAttrCount, cmUnderTest.getAttributes().size());
        
        List<Attribute> sortedAttrList = new ArrayList<Attribute>(cmUnderTest.getAttributes());
        Collections.sort(sortedAttrList, new ComparePersistentImplByWeight());
        
        index = 0;
        for (Attribute attribute : sortedAttrList) {
            Assert.assertEquals(Integer.parseInt(request.getParameter(String.format("add_weight_%d", index))), attribute.getWeight());
            Assert.assertEquals(request.getParameter(String.format("add_name_%d", index)), attribute.getName());
            Assert.assertEquals(request.getParameter(String.format("add_description_%d", index)), attribute.getDescription());
            Assert.assertEquals(request.getParameter(String.format("add_typeCode_%d", index)), attribute.getTypeCode());
            Assert.assertEquals(Boolean.parseBoolean(request.getParameter(String.format("add_tag_%d", index))), attribute.isTag());
            Assert.assertEquals(null, attribute.getScope());
        
            if (AttributeType.STRING_WITH_VALID_VALUES.getCode().equals(attribute.getTypeCode())) {
                String[] options = attributeOptions.split(",");
                for (int i = 0; i < options.length; i++) {
                    Assert.assertEquals(options[i], attribute.getOptions().get(i).getValue());
                }
            }
        
            index = index + 1;
        }
        
        Assert.assertEquals(2, cmUnderTest.getCensusMethods().size());
        Assert.assertTrue(cmUnderTest.getCensusMethods().contains(m2));
        Assert.assertTrue(cmUnderTest.getCensusMethods().contains(m3));
        Assert.assertTrue(Taxonomic.TAXONOMIC.equals(cmUnderTest.getTaxonomic()));
    }

    
    @Test
    public void testSearchService() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/censusMethod/search.htm");
       
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "3");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, "name");
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(1, rowArray.size());
        Assert.assertEquals(3, json.getLong("records"));
        Assert.assertEquals("apple", ((JSONObject)rowArray.get(0)).getString("name"));     
        Assert.assertEquals("Non Taxonomic", ((JSONObject)rowArray.get(0)).getString("taxonomic"));
    }
    
    @Test
    public void testSearchServiceWithSurvey() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/censusMethod/search.htm");
       
        // should have 0 matches
        request.setParameter("surveyId", "3");
        
        request.setParameter(JqGridDataHelper.MAX_PER_PAGE_PARAM, "1");
        request.setParameter(JqGridDataHelper.REQUESTED_PAGE_PARAM, "3");
        request.setParameter(JqGridDataHelper.SORT_IDX_PARAM, "name");
        request.setParameter(JqGridDataHelper.SORT_ORDER_PARAM, JqGridDataHelper.DESC);
        
        this.handle(request, response);
        
        JSONObject json = (JSONObject) JSONSerializer.toJSON(response.getContentAsString());
        JSONArray rowArray = json.getJSONArray("rows");
        Assert.assertEquals(0, rowArray.size());
        Assert.assertEquals(0, json.getLong("records"));
    }
    
    @Test
    public void testAjaxAddRow() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/censusMethod/ajaxAddSubCensusMethod.htm");
        request.setParameter("id", m1.getId().toString());
        
        ModelAndView mv = this.handle(request,response);
        
        Assert.assertEquals(m1.getName(), mv.getModel().get("name"));
        Assert.assertEquals(m1.getTaxonomic(), mv.getModel().get("taxonomic"));
    }
    
    @Test
    public void testGetCensusMethodForSurvey() throws Exception {
        request.setMethod("GET");
        request.setRequestURI("/bdrs/user/censusMethod/getSurveyCensusMethods.htm");
        request.setParameter("surveyId", survey.getId().toString());
        
        this.handle(request,response);
                
        JSONArray array = (JSONArray)JSONSerializer.toJSON(response.getContentAsString());
        
        // 2 census method items plus the default item
        Assert.assertEquals(3, array.size());
    }
}
