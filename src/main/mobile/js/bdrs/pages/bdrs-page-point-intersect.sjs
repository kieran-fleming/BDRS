exports._point_data = {};
exports._point_data.substrateMethod = null;
exports._point_data.substrateAttr = null;
exports._point_data.obsMethod = null;
exports._point_data.obsHeightAttr = null;
exports._point_data.obsFloweringAttr = null;

exports._point_data.parentRecord = null;
exports._point_data.substrateRecord = null;
exports._point_data.substrateRecAttr = null;

exports._last_selected_substrate = null;

/**
 * Invoked when the page is created.
 */
exports.Create =  function() {
    jQuery("#point-intersect-save").click(function() {
        if(bdrs.mobile.validation.isValidForm('#point-intersect-form')) {
            bdrs.mobile.pages.point_intersect._savePoint();
            jQuery.mobile.changePage("#review", "slide", false, true);
        }
    });
    
    jQuery("#point-intersect-save-continue").click(function() {
        if(bdrs.mobile.validation.isValidForm('#point-intersect-form')) {
            bdrs.mobile.pages.point_intersect._savePoint();

            exports.Hide();
            exports._point_data.substrateRecord = new Record();
            
            exports.Show();
        }
    });
    
    
    jQuery('#pi-gps').click(function (event) {
		var position;
		waitfor(position) {
			bdrs.mobile.geolocation.getCurrentPosition(resume);
		}
		if (position !== undefined) {
			jQuery('#pi-latitude').val(bdrs.mobile.roundnumber(position.coords.latitude, 5));
        	jQuery('#pi-longitude').val(bdrs.mobile.roundnumber(position.coords.longitude, 5));
			jQuery('#pi-accuracy').val(bdrs.mobile.roundnumber(position.coords.accuracy, 5));
		} else {
			bdrs.mobile.Debug('Could not get GPS loc');
		}
	});
	
	jQuery('#pi-when').datepicker();
	jQuery('#pi-time').timepicker();
}

/**
 * Invoked when the page is displayed.
 */
exports.Show = function() {
	var setting;
	waitfor(setting) {
		Settings.findBy('key', 'current-survey-id' , resume);
	}
	
    //////////////////////////////////////////////////////
    // Get the number of points in this transect
    //////////////////////////////////////////////////////
    var count;
    var isNewRecord = false;
    var record = exports._point_data.substrateRecord;
        
    // if new record/.
    if(exports._point_data.substrateRecord.parent() === null || exports._point_data.substrateRecord.parent() === undefined) {
		isNewRecord = true;
        // Adding a record
        waitfor(count) {
            Record.all().
                filter('parent','=',exports._point_data.parentRecord.id).
                filter('censusMethod','=',exports._point_data.substrateMethod.id).
                count(resume);
        }
        count += 1;
    	// Setup default form values...
       	bdrs.mobile.Debug('Default Values');
		var when = bdrs.mobile.getCurrentDate();
		jQuery('#pi-when').val(when);
		var time = bdrs.mobile.getCurrentTime(); 
		jQuery('#pi-time').val(time);
        jQuery('#pi-notes').val('');        
    } else {
        // Updating a record
        count = '';
        var record = exports._point_data.substrateRecord;
        
		var when = record.when() === null ? bdrs.mobile.getCurrentDate() : bdrs.mobile.formatDate(record.when());
		jQuery('#pi-when').val(when);
		bdrs.mobile.Debug('Record time is : ' + record.time());
    	var time = record.time().length === 0 ? bdrs.mobile.getCurrentTime() : record.time(); 
		jQuery('#pi-time').val(time);
	
		bdrs.mobile.Debug('Adding values for notes and number');
		jQuery('#pi-notes').val(record.notes());
    	jQuery('#pi-number').val(record.number());   
    }
    
    jQuery('#point-intersect-sibling-count').text(count);
	
    //////////////////////////////////////////////////////
    // Get the substrate record attribute
    //////////////////////////////////////////////////////
	// TODO this can be cleaned up more.
	// Map of record attributes where { attributeID: attributeValueObj }
	var attributeValueMap = {}
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
    // Taxonomy Table
    ////////////////////////
    var obsRecords;
    waitfor(obsRecords) {
        exports._point_data.substrateRecord.children().prefetch('species').list(resume);
    }
    for(var p=0; p<obsRecords.length; p++) {
        exports._insertObservationRow(obsRecords[p], obsRecords[p].species());
    }
        
    ////////////////////////
    // Taxonomy Quick List
    ////////////////////////
    
    // For the moment just grab a random sample of taxa
    var quickListTaxa;
    waitfor(quickListTaxa) {
        SpeciesCount.all().order('count', false).limit(15).prefetch('species').list(resume);
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
        bdrs.mobile.restyle('#point-intersect');
    });
    
    
    bdrs.mobile.restyle('#point-intersect');
    
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
    jQuery('#pi-latitude').val(latCoord);
    jQuery('#pi-longitude').val(lonCoord);
	jQuery('#pi-accuracy').val(accuracy);
}
	
/**
 * Invoked when the page is hidden.
 */
exports.Hide = function() {
	bdrs.mobile.Debug('Point Intersect Form Hide');
	
	jQuery("#point-intersect-substrate").empty();
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
    var isPointIntersect = false;

    // The census method must be called POINT_INTERSECT_SUBSTRATE
    if(cmethod.type() === 'POINT_INTERSECT_SUBSTRATE') {
        exports._point_data.substrateMethod = cmethod;

        // The census method must have at least one attribute of type SV with name SUBSTRATE.    
        var substrateMethodAttributes = 
            bdrs.mobile.pages.record._getCensusMethodAttribute(cmethod,'SUBSTRATE',bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES);

        if(substrateMethodAttributes.length > 0) {
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
                exports._point_data.obsMethod = obsMethod[0];
                
                // The observation method must have at least one height attribute and one flowering attribute.
                var heightAttr = 
                    bdrs.mobile.pages.record._getCensusMethodAttribute(obsMethod[0],'HEIGHT',bdrs.mobile.attribute.type.DECIMAL);
                if(heightAttr.length > 0) {
                    exports._point_data.obsHeightAttr = heightAttr[0];
                    
                    var floweringAttr = 
                        bdrs.mobile.pages.record._getCensusMethodAttribute(obsMethod[0],'FLOWERING',bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES);
                    if(floweringAttr.length > 0) {
                        exports._point_data.obsFloweringAttr = floweringAttr[0];
                        
                        // And finally if you get all the way down here, congratulations, you have a 
                        // point intersect module.
                        isPointIntersect = true;
                        exports._point_data.parentRecord = parentRecord;
                        exports._point_data.substrateRecord = substrateRecord;
                    }
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
    var flowering = '';
    if(obsRecord === null || obsRecord === undefined) {
        // Adding a new row
        // Get and increment the index
        var indexElem = jQuery("#pi-obs-index");
        var index = parseInt(indexElem.val(),10);
        indexElem.val(index+1);
        id = index;
        
        height = '';
        flowering = '';
        
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
        
        var floweringRecAttr;
        waitfor(floweringRecAttr) {
            AttributeValue.all().
                filter('record','=',obsRecord.id).
                filter('attribute','=',exports._point_data.obsFloweringAttr.id).
                order('weight', true).
                one(resume);
        };
        if(floweringRecAttr !== undefined && floweringRecAttr !== null) {
            flowering = floweringRecAttr.value();
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
		    var species;
		    waitfor(species) {
			    Species.search('*' + request.term + '*').limit(5).list(resume); // @todo add some survey awareness.
		    }
		    var names = [];
		    for (var i = 0; i < species.length; i++) {
			    names.push({ label : species[i].commonName() + ' - <i>' + species[i].scientificName()+ '</i>', value : species[i].scientificName()});
		    }
		    response(names);
	    },
	    change: function(event, ui) {
        },
	    html: true
    });
    
    // Injecting the height
    var heightTmplParams = { id: id, value: height };
    waitfor() {
        bdrs.template.renderCallback('recordPointIntersect-height', heightTmplParams, '#block-b-'+rowTmplParams.id, resume);
    }
    
    // Injecting the Flowering (Phenology)
    var optionTmplParams = [];
    
    var attrOpts;
    waitfor(attrOpts) {
        exports._point_data.obsFloweringAttr.options().list(resume);
    }
    
    var opt;
    for(var i=0; i<attrOpts.length; i++) {
        opt = attrOpts[i];
        optionTmplParams.push({
            value: opt.value(),
            text: opt.value(),
            selected: opt.value() === flowering
        });
    }

    var optionElements;		    
    waitfor (optionElements) { 
        bdrs.template.renderOnlyCallback('recordPointIntersect-flowering-option', optionTmplParams, resume); 
    }
    
    var tmpSelect = jQuery("<select></select>");
    optionElements.appendTo(tmpSelect);
    
    var floweringTmplParams = {
        id: id,
        required: true,
        options: tmpSelect.html()
    }
    waitfor() {
        bdrs.template.renderCallback('recordPointIntersect-flowering', floweringTmplParams, '#block-c-'+rowTmplParams.id, resume);
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
    
    jQuery.mobile.pageLoading(false);
	bdrs.mobile.Debug ('Save Point Called');
	
	// Get the survey
	var setting;
    waitfor(setting) {
        Settings.findBy('key', 'current-survey-id', resume);
    }
    var survey;
    waitfor(survey) {
        Survey.findBy('server_id', setting.value(), resume);
    }
    
    var lat = jQuery('#pi-latitude').val();
    var lon = jQuery('#pi-longitude').val();
	var accuracy = jQuery('#pi-accuracy').val();
    var when = bdrs.mobile.parseDate(jQuery('#pi-when').val());
    var time = jQuery('#pi-time').val();    
    var notes = jQuery('#pi-notes').val();
    
	// Save the substrate first
	var substrateRec = exports._point_data.substrateRecord;
	substrateRec.parent(exports._point_data.parentRecord);
	substrateRec.censusMethod(exports._point_data.substrateMethod);
	
	var now = new Date();
	substrateRec.survey(survey);
	substrateRec.modifiedAt(now);
	substrateRec.when(when);
	substrateRec.time(time);
	substrateRec.latitude(lat);
	substrateRec.longitude(lon);
	substrateRec.accuracy(accuracy);
	substrateRec.notes(notes);
	
	var substrateRecAttr = exports._point_data.substrateRecAttr;
	substrateRec.attributeValues().add(substrateRecAttr);
	substrateRecAttr.value(jQuery("[name=radio-choice-pi-substrate]:checked").val());
	exports._last_selected_substrate = substrateRecAttr.value();
	    
    // Save the updated observed species
    var substrateRecChildren;
    waitfor(substrateRecChildren) {
        substrateRec.children().list(resume);
    }
    for(var y=0; y<substrateRecChildren.length; y++) {
        var obsRec = substrateRecChildren[y];
        var speciesElem = jQuery("#pi-taxonomy-species-"+obsRec.id);
        var heightElem = jQuery("#pi-taxonomy-height-"+obsRec.id);
        var floweringElem = jQuery("#pi-taxonomy-flowering-"+obsRec.id);
        
        if(speciesElem.length > 0 && heightElem.length > 0 && floweringElem.length > 0) {
            obsRec.modifiedAt(now);
	        obsRec.when(now);
	        obsRec.time([now.getHours(), now.getMinutes(), now.getSeconds()].join(':'));
	        obsRec.latitude(lat);
	        obsRec.longitude(lon);
	        obsRec.accuracy(accuracy);
	        obsRec.species(exports._getSpeciesByScientificName(speciesElem.val()));
	        
	        var heightRecAttr;
            waitfor(heightRecAttr) {
                AttributeValue.all().
                    filter('record','=',obsRec.id).
                    filter('attribute','=',exports._point_data.obsHeightAttr.id).
                    order('weight', true).
                    one(resume);
            };
            if(heightRecAttr === undefined || heightRecAttr === null) {
                heightRecAttr = new AttributeValue();
	            obsRec.attributeValues().add(heightRecAttr);
                heightRecAttr.attribute(exports._point_data.obsHeightAttr);
            }
            heightRecAttr.value(heightElem.val());
            
            var floweringRecAttr;
            waitfor(floweringRecAttr) {
                AttributeValue.all().
                    filter('record','=',obsRec.id).
                    filter('attribute','=',exports._point_data.obsFloweringAttr.id).
                    order('weight', true).
                    one(resume);
            };
            if(floweringRecAttr === undefined && floweringRecAttr === null) {
                floweringRecAttr = new AttributeValue();
	            obsRec.attributeValues().add(floweringRecAttr);
	            floweringRecAttr.attribute(exports._point_data.obsFloweringAttr);
            }
            floweringRecAttr.value(floweringElem.val());
        }
    }
	
	// Save the new observed species
    var index = parseInt(jQuery("#pi-obs-index").val(),10);
    for(var i=0; i<index; i++) {
        var speciesElem = jQuery("#pi-taxonomy-species-"+i);
        var heightElem = jQuery("#pi-taxonomy-height-"+i);
        var floweringElem = jQuery("#pi-taxonomy-flowering-"+i);
        
        if(speciesElem.length > 0 && heightElem.length > 0 && floweringElem.length > 0) {
            // Observation Record
            var obsRec = new Record();
            obsRec.parent(substrateRec);
	        obsRec.censusMethod(exports._point_data.obsMethod);
            obsRec.survey(survey);
	        obsRec.modifiedAt(now);
	        obsRec.when(now);
	        obsRec.time([now.getHours(), now.getMinutes(), now.getSeconds()].join(':'));
	        obsRec.latitude(lat);
	        obsRec.longitude(lon);
	        obsRec.accuracy(accuracy);
			// Field Species check.
			var species = exports._getSpeciesByScientificName(speciesElem.val())         
			if (species != undefined) {
				bdrs.mobile.Debug(species.commonName());
				bdrs.mobile.Debug(species.scientificName());
			} else if (speciesElem.val() != '') {
				// Create a field species.
				var sp = new Species({
					scientificNameAndAuthor: "Field Species",
			    	scientificName: speciesElem.val(),
			    	commonName: speciesElem.val(),
			    	rank: "Field Species",
			    	author: "Field Species",
			    	year: ""
				});
				
				// Check for field taxon group
				var tg;
				waitfor(tg) {
					TaxonGroup.all().filter('name', '=', 'Field Species').one(resume);
				}
				if (tg == null) {
					tg = new TaxonGroup({
						name: "Field Species"
					});
					persistence.add(tg);
				}
				persistence.add(sp);
				tg.species().add(sp);
				species = sp;
				waitfor() {
					persistence.flush(resume);
				}
				bdrs.mobile.Debug('Created a new species: ' + species.scientificName());
				bdrs.mobile.Debug(species.scientificName());
			}
	        obsRec.species(species);
	        
	        // Height Record Attribute
	        var heightRecAttr = new AttributeValue();
	        obsRec.attributeValues().add(heightRecAttr);
            heightRecAttr.attribute(exports._point_data.obsHeightAttr);
	        heightRecAttr.value(heightElem.val());
	        
	        // Flowering Record Attribute
	        var floweringRecAttr = new AttributeValue();
	        obsRec.attributeValues().add(floweringRecAttr);
	        floweringRecAttr.attribute(exports._point_data.obsFloweringAttr);
	        floweringRecAttr.value(floweringElem.val());
	        
	        // Update Species Count index.
			bdrs.mobile.Debug('Updating species count');
        	var counter;
        	waitfor(counter) {
				SpeciesCount.all().filter('scientificName', '=', obsRec.species().scientificName()).one(resume);
        	}
        	if (counter == null) {
				bdrs.mobile.Debug('Count not found, creating new one.');
				counter = new SpeciesCount({scientificName: obsRec.species().scientificName(), 
					count: 1 });
				persistence.add(counter);
				counter.species(obsRec.species());
        	} else {
        		bdrs.mobile.Debug('Count found, incrementing');
        		counter.count(counter.count() + 1);
        	}
        }
    }

	persistence.flush();
	jQuery.mobile.pageLoading(true);
};

exports._getSpeciesByScientificName = function(scientificName) {
	var species;
	waitfor(species) {
		Species.all().filter('scientificName', '=', scientificName).prefetch('taxonGroup').one(resume);
	}
	return species;
};


