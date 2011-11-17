package au.com.gaiaresources.bdrs.model.taxa.impl;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;
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
}
