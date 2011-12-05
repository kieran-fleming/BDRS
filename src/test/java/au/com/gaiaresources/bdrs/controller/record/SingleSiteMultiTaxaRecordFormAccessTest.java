package au.com.gaiaresources.bdrs.controller.record;

import java.util.HashMap;
import java.util.Map;

public class SingleSiteMultiTaxaRecordFormAccessTest extends
        AbstractRecordFormAccessTest {

    @Override
    protected String getExpectedViewName() {
        return SingleSiteMultiTaxaController.SINGLE_SITE_MULTI_TAXA_VIEW_NAME;
    }

    @Override
    protected String getRecordIdKey() {
        return SingleSiteController.PARAM_RECORD_ID;
    }

    @Override
    protected String getSurveyIdKey() {
        return SingleSiteController.PARAM_SURVEY_ID;
    }

    @Override
    protected String getGetUri() {
        return SingleSiteMultiTaxaController.SINGLE_SITE_MULTI_TAXA_URL;
    }

    @Override
    protected Map<String, String> getPostMap() {
        Map<String, String> postMap = new HashMap<String, String>();
        
        postMap.put(SingleSiteController.PARAM_SURVEY_ID, "2");
        postMap.put(SingleSiteController.PARAM_SIGHTING_INDEX, "2");
        
        return postMap;
    }

    @Override
    protected String getPostUri() {
        return SingleSiteMultiTaxaController.SINGLE_SITE_MULTI_TAXA_URL;
    }

}
