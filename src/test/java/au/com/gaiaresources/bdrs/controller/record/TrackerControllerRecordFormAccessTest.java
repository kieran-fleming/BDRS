package au.com.gaiaresources.bdrs.controller.record;

import java.util.HashMap;
import java.util.Map;

public class TrackerControllerRecordFormAccessTest extends
        AbstractRecordFormAccessTest {

    @Override
    protected String getGetUri() {
        return TrackerController.EDIT_URL;
    }

    @Override
    protected String getExpectedViewName() {
        return TrackerController.TRACKER_VIEW_NAME;
    }

    @Override
    protected String getRecordIdKey() {
        return TrackerController.PARAM_RECORD_ID;
    }

    @Override
    protected String getSurveyIdKey() {
        return TrackerController.PARAM_SURVEY_ID;
    }

    @Override
    protected Map<String, String> getPostMap() {
        Map<String, String> result = new HashMap<String, String>();
        result.put(TrackerController.PARAM_SURVEY_ID, "1");
        return result;
    }

    @Override
    protected String getPostUri() {
        return TrackerController.EDIT_URL;
    }
}
