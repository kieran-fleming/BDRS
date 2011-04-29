package au.com.gaiaresources.bdrs.controller.fieldguide;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.controller.test.TestDataCreator;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.security.Role;

public class BDRSFieldGuideControllerTest extends AbstractControllerTest {
    
    @Autowired
    private TaxaDAO taxaDAO;
    
    @Test
    public void testListGroups() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });
        
        createTestData();
        request.setMethod("GET");
        request.setRequestURI("/fieldguide/groups.htm");
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroups");
        List<TaxonGroup> taxonGroups = (List<TaxonGroup>)mv.getModel().get("taxonGroups");
        Assert.assertEquals(taxaDAO.getTaxonGroups().size(), taxonGroups.size());
        ModelAndViewAssert.assertViewName(mv, "fieldGuideGroupListing");
    }
    
    @Test
    public void testListTaxa() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        createTestData();
        
        TaxonGroup expected = taxaDAO.getTaxonGroups().get(0);
        
        request.setMethod("GET");
        request.setRequestURI("/fieldguide/taxa.htm");
        request.setParameter("groupId", expected.getId().toString());
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxonGroup");
        Assert.assertEquals(expected.getId(), ((TaxonGroup)mv.getModel().get("taxonGroup")).getId());
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxaPaginator");
        ModelAndViewAssert.assertViewName(mv, "fieldGuideTaxaListing");
    }
    
    @Test
    public void testViewTaxa() throws Exception {
        login("admin", "password", new String[] { Role.ADMIN });

        createTestData();
        
        IndicatorSpecies expected = taxaDAO.getIndicatorSpecies().get(0);
        
        request.setMethod("GET");
        request.setRequestURI("/fieldguide/taxon.htm");
        request.setParameter("id", expected.getId().toString());
        
        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertModelAttributeAvailable(mv, "taxon");
        Assert.assertEquals(expected.getId(), ((IndicatorSpecies)mv.getModel().get("taxon")).getId());
        ModelAndViewAssert.assertViewName(mv, "fieldGuideViewTaxon");
    }
    
    private void createTestData() throws Exception {
        ApplicationContext appContext = getRequestContext().getApplicationContext();
        TestDataCreator testDataCreator = new TestDataCreator(appContext);
        
        testDataCreator.createTaxonGroups(2, 0, true);
        testDataCreator.createTaxa(3, 0);
        testDataCreator.createTaxonProfile();
    }
}
