/**
 * Event handlers for Dashboard page 
 */
bdrs.mobile.pages.dashboard = {

	Create: function() {
		
	},
	
	Show: function() {
//	    if(!bdrs.mobile.pages.clock_warning.isShown()) {
//	    	jQuery.mobile.changePage("#clock-warning", "slidedown");
//		}
				
		jQuery('.dashboard-status').empty();
		Settings.findBy('key', 'current-survey' , function(settings) {
			if (settings != null) {
				bdrs.template.render('dashboard-status', { name : settings.value }, '.dashboard-status');
			}
		});
	},
	
	Hide: function() {

	}
};
