/**
 * Event handlers for the load page. 
 */
bdrs.mobile.pages.splash = {
	Create: function() {
		// code to run when page first created and cached, probably most of page's event bindings here
		//bdrs.mobile.Debug('Splash Page Create');
		setTimeout(function(){
			jQuery.mobile.changePage("#login", jQuery.mobile.defaultPageTransition, false, true);
		},5000);
	},
	
	BeforeShow: function() {
		//bdrs.mobile.Debug("Splash Page Before Show");
	},
	
	Show: function() {
		// code to run when page shows, probably mostly "page state" resetting due to caching issues that can come up
		//bdrs.mobile.Debug('Splash Page Show');
	},
	
	Hide: function() {
		// code to run when user leaves the page
		//alert('Login Page Hide');
	}
};