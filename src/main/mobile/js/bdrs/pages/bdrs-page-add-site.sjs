/**
 * Event handlers for the add site page. 
 */
exports.Create = function() {
	jQuery('#add-site-save').click(function (event) {
		record();
		jQuery.mobile.changePage("#sites", "slide", false, true);
	});
}
	
exports.Show = function() {
	var position;
	waitfor(position) {
		bdrs.mobile.geolocation.getCurrentPosition(resume);
	}
	if (position !== undefined) {
		jQuery('#add-site-latitude').val(bdrs.mobile.roundnumber(position.coords.latitude, 5));
		jQuery('#add-site-longitude').val(bdrs.mobile.roundnumber(position.coords.longitude, 5));
	}
}

exports.Hide = function() {
}

record = function() {
	bdrs.mobile.Debug ('Record called');
	
	bdrs.mobile.Debug(jQuery('#add-site-latitude').val());
	bdrs.mobile.Debug(jQuery('#add-site-longitude').val());
	bdrs.mobile.Debug(jQuery('#add-site-id').val());
	var site = new Location( {
		name: jQuery('#add-site-id').val(),
		latitude: jQuery('#add-site-latitude').val(),
		longitude: jQuery('#add-site-longitude').val()
	});

	var setting;
	waitfor(setting) {
		Settings.findBy('key', 'current-survey-id', resume);
	}
	var survey;
	waitfor(survey) {
		Survey.findBy('server_id', setting.value(), resume);
	}
	
	persistence.add(site);
	survey.locations().add(site);
	persistence.flush();
}
