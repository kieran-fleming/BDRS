package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.TypedAttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;

/**
 * The <code>TaxonAttributeFormField</code> is a representation of a
 * configurable field on the taxon editing form that stores its value in a
 * {@link IndicatorSpeciesAttribute}.
 */
public class TaxonAttributeFormField extends AbstractFormField implements TypedAttributeValueFormField {

    private Logger log = Logger.getLogger(getClass());

    private IndicatorSpeciesAttribute taxonAttribute;
    private Attribute attribute;

    /**
     * Creates a new <code>TaxonAttributeFormField</code> for the specified
     * survey attribute.
     * 
     * @param attribute
     *            the attribute represented by this field.
     * @param taxonAttribute
     *            the current value of this field or null
     * @param prefix
     *            the prefix to be prepended to input names.
     */
    TaxonAttributeFormField(Attribute attribute, 
    		IndicatorSpeciesAttribute taxonAttribute, String prefix) {

        super(prefix);

        this.attribute = attribute;
        this.taxonAttribute = taxonAttribute;
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

    public IndicatorSpeciesAttribute getTaxonAttribute() {
        return taxonAttribute;
    }

    public void setTaxonAttribute(IndicatorSpeciesAttribute taxonAttribute) {
        this.taxonAttribute = taxonAttribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

	@Override
	public TypedAttributeValue getAttributeValue() {
		return this.taxonAttribute;
	}

	@Override
	public void setAttributeValue(TypedAttributeValue attributeValue) {
		if(!(attributeValue instanceof IndicatorSpeciesAttribute)) {
			throw new IllegalArgumentException(String.format("Attribute Value %s is not an instance of IndicatorSpeciesAttribute", attributeValue));
		} 
		this.taxonAttribute = (IndicatorSpeciesAttribute) attributeValue;
	}

	/**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(FormField other) {
        return new Integer(this.getWeight()).compareTo(other.getWeight());
    }

}
