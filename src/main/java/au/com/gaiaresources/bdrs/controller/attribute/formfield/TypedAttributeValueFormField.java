package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

public interface TypedAttributeValueFormField {
	
        /**
         * get/set the AttributeValue for this form field
         * @return
         */
	public TypedAttributeValue getAttributeValue();
	public void setAttributeValue(TypedAttributeValue attributeValue);
	
	/**
	 * gets/sets the attribute for this form field
	 * @return
	 */
	public Attribute getAttribute();
	public void setAttribute(Attribute attribute);
	
	/**
	 * gets/sets the prefix for the name of this form field. This prefix is normally
	 * used when generating the input name
	 * @return
	 */
	public String getPrefix();
	public void setPrefix(String prefix);
}
