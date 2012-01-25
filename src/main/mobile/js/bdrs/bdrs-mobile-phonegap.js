if (!window.bdrs) {
	bdrs = {};
}

bdrs.phonegap = {};

// phoneGap plugins
bdrs.phonegap.barcode = {};
  
bdrs.phonegap.isPhoneGapFlag = false;

/** Checks if the application is a phonegap app.
 *  @return boolean
 */
bdrs.phonegap.isPhoneGap = function() {
	return bdrs.phonegap.isPhoneGapFlag;
};

/** When phoneGap is fully loaded
 *  set phoneGapFlag
 */
bdrs.phonegap.onDeviceReady = function(){
	bdrs.phonegap.isPhoneGapFlag = true;
};

/**
 * Activate barcodescanner phonegap plugin.
 * We need to check which platform we are on because the differnt platforms use different api's.
 * TODO: upgrade barcodescanner plugin for android so the api can be generic. 
 */ 
bdrs.phonegap.barcode.scan = function(scanId){
	if(device.platform === "Android") {
		window.plugins.barcodeScanner.scan( BarcodeScanner.Type.QR_CODE, function(result) {
			jQuery("#record-attr-" + scanId.substr(11)).val(result);
	    }, function(error) {
	        bdrs.mobile.Warn("Scanning interrupted: " + error);
	    }, {yesString: "Install"});
	} else {
	//It is iOS
		window.plugins.barcodeScanner.scan(function(result) {
			if (result.cancelled) {
				bdrs.mobile.Debug("Scanning cancelled by the user");
			} else {
				jQuery("#record-attr-" + scanId.substr(11)).val(result.text);
			}
		}, function(error) {
			bdrs.mobile.Warn("Scanning interrupted: " + error);
		});
	}
};

