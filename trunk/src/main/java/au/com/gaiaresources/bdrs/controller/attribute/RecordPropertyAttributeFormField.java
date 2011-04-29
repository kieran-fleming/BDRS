package au.com.gaiaresources.bdrs.controller.attribute;

import java.util.Map;

import org.apache.commons.lang.NotImplementedException;

import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.model.metadata.Metadata;
import au.com.gaiaresources.bdrs.model.metadata.MetadataDAO;
import au.com.gaiaresources.bdrs.model.survey.Survey;

/**
 * The <code>RecordPropertyAttributeFormField</code> represents a
 * <code>Record</code> property field. Such fields are not user modifiable like
 * <code>Attribute</code> fields except for the ordering where they appear on
 * the <code>Record</code> adding form.
 */
public class RecordPropertyAttributeFormField extends
        AbstractAttributeFormField {

    private String propertyName;
    private Survey survey;
    private Metadata metadata;

    private MetadataDAO metadataDAO;

    private String weightName;

    /**
     * Creates a new form field representing a <code>Record</code> property.
     * This will create and save a new Metadata instance if an instance for the
     * specified property does not already exist.
     * 
     * @param metadataDAO
     *            the database object to use when saving metadata.
     * @param survey
     *            the survey where the metadata shall be added.
     * @param propertyName
     *            the name of the record property represented by this field
     */
    public RecordPropertyAttributeFormField(MetadataDAO metadataDAO,
            Survey survey, String propertyName) {
        this.survey = survey;
        this.propertyName = propertyName;
        this.metadataDAO = metadataDAO;

        String mdkey = String.format(Metadata.RECORD_PROPERTY_FIELD_METADATA_KEY_TEMPLATE, propertyName);
        Metadata md = survey.getMetadataByKey(mdkey);
        if (md == null) {
            md = new Metadata();
            md.setKey(mdkey);
            md.setValue(String.valueOf(PersistentImpl.DEFAULT_WEIGHT));

            md = metadataDAO.save(md);
            this.survey.getMetadata().add(md);
        }

        this.metadata = md;
        this.weightName = String.format("property_weight_%s", this.propertyName);
    }

    /**
     * Updates the record property sorting weight metadata from the specified
     * POST parameter map.
     * 
     * @param metadataDAO
     *            the database object to use when saving metadata.
     * @param survey
     *            the survey where the metadata shall be added.
     * @param propertyName
     *            the name of the record property represented by this field
     * @param parameterMap
     *            the map of POST parameters that the form field will utilise to
     *            populate the <code>Metadata</code> that is created.
     */
    public RecordPropertyAttributeFormField(MetadataDAO metadataDAO,
            Survey survey, String propertyName,
            Map<String, String[]> parameterMap) {
        this(metadataDAO, survey, propertyName);
        String weightStr = getParameter(parameterMap, this.weightName);
        if(weightStr == null) {
//            weightStr = String.valueOf(PersistentImpl.DEFAULT_WEIGHT);
            Thread.dumpStack();
        }
        this.metadata.setValue(weightStr);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getWeight() {
        return Integer.parseInt(metadata.getValue());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setWeight(int weight) {
        metadata.setValue(String.valueOf(weight));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PersistentImpl save() {
        return metadataDAO.save(this.metadata);
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
        return propertyName;
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
        return this.weightName;
    }
}
