package au.com.gaiaresources.bdrs.model.taxa;

import java.util.List;

/**
 * Data Access Object for dealing with Attributes.
 * 
 * @author anthony
 * 
 */
public interface AttributeDAO {
    /**
     * Returns a list of all the values that have been set for a given attribute
     * 
     * @param attr
     * @return
     */
    public List<String> getAttributeValues(Attribute attr);

    /**
     * Returns a list of all the values that have been set for a given attribute
     * 
     * @param attributePK
     * @return
     */
    public List<String> getAttributeValues(int attributePK);

    /**
     * Returns a list of all the values that have been set for a given attribute
     * that match %q%
     * 
     * @param attr
     * @return
     */
    public List<String> getAttributeValues(Attribute attr, String q);

    /**
     * Returns a list of all the values that have been set for a given attribute
     * that match %q%
     * 
     * @param attributePK
     * @return
     */
    public List<String> getAttributeValues(int attributePK, String q);

    /**
     * Creates or updates the specified attribute.
     * 
     * @param attribute
     *            the attribute to be saved.
     * @return the saved attribute instance.
     */
    public Attribute save(Attribute attribute);
    
    /**
     * Creates or updates the specified attribute option.
     * @param attributeOption the option to be saved.
     * @return the saved attribute option.
     */
    public AttributeOption save(AttributeOption attributeOption);
    /**
     * Removes the specified attribute option from the persistent store.
     * @param option the option to be deleted.
     */
    public void delete(AttributeOption option);

    /**
     * Removes the specified attribute from the persistent store.
     * @param attr the attribute to be deleted.
     */
    public void delete(Attribute attr);
    
}
