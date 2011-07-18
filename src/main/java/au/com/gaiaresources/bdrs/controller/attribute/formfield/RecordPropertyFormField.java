package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import org.apache.commons.lang.NotImplementedException;

import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.method.Taxonomic;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;

/**
 * The <code>RecordPropertyFormField</code> is a representation of a
 * record property on the record form.
 */
public class RecordPropertyFormField extends AbstractRecordFormField {

    private IndicatorSpecies species;
    private Taxonomic taxonomic;
    private String propertyName;

    private int weight = 0;

    /**
     * Creates a new <code>RecordPropertyFormField</code> for a record property.
     * 
     * @param survey
     *            the survey containing the record
     * @param record
     *            the record to be updated
     * @param propertyName
     *            the name of the bean property represented by this field.
     * @param species
     *            the indicator species to be represented by this field, or null
     *            otherwise.
     * @param prefix
     *            the prefix to be prepended to input names.
     */
    RecordPropertyFormField(Survey survey, Record record, String propertyName,
            IndicatorSpecies species, Taxonomic taxonomic, String prefix) {

        super(survey, record, prefix);

        this.propertyName = propertyName;
        this.taxonomic = taxonomic;
        this.species = species;

        String mdkey = String.format(Metadata.RECORD_PROPERTY_FIELD_METADATA_KEY_TEMPLATE, this.propertyName);
        Metadata md = survey.getMetadataByKey(mdkey);
        if (md != null) {
            this.weight = Integer.parseInt(md.getValue());
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight() {
        return this.weight;
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

	public String getPropertyName() {
        return propertyName;
    }

    public void setPropertyName(String propertyName) {
        throw new NotImplementedException();
    }
}
