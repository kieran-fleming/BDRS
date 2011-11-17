package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordProperty;
import au.com.gaiaresources.bdrs.controller.attribute.formfield.RecordPropertySetting;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;

/**
 * The <code>RecordPropertyAttributeFormField</code> represents a
 * <code>Record</code> property field. Such fields are not user modifiable like
 * <code>Attribute</code> fields except for the ordering where they appear on
 * the <code>Record</code> adding form.
 */
public class RecordPropertyAttributeFormField extends
        AbstractAttributeFormField {

	private RecordProperty recordProperty;

    /**
     * Creates a new form field representing a <code>Record</code> property.
     * This will create and save a new Metadata instance if an instance for the
     * specified property does not already exist.
     * 
     * @param recordProperty
     *            the <code>RecordProperty</code> represented by this field
     */
    public RecordPropertyAttributeFormField(RecordProperty recordProperty) {
    	this.recordProperty = recordProperty;
    }

    /**
     * Updates the record property sorting weight metadata from the specified
     * POST parameter map.
     * 
	 * @param recordProperty
     *            the <code>RecordProperty</code> represented by this field
     * @param parameterMap
     *            the map of POST parameters that the form field will utilise to
     *            populate the <code>Metadata</code> that is created.
     */
    public RecordPropertyAttributeFormField(RecordProperty recordProperty,
            Map<String, String[]> parameterMap) {
        this(recordProperty);
    	HashMap<RecordPropertySetting, String> mdKeys = RecordProperty.getMetaDataKeys(this.recordProperty.getRecordPropertyType());
    	for (RecordPropertySetting setting : RecordPropertySetting.values()){
    		String key = mdKeys.get(setting);
    		String[] values = parameterMap.get(key);
			if (values != null && values.length > 0 && setting.equals(RecordPropertySetting.WEIGHT)) {
				this.recordProperty.setWeight(Integer.valueOf(values[0]));
    		} else if (values != null && values.length > 0 && setting.equals(RecordPropertySetting.DESCRIPTION)) {
    			this.recordProperty.setDescription(values[0]);
    		} else if (setting.equals(RecordPropertySetting.REQUIRED)) {
    			if (values != null && values.length > 0) {
					this.recordProperty.setRequired(Boolean.valueOf(values[0]));
				} else {
					this.recordProperty.setRequired(false);
				}
    		} else if (setting.equals(RecordPropertySetting.HIDDEN)) {
    			if (values != null && values.length > 0) {
					this.recordProperty.setHidden(Boolean.valueOf(values[0]));
				} else {
					this.recordProperty.setHidden(false);
				}
    		}
    	}
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight() {
    	return recordProperty.getWeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWeight(int weight) {
    	recordProperty.setWeight(weight);
    }
    
    /**
     * Gets the description from the <code>RecordProperty</code> or null when no description is available.
     * @return String description of the field on the form.
     */
    public String getDescription() {
    	return recordProperty.getDescription();
    }

    /**
     * Gets the required value from the <code>RecordProperty</code>
     * @return  Either true or false
     */
    public boolean isRequired() {
    	return recordProperty.isRequired();
    }
    
    /**
     * Gets the <code>AttributeScope</code> from the <code>RecordProperty</code>
     * @return  An <code>AttributeScope</code>
     */
    public AttributeScope getScope() {
    	return recordProperty.getScope();
    }

    /**
     * Gets the hidden value from the <code>RecordProperty</code>
     * @return  Either true or false
     */
    public boolean isHidden() {
    	return recordProperty.isHidden();
    }

    /**
     * Not implemented, functionality is already implemented in <code>RecordProperty</code>
     * @see RecordProperty
     */
    @Override
    public PersistentImpl save() {
    	throw new NotImplementedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPropertyField() {
        return true;
    }

    /**
     * Gets the property name of the Record that is represented by this field.
     * 
     * @return the property name of the Record that is represented by this
     *         field.
     */
    public String getPropertyName() {
    	return recordProperty.getRecordPropertyType().getName();       
    }

    /**
     * This method is not implemented. This method is only present to fulfill
     * the bean contract.
     * 
     * @param propertyName
     */
    public void setPropertyName(String propertyName) {
        throw new NotImplementedException();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getWeightName() {
    	return null;
    }
}
