package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import org.apache.log4j.Logger;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

/**
 * The <code>RecordAttributeFormField</code> is a representation of a
 * configurable field on the record form that stores its value in a
 * {@link RecordAttribute}.
 */
public class RecordAttributeFormField extends AbstractRecordFormField implements AttributeValueFormField {

    private Logger log = Logger.getLogger(getClass());

    private AttributeValue recordAttribute;
    private Attribute attribute;

    /**
     * Creates a new <code>RecordAttributeFormField</code> for the specified
     * survey attribute.
     * 
     * @param survey
     *            the survey containing the record
     * @param record
     *            the record to be updated
     * @param attribute
     *            the attribute represented by this field.
     * @param recordAttribute
     *            the current value of this field or null
     * @param prefix
     *            the prefix to be prepended to input names.
     */
    RecordAttributeFormField(Survey survey, Record record, Attribute attribute,
            AttributeValue recordAttribute, String prefix) {

        super(survey, record, prefix);

        this.attribute = attribute;
        this.recordAttribute = recordAttribute;
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

    public AttributeValue getRecordAttribute() {
        return recordAttribute;
    }

    public void setRecordAttribute(AttributeValue recordAttribute) {
        this.recordAttribute = recordAttribute;
    }

    public Attribute getAttribute() {
        return attribute;
    }

    public void setAttribute(Attribute attribute) {
        this.attribute = attribute;
    }

	@Override
	public AttributeValue getAttributeValue() {
		return this.recordAttribute;
	}

	@Override
	public void setAttributeValue(AttributeValue attributeValue) {
		if(!(attributeValue instanceof RecordAttribute)) {
			throw new IllegalArgumentException(String.format("Attribute Value %s is not an instance of RecordAttribute", attributeValue));
		} 
		this.recordAttribute = (AttributeValue) attributeValue;
	}
}
