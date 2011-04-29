package au.com.gaiaresources.bdrs.controller.taxongroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.attribute.AttributeFormField;
import au.com.gaiaresources.bdrs.controller.insecure.taxa.ComparePersistentImplByWeight;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeDAO;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;

public class TaxonGroupManagementControllerTest extends AbstractControllerTest {
    
    @Autowired
    private TaxaDAO taxaDAO;
    @Autowired
    private AttributeDAO attributeDAO;
    
    @Test
    public void testListing() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxongroup/listing.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "taxonGroupList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroupList");
    }
    
    @Test
    public void testAddTaxonGroup() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxongroup/edit.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "taxonGroupEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroup");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "attributeFormFieldList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "identificationFormFieldList");
    }
    
    @Test
    public void testAddTaxonGroupSubmit() throws Exception {
        
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/taxongroup/edit.htm");
        
        request.setParameter("name", "test name");
        request.setParameter("behaviourIncluded", "true");
        request.setParameter("firstAppearanceIncluded", "true");
        request.setParameter("lastAppearanceIncluded", "true");
        request.setParameter("habitatIncluded", "true");
        request.setParameter("weatherIncluded", "true");
        request.setParameter("numberIncluded", "true");
        
        // Image and Thumbnail
        for(String propertyName : new String[] { "image", "thumbNail" }) {
            String key = String.format("%s_file", propertyName);
            String image_filename = String.format("%s_filename", propertyName);
            MockMultipartFile mockImageFile = new MockMultipartFile(key, image_filename, "image/png", image_filename.getBytes());
            ((MockMultipartHttpServletRequest)request).addFile(mockImageFile);
            
            request.setParameter(propertyName, image_filename);
        }
        
        // Attributes
        int curWeight = 0;
        String attributeOptions = "Option A,Option B,Option C,Option D";
        int index = 0;
        for(Boolean isTag : new Boolean[] { true, false }) {
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
        }
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/taxongroup/listing.htm", redirect.getUrl());
        
        List<? extends TaxonGroup> groups = taxaDAO.getTaxonGroups();
        Assert.assertEquals(groups.size(), 1);
        TaxonGroup actualGroup = (TaxonGroup)groups.get(0);
        
        Assert.assertEquals(request.getParameter("name"), actualGroup.getName());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("behaviourIncluded")), actualGroup.isBehaviourIncluded()); 
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("firstAppearanceIncluded")), actualGroup.isFirstAppearanceIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("lastAppearanceIncluded")), actualGroup.isLastAppearanceIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("habitatIncluded")), actualGroup.isHabitatIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("weatherIncluded")), actualGroup.isWeatherIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("numberIncluded")), actualGroup.isNumberIncluded());
        Assert.assertEquals("image_filename", actualGroup.getImage());
        Assert.assertEquals("thumbNail_filename", actualGroup.getThumbNail());
        
        // Multiply by two because half are tags and half are not.
        int expectedAttrCount = AttributeType.values().length * 2;
        Assert.assertEquals(expectedAttrCount, actualGroup.getAttributes().size());
        
        List<Attribute> sortedAttrList = new ArrayList<Attribute>(actualGroup.getAttributes());
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
    }
    
    @Test
    public void testEditTaxonGroup() throws Exception {
        
        TaxonGroup group = new TaxonGroup();
        group.setName("Test Taxon Group");
        group.setBehaviourIncluded(true);
        group.setFirstAppearanceIncluded(true);
        group.setLastAppearanceIncluded(true);
        group.setHabitatIncluded(true);
        group.setWeatherIncluded(true);
        group.setNumberIncluded(true);
        
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for(boolean isTag : new boolean[] {true, false}) {
            for (AttributeType attrType : AttributeType.values()) {
                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(null);
                attr.setTag(isTag);
    
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
        group.setAttributes(attributeList);
        group = taxaDAO.save(group);
                
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxongroup/edit.htm");
        request.setParameter("pk", group.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "taxonGroupEdit");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroup");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "attributeFormFieldList");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "identificationFormFieldList");
        
        int attributeListSize = ((List<AttributeFormField>)mv.getModel().get("attributeFormFieldList")).size() +
            ((List<AttributeFormField>)mv.getModel().get("identificationFormFieldList")).size();
        Assert.assertEquals(attributeList.size(), attributeListSize);
    }
    
    @Test
    public void testEditTaxonGroupSubmit() throws Exception {
        
        TaxonGroup group = new TaxonGroup();
        group.setName("Test Taxon Group");
        group.setBehaviourIncluded(true);
        group.setFirstAppearanceIncluded(true);
        group.setLastAppearanceIncluded(true);
        group.setHabitatIncluded(true);
        group.setWeatherIncluded(true);
        group.setNumberIncluded(true);
        
        List<Attribute> attributeList = new ArrayList<Attribute>();
        Attribute attr;
        for(boolean isTag : new boolean[] {true, false}) {
            for (AttributeType attrType : AttributeType.values()) {
                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(attrType.toString()+"is Tag = "+isTag);
                attr.setDescription("test description. is Tag = "+isTag);
                attr.setTypeCode(attrType.getCode());
                attr.setScope(null);
                attr.setTag(isTag);
    
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
        group.setAttributes(attributeList);
        group = taxaDAO.save(group);
        
        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/taxongroup/edit.htm");
        
        request.setParameter("taxonGroupPk", group.getId().toString());
        request.setParameter("name", "test name");
        request.setParameter("behaviourIncluded", "false");
        request.setParameter("firstAppearanceIncluded", "false");
        request.setParameter("lastAppearanceIncluded", "false");
        request.setParameter("habitatIncluded", "false");
        request.setParameter("weatherIncluded", "false");
        request.setParameter("numberIncluded", "false");
        
        // Image and Thumbnail
        for(String propertyName : new String[] { "image", "thumbNail" }) {
            String key = String.format("%s_file", propertyName);
            String image_filename = String.format("%s_filename", propertyName);
            MockMultipartFile mockImageFile = new MockMultipartFile(key, image_filename, "image/png", image_filename.getBytes());
            ((MockMultipartHttpServletRequest)request).addFile(mockImageFile);
            
            request.setParameter(propertyName, image_filename);
        }
        
        // Attributes
        String attributeOptions = "Option A,Option B,Option C,Option D";
        int curWeight = 0;
        for(Attribute attribute : group.getAttributes()) {
            request.addParameter("attribute", attribute.getId().toString());
            request.setParameter(String.format("weight_%d", attribute.getId()), String.valueOf(curWeight));
            request.setParameter(String.format("name_%d", attribute.getId()), "test name edited");
            request.setParameter(String.format("description_%d", attribute.getId()), "test description edited");
            request.setParameter(String.format("typeCode_%d", attribute.getId()), attribute.getType().getCode());

            if (AttributeType.STRING_WITH_VALID_VALUES.equals(attribute.getType())) {
                request.setParameter(String.format("option_%d", attribute.getId()), attributeOptions);
            }

            curWeight = curWeight + 100;
        }
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/taxongroup/listing.htm", redirect.getUrl());
        
        TaxonGroup actualGroup = taxaDAO.getTaxonGroup(group.getId());
        Assert.assertEquals(request.getParameter("name"), actualGroup.getName());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("behaviourIncluded")), actualGroup.isBehaviourIncluded()); 
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("firstAppearanceIncluded")), actualGroup.isFirstAppearanceIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("lastAppearanceIncluded")), actualGroup.isLastAppearanceIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("habitatIncluded")), actualGroup.isHabitatIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("weatherIncluded")), actualGroup.isWeatherIncluded());
        Assert.assertEquals(Boolean.parseBoolean(request.getParameter("numberIncluded")), actualGroup.isNumberIncluded());
        Assert.assertEquals("image_filename", actualGroup.getImage());
        Assert.assertEquals("thumbNail_filename", actualGroup.getThumbNail());
        
        // Multiply by two because half are tags and half are not.
        int expectedAttrCount = AttributeType.values().length * 2;
        Assert.assertEquals(expectedAttrCount, actualGroup.getAttributes().size());
        
        for (Attribute attribute : actualGroup.getAttributes()) {
            Assert.assertEquals(Integer.parseInt(request.getParameter(String.format("weight_%d", attribute.getId()))), attribute.getWeight());
            Assert.assertEquals(request.getParameter(String.format("name_%d", attribute.getId())), attribute.getName());
            Assert.assertEquals(request.getParameter(String.format("description_%d", attribute.getId())), attribute.getDescription());
            Assert.assertEquals(request.getParameter(String.format("typeCode_%d", attribute.getId())), attribute.getTypeCode());
            Assert.assertEquals(Boolean.parseBoolean(request.getParameter(String.format("tag_%d", attribute.getId()))), attribute.isTag());
            Assert.assertEquals(null, attribute.getScope());
        
            if (AttributeType.STRING_WITH_VALID_VALUES.getCode().equals(attribute.getTypeCode())) {
                String[] options = attributeOptions.split(",");
                for (int i = 0; i < options.length; i++) {
                    Assert.assertEquals(options[i], attribute.getOptions().get(i).getValue());
                }
            }
        }
    }

    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}
