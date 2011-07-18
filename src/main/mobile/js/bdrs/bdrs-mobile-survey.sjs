/**
 * Retrieves the users surveys from the server
 * and stores them on the device.
 */
exports.getAllRemote = function() {
	var response;
	waitfor(response) {
		var jqxhr = jQuery.ajax(
			{
				type : "GET",
				url : bdrs.mobile.User.server_url()
						+ "/webservice/survey/surveysForUser.htm",
				cache : false,
				crossDomain : true,
				dataType : "jsonp",
				data : {
					"ident" : bdrs.mobile.User.ident()
				},
				success : resume,
				error : function() {
					alert("error while trying to download surveys");
					resume();
				}
			}
		);
	}
	
	for (var i=0; i < response.length; i++) {
		// Tries to get the survey from local persistence
		var survey;
		waitfor(survey) {
			Survey.findBy('server_id', String(response[i].id), resume);
		}
		// Adds the survey to local persistence if it is not already in there
		if (survey === null) {
			persistence.add(new Survey( {
				server_id : response[i].id,
				weight: response[i].weight,
				name : response[i].name,
				description : response[i].description,
				active : response[i].active 
			}));
		}	
		waitfor() {
			persistence.flush(resume);
		}			
	}
	
},

/**
 * Gets all surveys from local persistence.
 * @return	allSurveys	The surveys that a the user has downloaded from the server before .
 */
exports.getAll = function(){

	var allSurveys;
	waitfor(allSurveys){
		Survey.all().order('name', true).list(resume);
	}
	return allSurveys;

},

/**
 * Retrieves data of a specific survey from the server
 * and stores it on the device.
 * @param	id			The id of the survey.
 * @return	response	The local survey data or false when an error has occured.
 */
exports.getRemote = function(id){

	//TODO: maybe move getting the 'complete local surveys' to a separate function in this file?
	// get all surveys that are completeley downloaded to the device
	var allSurveys;
	waitfor(allSurveys){
		Survey.all().filter('local', '=', 'true').list(resume);
	}
	
	// extract ids from surveys
	var surveysOnDeviceIds = [];
	for(var z=0; z<allSurveys.length; z++){
		surveysOnDeviceIds.push(allSurveys[z].server_id());
	}
	
	bdrs.mobile.Debug("start survey download " + new Date());
	//retrieve data for requested survey from the server
	var response;
	
	
	
	waitfor(response) {
		var jqxhr = jQuery.ajax(
			{
				type : "GET",
				url : bdrs.mobile.User.server_url() + "/webservice/application/survey.htm",
				cache : false,
				crossDomain : true,
				dataType : "jsonp",
				data : {
					"ident" : bdrs.mobile.User.ident(),
					"sid" : id,
					"surveysOnDevice" : JSON.stringify(surveysOnDeviceIds)
				},
				timeout : 120000,
				success : resume,
				error : resume
			}
		);
	}
	
	if (response.status != undefined) {
		// error
		bdrs.mobile.Error('Error retrieving survey ' + response.status);
		return false;
	} else {
		bdrs.mobile.Debug("finished survey download " + new Date());
		//success
		bdrs.mobile.Debug('Retrieved survey data package ' + response.status);
		return response;
	}
	
},

/**
 * Stores surveydata in the survey that exists on the device.
 * @param	id		The id of the survey on the device that will get data stored inside of it.
 * @param	data	Survey specific data.
 * @return	survey	The survey that is stored on the device.
 */
exports.save = function(id,data){
	//persist taxonGroups and its attributes and attribute-options
	for ( var i = 0; i < data.taxonGroups.length; i++) {
		var taxonGroup = data.taxonGroups[i];
		// create taxon group
		var t = new TaxonGroup({
			server_id : taxonGroup.server_id,
			weight: taxonGroup.weight,
			name : taxonGroup.name,
			image : taxonGroup.image,
			thumbNail : taxonGroup.thumbNail});
		for ( var j = 0; j < taxonGroup.attributes.length; j++){
			var attribute = taxonGroup.attributes[j];
			//create attributes
			var a = new Attribute({
				server_id: attribute.server_id,
				weight: attribute.weight,
				typeCode: attribute.typeCode,
				required: attribute.required,
				name: attribute.name,
				description: attribute.description,
				tag: attribute.tag,
				scope: attribute.scope
			});
			for ( var k = 0; k < attribute.options.length; k++) {
				var option = attribute.options[k];
				// create options
				var ao = new AttributeOption({
					server_id: option.server_id,
					weight: option.weight,
					value: option.value
				});
				// add option to attribute
				a.options().add(ao);
			}
			//add attribute to taxon group
			t.attributes().add(a);
		}
		// add taxongroup to persistence
		persistence.add(t);
		waitfor() {
			persistence.flush(resume);
		}
		
	}
	var taxonGroupMap = {};
	
	// precache all the taxon groups.
	var allTaxonGroups;
	waitfor(allTaxonGroups) {
		TaxonGroup.all().list(resume)
	}
	for (var i = 0; i < allTaxonGroups.length; i++) {
		taxonGroupMap[allTaxonGroups[i].server_id()] = allTaxonGroups[i];
	}
	bdrs.mobile.Debug('Cached taxon groups...');
	console.log(taxonGroupMap);

	bdrs.mobile.Debug(" startspecies (" + data.indicatorSpecies.length + ")");
	var counter = 0;
	for ( var i = 0; i < data.indicatorSpecies.length; i++) {
		var species = data.indicatorSpecies[i];
		
		// create a species
		var sp = new Species({
			server_id : species.server_id,
			weight: species.weight,
			scientificNameAndAuthor : species.scientificNameAndAuthor,
			scientificName : species.scientificName,
			commonName : species.commonName,
			rank : species.taxonRank,
			author : species.author,
			year : species.year});
		
		for ( var j = 0; j < species.attributes.length; j++) {
			
			var speciesAttribute = species.attributes[j];
			var attribute = speciesAttribute.attribute;
			
			// create attribute
			var a = new Attribute({
				server_id: attribute.server_id,
				weight: attribute.weight,
				typeCode: attribute.typeCode,
				required: attribute.required,
				name: attribute.name,
				description: attribute.description,
				tag: attribute.tag,
				scope: attribute.scope
			});

			// create speciesAttribute
			var sa = new SpeciesAttribute({
				server_id : speciesAttribute.server_id,
				weight: speciesAttribute.weight,
				numericValue : speciesAttribute.numericValue,
				stringValue : speciesAttribute.stringValue,
				dateValue : speciesAttribute.dateValue,
				description : speciesAttribute.description
			});
			
			// add attribute to speciesAttribute
			//sa.attribute().add(a);
			// add speciesAttribute to species
			//sp.attributes().add(sa);
		}
		
		//create info item
		for ( var k = 0; k < species.infoItems.length; k++) {
			
			var infoItem = species.infoItems[k];
			var item = new SpeciesProfile({
				content : infoItem.content,
				weight : infoItem.weight,
				description : infoItem.description,
				type : infoItem.type,
				header : infoItem.header,
				server_id : infoItem.server_id,
				weight: infoItem.weight
			});
			
			//get image from server if infoitem is an image
			//if (infoItem.type == "profile_img" || infoItem.type == "profile_img_40x40" || infoItem.type == "profile_img_32x32" || infoItem.type == "profile_img_16x16" || infoItem.type == "map" || infoItem.type == "map_40x40" || infoItem.type == "silhouette" || infoItem.type == "silhouette_40x40"){
			if (bdrs.phonegap.isPhoneGap() && 
					(infoItem.type == "profile_img" 
						|| infoItem.type == "profile_img_40x40" 
						|| infoItem.type == "map_40x40" 
						|| infoItem.type == "silhouette")) {
				
				var response;
				waitfor (response) {
					jQuery.ajax( {
						type : "GET",
						url: bdrs.mobile.User.server_url() + "/files/downloadByUUID.htm",
						cache : false,
						crossDomain : true,
						dataType : "jsonp",
						data : {
					 		uuid : infoItem.content,
					 		encode : true
						}
					})
					.success(resume)
					.error(resume);
				}
				
				if (response.status != undefined) {
					//TODO: check error handling in the whole scenario
					// error
					bdrs.mobile.Error('There was an error whilst downloading image UUID : ' 
							+ infoItem.content + ' Error : ' + response.status);
				} else {
					//TODO: review storage
					// Local Storage
/*									if(localStorage){
						var jsonImage = {'data':response.base64, 'type' : response.fileType};
						localStorage[infoItem.content] = JSON.stringify(jsonImage);
					}*/
					
					//WebSQL via persitenceJs
					var image = new Image({
						path : infoItem.content,
						data : response.base64,
						type : response.fileType
					});
					
					persistence.add(image);
					
					waitfor() {
						persistence.flush(resume);
					}
				}
			}
			
			// add info item to species
			sp.infoItems().add(item);
		}
		
		//save species
		persistence.add(sp);
		
		//save species in taxonGroup
		taxonGroupMap[species.taxonGroup].species().add(sp);
		if (i % 500 == 0) {
			bdrs.mobile.Debug('Flushing species : ' + i);
			waitfor() {
				persistence.flush(function() {
      				persistence.clean();
      				resume();
      			});
			}
		}
	}
	
	bdrs.mobile.Debug("stop species and attributes: " + new Date());
	waitfor() {
		persistence.flush(function() {
			persistence.clean();
			resume();
		});
	}
	bdrs.mobile.Debug("Done flush of species and attributes: " + new Date());

	//get the survey from the device of which we want to add data to
	var survey;
	waitfor(survey){
		Survey.findBy('server_id', id, resume);
	}
	bdrs.mobile.Debug("Done retrieving survey: " + new Date());
	
	//save surveylocations in survey
	for ( var j = 0; j < data.locations.length; j++) {
		var location = data.locations[j];
		var point = new bdrs.mobile.Point(location.location);
		survey.locations().add(new Location({
			server_id : location.server_id,
			weight: location.weight,
			name : location.name,
			latitude : point.getLatitude(),
			longitude : point.getLongitude() }));
	}
	
	// save survey attributes and options in survey
	for ( var k = 0; k < data.attributesAndOptions.length; k++) {
		var a = data.attributesAndOptions[k];
		var attribute = new Attribute({
			server_id: a.server_id,
			weight: a.weight,
			typeCode: a.typeCode,
			required: a.required,
			name: a.name,
			description: a.description,
			tag: a.tag,
			scope: a.scope
		});
		for ( var l = 0; l < a.options.length; l++) {
			var ao = a.options[l];
			var option = new AttributeOption({
				server_id: ao.server_id,
				weight: ao.weight,
				value: ao.value
			});
			attribute.options().add(option);
		}
        survey.attributes().add(attribute);
	}
	bdrs.mobile.Debug("Done with survey options and attributes: " + new Date());
	
	// save survey species relation
	var surveySpeciesIds = data.indicatorSpecies_server_ids.species;
	
//	var allSpecies;
//	waitfor(allSpecies) {
//		Species.all().filter('server_id', 'in', surveySpeciesIds).list(resume);
// 'in' filter seems really slow.
//		Species.all().list(resume);
//	}


//	bdrs.mobile.Debug("Done getting all species for the survey: " + new Date());
	// todo: find a better way of doing this, n^2 efficiency hurts...
//	for (var m = 0; m < surveySpeciesIds.length; m++) {  
//		for (var l = 0; l < allSpecies.length; l++) {
//			if (surveySpeciesIds[m] == allSpecies[l].server_id()) {
//				var surveySp = new SurveySpecies();
//				survey.surveySpecies().add(surveySp);
//				allSpecies[l].surveySpecies().add(surveySp);
//				l = allSpecies.length; // finish up.
//			}
//		}
//	}
	
	bdrs.mobile.Debug("Done linking species to survey: " + new Date());
	 
//	for ( var l = 0; l < surveySpeciesIds.length; l++) {
//		Species.findBy('server_id', surveySpeciesIds[l],function(sp){
//			var surveySp = new SurveySpecies({
//			});
//			survey.surveySpecies().add(surveySp);
//			sp.surveySpecies().add(surveySp);
//		});
//	}
	
	// Create Census Methods
	for(var i=0; i<data.censusMethods.length; i++) {
	    var flatCensusMethod = data.censusMethods[i];
        var censusMethod =  bdrs.mobile.survey._saveCensusMethod(flatCensusMethod);
	    survey.censusMethods().add(censusMethod);
	}
	bdrs.mobile.Debug("Done with census methods: " + new Date());
	
	// flush
	waitfor() {
		persistence.flush(resume);
	}
	
	//sets 'local' flag on Survey to 'true'
	jQuery(survey).data('local', true);
	//adds local class to downloaded survey
	jQuery('.bdrs-page-download-surveys label[for="checkbox-' + id + '"]').addClass('local');
	bdrs.mobile.Debug("finish survey download");
	waitfor() {
		persistence.flush(resume);
	}
	jQuery.mobile.pageLoading(true);
	
	return survey;
},

/**
 * Persists the flattened census method and any associated attribute or
 * sub-census methods.
 * @param the flat census method to be persisted
 * @return the persisted census method.
 */
exports._saveCensusMethod = function(flatCensusMethod) {
    var censusMethod = new CensusMethod({
        server_id: flatCensusMethod.server_id,
        weight: flatCensusMethod.weight,
		name: flatCensusMethod.name,
		description: flatCensusMethod.description,
		type: flatCensusMethod.type,
		taxonomic: flatCensusMethod.taxonomic
	});
	persistence.add(censusMethod);

    // Link the attributes
	for(var j=0; j<flatCensusMethod.attributes.length; j++) {
	    var flatAttr = flatCensusMethod.attributes[j];
		var attr = bdrs.mobile.survey._saveAttribute(flatAttr);
		censusMethod.attributes().add(attr);
	}
	
	for(var i=0; i<flatCensusMethod.censusMethods.length; i++) {
	    var flatSubCensusMethod = flatCensusMethod.censusMethods[i];
        var subCensusMethod = bdrs.mobile.survey._saveCensusMethod(flatSubCensusMethod);
        censusMethod.children().add(subCensusMethod);
	}
	
	return censusMethod;
};

/**
 * Persists flattened attributes and any associated attribute options.
 * @param flatAttribute the flatted attribute to be persisted.
 * @return the persisted attribute.
 */
exports._saveAttribute = function(flatAttribute) {
	var attr = new Attribute({
	    typeCode: flatAttribute.typeCode,
	    required: flatAttribute.required,
	    name: flatAttribute.name,
	    description: flatAttribute.description,
	    tag: flatAttribute.tag,
	    scope: flatAttribute.scope,
	    server_id: flatAttribute.server_id,
	    weight: flatAttribute.weight
    });
    persistence.add(attr);
    
    for(var i=0; i<flatAttribute.options.length; i++) {
        var flatOpt = flatAttribute.options[i];
      	attr.options().add(new AttributeOption({
      	    value: flatOpt.value, 
  	        server_id: flatOpt.server_id,
  	        weight: flatOpt.weight
        }));
    }
    
    return attr;
};


/**
 * Sets a specific survey as the default one.
 * @param	survey	The survey that needs to be made the default one.
 */
exports.makeDefault = function(survey){
	
	Settings.findBy('key', 'current-survey' , function(setting) {
		if (setting === null) {
			var s = new Settings({ key : 'current-survey', value : survey.name() });
			persistence.add(s);
			jQuery('.dashboard-status').empty();
			bdrs.template.render('dashboard-status', { name : survey.name() }, '.dashboard-status');

		} else {
			jQuery(setting).data('value', survey.name());
		}
	});
	
	Settings.findBy('key', 'current-survey-id' , function(setting) {
		if (setting === null) {
			var s = new Settings({ key : 'current-survey-id', value : String(survey.server_id()) });
			persistence.add(s);
		} else {
			jQuery(setting).data('value', String(survey.server_id()));
		}
	});
	
}

