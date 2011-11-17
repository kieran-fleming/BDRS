exports.DELETE_SURVEY_CONFIRM_BUTTON_SELECTOR = "#survey-delete-confirm-button";

exports.Create = function() {
	jQuery(exports.DELETE_SURVEY_CONFIRM_BUTTON_SELECTOR).click(function() {
        var page = jQuery('#survey-delete-confirm');
        var sid = bdrs.mobile.getParameter('deleting-survey-id');
    	var removeResult = bdrs.mobile.survey.remove(sid);
    	if(removeResult === true) {
    		bdrs.mobile.Debug("Succesfully removed  survey with server_id = " + sid);
    	} else {
    		bdrs.mobile.Error(removeResult.errorMsg);
    	}
    	jQuery.mobile.pageLoading(true);
        page.dialog('close');
    });
};
    
exports.Show = function() {
};

exports.Hide = function() {
	var sid = bdrs.mobile.getParameter('deleting-survey-id');
	waitfor(survey) {
		Survey.findBy('server_id', sid, resume);
	}
	jQuery('#checkbox-' + sid).prop('checked', survey.local());
	bdrs.mobile.restyle('.bdrs-page-download-surveys .surveyListContainer');
	
};