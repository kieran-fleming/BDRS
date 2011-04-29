package au.com.gaiaresources.bdrs.db;

import java.util.Date;
import java.util.Map;

/**
 * Base interface for objects that can be saved to the database.
 * 
 * @author Tim Carpenter
 */
public interface Persistent {
    /**
     * Get the ID of this instance.
     * 
     * @return <code>Integer</code>.
     */
    Integer getId();

    Date getCreatedAt();

    Date getUpdatedAt();

    Integer getCreatedBy();

    Integer getUpdatedBy();

    /**
     * Sets the sorted weight of this object. Lower weights should be displayed
     * first.
     * @param weight the sorted weight of this object.
     */
    void setWeight(int weight);

    /**
     * Returns the sorted weight of this object. Objects with a lower weight
     * should be displayed first. By default all objects have a weight of 0.
     * 
     * @return the sorted weight of this object.
     */
    int getWeight();

}
