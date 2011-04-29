package au.com.gaiaresources.bdrs.service.threshold;

/**
 * Provides a simple matching interface for two objects.  
 */
public interface SimpleOperatorHandler extends OperatorHandler {
    
    /**
     * Return true if the objects match, false otherwise.
     * 
     * @param objA 
     * @param objB
     * @return true if the the objects match, false otherwise.
     */
    public boolean match(Object objA, Object objB);

}
