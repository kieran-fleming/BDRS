package au.com.gaiaresources.bdrs.service.threshold;

import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.EmailActionHandler;
import au.com.gaiaresources.bdrs.service.threshold.actionhandler.HoldRecordHandler;
import au.com.gaiaresources.bdrs.service.threshold.operatorhandler.ContainsHandler;
import au.com.gaiaresources.bdrs.service.threshold.operatorhandler.EqualsHandler;
import au.com.gaiaresources.bdrs.service.threshold.operatorhandler.RecordAttributeHandler;
import au.com.gaiaresources.bdrs.db.impl.PersistentImpl;
import au.com.gaiaresources.bdrs.email.EmailService;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;
import au.com.gaiaresources.bdrs.model.taxa.IndicatorSpecies;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.ActionType;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Operator;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.user.User;

@SuppressWarnings("serial")
@Service
/**
 * <p>Thresholding is the establishment of certain criteria known as 
 * {@link Condition}s that, when matched, result in one to many {@link Action}s
 * being applied.</p>
 * 
 * <p>An example of a threshold is a record that when created created
 * for a survey with the name "Wiggle", and the record contains an attribute
 * with the key of "behaviour" whose value is "nesting", then hold the record
 * and email someone.</p>
 * 
 * <p>The <code>ThresholdService</code> is the stateful repository where 
 * threshold enabled classes are registered with action handlers and operators 
 * are mapped to datatypes.</p>
 */
public class ThresholdService implements ConditionOperatorHandler {
    /**
     * The list of classes where theresholding may be applied.
     */
    public static final Class<?>[] THRESHOLD_CLASSES = new Class<?>[] {
            Record.class, Survey.class, IndicatorSpecies.class, User.class };

    /**
     * A mapping of simple datatypes and the possible operations that may be
     * applied to perform a comparison. Simple datatypes are defined as those
     * that only involve a single value such as a string or a number.
     */
    public static Map<Class<?>, Operator[]> SIMPLE_TYPE_TO_OPERATOR_MAP = Collections.unmodifiableMap(new HashMap<Class<?>, Operator[]>() {
        {
            put(String.class, new Operator[] { Operator.EQUALS, Operator.CONTAINS });
            put(Integer.class, new Operator[] { Operator.EQUALS });
            put(Long.class, new Operator[] { Operator.EQUALS });
            put(Date.class, new Operator[] { Operator.EQUALS });
            put(Boolean.class, new Operator[] { Operator.EQUALS });
        }
    });

    /**
     * A mapping of complex datatypes and the possible operations that may be
     * applied to perform the comparison. Complex datatypes are defined as those
     * that involve more than one value such as a key/value pair and hence
     * require a more intricate comparison operation.
     */
    public static Map<Class<?>, ComplexTypeOperator> COMPLEX_TYPE_TO_OPERATOR_MAP = Collections.unmodifiableMap(new HashMap<Class<?>, ComplexTypeOperator>() {
        {
            put(AttributeValue.class, new RecordAttributeOperator());
        }
    });

    /**
     * A mapping of threshold enabled classes to the possible actions that may
     * be taken if the conditions are met. All classes in the
     * {@link #THRESHOLD_CLASSES} must be specified as keys in this map.
     */
    public static Map<Class<?>, ActionType[]> CLASS_TO_ACTION_MAP = Collections.unmodifiableMap(new HashMap<Class<?>, ActionType[]>() {
        {
            put(IndicatorSpecies.class, new ActionType[] { ActionType.EMAIL_NOTIFICATION });
            put(Record.class, new ActionType[] { ActionType.EMAIL_NOTIFICATION,
                    ActionType.HOLD_RECORD });
            put(User.class, new ActionType[] { ActionType.EMAIL_NOTIFICATION });
            put(Survey.class, new ActionType[] { ActionType.EMAIL_NOTIFICATION });
        }
    });

    private Logger log = Logger.getLogger(getClass());

    /**
     * A map of complex type operators to their comparison handler (object that
     * performs the actual comparison).
     */
    private Map<ComplexTypeOperator, OperatorHandler> operatorHandlerMap;
    /**
     * A map of simple type operators to their comparison handler.
     */
    private Map<SimpleTypeOperator, SimpleOperatorHandler> simpleOperatorHandlerMap;

    /**
     * A map of all possible action types and their handler which performs the
     * action.
     */
    private Map<ActionType, ActionHandler> actionHandlerMap;

    /**
     * A set of all instances that are currently being processed by the
     * thresholding framework. By recording all instances being processed,
     * actions that cause further hibernate events do not cause further actions
     * thereby preventing cascading or repeating sequences of actions. All items
     * in the set take the form of
     * <code>{canonical class name}.{primary key}</code>
     */
    private Set<String> referenceCountSet = new HashSet<String>();

    @Autowired
    private EmailService emailService;

    @Autowired
    private PropertyService propertyService;

    
    @PostConstruct
    public void init() {
        populateOperatorHandlers();
        populateActionHandlers();
    }

    private void populateActionHandlers() {
        actionHandlerMap = new HashMap<ActionType, ActionHandler>(
                ActionType.values().length);
        actionHandlerMap.put(ActionType.EMAIL_NOTIFICATION, new EmailActionHandler(
                emailService, propertyService));
        actionHandlerMap.put(ActionType.HOLD_RECORD, new HoldRecordHandler());
    }

    private void populateOperatorHandlers() {
        operatorHandlerMap = new HashMap<ComplexTypeOperator, OperatorHandler>(
                COMPLEX_TYPE_TO_OPERATOR_MAP.size());
        operatorHandlerMap.put(COMPLEX_TYPE_TO_OPERATOR_MAP.get(AttributeValue.class), new RecordAttributeHandler());

        simpleOperatorHandlerMap = new HashMap<SimpleTypeOperator, SimpleOperatorHandler>(
                Operator.values().length);
        simpleOperatorHandlerMap.put(Operator.CONTAINS, new ContainsHandler());
        simpleOperatorHandlerMap.put(Operator.EQUALS, new EqualsHandler());
    }

    /**
     * Applies the specified <code>action</code> contained by the
     * <code>threshold</code> to the <code>entity</code> using the given
     * <code>session</code>, if necessary.
     * 
     * @param sesh
     *            the session to use if database access is required.
     * @param threshold
     *            the threshold that contains the <code>action</code> and is
     *            currently being processed.
     * @param entity
     *            the entity to which the threshold and action applies.
     * @param action
     *            the action to execute.
     */
    public void applyAction(Session sesh, Threshold threshold, Object entity,
            Action action) {
        ActionHandler handler = actionHandlerMap.get(action.getActionType());
        if (handler == null) {
            log.error("No action handler for action type: "
                    + action.getActionType());
        } else {
            try {
                handler.executeAction(sesh, threshold, entity, action);
            } catch (ClassNotFoundException cfe) {
                log.error("Unable to execute threshold action", cfe);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(Session sesh, Object entity, Condition condition) {
        boolean match = true;
        try {

            ConditionOperator operator;
            OperatorHandler handler;
            if (condition.isSimplePropertyType()) {
                operator = condition.getValueOperator();
                handler = simpleOperatorHandlerMap.get(operator);
            } else {
                operator = condition.getComplexTypeOperator();
                handler = operatorHandlerMap.get(operator);
            }

            if (handler == null) {
                log.error("No operator handler found for operator: "
                        + operator.toString());
                System.err.println("No operator handler found");
                match = false;
            } else {
                match = match && handler.match(sesh, this, entity, condition);
            }

        } catch (Exception e) {
            e.printStackTrace();
            log.warn(e);
            match = false;
        }

        return match;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean match(SimpleTypeOperator operator, Object objA, Object objB) {
        SimpleOperatorHandler handler = simpleOperatorHandlerMap.get(operator);
        if (handler == null) {
            log.warn("No operator handler found for operator: "
                    + operator.toString());
            return false;
        } else {
            return handler.match(objA, objB);
        }
    }

    private String getReferenceKey(PersistentImpl entity) {
        if(entity == null) {
            throw new NullPointerException();
        }
        return String.format("%s.%d", entity.getClass().getCanonicalName(), entity.getId());
    }

    /**
     * Registers the instance that is being worked on by the thresholding framework. 
     * @param entity the instance being processed.
     * @see #referenceCountSet
     */
    public void registerReference(PersistentImpl entity) {
        if(entity == null) {
            throw new NullPointerException();
        }
        referenceCountSet.add(getReferenceKey(entity));
    }

    /**
     * Removes the specified instance from the set of instances being processed
     * by the thresholding framework.
     * @param entity the instance to be removed.
     * * @see #referenceCountSet
     */
    public void deregisterReference(PersistentImpl entity) {
        if(entity != null) {
            referenceCountSet.remove(getReferenceKey(entity));
        }
    }

    /**
     * Returns true if the specified instance is currently being processed
     * by the thresholding framework, false otherwise.
     * @param entity the entity that may require testing for threshold matches.  
     * @return true if the specified instance is currently being processed
     * by the thresholding framework, false otherwise.
     */
    public boolean isRegisteredReference(PersistentImpl entity) {
        return entity != null && referenceCountSet.contains(getReferenceKey(entity));
    }
}
