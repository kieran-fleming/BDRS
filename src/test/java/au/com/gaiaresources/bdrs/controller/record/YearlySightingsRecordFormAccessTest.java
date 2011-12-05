package au.com.gaiaresources.bdrs.controller.record;

import java.util.HashMap;
import java.util.Map;

public class YearlySightingsRecordFormAccessTest extends
        AbstractRecordFormAccessTest {

    @Override
    protected String getExpectedViewName() {
        return YearlySightingsController.YEARLY_SIGHTINGS_FORM_VIEW_NAME;
    }

    @Override
    protected String getGetUri() {
        return YearlySightingsController.YEARLY_SIGHTINGS_URL;
    }

    @Override
    protected Map<String, String> getPostMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(YearlySightingsController.PARAM_SURVEY_ID, "1");
        map.put(YearlySightingsController.PARAM_LOCATION_ID, "1");
        return map;
    }

    @Override
    protected String getPostUri() {
        return YearlySightingsController.YEARLY_SIGHTINGS_URL;
    }

    @Override
    protected String getRecordIdKey() {
        return YearlySightingsController.PARAM_RECORD_ID;
    }

    @Override
    protected String getSurveyIdKey() {
        return YearlySightingsController.PARAM_SURVEY_ID;
    }

}
