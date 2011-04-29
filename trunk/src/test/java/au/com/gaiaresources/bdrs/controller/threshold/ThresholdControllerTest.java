package au.com.gaiaresources.bdrs.controller.threshold;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import junit.framework.Assert;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.ModelAndViewAssert;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.controller.AbstractControllerTest;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.threshold.Action;
import au.com.gaiaresources.bdrs.model.threshold.ActionType;
import au.com.gaiaresources.bdrs.model.threshold.Condition;
import au.com.gaiaresources.bdrs.model.threshold.Operator;
import au.com.gaiaresources.bdrs.model.threshold.Threshold;
import au.com.gaiaresources.bdrs.model.threshold.ThresholdDAO;
import au.com.gaiaresources.bdrs.security.Role;
import au.com.gaiaresources.bdrs.service.threshold.ThresholdService;

/**
 * Tests all aspects of the <code>ThresholdController</code>.
 */
public class ThresholdControllerTest extends AbstractControllerTest {

    @Autowired
    private ThresholdDAO thresholdDAO;

    @Test
    public void testListThreshold() throws Exception {

        for (Class<?> klass : ThresholdService.THRESHOLD_CLASSES) {
            Threshold t = new Threshold();
            t.setClassName(klass.getCanonicalName());
            t.setEnabled(true);
            thresholdDAO.save(t);
        }

        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/listing.htm");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdList");
        Assert.assertTrue(mv.getModel().containsKey("displayNameThresholdMap"));

        @SuppressWarnings("unchecked")
        Map<Threshold, String> thresholdDisplayNameMap = (Map<Threshold, String>) mv.getModel().get("displayNameThresholdMap");

        Assert.assertEquals(ThresholdService.THRESHOLD_CLASSES.length, thresholdDisplayNameMap.size());
    }

    @Test
    public void testAddThreshold() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/edit.htm");
        ModelAndView mv = handle(request, response);

        ModelAndViewAssert.assertViewName(mv, "threshold");
        Assert.assertTrue(mv.getModel().containsKey("threshold"));
        Assert.assertNull(((Threshold) mv.getModel().get("threshold")).getId());
    }

    @Test
    public void testAddThresholdSubmit() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/threshold/edit.htm");

        Map<String, String> params = new HashMap<String, String>();
        params.put("class_name", "au.com.gaiaresources.bdrs.model.record.Record");

        params.put("condition_index", "3");

        //params.put("new_condition",  "0");
        params.put("add_condition", "0");
        params.put("add_property_path_0", "attributes");
        params.put("add_key_operator_0", "EQUALS");
        params.put("add_key_value_0", "attribute_name");
        params.put("add_value_operator_0", "CONTAINS");
        params.put("add_value_value_0", "record_attribute_value");

        //params.put("new_condition",  "1");
        params.put("add_condition", "1");
        params.put("add_property_path_1", "notes");
        params.put("add_value_operator_1", "EQUALS");
        params.put("add_value_value_1", "test");

        //params.put("new_condition",  "2");
        params.put("add_condition", "2");
        params.put("add_property_path_2", "held");
        params.put("add_value_operator_2", "EQUALS");
        params.put("add_value_value_2", "true");

        params.put("action_index", "2");

        //params.put("new_action",  "0");
        params.put("add_action_actiontype_0", "EMAIL_NOTIFICATION");
        params.put("add_action_value_0", "person@fakeemail.com");

        //params.put("new_action",  "1");
        params.put("add_action_actiontype_1", "HOLD_RECORD");
        params.put("add_action_value_1", "");

        params.put("enabled", "true");

        request.setParameters(params);
        request.addParameter("new_condition", new String[] { "0", "1", "2" });
        request.addParameter("new_action", new String[] { "0", "1" });

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/threshold/listing.htm", redirect.getUrl());

        List<Threshold> thresholdList = thresholdDAO.getEnabledThresholdByClassName(params.get("class_name"));
        Assert.assertEquals(1, thresholdList.size());

        Threshold threshold = thresholdList.get(0);
        Assert.assertEquals(threshold.getClassName(), params.get("class_name"));
        Assert.assertEquals(threshold.isEnabled(), Boolean.parseBoolean(params.get("enabled")));
        Assert.assertEquals(ThresholdService.CLASS_TO_ACTION_MAP.get(Record.class), threshold.getPossibleActionTypes());

        // Test Conditions
        for (Condition condition : threshold.getConditions()) {
            if (params.get("add_property_path_0").equals(condition.getPropertyPath())) {

                Assert.assertEquals(Operator.EQUALS, condition.getKeyOperator());
                Assert.assertEquals(params.get("add_key_value_0"), condition.getKey());
                Assert.assertEquals(Operator.CONTAINS, condition.getValueOperator());
                Assert.assertEquals(params.get("add_value_value_0"), condition.getValue());

            } else if (params.get("add_property_path_1").equals(condition.getPropertyPath())) {

                Assert.assertEquals(null, condition.getKeyOperator());
                Assert.assertEquals(null, condition.getKey());
                Assert.assertEquals(Operator.EQUALS, condition.getValueOperator());
                Assert.assertEquals(params.get("add_value_value_1"), condition.getValue());

            } else if (params.get("add_property_path_2").equals(condition.getPropertyPath())) {

                Assert.assertEquals(null, condition.getKeyOperator());
                Assert.assertEquals(null, condition.getKey());
                Assert.assertEquals(Operator.EQUALS, condition.getValueOperator());
                Assert.assertEquals(params.get("add_value_value_2"), condition.getValue());

            } else {
                Assert.assertFalse(true);
            }
        }

        // Test Actions
        for (Action action : threshold.getActions()) {
            if (ActionType.EMAIL_NOTIFICATION.equals(action.getActionType())) {

                Assert.assertEquals(params.get("add_action_value_0"), action.getValue());

            } else if (ActionType.HOLD_RECORD.equals(action.getActionType())) {

                Assert.assertEquals(params.get("add_action_value_1"), action.getValue());

            } else {
                Assert.assertFalse(true);
            }
        }
    }

    @Test
    public void testThresholdEdit() throws Exception {

        List<Condition> conditionList = new ArrayList<Condition>();
        List<Action> actionList = new ArrayList<Action>();

        Condition conditionA = new Condition();
        conditionA.setClassName(Record.class.getCanonicalName());
        conditionA.setPropertyPath("attributes");
        conditionA.setKeyOperator(Operator.EQUALS);
        conditionA.setKey("attribute_name");
        conditionA.setValueOperator(Operator.EQUALS);
        conditionA.setValue("record_attribute_value");
        conditionA = thresholdDAO.save(conditionA);
        conditionList.add(conditionA);

        Condition conditionB = new Condition();
        conditionB.setClassName(Record.class.getCanonicalName());
        conditionB.setPropertyPath("notes");
        conditionB.setValueOperator(Operator.EQUALS);
        conditionB.setValue("test");
        conditionB = thresholdDAO.save(conditionB);
        conditionList.add(conditionB);

        Condition conditionC = new Condition();
        conditionC.setClassName(Record.class.getCanonicalName());
        conditionC.setPropertyPath("held");
        conditionC.setValueOperator(Operator.EQUALS);
        conditionC.setValue("true");
        conditionC = thresholdDAO.save(conditionC);
        conditionList.add(conditionC);

        Action actionA = new Action();
        actionA.setActionType(ActionType.EMAIL_NOTIFICATION);
        actionA.setValue("person@fakeemail.com");
        actionA = thresholdDAO.save(actionA);
        actionList.add(actionA);

        Action actionB = new Action();
        actionB.setActionType(ActionType.HOLD_RECORD);
        actionB.setValue("");
        actionB = thresholdDAO.save(actionB);
        actionList.add(actionB);

        Threshold threshold = new Threshold();
        threshold.setClassName(Record.class.getCanonicalName());
        threshold.setEnabled(true);
        threshold.getConditions().addAll(conditionList);
        threshold.getActions().addAll(actionList);
        threshold = thresholdDAO.save(threshold);

        login("admin", "password", new String[] { Role.ADMIN });

        Map<String, String> params = new HashMap<String, String>();
        params.put("threshold_id", threshold.getId().toString());
        params.put("class_name", threshold.getClassName());

        params.put("condition_index", "0");

        //params.put("condition_pk", conditionA.getId().toString());
        params.put(String.format("property_path_%d", conditionA.getId()), "held");
        params.put(String.format("value_operator_%d", conditionA.getId()), "EQUALS");
        params.put(String.format("value_value_%d", conditionA.getId()), "false");

        //params.put("condition_pk", conditionB.getId().toString());
        params.put(String.format("property_path_%d", conditionB.getId()), "behaviour");
        params.put(String.format("value_operator_%d", conditionB.getId()), "CONTAINS");
        params.put(String.format("value_value_%d", conditionB.getId()), "testEdit");

        //params.put("condition_pk", conditionC.getId().toString());
        params.put(String.format("property_path_%d", conditionC.getId()), "attributes");
        params.put(String.format("key_operator_%d", conditionC.getId()), "CONTAINS");
        params.put(String.format("key_value_%d", conditionC.getId()), "edit_attribute_name");
        params.put(String.format("value_operator_%d", conditionC.getId()), "CONTAINS");
        params.put(String.format("value_value_%d", conditionC.getId()), "edit_record_attribute_value");

        params.put("action_index", "0");

        //params.put("action_pk", actionA.getId().toString());
        params.put(String.format("action_actiontype_%d", actionA.getId()), "EMAIL_NOTIFICATION");
        params.put(String.format("action_value_%d", actionA.getId()), "person@fakeemail.com");

        //params.put("action_pk", actionB.getId().toString());
        params.put(String.format("action_actiontype_%d", actionB.getId()), "HOLD_RECORD");
        params.put(String.format("action_value_%d", actionB.getId()), "");

        params.put("enabled", "false");

        request.setMethod("POST");
        request.setRequestURI("/bdrs/admin/threshold/edit.htm");
        request.setParameters(params);
        request.addParameter("condition_pk", new String[] {
                conditionA.getId().toString(), conditionB.getId().toString(),
                conditionC.getId().toString() });
        request.addParameter("action_pk", new String[] {
                actionA.getId().toString(), actionB.getId().toString() });

        ModelAndView mv = handle(request, response);
        Assert.assertTrue(mv.getView() instanceof RedirectView);
        RedirectView redirect = (RedirectView) mv.getView();
        Assert.assertEquals("/bdrs/admin/threshold/listing.htm", redirect.getUrl());

        threshold = thresholdDAO.getThreshold(threshold.getId());
        Assert.assertEquals(params.get("class_name"), threshold.getClassName());
        Assert.assertEquals(Boolean.parseBoolean(params.get("enabled")), threshold.isEnabled());

        for (Condition testCondition : threshold.getConditions()) {
            if (conditionA.getId().equals(testCondition.getId())) {

                Assert.assertEquals(params.get(String.format("property_path_%d", conditionA.getId())), testCondition.getPropertyPath());
                Assert.assertEquals(Operator.EQUALS, testCondition.getValueOperator());
                Assert.assertFalse(testCondition.booleanValue());

            } else if (conditionB.getId().equals(testCondition.getId())) {

                Assert.assertEquals(params.get(String.format("property_path_%d", conditionB.getId())), testCondition.getPropertyPath());
                Assert.assertEquals(Operator.CONTAINS, testCondition.getValueOperator());
                Assert.assertEquals(params.get(String.format("value_value_%d", conditionB.getId())), testCondition.stringValue());

            } else if (conditionC.getId().equals(testCondition.getId())) {

                Assert.assertEquals(params.get(String.format("property_path_%d", conditionC.getId())), testCondition.getPropertyPath());
                Assert.assertEquals(Operator.CONTAINS, testCondition.getKeyOperator());
                Assert.assertEquals(params.get(String.format("key_value_%d", conditionC.getId())), testCondition.stringKey());
                Assert.assertEquals(Operator.CONTAINS, testCondition.getValueOperator());
                Assert.assertEquals(params.get(String.format("value_value_%d", conditionC.getId())), testCondition.stringValue());

            } else {
                Assert.assertTrue(false);
            }
        }

        for (Action testAction : threshold.getActions()) {
            if (actionA.getId().equals(testAction.getId())) {

                Assert.assertEquals(ActionType.EMAIL_NOTIFICATION, testAction.getActionType());
                Assert.assertEquals(params.get(String.format("action_value_%d", actionA.getId())), testAction.getValue());

            } else if (actionB.getId().equals(testAction.getId())) {

                Assert.assertEquals(ActionType.HOLD_RECORD, testAction.getActionType());
                Assert.assertEquals(params.get(String.format("action_value_%d", actionB.getId())), testAction.getValue());

            } else {
                Assert.assertTrue(false);
            }
        }
    }

    @Test
    public void testAjaxChangePropertyPathPersistent() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxChangePropertyPath.htm");

        request.setParameter("className", Record.class.getCanonicalName());
        request.setParameter("index", "1");
        request.setParameter("propertyPath", "species");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdPathDescriptorRenderer");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("condition"));
        Assert.assertTrue(mv.getModel().containsKey("path_descriptor_list"));

        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));
        Condition condition = (Condition) mv.getModel().get("condition");
        Assert.assertEquals(Record.class.getCanonicalName(), condition.getClassName());
        Assert.assertEquals(request.getParameter("propertyPath"), condition.getPropertyPath());
    }

    @Test
    public void testAjaxChangePropertyPathPrimitive() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxChangePropertyPath.htm");

        request.setParameter("className", Record.class.getCanonicalName());
        request.setParameter("index", "1");
        request.setParameter("propertyPath", "behaviour");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdOperatorRenderer");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("condition"));

        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));
        Condition condition = (Condition) mv.getModel().get("condition");
        Assert.assertEquals(Record.class.getCanonicalName(), condition.getClassName());
        Assert.assertEquals(request.getParameter("propertyPath"), condition.getPropertyPath());

    }

    @Test
    public void testAjaxChangePropertyPathComplex() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxChangePropertyPath.htm");

        request.setParameter("className", Record.class.getCanonicalName());
        request.setParameter("index", "1");
        request.setParameter("propertyPath", "attributes");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdOperatorRenderer");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("condition"));

        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));
        Condition condition = (Condition) mv.getModel().get("condition");
        Assert.assertEquals(Record.class.getCanonicalName(), condition.getClassName());
        Assert.assertEquals(request.getParameter("propertyPath"), condition.getPropertyPath());
    }

    @Test
    public void testAjaxAddCondition() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxAddCondition.htm");

        request.setParameter("className", Record.class.getCanonicalName());
        request.setParameter("index", "1");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdConditionRow");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("condition"));

        Assert.assertEquals(request.getParameter("index"), mv.getModel().get("index"));
        Condition condition = (Condition) mv.getModel().get("condition");
        Assert.assertEquals(request.getParameter("className"), condition.getClassName());
        Assert.assertEquals(null, condition.getPropertyPath());
    }

    @Test
    public void testAjaxConditionValueSimple() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });

        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxConditionValue.htm");
        request.setParameter("className", Record.class.getCanonicalName());
        request.setParameter("propertyPath", "behaviour");
        request.setParameter("index", "1");
        request.setParameter("valueOperator", "EQUALS");
        request.setParameter("value", "nesting");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdConditionValue");

        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));
        Condition condition = (Condition) mv.getModel().get("condition");
        Assert.assertEquals(request.getParameter("className"), condition.getClassName());
        Assert.assertEquals(request.getParameter("propertyPath"), condition.getPropertyPath());
    }

    @Test
    public void testAjaxAddAction() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxAddAction.htm");

        request.setParameter("className", Record.class.getCanonicalName());
        request.setParameter("index", "1");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdActionRow");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("action"));
        Assert.assertTrue(mv.getModel().containsKey("threshold"));

        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));
        Threshold threshold = (Threshold) mv.getModel().get("threshold");
        Action action = (Action) mv.getModel().get("action");
        Assert.assertEquals(request.getParameter("className"), threshold.getClassName());
        Assert.assertNotNull(action.getActionType());
    }

    @Test
    public void testAjaxActionValueForActionTypeEmail() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxActionValueForActionType.htm");

        request.setParameter("actionType", ActionType.EMAIL_NOTIFICATION.toString());
        request.setParameter("index", "1");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdActionValue");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("action"));
        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));

        Action action = (Action) mv.getModel().get("action");
        Assert.assertEquals(ActionType.EMAIL_NOTIFICATION, action.getActionType());
    }

    @Test
    public void testAjaxActionValueForActionTypeHoldRecord() throws Exception {

        login("admin", "password", new String[] { Role.ADMIN });
        request.setMethod("GET");
        request.setRequestURI("/bdrs/admin/threshold/ajaxActionValueForActionType.htm");

        request.setParameter("actionType", ActionType.HOLD_RECORD.toString());
        request.setParameter("index", "1");

        ModelAndView mv = handle(request, response);
        ModelAndViewAssert.assertViewName(mv, "thresholdActionValue");

        Assert.assertTrue(mv.getModel().containsKey("index"));
        Assert.assertTrue(mv.getModel().containsKey("action"));
        Assert.assertEquals(Integer.parseInt(request.getParameter("index")), mv.getModel().get("index"));

        Action action = (Action) mv.getModel().get("action");
        Assert.assertEquals(ActionType.HOLD_RECORD, action.getActionType());
    }
}
