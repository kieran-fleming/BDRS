
/**
 * Event handlers for Dashboard page 
 */
bdrs.mobile.pages.clock_warning = {

    _isShown: false,

	Init: function() {},
	
	Show: function() {
	    jQuery("#clock-warning-current-datetime").text(new Date().toString());
	},
	
	Hide: function() {
    	bdrs.mobile.pages.clock_warning._isShown = true;
    	jQuery.mobile.changePage("#dashboard", {showLoadMsg: false, changeHash: false});
    	bdrs.template.restyle('#dashboard');
	},
	
	isShown: function() {
	    return bdrs.mobile.pages.clock_warning._isShown;
    }
};
