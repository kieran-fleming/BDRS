package au.com.gaiaresources.bdrs.controller.record;

import java.text.ParseException;
import java.util.HashMap;
import java.util.Map;

import au.com.gaiaresources.bdrs.deserialization.record.AttributeParser;
import au.com.gaiaresources.bdrs.model.attribute.Attributable;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

public class WebFormAttributeParser extends AttributeParser {
    
    public boolean validate(RecordFormValidator validator,
            Attribute attribute, Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) {
        return this.validate(validator, DEFAULT_PREFIX, attribute, parameterMap, fileMap);
    }
    
    public boolean validate(RecordFormValidator validator, String prefix,
            Attribute attribute, 
            Map<String, String[]> parameterMap, 
            Map<String, MultipartFile> fileMap) {
        
        String paramKey = getParamKey(prefix, attribute);
        String fileKey = getFileKey(prefix, attribute);
        return validate(validator, paramKey, fileKey, attribute, parameterMap, fileMap);
    }
    
    @Override
    protected void duckPunchTimeParameter(String prefix, Attribute attribute,
            Map<String, String[]> parameterMap) {

        if (attribute.getType() != AttributeType.TIME) {
            throw new IllegalArgumentException(
                    "The attribute passed in must be of type TIME but was: "
                            + attribute.getType());
        }
        String timeKey = getParamKey(prefix, attribute);
        if (parameterMap.containsKey(timeKey)) {
            // don't overwrite the existing key
            return;
        }

        String hourKey = String.format(AttributeParser.ATTRIBUTE_TIME_HOUR_TEMPLATE, prefix, attribute.getId());
        String minuteKey = String.format(AttributeParser.ATTRIBUTE_TIME_MINUTE_TEMPLATE, prefix, attribute.getId());

        String timeValue = formatTime(hourKey, minuteKey, parameterMap);

        parameterMap.put(timeKey, new String[] { timeValue });
    }

    @Override
    public String getTimeValue(String timeKey, String timeHourKey, String timeMinuteKey, Map<String, String[]> parameterMap) {
        
        if (timeHourKey == null) {
            throw new IllegalArgumentException("arg timeHourKey cannot be null");
        }
        if (timeMinuteKey == null) {
            throw new IllegalArgumentException(
                    "arg timeMinuteKey cannot be null");
        }
        if (parameterMap == null) {
            throw new IllegalArgumentException("arg parmeterMap cannot be null");
        }

        return formatTime(timeHourKey, timeMinuteKey, parameterMap);
    }

    private String formatTime(String timeHourKey, String timeMinuteKey,
            Map<String, String[]> parameterMap) {

        String hour = getParameter(parameterMap, timeHourKey);
        String min = getParameter(parameterMap, timeMinuteKey);

        // default time:
        hour = StringUtils.hasLength(hour) ? hour : "00";
        min = StringUtils.hasLength(min) ? min : "00";
        return hour + ":" + min;
    }
    
    /**
     * Constructs and populates a <code>TypedAttributeValue</code> for the specified
     * <code>Attributable</code> and <code>Attribute</code> using the parameters 
     * provided. 
     * 
     * @param attribute The <code>Attribute</code> instance that pertains
     * to the <code>TypedAttributeValue</code> to be returned.
     * @param attributable The Attributable that owns (or will own) the returned 
     * <code>TypedAttributeValue</code>
     * @param parameterMap a <code>Map</code> of all POST parameters.
     * @param fileMap a <code>Map</code> of all files posted by the browser.
     * @return the record attribute to be saved or deleted.
     * @throws ParseException thrown if the provided date value cannot be 
     * parsed.
     */
    public TypedAttributeValue parse(String prefix, Attribute attribute, Attributable<? extends TypedAttributeValue> attributable,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {
        
        Map<Attribute, TypedAttributeValue> typedAttrMap = new HashMap<Attribute, TypedAttributeValue>();
        for (TypedAttributeValue attr : attributable.getAttributes()) {
            typedAttrMap.put(attr.getAttribute(), attr);
        }

        TypedAttributeValue typedAttr;
        addOrUpdateAttribute = true;

        // Retrieve or instantiate the attribute
        if (typedAttrMap.containsKey(attribute)) {
            typedAttr = typedAttrMap.get(attribute);
        } else {
            typedAttr = attributable.createAttribute();
            typedAttr.setAttribute(attribute);
        }

        // the record attribute is updated by reference.
        parseAttributeValue(prefix, attribute, parameterMap, fileMap, typedAttr);
        
        if(!addOrUpdateAttribute) {
            typedAttrMap.remove(typedAttr.getAttribute());
        }
        
        return typedAttr;
    }  
    
    private void parseAttributeValue(String prefix, Attribute attribute,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap, TypedAttributeValue attributeValue)
            throws ParseException {
        
        if (attribute.getType() == AttributeType.TIME) {
            duckPunchTimeParameter(prefix, attribute, parameterMap);
        }
        
        String paramKey = getParamKey(prefix, attribute);
        String fileKey = getFileKey(prefix, attribute);

        parseAttributeValue(paramKey, fileKey, attribute, parameterMap, fileMap, attributeValue);
    }
    
    /**
     * Constructs and populates a <code>RecordAttribute</code> for the specified
     * <code>Record</code> and <code>Attribute</code> using the parameters 
     * provided. 
     * 
     * @param prefix A prefix to the name of the attribute in the parameter map.
     * @param attribute The <code>Attribute</code> instance that pertains
     * to the <code>RecordAttribute</code> to be returned.
     * @param record The record that owns (or will own) the returned 
     * <code>RecordAttribute</code>
     * @param parameterMap a <code>Map</code> of all POST parameters.
     * @param fileMap a <code>Map</code> of all files posted by the browser.
     * @return the record attribute to be saved or deleted.
     * @throws ParseException thrown if the provided date value cannot be 
     * parsed.
     */
    public AttributeValue parse(String prefix, Attribute attribute, Record record,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {

        Map<Attribute, AttributeValue> recAttrMap = new HashMap<Attribute, AttributeValue>();
        for (AttributeValue recAttr : record.getAttributes()) {
            recAttrMap.put(recAttr.getAttribute(), recAttr);
        }

        AttributeValue recAttr;
        addOrUpdateAttribute = true;

        // Retrieve or instantiate the attribute
        if (recAttrMap.containsKey(attribute)) {
            recAttr = recAttrMap.get(attribute);
        } else {
            recAttr = new AttributeValue();
            recAttr.setAttribute(attribute);
        }

        // the record attribute is updated by reference.
        parseAttributeValue(prefix, attribute, parameterMap, fileMap, recAttr);
        
        if(!addOrUpdateAttribute) {
            recAttrMap.remove(recAttr.getAttribute());
        }
        
        return recAttr;
    }
    
    public TypedAttributeValue parse(Attribute attribute, Attributable<? extends TypedAttributeValue> taxon,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {
        return this.parse(DEFAULT_PREFIX, attribute, taxon, parameterMap, fileMap);
    }

    /**
     * refactor: only used by web form sublcass
     * 
     * @param prefix
     * @param attribute
     * @return
     */
    public static String getParamKey(String prefix, Attribute attribute) {
        return String.format(AttributeParser.ATTRIBUTE_NAME_TEMPLATE, prefix, attribute.getId());
    }
    
    /**
     * refactor: only used by web form subclass
     * 
     * @param prefix
     * @param attribute
     * @return
     */
    public static String getFileKey(String prefix, Attribute attribute) {
        return String.format(AttributeParser.ATTRIBUTE_FILE_NAME_TEMPLATE, prefix, attribute.getId());
    }
}
