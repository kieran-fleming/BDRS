exports.start = function() {
	
	document.addEventListener("deviceready", bdrs.phonegap.onDeviceReady, true);
	
	bdrs.template.render('footer', null, ".footer");
	bdrs.template.render('header', null, ".header");
	
	//Add download app links if app is available
	if(bdrs.mobile.getParameterByName("hasApp")){
		//link on login page
		jQuery('#appLink').fadeIn('600').show('highlight');
		// link in settings menu
		jQuery('#settings ul').append("<li id=\"#settingsAppLink\"><a href=\"application.htm\" rel=\"external\"><h3>Install application</h3><p>Install as a native application</p></a></li>");
	}

	jQuery(".bdrs-page-login #login_button").click(function(event) {
		console.log("set login button link");
		jQuery.jsonp({
				url: jQuery(".bdrs-page-login #url").val() + "/webservice/user/validate.htm",
				data: jQuery(".bdrs-page-login #login_form").serializeArray(),
				type: 'POST',
				callbackParameter: 'callback',
				error: function(jqXHR, textStatus, errorThrown) {
					jQuery.mobile.changePage("#login_error", "slidedown", false, true);
				},
				success: function(data) {
					bdrs.mobile.Debug(JSON.stringify(data));
					
					// stuff into database
					var server_url = jQuery(".bdrs-page-login #url").val();
					// If the URL ends with a slash, don't add another one, otherwise add one.
					server_url = server_url[server_url.length-1] == '/' ? server_url : server_url + '/';
					server_url = server_url + "portal/" + data.portal_id;
					var t = new User({ 	name : data.user.name, 
										ident : data.ident,
										firstname: data.user.firstName,
										lastname : data.user.lastName,
										server_id : data.user.server_id,
										portal_id : data.portal_id,
										server_url: server_url });
					// add user locations to user
					for (var i = 0; i < data.location.length; i++){
						var location = data.location[i];
						var point = new bdrs.mobile.Point(location.location);
						t.locations().add(new Location({
							server_id : location.server_id,
							name : location.name,
							latitude : point.getLatitude(),
							longitude : point.getLongitude() }));
					}
					persistence.add(t);
					persistence.flush();
					bdrs.mobile.User = t;
					// change the page
					jQuery.mobile.changePage("#dashboard", "slide", false, true);
				}
		});
	});
	
	User.all().count(function(number) { 
			if (number > 0) { 
				User.all().one(function(user) {
					bdrs.mobile.User = user;
				});
			
				if (jQuery(jQuery.mobile.activePage).attr('data-url') == 'dashboard') {
					bdrs.mobile.pages.dashboard.Show();
				} else {
					jQuery.mobile.changePage("#dashboard", "slide", false, true);
				}
			}
	});
	
	jQuery.datepicker.setDefaults({dateFormat : 'dd M yy'});
};