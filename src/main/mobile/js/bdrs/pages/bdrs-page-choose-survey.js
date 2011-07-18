/**
 * Event handlers for the login page. 
 */
bdrs.mobile.pages.choose_survey = {
	Create: function() {
		// code to run when page first created and cached, probably most of page's event bindings here
		//alert('Login Page Create');
	},
	
	Show: function() {
		Survey.all().filter('local', '=', true).order('name', true).each(function(survey) {
			var surveyName = survey.name();
			var surveyId = survey.server_id();
			
			Queue.sync(function (queue) {
				bdrs.template.renderSync('surveyListItem', 
					{ name: survey.name(), description: survey.description(), id: survey.server_id() }, 
					'.bdrs-page-choose-survey .surveyList', 
					queue);
			});
			Queue.sync(function (queue) {
				jQuery('.bdrs-page-choose-survey .survey-' + survey.server_id()).click(function (event) {
					Queue.sync(function (queue) {
						Settings.findBy('key', 'current-survey' , function(setting) {
							if (setting === null) {
								var s = new Settings({ key : 'current-survey', value : surveyName });
								persistence.add(s);
							} else {
								jQuery(setting).data('value', surveyName);
							}
							jQuery('.dashboard-status').empty();
							bdrs.template.render('dashboard-status', { name : surveyName }, '.dashboard-status');
							queue.next();	
						});
					});
					Queue.sync(function(queue) {
						Settings.findBy('key', 'current-survey-id' , function(setting) {
							if (setting === null) {
								var s = new Settings({ key : 'current-survey-id', value : String(surveyId) });
								persistence.add(s);
							} else {
								jQuery(setting).data('value', String(surveyId));
							}
							queue.next();	
						});
					});
					Queue.sync(function(queue) {
						persistence.flush(function () {
							queue.next(); // release the queue before we redirect.
							history.back();
						});
					});
				});
				queue.next(); // outer use of queue.
			});
			
			Queue.sync(function (queue) {
			 	jQuery('.bdrs-page-choose-survey .surveyList').listview("refresh");
			 	queue.next();
			});
		});
	},
	
	Hide: function() {
		jQuery('.bdrs-page-choose-survey .surveyList').empty();
	}
};