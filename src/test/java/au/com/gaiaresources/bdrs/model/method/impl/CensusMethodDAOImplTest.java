package au.com.gaiaresources.bdrs.model.method.impl;

import java.util.Date;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.db.impl.PagedQueryResult;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter;
import au.com.gaiaresources.bdrs.db.impl.PaginationFilter.SortOrder;
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

public class CensusMethodDAOImplTest extends AbstractControllerTest {
    
    @Autowired 
    CensusMethodDAO cmDAO;
    @Autowired
    AttributeDAO attrDAO;
    @Autowired 
    MetadataDAO metadataDAO;
    @Autowired
    SurveyDAO surveyDAO;
    
    private CensusMethod m1;
    private CensusMethod m2;
    private CensusMethod m3;
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
    public void testSearch() {
        PaginationFilter filter = new PaginationFilter(0, 1);
        filter.addSortingCriteria("name", SortOrder.DESCENDING);       
        PagedQueryResult<CensusMethod> result = cmDAO.search(filter, null, survey.getId());
        Assert.assertEquals(2, result.getCount());
        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals(m2.getId(), result.getList().get(0).getId());
    }
    
    @Test
    public void testSearchNoSurvey() {
        PaginationFilter filter = new PaginationFilter(0, 1);
        filter.addSortingCriteria("name", SortOrder.DESCENDING);       
        PagedQueryResult<CensusMethod> result = cmDAO.search(filter, null, null);
        Assert.assertEquals(3, result.getCount());
        Assert.assertEquals(1, result.getList().size());
        Assert.assertEquals(m3.getId(), result.getList().get(0).getId());
    }
}
