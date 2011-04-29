package au.com.gaiaresources.bdrs.service.threshold;

import au.com.gaiaresources.bdrs.model.threshold.Operator;

/**
 * Represents an operation that is performed on a key/value pair.
 */
public interface ComplexTypeOperator extends ConditionOperator {

    /**
     * Returns the label that should be presented next to the input for the key.
     * 
     * @return the label that should be presented next to the input for the key.
     */
    public String getKeyLabel();

    /**
     * The datatype of the key. This dataype can be used in determining the
     * presentation widget to select the key.
     * 
     * @return datatype of the key.
     */
    public Class<?> getKeyClass();

    /**
     * The possible comparison operations that can be applied to the key.
     * 
     * @return possible comparison operations that can be applied to the key
     */
    public Operator[] getKeyOperators();

    /**
     * Returns the label that should be presented next to the input for the
     * value.
     * 
     * @return the label that should be presented next to the input for the
     *         value.
     */
    public String getValueLabel();

    /**
     * The datatype of the value. This dataype can be used in determining the
     * presentation widget to select the value.
     * 
     * @return the datatype of the value.
     */
    public Class<?> getValueClass();

    /**
     * The possible comparison operations that can be applied to the value.
     * 
     * @return the possible conmparison operations that can be applied to the
     *         value.
     */
    public Operator[] getValueOperators();
}
