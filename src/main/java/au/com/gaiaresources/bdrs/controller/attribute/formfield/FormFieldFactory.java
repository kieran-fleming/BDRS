package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import au.com.gaiaresources.bdrs.controller.record.AttributeParser;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpeciesAttribute;

/**
 * The <code>RecordFormFieldFactory</code> is the one-stop-shop for the creation
 * of all <code>RecordFormFields</code>.
 */
public class FormFieldFactory {

    /**
     * Creates a new {@link FormField} for the specified survey attribute.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param attribute the attribute represented by this field. 
     * @param recordAttribute the current value of this field or null
     * @param prefix the prefix to be prepended to input names.
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey, Record record,
            Attribute attribute, AttributeValue recordAttribute, String prefix) {
        return new RecordAttributeFormField(survey, record, attribute,
                recordAttribute, prefix);
    }

    /**
     * Creates a new {@link FormField} for the specified survey attribute.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param attribute the attribute represented by this field. 
     * @param recordAttribute the current value of this field or null
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey,
            Record record, Attribute attribute, AttributeValue recordAttribute) {
        return new RecordAttributeFormField(survey, record, attribute,
                recordAttribute, AttributeParser.DEFAULT_PREFIX);
    }

    /**
     * Creates a new {@link FormField} for the specified survey attribute.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param attribute the attribute represented by this field. 
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey,
            Record record, Attribute attribute) {
        return new RecordAttributeFormField(survey, record, attribute, null,
                AttributeParser.DEFAULT_PREFIX);
    }
    
    /**
     * Creates a new {@link FormField} for the specified survey attribute.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param attribute the attribute represented by this field.
     * @param prefix the prefix to be prepended to input names. 
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey, Record record,
            Attribute attribute, String prefix) {
        return new RecordAttributeFormField(survey, record, attribute, null, prefix);
    }

    /**
     * Creates a new {@link FormField} for a record property.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param propertyName the name of the bean property represented by this field.
     * @param species the indicator species to be represented by this field, or
     * null otherwise.
     * @param prefix the prefix to be prepended to input names.
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey,
            Record record, String propertyName, IndicatorSpecies species,
            String prefix) {
        return new RecordPropertyFormField(survey, record, propertyName,
                species, prefix);
    }

    /**
     * Creates a new {@link FormField} for a record property.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param propertyName the name of the bean property represented by this field.
     * @param species the indicator species to be represented by this field, or
     * null otherwise.
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey,
            Record record, String propertyName, IndicatorSpecies species) {
        return new RecordPropertyFormField(survey, record, propertyName,
                species, AttributeParser.DEFAULT_PREFIX);
    }

    /**
     * Creates a new {@link FormField} for a record property.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param propertyName the name of the bean property represented by this field.
     * @return a <code>RecordFormField</code>
     */
    public FormField createRecordFormField(Survey survey, Record record,
            String propertyName) {
        return new RecordPropertyFormField(survey, record, propertyName, null,
                AttributeParser.DEFAULT_PREFIX);
    }
    
    /**
     * Creates a new form field for the taxon.
     * @param attribute the attribute (tag) from the taxon group.
     * @param value the current value for this attribute
     * @return a <code>TaxonAttributeFormField</code>
     */
    public FormField createTaxonFormField(Attribute attribute, 
    										IndicatorSpeciesAttribute value) {
    	return new TaxonAttributeFormField(attribute, value, AttributeParser.DEFAULT_PREFIX);
    }
    
    /**
     * Create a new form field for the taxon.
     * @param attribute the attribute (tag) from the taxon group.
     * @return the <code>TaxonAttributeFormField</code>
     */
    public FormField createTaxonFormField(Attribute attribute) {
    	return new TaxonAttributeFormField(attribute, null, AttributeParser.DEFAULT_PREFIX);
    }
}
