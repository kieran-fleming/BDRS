/**
 * Flag that keeps track of 
 * if there is any deleting of survey data in progress. 
 */
exports.removingInProgress = false;

/**
 * Retrieves the users surveys from the server
 * and stores them on the device.
 */
exports.getAllRemote = function(callback) {
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
    callback();
},

/**
 * Gets all surveys from local persistence.
 * @return  allSurveys  The surveys that a the user has downloaded from the server before .
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
 * @param   id          The id of the survey.
 * @return  response    The local survey data or false when an error has occured.
 */
exports.getRemote = function(id){

	if (id == null || id == "") {
		return {'errorMsg': "Parameter id is not valid"};
	}
    //TODO: maybe move getting the 'complete local surveys' to a separate function in this file?
    // get all surveys that are completely downloaded to the device
    var allSurveys = bdrs.mobile.survey.getLocal();
    
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
		return {'errorMsg': 'Error retrieving survey ' + response.status};
    } else {
        bdrs.mobile.Debug("finished survey download " + new Date());
        //success
        bdrs.mobile.Debug('Retrieved survey data package ' + response.status);
        return response;
    }
    
},

exports._save_taxon_groups = function(rawTaxonGroupArray) {
    //persist taxonGroups and its attributes and attribute-options
    for ( var i = 0; i < rawTaxonGroupArray.length; i++) {
        var taxonGroup = rawTaxonGroupArray[i];
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
    }
    waitfor() {
        persistence.flush(resume);
    }

    // precache all the taxon groups.
    var taxonGroupMap = {};
    var allTaxonGroups;
    waitfor(allTaxonGroups) {
        TaxonGroup.all().list(resume);
    }
    for (var i = 0; i < allTaxonGroups.length; i++) {
        var group = allTaxonGroups[i];
        taxonGroupMap[group.server_id()] = group;
    }
    bdrs.mobile.Debug('Cached taxon groups...');
    return taxonGroupMap;
};

exports._save_species_profiles = function(species, rawInfoItemArray) {

    for (var k = 0; k < rawInfoItemArray.length; k++) {
        var infoItem = rawInfoItemArray[k];

        var item = new SpeciesProfile({
            content : infoItem.content,
            weight : infoItem.weight,
            description : infoItem.description,
            type : infoItem.type,
            header : infoItem.header,
            server_id : infoItem.server_id,
            weight: infoItem.weight
        });
        
        // Get image from server if infoitem is an image
        if (bdrs.phonegap.isPhoneGap() && 
                (infoItem.type == "profile_img" 
                    || infoItem.type == "profile_img_40x40" 
                    || infoItem.type == "map_40x40" 
                    || infoItem.type == "silhouette")) {
            
            // This bit here will be VERY VERY slow
            // Not fixing right now because it is out of scope
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
                bdrs.mobile.Error('There was an error whilst downloading image UUID : ' 
                        + infoItem.content + ' Error : ' + response.status);
            } else {
                var image = new Image({
                    path : infoItem.content,
                    data : response.base64,
                    type : response.fileType
                });
                
                persistence.add(image);
                
                // Nooo don't flush just for kicks.
                waitfor() {
                    persistence.flush(resume);
                }
            }
        }
        
        // add info item to species
        species.infoItems().add(item);
    }  
};

exports._save_species = function(survey, taxonGroupMap, speciesMap, 
    rawIndicatorSpeciesArray, surveySpeciesServerIdArray) {

    bdrs.mobile.Debug("Starting to save " + rawIndicatorSpeciesArray.length + " species.");
    
    var species;
    var sp;
    var sp_server_id = [];
    for (var i = 0; i < rawIndicatorSpeciesArray.length; i++) {
        species = rawIndicatorSpeciesArray[i];
        if(speciesMap[species.server_id] === undefined) {
            
            // create a species
            sp = new Species({
                server_id : species.server_id,
                weight: species.weight,
                scientificNameAndAuthor : species.scientificNameAndAuthor,
                scientificName : species.scientificName,
                commonName : species.commonName,
                rank : species.taxonRank,
                author : species.author,
                year : species.year
            });

            // Create info item
            exports._save_species_profiles(sp, species.infoItems);
            
            // Save species
            persistence.add(sp);

            // Populate survey to species join table part 2
            // survey.species().add(sp);
            sp_server_id.push(species.server_id);

            // Do not store a reference to the species in order to save
            // on heap space.
            speciesMap[sp.server_id()] = null;

            // Save species in taxonGroup
            taxonGroupMap[species.taxonGroup].species().add(sp);

            // Incremental flush
            if (i % 500 === 0) {
                bdrs.mobile.Debug('Flushing species : ' + i);
                waitfor() {
                    persistence.flush(resume);
                }
                waitfor() {
                    bdrs.persistence.util.joinSurveyToSpecies(survey.server_id(), sp_server_id, resume, resume);
                    sp_server_id = [];
                }
            }
        }
    }

    bdrs.mobile.Debug("Flush final set of species: " + new Date());
    waitfor() {
        persistence.flush(resume);
    }
    waitfor() {
        bdrs.persistence.util.joinSurveyToSpecies(survey.server_id(), sp_server_id, resume, resume);
        sp_server_id = [];
    }

    bdrs.mobile.Debug("Done flush of species and attributes: " + new Date());
    return speciesMap;
};

/**
 * Stores surveydata in the survey that exists on the device.
 * @param   id      The id of the survey on the device that will get data stored inside of it.
 * @param   data    Survey specific data.
 * @return  survey  The survey that is stored on the device or a map with an errorMsg.
 */
exports.save = function(id, data) {
	
	if (id == null || id == "" || data == null || data == "") {
		return {"errorMsg" : "Survey save function received invalid parameters"};
	}
    var start = new Date().getTime();
    //get the survey from the device of which we want to add data to
    var survey;
    waitfor(survey){
        Survey.findBy('server_id', id, resume);
    }
    if (survey == null) {
    	return {"errorMsg" : "Failed saving data for survey, no survey found with server_id = " + id};
    }
    
    bdrs.mobile.Debug("Saving data for survey: " + survey.name());
    
    var taxonGroupMap = exports._save_taxon_groups(data.taxonGroups);

    // The following section of code will use raw SQL (instead of persistence)
    // to generate the rows in the species to survey join table. 
    // If we use persistence, it means that we need to pull data into memory
    // before we create individual insert statements. Not only is this very
    // slow, persistence will cache all the objects. The same effect
    // can be achieved using a single SQL statement.
    if(data.indicatorSpecies_server_ids.species.length === 0) {
        // Species use case where the survey is attached to all species.
        waitfor() {
            bdrs.persistence.util.joinSurveyToAllSpecies(survey.server_id(), resume, resume);
        }
    } else {
        waitfor() {
            bdrs.persistence.util.joinSurveyToSpecies(survey.server_id(), data.indicatorSpecies_server_ids.species, resume, resume);
        }
    }
    
    var surveySpeciesIds;
    waitfor(surveySpeciesIds) {
        bdrs.persistence.util.getAllSpeciesServerId(resume, resume);
    }

    // Generate a map {species.server_id : species} of the existing saved species.
    // We will not save these species again however we will need them
    // eventually to join them to the survey.
    var speciesMap = {};
    for(var q=0; q<surveySpeciesIds.length; q++) {
        speciesMap[surveySpeciesIds[q]] = null;
    }

    speciesMap = exports._save_species(survey, taxonGroupMap, speciesMap,
        data.indicatorSpecies, data.indicatorSpecies_server_ids.species);

    bdrs.mobile.survey._saveLocations(survey, data.locations);
    bdrs.mobile.survey._saveRecordProperties(survey, data);
    
    // save survey attributes and options in survey
    var a;
    var attribute;
    for ( var k = 0; k < data.attributesAndOptions.length; k++) {
        a = data.attributesAndOptions[k];
        attribute = new Attribute({
            server_id: a.server_id,
            weight: a.weight,
            typeCode: a.typeCode,
            required: a.required,
            name: a.name,
            description: a.description,
            tag: a.tag,
            scope: a.scope
        });
        var ao;
        var option;
        for ( var l = 0; l < a.options.length; l++) {
            ao = a.options[l];
            option = new AttributeOption({
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
    
    bdrs.mobile.Debug("Done linking species to survey: " + new Date());
     
    // Create Census Methods
    var flatCensusMethod;
    var censusMethod;
    for(var i=0; i<data.censusMethods.length; i++) {
        flatCensusMethod = data.censusMethods[i];
        censusMethod =  bdrs.mobile.survey._saveCensusMethod(flatCensusMethod);
        survey.censusMethods().add(censusMethod);
    }
    bdrs.mobile.Debug("Done with census methods: " + new Date());
    
    survey.local(true);

    // flush
    waitfor() {
        persistence.flush(resume);
    }
    
    bdrs.mobile.Debug("finish survey download");

    jQuery.mobile.pageLoading(true);
    
    var end = new Date().getTime();
    bdrs.mobile.Debug("Survey saved in: "+(end - start)+" ms");
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
    var flatAttr;
    var attr;
    for(var j=0; j<flatCensusMethod.attributes.length; j++) {
        flatAttr = flatCensusMethod.attributes[j];
        attr = bdrs.mobile.survey._saveAttribute(flatAttr);
        censusMethod.attributes().add(attr);
    }
    
    var flatSubCensusMethod;
    var subCensusMethod;
    for(var i=0; i<flatCensusMethod.censusMethods.length; i++) {
        flatSubCensusMethod = flatCensusMethod.censusMethods[i];
        subCensusMethod = bdrs.mobile.survey._saveCensusMethod(flatSubCensusMethod);
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
        weight: flatAttribute.weight,
        isDWC: false
    });
    persistence.add(attr);
    
    var flatOpt;
    for(var i=0; i<flatAttribute.options.length; i++) {
        flatOpt = flatAttribute.options[i];
        attr.options().add(new AttributeOption({
            value: flatOpt.value, 
            server_id: flatOpt.server_id,
            weight: flatOpt.weight
        }));
    }
    
    return attr;
};

/**
 * Persists the the recordProperties as Attributes and stores those in the survey.
 * @param survey in which we want to store the attributes.
 * @param recordProperties array of dwc fields.
 */
exports._saveRecordProperties = function(survey, data) {
	var recordProperty;
	var recordProperties = data.recordProperties;
	var attribute;
    var attributeType;
    var attributeOptions;
	
	if (recordProperties !== null) {
	    for (var i=0; i<recordProperties.length; i++) {
	    	recordProperty = recordProperties[i];
	    	
	    	switch (recordProperty.name) {
	    		case "Number":
		    	case "AccuracyInMeters":
		    		attributeType = bdrs.mobile.attribute.type.INTEGER_WITH_RANGE;
		    		attributeOptions = ['0','1000000'];
		    		break;
		    	case "Species":
		    		attributeType = bdrs.mobile.attribute.type.STRING_AUTOCOMPLETE_WITH_DATASOURCE;
		    		attributeOptions = [];
		    		break;
		    	case "Location":
		    		attributeType = bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES;
		    		attributeOptions = [];
		    		break;
		    	case "Point":
		    		attributeType = bdrs.mobile.attribute.type.LATITUDE_LONGITUDE;
		    		attributeOptions = [];
		    		break;
		    	case "When":
		    		attributeType = bdrs.mobile.attribute.type.DATE;
		    		attributeOptions = [];
		    		break;
		    	case "Time":
		    		attributeType = bdrs.mobile.attribute.type.TIME;
		    		attributeOptions = [];
		    		break;
		    	case "Notes":
		    		attributeType = bdrs.mobile.attribute.type.TEXT;
		    		attributeOptions = [];
		    		break;
		    	default:
		    		attributeType = bdrs.mobile.attribute.type.STRING;
		    		attributeOptions = [];
	    	}
	    	
	    	attribute = new Attribute({
	    		 server_id: null,
	             weight: recordProperty.weight,
	             typeCode: attributeType,
	             required: recordProperty.required,
	             name: recordProperty.name,
	             description: recordProperty.description,
	             tag: false,
	             scope: recordProperty.scope,
	             isDWC: true
	    	});
	    	bdrs.mobile.Debug("Number of attributeOptions = " + attributeOptions.length);
	    	var option;
	    	for(var j=0; j<attributeOptions.length; j++) {
	    		bdrs.mobile.Debug("Going to create a new option wit value " + attributeOptions[j] + " for attribute " + attribute.name());
	    		attribute.options().add(new AttributeOption({
	    			server_id: null,
	    			weight: null,
	    			value: attributeOptions[j]
	    		}));
	    	}
	    	survey.attributes().add(attribute);
	    }
	}

}

/**
 * Persists the locations them in the survey.
 * @param survey in which we want to store the attributes.
 * @param locations array of pointdata.
 */
exports._saveLocations = function(survey, locations) {
	var location;
    var point;
    for ( var j = 0; j < locations.length; j++) {
        location = locations[j];
        point = new bdrs.mobile.Point(location.location);
        survey.locations().add(new Location({
            server_id : location.server_id,
            weight: location.weight,
            name : location.name,
            latitude : point.getLatitude(),
            longitude : point.getLongitude() }));
    }
}

/**
 * Sets a specific survey as the default one.
 * @param   survey  The survey that needs to be made the default one.
 */
exports.makeDefault = function(survey){
    
	var setting;
	
    waitfor(setting) {
		 Settings.findBy('key', 'current-survey' , resume);
    }
    if (setting === null) {
        var s = new Settings({ key : 'current-survey', value : survey.name() });
        persistence.add(s);
        jQuery('.dashboard-status').empty();
        bdrs.template.render('dashboard-status', { name : survey.name() }, '.dashboard-status');

    } else {
		setting.value(survey.name());
        jQuery(setting).data('value', survey.name());
    }

    waitfor(setting) {
		 Settings.findBy('key', 'current-survey-id' , resume);
   }
    if (setting === null) {
        var s = new Settings({ key : 'current-survey-id', value : String(survey.server_id()) });
        persistence.add(s);
    } else {
		setting.value(String(survey.server_id()));
        jQuery(setting).data('value', String(survey.server_id()));
    }
    
	persistence.flush();   
	
}

/**
 * Gets the default survey.
 * @return the current survey.
 */
exports.getDefault = function(){
    var setting;
    waitfor(setting) {
        Settings.findBy('key', 'current-survey-id', resume);
    }
    var survey;
    waitfor(survey) {
        Survey.findBy('server_id', setting.value(), resume);
    }
    return survey
}



/**
 * Gets the local surveys.
 * @return an Array of Survey entities that are flagged local.
 */
exports.getLocal = function(){
    var localSurveys;
    waitfor(localSurveys) {
    	Survey.all().filter('local', '=', 'true').list(resume);
    }
    return localSurveys
}

/**
 * Gets the survey by it's serverId.
 * @param sid the id that the survey that we want has on the server 
 * @return the survey or null when not found.
 */
exports.getByServerId = function(sid) {
	var clickedSurvey;
	waitfor(clickedSurvey){
		Survey.findBy('server_id', sid, resume);
	}
	return clickedSurvey;
}

/**
 * Removes the records from a survey
 * @param survey from which the records need to be removed
 */
exports.removeRecords = function(survey) {
	var recordsList;
	waitfor(recordsList) {
		survey.records().list(resume);
	}
	bdrs.mobile.Debug("Deleting " + recordsList.length + " records from survey with server_id " + survey.server_id());
	recordsList.forEach(function(recordToDelete){
		bdrs.mobile.pages.trash._recurse_delete_record(recordToDelete);
	});
}

/**
 * Removes the profile from a particular species.
 * @param species from which we want to remove the profile.
 */
exports.removeSpeciesProfile = function(aSpecies) {
	var infoItems;
	waitfor (infoItems) {
		aSpecies.infoItems().list(resume);
	}
	//TODO: Replace with raw sql if this is too slow.
	for (var g=0; g<infoItems.length; g++) {
		persistence.remove(infoItems[g]);
	}
}

/**
 * Removes the species and their profile and count from a survey when not used by other surveys that exist on the device.
 * @param survey from which the species need to be removed.
 */
exports.removeSpecies = function(survey) {
	var species;
	waitfor (species) {
		survey.species().list(resume);
	}
	var aSpecies;
	for (var f=0; f<species.length; f++) {
		aSpecies = species[f];
		survey.species().remove(aSpecies);
		var surveyCount;
		waitfor (surveyCount) {
			aSpecies.surveys().count(resume);
		}
		if (surveyCount === 0) {
			bdrs.mobile.survey.removeSpeciesProfile(aSpecies);
			persistence.remove(aSpecies);
		}
	}
	//delete speciescounts
	var speciesCountToDelete;
	waitfor (speciesCountToDelete) {
		SpeciesCount.all().filter('survey','=',survey.id).list(resume);
	}
	//TODO: Replace with raw sql if this is too slow.
	for (var e=0; e<speciesCountToDelete.length; e++) {
		persistence.remove(speciesCountToDelete[e]);
	}
}

/**
 * Removes all taxonGroups and their related attributes and atribute-options that do not have any species attached to them.
 */
exports.removeTaxonGroups = function() {
	var taxonGroups;
	waitfor (taxonGroups) {
		TaxonGroup.all().list(resume);
	}
	var taxonGroup;
	var hasSpecies;
	for (var d=0; d<taxonGroups.length; d++) {
		taxonGroup = taxonGroups[d];
		waitfor (hasSpecies) {
			taxonGroup.species().count(resume);
		}
		if (hasSpecies === 0) {
			var attributes;
			waitfor (attributes) {
				taxonGroup.attributes().list(resume);
			}
			bdrs.mobile.attribute.removeAttributes(attributes);
			persistence.remove(taxonGroup);
		}
	}
}

/**
 * Removes censusmethods from the survey.
 * @param survey from which the censusmethods need to be removed.
 */
exports.removeCensusMethods = function (survey) {
	var censusMethods;
	waitfor (censusMethods) {
		CensusMethod.all().filter('survey','=',survey.id).and(new  persistence.PropertyFilter('parent','=',null)).list(resume);
	}
	for (var b=0; b<censusMethods.length; b++) {
		var censusMethod = censusMethods[b];
		bdrs.mobile.censusmethod.recurse_delete_censusmethod(censusMethod);
	}
}

/**
 * Removes a survey and all the related data that is not used by other surveys.
 * @return true if succesful or errorMsg when failed.
 */
exports.remove = function(){
	var sid = bdrs.mobile.getParameter('deleting-survey-id');
	if (sid == null || sid == "") {
		return {"errorMsg" : "Survey remove received an invalid parameter"};
	}
	//attach this function to 'finished syncing' event when syncing records to the server.
	if (bdrs.mobile.syncService._syncing) {
		bdrs.mobile.syncService.addSyncListener(bdrs.mobile.survey.remove);
		return {"errorMsg" : "Syncing records to server while trying to remove a survey. Trying to recover ...."};
	}
	var syncEventListenerIsRemoved = bdrs.mobile.syncService.removeSyncListener(bdrs.mobile.survey.remove);
	bdrs.mobile.survey.removingInProgress = true;
	jQuery.mobile.pageLoading(false);
	var surveyToDelete;
	waitfor(surveyToDelete) {
		Survey.findBy('server_id', sid, resume);
	}
	bdrs.mobile.survey.removeRecords(surveyToDelete);
	bdrs.mobile.survey.removeSpecies(surveyToDelete);
	bdrs.mobile.survey.removeTaxonGroups();
	var surveyAttributes;
	waitfor (surveyAttributes) {
		Attribute.all().filter('survey','=',surveyToDelete.id).list(resume);
	}
	bdrs.mobile.attribute.removeAttributes(surveyAttributes);
	bdrs.mobile.survey.removeCensusMethods(surveyToDelete);
	surveyToDelete.local(false);
	persistence.flush(function() {
		var defaultSurveyId;
		waitfor(defaultSurveyId) {
			Settings.findBy('key','current-survey-id',resume);
		}
		var defaultSurv;
		waitfor(defaultSurv) {
			Settings.findBy('key','current-survey',resume);
		}
		if(surveyToDelete.server_id() == defaultSurveyId.value()) {
			var localSurvey;
			waitfor(localSurvey){
				Survey.all().filter('local','=',true).one(resume);
			}
			if (localSurvey != null) {
				bdrs.mobile.survey.makeDefault(localSurvey);
				bdrs.mobile.Debug("Changed current survey to " + localSurvey.name());
			} else {
				//There are no local surveys, remove the current survey settings
				persistence.remove(defaultSurveyId);
				persistence.remove(defaultSurv);
				persistence.flush();
			}
		}
	});
	bdrs.mobile.survey.removingInProgress = false;
	return true;
}