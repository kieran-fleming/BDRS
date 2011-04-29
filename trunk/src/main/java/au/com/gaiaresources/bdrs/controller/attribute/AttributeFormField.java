package au.com.gaiaresources.bdrs.controller.attribute;

import au.com.gaiaresources.bdrs.db.Persistent;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;

/**
 * <p>
 * The <code>AttributeFormField</code> represents a field that is available when
 * adding or updating a <code>Record</code>. Typically these fields are either
 * an <code>Attribute</code> on the <code>Survey</code> or a property of the
 * <code>Record</code>.
 * </p>
 * 
 * <code>AttributeFormField</code>s must be sortable by the weight of the field
 * to provide ordering when rendering the add record form. Attributes are sorted
 * by the weight property of {@link Persistent}
 */
public interface AttributeFormField extends Comparable<AttributeFormField> {

    /**
     * Sets the sorted weight of the field on the persistent instance that will
     * store the value.
     * 
     * @param weight
     *            the sorted weight of the field.
     */
    public void setWeight(int weight);

    /**
     * Returns the sorted weight of the field.
     * 
     * @return the sorted weight of the field.
     */
    public int getWeight();

    /**
     * Saves the <code>Persistent</code> instance that stores the sorting order
     * of the field.
     * 
     * @return the instance that was saved.
     */
    public PersistentImpl save();

    /**
     * Returns true if the field encapsulates a <code>Record</code> property,
     * false otherwise.
     * 
     * @return true if the field encapsulates a <code>Record</code> property,
     *         false otherwise.
     */
    public boolean isPropertyField();

    /**
     * Returns true if the field encapsulates an <code>Attribute</code> on a 
     * <code>Survey</code>, false otherwise.
     * @return true if the field encapsulates an <code>Attribute</code> on a 
     * <code>Survey</code>, false otherwise.
     */
    public boolean isAttributeField();

    /**
     * The name of the input that records the sorting weight of the field.
     * @return the name of the input that records the sorting weight of the field.
     */
    public String getWeightName();

    /**
     * Not Implemented. This method is only present to fulfill the bean contract.
     * 
     * @param weightName the name of the input name.
     */
    public void setWeightName(String weightName);
}
