package au.com.gaiaresources.bdrs.controller.admin.taxa;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;

import au.com.gaiaresources.bdrs.model.taxa.TaxaService;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;
import au.com.gaiaresources.bdrs.validation.Validator;

/**
 * Validator for <code>TaxonGroupForm</code> instances.
 * @author Tim Carpenter
 *
 */
@Component
public class TaxonGroupFormValidator extends Validator<TaxonGroupForm> {
    @Autowired
    private TaxaService taxaService;
    
    /**
     * Supports <code>TaxonGroupForm</code>.
     * @return <code>TaxonGroupForm.class</code>.
     */
    @Override
    protected Class<TaxonGroupForm> getSupportedClass() {
        return TaxonGroupForm.class;
    }

    /**
     * Check if there is an existing taxon group with the same name.
     * If there is and it does not have the same id as the received instance or the received
     * instance is new then and error is reported.
     * @param target <code>TaxonGroupForm</code>.
     * @param errors </code>Errors</code>.
     */
    @Override
    protected void internalValidate(TaxonGroupForm target, Errors errors) {
        TaxonGroup group = taxaService.getTaxonGroup(target.getName());
        if (group != null) {
            if (target.getId() == null || !target.getId().equals(group.getId())) {
                errors.rejectValue("name", "TaxonGroupForm.uniqueName", new Object[] {target.getName()}, "");
            }
        }
    }
}
