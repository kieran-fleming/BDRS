/**
 *  
 * @return Instance of DirectoryListing
 */
var BDRSPlugin = function() { 
}
 
/**
 * @param directory The directory for which we want the listing
 * @param successCallback The callback which will be called when directory listing is successful
 * @param failureCallback The callback which will be called when directory listing encouters an error
 */
BDRSPlugin.prototype.testAction = function(arg, successCallback, failureCallback) {
	return PhoneGap.exec(successCallback,   //Callback which will be called when directory listing is successful
		failureCallback,     				//Callback which will be called when directory listing encounters an error
		'BDRSPlugin',  			//Telling PhoneGap that we want to run "DirectoryListing" Plugin
		'testAction',              				//Telling the plugin, which action we want to perform
		[]);        				//Passing a list of arguments to the plugin, in this case this is the directory path
};
 
/**
 * @param directory The directory for which we want the listing
 * @param successCallback The callback which will be called when directory listing is successful
 * @param failureCallback The callback which will be called when directory listing encouters an error
 */
BDRSPlugin.prototype.exit = function(arg, successCallback, failureCallback) {
	return PhoneGap.exec(successCallback,   //Callback which will be called when directory listing is successful
		failureCallback,     				//Callback which will be called when directory listing encounters an error
		'BDRSPlugin',  			//Telling PhoneGap that we want to run "DirectoryListing" Plugin
		'exit',              				//Telling the plugin, which action we want to perform
		[]);        				//Passing a list of arguments to the plugin, in this case this is the directory path
};
 
/**
 * <ul>
 * <li>Register the Directory Listing Javascript plugin.</li>
 * <li>Also register native call which will be called when this plugin runs</li>
 * </ul>
 */
PhoneGap.addConstructor(function() {
	
	//Register the javascript plugin with PhoneGap
	PhoneGap.addPlugin('bdrs', new BDRSPlugin());
	 
	//Register the native class of plugin with PhoneGap
	PluginManager.addService("BDRSPlugin","au.com.gaiaresources.bdrs.mobile.android.BDRSPlugin");
});