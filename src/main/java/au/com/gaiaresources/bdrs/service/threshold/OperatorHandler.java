package au.com.gaiaresources.bdrs.service.threshold;

import java.lang.reflect.InvocationTargetException;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.threshold.Condition;

/**
 * Compares the properties of an object against a {@link Condition}.
 */
public interface OperatorHandler {

    /**
     * Returns <code>true</code> if the <code>entity</code> matches the
     * specified condition.
     * 
     * @param sesh
     *            the session to use if database access is required.
     * @param conditionOperatorHandler
     *            if this handler requires further operators to be applied, the
     *            <code>conditionOperatorHandler</code> provides a way of using
     *            the appropriate handlers to perform he comparisons.
     * @param entity
     *            the entity to be tested against the <code>condition</code>
     * @param condition
     *            the condition to be met.
     * @return true if the <code>entity</code> matches the specified
     *         <code>condition</code>.
     * @throws IllegalArgumentException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws ClassNotFoundException
     */
    boolean match(Session sesh,
            ConditionOperatorHandler conditionOperatorHandler, Object entity,
            Condition condition) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            ClassNotFoundException;
}
