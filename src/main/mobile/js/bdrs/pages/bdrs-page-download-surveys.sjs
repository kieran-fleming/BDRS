exports.Init = function() {
	bdrs.mobile.Debug("Surveylist Init");
};

exports.BeforeShow = function() {
	bdrs.mobile.Debug("SurveyList BEFORESHOW");
};

exports.Show = function() {
	bdrs.mobile.Debug("SurveyList SHOW");
	jQuery('.bdrs-page-download-surveys').hide();
	// show download in progress dialog
	jQuery.mobile.loadingMessage = "Trying to retrieve surveys from the server...";
	jQuery.mobile.showPageLoadingMsg();
	waitfor(){
		bdrs.mobile.survey.getAllRemote(resume);
	}
	var allSurveys = bdrs.mobile.survey.getAll();
	//Render surveys as list items
	var count = 0;
	if (allSurveys.length > 0) {
		jQuery('#noSurvey').hide();
		for(var z=0; z<allSurveys.length; z++){
			var survey = allSurveys[z];
			bdrs.template.renderCallback('surveyListDownloadItem', {
				name : survey.name(),
				description : survey.description(),
				id : survey.server_id(),
				local: survey.local()},
			'.bdrs-page-download-surveys .surveyList', function(){
					count = count +1;
					if (count === allSurveys.length) {
						// hide download in progress dialog
						jQuery.mobile.hidePageLoadingMsg();
						//check the check-box for all surveys that exist on the device
						var localSurveys = bdrs.mobile.survey.getLocal();
						for (var i=0; i<localSurveys.length; i++) {
							jQuery('#checkbox-' + localSurveys[i].server_id()).prop('checked',true);
						}
						
						jQuery('.bdrs-page-download-surveys').fadeIn('fast');
						 bdrs.template.restyle('.bdrs-page-download-surveys .surveyListContainer');
						jQuery('.surveyListContainer .ui-checkbox .ui-icon').css('top', '50%');
						
						// Add click Handler to surveys
						jQuery('.bdrs-page-download-surveys .downloadSurvey').change(function(event){
							var sid = jQuery(this).attr('survey-id');
							if (sid !== undefined) {
								bdrs.mobile.setParameter('deleting-survey-id', sid);
								var clickedSurvey = bdrs.mobile.survey.getByServerId(sid);
								if (clickedSurvey !== null && clickedSurvey.local() === true) {
									// The survey that was selecetd exists on the device, ask user to confirm delete.
									jQuery.mobile.changePage("#survey-delete-confirm", {showLoadMsg: false, transition: "slidedown"});
								} else if(clickedSurvey !== null && clickedSurvey.local() === false) {
									jQuery.mobile.loadingMessage = "Retrieving survey content";
									jQuery.mobile.showPageLoadingMsg();
									// get surveydata from server
									var result = bdrs.mobile.survey.getRemote(sid);
									// persist surveydata in local survey 
									var survey = bdrs.mobile.survey.save(sid, result);
									if (survey !== null) {
										// make the survey default
										bdrs.mobile.survey.makeDefault(survey);
										//add class local to survey listitem
										jQuery(this).addClass('local')
									}
								} else {
									bdrs.mobile.Error("Could not find survey with server_id = " + sid);
								}
							} else {
								bdrs.mobile.Error("The clickhandler of the survey failed to retrieve the value of the 'survey-id attribute.");
							}
						});
					}
			});
		}
	} else {
		bdrs.mobile.Debug("there are no surveys");
		jQuery('.bdrs-page-download-surveys').fadeIn('fast');
		// hide download in progress dialog
		jQuery.mobile.hidePageLoadingMsg();
		//show message to user to check connection etc..
		jQuery('#noSurvey').show();
	}
	jQuery.mobile.loadingMessage = "Loading";
};

/* 
 * Removes the survey List from the DOM
 */
exports.Hide = function() {
	jQuery('.bdrs-page-download-surveys .surveyList').empty();
};