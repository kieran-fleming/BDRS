package au.com.gaiaresources.bdrs.service.threshold;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;

/**
 * Generic interface for any Action that may be performed when a threshold 
 * is met. 
 */
public interface ActionHandler {

    /**
     * Performs the specified action. 
     *  
     * @param sesh the session to use for any database access.
     * @param threshold the threshold that has been met.
     * @param entity the entity that has met the threshold.
     * @param action the action that this handler will perform.
     * @throws ClassNotFoundException 
     */
    public void executeAction(Session sesh, Threshold threshold, Object entity, Action action) throws ClassNotFoundException;

}
