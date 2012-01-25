package au.com.gaiaresources.bdrs.controller.webservice;

import java.util.HashMap;
import java.util.Map;

/**
 * Helper class for constructing a jqgrid compatible row
 */
public class JqGridDataRow {
    private Integer id;
    private Map<String, Object> values;
    
    /**
     * Construct a new JqGridDataRow
     * @param id - unqiue identifier for the row
     */
    public JqGridDataRow(Integer id) {
        this.values = new HashMap<String, Object>();
        this.id = id;
        this.addValue("id", id.toString());
    }
    
    /**
     * Add a key value pair for the row
     * 
     * @param key
     * @param value
     * @return
     */
    public JqGridDataRow addValue(String key, Object value) {
        values.put(key, value);
        return this;
    }
    
    /**
     * Add a collection of key value pairs
     * 
     * @param values
     * @return
     */
    public JqGridDataRow addValues(Map<String, Object> values) {
        this.values.putAll(values);
        return this;
    }
    
    /**
     * Gets the unique ID for this row.
     * 
     * @return id
     */
    public Integer getId() {
        return id;
    }
    /**
     * Sets the unique ID for this row
     * 
     * @param id id to set
     */
    public void setId(int id) {
        this.id = id;
    }

    /**
     * Get map containing all the values for this row.
     * 
     * @return Map
     */
    public Map<String, Object> getValueMap() {
        return this.values;
    }
}