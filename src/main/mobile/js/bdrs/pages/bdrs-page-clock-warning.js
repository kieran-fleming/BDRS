
/**
 * Event handlers for Dashboard page 
 */
bdrs.mobile.pages.clock_warning = {

    _isShown: false,

	Create: function() {},
	
	Show: function() {
	    jQuery("#clock-warning-current-datetime").text(new Date().toString());
	},
	
	Hide: function() {
    	bdrs.mobile.pages.clock_warning._isShown = true;
	},
	
	isShown: function() {
	    return bdrs.mobile.pages.clock_warning._isShown;
    }
};
