/**
 * Does a persistenceJS dump as JSON.
 * The JSON gets stored in a file on the sdcard.
 */
exports.backupToJSON = function() {
	
	jQuery.mobile.pageLoading(false);
	
	var paths = navigator.fileMgr.getRootPaths();
	var location = paths[0] + "external_sd/bdrsMobileBu.txt";
	var writer = new FileWriter(location);
	writer.onwrite = function(){
		console.log("backup was succesfull");
		jQuery.mobile.pageLoading(true);
	};
	
	writer.onfail = function(){
		console.log("backup failed");
		jQuery.mobile.pageLoading(true);
	}
	
	persistence.dumpToJson(function(dump) {
		console.log("about to write the file");
		writer.write(dump);
	});
	
},

exports.restoreFromJSON = function() {
	var paths = navigator.fileMgr.getRootPaths();
	var location = paths[0] + "external_sd/bdrsMobileBu.txt";
    var reader = new FileReader();
    reader.onload = function(evt){
    	console.log("LOAD: " + evt.target.result);
    	persistence.loadFromJson(evt.target.result, function(){
    		alert("Dump restored");
    	});
    };
    reader.onerror = function(evt){
    	console.log("ERROR: " + evt.target.error.code);
    };
    reader.readAsText(location);
    
}
