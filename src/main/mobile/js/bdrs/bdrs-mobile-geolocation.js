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
	this.maximumAge = 10000;
	
};

bdrs.mobile.positionOptions = new BdrsPositionOptions();

bdrs.mobile.geolocation = {};
bdrs.mobile.geolocation.watchid;
bdrs.mobile.geolocation.currentPosition = {};
bdrs.mobile.geolocation.getCurrentPosition = function(callback, error) {
		if (bdrs.mobile.geolocation.watchId !== undefined && bdrs.mobile.geolocation.currentPosition !== {} ){
			//We have a watch set and the position is stored and updated
			callback(bdrs.mobile.geolocation.currentPosition);
		}else {
			//We have no watch, so have to create one.
			bdrs.mobile.geolocation.watchId = navigator.geolocation.watchPosition(function(position) {
				bdrs.mobile.geolocation.currentPosition = position;
				callback(position);
			}, function(){
				if(error){
					bdrs.mobile.Error('Could not get coordinates');
				}else{
					bdrs.mobile.geolocation.disable();
					bdrs.mobile.geolocation.getCurrentPosition(callback, true);
				}
			}, bdrs.mobile.positionOptions);
		}
};
bdrs.mobile.geolocation.disable = function(){
	bdrs.mobile.Info('Disabling geolocation');
	navigator.geolocation.clearWatch(bdrs.mobile.geolocation.watchId);
	bdrs.mobile.geolocation.watchId = undefined;
	bdrs.mobile.geolocation.currentPosition = {};
};
bdrs.mobile.geolocation.enable = function(){
	bdrs.mobile.geolocation.getCurrentPosition(function(){});
};

/**
 * Sets a GPS watch as we start the app
 */
bdrs.mobile.geolocation.enable();