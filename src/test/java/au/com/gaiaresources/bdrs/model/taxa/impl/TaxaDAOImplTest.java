package au.com.gaiaresources.bdrs.model.taxa.impl;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.test.AbstractTransactionalTest;

public class TaxaDAOImplTest extends AbstractTransactionalTest {

    @Autowired
    private TaxaDAO taxaDAO;
    
    @Test
    public void testHqlInjection() {
        // will error if search is not implemented properly
        taxaDAO.getIndicatorSpeciesByNameSearch("%Lewin's%Honeyeater%");
    }
    
    @Test
    public void testHqlInjectionSemiColon() {
        // will error if search is not implemented properly
        taxaDAO.getIndicatorSpeciesByNameSearch("%Lewin's%Honey; select from User");
    }
    
    @Test
    public void testGetBySourceDataId() {
        String testSourceId = "testsourceid12345";
        
        TaxonGroup group = new TaxonGroup();
        group.setName("my taxon group");
        taxaDAO.save(group);
        
        IndicatorSpecies species = new IndicatorSpecies();
        species.setScientificName("my name");
        species.setCommonName("species common name");
        species.setSourceId(testSourceId);
        species.setTaxonGroup(group);
        
        taxaDAO.save(species);
        
        IndicatorSpecies result = taxaDAO.getIndicatorSpeciesBySourceDataID(this.sesh, testSourceId);
        Assert.assertNotNull(result);
        Assert.assertEquals("source id mismatch", testSourceId, result.getSourceId());
    }
}
