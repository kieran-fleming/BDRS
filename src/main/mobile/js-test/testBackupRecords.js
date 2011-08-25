describe("testBackupRecords", function() {
	
    it("write to sd card", function() {
  
    	runs(function() {
    		
    		var writeSuccess = function(evt) {
        	   // console.log("Writing successfull content is: ...(to be implemented)...");
        	    var fileContent = "";
        	    expect(fileContent).toEqual("some sample text");
        	};
        	
    		var paths = navigator.fileMgr.getRootPaths();
    		var writer = new FileWriter(paths[0] + "bdrsMobileRecordsBu.txt");
    		writer.onwrite = writeSuccess;
    		persistence.dumpToJson(function(dump) {
    			  //console.log(dump);
    			  writer.write("some sample text");
    			});
    		
    	});

    });
    
});