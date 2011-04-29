function bdrs (action, parameters) {
	this.home = function(parameterNames){
       renderHome();
       setNavigationForward("home");
	}
	
	this.help = function(){
		alert("The 'help' section is not yet implemented.");
	}
	
	this.about = function(){
		alert("The 'about' section is not yet implemented.");
	}
	
	this.contact = function(){
		alert("The 'contact' section is not yet implemented.");
	}
	
	this.survey = function(parameterNames){
		if(!jQuery.address.parameter("sid")){
			// list all surveys
			surveysForUser_webSQL({'callback':'renderSurveyList' });
			setNavigationForward("surveys");
		}else{
			// list records for survey jQuery.address.parameter("sid")
			recordsForSurvey_webSQL({'callback':'renderRecordsList', 'sid':jQuery.address.parameter("sid")});
			//document.cookie="surveyId=" + jQuery.address.parameter("sid");
			setNavigationForward("surveyX");
		}
	};
	
	this.record = function(parameterNames){
		if(parameterNames){
			var action = jQuery.address.parameter('action');
			switch(action){
				case "add":
					// render record form
					attributesForSurvey_webSQL({'callback':"renderRecordForm", 'action':'add', 'sid':jQuery.address.parameter("sid")});
					// populate record form with locations that are available for the survey
					locationsForSurvey_webSQL({'callback':"appendLocationsTo", 'appendId':'locationList', 'sid':jQuery.address.parameter("sid"), 'regkey':getCookie("regkey")});
					setNavigationForward("add");
					break;
				case "save": 
					var valuesMap = getFormData();
					valuesMap.survey = jQuery.address.parameter('sid');
					//valuesMap.callback = 'recordController';
					saveRecord_webSQL(valuesMap);
					// Go to back to records list
					location.replace(jQuery.address.baseURL() + "#/survey/?sid=" + jQuery.address.parameter('sid'));
					break;
				case "edit":
					// render record form
					attributesForSurvey_webSQL({'callback':"renderRecordForm", 'action':'edit', 'sid':jQuery.address.parameter("sid")});
					// populate record form with locations that are available for the survey
					locationsForSurvey_webSQL({'callback':"appendLocationsTo", 'appendId':'locationList', 'sid':jQuery.address.parameter("sid"), 'regkey':getCookie("regkey")});
					setNavigationForward("edit");
					break;
				case "update":
					var values = getFormData();
					updateRecord_webSQL({'values':values, 'sid':jQuery.address.parameter("sid")});
					// Go to back to records list
					location.replace(jQuery.address.baseURL() + "#/survey/?sid=" + jQuery.address.parameter('sid'));
					break;
				case "delete": 
					//set status to delete
					var record_ids = jQuery('.record_checkbox:checked');
					var ids = [];
					for(var i=0; i<record_ids.length; i++){
						ids.push(jQuery(record_ids[i]).attr('id'));
					}
					alert("still have to actualy set status delete on the records");
					setStatusDelete_webSQL({'ids':ids});
					// Go to back to records list
					location.replace(jQuery.address.baseURL() + "#/survey/?sid=" + jQuery.address.parameter('sid'));
					break;
				default: logMessage("something went wrong while adding or editing a record");
			}
		}

	};

	/* deleting a record must be kept outside of address url */

	this.fieldguide = function(parameterNames){
		if(jQuery.address.parameter("action") == "filter"){
			//list species from taxa x in survey y with filter values
			var values = getFeatFormValues();
			speciesFiltered_webSQL({'callback':"renderSpeciesList", 'sid':jQuery.address.parameter("sid"), 'tid':jQuery.address.parameter("tid"), 'values':values});
			setNavigationForward("taxaY");
		}else{
			if(!jQuery.address.parameter("tid")){
				// list taxa for survey y
				taxaForSurvey_webSQL({'callback':"renderTaxaList", 'sid':jQuery.address.parameter("sid") });
				setNavigationForward("field guide");
			}else{
				// list species from taxa x in survey y 
				taxaSpeciesSurvey_webSQL({'callback':"renderSpeciesList", 'sid':jQuery.address.parameter("sid"), 'tid':jQuery.address.parameter("tid") });
				setNavigationForward("taxaX");
			}
		}
		
	};


	this.helpid = function (parameterNames){
			// list filters for taxa jQuery.address.parameter("tid") in survey jQuery.address.parameter("sid");
			filterForTaxa_webSQL({'callback':"renderTaxaFilter",'tid':jQuery.address.parameter("tid"), 'sid':jQuery.address.parameter("sid")});	
			setNavigationForward("help id");
	};

	// #/info/?id=4
	this.info = function(parameterNames){
		// Display info on species jQuery.address.parameter("id")
		profileForSpecies_webSQL({'callback':"renderSpeciesInfo", 'id':jQuery.address.parameter("id")});
		//infoForSpecies_webSQL({'callback':"renderSpeciesImagesBox", 'id':jQuery.address.parameter("id")});
		setNavigationForward("speciesX");
	};
	
	this[action](parameters);
}