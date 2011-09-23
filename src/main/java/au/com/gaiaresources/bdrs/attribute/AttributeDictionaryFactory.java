package au.com.gaiaresources.bdrs.attribute;

import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

public interface AttributeDictionaryFactory {
    
    /**
     * Creates a unique string identifier for each attribute in the argument objects
     * 
     * @param survey
     * @param taxonGroup
     * @param censusMethod
     * @return
     */
    public Map<Attribute, String> createNameKeyDictionary(Survey survey, TaxonGroup taxonGroup, CensusMethod censusMethod, Set<AttributeScope> scope);
    
    
    public Map<Attribute, String> createNameKeyDictionary(List<Survey> survey, TaxonGroup taxonGroup, List<CensusMethod> censusMethod);
    
    /**
     * Creates a unique string identifier for each attribute in the argument objects
     * 
     * @param survey
     * @param taxonGroup
     * @param censusMethod
     * @return
     */
    public Map<Attribute, String> createFileKeyDictionary(Survey survey, TaxonGroup taxonGroup, CensusMethod censusMethod, Set<AttributeScope> scope);
    
    public Map<Attribute, String> createFileKeyDictionary(List<Survey> survey, TaxonGroup taxonGroup, List<CensusMethod> censusMethod);


    public Map<Attribute, String> createFileKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod);
    
    public Set<AttributeScope> getDictionaryAttributeScope();

    public Map<Attribute, String> createNameKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod);
}
