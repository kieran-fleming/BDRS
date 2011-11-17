package au.com.gaiaresources.bdrs.service.web;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.model.map.GeoMapFeature;
import au.com.gaiaresources.bdrs.model.record.AccessControlledRecordAdapter;
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
    
    // first + last name of the recording user
    public static final String RECORD_KEY_USER = "owner";
    public static final String RECORD_KEY_USER_ID = "ownerId";
    
    public static final String DATE_FORMAT = "dd-MMM-yyyy";
    private SimpleDateFormat dateFormat = new SimpleDateFormat(DATE_FORMAT);
    
    private Logger log = Logger.getLogger(getClass());
    
    /**
     * 
     * @param record - the record to convert to json
     * @param hideDetails - whether or not we should hide the details of the record. In general, on a public map we will hide the details
     * @return
     */
    public JSONObject toJson(AccessControlledRecordAdapter record, String contextPath) {
        if (contextPath == null) {
            throw new IllegalArgumentException("String, contextPath, cannot be null");
        }
        if (record == null) {
            throw new IllegalArgumentException("AccessControlledRecordAdapter, record, cannot be null");
        }
        
        Map<String, Object> attrMap = new HashMap<String, Object>(16);
        
        addToAttributeMap(attrMap, RECORD_KEY_USER, record.getUser().getFirstName() + " " + record.getUser().getLastName());
        addToAttributeMap(attrMap, RECORD_KEY_USER_ID, record.getUser().getId());
        
        if (record.getCensusMethod() != null) {
            addToAttributeMap(attrMap, RECORD_KEY_CENSUS_METHOD, record.getCensusMethod().getName());
        } else {
            addToAttributeMap(attrMap, RECORD_KEY_CENSUS_METHOD, "Standard Taxonomic");
        }
        
        if (record.getSpecies() != null) {
            addToAttributeMap(attrMap, RECORD_KEY_SPECIES, record.getSpecies().getScientificName());
            addToAttributeMap(attrMap, RECORD_KEY_COMMON_NAME, record.getSpecies().getCommonName());
            addToAttributeMap(attrMap, RECORD_KEY_NUMBER, record.getNumber());
        }
        
        if (record.getNotes() != null) {
        	addToAttributeMap(attrMap, RECORD_KEY_NOTES, record.getNotes());
        }
        
        addToAttributeMap(attrMap, RECORD_KEY_HABITAT, record.getHabitat());
        addToAttributeMap(attrMap, JSON_KEY_ATTRIBUTES, getOrderedAttributes(record.getOrderedAttributes(), contextPath));
        addToAttributeMap(attrMap, RECORD_KEY_BEHAVIOUR, record.getBehaviour());   
        
        if(record.getWhen() != null) {
        	addToAttributeMap(attrMap, RECORD_KEY_WHEN, record.getWhen().getTime());
        }
        
        
        // legacy
        addToAttributeMap(attrMap, RECORD_KEY_RECORD_ID, record.getId());
        addToAttributeMap(attrMap, RECORD_KEY_SURVEY_ID, record.getSurvey().getId());
        
        // This is important, always include this stuff
        addToAttributeMap(attrMap, JSON_KEY_ID, record.getId());
        addToAttributeMap(attrMap, JSON_KEY_TYPE, JSON_ITEM_TYPE_RECORD);

        return JSONObject.fromObject(attrMap);
    }
    
    public JSONObject toJson(GeoMapFeature feature) {
        Map<String, Object> attrMap = new HashMap<String, Object>(3);
        attrMap.put(JSON_KEY_ID, feature.getId());
        attrMap.put(JSON_KEY_TYPE, JSON_ITEM_TYPE_MAP_FEATURE);
        // it's ok to use an empty context path here since GeoMapFeatures cannot have file attributes
        // which is the only type that requires the contextPath to create the download link
        attrMap.put(JSON_KEY_ATTRIBUTES, getOrderedAttributes(feature.getOrderedAttributes(), ""));
        return JSONObject.fromObject(attrMap);
    }
    
    private void addToAttributeMap(Map<String, Object> attrMap, String key, Object value) {
        if (attrMap.containsKey(key)) {
            log.warn("overwriting attribute map key : " + key);
        }
        if (value != null) {
            attrMap.put(key, value);
        }
    }
    
    private JSONArray getOrderedAttributes(List<AttributeValue> attributeValues, String contextPath) {
        JSONArray array = new JSONArray();
        for (AttributeValue av : attributeValues) {
            array.add(toJson(av, contextPath));
        }
        return array;
    }
    
    private JSONObject toJson(AttributeValue av, String contextPath) {
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
                    case HTML:
                    case HTML_COMMENT:
                    case HTML_HORIZONTAL_RULE:
		    case STRING:
		    case STRING_AUTOCOMPLETE:
		    case TEXT:
		    case STRING_WITH_VALID_VALUES:
		        obj.accumulate(key, av.getStringValue());
		        break;
		    // allow download of files and image attribute types
		    case IMAGE:
		    case FILE:
		        obj.accumulate(key, getAttributeValueFileDownloadLink(av, contextPath));
		        break;
		    default:
		        // ignore
        }
        return obj;
    }
    
    private String getAttributeValueFileDownloadLink(AttributeValue av, String contextPath) {
        StringBuilder sb = new StringBuilder();
        sb.append("<a href=\"");
        sb.append(contextPath);
        sb.append("/files/download.htm?className=au.com.gaiaresources.bdrs.model.taxa.AttributeValue&id=");
        sb.append(av.getId().toString());
        sb.append("&fileName=");
        sb.append(av.getStringValue());
        sb.append("\">Download file</a>");
        return sb.toString();
    }
}
