package au.com.gaiaresources.bdrs.service.web;

import org.apache.log4j.Logger;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import au.com.gaiaresources.bdrs.controller.HomePageController;
import au.com.gaiaresources.bdrs.controller.RenderController;
import au.com.gaiaresources.bdrs.controller.admin.AdminHomePageController;
import au.com.gaiaresources.bdrs.controller.admin.EditUsersController;
import au.com.gaiaresources.bdrs.controller.admin.users.UserController;
import au.com.gaiaresources.bdrs.controller.file.DownloadFileController;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.taxa.Attribute;
import au.com.gaiaresources.bdrs.model.taxa.AttributeType;
import au.com.gaiaresources.bdrs.model.taxa.AttributeValue;

// The idea was to reduce the amount of times there were redirection urls as string literals in the controllers.
// Also with the addition of GET parameters things just got more hairy. Wanted to make it type safe as well as
// reduce duplication.
// Maybe you could call it something else like UrlService. 
@Service
public class RedirectionService {
    
    private Logger log = Logger.getLogger(getClass());
    
    private String domainAndContextPath = "";
    
    public RedirectionService() {
        
    }
    
    /**
     * We have the option to NOT autowire this service and provide a domain + contextPath in the ctor.
     * By default this will not be used as most of the code that uses this class will require a 
     * context relative path.
     * 
     * Exceptions should be thrown if the full url is requested.
     * 
     * @param domainAndContextPath
     */
    public RedirectionService(String domainAndContextPath) {
        this.domainAndContextPath = domainAndContextPath;
    }
    
    private static final String mySightingsUrl = "/map/mySightings.htm";
    
    public String getMySightingsUrl(Survey survey) {
        if (survey == null)
        {
            return mySightingsUrl;
        }
        return mySightingsUrl + "?survey_id=" + survey.getId().toString();
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
    
    /**
     * Returns a URL to download a file associated with an AttributeValue
     * 
     * @param av - the attribute value to retrieve the file url for
     * @param fullUrl - whether to return the full url including the domain and contextPath or a context relative path
     * @return the url
     */
    public String getFileDownloadUrl(AttributeValue av, boolean fullUrl) {
        if (fullUrl && !StringUtils.hasLength(this.domainAndContextPath)) {
            throw new IllegalStateException("Cannot return full url as the domain and context path have not been set.");
        }
        if (av.getAttribute() == null) {
            throw new IllegalStateException("Attribute av.attribute cannot be null");
        }
        Attribute a = av.getAttribute();
        if (a.getType() != AttributeType.FILE && a.getType() != AttributeType.IMAGE) {
            throw new IllegalArgumentException("AttributeValue av needs to be of type FILE or IMAGE to request download URL");
        }
        if (av.getId() == null) {
            throw new IllegalStateException("Integer av.id cannot be null");
        }
        if (av.getStringValue() == null) {
            throw new IllegalStateException("String av.stringValue cannot be null");
        }
        return (fullUrl ? this.domainAndContextPath : "") + DownloadFileController.FILE_DOWNLOAD_URL + "?" + av.getFileURL();
    }
    
    public String getUserListUrl() {
        return EditUsersController.USER_LISTING_URL;
    }
}
