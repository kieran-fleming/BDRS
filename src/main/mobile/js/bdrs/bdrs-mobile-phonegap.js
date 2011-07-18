if (!window.bdrs) {
	bdrs = {};
}

bdrs.phonegap = {};
  
bdrs.phonegap.isPhoneGapFlag = false;


/** Checks if the application is a phonegap app.
 *  @return boolean
 */
bdrs.phonegap.isPhoneGap = function() {
	return bdrs.phonegap.isPhoneGapFlag;
}

/** When phoneGap is fully loaded
 *  set phoneGapFlag
 */
bdrs.phonegap.onDeviceReady = function(){
	bdrs.phonegap.isPhoneGapFlag = true;
}

