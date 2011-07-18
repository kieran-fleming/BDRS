package au.com.gaiaresources.bdrs.service.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

@Service
public class JsonService {
    
    public static final String JSON_KEY_ITEMS = "items";
    public static final String JSON_KEY_TYPE = "type";
    public static final String JSON_KEY_ATTRIBUTES = "attributes";
    public static final String JSON_KEY_ID = "id";
    
    public static final String JSON_ITEM_TYPE_RECORD = "record";
    public static final String JSON_ITEM_TYPE_MAP_FEATURE = "geoMapFeature";
    
    public static final String RECORD_KEY_CENSUS_METHOD = "census_method";
    public static final String RECORD_KEY_NUMBER = "number";
    public static final String RECORD_KEY_NOTES = "notes";
    public static final String RECORD_KEY_SPECIES = "species";
    public static final String RECORD_KEY_COMMON_NAME = "common_name";
    public static final String RECORD_KEY_HABITAT = "habitat";
    public static final String RECORD_KEY_WHEN = "when";
    public static final String RECORD_KEY_BEHAVIOUR = "behaviour";
    public static final String RECORD_KEY_RECORD_ID = "recordId";
    public static final String RECORD_KEY_SURVEY_ID = "surveyId";
    
    public static final String DATE_FORMAT = "dd-MMM-yyyy";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    
    public JSONObject toJson(Record record) {
        Map<String, Object> attrMap = new HashMap<String, Object>(13);

        if (record.getCensusMethod() != null) {
            attrMap.put(RECORD_KEY_CENSUS_METHOD, record.getCensusMethod().getName());
        } else {
            attrMap.put(RECORD_KEY_CENSUS_METHOD, "Standard Taxonomic");
        }
        if (record.getSpecies() != null) {
            attrMap.put(RECORD_KEY_SPECIES, record.getSpecies().getScientificName());
            attrMap.put(RECORD_KEY_COMMON_NAME, record.getSpecies().getCommonName());
        }
        attrMap.put(RECORD_KEY_NUMBER, record.getNumber());
        attrMap.put(RECORD_KEY_NOTES, record.getNotes());
        attrMap.put(RECORD_KEY_HABITAT, record.getHabitat());
        attrMap.put(RECORD_KEY_WHEN, record.getWhen().getTime());
        attrMap.put(RECORD_KEY_BEHAVIOUR, record.getBehaviour());
        attrMap.put(RECORD_KEY_RECORD_ID, record.getId());
        attrMap.put(RECORD_KEY_SURVEY_ID, record.getSurvey().getId());
        
        attrMap.put(JSON_KEY_ID, record.getId());
        attrMap.put(JSON_KEY_TYPE, JSON_ITEM_TYPE_RECORD);

        attrMap.put(JSON_KEY_ATTRIBUTES, getOrderedAttributes(record.getAttributes()));
        return JSONObject.fromObject(attrMap);
    }
    
    public JSONObject toJson(GeoMapFeature feature) {
        Map<String, Object> attrMap = new HashMap<String, Object>(3);
        attrMap.put(JSON_KEY_ID, feature.getId());
        attrMap.put(JSON_KEY_TYPE, JSON_ITEM_TYPE_MAP_FEATURE);
        attrMap.put(JSON_KEY_ATTRIBUTES, getOrderedAttributes(feature.getAttributes()));
        return JSONObject.fromObject(attrMap);
    }
    
    private JSONArray getOrderedAttributes(Set<AttributeValue> attributeValues) {
        JSONArray array = new JSONArray();
        for (AttributeValue av : attributeValues) {
            array.add(toJson(av));
        }
        return array;
    }
    
    private JSONObject toJson(AttributeValue av) {
        Attribute attr = av.getAttribute();
        JSONObject obj = new JSONObject();
        String key = StringUtils.hasLength(attr.getDescription()) ? attr.getDescription() : attr.getName();
        switch (attr.getType()) {
		    case INTEGER:
		    case INTEGER_WITH_RANGE:
		    case DECIMAL:
		        obj.accumulate(key, av.getNumericValue());
		        break;
		    case DATE:
		    	Date d = av.getDateValue();
		    	String format = d == null ? null : dateFormat.format(av.getDateValue()); 
		        obj.accumulate(key, format);
		        break;
		    case STRING:
		    case STRING_AUTOCOMPLETE:
		    case TEXT:
		    case STRING_WITH_VALID_VALUES:
		        obj.accumulate(key, av.getStringValue());
		        break;
		    case IMAGE:
		    case FILE:
		        // ignore
		        break;
		    default:
		        // ignore
        }
        return obj;
    }
}
