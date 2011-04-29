/**
 * Object that can be used to synchronise records to the server.
 * @author 	Timo van der Schuit
 * recordsToSync	Contains records that need to be synced with the server
 * 					['records to delete','records to update','records to upload','anything to sync with server'] 
 * start			Retrieves records from the local database and inserts them into recordsToSync.
 * 					Sends recordsToSync to the server.
 *					The succesHandler gets called when recordsToSync reached the server and deletes records with status 'delete; from local database.
 * 					The fail handler gets called when recordsToSync did not reach the server and displays an error message to the user.		
 * reset			Clears recordsToSync. Should be called after a successful sync.
 */
function Sync () {
	//this.recordsToSync = [{},[],[],false];
	this.recordsToSync = [[],[],[],false];
    this.start = function() {
    
    	var s = this;
    	
    	GR159DB.transaction(function (transaction) {
    		var records_map_upload = {};
    		var records_map_update = {};
    		var records_map_delete = {};
    		
    		// Records that are new.
    		Queue.sync(function (queue) {
    			
    			logMessage('about to query records.');
    	    	//transaction.executeSql('select * from record where online_recordid = "";', [], function (transaction, results) {
    			transaction.executeSql('select * from record where status = "new";', [], function (transaction, results) {
    				var recordsLength = results.rows.length;
	    			for (var i=0; i<recordsLength; i++){
	    				var rec = results.rows.item(i);
	    				rec.attributes = {};
	    				records_map_upload[rec.id]=rec;
	    			}
	    			logMessage('done querying records.');
	    			
	    			if (recordsLength > 0) {
	    				logMessage('found records to sync, querying attributes');
	    				s.recordsToSync[3] = true;
	   				 	transaction.executeSql("SELECT * FROM recordattribute;", [], function(transaction, results){
	   				 		var attributesLength = results.rows.length;
	   				 		for (var j = 0; j < attributesLength; j++){
		    					var att = results.rows.item(j);
		    					if((records_map_upload !== undefined) && (records_map_upload[att.fkrecordid] !== undefined)){
		    						records_map_upload[att.fkrecordid].attributes[att.attributeid] = att.stringvalue;
		    						logMessage('adding attribute : ' + att.stringvalue);
		    					} else {
		    						logMessage('either the record map is not defined or we missed a record for attribute : ' + att.attributeid)
		    					}
	   				 		}
	   				 		//converting upload records map to upload records array
	   				 		for (var r in records_map_upload){
	   				 			s.recordsToSync[2].push(records_map_upload[r]);
	   				 		}
	   				 		logMessage('done querying attributes.');
	   				 	}, function() { 
	   				 		logMessage('failed querying attributes.');
	   				 	});
	    			} else {
	    				logMessage('no records to sync');
	    				queue.next();
	    			}
	    			
	    			queue.next(); // release lock on queue
	    			
    	    	}, function () { 
    	    		logMessage('failed querying records.');
    				queue.next(); // still need to release the queue, or we'll get stuck
    			});
    		});
    	
    		// Records that need to be updated
    		Queue.sync(function (queue) {
    			transaction.executeSql("select * from record where status='update' and online_recordid <> '' and online_recordid > 0;", [], function (transaction, results) {
	            	var recordsLength = results.rows.length;
	        		for (var i = 0; i < recordsLength; i++) {
	        			var rec = results.rows.item(i);
	        			rec.attributes = {};
	        			records_map_update[rec.id]=rec;
	        		}
	    			if(recordsLength > 0) {
	    				s.recordsToSync[3] = true;
		                transaction.executeSql("SELECT * FROM recordattribute;", [], function (transaction, results) {
		                	var attributesLength = results.rows.length;
		        			for (var i = 0; i < attributesLength; i++) {
		        				var att = results.rows.item(i);
		        				if ((records_map_update !== undefined) && (records_map_update[att.fkrecordid] !== undefined)) {
		        					records_map_update[att.fkrecordid].attributes[att.attributeid] = att.stringvalue;
		        					//logMessage('adding attribute : ' + att.stringvalue);
		        				}else{
		        					//logMessage('either the record map is not defined or we missed a record for attribute : ' + att.attributeid)
		        				}
		        			}
		        			//converting update records map to up records array
	   				 		for (var r in records_map_update){
	   				 			s.recordsToSync[1].push(records_map_update[r]);
	   				 		}
		        			//logMessage("Successfully added the record attributes for records to update");
		                	queue.next();
		                }, function() {
		                	//logMessage("Failed to add attributes for records to update.");
		                	queue.next(); // we failed, but we'll unlock the queue.
		                });
	    			} else {
	    				//logMessage("There are no records to update to get attributes for");
	    				queue.next();
	    			}
    			}, function () {
    				//logMessage("Failed to query records to be updated.");
    				queue.next(); // release the lock or else we're stuck. 
    			});
    		});
    	
    		// Records that need to be deleted
    		Queue.sync(function (queue) {
    			//transaction.executeSql("SELECT r.id, r.online_recordid from record r WHERE status = 'delete';", [], function (transaction, results) {
    			transaction.executeSql("SELECT * from record r WHERE status = 'delete';", [], function (transaction, results) {
    				var recordsLength = results.rows.length;
    				for (var i = 0; i < recordsLength; i++){
    					var rec = results.rows.item(i);
    					//records_map_delete[rec.id]=rec.online_recordid;
    					records_map_delete[rec.id]=rec;
    				}
    				if(recordsLength > 0){
   				 		//converting upload records map to upload records array
   				 		for (var r in records_map_delete){
   				 			s.recordsToSync[0].push(records_map_delete[r]);
   				 		}
    					s.recordsToSync[3] = true;
    					//s.recordsToSync[0] = records_map_delete;
    				} else {
    					//logMessage("Found no records to delete");
    				}
    				queue.next();
    			}, function() {
    				//logMessage("There was an error whilst querying records to delete");
    				queue.next();
    			});
            });
    		
        	//Sends recordsToSync to the server if flag is true
    		Queue.sync(function (queue) {
    	    	if(s.recordsToSync[3]) {
    			
    	    		jQuery('#syncIcon').show('slow');
    	        	var regkey = getCookie("regkey");

    			var jqxhr = jQuery.post(contextPath + "/webservice/record/syncToServer.htm", {
    			JSONrecords: JSON.stringify(s.recordsToSync),
    			ident: regkey}, 
    			function(data, textStatus, jqXHR) {
    				if(data != null){
    					//clear recordsToSync
    					s.recordsToSync = [[],[],[],false];
    					//TODO: handle deleted records online [delete local records]
    					if(data.deleteResponse != null){
    						console.log("data.deleteResponse : ...");
    						console.log(data.deleteResponse);
    						GR159DB.transaction(function (transaction) {
    							//var deleteStatement = "DELETE FROM record WHERE online_recordid = ?;";
    							var deleteStatement = "DELETE FROM record WHERE id = ?;";
    							for (var id in data.deleteResponse){
    								transaction.executeSql( deleteStatement,[id],function(transaction, results){
    									}, errorHandler);
    							}
    						});
    						GR159DB.transaction(function (transaction) {
    							var deleteStatement = "DELETE FROM recordattribute WHERE fkrecordid = ?;";
    							for (var id in data.deleteResponse){
    								transaction.executeSql( deleteStatement,[id],function(transaction, results){
    									}, errorHandler);
    							}
    						});
    					}
    					//TODO: handle updated records
    					logMessage(data.updateResponse);
    					//Handle uploaded records [insert onlinerecordid]
    					setOnlineRecordIds(data.uploadResponse);
    					jQuery('#syncIcon').hide('slow');
    					queue.next();
    				}else{
    					alert("data is null but succesfull");
    					queue.next();
    				}
    			})
    			.error(function() { 
    				//fail handler
    				jQuery('#syncIcon').hide('slow');
    				alert("Something went wrong while uploading changes to the server. Please contact your administrator.");
    				queue.next();
    			});

    	    	} else {
    	    		jQuery('#syncIcon').hide('slow');
    	    		queue.next();
    	    	}
    	});
        	
    	// end GR159DB.transaction
    	});
    //end of function start()
    };
}
var sync = new Sync();