/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}
	
exports.Show = function() {
	
    var survey = bdrs.mobile.survey.getDefault();

	var methods;
	waitfor(methods) {
	    survey.censusMethods().order('name', true).list(resume);
	}
	
	var Callback =  function(censusMethodId) {
        var methodId = censusMethodId;
        
        this.handler = function(event) {
        	bdrs.mobile.setParameter('censusMethodId', methodId);
			jQuery.mobile.changePage("#record", jQuery.mobile.defaultPageTransition, false, true);
        }
        return this;
	};

	for (var i = 0; i < methods.length; i++) {
		waitfor() {
			bdrs.template.renderCallback(
				'methodListItem', 
				{id: methods[i].id, title : methods[i].name(), description: methods[i].description() }, 
				'.bdrs-page-censusmethods .censusMethodList', 
				resume);
		}
		jQuery('.bdrs-page-censusmethods #'+methods[i].id).click(new Callback(methods[i].id).handler);
	}
	jQuery('.bdrs-page-censusmethods .censusMethodList').listview("refresh");
}
	
exports.Hide = function() {
	jQuery('.bdrs-page-censusmethods .censusMethodList').empty();
}
