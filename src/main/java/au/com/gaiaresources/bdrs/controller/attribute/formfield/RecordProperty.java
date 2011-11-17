package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import java.util.HashMap;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.annotation.CompactAttribute;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;

/**
 * Contains metadata keys for each <code>RecordPropertySetting</code> of each <code>RecordPropertyType</code>.
 * Gets the the values for each <code>RecordPropertySetting</code> for the <code>RecordPropertyType</code> that is contained in the <code>RecordProperty</code>.
 * Sets the default values for each  <code>RecordPropertySetting</code>.
 * @author timo
 */
public class RecordProperty extends PersistentImpl {
	
	private static final HashMap<RecordPropertyType, HashMap<RecordPropertySetting, String>> metadataKeys ;
	public static final String METADATA_KEY_TEMPLATE = "RECORD.%s.%s"; 
	private Survey survey;
	private RecordPropertyType recordPropertyType;
	private MetadataDAO metadataDAO;
	
    static {
    	// create the metadata keys
    	metadataKeys = new HashMap<RecordPropertyType, HashMap<RecordPropertySetting, String>>();
    	
    	for (RecordPropertyType type : RecordPropertyType.values()) {
    		HashMap<RecordPropertySetting, String> map = new HashMap<RecordPropertySetting,String>();
    		metadataKeys.put(type, map);
    		for (RecordPropertySetting setting : RecordPropertySetting.values()) {
    			metadataKeys.get(type).put(setting, String.format(METADATA_KEY_TEMPLATE, type.getName(), setting.toString()));
    		}
    	}
    }
    
    Logger log = Logger.getLogger(getClass());
	
	/**
	 * Stores the objects that are required to access the values for each <code>RecordPropertySetting</code> locally.
	 * @param survey that relates to the <code>RecordProperty</code>
	 * @param recordPropertyType for which we need to set and get the <code>RecordPropertySetting</code>s
	 * @param metadataDAO used to manipulate the <code>Metadata</code> for the <code>Survey</code> that is being stored in the <code>RecordProperty</code>. 
	 */
	public RecordProperty (Survey survey, RecordPropertyType recordPropertyType, MetadataDAO metadataDAO) {
		this.survey = survey;
		this.recordPropertyType = recordPropertyType;
		this.metadataDAO = metadataDAO;
	}
	
	@CompactAttribute
	public String getName() {
		return recordPropertyType.getName();
	}
	
	public RecordPropertyType getRecordPropertyType() {
		return recordPropertyType;
	}
	
	/**
	 * Gets the metadata value for the <code>RecordPropertySetting</code> hidden and casts it to a boolean.
	 * @return either true or false.
	 */
	public boolean isHidden() {
		return Boolean.valueOf(getMetadataValue(RecordPropertySetting.HIDDEN)).booleanValue();
	}
	
	/**
	 * Sets the metadata value for the <code>RecordPropertySetting</code> hidden. 
	 * @param hidden true when the <code>RecordPropertyFormField</code> needs to be hidden from the users view.
	 */
	public void setHidden( boolean hidden) {
		setMetadataValue(RecordPropertySetting.HIDDEN, String.valueOf(hidden));
	}
	
	/**
	 * Gets the metadata value for the <code>RecordPropertySetting</code> required and casts it to a boolean.
	 * @return either true or false.
	 */
	@CompactAttribute
	public boolean isRequired() {
		
		return Boolean.valueOf(getMetadataValue(RecordPropertySetting.REQUIRED)).booleanValue();
	}
	
	/**
	 * Sets the metadata value for the <code>RecordPropertySetting</code> required. 
	 * @param required true when the <code>RecordPropertyFormField</code> requires input from the user.
	 */
	public void setRequired(boolean required) {
		setMetadataValue(RecordPropertySetting.REQUIRED, String.valueOf(required));
	}
	
	/**
	 * Gets the metadata value for the <code>RecordPropertySetting</code> description.
	 * @return the description for this <code>RecordPropertType</code>
	 */
	@CompactAttribute
	public String getDescription() {
		 return getMetadataValue(RecordPropertySetting.DESCRIPTION);
	}
	
	/**
	 * Sets the metadata value for the <code>RecordPropertySetting</code> description.
	 * @param description for this <code>RecordPropertType</code>
	 */
	public void setDescription(String description) {
		setMetadataValue(RecordPropertySetting.DESCRIPTION, description);
	}
	
	/**
	 * Gets the metadata value for the <code>RecordPropertySetting</code> weight, which indicates the order in which the fields appear.
	 * @return the weight for this <code>RecordPropertType</code>
	 */
	@CompactAttribute
	public int getWeight() {
		 return Integer.valueOf(getMetadataValue(RecordPropertySetting.WEIGHT)).intValue();
	}
	
	/**
	 * Sets the metadata value for the <code>RecordPropertySetting</code> weight, which indicates the order in which the fields appear.
	 * @param weight for this <code>RecordPropertType</code>
	 */
	public void setWeight(int weight) {
		setMetadataValue(RecordPropertySetting.WEIGHT, String.valueOf(weight));
	}
	
	/**
	 * Gets the metadata value for the <code>RecordPropertySetting</code> scope and casts it to a <code>AttributeScope</code>.
	 * 
	 * In the survey form editor this is actually uneditable.
	 * 
	 * @return an <code>AttributeScope</code>
	 */
	@CompactAttribute
	public AttributeScope getScope() {
		return AttributeScope.valueOf(getMetadataValue(RecordPropertySetting.SCOPE));
	}
	
	/**
	 * Sets the metadata value for the <code>RecordPropertySetting</code> scope to a <code>AttributeScope</code> value
	 * @param the <code>AttributeScope</code> for this <code>RecordPropertType</code>
	 */
	public void setScope(AttributeScope scope) {
		setMetadataValue(RecordPropertySetting.SCOPE, scope.toString());
	}
	
	public Survey getSurvey() {
		return this.survey;
	}
	
	/**
	 * Gets the metadata keys for a particular <code>RecordPropertyType</code>.
	 * @param type the <code>RecordPropertyType</code> for which we want to retrieve the metadata keys.
	 * @return a map with metadata keys for each <code>RecordPropertySetting</code>
	 */
	public static HashMap<RecordPropertySetting, String> getMetaDataKeys(RecordPropertyType type) {
		return metadataKeys.get(type);
	}
	
	/**
	 * Gets the metadata value for a particular <code>RecordPropertySetting</code> for the <code>RecordPropertyType</code> contained in this <code>RecordProperty</code>.
	 * @param propertySetting the <code>RecordPropertySetting</code> for which we want the metadata value
	 * @return the <code>Metadata</code> value 
	 */
	public String getMetadataValue(RecordPropertySetting propertySetting) {
		HashMap<RecordPropertySetting, String> map = metadataKeys.get(this.recordPropertyType);
		String mdKey = map.get(propertySetting);
		Metadata md =  survey.getMetadataByKey(mdKey);
		if (md != null) {
			String value  = md.getValue();
			return value;
		} else {
			Metadata mdDefault = setMetadataValue(propertySetting, null);
			return mdDefault.getValue();
		}
	}
	
	/**
	 * Sets the value for the <code>RecordPropertySetting</code> to the value that was passed in or to the default value when nothing has been passed in.
	 * @param propertySetting the <code>RecordPropertySetting</code> for which we want to set the value.
	 * @param value 
	 * @return  a <code>Metadata</code> object that contains a default or custom value for the <code>RecordPropertySetting</code> for the <code>RecordPropertyType</code> contained in this object.
	 */
	private Metadata setMetadataValue(RecordPropertySetting propertySetting, String value) {
		
		if (value == null) {
		 if (propertySetting == RecordPropertySetting.WEIGHT) {
             	value = String.valueOf(PersistentImpl.DEFAULT_WEIGHT);
             } else if (propertySetting == RecordPropertySetting.DESCRIPTION) {
             	value = recordPropertyType.getDefaultDescription();
             } else if (propertySetting == RecordPropertySetting.REQUIRED) {
            	 if (this.recordPropertyType == RecordPropertyType.ACCURACY) {
            		 value = "false";
            	 } else {
            		 value = "true"; 
            	 }
             } else if (propertySetting == RecordPropertySetting.SCOPE) {
             	if (this.recordPropertyType == RecordPropertyType.NUMBER || this.recordPropertyType == RecordPropertyType.SPECIES) {
             		value = AttributeScope.RECORD.toString();
             	} else {
             		value = AttributeScope.SURVEY.toString();
             	}
             } else if (propertySetting == RecordPropertySetting.HIDDEN) {
             	value = "false";
             }
		}
		
		String mdKey = metadataKeys.get(this.recordPropertyType).get(propertySetting);
		Metadata md = this.survey.getMetadataByKey(mdKey);
		if (md == null) {
			md = new Metadata();
			md.setKey(mdKey);
		}
		
		md.setValue(value);
		md = metadataDAO.save(md);
        this.survey.getMetadata().add(md);
		return md;
	}

	
}
