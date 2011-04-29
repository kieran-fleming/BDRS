package au.com.gaiaresources.bdrs.service.threshold;

import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.threshold.Operator;

/**
 * <p>
 * Represents the comparison of a key/value pair against an {@link Attribute}/
 * {@link RecordAttribute} pair.
 * </p>
 * 
 * <p>
 * The key must match the <code>name</code> property of the {@link Attribute}
 * and the value must match the value attribute of the {@link RecordAttribute}.
 * </p>
 */
public class RecordAttributeOperator implements ComplexTypeOperator {

    public static final Class<?> KEY_CLASS = String.class;
    public static final Class<?> VALUE_CLASS = String.class;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public String getKeyLabel() {
        return "Attribute Name";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Operator[] getKeyOperators() {
        return ThresholdService.SIMPLE_TYPE_TO_OPERATOR_MAP.get(KEY_CLASS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getKeyClass() {
        return KEY_CLASS;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getValueLabel() {
        return "Value";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Operator[] getValueOperators() {
        return ThresholdService.SIMPLE_TYPE_TO_OPERATOR_MAP.get(VALUE_CLASS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Class<?> getValueClass() {
        return VALUE_CLASS;
    }
}
