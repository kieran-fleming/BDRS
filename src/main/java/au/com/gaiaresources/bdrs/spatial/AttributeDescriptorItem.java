package au.com.gaiaresources.bdrs.spatial;

import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;

public class AttributeDescriptorItem {        
    private String key;
    private Attribute attr;
    private String desc;
    private Survey survey;
    private CensusMethod censusMethod;
    
    public AttributeDescriptorItem(String key, Attribute a, String desc, Survey survey, CensusMethod cm) {
        this.key = key;
        this.attr = a;
        this.desc = desc;
        this.survey = survey;
        this.censusMethod = cm;
    }
    
    public String getKey() {
        return key;
    }
    
    public Attribute getAttribute() {
        return attr;
    }
    
    public String getDatabaseName() {
        if (attr != null) {
            return attr.getName();
        }
        return "N/A";
    }
    
    public String getDescription() {
        return desc;
    }
    
    public String getSurveyDescription() {
        if (survey == null) {
            return "N/A";
        }
        return survey.getName();
    }
    
    public String getCensusMethodDescription() {
        if (censusMethod == null) {
            return "N/A";
        }
        return censusMethod.getName();
    }
}
