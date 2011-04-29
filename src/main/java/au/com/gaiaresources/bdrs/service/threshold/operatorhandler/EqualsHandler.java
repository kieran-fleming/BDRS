package au.com.gaiaresources.bdrs.service.threshold.operatorhandler;

import java.lang.reflect.InvocationTargetException;
import java.util.Date;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.service.threshold.ConditionOperatorHandler;
import au.com.gaiaresources.bdrs.service.threshold.SimpleOperatorHandler;
import au.com.gaiaresources.bdrs.model.threshold.Condition;

/**
 * Checks for the equality of two objects. The value stored by the 
 * {@link Condition} must be parsed into the appropriate datatype so only the
 * following datatypes are supported:
 * 
 * <ul>
 *      <li>{@link String}</li>
 *      <li>{@link Integer}</li>
 *      <li>{@link Long}</li>
 *      <li>{@link Double}</li>
 *      <li>{@link Float}</li>
 *      <li>{@link Boolean}</li>
 *      <li>{@link Date}</li>
 * </ul>
 */
public class EqualsHandler implements SimpleOperatorHandler {
    private Logger log = Logger.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(Session sesh,
            ConditionOperatorHandler conditionOperatorHandler, Object entity,
            Condition condition) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException {

        Object actualValue = condition.getPropertyForPath(entity);
        Class<?> returnType = actualValue.getClass();
        
        Object val;
        if(String.class.equals(returnType)) {
            val = condition.stringValue();
        } else if(Integer.class.equals(returnType)) {
            val = condition.intValue();
        } else if(Long.class.equals(returnType)) {
            val = condition.longValue();
        } else if(Double.class.equals(returnType)) {
            val = condition.doubleValue();
        } else if(Float.class.equals(returnType)) {
            val = condition.floatValue();
        } else if(Boolean.class.equals(returnType)) {
            val = condition.booleanValue();
        } else if(Date.class.equals(returnType)) {
            val = condition.dateValue();
        } else {
            val = condition.getValue();
        }
        
        boolean returnValue = match(actualValue, val);

        return returnValue;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(Object objA, Object objB) {

        if (objA == null || objB == null) {
            // Cannot equality match null objects.
            return false;
        } else if (!objA.getClass().equals(objB.getClass())) {
            // Both objects must be of the same class.
            return false;
        } else if ((String.class.equals(objA.getClass()))
                || (Integer.class.equals(objA.getClass()))
                || (Long.class.equals(objA.getClass()))
                || (Double.class.equals(objA.getClass()))
                || (Float.class.equals(objA.getClass()))
                || (Boolean.class.equals(objA.getClass()))
                || (Date.class.equals(objA.getClass()))) {
            return objA.equals(objB);
        } else {

            log.warn("Unsupported equality type: "
                    + objA.getClass().getCanonicalName() + " and "
                    + objB.getClass().getCanonicalName());
            return objA.equals(objB);
        }
    }
}
