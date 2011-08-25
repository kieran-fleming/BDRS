package au.com.gaiaresources.bdrs.service.web;

import org.springframework.stereotype.Service;

import au.com.gaiaresources.bdrs.controller.HomePageController;
import au.com.gaiaresources.bdrs.controller.RenderController;
import au.com.gaiaresources.bdrs.controller.admin.AdminHomePageController;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;

// The idea was to reduce the amount of times there were redirection urls as string literals in the controllers.
// Also with the addition of GET parameters things just got more hairy. Wanted to make it type safe as well as
// reduce duplication.
// Maybe you could call it something else like UrlService. 
@Service
public class RedirectionService {
    
    private static final String mySightingsUrl = "/map/mySightings.htm";
    public String getMySightingsUrl(Survey survey) {
        if (survey == null)
        {
            return mySightingsUrl;
        }
        return mySightingsUrl + "?defaultSurveyId=" + survey.getId().toString();
    }
    
    public String getViewRecordUrl(Record record) {
        if (record == null) {
            throw new IllegalArgumentException("record cannot be null");
        }
        if (record.getId() == null) {
            throw new IllegalArgumentException("record.id cannot be null");
        }
        if (record.getSurvey() == null) {
            throw new IllegalArgumentException("record.survey cannot be null");
        }
        if (record.getSurvey().getId() == null) {
            throw new IllegalArgumentException("record.survey.id cannot be null");
        }
        return RenderController.SURVEY_RENDER_REDIRECT_URL + "?" + 
            RenderController.PARAM_SURVEY_ID + "=" + record.getSurvey().getId().toString() + "&" +
            RenderController.PARAM_RECORD_ID + "=" + record.getId().toString();
    }
    
    public String getAdminHomeUrl() {
        return AdminHomePageController.ADMIN_HOME_URL;
    }
    
    public String getHomeUrl() {
        return HomePageController.HOME_URL;
    }
}
