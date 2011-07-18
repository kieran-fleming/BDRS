if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {};
}

bdrs.mobile.positionOptions = new PositionOptions();
bdrs.mobile.positionOptions.enableHighAccuracy = true;
bdrs.mobile.positionOptions.timeout = 5000;
bdrs.mobile.positionOptions.maximumAge = 0;

bdrs.mobile.geolocation = {
	getCurrentPosition: function(callback) {
		bdrs.mobile.Debug('Getting GPS Loc');
		navigator.geolocation.getCurrentPosition(callback, null, bdrs.mobile.positionOptions);
	}
};

bdrs.mobile.geolocation.watchId = navigator.geolocation.watchPosition(function(position) {
		//bdrs.mobile.Debug('Current Position Accuracy : ' + position.coords.accuracy);
	}, null, bdrs.mobile.positionOptions);