package au.com.gaiaresources.bdrs.service.threshold.operatorhandler;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.service.threshold.ConditionOperatorHandler;
import au.com.gaiaresources.bdrs.service.threshold.SimpleOperatorHandler;
import au.com.gaiaresources.bdrs.model.threshold.Condition;

/**
* Checks if one string contains another string with case sensitivity. 
*/
public class ContainsHandler implements SimpleOperatorHandler {
    
    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(Session sesh,
            ConditionOperatorHandler conditionOperatorHandler, Object entity, Condition condition)
            throws IllegalArgumentException, IllegalAccessException,
            InvocationTargetException {
        
        String actualValue = condition.getPropertyForPath(entity).toString();
        String expectedValue = condition.stringValue();
        boolean result = match(actualValue, expectedValue);
        
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(Object objA, Object objB) {
        if(objA == null || objB == null) {
            return false;
        } else {
            return objA.toString().contains(objB.toString());
        }
    }
}
