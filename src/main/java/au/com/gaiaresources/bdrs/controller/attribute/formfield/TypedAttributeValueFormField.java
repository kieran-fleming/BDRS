package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

public interface TypedAttributeValueFormField {
	
	public TypedAttributeValue getAttributeValue();
	public void setAttributeValue(TypedAttributeValue attributeValue);
	
	public Attribute getAttribute();
	public void setAttribute(Attribute attribute);
	
	public String getPrefix();
	public void setPrefix(String prefix);
}
