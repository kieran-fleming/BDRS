exports.start = function() {
	
    //Add download app links if app is available
/*    if(bdrs.mobile.getParameterByName("hasApp")){
        //link on login page
        jQuery('#appLink').fadeIn('600').show('highlight');
        // link in settings menu
        jQuery('#settings ul').append("<li id=\"#settingsAppLink\"><a href=\"application.htm\" rel=\"external\"><h3>Install application</h3><p>Install as a native application</p></a></li>");
    }*/
	
    jQuery.datepicker.setDefaults({dateFormat : 'dd M yy'});
    
    // Phonegap events.
    document.addEventListener("menubutton", function() {
        window.plugins.bdrs.exit();
    }, false);
    document.addEventListener("pause", function() {
        bdrs.mobile.geolocation.disable();
    }, false);
    document.addEventListener("resume", function() {
    	bdrs.mobile.geolocation.enable();
    }, false);
    
    /* Onload there is no hastag unless someone has been trying to reload on a page with a hashtag.
     * In that case set location to origin + path without hashtag en reload.
     */
	if(window.location.hash !== ""){
		window.location.href = window.location.origin + window.location.pathname;
	}
    
    //redirect user depending on login status
	var theUser;
	waitfor(theUser) {
		User.all().one(resume);
	}
	bdrs.mobile.User = theUser;
	if (bdrs.mobile.User === null || bdrs.mobile.User === undefined) {
		jQuery.mobile.changePage("#login", {changeHash: false, transition: "fade", showLoadMsg: false});
	} else {
		jQuery.mobile.changePage("#dashboard", {changeHash: false, transition: "fade", showLoadMsg: false});
	}
};
