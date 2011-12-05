package au.com.gaiaresources.bdrs.controller.record;

import java.util.HashMap;
import java.util.Map;

public class AtlasControllerRecordFormAccessTest extends
        AbstractRecordFormAccessTest {

    @Override
    protected String getExpectedViewName() {
        return AtlasController.ATLAS_FORM_VIEW_NAME;
    }

    @Override
    protected String getGetUri() {
        return AtlasController.ATLAS_URL;
    }

    @Override
    protected Map<String, String> getPostMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(TrackerController.PARAM_SURVEY_ID, "1");
        return map;
    }

    @Override
    protected String getPostUri() {
        return TrackerController.EDIT_URL;
    }

    @Override
    protected String getRecordIdKey() {
        return AtlasController.PARAM_RECORD_ID;
    }

    @Override
    protected String getSurveyIdKey() {
        return AtlasController.PARAM_SURVEY_ID;
    }

    @Override
    protected Map<String, String> getGetMap() {
        Map<String, String> map = new HashMap<String, String>();
        map.put(AtlasController.PARAM_TAXON_SEARCH, species.getScientificName());
        return map;
    }
}
