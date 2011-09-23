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
    
    for (var i = 0; i < rawIndicatorSpeciesArray.length; i++) {
        var species = rawIndicatorSpeciesArray[i];
        if(speciesMap[species.server_id] === undefined) {
            
            // create a species
            var sp = new Species({
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
            survey.species().add(sp);
            speciesMap[sp.server_id()] = sp;

            // Save species in taxonGroup
            taxonGroupMap[species.taxonGroup].species().add(sp);

            // Incremental flush
            if (i % 500 == 0) {
                bdrs.mobile.Debug('Flushing species : ' + i);
                waitfor() {
                    persistence.flush(function() {
                        resume();
                    });
                }
            }
        }
    }

    bdrs.mobile.Debug("Flush final set of species: " + new Date());
    waitfor() {
        persistence.flush(function() {
            resume();
        });
    }
    bdrs.mobile.Debug("Done flush of species and attributes: " + new Date());
    return speciesMap;
};

/**
 * Stores surveydata in the survey that exists on the device.
 * @param   id      The id of the survey on the device that will get data stored inside of it.
 * @param   data    Survey specific data.
 * @return  survey  The survey that is stored on the device.
 */
exports.save = function(id, data) {
    var start = new Date().getTime();
    //get the survey from the device of which we want to add data to
    var survey;
    waitfor(survey){
        Survey.findBy('server_id', id, resume);
    }
    bdrs.mobile.Debug("Saving data for survey: " + survey.name());

    var taxonGroupMap = exports._save_taxon_groups(data.taxonGroups);

    // Generate a map {species.server_id : species} of the existing saved species.
    // We will not save these species again however we will need them
    // eventually to join them to the survey.
    var surveySpeciesIds;
    if(data.indicatorSpecies_server_ids.species.length === 0) {
        // Species use case where the survey is attached to all species.
        surveySpeciesIds = [];
        for(var g=0; g<data.indicatorSpecies.length; g++) {
            surveySpeciesIds.push(data.indicatorSpecies[g].server_id);
        }
    } else {
        surveySpeciesIds = data.indicatorSpecies_server_ids.species;
    }

    var speciesMap = {};
    if (surveySpeciesIds.length !== 0) {
        // Persistence seems to have this limitation where 'in' clauses cannot
        // have more than 999 items in the list.
        bdrs.mobile.Debug("Temporarily disabling persistence debug to prevent spamming the console.");
        var persistence_debug_state = persistence.debug;
        persistence.debug = false;

        while (surveySpeciesIds.length > 0) {
            var idList = surveySpeciesIds.splice(0,Math.min(999,surveySpeciesIds.length));

            var existingSpecies;
            waitfor(existingSpecies) {
                Species.all().filter('server_id', 'in', idList).list(resume);
            }

            for (var q=0;q<existingSpecies.length; q++) {
                var species = existingSpecies[q];
                speciesMap[species.server_id()] = species;

                // Populate survey to species join table part 1
                survey.species().add(species);
            }
        }
        persistence.debug = persistence_debug_state;
    }

    speciesMap = exports._save_species(survey, taxonGroupMap, speciesMap,
        data.indicatorSpecies, data.indicatorSpecies_server_ids.species);

    // Save survey locations in survey
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
    
    bdrs.mobile.Debug("Done linking species to survey: " + new Date());
     
    // Create Census Methods
    for(var i=0; i<data.censusMethods.length; i++) {
        var flatCensusMethod = data.censusMethods[i];
        var censusMethod =  bdrs.mobile.survey._saveCensusMethod(flatCensusMethod);
        survey.censusMethods().add(censusMethod);
    }
    bdrs.mobile.Debug("Done with census methods: " + new Date());
    
    survey.local(true);

    // flush
    waitfor() {
        persistence.flush(resume);
    }
    
    jQuery('.bdrs-page-download-surveys label[for="checkbox-' + id + '"]').addClass('local');
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
 * @param   survey  The survey that needs to be made the default one.
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

