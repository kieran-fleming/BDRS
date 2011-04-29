package au.com.gaiaresources.bdrs.service.threshold;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.threshold.Condition;

/**
 * The implementation of the <code>ConditionOperatorHandler</code> interface
 * provides a boolean result determining if a specified object matches a given
 * <code>Condition</code> or if a specified pair of objects matches a
 * <code>SimpleTypeOperator</code>.
 * 
 */
public interface ConditionOperatorHandler {

    /**
     * Returns <code>true</code> if the specified <code>entity</code> meets the
     * <code>condition</code> specified.
     * 
     * @param sesh
     *            the session to use for any database queries.
     * @param entity
     *            the entity to be interrogated.
     * @param condition
     *            the criteria to be met.
     * @return true if the entity fulfills the specified condition
     */
    boolean match(Session sesh, Object entity, Condition condition);

    /**
     * Return <code>true</code> if the pair of objects specified fulfilly the
     * operator specified by the <code>operator</code>.
     * 
     * @param operator
     *            the operation to be applied to the objects such as equals or
     *            contains.
     * @param objA
     *            the object to be tested.
     * @param objB
     *            the other object to be tested.
     * @return true if the objects pass the operation, false otherwise.
     */
    boolean match(SimpleTypeOperator operator, Object objA, Object objB);

}
