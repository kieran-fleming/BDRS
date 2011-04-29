package au.com.gaiaresources.bdrs.service.threshold.operatorhandler;

import java.lang.reflect.InvocationTargetException;

import org.apache.log4j.Logger;
import org.hibernate.Session;

import au.com.gaiaresources.bdrs.service.threshold.ConditionOperatorHandler;
import au.com.gaiaresources.bdrs.service.threshold.OperatorHandler;
import au.com.gaiaresources.bdrs.model.record.RecordAttribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.threshold.Condition;

/**
 * Checks that at least one {@link RecordAttribute} from an iterable of
 * {@link RecordAttribute} objects matches the {@link Condition}. This handler
 * is attribute type aware and will appropriately convert the attribute
 * value before comparison.
 */
public class RecordAttributeHandler implements OperatorHandler {

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Override
    public boolean match(Session sesh,
            ConditionOperatorHandler conditionOperatorHandler, Object entity,
            Condition condition) throws IllegalArgumentException,
            IllegalAccessException, InvocationTargetException,
            ClassNotFoundException {
        
        @SuppressWarnings("unchecked")
        Iterable<RecordAttribute> attributes = (Iterable<RecordAttribute>) condition.getPropertyForPath(entity);
        if (attributes == null) {
            return false;
        }
        
        String expectedKey = condition.getKey();
        boolean returnValue = false;
        for (AttributeValue recAttr : attributes) {
            String actualKey = recAttr.getAttribute().getName();
            boolean match = false;
            
            boolean isKeyMatch = conditionOperatorHandler.match(condition.getKeyOperator(), actualKey, expectedKey); 
            // If we have not previously found a match and 
            // this is an attribute that matches our key, check out the value.
            if (!returnValue && isKeyMatch) {
                switch (recAttr.getAttribute().getType()) {
                case STRING:
                case STRING_AUTOCOMPLETE:
                case TEXT:
                case STRING_WITH_VALID_VALUES:
                case IMAGE:
                case FILE:
                    match = conditionOperatorHandler.match(condition.getValueOperator(), recAttr.getStringValue(), condition.stringValue());
                    break;
                case INTEGER:
                    match = conditionOperatorHandler.match(condition.getValueOperator(), recAttr.getNumericValue().intValue(), condition.intValue());
                    break;
                case DECIMAL:
                    match = conditionOperatorHandler.match(condition.getValueOperator(), recAttr.getNumericValue().doubleValue(), condition.doubleValue());
                    break;
                case DATE:
                    match = conditionOperatorHandler.match(condition.getValueOperator(), recAttr.getDateValue(), condition.dateValue());
                    break;
                default:
                    log.warn(String.format("Unknown attribute type found %s Match is false.", recAttr.getAttribute().getType().toString()));
                    match = false;
                    break;
                }
            }
            returnValue = returnValue || match;
        }
        return returnValue;
    }
}
