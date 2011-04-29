package au.com.gaiaresources.bdrs.controller.threshold;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.security.RolesAllowed;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;
import org.hibernate.FlushMode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.validation.DataBinder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.property.PropertyService;
import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;
import au.com.gaiaresources.bdrs.controller.AbstractController;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.ActionType;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Operator;
import au.com.gaiaresources.bdrs.model.threshold.PathDescriptor;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.threshold.ThresholdDAO;

/**
 * The <code>ThresholdController</code> handles all request for system
 * notifications.
 */
@Controller
public class ThresholdController extends AbstractController {

    public static final String THRESHOLD_CLASS_DISPLAYNAME_PLURAL_TEMPLATE = "%s.plural";
    public static final String THRESHOLD_CLASS_DISPLAYNAME_TEMPLATE = "%s";

    @SuppressWarnings("unused")
    private Logger log = Logger.getLogger(getClass());

    @Autowired
    private ThresholdDAO thresholdDAO;

    @Autowired
    private PropertyService propertyService;

    /**
     * Displays a listing of all thresholds currently in the system.
     * 
     * @param request
     *            the browser request
     * @param response
     *            the server response
     * @return
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/listing.htm", method = RequestMethod.GET)
    public ModelAndView listThreshold(HttpServletRequest request,
            HttpServletResponse response) {

        List<Threshold> thresholdList = thresholdDAO.all();
        Map<Threshold, String> displayNameThresholdMap = new LinkedHashMap<Threshold, String>();
        for (Threshold threshold : thresholdList) {
            String displayNameKey = String.format(THRESHOLD_CLASS_DISPLAYNAME_TEMPLATE, threshold.getClassName());
            displayNameThresholdMap.put(threshold, propertyService.getMessage(displayNameKey));
        }

        ModelAndView mv = new ModelAndView("thresholdList");
        mv.addObject("displayNameThresholdMap", displayNameThresholdMap);

        return mv;
    }

    /**
     * Displays a form for creating/editing a threshold. If the thresholdId is
     * not specified or does not match the primary key of an existing threshold,
     * then the add form shall be displayed, otherwise the edit form is shown.
     * 
     * @param request
     *            the browser request
     * @param response
     *            the server response
     * @return
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/edit.htm", method = RequestMethod.GET)
    public ModelAndView editThreshold(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "thresholdId", defaultValue = "0", required = false) int thresholdPk) {

        Threshold threshold = thresholdDAO.getThreshold(thresholdPk);
        if (threshold == null) {
            threshold = new Threshold();
        }

        String displayNameKey;
        Map<String, String> displayNameMap = new HashMap<String, String>(
                ThresholdService.THRESHOLD_CLASSES.length);
        for (Class<?> thresholdClass : ThresholdService.THRESHOLD_CLASSES) {
            displayNameKey = String.format(THRESHOLD_CLASS_DISPLAYNAME_PLURAL_TEMPLATE, thresholdClass.getCanonicalName());
            displayNameMap.put(thresholdClass.getCanonicalName(), propertyService.getMessage(displayNameKey));
        }

        ModelAndView mv = new ModelAndView("threshold");
        mv.addObject("displayNameMap", displayNameMap);
        mv.addObject("threshold", threshold);
        return mv;
    }

    /**
     * The POST handler for the creation/update of thresholds.
     * 
     * @param request
     *            the browser request
     * @param response
     *            the server response
     * @param thresholdPk
     *            the primary key of the threshold if there is one.
     * @param isEnabled
     *            indicates if this threshold should be activated.
     * @param className
     *            the class where this threshold applies.
     * @param newConditionIndex
     *            the list of new conditions
     * @param conditionPks
     *            the list of primary keys to existing conditions (to be
     *            retained by the threshold). If the threshold contains a
     *            condition whose primary key is not in this array, then the
     *            condition will be deleted.
     * @param newActionIndex
     *            the list of new actions.
     * @param actionPks
     *            the list of primary keys to existing actions (to be retained
     *            by the threshold). If the threshold contains an action whose
     *            primary key is not in this array, then the action shall be
     *            deleted.
     * @return
     * @throws ClassNotFoundException
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/edit.htm", method = RequestMethod.POST)
    public ModelAndView editThreshold(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "threshold_id", required = false) Integer thresholdPk,
            @RequestParam(value = "enabled", required = false, defaultValue = "false") boolean isEnabled,
            @RequestParam(value = "class_name", required = true) String className,
            @RequestParam(value = "new_condition", required = false) int[] newConditionIndex,
            @RequestParam(value = "condition_pk", required = false) int[] conditionPks,
            @RequestParam(value = "new_action", required = false) int[] newActionIndex,
            @RequestParam(value = "action_pk", required = false) int[] actionPks)
            throws ClassNotFoundException {

        Class<?> klass = Class.forName(className);
        Threshold threshold = thresholdPk == null ? new Threshold()
                : thresholdDAO.getThreshold(thresholdPk);
        threshold.setClassName(klass.getCanonicalName());
        threshold.setEnabled(isEnabled);

        // Conditions
        Map<Integer, Condition> conditionMap = new HashMap<Integer, Condition>();
        for (Condition condition : threshold.getConditions()) {
            conditionMap.put(condition.getId(), condition);
        }
        List<Condition> conditionList = new ArrayList<Condition>();

        // Update existing conditions here.
        if (conditionPks != null) {
            for (int conditionPk : conditionPks) {

                String propertyPath = request.getParameter(String.format("property_path_%d", conditionPk));
                String keyOperatorStr = request.getParameter(String.format("key_operator_%d", conditionPk));
                Operator keyOperator = keyOperatorStr == null ? null
                        : Operator.valueOf(keyOperatorStr);
                Operator valueOperator = Operator.valueOf(request.getParameter(String.format("value_operator_%d", conditionPk)));
                String key = request.getParameter(String.format("key_value_%d", conditionPk));
                String value = request.getParameter(String.format("value_value_%d", conditionPk));

                Condition condition = conditionMap.remove(conditionPk);
                condition.setClassName(className);
                condition.setPropertyPath(propertyPath);
                condition.setKeyOperator(keyOperator);
                condition.setValueOperator(valueOperator);
                condition.setKey(key);
                condition.setValue(value);
                
                condition = thresholdDAO.save(condition);

                conditionList.add(condition);
            }
        }

        // Add new conditions
        if (newConditionIndex != null) {
            for (int conditionIndex : newConditionIndex) {

                String propertyPath = request.getParameter(String.format("add_property_path_%d", conditionIndex));
                String keyOperatorStr = request.getParameter(String.format("add_key_operator_%d", conditionIndex));
                Operator keyOperator = keyOperatorStr == null ? null
                        : Operator.valueOf(keyOperatorStr);
                Operator valueOperator = Operator.valueOf(request.getParameter(String.format("add_value_operator_%d", conditionIndex)));
                String key = request.getParameter(String.format("add_key_value_%d", conditionIndex));
                String value = request.getParameter(String.format("add_value_value_%d", conditionIndex));

                Condition condition = new Condition();
                condition.setClassName(className);
                condition.setPropertyPath(propertyPath);
                condition.setKeyOperator(keyOperator);
                condition.setValueOperator(valueOperator);
                condition.setKey(key);
                condition.setValue(value);
                
                condition = thresholdDAO.save(condition);

                conditionList.add(condition);
            }
        }
        threshold.setConditions(conditionList);

        // Actions
        Map<Integer, Action> actionMap = new HashMap<Integer, Action>();
        for (Action action : threshold.getActions()) {
            actionMap.put(action.getId(), action);
        }
        List<Action> actionList = new ArrayList<Action>();

        // Update Existing Actions
        if (actionPks != null) {
            for (int actionPk : actionPks) {
                ActionType actionType = ActionType.valueOf(request.getParameter(String.format("action_actiontype_%d", actionPk)));
                String actionValue = request.getParameter(String.format("action_value_%d", actionPk));

                Action action = actionMap.remove(actionPk);
                action.setActionType(actionType);
                action.setValue(actionValue);

                actionList.add(action);
            }
        }

        // Add new Actions
        if (newActionIndex != null) {
            for (int actionIndex : newActionIndex) {

                ActionType actionType = ActionType.valueOf(request.getParameter(String.format("add_action_actiontype_%d", actionIndex)));
                String actionValue = request.getParameter(String.format("add_action_value_%d", actionIndex));

                Action action = new Action();
                action.setActionType(actionType);
                action.setValue(actionValue);

                action = thresholdDAO.save(action);

                actionList.add(action);
            }
        }
        threshold.setActions(actionList);

        thresholdDAO.save(threshold);

        // Delete the now orphaned conditions.
        for (Condition condition : conditionMap.values()) {
            thresholdDAO.delete(condition);
        }
        // Delete the now orphaned actions.
        for (Action action : actionMap.values()) {
            thresholdDAO.delete(action);
        }

        ModelAndView mv = new ModelAndView(new RedirectView(
                "/bdrs/admin/threshold/listing.htm", true));
        return mv;
    }

    // ------------------------
    // AJAX Request Handlers
    // ------------------------

    // -- Property Navigation -
    
    /**
     * Responds to an AJAX request resulting from a change in the selected
     * property path.
     * 
     * @param className the class where this threshold applies.
     * @param propertyPath the selected property path.
     * @param index the index of the condition if this is a new condition.
     * @param conditionPk the primary key of the condition if this is an existing condition.
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/ajaxChangePropertyPath.htm", method = RequestMethod.GET)
    public ModelAndView ajaxChangePropertyPath(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "className", required = true) String className,
            @RequestParam(value = "propertyPath", required = true) String propertyPath,
            @RequestParam(value = "index", required = false, defaultValue = "0") int index,
            @RequestParam(value = "conditionId", required = false, defaultValue = "0") int conditionPk)
            throws ClassNotFoundException, IOException {

        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);

        Condition condition = thresholdDAO.getCondition(conditionPk);
        if (condition == null) {
            condition = new Condition();
        }
        condition.setClassName(className);
        condition.setPropertyPath(propertyPath);

        List<PathDescriptor> childPathDescriptors = condition.getChildPathDescriptors();

        ModelAndView mv;
        if (childPathDescriptors.isEmpty()) {
            // Display Operators
            mv = new ModelAndView("thresholdOperatorRenderer");

            if (condition.getValueOperator() == null) {
                // Should be safe to assume there is at least one thing in the array.
                condition.setValueOperator(condition.getPossibleValueOperators()[0]);
            }
        } else {
            mv = new ModelAndView("thresholdPathDescriptorRenderer");
            mv.addObject("path_descriptor_list", condition.getChildPathDescriptors());
        }

        mv.addObject("index", index);
        mv.addObject("condition", condition);
        return mv;
    }

    // -- Conditions ----------

    /**
     * Handles an AJAX request to add a new condition.
     * 
     * @param className the class where the condition applies.
     * @param index the index for the new condition.
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/ajaxAddCondition.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddCondition(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "className", required = true) String className,
            @RequestParam(value = "index", required = true) String index)
            throws ClassNotFoundException {

        Condition condition = new Condition();
        condition.setClassName(className);

        ModelAndView mv = new ModelAndView("thresholdConditionRow");
        mv.addObject("condition", condition);
        mv.addObject("index", index);

        return mv;
    }

    /**
     * An AJAX request handler to display the input fields where a user can
     * input the values to be matched by the condition.
     * 
     * @param request the browser request
     * @param response the server response
     * @param className the class where this threshold applies.
     * @param propertyPath the path to the property to be checked.
     * @param index the index of the condition if it is new.
     * @param conditionPk the primary key of the condition if it is being edited.
     * @param keyOperatorStr the operator to be applied to the condition key
     * @param valueOperatorStr the operator to be applied to the condition value.
     * @param key the user inputted value for the key.
     * @param value the user inputted value for the value.
     * @param valueForKey true if this is a complex operation
     * @return
     * @throws ClassNotFoundException
     * @throws IOException
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/ajaxConditionValue.htm", method = RequestMethod.GET)
    public ModelAndView ajaxConditionValue(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "className", required = true) String className,
            @RequestParam(value = "propertyPath", required = true) String propertyPath,
            @RequestParam(value = "index", required = false, defaultValue = "0") int index,
            @RequestParam(value = "conditionId", required = false, defaultValue = "0") int conditionPk,
            @RequestParam(value = "keyOperator", required = false) String keyOperatorStr,
            @RequestParam(value = "valueOperator", required = true) String valueOperatorStr,
            @RequestParam(value = "key", required = false) String key,
            @RequestParam(value = "value", required = true) String value,
            @RequestParam(value = "valueForKey", required = false, defaultValue = "true") boolean valueForKey)
            throws ClassNotFoundException, IOException {

        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);

        Condition condition = thresholdDAO.getCondition(conditionPk);
        if (condition == null) {
            condition = new Condition();
        }
        condition.setClassName(className);
        condition.setPropertyPath(propertyPath);
        condition.setValueOperator(Operator.valueOf(valueOperatorStr));
        Operator keyOperator = keyOperatorStr == null ? null
                : Operator.valueOf(keyOperatorStr);
        condition.setKeyOperator(keyOperator);
        condition.setKey(key);
        condition.setValue(value);

        ModelAndView mv = new ModelAndView("thresholdConditionValue");
        mv.addObject("condition", condition);
        mv.addObject("index", index);
        mv.addObject("valueForKey", valueForKey);
        return mv;
    }

    // -- Action ----------- 
    
    /**
     * Handles an AJAX request to add a new action.
     * 
     * @param request the browser request
     * @param response the server response
     * @param className the class where this threshold applies.
     * @param index the index of the new action.
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/ajaxAddAction.htm", method = RequestMethod.GET)
    public ModelAndView ajaxAddAction(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "className", required = true) String className,
            @RequestParam(value = "index", required = true) int index)
            throws ClassNotFoundException {

        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);

        Class<?> klass = Class.forName(className);
        Threshold threshold = new Threshold();
        threshold.setClassName(className);

        Action action = new Action();
        action.setActionType(ThresholdService.CLASS_TO_ACTION_MAP.get(klass)[0]);

        ModelAndView mv = new ModelAndView("thresholdActionRow");
        mv.addObject("threshold", threshold);
        mv.addObject("action", action);
        mv.addObject("index", index);
        return mv;
    }

    /**
     * Handles an AJAX request to display the appropriate input fields for the
     * specified action type.
     * 
     * @param request the browser request
     * @param response the server response
     * @param actionPk the primary key of the action if the action is being edited
     * @param actionTypeStr the type of the action which affects the inputs to be redered
     * @param index the index of the action if the action is new.
     * @return
     * @throws ClassNotFoundException
     */
    @RolesAllowed( { Role.ADMIN })
    @RequestMapping(value = "/bdrs/admin/threshold/ajaxActionValueForActionType.htm", method = RequestMethod.GET)
    public ModelAndView ajaxActionValueForActionType(
            HttpServletRequest request,
            HttpServletResponse response,
            @RequestParam(value = "actionId", required = false, defaultValue = "0") int actionPk,
            @RequestParam(value = "actionType", required = true) String actionTypeStr,
            @RequestParam(value = "index", required = false, defaultValue = "0") int index)
            throws ClassNotFoundException {

        getRequestContext().getHibernate().setFlushMode(FlushMode.MANUAL);

        ActionType actionType = ActionType.valueOf(actionTypeStr);
        Action action = thresholdDAO.getAction(actionPk);
        if (action == null) {
            action = new Action();
        }
        action.setActionType(actionType);

        ModelAndView mv = new ModelAndView("thresholdActionValue");
        mv.addObject("action", action);
        mv.addObject("index", index);

        return mv;
    }
}
