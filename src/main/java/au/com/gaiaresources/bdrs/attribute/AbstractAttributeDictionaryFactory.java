/**
 * 
 */
package au.com.gaiaresources.bdrs.attribute;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.emory.mathcs.backport.java.util.Collections;

import au.com.gaiaresources.bdrs.controller.record.TrackerController;
import au.com.gaiaresources.bdrs.controller.record.WebFormAttributeParser;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

/**
 * Abstract class to handle creation of attribute maps.
 * @author stephanie
 */
public abstract class AbstractAttributeDictionaryFactory implements
        AttributeDictionaryFactory {
    
    protected static final Set<AttributeScope> SCOPE_RECORD_SURVEY;
    protected static final Set<AttributeScope> SCOPE_LOCATION;
    
    static {
        Set<AttributeScope> tmp = new HashSet<AttributeScope>(3);
        tmp.add(null);
        tmp.add(AttributeScope.RECORD);
        tmp.add(AttributeScope.SURVEY);
        tmp.add(AttributeScope.RECORD_MODERATION);
        tmp.add(AttributeScope.SURVEY_MODERATION);
        SCOPE_RECORD_SURVEY = Collections.unmodifiableSet(tmp);
        
        Set<AttributeScope> tmp2 = new HashSet<AttributeScope>(1);
        tmp2.add(AttributeScope.LOCATION);
        SCOPE_LOCATION = Collections.unmodifiableSet(tmp2);
    }
    
    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.deserialization.record.AttributeDictionaryFactory#createFileKeyDictionary(au.com.gaiaresources.bdrs.model.survey.Survey, au.com.gaiaresources.bdrs.model.taxa.TaxonGroup, au.com.gaiaresources.bdrs.model.method.CensusMethod)
     */
    @Override
    public Map<Attribute, String> createFileKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod, Set<AttributeScope> scope) {

        if (survey == null) {
            throw new IllegalArgumentException("survey cannot be null");
        }
        
        Map<Attribute, String> result = new HashMap<Attribute, String>();
        Set<String> check = new HashSet<String>();
        
        if (survey.getAttributes() != null) {
            for (Attribute attribute : survey.getAttributes()) {
                if(scope.contains(attribute.getScope())) {
                    String paramKey = WebFormAttributeParser.getFileKey(
                                      WebFormAttributeParser.DEFAULT_PREFIX, attribute);
                    addKey(result, check, paramKey, attribute, "Survey");
                }
            }
        }
        
        if (taxonGroup != null && taxonGroup.getAttributes() != null) {
            for (Attribute attribute : taxonGroup.getAttributes()) {
                String paramKey = WebFormAttributeParser.getFileKey(TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX, attribute);
                addKey(result, check, paramKey, attribute, "Taxon group");
            }
        }
        
        if (censusMethod != null && censusMethod.getAttributes() != null) {
            for (Attribute attribute : censusMethod.getAttributes()) {
                String paramKey = WebFormAttributeParser.getFileKey(TrackerController.CENSUS_METHOD_ATTRIBUTE_PREFIX, attribute);
                addKey(result, check, paramKey, attribute, "Census method");
            }
        }
        
        return result;
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.deserialization.record.AttributeDictionaryFactory#createFileKeyDictionary(java.util.List, au.com.gaiaresources.bdrs.model.taxa.TaxonGroup, java.util.List)
     */
    @Override
    public abstract Map<Attribute, String> createFileKeyDictionary(List<Survey> survey,
            TaxonGroup taxonGroup, List<CensusMethod> censusMethod);

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.deserialization.record.AttributeDictionaryFactory#createNameKeyDictionary(au.com.gaiaresources.bdrs.model.survey.Survey, au.com.gaiaresources.bdrs.model.taxa.TaxonGroup, au.com.gaiaresources.bdrs.model.method.CensusMethod)
     */
    @Override
    public Map<Attribute, String> createNameKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod, Set<AttributeScope> scope) {
        if (survey == null) {
            throw new IllegalArgumentException("survey cannot be null");
        }

        Map<Attribute, String> result = new HashMap<Attribute, String>();
        Set<String> check = new HashSet<String>();
        
        if (survey.getAttributes() != null) {
            for (Attribute attribute : survey.getAttributes()) {
                if(scope.contains(attribute.getScope())) {
                    String paramKey = WebFormAttributeParser.getParamKey("", attribute);
                    addKey(result, check, paramKey, attribute, "Survey");
                } 
            }
        }
        
        if (taxonGroup != null && taxonGroup.getAttributes() != null) {
            for (Attribute attribute : taxonGroup.getAttributes()) {
                String paramKey = WebFormAttributeParser.getParamKey(TrackerController.TAXON_GROUP_ATTRIBUTE_PREFIX, attribute);
                addKey(result, check, paramKey, attribute, "Taxon group");
            }
        }
        
        if (censusMethod != null && censusMethod.getAttributes() != null) {
            for (Attribute attribute : censusMethod.getAttributes()) {
                String paramKey = WebFormAttributeParser.getParamKey(TrackerController.CENSUS_METHOD_ATTRIBUTE_PREFIX, attribute);
                addKey(result, check, paramKey, attribute, "Census method");
            }
        }
        return result;
    }

    /* (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.deserialization.record.AttributeDictionaryFactory#createNameKeyDictionary(java.util.List, au.com.gaiaresources.bdrs.model.taxa.TaxonGroup, java.util.List)
     */
    @Override
    public abstract Map<Attribute, String> createNameKeyDictionary(List<Survey> survey,
            TaxonGroup taxonGroup, List<CensusMethod> censusMethod);

    /**
     * Adds the attribute/key pair to the map.
     * @param map
     * @param existingKeys
     * @param key
     * @param attribute
     * @param attributeSource
     */
    protected void addKey(Map<Attribute, String> map, Set<String> existingKeys, String key, Attribute attribute, String attributeSource) {
        if (key == null) {
            throw new NullPointerException("Error: key cannot be null!");
        }
        if (existingKeys.add(key)) {
            map.put(attribute, key);
        } else {
            throw new IllegalArgumentException(attributeSource + " key: " + key + " already exists.");
        }
    }
    
    @Override
    public abstract Set<AttributeScope> getDictionaryAttributeScope();
    

    @Override
    public Map<Attribute, String> createNameKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod) {
        return createNameKeyDictionary(survey, taxonGroup, censusMethod, getDictionaryAttributeScope());
    }
    
    public Map<Attribute, String> createFileKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod) {
        return createFileKeyDictionary(survey, taxonGroup, censusMethod, getDictionaryAttributeScope());
    }
}
