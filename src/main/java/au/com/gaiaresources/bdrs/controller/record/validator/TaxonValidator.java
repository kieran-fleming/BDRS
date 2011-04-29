package au.com.gaiaresources.bdrs.controller.record.validator;

import java.util.List;
import java.util.Map;

import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.TaxaDAO;

/**
 * Validates that the input exactly matches, including case,
 * the scientific name of a taxon.
 */
public class TaxonValidator extends AbstractValidator {

    private static final String TAXON_MESSAGE_KEY = "TaxonValidator.taxon";
    private static final String TAXON_MESSAGE = "Must exactly match a valid scientific name including case e.g Homo Sapien";

    private static final String TAXON_OR_BLANK_MESSAGE_KEY = "TaxonValidator.taxonOrBlank";
    private static final String TAXON_OR_BLANK_MESSAGE = "Must be a valid scientific name including case (e.g Homo sapien) or blank";

    private TaxaDAO taxaDAO;

    /**
    * Creates a new <code>TaxonValidator</code>.
    * 
    * @param propertyService used to access configurable messages displayed to the user.
    * @param required true if the input is mandatory, false otherwise.
    * @param blank true if the value can be an empty string, false otherwise.
    * @param taxaDAO used to access stored taxonomy.
    */
    public TaxonValidator(PropertyService propertyService, boolean required,
            boolean blank, TaxaDAO taxaDAO) {
        super(propertyService, required, blank);
        this.taxaDAO = taxaDAO;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validate(Map<String, String[]> parameterMap, String key, Map<String, String> errorMap) {

        boolean isValid = super.validate(parameterMap, key, errorMap);
        if (isValid) {
            String value = getSingleParameter(parameterMap, key);

            List<IndicatorSpecies> taxaList = taxaDAO.getIndicatorSpeciesListByScientificName(value);

            if (taxaList.isEmpty() || taxaList.size() > 1) {
                if (blank) {
                    errorMap.put(key, propertyService.getMessage(TAXON_OR_BLANK_MESSAGE_KEY, TAXON_OR_BLANK_MESSAGE));
                } else {
                    errorMap.put(key, propertyService.getMessage(TAXON_MESSAGE_KEY, TAXON_MESSAGE));
                }
            }
            // Otherwise it is valid
        }

        return isValid && !errorMap.containsKey(key);
    }
}
