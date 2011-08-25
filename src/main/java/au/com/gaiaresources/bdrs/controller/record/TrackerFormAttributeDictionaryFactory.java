package au.com.gaiaresources.bdrs.controller.record;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import au.com.gaiaresources.bdrs.deserialization.record.AttributeDictionaryFactory;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.TaxonGroup;

public class TrackerFormAttributeDictionaryFactory implements
        AttributeDictionaryFactory {

    @Override
    public Map<Attribute, String> createFileKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod) {
        
        if (survey == null) {
            throw new IllegalArgumentException("survey cannot be null");
        }
        
        Map<Attribute, String> result = new HashMap<Attribute, String>();
        Set<String> check = new HashSet<String>();
        
        if (survey.getAttributes() != null) {
            for (Attribute attribute : survey.getAttributes()) {
                String paramKey = WebFormAttributeParser.getFileKey("", attribute);
                addKey(result, check, paramKey, attribute, "Survey");
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

    @Override
    public Map<Attribute, String> createNameKeyDictionary(Survey survey,
            TaxonGroup taxonGroup, CensusMethod censusMethod) {
        if (survey == null) {
            throw new IllegalArgumentException("survey cannot be null");
        }
        
        Map<Attribute, String> result = new HashMap<Attribute, String>();
        Set<String> check = new HashSet<String>();
        
        if (survey.getAttributes() != null) {
            for (Attribute attribute : survey.getAttributes()) {
                String paramKey = WebFormAttributeParser.getParamKey("", attribute);
                addKey(result, check, paramKey, attribute, "Survey");
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


    private void addKey(Map<Attribute, String> map, Set<String> existingKeys, String key, Attribute attribute, String attributeSource) {
        if (existingKeys.add(key)) {
            map.put(attribute, key);
        } else {
            throw new IllegalArgumentException(attributeSource + " key: " + key + " already exists.");
        }
    }

    /**
     * These methods are not supported in the tracker form input. At the moment it is impossible
     * to submit multiple records from different surveys and different census methods. If
     * this behaviour is required in the future these methods can be implemented. 
     */
    @Override
    public Map<Attribute, String> createFileKeyDictionary(List<Survey> survey,
            TaxonGroup taxonGroup, List<CensusMethod> censusMethod) {
        throw new IllegalStateException("not supported");
    }

    @Override
    public Map<Attribute, String> createNameKeyDictionary(List<Survey> survey,
            TaxonGroup taxonGroup, List<CensusMethod> censusMethod) {
        throw new IllegalStateException("not supported");
    }
}
