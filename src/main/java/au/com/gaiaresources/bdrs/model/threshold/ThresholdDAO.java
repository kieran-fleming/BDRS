package au.com.gaiaresources.bdrs.model.threshold;

import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.portal.Portal;

public interface ThresholdDAO {
    /**
     * Returns the requested threshold
     * 
     * @param id
     *            of the threshold that is requested.
     * @return the requested Threshold.
     */
    Threshold getThreshold(Integer id);

    /**
     * Returns the requested condition
     * 
     * @param id
     *            of the condition that is requested.
     * @return the requested Condition.
     */
    Condition getCondition(Integer id);

    /**
     * Returns the requested action
     * 
     * @param id
     *            of the action that is requested.
     * @return the requested Action.
     */
    Action getAction(Integer id);

    /**
     * Creates or updates the specified threshold instance.
     * 
     * @param t
     *            the instance to be persisted
     * @return the saved threshold instance.
     */
    Threshold save(Threshold t);

    /**
     * Creates or updates the specified condition instance.
     * 
     * @param condition
     *            the instance to be persisted
     * @return the saved condition instance.
     */
    Condition save(Condition condition);
    
    /**
     * Creates or updates the specified action instance.
     * 
     * @param action
     *            the instance to be persisted
     * @return the saved action instance.
     */
    Action save(Action action);

    /**
     * Creates or updates the specified threshold instance using the provided
     * session.
     * 
     * @param sesh
     *            the session to use when persistenting.
     * @param t
     *            the instance to be persisted
     * @return the saved threshold instance.
     */
    Threshold save(Session sesh, Threshold t);

    /**
     * Creates or updates the specified condition instance using the provided 
     * session.
     * 
     * @param sesh
     *            the session to use when persistenting.
     * @param condition
     *            the instance to be persisted
     * @return the saved condition instance.
     */
    public Condition save(Session sesh, Condition condition);
    
    /**
     * Creates or updates the specified action instance using the provided session.
     * 
     * @param sesh
     *            the session to use when persistenting.
     * @param action
     *            the instance to be persisted
     * @return the saved action instance.
     */
    public Action save(Session sesh, Action action);
    
    /**
     * Deletes the specified threshold from the database.
     * 
     * @param t
     *            the threshold to be removed.
     */
    void delete(Threshold t);

    /**
     * Deletes the specified condition from the database.
     * 
     * @param condition
     *            the condition to be removed.
     */
    void delete(Condition condition);
    
    /**
     * Deletes the specified action from the database.
     * 
     * @param action
     *            the action to be removed.
     */
    void delete(Action action);

    /**
     * Returns a list of all persisted thresholds.
     * 
     * @return a list of all persisted thresholds.
     */
    List<Threshold> all();
    
    /**
     * Returns all thresholds that are enabled and have the specified class name.
     * @param sesh the session that will be used to execute the query.
     * @return all thresholds that are enabled and have the specified class name.
     */
    List<Threshold> getEnabledThresholdByClassName(Session sesh, String className);
    
    /**
     * Returns all thresholds that are enabled and have the specified class name.
     * @return all thresholds that are enabled and have the specified class name.
     */
    List<Threshold> getEnabledThresholdByClassName(String className);

    /**
     * Gets a {@link List} of {@link Threshold} with the given name and portal.
     * @param name the name of the {@link Threshold} to find.
     * @param portal the portal of the {@link Threshold} to find.
     * @return The {@link List} of {@link Threshold} with the name or and empty {@link List} if no matches are found.
     */
    public List<Threshold> getThresholdsByName(String name, Portal portal);

    /**
     * Gets a {@link List} of {@link Threshold} with the given name and portal.
     * @param session the session that will be used to execute the query.
     * @param name the name of the {@link Threshold} to find.
     * @param portal the portal of the {@link Threshold} to find.
     * @return The {@link List} of {@link Threshold} with the name or and empty {@link List} if no matches are found.
     */
    public List<Threshold> getThresholdsByName(Session session, String name, Portal portal);
}
