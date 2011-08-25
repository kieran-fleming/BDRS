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
}

/** When phoneGap is fully loaded
 *  set phoneGapFlag
 */
bdrs.phonegap.onDeviceReady = function(){
	bdrs.phonegap.isPhoneGapFlag = true;
}

bdrs.phonegap.barcode.scan = function(scanId){
	window.plugins.barcodeScanner.scan( BarcodeScanner.Type.QR_CODE, function(result) {
		jQuery("#record-attr-" + scanId.substr(11)).val(result);
    }, function(error) {
        bdrs.mobile.Warn("Scanning interrupted: " + error);
    }, {yesString: "Install"});
}



