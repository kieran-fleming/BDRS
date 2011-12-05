package au.com.gaiaresources.bdrs.util;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.Session;

import au.com.gaiaresources.bdrs.model.portal.Portal;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.taxa.AttributeScope;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.ActionEvent;
import au.com.gaiaresources.bdrs.model.threshold.ActionType;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Operator;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.threshold.ThresholdDAO;

/**
 * The ModerationUtil creates the default {@link Threshold} for moderation.
 * @author stephanie
 */
public class ModerationUtil {
    public static final String MODERATION_THRESHOLD_NAME = "Moderation Threshold";
    private static final String MODERATION_THRESHOLD_DESCRIPTION = 
        "This threshold will send an email to the moderators when a record is created/updated, " +
        "and an email to the record owner when their record is updated by a moderator.  It will " +
        "also hold a record on creation if it has moderation attributes.";
    
    private static final String SCOPE_PROPERTY_PATH = "survey.attributes.scope";
    
    private ThresholdDAO thresholdDAO;
    
    public ModerationUtil(ThresholdDAO thresholdDAO) {
        this.thresholdDAO = thresholdDAO;
    }
    
    public Threshold createModerationThreshold(Session sesh, Portal portal) {
        Threshold threshold = new Threshold();
        String className = Record.class.getCanonicalName();
        threshold.setClassName(className);
        threshold.setEnabled(true);
        threshold.setName(MODERATION_THRESHOLD_NAME);
        threshold.setDescription(MODERATION_THRESHOLD_DESCRIPTION);
        threshold.setPortal(portal);
        
        List<Condition> conditionList = new ArrayList<Condition>();
        // set the conditions
        Condition condition = new Condition();
        condition.setClassName(className);
        condition.setPropertyPath(SCOPE_PROPERTY_PATH);
        condition.setKeyOperator(null);
        condition.setValueOperator(Operator.CONTAINS);
        condition.setKey(null);
        condition.setValue(new String[]{
                AttributeScope.RECORD_MODERATION.toString(),
                AttributeScope.SURVEY_MODERATION.toString()
        });
        condition.setPortal(portal);
        
        condition = thresholdDAO.save(sesh, condition);
        conditionList.add(condition);
        threshold.setConditions(conditionList);
        
        // set the actions
        List<Action> actionList = new ArrayList<Action>();
        // hold action
        Action action = new Action();
        action.setActionType(ActionType.HOLD_RECORD);
        action.setValue("");
        action.setActionEvent(ActionEvent.CREATE);
        action.setPortal(portal);
        action = thresholdDAO.save(sesh, action);
        actionList.add(action);
        
        // email action
        action = new Action();
        action.setActionType(ActionType.MODERATION_EMAIL_NOTIFICATION);
        action.setValue("");
        action.setActionEvent(ActionEvent.CREATE_AND_UPDATE);
        action.setPortal(portal);
        action = thresholdDAO.save(sesh, action);
        actionList.add(action);
        
        threshold.setActions(actionList);

        return thresholdDAO.save(sesh, threshold);
    }
}
