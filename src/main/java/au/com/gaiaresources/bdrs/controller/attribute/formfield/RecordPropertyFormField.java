package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import org.apache.commons.lang.NotImplementedException;

import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

/**
 * The <code>RecordPropertyFormField</code> is a representation of a
 * record property on the record form.
 */
public class RecordPropertyFormField extends AbstractRecordFormField {

    private IndicatorSpecies species;
    private Taxonomic taxonomic;
    private RecordProperty recordProperty;

    /**
     * Creates a new <code>RecordPropertyFormField</code> for a record property.
     * 
     * @param record
     *            the record to be updated
     * @param recordProperty
     *            the <code>RecordProperty</code> represented by this field
     * @param species
     *            the indicator species to be represented by this field, or null
     *            otherwise.
     * @param prefix
     *            the prefix to be prepended to input names.
     */
    public RecordPropertyFormField(Record record, RecordProperty recordProperty,
            IndicatorSpecies species, Taxonomic taxonomic, String prefix) {
        super(recordProperty.getSurvey(), record, prefix);
        this.recordProperty = recordProperty;
        this.taxonomic = taxonomic;
        this.species = species;
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
    public boolean isPropertyFormField() {
        return true;
    }

    public IndicatorSpecies getSpecies() {
        return species;
    }

    public void setSpecies(IndicatorSpecies species) {
        this.species = species;
    }
    
    public Taxonomic getTaxonomic() {
		return taxonomic;
	}

	public void setTaxonomic(Taxonomic taxonomic) {
		this.taxonomic = taxonomic;
	}

	/**
	 * Returns the fields <code>RecordPropertyType</code>
	 * @return the fields <code>RecordPropertyType</code>
	 */
	public String getPropertyName() {
        return this.recordProperty.getRecordPropertyType().getName();
    }

	/**
	 * Not implemented
	 */
    public void setPropertyName(String propertyName) {
        throw new NotImplementedException();
    }
    /**
     * Returns the fields description on the form or null when no description is available.
     * @return String description of the field on the form.
     */
    public String getDescription() {
    	return this.recordProperty.getDescription();
    }
    
    /**
     * Gets the required value from the <code>RecordProperty</code>
     * @return  Either true or false
     */
    public boolean isRequired() {
    	return this.recordProperty.isRequired();
    }
    
    /**
     * Gets the scope from the <code>RecordProperty</code>
     * @return  a String containing the scope
     */
    public AttributeScope getScope() {
    	return this.recordProperty.getScope();
    }
    
    /**
     * Gets the hidden value from the <code>RecordProperty</code>
     * @return  Either true or false
     */
    public boolean isHidden() {
    	return this.recordProperty.isHidden();
    }
    
    /**
    * @return the recordProperty
    */
    public RecordProperty getRecordProperty() {
        return recordProperty;
    }
    
    /*
     * (non-Javadoc)
     * @see au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractFormField#isModerationFormField()
     */
    @Override
    public boolean isModerationFormField() {
        return AttributeScope.isModerationScope(recordProperty.getScope());
    }
}
