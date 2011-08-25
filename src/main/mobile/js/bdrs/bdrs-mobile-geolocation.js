if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {};
}


var BdrsPositionOptions = function() {
	/**
	 * Specifies the desired position accuracy.
	 */
	this.enableHighAccuracy = true;
	/**
	 * The timeout after which if position data cannot be obtained the errorCallback
	 * is called.
	 */
	this.timeout = 5000;
	/**
	 * 
	 */
	this.maximumAge = 0;
	
};

bdrs.mobile.positionOptions = new BdrsPositionOptions();

bdrs.mobile.geolocation = {
	getCurrentPosition: function(callback) {
		bdrs.mobile.Debug('Getting GPS Loc');
		navigator.geolocation.getCurrentPosition(callback, null, bdrs.mobile.positionOptions);
	}
};

bdrs.mobile.geolocation.watchId = navigator.geolocation.watchPosition(function(position) {
		//bdrs.mobile.Debug('Current Position Accuracy : ' + position.coords.accuracy);
	}, null, bdrs.mobile.positionOptions);