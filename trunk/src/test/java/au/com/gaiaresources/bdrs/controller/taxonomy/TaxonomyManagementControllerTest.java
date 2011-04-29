package au.com.gaiaresources.bdrs.controller.taxonomy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.mock.web.MockMultipartHttpServletRequest;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.record.AttributeParser;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeOption;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfile;
import au.com.gaiaresources.bdrs.model.taxa.SpeciesProfileDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.model.taxa.TaxonRank;
import au.com.gaiaresources.bdrs.security.Role;

public class TaxonomyManagementControllerTest extends AbstractControllerTest {

    @Autowired
    private TaxaDAO taxaDAO;
    
    @Autowired
    private SpeciesProfileDAO profileDAO;

    private TaxonGroup taxonGroupBirds;

    private TaxonGroup taxonGroupButterflies;
    
    private TaxonGroup taxonGroupFrogs;

    private IndicatorSpecies speciesA;

    private IndicatorSpecies speciesB;

    @Before
    public void setUp() throws Exception {
        taxonGroupBirds = new TaxonGroup();
        taxonGroupBirds.setName("Birds");
        taxonGroupBirds = taxaDAO.save(taxonGroupBirds);

        taxonGroupButterflies = new TaxonGroup();
        taxonGroupButterflies.setName("Butterflies");
        taxonGroupButterflies = taxaDAO.save(taxonGroupButterflies);
        
        taxonGroupFrogs = new TaxonGroup();
        taxonGroupFrogs.setName("Frogs");
        taxonGroupFrogs = taxaDAO.save(taxonGroupFrogs);

        List<Attribute> attributeList;
        Attribute attr;
        for (TaxonGroup group : new TaxonGroup[] { taxonGroupBirds,
                taxonGroupButterflies, taxonGroupFrogs }) {
            attributeList = new ArrayList<Attribute>();
            for (AttributeType attrType : AttributeType.values()) {
                attr = new Attribute();
                attr.setRequired(true);
                attr.setName(group.getName() + "_" + attrType.toString());
                attr.setDescription(group.getName() + "_" + attrType.toString());
                attr.setTypeCode(attrType.getCode());
                attr.setScope(null);
                attr.setTag(true);

                if (AttributeType.STRING_WITH_VALID_VALUES.equals(attrType)) {
                    List<AttributeOption> optionList = new ArrayList<AttributeOption>();
                    for (int i = 0; i < 4; i++) {
                        AttributeOption opt = new AttributeOption();
                        opt.setValue(String.format("Option %d", i));
                        opt = taxaDAO.save(opt);
                        optionList.add(opt);
                    }
                    attr.setOptions(optionList);
                }

                attr = taxaDAO.save(attr);
                attributeList.add(attr);
            }
            group.setAttributes(attributeList);
            taxaDAO.save(group);
        }

        SpeciesProfile profile;
        List<SpeciesProfile> profileList = new ArrayList<SpeciesProfile>();
        for(String profileIndex : new String[]{ "A", "B", "C"}) {
            profile = new SpeciesProfile();
            profile.setType("Profile Type "+profileIndex);
            profile.setContent("Profile Content "+profileIndex);
            profile.setDescription("Profile Description "+profileIndex);
            profile.setHeader("Profile Header "+profileIndex);
            profile = profileDAO.save(profile);
            profileList.add(profile);
        }

        speciesA = new IndicatorSpecies();
        speciesA.setCommonName("Indicator Species A");
        speciesA.setScientificName("Indicator Species A");
        speciesA.setTaxonGroup(taxonGroupBirds);
        speciesA.setInfoItems(profileList);
        speciesA = taxaDAO.save(speciesA);

        profileList = new ArrayList<SpeciesProfile>();
        for(String profileIndex : new String[]{ "X", "Y", "Z"}) {
            profile = new SpeciesProfile();
            profile.setType("Profile Type "+profileIndex);
            profile.setContent("Profile Content "+profileIndex);
            profile.setDescription("Profile Description "+profileIndex);
            profile.setHeader("Profile Header "+profileIndex);
            profile = profileDAO.save(profile);
            profileList.add(profile);
        }
        
        speciesB = new IndicatorSpecies();
        speciesB.setCommonName("Indicator Species B");
        speciesB.setScientificName("Indicator Species B");
        speciesB.setTaxonGroup(taxonGroupButterflies);
        speciesB.setInfoItems(profileList);
        speciesB = taxaDAO.save(speciesB);
    }

    @Test
    public void testListing() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxonomy/listing.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "taxonomyList");
    }
    
    @Test
    public void testTaxonSearchWithDepth() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/webservice/taxon/searchTaxon.htm");
        request.setParameter("q", speciesA.getCommonName());
        request.setParameter("depth", "2");

        handle(request, response);
        Assert.assertEquals("Content type should be application/json",
                            "application/json", response.getContentType());
    }
    
    @Test
    public void testTaxonByIdWithDepth() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/webservice/taxon/getTaxonById.htm");
        request.setParameter("id", speciesA.getId().toString());
        request.setParameter("depth", "2");

        handle(request, response);
        Assert.assertEquals("Content type should be application/json",
                            "application/json", response.getContentType());
    }
    
    @Test
    public void testAddTaxon() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxonomy/edit.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "editTaxon");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxon");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");
    }

    @Test
    public void testAddProfileRow() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxonomy/ajaxAddProfile.htm");
        request.setParameter("index", "0");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "taxonProfileRow");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "index");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "profile");
    }
    
    @Test
    public void testAddTaxonSubmit() throws Exception {
        testAddTaxon(true);
    }

    @Test
    public void testAddTaxonSubmitNoParent() throws Exception {
        testAddTaxon(false);
    }
    
    @Test
    public void testEditTaxon() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/taxonomy/edit.htm");
        request.setParameter("pk", speciesA.getId().toString());

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "editTaxon");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxon");
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "formFieldList");
    }
    
    @Test
    public void testEditTaxonSubmit() throws Exception {
        testEditTaxon(true);
    }

    @Test
    public void testEditTaxonSubmitNoParent() throws Exception {
        testEditTaxon(false);
    }
    
    private void testEditTaxon(boolean withParent) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Date today = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/taxonomy/edit.htm");
        
        request.setParameter("taxonPk", speciesA.getId().toString());
        request.setParameter("scientificName", "Test Scientific Name");
        request.setParameter("commonName", "Test Common Name");
        request.setParameter("taxonRank", TaxonRank.SUBSPECIES.toString());
        if(withParent) {
            request.setParameter("parentPk", speciesB.getId().toString());
        } else {
            request.setParameter("parentPk", new String());
        }
        request.setParameter("taxonGroupPk", taxonGroupFrogs.getId().toString());
        request.setParameter("author", "Brock Urban");
        request.setParameter("year", "2010");
        
        // Delete a profile, edit a profile and add a profile
        request.addParameter("new_profile", "3");
        request.setParameter("new_profile_type_3", "Test Type 3");
        request.setParameter("new_profile_content_3", "Test Content 3");
        request.setParameter("new_profile_description_3", "Test Description 3");
        request.setParameter("new_profile_header_3", "Test Header 3");
        request.setParameter("new_profile_weight_3", "300");
        
        String profileId = speciesA.getInfoItems().get(0).getId().toString();
        request.addParameter("profile_pk", profileId);
        request.setParameter("profile_type_"+profileId, "Edited Test Type "+profileId);
        request.setParameter("profile_content_"+profileId, "Edited Test Content "+profileId);
        request.setParameter("profile_description_"+profileId, "Edited Test Description "+profileId);
        request.setParameter("profile_header_"+profileId, "Edited Test Header "+profileId);
        request.setParameter("profile_weight_"+profileId, "1400");
        
        String key;
        String value;
        for (Attribute attr : taxonGroupFrogs.getAttributes()) {
            key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
            value = new String();
            switch (attr.getType()) {
                case INTEGER:
                    value = "123";
                    break;
                case DECIMAL:
                    value = "456.7";
                    break;
                case DATE:
                    value = dateFormat.format(today);
                    break;
                case STRING_AUTOCOMPLETE:
                case STRING:
                    value = "Test Species Attr String";
                    break;
                case TEXT:
                    value = "Test Species Attr Text";
                    break;
                case STRING_WITH_VALID_VALUES:
                    value = attr.getOptions().iterator().next().getValue();
                    break;
                case FILE:
                    String file_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockFileFile = new MockMultipartFile(key, file_filename, "audio/mpeg", file_filename.getBytes());
                    ((MockMultipartHttpServletRequest)request).addFile(mockFileFile);
                    value = file_filename;
                    break;
                case IMAGE:
                    String image_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockImageFile = new MockMultipartFile(key, image_filename, "image/png", image_filename.getBytes());
                    ((MockMultipartHttpServletRequest)request).addFile(mockImageFile);
                    value = image_filename;
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "+attr.getType().toString(), false);
                    break;
            }
            request.setParameter(key, value);
        }
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/taxonomy/listing.htm", redirect.getUrl());
        
        IndicatorSpecies taxon = taxaDAO.getIndicatorSpecies(speciesA.getId());

        Assert.assertEquals(request.getParameter("scientificName"), taxon.getScientificName());
        Assert.assertEquals(request.getParameter("commonName"), taxon.getCommonName());
        Assert.assertEquals(request.getParameter("taxonRank"), taxon.getTaxonRank().toString());
        if(withParent) {
            Assert.assertEquals(request.getParameter("parentPk"), taxon.getParent().getId().toString());
        } else {
            Assert.assertEquals(null, taxon.getParent());
        }
        Assert.assertEquals(request.getParameter("taxonGroupPk"), taxon.getTaxonGroup().getId().toString());
        Assert.assertEquals(request.getParameter("author"), taxon.getAuthor());
        Assert.assertEquals(request.getParameter("year"), taxon.getYear());
        
        Assert.assertEquals(taxon.getInfoItems().size(), 2);
        String index;
        for(SpeciesProfile profile : taxon.getInfoItems()) {
            if(profile.getType().startsWith("Edited")) {
                index = profileId;
                Assert.assertEquals(request.getParameter(String.format("profile_type_%s", index)), profile.getType());
                Assert.assertEquals(request.getParameter(String.format("profile_description_%s", index)), profile.getDescription());
                Assert.assertEquals(request.getParameter(String.format("profile_header_%s", index)), profile.getHeader());
                Assert.assertEquals(request.getParameter(String.format("profile_content_%s", index)), profile.getContent());
                Assert.assertEquals(request.getParameter(String.format("profile_weight_%s", index)), String.valueOf(profile.getWeight()));
            } else {
                String[] split = profile.getType().split(" ");
                index = split[split.length - 1];
                Assert.assertEquals("3", index);
                Assert.assertEquals(request.getParameter(String.format("new_profile_type_%s", index)), profile.getType());
                Assert.assertEquals(request.getParameter(String.format("new_profile_description_%s", index)), profile.getDescription());
                Assert.assertEquals(request.getParameter(String.format("new_profile_header_%s", index)), profile.getHeader());
                Assert.assertEquals(request.getParameter(String.format("new_profile_content_%s", index)), profile.getContent());
                Assert.assertEquals(request.getParameter(String.format("new_profile_weight_%s", index)), String.valueOf(profile.getWeight()));
            }
        }
        
        for(AttributeValue taxonAttr: taxon.getAttributes()) {
            key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", taxonAttr.getAttribute().getId());
            switch (taxonAttr.getAttribute().getType()) {
                case INTEGER:
                    Assert.assertEquals(Integer.parseInt(request.getParameter(key)), taxonAttr.getNumericValue().intValue());
                    break;
                case DECIMAL:
                    Assert.assertEquals(Double.parseDouble(request.getParameter(key)), taxonAttr.getNumericValue().doubleValue());
                    break;
                case DATE:
                    Assert.assertEquals(today, taxonAttr.getDateValue());
                    break;
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                    Assert.assertEquals(request.getParameter(key), taxonAttr.getStringValue());
                    break;
                case STRING_WITH_VALID_VALUES:
                    Assert.assertEquals(request.getParameter(key), taxonAttr.getStringValue());
                    break;
                case FILE:
                case IMAGE:
                    Assert.assertEquals(request.getParameter(key), taxonAttr.getStringValue());
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "+taxonAttr.getAttribute().getType().toString(), false);
                    break;
            }
        }
    }
    
    private void testAddTaxon(boolean withParent) throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        DateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");
        dateFormat.setLenient(false);
        Date today = dateFormat.parse(dateFormat.format(new Date(System.currentTimeMillis())));

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/taxonomy/edit.htm");
        
        request.setParameter("scientificName", "Test Scientific Name");
        request.setParameter("commonName", "Test Common Name");
        request.setParameter("taxonRank", TaxonRank.SUBSPECIES.toString());
        if(withParent) {
            request.setParameter("parentPk", speciesA.getId().toString());
        } else {
            request.setParameter("parentPk", new String());
        }
        request.setParameter("taxonGroupPk", taxonGroupButterflies.getId().toString());
        request.setParameter("author", "Anna Abigail");
        request.setParameter("year", "2011");
        
        request.addParameter("new_profile", "2");
        request.setParameter("new_profile_type_2", "Test Type 2");
        request.setParameter("new_profile_content_2", "Test Content 2");
        request.setParameter("new_profile_description_2", "Test Description 2");
        request.setParameter("new_profile_header_2", "Test Header 2");
        request.setParameter("new_profile_weight_2", "200");
        
        request.addParameter("new_profile", "3");
        request.setParameter("new_profile_type_3", "Test Type 3");
        request.setParameter("new_profile_content_3", "Test Content 3");
        request.setParameter("new_profile_description_3", "Test Description 3");
        request.setParameter("new_profile_header_3", "Test Header 3");
        request.setParameter("new_profile_weight_3", "300");
        
        String key;
        String value;
        for (Attribute attr : taxonGroupButterflies.getAttributes()) {
            key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", attr.getId());
            value = new String();
            switch (attr.getType()) {
                case INTEGER:
                    value = "123";
                    break;
                case DECIMAL:
                    value = "456.7";
                    break;
                case DATE:
                    value = dateFormat.format(today);
                    break;
                case STRING_AUTOCOMPLETE:
                case STRING:
                    value = "Test Species Attr String";
                    break;
                case TEXT:
                    value = "Test Species Attr Text";
                    break;
                case STRING_WITH_VALID_VALUES:
                    value = attr.getOptions().iterator().next().getValue();
                    break;
                case FILE:
                    String file_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockFileFile = new MockMultipartFile(key, file_filename, "audio/mpeg", file_filename.getBytes());
                    ((MockMultipartHttpServletRequest)request).addFile(mockFileFile);
                    value = file_filename;
                    break;
                case IMAGE:
                    String image_filename = String.format("attribute_%d", attr.getId());
                    MockMultipartFile mockImageFile = new MockMultipartFile(key, image_filename, "image/png", image_filename.getBytes());
                    ((MockMultipartHttpServletRequest)request).addFile(mockImageFile);
                    value = image_filename;
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "+attr.getType().toString(), false);
                    break;
            }
            request.setParameter(key, value);
        }
        
        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/taxonomy/listing.htm", redirect.getUrl());
        
        IndicatorSpecies taxon = taxaDAO.getIndicatorSpeciesByScientificName(sessionFactory.getCurrentSession(),
                                                                             request.getParameter("scientificName"));

        Assert.assertEquals(request.getParameter("scientificName"), taxon.getScientificName());
        Assert.assertEquals(request.getParameter("commonName"), taxon.getCommonName());
        Assert.assertEquals(request.getParameter("taxonRank"), taxon.getTaxonRank().toString());
        if(withParent) {
            Assert.assertEquals(request.getParameter("parentPk"), taxon.getParent().getId().toString());
        } else {
            Assert.assertEquals(null, taxon.getParent());
        }
        Assert.assertEquals(request.getParameter("taxonGroupPk"), taxon.getTaxonGroup().getId().toString());
        Assert.assertEquals(request.getParameter("author"), taxon.getAuthor());
        Assert.assertEquals(request.getParameter("year"), taxon.getYear());
        
        for(SpeciesProfile profile : taxon.getInfoItems()) {
            String[] split = profile.getType().split(" ");
            String index = split[split.length -1];
            
            Assert.assertEquals(request.getParameter(String.format("new_profile_type_%s", index)), profile.getType());
            Assert.assertEquals(request.getParameter(String.format("new_profile_description_%s", index)), profile.getDescription());
            Assert.assertEquals(request.getParameter(String.format("new_profile_header_%s", index)), profile.getHeader());
            Assert.assertEquals(request.getParameter(String.format("new_profile_content_%s", index)), profile.getContent());
            Assert.assertEquals(request.getParameter(String.format("new_profile_weight_%s", index)), String.valueOf(profile.getWeight()));
        }
        
        for(AttributeValue taxonAttr: taxon.getAttributes()) {
            key = String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, "", taxonAttr.getAttribute().getId());
            switch (taxonAttr.getAttribute().getType()) {
                case INTEGER:
                    Assert.assertEquals(Integer.parseInt(request.getParameter(key)), taxonAttr.getNumericValue().intValue());
                    break;
                case DECIMAL:
                    Assert.assertEquals(Double.parseDouble(request.getParameter(key)), taxonAttr.getNumericValue().doubleValue());
                    break;
                case DATE:
                    Assert.assertEquals(today, taxonAttr.getDateValue());
                    break;
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                    Assert.assertEquals(request.getParameter(key), taxonAttr.getStringValue());
                    break;
                case STRING_WITH_VALID_VALUES:
                    Assert.assertEquals(request.getParameter(key), taxonAttr.getStringValue());
                    break;
                case FILE:
                case IMAGE:
                    Assert.assertEquals(request.getParameter(key), taxonAttr.getStringValue());
                    break;
                default:
                    Assert.assertTrue("Unknown Attribute Type: "+taxonAttr.getAttribute().getType().toString(), false);
                    break;
            }
        }
    }
    
    @Override
    protected MockHttpServletRequest createMockHttpServletRequest() {
        return new MockMultipartHttpServletRequest();
    }
}
