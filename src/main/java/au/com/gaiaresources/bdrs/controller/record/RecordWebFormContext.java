package au.com.gaiaresources.bdrs.controller.record;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.access.AccessDeniedException;

import au.com.gaiaresources.bdrs.model.record.Record;
import au.com.gaiaresources.bdrs.model.survey.Survey;
import au.com.gaiaresources.bdrs.model.user.User;

/**
 * Helper for holding constants used across the board for record forms
 * 
 * @author aaron
 *
 */
public class RecordWebFormContext {
    public static final String PARAM_EDIT = "editForm";
    public static final String MODEL_EDIT = "editEnabled";
    public static final String MODEL_WEB_FORM_CONTEXT = "recordWebFormContext";
    public static final String PARAM_PREVIEW = "preview";
    
    public static final String PARAM_SURVEY_ID = "surveyId";
    
    public static final String MSG_CODE_EDIT_AUTHFAIL = "bdrs.record.edit.authfail";
    public static final String MSG_CODE_VIEW_AUTHFAIL = "bdrs.record.view.authfail";
    
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
    
    public boolean isUnlockable() {
        return this.unlockable;
    }
    
    public boolean isExistingRecord() {
        return this.existingRecord;
    }
    
    public Integer getRecordId() {
        return this.recordId;
    }
    
    public Integer getSurveyId() {
        return this.surveyId;
    }
    
    public boolean isPreview() {
        return this.preview;
    }
}
