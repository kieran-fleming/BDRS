package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;

/**
 * The <code>LocationAttributeFormField</code> is a representation of a
 * configurable field on the location editing form that stores its value in a
 * {@link AttributeValue}.
 */
public class LocationAttributeFormField extends AbstractFormField implements TypedAttributeValueFormField {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    private TypedAttributeValue locationAttribute;
    private Attribute attribute;

    public static final String LOCATION_PREFIX = "";
    
    /**
     * Creates a new <code>locationAttributeFormField</code> for the specified
     * survey attribute.
     * 
     * @param attribute
     *            the attribute represented by this field.
     * @param locationAttribute
     *            the current value of this field or null
     * @param prefix
     *            the prefix to be prepended to input names.
     */
    LocationAttributeFormField(Attribute attribute, 
            TypedAttributeValue locationAttribute, String prefix) {

        super(prefix);

        this.attribute = attribute;
        this.locationAttribute = locationAttribute;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight() {
        return this.attribute.getWeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAttributeFormField() {
        return true;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

    @Override
    public TypedAttributeValue getAttributeValue() {
        return this.locationAttribute;
    }

    @Override
    public void setAttributeValue(TypedAttributeValue attributeValue) {
        this.locationAttribute = attributeValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(FormField other) {
        return Integer.valueOf(this.getWeight()).compareTo(other.getWeight());
    }

    
    @Override
    public boolean isDisplayFormField() {
        return attribute != null && 
               (AttributeType.HTML.equals(attribute.getType()) || 
                AttributeType.HTML_COMMENT.equals(attribute.getType()) || 
                AttributeType.HTML_HORIZONTAL_RULE.equals(attribute.getType()));
    }
    
    @Override
    public boolean equals(Object obj) {
        return super.equals(obj);
    }
    
    @Override
    public int hashCode() {
        return super.hashCode();
    }
    
    
    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractFormField#isModerationFormField()
     */
    @Override
    public boolean isModerationFormField() {
        return AttributeScope.isModerationScope(attribute.getScope());
    }
}
