var backHeader= [];

/**
 * 
 * @return
 */
function dataForOffline(){
	jQuery.ajax(contextPath+"/js/mobile/bdrs-mobile-database-content.js",{
		type : "POST",
		cache : false,
		dataType : "script"
	})
	.success(function(data) {
		var t = function(){data};
		t();
	})
	.error(function(){logMessage("datforOffline error");});
}

/**
 * 
 * @param request
 * @return
 */
function show(request){
	var hashParams= {};
	hashParams = jQuery.deparam(request.fragment);
	if(hashParams.servicetype){
		if (GR159DB != null){
			runLocal(hashParams);
		}else{
			runRemote(hashParams);
		}
	}else{
		//show home
		var ident = getCookie("regkey");
		jQuery.bbq.pushState({'servicetype':'survey','servicename':'surveysForUser','callback':'renderHome', 'ident':ident});
	}
}

/**
 * 
 * @param hashParams
 * @return
 */
function runLocal(hashParams){
	window[hashParams.servicename+"_webSQL"](hashParams);
}

/**
 * 
 * @param hashParams
 * @return
 */
function runRemote(hashParams){
	logMessage("Running " + hashParams.servicetype + " service on the server.");
	var jsonurl = contextPath+"/webservice/"+hashParams.servicetype+"/"+hashParams.servicename+".htm";
	jQuery.getJSON(jsonurl, hashParams, function(data, textStatus) {		
		window[hashParams.callback](data,hashParams);
	});
}

/**
 * Checks if console.log is supported by the browser before the actual 'concole.log' is executed.
 * @param logMsg The message that needs to be logged.
 */
function logMessage(logMsg){
	if(console.log){
		console.log(logMsg);
	}
}

/**
 * Displays a dialog with a browser specific download result message.
 * @param browserDetected A string conatining the name of the browser.
 * @param succes Boolean is true when download went alright and is false when it failed.
 * @see #updatereadyEvent()
 * @see #cachedEvent()
 * @see #errorEvent()
 */
function setDownloadResultMsg(browserDetected, succes){
	var msg ="";
	switch (browserDetected.toLowerCase())
	{
	case "safari":
		if(deviceType == "ipad" || deviceType =="iphone")
			if(succes)
				msg="Congratulations! You have successfully downloaded this web application. Please select the + at the bottom of this screen and choose 'Add to Home Screen' so you can return to this page when offline.";
			else
				msg="Unfortunately you have not been able to download this web application. Please confirm that you are connected to the internet before trying again.";
		else
			if(succes)
				msg="Congratulations! You have successfully downloaded this web application. Please book mark this page immediately so you can return to this page when offline.";
			else
				msg="Unfortunately you have not been able to download this web application. Please confirm you are using the latest Safari browser and are connected to the internet before trying again.";
		break;
	case "chrome":
		if(succes)
			msg="Congratulations! You have successfully downloaded this web application. You can now select 'Control this current page' icon and choose 'Create application shortcuts' so you can return to this page when offline.";
		else
			msg="Unfortunately you have not been able to download this web application. Please confirm you are using the latest Chrome browser and are connected to the internet before trying again.";
		break;
	default:
		if(succes)
			msg="Congratulations! You have successfully downloaded this web application. Please book mark this page immediately so you can return to this page when offline.";
		else
			msg="Unfortunately you have not been able to download this web application. Please confirm that you are connected to the internet before trying again.";
	}
	setDialog(msg,{
		title: "Cacheing result",
		autoOpen: false,
		show: "drop",
		hide: "drop",
		modal: true
	});
	jQuery( "#dialog" ).dialog( "open" );
}

/**
 * Displays a dialog with a browser specific download result message.
 * @param browserDetected A string conatining the name of the browser.
 * @param succes Boolean is true when download went alright and is false when it failed.
 * @see #updatereadyEvent()
 * @see #cachedEvent()
 * @see #errorEvent()
 */
function setUpdateResultMsg(succes){
	var msg ="";
	if(succes)
		msg="Update was succesfull.";
	else
		msg="Update failed, please try again.";
	setStatusMsg(msg);
}

/**
 * Tries every 3 seconds to connect with the server and passes the connection result to an anonymous callback function.
 * When result is "1" a hidden connection field is set true, otherwise it is set false.
 */
function ping() {
	//temp note: changed to get because application mode on ipad would not enable wifi when coming out of flight mode.
	jQuery.ajax(contextPath + "/webservice/user/ping.htm",{
		type : "GET",
		cache : false,
		dataType : "jsonp"
	})
	.success(function(){sync.start();})
	.error(function(){logMessage("ping error")});
	
	setTimeout(ping, 8000);
}

/**
 * Converts a timestamp of the form yyyy-MM-dd HH:mm:ss.SS.
 * @param timestampStr The timestampStr that needs to be converted.
 * @returns A Date object.
 */
function timestampToDate(timestampStr) {
    // yyyy-MM-dd HH:mm:ss.SS
    var split = timestampStr.split(" ");
    var dateStr = split[0];
    var timeStr = split[1];
    // yyyy-MM-dd HH
    split = dateStr.split("-");
    var year = parseInt(split[0], 10);
    var month = parseInt(split[1], 10)-1;
    var day = parseInt(split[2], 10);
    // HH:mm
    split = timeStr.split(":");
    var hours = parseInt(split[0], 10);
    var minutes = parseInt(split[1], 10);
    // ss.SS
    split = split[2].split(".");
    var seconds = parseInt(split[0], 10);
    var milliseconds = parseInt(split[1], 10);
    return new Date(year, month, day, hours, minutes, seconds, milliseconds);
}

/**
 * Extracts the latitude and longitude from the selected location in the dropdownlist or gets the gps coordinates and injects the values into the corresponding input fields.
 */
function setLocation(){
	 var split = [];
	 var select_locid_lat_lng = jQuery("#locationList").val();
	 split = select_locid_lat_lng.split(",");
	 if(split[0]== "-1"){
		//set longitude and latitude fields with the coordinates from the gps
		 navigator.geolocation.getCurrentPosition(function(position){
			    if (position != null) {
			    	//insertLocation was called from checkNavigator
			        jQuery("input[id=longitude]").val(position.coords.longitude);
			        jQuery("input[id=latitude]").val(position.coords.latitude);
			    }
		 });
	 }else{
		 //set longitude and latitude fields with the coordinates related to the locationid
		 jQuery("#locationid").val(split[0]);
		 jQuery("input[id=latitude]").val(split[1]);
		 jQuery("input[id=longitude]").val(split[2]);
	 }	
}

/**
 * 
 * @return
 */
function getFormData(){
	//get form values
	var values = jQuery("#recordForm").serializeArray();
	var valuesMap = {};
	for(v in values){
		console.log(values[v].name);
		valuesMap[values[v].name] = values[v].value; 
	}
	//change dateformat to milliseconds
	valuesMap.when = Date.parse(valuesMap.when);
	//changetimeformat to milliseconds
	var time = valuesMap.time;
	var d = new Date(valuesMap.when);
    var split = time.split(":");
    var hours = split[0];
    var minutes = split[1];
	d.setHours(hours);
	d.setMinutes(minutes);
	valuesMap.time = d.getTime();
	return valuesMap;
}

function getFeatFormValues(){
	var values = jQuery("#featForm").serializeArray();
	var valuesMap = {};
	for(var i=0; i<values.length; i++){
		
		valuesMap[values[i].name] = values[i].value; 
	}
	return valuesMap;
}

/**
 * 
 * @param JSON_data
 * @return
 */
function setFormData(JSON_data){
	console.log(JSON_data);
		//Set standard values
		jQuery('#record').val(JSON_data.id);
		jQuery('#onlineRecordId').val(JSON_data.online_recordid);
		jQuery('#survey_species_search').val(JSON_data.scientificName);
		jQuery('#selected_species').val(JSON_data.species);
		if(JSON_data.locationid != ""){
			jQuery('#locationList').val(JSON_data.locationid + ","+JSON_data.latitude+","+JSON_data.longitude);
		}else{
			jQuery('#locationList').val("-1");
		}
		jQuery('#latitude').val(JSON_data.latitude);
		jQuery('#longitude').val(JSON_data.longitude);
		jQuery('#locationid').val(JSON_data.locationid);
		var timeNumber = new Number(JSON_data.time);
		var time = new Date(timeNumber);
		var hours = time.getHours();
		if(hours<10){
			hours = "0"+hours;
		}
		var minutes = time.getMinutes();
		if(minutes<10){
			minutes = "0"+minutes;
		}
		jQuery('#time').val(hours+":"+minutes);
		var dateNumber = new Number(JSON_data.when);
		var date = jQuery.datepicker.formatDate('dd M yy', new Date(dateNumber));
		jQuery('#when').val(date);
		jQuery('#number').val(JSON_data.numberseen);
		jQuery('#notes').val(JSON_data.notes);
		//Set custom values
		for(var att in JSON_data.attributes){
			jQuery('#'+att).val(JSON_data.attributes[att]);
		}
		jQuery('#save_or_update').attr({'value':'update'});
}

/**
 *  Obsolete???
 * @return
 */
function saveRecord(){
	var values = getFormData();
	//TODO: addValues to save_recordWebSQLS
	saveRecord_webSQL({'callback':'recordController'});
	//TODO: add surveyID 
	jQuery.address.value('/survey/?sid=');
	return false;
}

/**
 * 
 * @return
 */
function updateRecord(){
	var values = getFormData();
	show({'fragment':"servicetype=survey&servicename=updateRecord&callback=recordController&"+values});
	return false;
}

/**
 * 
 * @param item
 * @return
 */
function deleteRecord(item){
	var recordId = jQuery(item).attr('id');
	var regkey = getCookie("regkey");
	var surveyId = jQuery.bbq.getState("surveyId");
	jQuery.bbq.pushState({"servicetype":"record", "servicename":"deleteRecord", "callback":"recordController", "regkey":regkey,"surveyId":surveyId, "recordId":recordId});
}

/**
 * Rertrieves record ids from the DOM and then runs remote and local deleterecords service when webSql is supported,
 * otherwise only runs remote deleteRecords service.
 */
function deleteRecords(){
	location.replace(jQuery.address.baseURL() + "#/record/?action=delete&sid=" + jQuery.address.parameter('sid'));
}

function selectRecords(){
	if(jQuery(".record_checkbox").attr("checked")){
		jQuery(".record_checkbox").attr("checked",false);
	}else{
		jQuery(".record_checkbox").attr("checked",true);
	}
}


/**
 * Deletes records on server.
 * On success removes records from DOM
 * @param map Contains online record ids and the user registration key.
 */
function deleteRecords_server(map){
	jQuery.post(contextPath+"/webservice/record/deleteRecords.htm", {
        JSONrecords: JSON.stringify(map.ids),
        ident: map.ident
    }, function (response) {
        for(var id in map.ids){
        	//TODO: add record identifier
        	jQuery('#record'+id).remove();
        }
    }, 'json');
}
/**
 * 
 * @param hashParams
 * @return
 */
function recordController(hashParams){
	var regkey = getCookie("regkey");
	//start displaying records list
	jQuery.bbq.pushState({'servicetype':"record",'servicename':"recordsForSurvey", "callback":"renderRecordsList", "regkey":regkey, "surveyId":hashParams.survey});
}

/**
 * 
 * @param JSON_data
 * @return
 */
function updateRecordHandler(JSON_data){
	
}

/**
 * 
 * @return
 */
function taxaForSurvey(){
	var searchQry = "SELECT * FROM taxongroup;";
	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(searchQry, [], function(transaction, results){
			render("taxaForSurvey",getJsontaxaForSurvey(results));
		},errorHandler);});
}

/**
 * 
 * @param results
 * @return
 */
function getJsontaxaForSurvey(results){
	var resultLength = results.rows.length;
	 var myJSONObject = {"lenght":resultLength};
	 var myJSONArray = new Array();
	 if(resultLength>0){
		 for (var i=0; i<resultLength; i++){
			 var row = results.rows.item(i);
			var myJSONObject = {"_class": "TaxonGroup",
		                "id":row.taxongroupid,
		                "thumbNail":row.thumbnail,
		                "name":row.name
		                }; 
			myJSONArray[i]=myJSONObject;
		 }
	 }
	 return myJSONArray;
}

/**
 * 
 * @return
 */
function surveySpeciesForTaxon(){
	var id = jQuery.bbq.getState('taxonId');
	var searchQry = "SELECT * FROM indicatorspecies WHERE taxongroupid="+id+";";
	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(searchQry, [], function(transaction, results){
			render("surveySpeciesForTaxon",getJsonsurveySpeciesForTaxon(results));
		},errorHandler);});
}

/**
 * 
 * @param results
 * @return
 */
function getJsonsurveySpeciesForTaxon(results){
	 var resultLength = results.rows.length;
	 var myJSONObject = {"lenght":resultLength};
	 for (var i=0; i<resultLength; i++){
		 var row = results.rows.item(i);
	     myJSONObject = {"_class": "IndicatorSpecies",
	                "id":row.indicatorspeciesid,
	                "scientificName":row.scientificname,
	                "commonName":row.commonname,
	                "taxonGroup":row.taxongroupid,
	                "lenght":resultLength
	                }; 
	 }
	 return myJSONObject;
}

/**
 * 
 * @return
 */
function getTaxonById(){
	var id = jQuery.bbq.getState('id');
	var searchQry = "SELECT * FROM indicatorspecies WHERE indicatorspeciesid="+id+" ;";
	//createConnection();
	GR159DB.transaction(function(transaction) {
		transaction.executeSql(searchQry, [], function(transaction, results){
			render("getTaxonById",getJsonTaxonById(results));
		},errorHandler);});
}

/**
 * 
 * @param results
 * @return
 */
function getJsonTaxonById(results){
	 var resultLength = results.rows.length;
	 var myJSONObject = {"lenght":resultLength};
	 if(resultLength>0){
		 var row = results.rows.item(0);
	     myJSONObject = {"_class": "IndicatorSpecies",
	                "id":row.indicatorspeciesid,
	                "scientificName":row.scientificname,
	                "commonName":row.commonname,
	                "taxonGroup":row.taxongroupid,
	                "lenght":resultLength
	                }; 
	 }
	 return myJSONObject;
}

/**
 * 
 * @param c_name
 * @return
 */
function getCookie(c_name)
{
	if (document.cookie.length>0)
	  {
	  c_start=document.cookie.indexOf(c_name + "=");
	  if (c_start!=-1)
	    {
	    c_start=c_start + c_name.length+1;
	    c_end=document.cookie.indexOf(";",c_start);
	    if (c_end==-1) c_end=document.cookie.length;
	    return unescape(document.cookie.substring(c_start,c_end));
	    }
	  }
	return "";
}

/**
 * 
 * @param msg
 * @param options
 * @return
 */
function setDialog(msg,options){
	//set options
	jQuery( "#dialog" ).dialog(options);
	//set content
	jQuery("#dialog_content").append(msg);
}

/**
 * 
 * @param data
 * @param hashParams
 * @return
 */
function locationsForRecordFormController(data, hashParams){
	if(data){
		renderLocationsForRecordForm(data);
	}else{
		window[hashParams.servicename+"_webSQL"](hashParams);
	}
}

/**
 * 
 * @param data
 * @param hashParams
 * @return
 */
function recordsListController(data, hashParams){
	if(data){
			renderRecordsList(data);
	}else{
		window[hashParams.servicename+"_webSQL"](hashParams);	
	}
}

/**
 * 
 * @param hashParams
 * @return
 */
function recordFormController(hashParams){
	window[hashParams.servicename+"_webSQL"](hashParams);
}

/**
 * 
 * @return
 */
function setDatePickerOnFields(){
	var dateFormat = 'dd M yy';
	// This blur prevents the ketchup validator from indicating the
	// field is required when it is already filled in
	var onSelectHandler = function(dateText, datepickerInstance) { jQuery(this).trigger('blur'); };
	var keyDown = function(){ return false};
	jQuery(".datepicker").datepicker({
	    dateFormat: dateFormat,
	    onSelect: onSelectHandler
	}).keydown(keyDown);
	jQuery(".datepicker_historical").datepicker({
	    dateFormat: dateFormat,
	    onSelect: onSelectHandler,
	    maxDate: new Date() // Cannot pick a date in the future
	}).keydown(keyDown);
}

/**
 * 
 * @param request
 * @param callback
 * @return
 */
function getSpeciesAutoCompleteField(request, callback){
	var surveyId = jQuery.address.parameter("sid");
	var resultData = {};
/*	var jqxhr = jQuery.getJSON(contextPath+"/webservice/survey/speciesForSurvey.htm", {'surveyId':surveyId, 'q':request.term}, function(data, textStatus, jqXHR) {
		callback(data);
	})
	.error(function() {*/
		var qry = "SELECT * FROM indicatorspecies WHERE scientificname LIKE '%"+request.term+"%' OR commonname LIKE '%"+request.term+"%' ;";
		GR159DB.transaction(function(transaction) {
			transaction.executeSql(qry, [], function(transaction,results){
				var resultLength = results.rows.length;
				var species_array = new Array();
				for (var i=0; i<resultLength; i++){
					var row = results.rows.item(i);
					var species = {"_class": "IndicatorSpecies",
				                "id":row.indicatorspeciesid,
				                "scientificName":row.scientificName,
				                "commonName":row.commonName,
				                "taxonGroup":row.taxongroupid,
				                "lenght":resultLength,
				                "label":row.scientificName+" <br> "+row.commonName,
				                "value":row.scientificname
				                }; 
					species_array[i] = species;
				}
				callback(species_array);
			} ,errorHandler);});
		/*});*/
}

/**
 * 
 * @param event
 * @param ui
 * @return
 */
function setSpeciesAutoCompleteField(event, ui){
	jQuery('#selected_species').val(ui.item.id);
	jQuery('#survey_species_search').val(ui.item.scientificName);
    return false;
}

/**
 * 
 * @return
 */
function homePageController(){
	if(useCache == true){
		//go to offline home
		window.location = contextPath +"/bdrs/mobile/home.htm?offline=true";
	}else{
		//go to online home
		window.location = contextPath +"/bdrs/mobile/home.htm";
	}
}


/**
 * Show a message for a few seconds in the statusbar
 * and then add the message to the messageContainer
 * and show the msgIcon in the statusbar
 * @param The message
 */
function setStatusMsg(msg){
	setTimeout(function(){
		var view = "";
		var msg_begin = "";
		var msg_end = "";
		if(msg.length >10){
			//TODO: make into a link and make the link display it in a box
			view ="<span id=\"statusMsg\">"+msg.slice(0,20)+"..."+"</span>";
		}else{
			view = "<span id=\"statusMsg\">"+msg+"</span>";
		}
		//add msg to statusbar but hide it
		jQuery("#statusBarMsg").append(jQuery(view).hide());
		//show status msg icon
		jQuery('#msgIcon').show();
		//show hidden msg
		jQuery('#statusMsg').show("slide", {"direction":"up", "mode":"show"}, 1000, function(){
			//hide msg after 5 seconds
			setTimeout(function(){
				jQuery('#statusMsg').hide("slide", {"direction":"down", "mode":"show"}, 1000,clearStatusBarMsg);	
			},3000);
			//add msg to msgContainer
			jQuery("#msgs").append(msg);
			jQuery("#clearBtn").click(function(){clearMsgContainer();});
		});
	},3000);
}

/**
 * Remove all messages from the messageContainer
 * and hide the msgIcon that is displayed in the statusbar
 */
function clearMsgContainer(){
	jQuery("#msgs").empty();
	jQuery('#msgContainer').hide("slide", {"direction":"up", "mode":"show"}, 100);
	jQuery('#msgIcon').hide();
}

/**
 * Remove message from the statusbar
 */
function clearStatusBarMsg(){
	jQuery("#statusBarMsg").empty();
}

/**
 * 
 */
function syncToServer(){
	runLocal({'servicename':'recordsToDelete','callback':'sync.add','synctype':0});
	runLocal({'servicename':'recordsToUpdate','callback':'sync.add','synctype':1});
	runLocal({'servicename':'recordsToUpload','callback':'sync.add','synctype':2});
/*	deleteRecordsSync();
	updateRecordsSync();
	uploadRecordsSync();*/
}

/**
 * Sets status of local records to "delete".
 * @param localRecIds The ids of the local records that need to be deleted.
 * @see #syncToServer()
 */
function setLocalRecStatusDelete(localRecIds){
	 //createConnection();
	GR159DB.transaction(function (transaction) {
		var updateStatement = "UPDATE record SET status = 'delete' WHERE online_recordid=?;";
		for (var id in localRecIds){
			transaction.executeSql( updateStatement,[localRecIds[id]],function(){logMessage("stats is set to 'delete'")}, errorHandler);
		}
	});	
}

function setNavigationForward(pageName){
	backHeader.push(jQuery('#previousPage').html());
	jQuery('#previousPage').html(jQuery('#currentPage').html());
	jQuery('#currentPage').html(pageName);
}

function setNavigationBack(){
	var prevHeader = backHeader.pop();
	history.go(-1);
	jQuery('#previousPage').html(jQuery('#currentPage').html());
	jQuery('#currentPage').html(prevHeader);
}

/**
 * Making a-synchronise calls synchronise.
 */
var Queue = (function () {
    "use strict";

    var results = [],
        stack = [],
        queue = [];

    function buildControl(queue) {
        var obj = {
            next: function () {
                obj.next = function () {};
                stack.shift();

                var first = stack[0];
                if (first) {
                    first(buildControl(queue));
                } else {
                    queue();
                }
            }
        };
        return obj;
    }

    var obj = {
        sync: function (func) {
            if (typeof func === "function") {
                stack.push(func);

                if (stack.length === 1) {
                    func(buildControl(obj.async()));
                }
            }
        },

        async: function (func) {
            var index = queue.push(func) - 1;

            return function () {
                if (index === null) {
                    return;
                }

                results[index] = Array.prototype.slice.call(arguments);
                index = null;

                for (var i = 0; i < queue.length; i += 1) {
                    if (results[i]) {
                        if (typeof queue[i] === "function") {
                            queue[i].apply(null, results[i]);
                            delete queue[i];
                        }
                    } else {
                        return;
                    }
                }

                queue.length = results.length = 0;
            };
        },

        run: function (func) {
            return obj.async(func)();
        }
    };
    return obj;
}());