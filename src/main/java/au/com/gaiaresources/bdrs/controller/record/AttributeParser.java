package au.com.gaiaresources.bdrs.controller.record;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.springframework.web.multipart.MultipartFile;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;

/**
 * The <code>AttributeParser</code> is the server companion to the
 * attributeRenderer tile that parses POST parameters delivered by the tile into
 * a RecordAttribute.
 */
public class AttributeParser {
    
    public static final String DEFAULT_PREFIX = "";

    public static final String ATTRIBUTE_NAME_TEMPLATE = "%sattribute_%d";
    public static final String ATTRIBUTE_FILE_NAME_TEMPLATE = "%sattribute_file_%d";
    
    private Logger log = Logger.getLogger(getClass());

    private SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy");

    private boolean addOrUpdateAttribute;
    private MultipartFile attrFile;

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
    
    public boolean validate(RecordFormValidator validator,
            Attribute attribute, Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) {
        return this.validate(validator, DEFAULT_PREFIX, attribute, parameterMap, fileMap);
    }
    
    public boolean validate(RecordFormValidator validator, String prefix,
            Attribute attribute, 
            Map<String, String[]> parameterMap, 
            Map<String, MultipartFile> fileMap) {
        
        String key = String.format(ATTRIBUTE_NAME_TEMPLATE, prefix, attribute.getId());
        
        ValidationType validationType;
        switch (attribute.getType()) {
        case STRING:
        case STRING_AUTOCOMPLETE:
        case TEXT:
        case STRING_WITH_VALID_VALUES:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_NONBLANK_STRING : ValidationType.STRING;
            return validator.validate(parameterMap, validationType, key);
        case IMAGE:
        case FILE:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_NONBLANK_STRING : ValidationType.STRING;
            MultipartFile file = fileMap.get(String.format(ATTRIBUTE_FILE_NAME_TEMPLATE, prefix, attribute.getId()));
            if(parameterMap.containsKey(key) && getParameter(parameterMap, key).isEmpty() && file != null) {
                // This bit of trickyness is to work around an issue where
                // with javascript turned off, the attribute value is not
                // populated even though the file is present. We are simply
                // fake the javascript component here by manually inserting
                // the filename into the attribute input and attempt a validation.
                parameterMap = new HashMap<String, String[]>(parameterMap);
                parameterMap.put(key, new String[]{file.getOriginalFilename()});
            }
            boolean isValid = validator.validate(parameterMap, validationType, key);
            return isValid;
        case INTEGER:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_INTEGER : ValidationType.INTEGER;
            return validator.validate(parameterMap, validationType, key);
        case DECIMAL:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_DOUBLE : ValidationType.DOUBLE;
            return validator.validate(parameterMap, validationType, key);
        case DATE:
            validationType = attribute.isRequired() ? ValidationType.REQUIRED_DATE : ValidationType.DATE;
            return validator.validate(parameterMap, validationType, key);
        default:
            log.warn("Unknown Attribute Type: " + attribute.getType());
            throw new IllegalArgumentException("Unknown Attribute Type: " + attribute.getType());
        }
    }
    
    /**
     * Constructs and populates a <code>RecordAttribute</code> for the specified
     * <code>Record</code> and <code>Attribute</code> using the parameters 
     * provided. 
     * 
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
    public RecordAttribute parse(Attribute attribute, Record record,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {
        return this.parse(DEFAULT_PREFIX, attribute, record, parameterMap, fileMap);
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
    public RecordAttribute parse(String prefix, Attribute attribute, Record record,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {

    	Map<Attribute, RecordAttribute> recAttrMap = new HashMap<Attribute, RecordAttribute>();
        for (RecordAttribute recAttr : record.getAttributes()) {
            recAttrMap.put(recAttr.getAttribute(), recAttr);
        }

        RecordAttribute recAttr;
        addOrUpdateAttribute = true;

        // Retrieve or instantiate the attribute
        if (recAttrMap.containsKey(attribute)) {
            recAttr = recAttrMap.get(attribute);
        } else {
            recAttr = new RecordAttribute();
            recAttr.setAttribute(attribute);
        }

        // the record attribute is updated by reference.
        parseAttributeValue(prefix, attribute, parameterMap, fileMap, recAttr);
        
        if(!addOrUpdateAttribute) {
            recAttrMap.remove(recAttr.getAttribute());
        }
        
        return recAttr;
    }
    
    public IndicatorSpeciesAttribute parse(Attribute attribute, IndicatorSpecies taxon,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {
        return this.parse(DEFAULT_PREFIX, attribute, taxon, parameterMap, fileMap);
    }
    
    public IndicatorSpeciesAttribute parse(String prefix, Attribute attribute, IndicatorSpecies taxon,
            Map<String, String[]> parameterMap,
            Map<String, MultipartFile> fileMap) throws ParseException {

    	Map<Attribute, IndicatorSpeciesAttribute> taxonAttrMap = new HashMap<Attribute, IndicatorSpeciesAttribute>();
        for (IndicatorSpeciesAttribute taxonAttr : taxon.getAttributes()) {
            taxonAttrMap.put(taxonAttr.getAttribute(), taxonAttr);
        }

        IndicatorSpeciesAttribute taxonAttr;
        addOrUpdateAttribute = true;

        // Retrieve or instantiate the attribute
        if (taxonAttrMap.containsKey(attribute)) {
            taxonAttr = taxonAttrMap.get(attribute);
        } else {
            taxonAttr = new IndicatorSpeciesAttribute();
            taxonAttr.setAttribute(attribute);
        }

        // the record attribute is updated by reference.
        parseAttributeValue(prefix, attribute, parameterMap, fileMap, taxonAttr);
        
        if(!addOrUpdateAttribute) {
            taxonAttrMap.remove(taxonAttr.getAttribute());
        }
        
        return taxonAttr;
    }    

	private void parseAttributeValue(String prefix, Attribute attribute,
			Map<String, String[]> parameterMap,
			Map<String, MultipartFile> fileMap, AttributeValue attributeValue)
			throws ParseException {
		
		String attrValue;
		attrValue = getParameter(parameterMap, String.format(ATTRIBUTE_NAME_TEMPLATE, prefix, attribute.getId()));
        attrFile = fileMap.get(String.format(ATTRIBUTE_FILE_NAME_TEMPLATE, prefix, attribute.getId()));

        if (attrValue != null || attrFile != null) {
            attributeValue.setStringValue(attrValue);
            switch (attribute.getType()) {
            case STRING:
            case STRING_AUTOCOMPLETE:
            case TEXT:
                break;
            case STRING_WITH_VALID_VALUES:
                addOrUpdateAttribute = !attrValue.isEmpty();
                break;
            case INTEGER:
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
        }
	}
    
    private String getParameter(Map<String, String[]> parameterMap, String key) {
        String[] value = parameterMap.get(key);
        return value == null || value.length == 0 ? null : value[0];
    }
}