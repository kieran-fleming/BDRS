exports._point_data = {};
exports._point_data.substrateMethod = null;
exports._point_data.substrateAttr = null;
exports._point_data.pointNumberAttr = null;
exports._point_data.obsMethod = null;
exports._point_data.obsHeightAttr = null;

exports._point_data.parentRecord = null;
exports._point_data.substrateRecord = null;
exports._point_data.substrateRecAttr = null;
exports._point_data.pointNumRecAttr = null;

exports._last_selected_substrate = null;

// { species.id : last_height_value}
exports._last_height = {};

/**
 * Invoked when the page is created.
 */
exports.Init =  function() {
    jQuery("#point-intersect-save").click(function() {
        if(bdrs.mobile.validation.isValidForm('#point-intersect-form')) {
            bdrs.mobile.pages.point_intersect._savePoint();
            jQuery.mobile.changePage("#review", {showLoadMsg: false});
        }
    });
    
    jQuery("#point-intersect-save-continue").click(function() {
        if(bdrs.mobile.validation.isValidForm('#point-intersect-form')) {
            bdrs.mobile.pages.point_intersect._savePoint();

            exports.Hide();
            exports._point_data.substrateRecord = new Record();
            exports._point_data.substrateRecord.deleted(false);
            
            exports.Show();
        }
    });
    
    jQuery("#pi-delete-substrate").click(function() {
        bdrs.mobile.pages.record.markRecordForDelete(exports._point_data.substrateRecord);
        jQuery.mobile.changePage("#review", {showLoadMsg: false});
        
    });
    
    jQuery('#record-gps').click(function (event) {
		var position;
		waitfor(position) {
			bdrs.mobile.geolocation.getCurrentPosition(resume);
		}
		if (position !== undefined) {
			var survey = bdrs.mobile.survey.getDefault();
			var spatialAttributes;
			waitfor(spatialAttributes) {
				survey.attributes().filter('name','=','Point').or(new persistence.PropertyFilter('name','=','AccuracyInMeters')).limit(2).list(resume);
			}
			if(spatialAttributes.length === 2) {
				jQuery('#record-attr-' + spatialAttributes[0].id +'-lat').val(bdrs.mobile.roundnumber(position.coords.latitude, 5));
	        	jQuery('#record-attr-' + spatialAttributes[0].id +'-lon').val(bdrs.mobile.roundnumber(position.coords.longitude, 5));
				jQuery('#record-attr-' + spatialAttributes[1].id).val(bdrs.mobile.roundnumber(position.coords.accuracy, 5));
			}
		} else {
			bdrs.mobile.Debug('Could not get GPS loc');
		}
	});
	
}

/**
 * Invoked when the page is displayed.
 */
exports.Show = function() {
	
	var currentSurvey = bdrs.mobile.survey.getDefault();
	var attributeValueMap = {};
	
    //////////////////////////////////////////////////////
    // Get the number of points in this transect
    //////////////////////////////////////////////////////
    var count;
    var isNewRecord = false;
    var record = exports._point_data.substrateRecord;
        
    // if new record
    if(exports._point_data.substrateRecord.parent() === null || exports._point_data.substrateRecord.parent() === undefined) {
		isNewRecord = true;
        // Adding a record
        waitfor(count) {
            Record.all().
                filter('parent','=',exports._point_data.parentRecord.id).
                filter('censusMethod','=',exports._point_data.substrateMethod.id).
                filter('deleted','=',false).
                count(resume);
        }      
    } else {
        // EDIT record form
        // get record and get all the attribute values
        count = '';
        var record = exports._point_data.substrateRecord;
        var attributeValues;
        waitfor(attributeValues) {
            record.attributeValues().prefetch('attribute').list(resume);
        }
        var recAttr;
        for(var i=0; i<attributeValues.length; i++) {
            recAttr = attributeValues[i];
            attributeValueMap[recAttr.attribute().id] = recAttr;
        }
    }
    
    jQuery('#point-intersect-sibling-count').text(count);
	
    //////////////////////////////////////////////////////
    // Get the substrate record attribute
    //////////////////////////////////////////////////////
	// TODO this can be cleaned up more.
	// Map of record attributes where { attributeID: attributeValueObj }
    var attributeValues;
    waitfor(attributeValues) {
        exports._point_data.substrateRecord.attributeValues().prefetch('attribute').list(resume);
    }
    var recAttr;
    for(var i=0; i<attributeValues.length; i++) {
        recAttr = attributeValues[i];
        attributeValueMap[recAttr.attribute().id] = recAttr;
    }
    
    ////////////////////////
    // Substrate Attribute
    ////////////////////////
    jQuery("#point-intersect-substrate-title").text(exports._point_data.substrateAttr.description());
    
    var curSubstrateRecAttr = attributeValueMap[exports._point_data.substrateAttr.id];
    if(curSubstrateRecAttr === undefined || curSubstrateRecAttr === null) {
        curSubstrateRecAttr = new AttributeValue();
        curSubstrateRecAttr.attribute(exports._point_data.substrateAttr);
    }
    exports._point_data.substrateRecAttr = curSubstrateRecAttr;
    
    var attrOpts;
    waitfor(attrOpts) {
        exports._point_data.substrateAttr.options().list(resume);
    }
    
    if(exports._last_selected_substrate === null && attrOpts.length > 0) {
        exports._last_selected_substrate = attrOpts[0].value();
    }
    
    for(var i=0; i<attrOpts.length; i++) {
        var attrOpt = attrOpts[i];
        var checked = false;
        
        if(curSubstrateRecAttr !== undefined && curSubstrateRecAttr.value().length > 0) {
            // If we are editing a current substrate record
            checked = curSubstrateRecAttr.value() === attrOpt.value();
        } else if(exports._last_selected_substrate !== null) {
            // Otherwise select the last picked substrate type
            checked = exports._last_selected_substrate === attrOpt.value();
        }
        
        var tmplParams = {
            id : attrOpt.id,
            displayName : attrOpt.value(),
            value : attrOpt.value(),
            checked: checked
        };
        waitfor() {
           bdrs.template.renderCallback('recordPointIntersect-substrate-radio', tmplParams, '#point-intersect-substrate', resume);
        }
    }

    ////////////////////////
    // Point Number
    ////////////////////////
    if(exports._point_data.pointNumberAttr !== null && exports._point_data.pointNumberAttr !== undefined) {
        var formField = new bdrs.mobile.attribute.AttributeValueFormField(exports._point_data.pointNumberAttr);
        waitfor() {
            formField.toFormField('#point-intersect-substrate-point-number', attributeValueMap[exports._point_data.pointNumberAttr.id], resume);
        }
        
        var pointNumElem = jQuery('#point-intersect-substrate-point-number').find('input');
        if(pointNumElem.val().trim().length === 0) {
            pointNumElem.val(count);
        }
        // Note that this may be null or undefined.
        exports._point_data.pointNumRecAttr = attributeValueMap[exports._point_data.pointNumberAttr.id];
    }
    
    ////////////////////////
    // Taxonomy Table
    ////////////////////////
    var obsRecords;
    waitfor(obsRecords) {
        exports._point_data.substrateRecord.children().filter('deleted','=',false).prefetch('species').list(resume);
    }
    for(var p=0; p<obsRecords.length; p++) {
        exports._insertObservationRow(obsRecords[p], obsRecords[p].species());
    }
        
    ////////////////////////
    // Taxonomy Quick List
    ////////////////////////
    
    // For the moment just grab a random sample of taxa
    var quickListTaxa;
    var survey = bdrs.mobile.survey.getDefault();
    waitfor(quickListTaxa) {
    	SpeciesCount.all().filter('survey','=',survey.id).order('userCount', false).limit(15).prefetch('species').list(resume);
    }
    for(var j=0; j<quickListTaxa.length; j++) {
        var taxon = quickListTaxa[j].species();
        var quickTaxonTmplParams = {
            id : taxon.id,
            displayName : taxon.scientificName(),
            value : taxon.scientificName()
        };
        waitfor() {
           bdrs.template.renderCallback('recordPointIntersect-taxonomy-btn', quickTaxonTmplParams, '#point-intersect-taxonomy-quicklist', resume);
        }
    }
    
    // Add the special 'other' button
    var quickTaxonTmplParams = {
        id : 'quick-taxon-other',
        displayName : 'Other',
        value : ''
    };
    waitfor() {
       bdrs.template.renderCallback('recordPointIntersect-taxonomy-btn', quickTaxonTmplParams, '#point-intersect-taxonomy-quicklist', resume);
    }
    
    // Attach the click handler
    jQuery(".pi-taxonomy-btn").click(function(event) {
        var btn = jQuery(event.currentTarget);
        var scientificName = btn.val();
        exports._insertObservationRow(null, exports._getSpeciesByScientificName(scientificName));
         bdrs.template.restyle('#point-intersect');
    });
    
    
     bdrs.template.restyle('#point-intersect');
    
    
    if (currentSurvey !== null) {
        jQuery.mobile.showPageLoadingMsg();
        exports._insertSurveyAttributes(record, currentSurvey, attributeValueMap);
        bdrs.mobile.pages.record._insertTaxonGroupAttributes(record.species(), attributeValueMap);
        jQuery.mobile.hidePageLoadingMsg();
    } else {
        bdrs.mobile.Error('Current Survey ID not known');
        return;
    }
    
    // This is down here because it crashes out on desktop machines with GPS @TODO
	var latCoord;
	var lonCoord;
	var accuracy;
	if(record.latitude() === null && record.longitude() === null && record.accuracy() === null) {
		var position;
		waitfor(position) {
			bdrs.mobile.geolocation.getCurrentPosition(resume);
		}
		if (position !== undefined) {
			latCoord = bdrs.mobile.roundnumber(position.coords.latitude, 5);
			lonCoord = bdrs.mobile.roundnumber(position.coords.longitude, 5);
			accuracy = bdrs.mobile.roundnumber(position.coords.accuracy, 5);
		}
	} else {
	    latCoord = record.latitude();
	    lonCoord = record.longitude();
		accuracy = record.accuracy();
	}
	
	 var dwcAttributes;
	    waitfor(dwcAttributes) {
	    	currentSurvey.attributes().filter('isDWC', '=', true).list(resume);
	    }
	    
	    for (var i=0; i<dwcAttributes.length; i++) {
	    	var attribute = dwcAttributes[i]; 
	    	switch(attribute.name()) {
		    	case "Point" :
		    	    	jQuery('#record-attr-' + attribute.id + '-lat').val(latCoord);
		    	        jQuery('#record-attr-' + attribute.id + '-lon').val(lonCoord);
		    		break;
		    	case "AccuracyInMeters" :
		    		jQuery('#record-attr-' + attribute.id).val(accuracy);
		    		break;
		    	case "When" :
		    		if(isNewRecord) {
		    	    	jQuery('#record-attr-' + attribute.id).datepicker("setDate", new Date());
		    	    }
		    		break;
		    	case "Time" :
		    	    if(isNewRecord) {
		    	    	jQuery('#record-attr-' + attribute.id).val(bdrs.mobile.getCurrentTime());
		    	    }
		    		break;
	    	}
	    }
}
	
/**
 * Invoked when the page is hidden.
 */
exports.Hide = function() {
	bdrs.mobile.Debug('Point Intersect Form Hide');
	
	jQuery("#point-intersect-substrate").empty();
    jQuery("#point-intersect-substrate-point-number").empty();
	jQuery("#point-intersect-taxonomy-quicklist").empty();
	jQuery("#point-intersect-species-table-content").empty();
    jQuery("#pi-obs-index").val(0);
	exports._point_data.substrateRecord = null;
	exports._point_data.substrateRecAttr = null;
	
    bdrs.mobile.removeParameter("censusMethodId");
    bdrs.mobile.removeParameter("selected-record");
    bdrs.mobile.removeParameter('record-parent');
}

exports.isPointIntersect = function(cmethod, parentRecord, substrateRecord) {
    bdrs.mobile.Debug("Testing for Point Intersect");
    var isPointIntersect = false;

    // The census method must be called POINT_INTERSECT_SUBSTRATE
    if(cmethod.type() === 'POINT_INTERSECT_SUBSTRATE') {
        bdrs.mobile.Debug("Point Intersect Substrate Method Found.");

        exports._point_data.substrateMethod = cmethod;

        // The census method __may__ also have at least one attribute of type IN with name POINT_NUMBER.
        var pointNumAttrs = 
            bdrs.mobile.pages.record._getCensusMethodAttribute(cmethod,'POINT_NUMBER',bdrs.mobile.attribute.type.INTEGER);
        exports._point_data.pointNumberAttr = pointNumAttrs.length === 0 ? null : pointNumAttrs[0];
        
        // The census method must have at least one attribute of type SV with name SUBSTRATE.
        var substrateMethodAttributes = 
            bdrs.mobile.pages.record._getCensusMethodAttribute(cmethod,'SUBSTRATE',bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES);
        if(substrateMethodAttributes.length > 0) {
            bdrs.mobile.Debug("Point Intersect Substrate Attribute Found");
            exports._point_data.substrateAttr = substrateMethodAttributes[0];

            // The substrate method must have at least one optionally taxonomic child method called OBSERVATION             
            var obsMethod;
            waitfor(obsMethod) {
                cmethod.children().
                    filter('type','=','POINT_INTERSECT_SUBSTRATE_OBSERVATION').
                    filter('taxonomic','=',bdrs.mobile.pages.record.TAXONOMIC).
                    order('weight', true).
                    list(resume);
            }
            if(obsMethod.length > 0) {
                bdrs.mobile.Debug("Point Intersect Substrate Observation Method Found");
                exports._point_data.obsMethod = obsMethod[0];
                
                // The observation method must have at least one height attribute.
                var heightAttr = 
                    bdrs.mobile.pages.record._getCensusMethodAttribute(obsMethod[0],'HEIGHT',bdrs.mobile.attribute.type.DECIMAL);
                if(heightAttr.length > 0) {
                    bdrs.mobile.Debug("Point Intersect Substrate Observation Height Attribute Found");
                    exports._point_data.obsHeightAttr = heightAttr[0];
                    
                    // And finally if you get all the way down here, congratulations, you have a 
                    // point intersect module.
                    isPointIntersect = true;
                    exports._point_data.parentRecord = parentRecord;
                    exports._point_data.substrateRecord = substrateRecord;

                    bdrs.mobile.Debug("isPointIntersect = " + isPointIntersect);
                }
            }
        }
    }
    
	bdrs.mobile.Debug('isPointIntersect: ' + isPointIntersect);
    return isPointIntersect;
};

exports._insertObservationRow = function(obsRecord, species) {
    var id;
    var height = '';
    if(obsRecord === null || obsRecord === undefined) {
        // Adding a new row
        // Get and increment the index
        var indexElem = jQuery("#pi-obs-index");
        var index = parseInt(indexElem.val(),10);
        indexElem.val(index+1);
        id = index;
        
        height = ''
        if(species !== undefined && 
            species !== null && 
            exports._last_height[species.id] !== undefined) {
            height = exports._last_height[species.id];
        }
        
    } else {
        // Adding an editing row
        id = obsRecord.id;

        var heightRecAttr;
        waitfor(heightRecAttr) {
            AttributeValue.all().
                filter('record','=',obsRecord.id).
                filter('attribute','=',exports._point_data.obsHeightAttr.id).
                order('weight', true).
                one(resume);
        };
        if(heightRecAttr !== undefined && heightRecAttr !== null) {
            height = heightRecAttr.value();
        }

    }
    
    // Render the content layout where we will later inject the content.
    var rowTmplParams = { id: id };
    waitfor() {
        bdrs.template.renderCallback('three-col-content', rowTmplParams, '#point-intersect-species-table-content', resume);
    }
    
    // Injecting the Taxonomy
    var speciesTmplParams = { 
        required: true, 
        id: id, 
        value: species !== null && species !== undefined ? species.scientificName() : ''
    };
    waitfor() {
        bdrs.template.renderCallback('recordPointIntersect-species', speciesTmplParams, '#block-a-'+rowTmplParams.id, resume);
    }
    
    jQuery('#pi-taxonomy-species-'+speciesTmplParams.id).autocomplete({
	    source: function(request, response) {
	    	var currentSurvey = bdrs.mobile.survey.getDefault();
			//get species for current survey
    		currentSurvey.species().filter('scientificName','like','%' + request.term + '%').or(new persistence.PropertyFilter('commonName','like','%' + request.term + '%')).limit(5).list(null,function(speciesList){
    			var names = [];
    			speciesList.forEach(function(aSpecies){
    				//add the names of the found species to the response
                    names.push({ label : aSpecies.commonName() + ' - <i>' + aSpecies.scientificName()+ '</i>', value : aSpecies.scientificName()});
    			});
    			response(names);
    		});
	    },
	    change: function(event, ui) {
        },
        open: function(event, ui){
        	jQuery('.ui-autocomplete').css('z-index',100);
        },
	    html: true
    });
    
    // Injecting the height
    var heightTmplParams = { id: id, value: height };
    waitfor() {
        bdrs.template.renderCallback('recordPointIntersect-height', heightTmplParams, '#block-b-'+rowTmplParams.id, resume);
    }

    // Delete button
    var delTmplParams = {id:id};
    waitfor(){
        bdrs.template.renderCallback('recordPointIntersect-delete', delTmplParams, '#block-c-'+rowTmplParams.id, resume);
    }
    var delButton = jQuery("a#"+id)
    delButton.jqmData('id', id);
    delButton.click(exports._delete_observation);
};

/**
 * Insert survey attributes into the record form.
 * 
 * @param record The object that backs the current form.
 * @param survey The survey containing the attributes to be added to the form.
 * @param attributeValueMap a map of record attributes keyed against the 
 * attribute ID. That is, { attributeID: attributeValueObj }.
 */
exports._insertSurveyAttributes = function(record, survey, attributeValueMap) {	
    var attributes;
    waitfor(attributes) {
        survey.attributes().order('weight', true).list(resume);
    }
    // Clear old attributes and hide the collapsible.
    var surveyAttrsWrapperElem = jQuery('#point-intersect-survey-attributes');
    surveyAttrsWrapperElem.empty();
    // Insert record attributes
    var recAttrFormField;
    var recPropertyAttrFormField;
    for (var i = 0; i < attributes.length; i++) {
    	var attribute = attributes[i]; 
        recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attribute);
        recAttrFormField.toFormField('#point-intersect-survey-attributes', attributeValueMap[attribute.id], record);
    }
};

exports._getCensusMethodAttribute = function(cmethod, attrName, attrTypeCode) {
    var methodAttributes;
    waitfor(methodAttributes) {
        cmethod.attributes().
            filter('name','=',attrName).
            filter('typeCode','=',attrTypeCode).
            order('weight', true).
            list(resume);
    }
    return methodAttributes;
};

exports._savePoint = function() {
    // Need to blur to force the species autocomplete to insert any 
    // required dom elements. 
    jQuery(":focus").blur();
    
    jQuery.mobile.showPageLoadingMsg();
	bdrs.mobile.Debug ('Save Point Called');
	
	var attributeValueMap = {};
	var survey = bdrs.mobile.survey.getDefault();
    
	// Save the substrate first
	var substrateRec = exports._point_data.substrateRecord;
	substrateRec.parent(exports._point_data.parentRecord);
	substrateRec.censusMethod(exports._point_data.substrateMethod);
	
	 //GET THE DWC attributes from the survey
    //USE the id from the DWC species attribute to retrieve the value. E.g.#record-attr-56724547867
    var DWC_attributes;
    waitfor(DWC_attributes) {
    	survey.attributes().filter("isDWC", '=', true).order('weight', true).list(resume);
    }
    var DWC_attributesMap = {};
    var DWC_attribute;
    for (var i=0; i<DWC_attributes.length; i++) {
    	DWC_attribute = DWC_attributes[i];
    	DWC_attributesMap[DWC_attribute.name()] = DWC_attribute; 
    }
    
    // Dwc values that exist in the form will be stored in the record
    if(DWC_attributesMap['Notes'] !== undefined) {
    	var notesElem = jQuery('#record-attr-' + DWC_attributesMap['Notes'].id);
    	if (notesElem !== null) {
    		substrateRec.notes(jQuery(notesElem).val());
    	}
    }
    if(DWC_attributesMap['Number'] !== undefined) {
    	var numberElem = jQuery('#record-attr-' + DWC_attributesMap['Number'].id);
    	if (numberElem !== null) {
    		substrateRec.number(jQuery(numberElem).val());
    	}
    }
    
    var lat;
    var lon;
    if(DWC_attributesMap['Point'] !== undefined) {
    	var latElem = jQuery('#record-attr-' + DWC_attributesMap['Point'].id + '-lat');
    	var lonElem = jQuery('#record-attr-' + DWC_attributesMap['Point'].id + '-lon');
    	if (latElem !== null) {
    		lat = jQuery(latElem).val();
    		substrateRec.latitude(lat);
    	}
    	if (lonElem !== null) {
    		lon = jQuery(lonElem).val();
    		substrateRec.longitude(lon);
    	}
    }
    if(DWC_attributesMap['When'] !== undefined) {
    	var whenElem = jQuery('#record-attr-' + DWC_attributesMap['When'].id);
    	if (whenElem !== null) {
    		var when = bdrs.mobile.parseDate(jQuery(whenElem).val());
    		substrateRec.when(when);
    	}
    }
    if(DWC_attributesMap['Time'] !== undefined) {
    	var timeElem = jQuery('#record-attr-' + DWC_attributesMap['Time'].id);
    	if (timeElem !== null) {
    	    var time = jQuery(timeElem).val(); 
    	    substrateRec.time(time);
    	}
    }
    var accuracy;
    if(DWC_attributesMap['AccuracyInMeters'] !== undefined) {
    	var accuracyElem = jQuery('#record-attr-' + DWC_attributesMap['AccuracyInMeters'].id);
    	if (accuracyElem !== null) {
    	    accuracy = jQuery(accuracyElem).val(); 
    	    substrateRec.accuracy(accuracy);
    	}
    }
    var now = new Date();
    substrateRec.modifiedAt(now);
    
    if(DWC_attributesMap['Location'] !== undefined) {
    	var locationElem = jQuery('#record-attr-' + DWC_attributesMap['Location'].id);
    	if (locationElem !== null) {
    	    var serveridLatLon = jQuery(locationElem + ":selected").val();
    	    var serveridLatLonArray = serveridLatLon.split(":");
    	    var locationServerId = serveridLatLonArray[0];
    	    if (locationServerId !== undefined && locationServerId !== null && locationServerId !== "") {
    	    	var location;
    	    	waitfor(location) {
    	    		Location.findBy('server_id', locationServerId, resume);
    	    	}
	    		if(location !== null) {
	    			location.records().add(substrateRec)
	    		}
    	    }
    	}
    }

	substrateRec.survey(survey);
	
	 // Survey Attributes
    var attributes;
    waitfor(attributes) {
        survey.attributes().order('weight', true).list(resume);
    }

    var recAttrFormField;
    for (var i = 0; i < attributes.length; i++) {
        recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attributes[i]);
        recAttrFormField.fromFormField('#point-intersect-survey-attributes', substrateRec.attributeValues(), attributeValueMap[attributes[i].id]);   
    }
	
	
	var substrateRecAttr = exports._point_data.substrateRecAttr;
	substrateRec.attributeValues().add(substrateRecAttr);
	substrateRecAttr.value(jQuery("[name=radio-choice-pi-substrate]:checked").val());
	exports._last_selected_substrate = substrateRecAttr.value();

    if(exports._point_data.pointNumberAttr !== null && exports._point_data.pointNumberAttr !== undefined) {
        //exports._point_data.pointNumRecAttr
        var formField = new bdrs.mobile.attribute.AttributeValueFormField(exports._point_data.pointNumberAttr);
        formField.fromFormField('#point-intersect-substrate-point-number', substrateRec.attributeValues(), exports._point_data.pointNumRecAttr);     
    }
	    
    // Save the updated observed species
    var substrateRecChildren;
    waitfor(substrateRecChildren) {
        substrateRec.children().filter('deleted','=',false).list(resume);
    }

    //---------------------------------
    //-- Updating species and Heights--
    //---------------------------------
	bdrs.mobile.Debug("About to update " + substrateRecChildren.length + " substrateRecChildren.");
    for(var y=0; y<substrateRecChildren.length; y++) {
        var obsRec = substrateRecChildren[y];
        var currentSpecies = obsRec.species();
        var speciesElem = jQuery("#pi-taxonomy-species-"+obsRec.id);
        var heightElem = jQuery("#pi-taxonomy-height-"+obsRec.id);
        bdrs.mobile.Debug("About to UPDATE " + currentSpecies.scientificName() + " WITH " + speciesElem.val());
        
        if(speciesElem.length > 0 && heightElem.length > 0) {
        	
        	bdrs.mobile.Debug("Trying to RETRIEVE " + speciesElem.val() + " FROM the database");
        	var updatedSpecies = exports._getSpeciesByScientificName(speciesElem.val());
        	
            obsRec.modifiedAt(now);
	        obsRec.when(now);
	        obsRec.time([now.getHours(), now.getMinutes(), now.getSeconds()].join(':'));
	        obsRec.latitude(lat);
	        obsRec.longitude(lon);
	        obsRec.accuracy(accuracy);
	        
	        if (updatedSpecies === null) {
	        	bdrs.mobile.Debug("SPECIES "  + speciesElem.val() + " does not exist in the database");
	        	bdrs.mobile.Debug("Creating a new field species for it");
        		var sp = new Species({
        			scientificNameAndAuthor: "Field Species",
        	    	scientificName: speciesElem.val(),
        	    	commonName: speciesElem.val(),
        	    	rank: "Field Species",
        	    	author: "Field Species",
        	    	year: ""
        		});
        		persistence.add(sp);
        		
        		//TODO: refactor counter
        		bdrs.mobile.Debug("Creating a COUNTER for new fieldspecies with scientific name " +  sp.scientificName());
                var fieldSpeciesCounter = new SpeciesCount({scientificName: sp.scientificName(), count: 0 , userCount: 0});
                persistence.add(fieldSpeciesCounter);
                fieldSpeciesCounter.species(sp);
                fieldSpeciesCounter.survey(survey);
                
                bdrs.mobile.Debug("Trying to RETRIEVE taxongroup FOR " + sp.scientificName() + " FROM the database");
        		var tg;
        		waitfor(tg) {
        			TaxonGroup.all().filter('name', '=', 'Field Species').one(resume);
        		}
        		if (tg == null) {
        			bdrs.mobile.Debug("taxongroup for SPECIES "  + sp.scientificName() + " does not exist in the database");
    	        	bdrs.mobile.Debug("Creating a new field taxongroup for it");
        			tg = new TaxonGroup({
        				name: "Field Species"
        			});
        			persistence.add(tg);
        		}
        		
        		tg.species().add(sp);
        		survey.species().add(sp);
        		updatedSpecies = sp;
        		waitfor() {
        			persistence.flush(resume);
        		}
	        }
	        
	        bdrs.mobile.Debug("REPLACING old species " + currentSpecies.scientificName() + " WITH " + updatedSpecies.scientificName());
	        obsRec.species(updatedSpecies);
	        
	        //TODO: refactor counter
	        bdrs.mobile.Debug("Trying to RETRIEVE counter FOR " + currentSpecies.scientificName() + " FROM the database");
	        var currentSpeciescounter;
	        waitfor(currentSpeciescounter) {
                SpeciesCount.all().filter('scientificName','=', currentSpecies.scientificName()).and(new persistence.PropertyFilter('survey','=',survey.id)).one(resume);
            }
	        
        	if(currentSpeciescounter !== null){
        		bdrs.mobile.Debug(currentSpecies.scientificName() + ": " + currentSpeciescounter.count() + " -1 ");
        		currentSpeciescounter.count(currentSpeciescounter.count() - 1);
        		currentSpeciescounter.userCount(currentSpeciescounter.userCount()- 1);
        	}
        	
        	bdrs.mobile.Debug("Trying to RETRIEVE counter FOR " + updatedSpecies.scientificName() + " FROM the database");
        	var updatedSpeciescounter;
	        waitfor(updatedSpeciescounter) {
                SpeciesCount.all().filter('scientificName','=', updatedSpecies.scientificName()).and(new persistence.PropertyFilter('survey','=',survey.id)).one(resume);
            }
	        
	        if(updatedSpeciescounter !== null){
	        	bdrs.mobile.Debug(updatedSpecies.scientificName() + ": " + updatedSpeciescounter.count() + " +1 ");
	        	updatedSpeciescounter.count(updatedSpeciescounter.count() + 1);
	        	updatedSpeciescounter.userCount(updatedSpeciescounter.userCount() + 1);
        	} else {
	        	bdrs.mobile.Debug("Counter FOR "  + updatedSpecies.scientificName() + " does not exist in the database");
	        	bdrs.mobile.Debug("Creating a new species counter for it and setting it to +1");
        		updatedSpeciescounter = new SpeciesCount({
                    scientificName: updatedSpecies.scientificName(), 
					count: 1,
                    userCount: 1
                });
				persistence.add(updatedSpeciescounter);
				updatedSpeciescounter.species(updatedSpecies);
				updatedSpeciescounter.survey(survey);
        	}
        	
	        bdrs.mobile.Debug("Trying to RETRIEVE height from existing record FROM the database");
	        var heightRecAttr;
            waitfor(heightRecAttr) {
                AttributeValue.all().
                    filter('record','=',obsRec.id).
                    filter('attribute','=',exports._point_data.obsHeightAttr.id).
                    order('weight', true).
                    one(resume);
            };
            
            if(heightRecAttr === undefined || heightRecAttr === null) {
	        	bdrs.mobile.Debug("Height for existing record does not exist in the database");
	        	bdrs.mobile.Debug("Creating a new height attribute for this record");
                heightRecAttr = new AttributeValue();
	            obsRec.attributeValues().add(heightRecAttr);
                heightRecAttr.attribute(exports._point_data.obsHeightAttr);
            }
            
            bdrs.mobile.Debug("REPLACING old height WITH new height");
            heightRecAttr.value(heightElem.val());
            exports._last_height[obsRec.species.id] = heightRecAttr.value();
        }
    }
	
    //-----------------------------------
    //-- Adding new species and Heights--
    //-----------------------------------
    bdrs.mobile.Debug("About to create new species and heights");
    var index = parseInt(jQuery("#pi-obs-index").val(),10);
    for(var i=0; i<index; i++) {
    	
        var speciesElem = jQuery("#pi-taxonomy-species-"+i);
        var heightElem = jQuery("#pi-taxonomy-height-"+i);
        bdrs.mobile.Debug("About to CREATE species " + speciesElem.val());
        
        if(speciesElem.length > 0 && heightElem.length > 0) {
            // Observation Record
            var obsRec = new Record();
            obsRec.deleted(false);
            obsRec.parent(substrateRec);
	        obsRec.censusMethod(exports._point_data.obsMethod);
            obsRec.survey(survey);
	        obsRec.modifiedAt(now);
	        obsRec.when(now);
	        obsRec.time([now.getHours(), now.getMinutes(), now.getSeconds()].join(':'));
	        obsRec.latitude(lat);
	        obsRec.longitude(lon);
	        obsRec.accuracy(accuracy);

	        bdrs.mobile.Debug("Trying to RETRIEVE " + speciesElem.val() + " FROM the database");
			var species = exports._getSpeciesByScientificName(speciesElem.val())
			if (species !== null) {
				bdrs.mobile.Debug(species.commonName());
				bdrs.mobile.Debug(species.scientificName());
			} else if (speciesElem.val() !== '') {
				bdrs.mobile.Debug("SPECIES "  + speciesElem.val() + " does not exist in the database");
	        	bdrs.mobile.Debug("Creating a new field species for it");
				var sp = new Species({
					scientificNameAndAuthor: "Field Species",
			    	scientificName: speciesElem.val(),
			    	commonName: speciesElem.val(),
			    	rank: "Field Species",
			    	author: "Field Species",
			    	year: ""
				});
				
                bdrs.mobile.Debug("Trying to RETRIEVE taxongroup FOR " + sp.scientificName() + " FROM the database");
				var tg;
				waitfor(tg) {
					TaxonGroup.all().filter('name', '=', 'Field Species').one(resume);
				}
				if (tg === null) {
					bdrs.mobile.Debug("taxongroup for SPECIES "  + sp.scientificName() + " does not exist in the database");
    	        	bdrs.mobile.Debug("Creating a new field taxongroup for it");
					tg = new TaxonGroup({
						name: "Field Species"
					});
					persistence.add(tg);
        		}
				persistence.add(sp);
				tg.species().add(sp);
				survey.species().add(sp);
				species = sp;
				waitfor() {
					persistence.flush(resume);
				}
			}
			bdrs.mobile.Debug("ADDING species " + species.scientificName() + " to observation record");
	        obsRec.species(species);
	        
	        bdrs.mobile.Debug("Creating a new height attribute with value " + heightElem.val());
	        var heightRecAttr = new AttributeValue();
	        obsRec.attributeValues().add(heightRecAttr);
            heightRecAttr.attribute(exports._point_data.obsHeightAttr);
	        heightRecAttr.value(heightElem.val());
            exports._last_height[obsRec.species().id] = heightRecAttr.value();
	        
            //TODO: refactor species count
	        // Update Species Count index.
            bdrs.mobile.Debug("Trying to RETRIEVE counter FOR " + obsRec.species().scientificName() + " FROM the database");
        	var counter;
        	waitfor(counter) {
				SpeciesCount.all().filter('scientificName', '=', obsRec.species().scientificName()).one(resume);
        	}
        	if (counter === null) {
        		bdrs.mobile.Debug("Counter FOR "  + obsRec.species().scientificName() + " does not exist in the database");
	        	bdrs.mobile.Debug("Creating a new species counter for it and setting it to +1");
				counter = new SpeciesCount({
                    scientificName: obsRec.species().scientificName(), 
					count: 1,
                    userCount: 1
                });
				persistence.add(counter);
				counter.species(obsRec.species());
        	} else {
        		bdrs.mobile.Debug(obsRec.species().scientificName() + ": " + counter.count() + " +1 ");
        		counter.count(counter.count() + 1);
                counter.userCount(counter.userCount() + 1);
        	}
        	counter.survey(survey);
        }
    }

	persistence.flush();
	jQuery.mobile.hidePageLoadingMsg();
};

exports._getSpeciesByScientificName = function(scientificName) {
	var species;
	waitfor(species) {
		Species.all().filter('scientificName', '=', scientificName).prefetch('taxonGroup').one(resume);
	}
	return species;
};

/**
 * Deletes an observation record. 
 */
exports._delete_observation = function(event) {
    var button = jQuery(event.currentTarget);
    // This record Id may be an index or a primary key.
    var recordId = button.jqmData('id');
    var record;
    waitfor(record) {
        Record.all().filter('id', '=', recordId).one(resume);
    }

    // If the id was a primary key, then retrieve the record and mark it deleted.
    if(record !== undefined && record !== null) {
        bdrs.mobile.pages.record.markRecordForDelete(record);
    }

    jQuery("#block-a-"+recordId+", #block-b-"+recordId+", #block-c-"+recordId).remove();
};

