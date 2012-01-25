/**
 * Event handlers for the load page. 
 */
bdrs.mobile.pages.splash = {
	Init: function() {
		// code to run when page first created and cached, probably most of page's event bindings here
		bdrs.mobile.Debug('Splash Page Init');
	},
	
	BeforeShow: function() {
		bdrs.mobile.Debug("Splash Page Before Show");
		jQuery('#splash').hide();
	},
	
	Show: function() {
		// code to run when page shows, probably mostly "page state" resetting due to caching issues that can come up
		bdrs.mobile.Debug('Splash Page Show');
		setTimeout("jQuery('#splash').fadeIn(1500)",1500);
		
	},
	
	Hide: function() {
		// code to run when user leaves the page
		bdrs.mobile.Debug('Splash Page Hide');
	}
};