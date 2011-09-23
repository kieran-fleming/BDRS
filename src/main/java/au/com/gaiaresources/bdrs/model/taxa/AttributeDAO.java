package au.com.gaiaresources.bdrs.model.taxa;

import java.util.List;

import org.hibernate.Session;

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
    
    /**
     * Removes the specified attribute from the persistent store.
     * 
     * @param sesh
     * @param attr
     */
    public void delete(Session sesh, Attribute attr);
    
    /**
     * Creates or updates the specified attribute.
     * 
     * @param sesh
     * @param attr
     */
    public Attribute save(Session sesh, Attribute attr);
    
    
    
    /**
     * Get all of the attribute values for an attribute
     * @param attr
     * @return
     */
    public <T extends TypedAttributeValue> List<T> getAttributeValueObjects(Attribute attr);

    /**
     * Get all of the attribute values for an attribute
     * 
     * @param sesh
     * @param attr
     * @return
     */
    public <T extends TypedAttributeValue> List<T> getAttributeValueObjects(Session sesh, Attribute attr);
    
    /**
     * Gets the attribute by primary key
     */
    public Attribute get(Integer pk);
    
    /**
     * Save an AttributeValue
     * @param av
     * @return
     */
    public <T extends TypedAttributeValue> T save(T av);
    
    /**
     * Save an AttributeValue
     * @param sesh
     * @param av
     * @return
     */
    public <T extends TypedAttributeValue> T save(Session sesh, T av);
    
    /**
     * Update an AttributeValue
     * @param av
     * @return
     */
    public <T extends TypedAttributeValue> T update(T av);
    
    /**
     * Delete an AttributeValue
     * @param av
     */
    public <T extends TypedAttributeValue> void delete(T av);
    
    /**
     * Delete an AttributeValue
     * @param sesh
     * @param av
     */
    public <T extends TypedAttributeValue> void delete(Session sesh, T av);
}
