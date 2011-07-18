/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}
	
exports.Show = function() {
    
    var records;
    waitfor(records) {
	    Record.all().prefetch('species').prefetch('censusMethod').filter('parent', '=', null).order('when', false).list(resume);
    };
    
    var data = '';
	
    for(var i=0; i<records.length; i++) {
        rec = records[i];
        
        data += '\n' + records[i].latitude() + ',' + records[i].longitude() + '<br/>'; 
    }
	jQuery('.bdrs-page-dump-data .data').html(data);
	
	// Guard for HTML5 only version.
	if (bdrs.mobile.fileMgrExists()) {
		var writeSuccess = function(evt) {
			bdrs.mobile.Debug("Write has succeeded");
		};
		
		var paths = navigator.fileMgr.getRootPaths();
		var writer = new FileWriter(paths[0] + "bdrs-mobile.txt");
		writer.onwrite = writeSuccess;
		writer.write(data);
	} else {
	    bdrs.mobile.Debug("Dump data not executed. No file manager.");
	}
}
	
exports.Hide = function() {
}