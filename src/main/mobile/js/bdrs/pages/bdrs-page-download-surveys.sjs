exports.Create = function() {
};

exports.Show = function() {
	// show download in progress dialog
	jQuery.mobile.pageLoading(false);
	waitfor(){
		bdrs.mobile.survey.getAllRemote(resume);
	}
	// hide download in progress dialog
	jQuery.mobile.pageLoading(true);
	
	var allSurveys = bdrs.mobile.survey.getAll();
	//Render surveys as list items
	for(var z=0; z<allSurveys.length; z++){
		var survey = allSurveys[z];
		waitfor(){
			bdrs.template.renderCallback('surveyListDownloadItem', {
			
			name : survey.name(),
			description : survey.description(),
			id : survey.server_id(),
			local: survey.local()},
			
			'.bdrs-page-download-surveys .surveyList', resume);
		}
	}
	
	//check the check-box for all surveys that exist on the device
	var localSurveys = bdrs.mobile.survey.getLocal();
	for (var i=0; i<localSurveys.length; i++) {
		jQuery('#checkbox-' + localSurveys[i].server_id()).prop('checked',true);
	}
	bdrs.mobile.restyle('.bdrs-page-download-surveys .surveyListContainer');
	
	// Add click Handler to surveys
	jQuery('.bdrs-page-download-surveys .downloadSurvey').change(function(event){
		var sid = jQuery(this).attr('survey-id');
		if (sid !== undefined) {
			bdrs.mobile.setParameter('deleting-survey-id', sid);
			var clickedSurvey = bdrs.mobile.survey.getByServerId(sid);
			if (clickedSurvey !== null && clickedSurvey.local() === true) {
				// The survey that was selecetd exists on the device, ask user to confirm delete.
				jQuery.mobile.changePage("#survey-delete-confirm", "slidedown");
			} else if(clickedSurvey !== null && clickedSurvey.local() === false) {
				jQuery.mobile.pageLoading(false);
				// get surveydata from server
				var result = bdrs.mobile.survey.getRemote(sid);
				if(result.errorMsg === undefined) {
					// persist surveydata in local survey 
					var survey = bdrs.mobile.survey.save(sid, result);
					if (survey.errorMsg === undefined) {
						// make the survey default
						bdrs.mobile.survey.makeDefault(survey);
						//add class local to survey listitem
						jQuery(this).addClass('local')
					} else {
						bdrs.mobile.Error(survey.errorMsg);
					}
				} else {
					bdrs.mobile.Error(result.errorMsg);
				}
			} else {
				bdrs.mobile.Error("Could not find survey with server_id = " + sid);
			}
		} else {
			bdrs.mobile.Error("The clickhandler of the survey failed to retrieve the value of the 'survey-id attribute.");
		}
	});
};

/* 
 * Removes the survey List from the DOM
 */
exports.Hide = function() {
	jQuery('.bdrs-page-download-surveys .surveyList').empty();
};