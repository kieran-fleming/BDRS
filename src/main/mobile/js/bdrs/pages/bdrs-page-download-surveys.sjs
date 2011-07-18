exports.Create = function() {
	
	bdrs.mobile.survey.getAllRemote();
},

exports.Show = function() {
	
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
	
	// check all checkboxes with class local
	jQuery('.bdrs-page-download-surveys .local').attr('checked', 'true');
	
	// Yes, We are crazy.
	jQuery('.bdrs-page-download-surveys .surveyListContainer').remove().appendTo(jQuery('.bdrs-page-download-surveys')).page();
	
	// Add click Handler to surveys
	jQuery('.bdrs-page-download-surveys .downloadSurvey').change(function(event){
		
		var sid = jQuery(this).attr('survey-id');
		
		if (jQuery(this).hasClass('local')) {
			// The survey that was selecetd exists on the device, ask user to confirm delete.
			if (confirm("Do you really want to remove this survey from your device?")) {
				// TODO: Delete all the data related to the selected survey
				persistence.flush(function() {
					// removes local class from downloaded survey
					jQuery('.bdrs-page-download-surveys label[for="checkbox-' + sid + '"]').removeClass('local');
				});
			} else {
				// TODO: re-check checkbox
			}
		} else {
			// show download in progress dialog
			jQuery.mobile.pageLoading(false);
			// get surveydata from server
			var result = bdrs.mobile.survey.getRemote(sid);
			if(result){
				// persist surveydata in local survey 
				var survey = bdrs.mobile.survey.save(sid, result);
				if (survey){
					// make the survey default
					bdrs.mobile.survey.makeDefault(survey);
				}else{
					// TODO: show user error while saving survey
				}
			}else{
				//TODO: show user error while retrieving survey data from server
			}
			// hide download in progress dialog
			jQuery.mobile.pageLoading(true);
		}

	});
},

/* 
 * Removes the survey List from the DOM
 */
exports.Hide = function() {
	jQuery('.bdrs-page-download-surveys .surveyList').empty();
}