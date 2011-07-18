/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}
	
exports.Show = function() {
	bdrs.mobile.Debug('site show');
	var site;
	waitfor(site) {
		Location.load(bdrs.mobile.getParameter('siteId'), resume);
	}
	
	var setting;
	waitfor(setting) {
		Settings.findBy('key', 'current-survey-id', resume);
	}
	var survey;
	waitfor(survey) {
		Survey.findBy('server_id', setting.value(), resume);
	}
	
	bdrs.mobile.Debug('Getting methods');
	var methodList;
	waitfor(methodList) {
		CensusMethod.all().filter('survey', '=', survey).order('type').list(resume); 
	}
	bdrs.mobile.Debug('Got methods : ' + methodList.length);
	
	var type;
	for (var i = 0; i < methodList.length; i++) {
		if ((type == undefined) || (type != methodList[i].type())) {
			waitfor() {
				type = methodList[i].type();
				if (bdrs.mobile.getParameter('censusType') == undefined) {
					bdrs.mobile.setParameter('censusType', type);
				}
				bdrs.template.renderCallback( 'censusTypeCheckbox',
					{ id : methodList[i].type(), name : methodList[i].type() },
					'.bdrs-page-site .censusControlGroup',
					resume);
			}
			jQuery(".bdrs-page-site .censusControlGroup #site-type-" + methodList[i].type()).checkboxradio();
		} 
		
		var Callback =  function(id) {
			var id = id;
			
			this.handler = function(event) {
				bdrs.mobile.setParameter('censusMethodId', id);
				bdrs.mobile.Debug('set current census method to : ' + id);
				// at this stage, we have set the site, the census method, and we should be able to head off to the recording form.
				jQuery.mobile.changePage("#record", "slide", false, true);
			}
			return this;
		};
	
		if (methodList[i].type() == bdrs.mobile.getParameter('censusType')) {
			// only add the selected census types to the page.
			waitfor() { 
				bdrs.template.renderCallback(
					'methodListItem', 
					{id : methodList[i].id, title : methodList[i].name(), description: methodList[i].description() }, 
					'.bdrs-page-site .methodList', 
					resume);
			}
			jQuery('.bdrs-page-site .methodList .method-' + methodList[i].id).click(new Callback(methodList[i].id).handler);
		}
	}

	jQuery('.bdrs-page-site .censusControlGroup input[type=radio]').change(function(event) {
		bdrs.mobile.setParameter('censusType', jQuery('.bdrs-page-site .censusControlGroup input[type=radio]:checked').val());
		bdrs.mobile.pages.site.Hide();
		bdrs.mobile.pages.site.Show();
	});
	
	jQuery(".bdrs-page-site .censusControlGroup").controlgroup();
	
	// select the correct button.
	bdrs.mobile.Debug('calling refresh...');
	jQuery('.bdrs-page-site #site-type-' + bdrs.mobile.getParameter('censusType')).attr('checked', true).checkboxradio('refresh');
	
	jQuery('.bdrs-page-site .methodList').listview("refresh");
}
	
exports.Hide = function() {
	jQuery('.bdrs-page-site .censusControlGroup').empty();
	jQuery('.bdrs-page-site .methodList').empty();
}