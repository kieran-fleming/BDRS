package au.com.gaiaresources.bdrs.deserialization.record;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import au.com.gaiaresources.bdrs.controller.record.RecordFormValidator;
import au.com.gaiaresources.bdrs.controller.record.ValidationType;
import au.com.gaiaresources.bdrs.model.attribute.Attributable;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

/**
 * The <code>AttributeParser</code> is the server companion to the
 * attributeRenderer tile that parses POST parameters delivered by the tile into
 * a RecordAttribute.
 */
public abstract class AttributeParser {
    
    public static final String DEFAULT_PREFIX = "";

    public static final String ATTRIBUTE_NAME_TEMPLATE = "%sattribute_%d";
    public static final String ATTRIBUTE_FILE_NAME_TEMPLATE = "%sattribute_file_%d";
    public static final String ATTRIBUTE_TIME_HOUR_TEMPLATE = "%sattribute_time_hour_%d";
    public static final String ATTRIBUTE_TIME_MINUTE_TEMPLATE = "%sattribute_time_minute_%d";
    
    private Logger log = Logger.getLogger(getClass());

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    protected boolean addOrUpdateAttribute;
    protected MultipartFile attrFile;

    public AttributeParser() {
    }
    
    /**
     * Indicates if the <code>RecordAttribute</code> returned by the last
     * invokation of {@link #parse(Attribute, Record, Map, Map)} should be 
     * saved. 
     *  
     * @return true if the <code>RecordAttribute</code> should be saved,
     * false otherwise.
     */
    public boolean isAddOrUpdateAttribute() {
        return addOrUpdateAttribute;
    }
    
    /**
     * @return the multipart file associated with this attribute or null if
     * one does not exist.
     */
    public MultipartFile getAttrFile() {
        return attrFile;
    }
    
    public boolean validate(RecordFormValidator validator, String paramKey, String fileKey,
            Attribute attribute, 
            Map<String, String[]> parameterMap, 
            Map<String, MultipartFile> fileMap) {
        
        ValidationType validationType;
        switch (attribute.getType()) {
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case STRING_WITH_VALID_VALUES:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_NONBLANK_STRING : ValidationType.STRING;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case MULTI_CHECKBOX:
        case MULTI_SELECT:
                validationType = attribute.isRequired() ? ValidationType.REQUIRED_NONBLANK_STRING : ValidationType.STRING;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case SINGLE_CHECKBOX:
                // No need to validate these values. They will always be valid no matter the input... even null.
                // (An unchecked checkbox does not get POSTed)
                return true;
        case IMAGE:
        case FILE:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_NONBLANK_STRING : ValidationType.STRING;
            MultipartFile file = fileMap.get(fileKey);
            if(parameterMap.containsKey(paramKey) && getParameter(parameterMap, paramKey).isEmpty() && file != null) {
                // This bit of trickyness is to work around an issue where
                // with javascript turned off, the attribute value is not
                // populated even though the file is present. We are simply
                // fake the javascript component here by manually inserting
                // the filename into the attribute input and attempt a validation.
                parameterMap = new HashMap<String, String[]>(parameterMap);
                parameterMap.put(paramKey, new String[]{file.getOriginalFilename()});
            }
            boolean isValid = validator.validate(parameterMap, validationType, paramKey, attribute);
            return isValid;
        case REGEX:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_REGEX : ValidationType.REGEX;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case BARCODE:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_BARCODE : ValidationType.BARCODE;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case INTEGER:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_INTEGER : ValidationType.INTEGER;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case INTEGER_WITH_RANGE:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_INTEGER_RANGE : ValidationType.INTEGER_RANGE;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case DECIMAL:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_DOUBLE : ValidationType.DOUBLE;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case DATE:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_DATE : ValidationType.DATE;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case TIME:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_TIME : ValidationType.TIME;
            return validator.validate(parameterMap, validationType, paramKey, attribute);
        case HTML:
        case HTML_COMMENT:
        case HTML_HORIZONTAL_RULE:
            return validator.validate(parameterMap, ValidationType.HTML, paramKey, attribute);
        default:
            log.warn("Unknown Attribute Type: " + attribute.getType());
            throw new IllegalArgumentException("Unknown Attribute Type: " + attribute.getType());
        }
    }
    
    public TypedAttributeValue parse(String paramKey, String fileKey, Attribute attribute, Attributable<? extends TypedAttributeValue> attributable,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {
        
        Map<Attribute, TypedAttributeValue> recAttrMap = new HashMap<Attribute, TypedAttributeValue>();
        for (TypedAttributeValue recAttr : attributable.getAttributes()) {
            recAttrMap.put(recAttr.getAttribute(), recAttr);
        }

        TypedAttributeValue recAttr;
        addOrUpdateAttribute = true;

        // Retrieve or instantiate the attribute
        if (recAttrMap.containsKey(attribute)) {
            recAttr = recAttrMap.get(attribute);
        } else {
            recAttr = new AttributeValue();
            recAttr.setAttribute(attribute);
        }
        
        // the record attribute is updated by reference.
        parseAttributeValue(paramKey, fileKey, attribute, parameterMap, fileMap, recAttr);
        
        if(!addOrUpdateAttribute) {
            recAttrMap.remove(recAttr.getAttribute());
        }
        
        return recAttr;
    }

    protected void parseAttributeValue(String paramKey, String fileKey, Attribute attribute,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap, TypedAttributeValue attributeValue)
            throws ParseException {
        AttributeType attrType = attribute.getType();
        if (attrType == AttributeType.TIME) {
            // parse out the time attribute if required...
            duckPunchTimeParameter("", attribute, parameterMap);
        }
        String attrValue = getParameter(parameterMap, paramKey);
        attrFile = fileMap.get(fileKey);
        
        if(AttributeType.MULTI_CHECKBOX.equals(attrType) || 
                        AttributeType.MULTI_SELECT.equals(attrType) || 
                        AttributeType.SINGLE_CHECKBOX.equals(attrType)) {
                
                // These types may have a null attrValue and still be valid.
                addOrUpdateAttribute = true;
                switch(attrType) {
                        case MULTI_CHECKBOX:
                                addOrUpdateAttribute = true;
                                attributeValue.setMultiCheckboxValue(parameterMap.get(paramKey));
                                break;
                        case MULTI_SELECT:
                                addOrUpdateAttribute = true;
                                attributeValue.setMultiSelectValue(parameterMap.get(paramKey));
                                break;
                        case SINGLE_CHECKBOX:
                                // Just clean up the input into "true" or "false"
                                attributeValue.setBooleanValue(Boolean.valueOf(attrValue).toString());
                                break;
                        default:
                                // Absolutely cannot get here.
                                log.warn("Unknown Attribute Type: " + attribute.getType());
                                break;
                }
        
        } else if (attrValue != null || attrFile != null) {
            addOrUpdateAttribute = true;
            attributeValue.setStringValue(attrValue);
            switch (attrType) {
                case TIME:
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                case BARCODE:
                case REGEX:
                case HTML:
                case HTML_COMMENT:
                case HTML_HORIZONTAL_RULE:
                    break;
                case STRING_WITH_VALID_VALUES:
                    addOrUpdateAttribute = !attrValue.isEmpty();
                    break;
                case INTEGER:
                case INTEGER_WITH_RANGE:
                case DECIMAL:
                    addOrUpdateAttribute = !attrValue.isEmpty();
                    if (addOrUpdateAttribute) {
                        attributeValue.setNumericValue(new BigDecimal(attrValue));
                    }
                    break;
                case DATE:
                    addOrUpdateAttribute = !attrValue.isEmpty();
                    if (addOrUpdateAttribute) {
                        attributeValue.setDateValue(dateFormat.parse(attrValue));
                    }
                    break;
                case IMAGE:
                case FILE:
                    
                    // attrValue is empty when a file is cleared or the client 
                    // does not have javascript enabled when uploading a file.
                    // Without javascript, it is not possible to clear a file.
                    
                    // attrFile will always have size zero unless a file
                    // is uploaded. 
                    
                    // If there is already a file, but the
                    // record is updated, without changing the file input,
                    // addAttribute will be true but attrFile will
                    // have size zero.
                    addOrUpdateAttribute = !attrValue.isEmpty() || (attrFile != null && attrFile.getSize() > 0);
                    if (addOrUpdateAttribute && attrFile != null && attrFile.getSize() > 0) {
                        attributeValue.setStringValue(attrFile.getOriginalFilename());
                    } else {
                        // Simplifies the need for users of this class to know about
                        // zero sized files.
                        attrFile = null;
                    }
                    break;
                default:
                    log.warn("Unknown Attribute Type: " + attribute.getType());
                    break;
            }
        } else {
            addOrUpdateAttribute = false;
        }
    }
    
    /**
     * 
     * 
     * @param prefix - prefix for the attribute key
     * @param attribute - attribute of type time
     * @param paramMap - the parameter map
     */
    protected void duckPunchTimeParameter(String prefix, Attribute attribute, Map<String, String[]> paramMap) {
        // by default do nothing
    }
    
    protected String getParameter(Map<String, String[]> parameterMap, String key) {
        String[] value = parameterMap.get(key);
        return value == null || value.length == 0 ? null : value[0];
    }
    
    /**
     * Hacky way to specify how to extract the non-attribute time field normally associated with a record
     * 
     * @param timeKey
     * @param timeHourKey
     * @param timeMinuteKey
     * @param parameterMap
     * @return
     */
    public abstract String getTimeValue(String timeKey, String timeHourKey, String timeMinuteKey, Map<String, String[]> parameterMap);
}
