package au.com.gaiaresources.bdrs.json;

/**
 * A marker interface for JSON types (Array or Object). 
 */
public interface JSON {

    /**
     * True if the implementor of the interface represents a JSON array type, 
     * false otherwise.
     * @return true if the implementor of the interface is a JSON array type. 
     */
    public boolean isArray();
    
    /**
     * The number of items contained in the JSON type.
     * @return the number of items contained in the JSON type.
     */
    public int size();
}
