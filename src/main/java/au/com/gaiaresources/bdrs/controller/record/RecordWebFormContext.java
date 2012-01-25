package au.com.gaiaresources.bdrs.controller.record;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

import au.com.gaiaresources.bdrs.config.AppContext;
import au.com.gaiaresources.bdrs.controller.RenderController;
import au.com.gaiaresources.bdrs.model.method.CensusMethod;
import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;
import au.com.gaiaresources.bdrs.service.web.RedirectionService;

/**
 * Helper for holding constants used across the board for record forms
 * 
 * @author aaron
 *
 */
public class RecordWebFormContext {
    
    /**
     * Request param - requests for an editable form
     */
    public static final String PARAM_EDIT = "editForm";
    /**
     * Model key - is the form editable
     */
    public static final String MODEL_EDIT = "editEnabled";
    /**
     * Model key - for the RecordWebFormContext
     */
    public static final String MODEL_WEB_FORM_CONTEXT = "recordWebFormContext";
    /**
     * Request param - is the form in preview mode
     */
    public static final String PARAM_PREVIEW = "preview";
    /**
     * Request param - the survey to open the form with.
     */
    public static final String PARAM_SURVEY_ID = "surveyId";
    /**
     * Msg code - cannot edit form due to auth failure
     */
    public static final String MSG_CODE_EDIT_AUTHFAIL = "bdrs.record.edit.authfail";
    /**
     * Msg code - cannot view form due to auth failure
     */
    public static final String MSG_CODE_VIEW_AUTHFAIL = "bdrs.record.view.authfail";
    /**
     * Request param - passed when the POST should redirect to a blank version of the same
     * form that has just been posted
     */
    public static final String PARAM_SUBMIT_AND_ADD_ANOTHER = "submitAndAddAnother";
    /**
     * Request param - redirect url to use after record form post.
     */
    public static final String PARAM_REDIRECT_URL = "redirecturl";
        
    /**
     * Url for survey redirect, see RenderController. This needs refactoring !
     * This actually causes a compile time circular dependency but java can compile anyway so...
     * it's ok for now ?
     */
    public static final String SURVEY_RENDER_REDIRECT_URL = RenderController.SURVEY_RENDER_REDIRECT_URL;
    
    /**
     * Request param - census method ID used to open the form.
     */
    public static final String PARAM_CENSUS_METHOD_ID = "censusMethodId";
    
    // From MySightingsController - the query parameter record ID.
    // Not refering to MySightings directly here since we may introduce
    // cyclic dependencies.
    public static final String MODEL_RECORD_ID = "record_id";
    
    private boolean editable;
    private boolean existingRecord;
    private boolean unlockable;
    private boolean preview;
    private Integer recordId;
    private Integer surveyId;
    
    /**
     * Create a new web form context for use with the form GET handler
     * 
     * Will throw AccessDeniedExceptions if appropriate.
     * 
     * @param request - the HttpServletRequest used when requesting the form
     * @param recordToLoad - The record requested to load, can be null
     * @param accessingUser - the User attempting to access the web form, can be null
     * @param survey - the survey that for the record. passing it in as a separate parameter as sometimes there is no
     * record to edit e.g. when previewing a form
     */
    public RecordWebFormContext(HttpServletRequest request, Record recordToLoad, User accessingUser, Survey survey) {
        // if the record is non persisted, we always want to edit.
        if (recordToLoad == null || recordToLoad.getId() == null) {
            editable = true;
        } else {
            // otherwise, check whether an edit has been requested.
            String reqString = request.getParameter(PARAM_EDIT);
            editable = reqString != null ? Boolean.parseBoolean(reqString) : false;   
        }
        
        // check whether the form is in preview mode...
        preview = request.getParameter(PARAM_PREVIEW) != null;
        
        recordId = recordToLoad != null ? recordToLoad.getId() : null;
        surveyId = survey != null ? survey.getId() : null;
        existingRecord = recordToLoad != null && recordToLoad.getId() != null;
        unlockable = existingRecord && recordToLoad.canWrite(accessingUser);
        
        recordAccessSecurityCheck(recordToLoad, accessingUser, editable);
    }
    
    /**
     * Create a new web form context for use with the form POST handler
     * 
     * Will throw AccessDeniedExceptions if appropriate
     * 
     * @param recordToSave - The record requested to save, can be null
     * @param writingUser - the User attempting to save the web form, can be null
     */
    public RecordWebFormContext(Record recordToSave, User writingUser) {
        // we are posting so editable must always be true.
        editable = true;
        recordAccessSecurityCheck(recordToSave, writingUser, editable);
    }
    
    /**
     * Helper method to throw AccessDeniedExceptions if the record cannot be edited or viewed.
     * The exceptions are handled in HandlerExceptionResolver.java
     * 
     * @param record - the record we are attempting to access. Can be nullable
     * @param loggedInUser - the user that is attempting to access the record. can be null
     * @param editEnabled - whether the record is being edited or viewed
     */
    private static void recordAccessSecurityCheck(Record record, User loggedInUser, boolean editEnabled) {
        if (record == null) {
            record = new Record();
        }
        // Check whether we can write or not...
        if (editEnabled) {
            if (!record.canWrite(loggedInUser)) {
                throw new AccessDeniedException(MSG_CODE_EDIT_AUTHFAIL);
            }   
        } else {
            if (!record.canView(loggedInUser)) {
                throw new AccessDeniedException(MSG_CODE_VIEW_AUTHFAIL);
            }
        }
        // if security pass is successful no exceptions will be thrown
    }
    
    /**
     * Is this web form editable (vs in view mode)
     * 
     * @return true if edit mode enabled, false otherwise
     */
    public boolean isEditable() {
        return this.editable;
    }
    
    /**
     * Is the accessing user capable of unlocking the requested record
     * @return boolean
     */
    public boolean isUnlockable() {
        return this.unlockable;
    }
    
    /**
     * Whether the record requested was an existing record or not
     * @return boolean
     */
    public boolean isExistingRecord() {
        return this.existingRecord;
    }
    
    /**
     * The record ID requested to be opened in this form
     * @return Integer record ID
     */
    public Integer getRecordId() {
        return this.recordId;
    }
    
    /**
     * The survey id to be opened for this form
     * @return Integer survey ID
     */
    public Integer getSurveyId() {
        return this.surveyId;
    }
    
    /**
     * Is the form being opened for preview
     * @return boolean
     */
    public boolean isPreview() {
        return this.preview;
    }
    
    /**
     * hacky helper method. Used to add the record ID onto the model and view
     * for highlighting after the redirect.
     * 
     * @param mv ModelAndView
     * @param r Record
     */
    public static void addRecordHighlightId(ModelAndView mv, Record r) {
        if (r != null) {
            mv.addObject(MODEL_RECORD_ID, r.getId());
        }
    }
        
    /**
     * Returns the Redirect model and view
     * 
     * @param request - HttpServletRequest from record form post
     * @param r - the persisted record
     * @return ModelAndView
     */
    public static ModelAndView getSubmitRedirect(HttpServletRequest request, Record r) {
        
        Survey survey = r.getSurvey();
        CensusMethod cm = r.getCensusMethod();
        RedirectionService redirectionService = AppContext.getBean(RedirectionService.class);
        
        ModelAndView mv;
        if (request.getParameter(PARAM_SUBMIT_AND_ADD_ANOTHER) != null) {
            mv = new ModelAndView(new RedirectView(
                    SURVEY_RENDER_REDIRECT_URL, true));
            mv.addObject(PARAM_SURVEY_ID, survey.getId());
            if (cm != null) {
                mv.addObject(PARAM_CENSUS_METHOD_ID, cm.getId());   
            }
        } else {
            // Normal submit case:
            if (request.getSession().getAttribute(PARAM_REDIRECT_URL) != null) {
                mv = new ModelAndView("redirect:"
                        + request.getSession().getAttribute(PARAM_REDIRECT_URL));
            } else if (request.getParameter(PARAM_REDIRECT_URL) != null) { 
                mv = new ModelAndView("redirect:"
                        + request.getParameter(PARAM_REDIRECT_URL));
            } else {
                switch (survey.getFormSubmitAction()) {
                case STAY_ON_FORM:
                    mv = new ModelAndView(new RedirectView(redirectionService.getViewRecordUrl(r), true));   
                    break;
                case MY_SIGHTINGS:
                default:
                    mv = new ModelAndView(new RedirectView(redirectionService.getMySightingsUrl(survey), true));
                    // highlight the record that has been created...
                    RecordWebFormContext.addRecordHighlightId(mv, r);
                    break;
                }
            }
        }
        return mv;
    }
}
