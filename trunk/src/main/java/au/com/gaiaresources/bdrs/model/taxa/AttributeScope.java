package au.com.gaiaresources.bdrs.model.taxa;

import au.com.gaiaresources.bdrs.model.record.Record;

/**
 * The <code>AttributeScope</code> defines the suggested extent where this
 * attribute is applicable. For example an attribute may have a scope of
 * {@link #SURVEY} indicating that the value for this attribute applies for all
 * records in a survey. Likewise a scope of {@link Record} indicates that
 * each record contains a different value for the attribute.
 */
public enum AttributeScope {
    SURVEY("Survey"),
    RECORD("Record");
    
    private String name;
    
    private AttributeScope(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
