/**
 * Event handlers for the login page. 
 */

exports.Init = function() {
	
	bdrs.mobile.Debug("Init Login Page");
	
	bdrs.template.renderCallback('loginWelcome', {}, '#loginWelcome', function(){
		bdrs.template.restyle('#login');
	});
	bdrs.template.renderCallback('portalNameField', {}, '#portalNameField', function(){
		bdrs.template.restyle('#login');
	});
	bdrs.template.renderCallback('portalUrlField', {}, '#portalUrlField', function(){
		bdrs.template.restyle('#login');
	});
	
    jQuery(".bdrs-page-login #login_button").click(function(event) {
        jQuery.jsonp({
                url: jQuery(".bdrs-page-login #url").val() + "/webservice/user/validate.htm",
                data: jQuery(".bdrs-page-login #login_form").serializeArray(),
                type: 'POST',
                callbackParameter: 'callback',
                error: function(jqXHR, textStatus, errorThrown) {
                    var trans = jQuery.mobile.defaultPageTransition === 'none' ? 'none' : 'slidedown';
                    jQuery.mobile.changePage("#login_error", {changeHash: false, transition: trans});
                },
                success: function(data) {
                    bdrs.mobile.Debug(JSON.stringify(data));
                    
                    // stuff into database
                    var server_url = jQuery(".bdrs-page-login #url").val();
                    // If the URL ends with a slash, don't add another one, otherwise add one.
                    server_url = server_url[server_url.length-1] == '/' ? server_url : server_url + '/';
                    server_url = server_url + "portal/" + data.portal_id;
                    var t = new User({  name : data.user.name, 
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
                    
					var s = new Settings({ key : 'user-logged-in', value : 'true' });
					persistence.add(s);
                    jQuery.mobile.changePage("#dashboard", {showLoadMsg: false});
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
            	jQuery.mobile.changePage("#dashboard", {showLoadMsg: false});
            }
        }
    });
    
};

exports.BeforeShow = function() {
	bdrs.mobile.Debug("BeforeShow Login Page");
};

exports.Show = function() {
	bdrs.mobile.Debug("Show Login Page");
	
};