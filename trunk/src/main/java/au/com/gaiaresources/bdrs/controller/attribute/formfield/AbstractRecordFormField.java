package au.com.gaiaresources.bdrs.controller.attribute.formfield;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;

/**
 * Basic implementation of the {@link FormField} providing accessors and
 * mutators for the <code>Survey</code>, <code>Record</code> and Prefix.
 */
public abstract class AbstractRecordFormField extends AbstractFormField {
    
    private Survey survey;
    private Record record;
    /**
     * Creates a new <code>AbstractRecordFormField</code>.
     * 
     * @param survey the survey containing the record
     * @param record the record to be updated
     * @param prefix the prefix to be prepended to input names
     */
    public AbstractRecordFormField(Survey survey, Record record, String prefix) {
    	super(prefix);
        this.survey = survey;
        this.record = record;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int compareTo(FormField other) {
        return new Integer(this.getWeight()).compareTo(other.getWeight());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return this.getClass().getName() + ": weight="+ getWeight();
    }

    public Survey getSurvey() {
        return survey;
    }

    public void setSurvey(Survey survey) {
        this.survey = survey;
    }

    public Record getRecord() {
        return record;
    }

    public void setRecord(Record record) {
        this.record = record;
    }
}
