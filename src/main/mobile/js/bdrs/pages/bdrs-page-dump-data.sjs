/**
 * Event handlers for the record page. 
 */
exports.Init =  function() {
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
	if (bdrs.mobile.fileIO.exists()) {
		var d = new Date();
	    var filepath = [
	        d.getFullYear(),
	        bdrs.mobile.zerofill((d.getMonth()+1), 2),
	        bdrs.mobile.zerofill(d.getDate(),2),
	        '_',
	        bdrs.mobile.zerofill(d.getHours(),2),
	        bdrs.mobile.zerofill(d.getMinutes(),2),
	        bdrs.mobile.zerofill(d.getSeconds(),2)
	    ].join('');
	    filepath = "bdrs/dump-data/" + filepath + ".txt";

	    bdrs.mobile.Debug("File will be written to: " + filepath);
	    bdrs.mobile.Debug("Writing File");
	    waitfor() {
	        bdrs.mobile.fileIO.writeFile(filepath, data, resume, resume);
	    }
	    bdrs.mobile.Debug("Writing Completed");
	} else {
	    bdrs.mobile.Debug("Dump data not executed. No File system.");
	}
}
	
exports.Hide = function() {
}