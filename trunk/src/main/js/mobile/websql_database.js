var GR159DB = null;
var shortName = 'GR159DB';
//TODO: version should be set in databaseJS.jsp because of db update check
var version = '1.0';
//TODO: displayName should be set in databaseJS.jsp because portal specific implementation
var displayName = 'Backyard Science database';
var maxSize = 5000000; // in bytes
var downld = false;

var curPercentage = 0;
var errorPercentage = 0;
var stepSize = 0;

/**
 * Creates connection to local database if browser supports webSql. Then checks if connection was successful.
 */
function createConnection() {
	if (!window.openDatabase) {logMessage('Databases are not supported in this browser');}
	else{
		try {
			if (!GR159DB) {
				GR159DB = openDatabase(shortName, version, displayName, maxSize, creationCallback);
				}
		}catch(e){
			if (e == 2) {logMessage("Invalid database version.");}
			else {logMessage("Unknown error " + e + ".");}
			return;
		}
	}
}

/**
 * Is called when the webSql openDatabase function failed to open a specific database
 */
function creationCallback(){
	console.log("Tried to open the local database but failed. The database does probably not exist or is updated on the server.")
	//Try to create a new database with the most recent data that is available on the server
}

/**
 * Creates tables and columns in the database. 
 * Note that foreign keys are not supported by Chrome at the time of writing this code. 
 * */
function createTables() {
	//createConnection();
	GR159DB
      .transaction(function(transaction) {
        transaction
        .executeSql(
            'CREATE TABLE IF NOT EXISTS userdefinition('+
            'userdefinitionid INTEGER NOT NULL PRIMARY KEY,'+
            'emailaddress TEXT NOT NULL,'+
            'firstname TEXT NOT NULL,'+
            'lastname TEXT NOT NULL,'+
            'registrationkey TEXT NOT NULL,'+
            'name TEXT NOT NULL);',
            [],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS taxongroup('+
                'taxongroupid INTEGER NOT NULL PRIMARY KEY,'+
                'name TEXT NOT NULL,'+
                'thumbnail TEXT NOT NULL,'+
                'image TEXT NOT NULL,'+
                'weight INTEGER NOT NULL DEFAULT 0);',
                [],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS indicatorspecies('+
                'indicatorspeciesid INTEGER NOT NULL PRIMARY KEY,'+
                'scientificName TEXT NOT NULL,'+
                'commonName TEXT NOT NULL,'+
                'taxongroupid INTEGER NOT NULL);',
                [],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS indicatorspeciesattribute('+
                'indicatorspeciesattributeid INTEGER NOT NULL PRIMARY KEY,'+
                'numericvalue INTEGER,'+
                'description TEXT,'+
                'datevalue TEXT,'+
                'stringvalue TEXT NOT NULL,'+
                'attributeid INTEGER NOT NULL,'+
                'indicatorspeciesid INTEGER NOT NULL);',
                [],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS indicatorspeciesprofile('+
                'indicatorspeciesprofileid INTEGER NOT NULL PRIMARY KEY,'+
                'type TEXT NOT NULL,'+
                'content TEXT NOT NULL,'+
                'header TEXT NOT NULL,'+
                'description TEXT,'+
                'indicatorspeciesid INTEGER NOT NULL,'+
                'weight INTEGER NOT NULL);',
                [],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS taxongroupattribute('+
            	'taxongroupattributeid INTEGER NOT NULL PRIMARY KEY,'+
            	'name TEXT NOT NULL,'+
            	'description TEXT NOT NULL,'+
            	'tag INTEGER NOT NULL,'+
            	'taxongroupid INTEGER NOT NULL,'+
            	'typecode TEXT NOT NULL);',
            	[],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS taxongroupattributeoption('+
                'taxongroupattributeoptionid INTEGER NOT NULL PRIMARY KEY,'+
                'value TEXT NOT NULL,'+
                'taxongroupattributeid INTEGER NOT NULL);',
                [],nullHandler,errorHandler);
        transaction
            .executeSql(
                'CREATE TABLE IF NOT EXISTS speciessound('+
                'speciessoundid INTEGER NOT NULL PRIMARY KEY,'+
                'filename TEXT NOT NULL,'+
                'path TEXT NOT NULL,'+
                'fk_indicatorspeciesid INTEGER NOT NULL);',
                [],nullHandler,errorHandler);
        transaction.executeSql(
        		'CREATE TABLE IF NOT EXISTS record('+
                'id INTEGER NOT NULL PRIMARY KEY,'+
                'fkindicatorspeciesid INTEGER NOT NULL REFERENCES indicatorspecies(indicatorspeciesid),'+
                'latitude INTEGER NOT NULL,'+
                'longitude INTEGER NOT NULL,'+
                'time INTEGER NOT NULL,'+
                '`when` INTEGER NOT NULL,'+
                'numberseen INTEGER NOT NULL,'+
                'notes TEXT NOT NULL,'+
                'online_recordid INTEGER,'+
                'status TEXT,'+
                'locationid INTEGER REFERENCES location(locationid),'+
                'surveyid INTEGER REFERENCES survey(id));',
                [], nullHandler,errorHandler);
        transaction.executeSql(
                'CREATE TABLE IF NOT EXISTS recordattribute('+
                'recordattributeid INTEGER NOT NULL PRIMARY KEY,'+
                'stringvalue TEXT,'+
                'fkrecordid INTEGER NOT NULL REFERENCES record(id),'+
                'attributeid INTEGER NOT NULL REFERENCES attribute(id));',
                [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS location('+
            'locationid INTEGER NOT NULL PRIMARY KEY,'+
            'name TEXT,'+
            'latitude TEXT,'+
            'longitude TEXT);',
            [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS survey('+
            'id INTEGER NOT NULL PRIMARY KEY,'+
            'name TEXT, description TEXT,'+
            'public INTEGER,'+
            'active INTEGER);',
            [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS survey_location('+
            'surveyid INTEGER,'+
            'locationid INTEGER,'+
            'pos INTEGER,'+
            'PRIMARY KEY(surveyid,pos));',
            [],nullHandler,errorHandler);
        
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS survey_attributes('+
            'id INTEGER,'+
            'attributeid INTEGER,'+
            'pos INTEGER,'+
            'PRIMARY KEY(id,pos));',
            [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS survey_indicatorspecies('+
            'surveyid INTEGER,'+
            'indicatorspeciesid INTEGER,'+
            'PRIMARY KEY(surveyid,indicatorspeciesid));',
            [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS attribute('+
            'id INTEGER NOT NULL PRIMARY KEY,'+
            'name TEXT,'+
            'typecode TEXT,'+
            'required INTEGER,'+
            'description TEXT,'+
            'tag INTEGER);',
            [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS attributeoption('+
            'id INTEGER NOT NULL PRIMARY KEY,'+
            'pos INTEGER,'+
            'value TEXT,'+
            'attributeid INTEGER);',
            [],nullHandler,errorHandler);
        transaction.executeSql(
            'CREATE TABLE IF NOT EXISTS taxongroup_attributes('+
            'id INTEGER, attributeid INTEGER,'+
            'pos INTEGER, PRIMARY KEY(id,pos));',
            [],nullHandler,errorHandler);
      });
}

/**
 * Drops tables in the local database.&nbsp;Excepted from this are the record table and the recordattribute table.
 */ 
function dropTablesExclRecs(){
  //createConnection();
  GR159DB.transaction(function(transaction) {
      transaction.executeSql("DROP TABLE IF EXISTS userdefinition;", [],
          nullHandler(), 
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS taxongroup;", [],
          nullHandler(), 
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS indicatorspecies;", [],
          nullHandler(),
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS indicatorspeciesattribute;", [],
          nullHandler(),
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS indicatorspeciesprofile;", [],
          nullHandler(),
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS taxongroupattribute;", [],
          nullHandler(),
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS taxongroupattributeoption;", [],
          nullHandler(),
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS speciessound;", [],
    		  nullHandler(),
    		  errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS location;", [],
          nullHandler(), 
          errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS attributeoption;", [],
              nullHandler(), 
              errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS taxongroup_attributes;", [],
              nullHandler(), 
              errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS survey_attributes;", [],
              nullHandler(), 
              errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS survey_location;", [],
              nullHandler(), 
              errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS survey_indicatorspecies;", [],
              nullHandler(), 
              errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS attribute;", [],
              nullHandler(), 
              errorHandler);
      transaction.executeSql("DROP TABLE IF EXISTS survey;", [],
              nullHandler(), 
              errorHandler);
    });
}

/**
 * 
 * @param map.surveyId			The survey from which the attributes are requested
 * @param map.values			The function that takes the results in JSON format
 * @return
 */
function saveRecord_webSQL(map){
	var qry = "INSERT INTO record (fkindicatorspeciesid, latitude, longitude, time, `when`, numberseen, notes, online_recordid, status, locationid, surveyid) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);";
	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.selected_species, map.locationLatitude, map.locationLongitude, map.time, map.when, map.number, map.notes, map.onlineRecordId, "new", map.locationid, map.survey], function(transaction,results){
			var localrecordid = results.insertId;
			var qry1 = "INSERT INTO recordattribute (stringvalue, fkrecordid, attributeid) VALUES (?, ?, ?);";
			for(var mapItem in map){
				if(mapItem.search("attribute") == 0){
					var attributeid = mapItem.substr(9);
					var stringvalue = map[mapItem];
					transaction.executeSql(qry1,[stringvalue, localrecordid, attributeid],function(transaction,results){
					},errorHandler);
				}
			}
			//window[map.callback](map);
		} ,errorHandler);
	});
}

/**
 * 
 * @param map
 * @return
 */
function updateRecord_webSQL(map){
	var qry = "UPDATE record SET fkindicatorspeciesid=?, latitude=? ,longitude=? ,time=? ,`when`=? , numberseen=?, notes=? , online_recordid=?, status=?, locationid=?, surveyid=? WHERE id=?;";
	var values = map.values;
	var sid = map.sid;
	var updateStatus = "";
	if(values.onlineRecordId == null || values.onlineRecordId == ""){
		updateStatus="new";
	}else{
		updateStatus="update";
	}

	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [values.selected_species, values.locationLatitude, values.locationLongitude, values.time, values.when, values.number, values.notes, values.onlineRecordId, updateStatus, values.locationid, sid, values.record], function(transaction,results){
			//delete old record attributes
			var qry1 = "DELETE FROM recordattribute where fkrecordid = ?;"
			transaction.executeSql(qry1,[values.record],function(transaction, results){
				//create new record attributes
				for(var mapItem in values){
					if(mapItem.search("attribute") == 0){
						var attributeid = mapItem.substr(9);
						var stringvalue = values[mapItem];
						var qry2 = "INSERT INTO recordattribute (stringvalue, fkrecordid, attributeid) VALUES (?, ?, ?);";
						transaction.executeSql(qry2,[stringvalue, values.record, attributeid],function(transaction,results){
						},errorHandler);
					}
				}
				//window[map.callback](map);
			}, errorHandler);
		} ,errorHandler);
	});
}

/**
 * Deletes records from the local database
 * On success removes records from DOM
 * @param map Contains an array of local record ids  
 */
function deleteRecords_webSQL(map) {
	var ids= map.ids;
	GR159DB.transaction(function(transaction) {
		for(var id in ids){
			var deleteStatement_recordAttribute = "DELETE FROM recordattribute where fkrecordid=?;";
		    var deleteStatement_record = "DELETE FROM record where id=?;";
		    //createConnection();
			var i = ids[id];
			//delete record attributes
			transaction.executeSql(deleteStatement_recordAttribute,[i],function(){},errorHandler);
			//delete record
		   	transaction.executeSql(deleteStatement_record,[i],function(){
		   		console.log("deleted local record with recordid "+i);
		   		//TODO: add record identifier
	        	jQuery('#record'+id).remove();
		   		},errorHandler);
		}
	});
	
}

/**
 * 
 * @param map
 * @return
 */
function getRecordById_webSQL(map){
	var qry = "SELECT r.id, r.fkindicatorspeciesid, r.latitude, r.longitude, r.time, r.`when`, r.numberseen, r.notes, r.online_recordid, r.status, r.locationid, r.surveyid, l.locationid, l.name AS locationName, i.scientificName  FROM indicatorspecies i JOIN record r ON i.indicatorspeciesid = r.fkindicatorspeciesid LEFT OUTER JOIN location l ON r.locationid = l.locationid  WHERE r.id="+map.recordId+";";
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [], function(transaction,results){
			var resultLength = results.rows.length;
			var record = results.rows.item(0); 
			var qry1 = "SELECT * FROM recordattribute WHERE fkrecordid ="+map.recordId+";";
			GR159DB.transaction(function(transaction) {
				transaction.executeSql(qry1, [], function(transaction,results){
					var resultLength1 = results.rows.length;
					var record_atts_array= {};
					for (var j=0; j<resultLength1; j++){
						var attVal = {};
						var row = results.rows.item(j);
						var id = row.attributeid;
						var stringvalue =  row.stringvalue;
						record_atts_array[id] =stringvalue;
					}
					record.attributes = record_atts_array;
					record.species = record.fkindicatorspeciesid;
					window[map.callback](record);
				},errorHandler);
			});
		} ,errorHandler);
	});
}

/**
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function recordsForSurvey_webSQL(map){
	var qry = "SELECT * FROM record r LEFT JOIN indicatorspecies isp ON isp.indicatorspeciesid = r.fkindicatorspeciesid WHERE r.status!='delete' AND surveyid=?;";
	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.sid], function(transaction,results){
			var resultLength = results.rows.length;
			var records_array = new Array();
			for (var i=0; i<resultLength; i++){
				var row = results.rows.item(i);
				row.type=map.type;
				records_array[i] = row;
			}
			map.records = records_array;
			window[map.callback](map);
		} ,errorHandler);
	});
}

/**
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function taxaForSurvey_webSQL(map){
	var qry = "SELECT * FROM taxongroup WHERE taxongroupid IN(SELECT DISTINCT taxongroupid FROM indicatorspecies WHERE indicatorspeciesid IN (SELECT indicatorspeciesid FROM survey_indicatorspecies WHERE surveyid=?));";
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.sid], function(transaction,results){
			var resultLength = results.rows.length;
			var taxa_array = new Array();
			for (var i=0; i<resultLength; i++){
				var row = results.rows.item(i);
				console.log("row in taxaForSurvey_webSQL: ...");
				console.log(row);
				var taxa = {
						'id': row.taxongroupid,
						'listItemIconUrl': row.thumbnail,
						'linkUrl': '#/fieldguide/?tid=' + row.taxongroupid + '&sid=' + map.sid,
						'alt': row.name,
						'listItemText': row.name,
						'listItemSubText': ''
						};
				console.log("taxa in taxaForSurvey_webSQL: ...");
				console.log(taxa);
				taxa_array.push(taxa);
				//taxa_array[i] = taxa;
			}
			window[map.callback](taxa_array);
		} ,errorHandler);
	});
}

/**
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function taxaSpeciesSurvey_webSQL(map){
	//var qry = "SELECT * FROM indicatorspecies WHERE taxongroupid=? AND indicatorspeciesid IN (SELECT indicatorspeciesid FROM survey_indicatorspecies WHERE surveyid =?);";
	var qry = "SELECT i.indicatorspeciesid, i.commonName, i.scientificName, ip.content "+
	"from indicatorspecies i "+ 
	"left join indicatorspeciesprofile ip "+ 
	"on i.indicatorspeciesid = ip.indicatorspeciesid "+ 
	"where ip.type='profile_img_med' "+ 
	"and i.taxongroupid=? "+ 
	"and i.indicatorspeciesid "+ 
	"in (select indicatorspeciesid from survey_indicatorspecies where surveyid=?);";
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.tid, map.sid], function(transaction,results){
			var resultLength = results.rows.length;
			var species_array = new Array();
			for (var i=0; i<resultLength; i++){
				var row = results.rows.item(i);
				var species = {
						'id': row.indicatorspeciesid,
						'listItemIconUrl': '/BDRS/'+row.content,
						'linkUrl': '#/info/?id=' + row.indicatorspeciesid,
						'alt': row.scientificName + ' | ' + row.commonName,
						'listItemText': row.commonName,
						'listItemSubText': row.scientificName
						};
				
				species_array.push(species);
			}
			map.species = species_array;
			window[map.callback](map);
		} ,errorHandler);
	});
}

/**
 * 
 * @param map
 * @return
 */
function surveysForUser_webSQL(map){
	var query = "SELECT * FROM survey;"
		//createConnection();
		GR159DB.transaction(function(transaction) {
			transaction.executeSql(query, [], function(transaction,results){
				var resultLength = results.rows.length;
				var survey_array = [];
				for(var i=0; i<resultLength; i++){
					var survey = results.rows.item(i);
					survey_array.push({'id':survey.id, 
						'name':survey.name,
						'description':survey.description,
						'active':survey.active
					});	
				}
				map.surveys = survey_array;
				window[map.callback](map);
			},errorHandler);
		});
}


/**
 * @param map.surveyId			The survey from which the attributes are requested
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function attributesForSurvey_webSQL(map){
	var qry = "SELECT a.required,  a.id AS attribute_id, a.typecode, a.name, a.description, ao.id AS attribute_option_id, ao.pos, ao.value " +
	"FROM attribute a " +
	"LEFT JOIN attributeoption ao " +
	"ON a.id = ao.attributeid " +
	"WHERE a.id IN (SELECT sa.attributeid FROM survey_attributes sa WHERE sa.id=?) ORDER BY attribute_id;";
	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.sid], function(transaction,results){
			var resultLength = results.rows.length;
			var attribute = {};
			var attributes = [];
			var prevId = "";

			for (var i=0; i<resultLength; i++){
				var row = results.rows.item(i);

				if(row.attribute_id == prevId && row.attribute_option_id != null){
					//add an option to the array inside the existing attribute
					attribute.options.push({'id':row.attribute_option_id, 'pos':row.pos, 'value':row.value});
				}else{
					//either the first row or the first new attribute
					if(i!=0){
						// first new attribute
						//add attribute with its options to array
						attributes.push(attribute);
						//clear previous attribute
						attribute = {}
					}
						// create an attribute
						attribute.id = row.attribute_id;
						attribute.name = row.name;
						attribute.description = row.description;
						attribute.typeCode = row.typecode;
						attribute.required = row.required;
						if(row.attribute_option_id != null){
							attribute.options = [];
							//add an option to the array inside the attribute
							attribute.options.push({'id':row.attribute_option_id, 'pos':row.pos, 'value':row.value});
						}
						
				}
				//set prevId to current attribute id
				prevId = row.attribute_id
			}
			//add last attribute with its options to array
			attributes.push(attribute);
			map.surveyAttributes = attributes;
			window[map.callback](map);
		} ,errorHandler);
	});
}


/**
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function profileForSpecies_webSQL(map){
	var qry = "select * from indicatorspeciesprofile where indicatorspeciesid=?";
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.id], function(transaction,results){
			var resultLength = results.rows.length;
			var speciesImage_array = new Array();
			for (var i=0; i<resultLength; i++){
				speciesImage_array.push(results.rows.item(i));
			}
			window[map.callback](speciesImage_array);
		} ,errorHandler);
	});
}

/**
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function filterForTaxa_webSQL(map){
	//TODO: also filter by survey
	var qry = "select distinct ia.stringvalue, a.name, a.typecode, a.description, a.id as attid from attribute a JOIN indicatorspeciesattribute ia on a.id = ia.attributeid where a.tag = 'true' and a.id in (select attributeid from taxongroup_attributes where id =? ) order by a.id;";
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.tid], function(transaction,results){
			var resultLength = results.rows.length;
			var taxaTags_array = new Array();
			for (var i=0; i<resultLength; i++){
				taxaTags_array.push(results.rows.item(i));
			}
			map.taxaTags = taxaTags_array;
			window[map.callback](map);
		} ,errorHandler);
	});
}

/**
 * @param map.callback			The function that takes the results in JSON format
 * @return
 */
function speciesFiltered_webSQL(map){
	var qry = "SELECT * FROM indicatorspecies WHERE taxongroupid =? AND indicatorspeciesid IN(select indicatorspeciesid from indicatorspeciesattribute where";
	var qryAtts = ""
	for (var key in map.values)
	{
		qryAtts += " (attributeid=" + key + " AND stringvalue='" + map.values[key] + "') AND";
		
	}
	console.log("qryAtts = " + qryAtts);
	qryAtts = qryAtts.slice(0,-3) + ");"
	qry = qry + qryAtts;
	console.log("qry =" +qry );
	
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.tid], function(transaction,results){
			var resultLength = results.rows.length;
			var species_array = new Array();
			for (var i=0; i<resultLength; i++){
				var row = results.rows.item(i);
				var species = {
						'id': row.indicatorspeciesid,
						'listItemIconUrl': '/BDRS/images/bdrs/mobile/sheet_40x40.png',
						'linkUrl': '#/info/?id=' + row.indicatorspeciesid,
						'alt': row.scientificName + ' | ' + row.commonName,
						'listItemText': row.commonName,
						'listItemSubText': row.scientificName
						};
				
				species_array.push(species);
			}
			map.species = species_array;
			window[map.callback](map);
		} ,errorHandler);
	});
}

/** 
 * Queries the local database for locations of a particular survey
 * @param map.surveyId			The survey from which the locations are requested
 * @param map.callback			The function that takes the results in JSON format
 */
function locationsForSurvey_webSQL(map){
	var qry = "SELECT * FROM location l WHERE locationid IN (SELECT sl.locationid FROM survey_location sl WHERE sl.surveyid=?);";
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(qry, [map.sid], function(transaction, results){
			var resultLength = results.rows.length; 
			 var locations_array = [];
			for (var i=0; i<resultLength; i++){
				var row = results.rows.item(i);
				var location = {'id':row.locationid, 'latitude':row.latitude, 'longitude':row.longitude, 'name':row.name};
				//locations_array[i] = location;
				locations_array.push(location);
			}
			window[map.callback](locations_array, map.appendId)
		} , errorHandler);});
}

/**
 * Updates the online_record_id fields in the record table from the succesfully uploaded records.
 * @param map A map that contains maps local record ids with online record ids
 */
function setOnlineRecordIds(map){
	GR159DB.transaction(function(transaction) {
	  for ( var index in map) {
		var onlineRecId = map[index];
		var updateStatement = "UPDATE record SET online_recordid=?, status=? WHERE id=?;"
		transaction.executeSql(updateStatement, [ onlineRecId, "", index ],function(){}, errorHandler);
	  }
	});
}

/** Set the status of the checked records that do exist on the server to 'delete'.
 * The actual deleting of the records will be picked up by the ping.
 */ 
function setStatusDelete_webSQL(map){
	var updateStatement = "UPDATE record SET status = 'delete' WHERE id=?;";
	GR159DB.transaction(function (transaction) {
		for(var i=0; i<map.ids.length; i++){
			transaction.executeSql( updateStatement,[map.ids[i]],function(transaction, results){
				var resultLength = results.rows.length;
				},transactionError );
		}
	});
}

function transactionSucces(){
	/*curPercentage = curPercentage + stepSize;
	console.log("curPercentage: " + curPercentage);
	jQuery("#progressbar").progressbar({ value: curPercentage }); */
}

function transactionError(transaction, error){
/*	errorPercentage = errorPercentage + stepSize;
	console.log("errorPercentage: " + errorPercentage);
	errorHandler(transaction, error);*/
}

/**
 * 
 * @return
 */
function nullHandler(){};

/**
 * Handles unsuccessful webSql transactions.
 * @param transaction The webSql transaction that throws the error. 
 * @param error The error.
 * @returns {Boolean}
 */
function errorHandler(transaction, error) {
  if (error.code == 1) {
	  logMessage(error);
  } else {
    // Error is a human-readable string.
	  logMessage('Oops.  Error was ' + error.message + ' (Code '		+ error.code + ')');
  }
  logMessage(transaction);
  logMessage(error);
  return false;
}