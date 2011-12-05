package au.com.gaiaresources.bdrs.service.threshold.operatorhandler;

import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import edu.emory.mathcs.backport.java.util.Arrays;

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
        
        List<Object> properties = condition.getPropertiesForPath(entity);
        
        for (Object property : properties) {
            Object actualValue = property;
            Object expectedValue = null;
            if (property.getClass().isEnum()) {
                actualValue = property.toString();
                expectedValue = condition.stringArrayValue();
            } else {
                expectedValue = condition.stringValue();
            }
            if (match(actualValue, expectedValue)) {
                return true;
            }
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(Object objA, Object objB) {
        if(objA == null || objB == null) {
            return false;
        } else if(objA.getClass().isArray()) {
        	boolean isContained = true;
        	for(Object item : (Object[])objB) {
        		isContained = isContained && Arrays.binarySearch((Object[])objA, item) > -1;
        	}
        	return isContained;
        } else if(objB.getClass().isArray()) {
            boolean isContained = true;
            isContained = isContained && Arrays.binarySearch((Object[])objB, objA) > -1;
            return isContained;
        } else {
            return objA.toString().contains(objB.toString());
        }
    }
}
