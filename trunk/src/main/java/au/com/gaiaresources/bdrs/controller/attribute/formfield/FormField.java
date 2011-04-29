package au.com.gaiaresources.bdrs.controller.attribute.formfield;

/**
 * The <code>FormField</code> binds an <code>Attribute</code> in an object that can be sorted by
 * weight for rendering by the view.
 */
public interface FormField extends Comparable<FormField> {

    /**
     * Returns the display weight of this form field. Lower weighted objects are
     * displayed first.
     * 
     * @return the display weight of this form field.
     */
    public int getWeight();

    /**
     * Returns true if this form field represents an <code>Attribute</code> from
     * a <code>Survey</code>, false otherwise.
     * 
     * @return true if this form field represents an Attribute
     * 
     */
    public boolean isAttributeFormField();

    /**
     * Returns true if this form field represents a bean property from a
     * <code>Record</code> false otherwise.
     * 
     * @return true if this form field represents a bean property from a
     *         <code>Record</code> false otherwise.
     */
    public boolean isPropertyFormField();
}
