package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

public interface AttributeValueFormField {
	
	public AttributeValue getAttributeValue();
	public void setAttributeValue(AttributeValue attributeValue);
	
	public Attribute getAttribute();
	public void setAttribute(Attribute attribute);
	
	public String getPrefix();
	public void setPrefix(String prefix);
}
