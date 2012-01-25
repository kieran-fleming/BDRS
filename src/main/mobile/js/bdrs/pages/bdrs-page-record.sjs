exports.NONTAXONOMIC = 'NONTAXONOMIC';
exports.TAXONOMIC = 'TAXONOMIC';
exports.OPTIONALLYTAXONOMIC = 'OPTIONALLYTAXONOMIC';

/**
 * Invoked when the page is created.
 */
exports.Init =  function() {
	bdrs.mobile.Debug("bdrs-page-record Init");
    jQuery('.record-save').click(function (event) {
        if(bdrs.mobile.validation.isValidForm('#record-form')) {
            bdrs.mobile.pages.record._record();
            jQuery.mobile.changePage("#review", {showLoadMsg: false});
        }
    });
    jQuery('.record-save-continue').click(function (event) {
        if(bdrs.mobile.validation.isValidForm('#record-form')) {
            // Save the record.
            var record = bdrs.mobile.pages.record._record();
            bdrs.mobile.removeParameter("selected-record");
            
            // If we have a census method, then we continue back to the
            // parent census method.
            if (bdrs.mobile.getParameter('censusMethodId') !== undefined) {
                var cmethod;
                waitfor(cmethod) {
                    CensusMethod.load(bdrs.mobile.getParameter('censusMethodId'), resume);
                }
                bdrs.mobile.Debug('setting up parent census method');
                if(cmethod.parent() !== null ) {
                    bdrs.mobile.setParameter('censusMethodId', cmethod.parent().id);
                }
            }
            
            // Set the parent record as the selected record.
            if (bdrs.mobile.getParameter('record-parent') !== undefined) {
                bdrs.mobile.setParameter('selected-record', bdrs.mobile.getParameter('record-parent'));
                bdrs.mobile.removeParameter('record-parent');
            } else if(record.parent() !== null) {
                bdrs.mobile.setParameter('selected-record', record.parent().id);
            }
        }
    });
    jQuery('.record-delete').click(function(event) {

        var selectedRecordId = bdrs.mobile.getParameter('selected-record');
        if (selectedRecordId !== undefined) {
            var record;
            waitfor(record) {
                Record.all().filter('id', '=', selectedRecordId).prefetch('species').one(resume);
            }

            bdrs.mobile.pages.record.markRecordForDelete(record);
            jQuery.mobile.changePage("#review", {showLoadMsg: false});
        }
    });
};

exports.BeforeShow = function() {
	bdrs.mobile.Debug("bdrs-page-record BEFORESHOW");
};

/**
 * Invoked when the page is displayed.
 */
exports.Show = function() {
	bdrs.mobile.Debug("bdrs-page-record SHOW");
	jQuery.mobile.showPageLoadingMsg();
	jQuery('.bdrs-page-record').hide();
    var currentSurvey = bdrs.mobile.survey.getDefault();
    // If editing load the record from the database, otherwise create a blank 
    // record.
    var record;
    var parent = null;
    // Map of record attributes where { attributeID: attributeValueObj }
    var attributeValueMap = {}
    var selectedRecordId = bdrs.mobile.getParameter('selected-record');
    var isNewRecord = false;

    if (selectedRecordId === undefined) {
    	//bdrs.mobile.Debug("create new empty record");
        record = new Record();
        record.deleted(false);
        isNewRecord = true;
        if (bdrs.mobile.getParameter('record-parent') !== undefined) {
            waitfor(parent) {
                Record.all().filter('deleted','=',false).filter('id','=',bdrs.mobile.getParameter('record-parent')).one(resume);
            }
        }
    } else {
    	bdrs.mobile.Debug("get record and get all the attribute values");
/*    	currentSurvey.records().filter('deleted','=',false).prefetch('species').prefetch('censusMethod').filter('parent', '=', null).order('when', false).list(null,function(recordList){
    		var prevRec;
    		var nextRec;
    		for(var i=0; i<recordList.length; i++) {
    			var curRec = recordList[i]; 
    			if(curRec.id === selectedRecordId) {
    				nextRec = recordList[i + 1];
    				break;
    			}
    			prevRec = curRec;
    		}
    		var prevRec_id;
    		if (prevRec !== undefined) {
    			prevRec_id = prevRec.id;
    		} else {
    			prevRec_id = '-1';
    		}
    		var nextRec_id;
    		if (nextRec !== undefined) {
    			nextRec_id = nextRec.id;
    		} else {
    			nextRec_id = '-1';
    		}
    		jQuery('.footer').empty();
    		bdrs.template.render('recordNav',{'prev': prevRec_id,'next': nextRec_id}, '.footer');
    	});*/
    	
        waitfor(record) {
            Record.all().filter('id', '=', selectedRecordId).filter('deleted','=',false).prefetch('censusMethod').prefetch('parent').one(resume);
        }
        parent = record.parent();
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
    
    //////////////////////////
    // Census Method Record
    //////////////////////////
    var isCensusMethodRecord = ((bdrs.mobile.getParameter('censusMethodId') !== undefined) || (record.censusMethod() !== null));
    
    bdrs.mobile.Debug('isCensusMethodRecord : ' + isCensusMethodRecord);
    var cmethod = null;
    
    var isTaxonomic;
    var isOptionallyTaxonomic;
    var isPointIntersect;
    var pointIntersectStruct = null;
    
    if (isCensusMethodRecord) {
        // we are doing a census type record
        if (record.censusMethod() === null) {
            waitfor(cmethod) {
               CensusMethod.load(bdrs.mobile.getParameter('censusMethodId'), resume);
            }
        } else {
            cmethod = record.censusMethod();
        }
        bdrs.mobile.Debug('Loaded Census Method : ' + bdrs.mobile.getParameter('censusMethodId'));
        bdrs.mobile.pages.record._insertCensusMethodAttributes(record, cmethod, attributeValueMap);
        
        isTaxonomic = cmethod.taxonomic() === bdrs.mobile.pages.record.TAXONOMIC;
        isOptionallyTaxonomic = cmethod.taxonomic() === bdrs.mobile.pages.record.OPTIONALLYTAXONOMIC;
        
    } else {
        // If there are no census method records, hide the collapsible.
        var methodAttrsWrapperElem = jQuery('#record-method-attributes');
        var methodAttrsCollapsible = methodAttrsWrapperElem.parents('[data-role=collapsible]');
        methodAttrsCollapsible.hide();
        methodAttrsWrapperElem.empty();
        
        bdrs.mobile.Debug("No census method means that the record will set to be optionally taxonomic.");
        isTaxonomic = false;
        isOptionallyTaxonomic = true;
    }
    
    // Launch the tracker
    jQuery("#record-tracker").show();
    jQuery("#record-point-intersect").hide();
    
    var recordTypeElem = jQuery("#record-type");
    if(isCensusMethodRecord) {
       recordTypeElem.text(cmethod.name());
    } else {
       recordTypeElem.text("Taxonomic");
    }

    //////////////////////////
    // Sibling Count
    //////////////////////////
    var siblingCountElem = jQuery("#record-sibling-count");
    siblingCountElem.text("");
    if(parent !== null && selectedRecordId === undefined) {
        var siblingCount;
        if(cmethod === null) {
            waitfor(siblingCount) {
                parent.children().filter('deleted','=',false).count(resume);
            }
        } else {
            waitfor(siblingCount) {
                parent.children().filter('deleted','=',false).filter('censusMethod','=', cmethod.id).count(resume);                
            }
        }
        
        // If we are adding a new record, we need to increment by one (the index of this record).
        siblingCount += 1;
        siblingCountElem.text(siblingCount);
    }

    if(isTaxonomic || isOptionallyTaxonomic) {
        // Is Taxonomy Required
        var templateParams = {
            required : isTaxonomic
        };
    }

    //////////////////////////
    // Sub Census Methods
    //////////////////////////
    bdrs.mobile.pages.record._insertSubCensusMethods(record, cmethod);

    ////////////////////////////////////
    // Survey & Taxon Group Attributes
    ////////////////////////////////////
    if (currentSurvey !== null) {
        jQuery.mobile.showPageLoadingMsg();
        bdrs.mobile.pages.record._insertSurveyAttributes(record, currentSurvey, attributeValueMap);
        bdrs.mobile.pages.record._insertTaxonGroupAttributes(record.species(), attributeValueMap);
        jQuery.mobile.hidePageLoadingMsg();
    } else {
        bdrs.mobile.Error('Current Survey ID not known');
        return;
    }
    

    // If editing, populate the form with data from the database.
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
    
    jQuery('.bdrs-page-record').fadeIn('fast', function(){jQuery.mobile.hidePageLoadingMsg();});
    bdrs.template.restyle('#record');
};
    
/**
 * Invoked when the page is hidden.
 */
exports.Hide = function() {
    bdrs.mobile.Debug('Record Form Hide');
    
    bdrs.mobile.removeParameter("censusMethodId");
    bdrs.mobile.removeParameter("selected-record");
    bdrs.mobile.removeParameter('record-parent');
    
    jQuery('#record-survey-attributes').empty();
    jQuery('#record-method-attributes').empty();
    jQuery('#record-taxongroup-attributes').empty();
    jQuery('#record-sub-methods').empty();
    
    jQuery('#record-form :input').val(null);
    
    jQuery('.recent-taxa-widget').remove();
    jQuery("#record-type").text('');
    jQuery.mobile.loadingMessage = "Loading ...";
}

/**
 * Insert census method attributes into the record form.
 * 
 * @param record The object that backs the current form.
 * @param cmethod The census method containing the attributes to be added to the form.
 * @param attributeValueMap a map of record attributes keyed against the 
 * attribute ID. That is, { attributeID: attributeValueObj }.
 */
exports._insertCensusMethodAttributes = function(record, cmethod, attributeValueMap) {
    var attributes;
    waitfor(attributes) {
        cmethod.attributes().order('weight', true).list(resume);
    }
    
    var methodAttrsWrapperElem = jQuery('#record-method-attributes');
    var methodAttrsCollapsible = methodAttrsWrapperElem.parents('[data-role=collapsible]');
    if(attributes.length === 0) {
        methodAttrsCollapsible.hide();
    }
    methodAttrsWrapperElem.empty();
    
    var recAttrFormField;
    for (var i = 0; i < attributes.length; i++) {
        recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attributes[i]);
        recAttrFormField.toFormField('#record-method-attributes', attributeValueMap[attributes[i].id]);
    }
    bdrs.mobile.Debug('Triggering CM Expand');
    if(attributes.length > 0) {
        bdrs.mobile.Debug('Triggering CM Expand 2');
        methodAttrsCollapsible.show().trigger('expand');
    }
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
    // Clear old attributes
    var surveyAttrsWrapperElem = jQuery('#record-survey-attributes');
    surveyAttrsWrapperElem.empty();
    // Insert record attributes
    var recAttrFormField;
    var recPropertyAttrFormField;
    for (var i = 0; i < attributes.length; i++) {
    	var attribute = attributes[i]; 
        recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attribute);
        recAttrFormField.toFormField('#record-survey-attributes', attributeValueMap[attribute.id], record);
    }
};

/**
 * Inserts record listing and record adding buttons for sub-census-methods.
 *
 * @param record The object that backs the current form.
 * @param cmethod contains the sub-census-methods to be rendered.
 */
exports._insertSubCensusMethods = function(record, cmethod) {
    var subCensusCollapsibleSet = jQuery("#record-sub-methods");
    subCensusCollapsibleSet.empty();
    if(cmethod === null) {
        subCensusCollapsibleSet.hide();
    } else {
        
        var subMethods;
        waitfor(subMethods) {
           cmethod.children().list(resume);
        }
        
        var collapsibleElem;
        var buttonElem;
        var childRecords;
        var tmplParams;
        for(var i=0; i<subMethods.length; i++) {
            ///////////////////////
            // Sub Census Methods
            ///////////////////////
            tmplParams = {
                title : subMethods[i].name(),
                description: subMethods[i].description(),
                collapsed : true                
            };
            waitfor(collapsibleElem) {
               bdrs.template.renderOnlyCallback('collapsibleBlock', tmplParams, resume);
            }
            collapsibleElem.appendTo(subCensusCollapsibleSet);
            
            //////////////////////
            // Child Record List
            //////////////////////
            waitfor(childRecords) {
                record.children().filter('deleted','=',false).prefetch('species').filter('censusMethod', '=', subMethods[i]).list(resume);
            }

            if(childRecords.length > 0) {
            
                // Create list.
                var listView;
                var listViewTmplParams = {
                    dataInset:true
                };
                waitfor(listView) {
                    bdrs.template.renderOnlyCallback('listView', listViewTmplParams, resume);
                };
                
                // Append to content
                listView.appendTo(collapsibleElem);
            
                // Create list items
                var listViewItem;
                var listViewItemTmplParams;
                var childRecordDescriptor;
                var childCount;
                var aside;
                var isModified;
                var now = new Date();
                for(var p=0; p<childRecords.length; p++) {
                    childRecordDescriptor = bdrs.mobile.record.util.getDescriptor(childRecords[p]);
                    waitfor(childCount) {
                        childRecords[p].children().filter('deleted','=',false).count(resume);
                    };
                    
                    isModified = (childRecords[p].uploadedAt() === null) || 
                        (childRecords[p].modifiedAt().getTime() > childRecords[p].uploadedAt().getTime());
                    
                    aside = [
                        isModified ? "Modified" : "Synched",
                        "Last Changed: "+bdrs.mobile.getDaysBetweenAsFormattedString(childRecords[p].modifiedAt(), now)
                    ].join("&nbsp;|&nbsp;");

                    listViewItemTmplParams = {
                        linkId: "record-"+childRecords[p].id,
                        link: "#record",
                        header: childRecordDescriptor.title,
                        description: childRecordDescriptor.description,
                        count: childCount,
                        aside: aside
                    };
                    waitfor(listViewItem) {
                        bdrs.template.renderOnlyCallback('listViewItemWithCountAside', listViewItemTmplParams, resume);
                    };
                    listViewItem.appendTo(listView);
                    
                    // Add click handler to the list handler.
                    listViewItem.find("a").jqmData("recordId", childRecords[p].id);
                    listView.find("#"+listViewItemTmplParams.linkId).click(function(event) {
                        var record;
                        waitfor(record) {
                            Record.all().filter('id','=',jQuery(event.currentTarget).jqmData("recordId")).filter('deleted','=',false).one(resume);
                        };
                        
                        bdrs.mobile.setParameter("selected-record", record.id);
                        bdrs.mobile.setParameter('record-parent', record.parent().id);
                        bdrs.mobile.setParameter('censusMethodId', record.censusMethod().id);
                        
                        var censusMethod;
                        waitfor(censusMethod) {
                            CensusMethod.all().filter('id','=',record.censusMethod().id).one(resume);
                        }
                        
                        var isPointIntersect  = bdrs.mobile.pages.point_intersect.isPointIntersect(censusMethod, record.parent(), record);
                        
                        if(isPointIntersect) {
                            jQuery.mobile.changePage("#point-intersect", {showLoadMsg: false});
                        } else {
                            exports.Show();
                        }
                    });
                }
            }
            
            ////////////////////////////
            // Add Child Record Button
            ///////////////////////////
            tmplParams = {
                id: 'add-submethod-button-' + subMethods[i].id, 
                text: 'Add ' + subMethods[i].name()
            };
            waitfor(buttonElem) {
                bdrs.template.renderOnlyCallback('inlineButton', tmplParams, resume);
            }
            buttonElem.appendTo(collapsibleElem.parent());
            
            buttonElem.jqmData('censusMethodId', subMethods[i].id);

            buttonElem.click(function(event) {
                if(bdrs.mobile.validation.isValidForm('#record-form')) {
                    // Save the record first.
                    var record = bdrs.mobile.pages.record._record();
                    
                    bdrs.mobile.setParameter("record-parent", record.id);
                    bdrs.mobile.removeParameter("selected-record");
                    jQuery('.recent-taxa-widget').remove();
                
                    // Set the desired census method.
                    var censusMethodId = jQuery(event.currentTarget).jqmData('censusMethodId');
                    bdrs.mobile.setParameter('censusMethodId', censusMethodId);
                    
                    var censusMethod;
                    waitfor(censusMethod) {
                        CensusMethod.all().filter('id','=',censusMethodId).one(resume);
                    }
                    var childRecord = new Record();
                    childRecord.deleted(false);
                    var isPointIntersect  = bdrs.mobile.pages.point_intersect.isPointIntersect(censusMethod, record, childRecord);
                    if(isPointIntersect) {
                        jQuery.mobile.changePage("#point-intersect", {showLoadMsg: false});
                    } else {
                        exports.Show();
                    }
                }
            });
        }
        
         bdrs.template.restyle(subCensusCollapsibleSet);
        subCensusCollapsibleSet.show();
    }
};

/**
 * Insert taxon group attributes to the form.
 * 
 * @param taxon The taxon for which we are getting the taxongroup attributes
 * @param attributeValueMap a map of record attributes keyed against the 
 * attribute ID. That is, { attributeID: attributeValueObj }.
 */
exports._insertTaxonGroupAttributes = function(taxon, attributeValueMap) {
    var taxonGroupAttrs = jQuery("#record-taxongroup-attributes"); 
    var taxonGroupAttrsCollapsible = taxonGroupAttrs.parents('[data-role=collapsible]');
    taxonGroupAttrsCollapsible.hide();
    taxonGroupAttrs.empty();
                    
    if(taxon !== null) {
        var taxonGroup;
        waitfor(taxonGroup) {
            taxon.fetch('taxonGroup', resume);
        }
        
        var attributes;
        waitfor(attributes) {
            taxonGroup.attributes().order('weight', true).filter('tag', '=', false).list(resume);
        }

        var recAttrFormField;
        for (var i = 0; i < attributes.length; i++) {
            recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attributes[i]);
            recAttrFormField.toFormField('#record-taxongroup-attributes', attributeValueMap[attributes[i].id]);
        }
        if(attributes.length > 0) {
            taxonGroupAttrsCollapsible.show();
        	bdrs.template.restyle('#record');
        }
    }
};

/**
 * Recursively checks the specified record and all children for an 
 * attached species. If found, the counter in the species map is 
 * incremented.
 *
 * @param record the record object to be checked for the presense of an
 * associated species. 
 * @param a mapping of the species primary key and a count of records
 * that refer to that species.
 * @return the species_map that has been updated by this function.
 */
exports._recurseCountRelatedSpecies = function(record, species_map) {
    var species = record.species();
    if(species !== undefined && species !== null) {
        if(species_map[species.id] === undefined) {
            species_map[species.id] = 1;
        } else {
            species_map[species.id] = species_map[species.id] + 1;
        }
    }

    var children;
    waitfor(children) {
        // Only check for un-deleted children because deleted children will
        // have already processed their species counts.
        record.children().filter('deleted','=',false).prefetch('species').list(resume);
    }
    for(var i=0; i<children.length; i++) {
        species_map = exports._recurseCountRelatedSpecies(children[i], species_map);
    }

    return species_map;
};

/**
 * Sets the deleted flag on the specified record and decrements
 * the SpeciesCount for any species associated with this record or any
 * child records.
 *
 * @param record the Record object to be marked for deletion.
 */
exports.markRecordForDelete = function(record) {
    
    record.deleted(true);
    bdrs.mobile.Debug("Record "+record.id+" marked for deletion.")

    // { species.id: number of times the species has been related to a soon to be deleted record }
    var species_map = {};
    bdrs.mobile.Debug("Updating Species Count");
    species_map = exports._recurseCountRelatedSpecies(record, species_map);

    var species_ids = [];
    for(var species_id in species_map) {
        if(species_map.hasOwnProperty(species_id)) {
            species_ids.push(species_id);
        }
    }

    var speciesCountCollection;
    waitfor(speciesCountCollection) {
        SpeciesCount.all().filter('species', 'in', species_ids).prefetch('species').list(resume);
    }

    for(var c=0; c<speciesCountCollection.length; c++) {
        var speciesCount = speciesCountCollection[c];
        var decrement = species_map[speciesCount.species().id];
        speciesCount.count(speciesCount.count() - decrement);
        speciesCount.userCount(speciesCount.userCount() - decrement);

        bdrs.mobile.Debug("Decrementing Species Count: "+speciesCount.id+" : "+
            speciesCount.species().scientificName()+" by "+decrement);
    }

    persistence.flush();
};

/**
 * Private function, does the actual recording or updating.
 */
exports._record = function() {
    // Need to blur to force the species autocomplete to insert any 
    // required dom elements. 
    jQuery(":focus").blur();
    
    jQuery.mobile.showPageLoadingMsg();
    bdrs.mobile.Debug ('Record called');

    var record;
    var attributeValueMap = {};
    var species_startedit;
    var survey = bdrs.mobile.survey.getDefault();
    var selectedRecordId = bdrs.mobile.getParameter('selected-record');
    bdrs.mobile.Debug(selectedRecordId);
    if (selectedRecordId === undefined) {
    	bdrs.mobile.Debug("Dealing with a new record");
        record = new Record();
        record.deleted(false);
        persistence.add(record);
    } else {
    	bdrs.mobile.Debug("Dealing with an existing record");
        waitfor(record) {
            Record.all().filter('id', '=', selectedRecordId).filter('deleted','=',false).one(resume);
        }
        species_startedit = record.species();
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

    // Get the Census Method and determine if the record is
    // taxonomic, non taxonomic or optionally taxonomic
    var isTaxonomic;
    var isOptionallyTaxonomic;
    var cmethod = record.censusMethod();

    if(cmethod === null || cmethod === undefined) {
        if (bdrs.mobile.getParameter('censusMethodId') !== undefined) {
            waitfor(cmethod) {
                CensusMethod.load(bdrs.mobile.getParameter('censusMethodId'), resume);
            }
        }
    }

    if(cmethod !== null && cmethod !== undefined) {
        isTaxonomic = cmethod.taxonomic() === bdrs.mobile.pages.record.TAXONOMIC;
        isOptionallyTaxonomic = cmethod.taxonomic() === bdrs.mobile.pages.record.OPTIONALLYTAXONOMIC;
    } else {
        // If there is no census method, the record is
        // optionally taxonomic by default
        isTaxonomic = false;
        isOptionallyTaxonomic = true;
    }
    
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
 

    //get values for dwc fields if they exist in the survey
    if(DWC_attributesMap['Species'] !== undefined) {
    	var species = null;
    	var speciesElem = jQuery('#record-attr-' + DWC_attributesMap['Species'].id);
    	var scientificName = speciesElem.length === 0 ? '' : speciesElem.val();
    	bdrs.mobile.Debug("Current value of species element = " + scientificName + " (length=" + scientificName.length + ")");
    	    if(isTaxonomic || (isOptionallyTaxonomic && scientificName.length > 0)) {
    	    	bdrs.mobile.Debug("isTaxonomic || (isOptionallyTaxonomic && scientificName.length > 0)");
    	        waitfor(species) {
    	            Species.all().filter('scientificName', '=', scientificName).prefetch('taxonGroup').one(resume);
    	        }
    	        if (species !== undefined && species !== null) {
    	            bdrs.mobile.Debug(species.commonName());
    	            bdrs.mobile.Debug(species.scientificName());
    	            species.records().add(record);
    	        } else if(scientificName !== '') {
    	            // Create a field species.
    	            var sp = new Species({
    	                scientificNameAndAuthor: "Field Species",
    	                scientificName: scientificName,
    	                commonName: scientificName,
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
    	            survey.species().add(sp);
    	            species = sp;
    	            waitfor() {
    	                persistence.flush(resume);
    	            }
    	            species.records().add(record);
    	        }

    	        // Update Species Count index.
    	        bdrs.mobile.Debug('Updating species count');
    	      //decrementing old species counter if we started of with a species
    	        if(species_startedit !== null && species_startedit !== undefined && species !== null) {
    	        	 var counter;
    	        	 waitfor(counter) {
    	        	     SpeciesCount.all().filter('scientificName','=', species_startedit.scientificName()).and(new persistence.PropertyFilter('survey','=',survey.id)).one(resume);
    	        	 }
	                 if (counter === null) {
	                     counter = new SpeciesCount({scientificName: species_startedit.scientificName(), count: 0 , userCount: 0});
	                     persistence.add(counter);
	                     counter.species(species_startedit);
	                     counter.survey(survey);
	                 } else {
	                	 counter.count(counter.count() - 1);
	    	        	 counter.userCount(counter.userCount() - 1);
	                 }
    	        }

    	        //if a new species exists, increment the counter or create a counter if there is none
    	        if(species !== null && species !== undefined){
    	        	 var counter;
    	                 waitfor(counter) {
    	                     SpeciesCount.all().filter('scientificName','=', species.scientificName()).and(new persistence.PropertyFilter('survey','=',survey.id)).one(resume);
    	                 }
    	        	 if (counter === null) {
    	                     counter = new SpeciesCount({scientificName: species.scientificName(), count: 1 , userCount: 1});
    	                     persistence.add(counter);
    	                     counter.species(species);
    	                     counter.survey(survey);
    	                 } else {
    	                     counter.count(counter.count() + 1);
    	                     counter.userCount(counter.userCount() + 1);
    	                 }
    	        }
    	    }
    	    
    	    if(species_startedit !== null && species_startedit !== undefined && species === null) {
    	    	species_startedit.records().remove(record);
    	    	 var counter;
	        	 waitfor(counter) {
	        	     SpeciesCount.all().filter('scientificName','=', species_startedit.scientificName()).and(new persistence.PropertyFilter('survey','=',survey.id)).one(resume);
	        	 }
                 if (counter !== null) {
                	 counter.count(counter.count() - 1);
    	        	 counter.userCount(counter.userCount() - 1);
                 } 
	        }
    }
    
    // Dwc values that exist in the form will be stored in the record
    if(DWC_attributesMap['Notes'] !== undefined) {
    	var notesElem = jQuery('#record-attr-' + DWC_attributesMap['Notes'].id);
    	if (notesElem !== null) {
        	record.notes(jQuery(notesElem).val());
    	}
    }
    if(DWC_attributesMap['Number'] !== undefined) {
    	var numberElem = jQuery('#record-attr-' + DWC_attributesMap['Number'].id);
    	if (numberElem !== null) {
        	record.number(jQuery(numberElem).val());
    	}
    }
    if(DWC_attributesMap['Point'] !== undefined) {
    	var latElem = jQuery('#record-attr-' + DWC_attributesMap['Point'].id + '-lat');
    	var lonElem = jQuery('#record-attr-' + DWC_attributesMap['Point'].id + '-lon');
    	if (latElem !== null) {
        	record.latitude(jQuery(latElem).val());
    	}
    	if (lonElem !== null) {
        	record.longitude(jQuery(lonElem).val());
    	}
    }
    if(DWC_attributesMap['When'] !== undefined) {
    	var whenElem = jQuery('#record-attr-' + DWC_attributesMap['When'].id);
    	if (whenElem !== null) {
    		var when = bdrs.mobile.parseDate(jQuery(whenElem).val());
    	    record.when(when);
    	}
    }
    if(DWC_attributesMap['Time'] !== undefined) {
    	var timeElem = jQuery('#record-attr-' + DWC_attributesMap['Time'].id);
    	if (timeElem !== null) {
    	    var time = jQuery(timeElem).val(); 
    	    record.time(time);
    	}
    }
    if(DWC_attributesMap['AccuracyInMeters'] !== undefined) {
    	var accuracyElem = jQuery('#record-attr-' + DWC_attributesMap['AccuracyInMeters'].id);
    	if (accuracyElem !== null) {
    	    var accuracy = jQuery(accuracyElem).val(); 
    	    record.accuracy(accuracy);
    	}
    }
    
    record.modifiedAt(new Date());
    
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
	    			location.records().add(record)
	    		}
    	    }
    	}
    }
    
    if (bdrs.mobile.getParameter('record-parent') !== undefined) {
        var parent;
        waitfor(parent) {
            Record.all().filter('deleted','=',false).filter('id','=',bdrs.mobile.getParameter('record-parent')).one(resume);
        }
        parent.children().add(record);
    }
     
    // Survey Attributes
    var attributes;
    waitfor(attributes) {
        survey.attributes().order('weight', true).list(resume);
    }

    var recAttrFormField;
    for (var i = 0; i < attributes.length; i++) {
        recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attributes[i]);
        recAttrFormField.fromFormField('#record-survey-attributes', record.attributeValues(), attributeValueMap[attributes[i].id]);   
    }
    
    // Taxon Group Attributes
    if(species !== null && species !== undefined) {
        var taxonGroup = species.taxonGroup();
        if(taxonGroup !== null) {
            var groupAttributes;
            waitfor(groupAttributes) {
                taxonGroup.attributes().order('weight', true).filter('tag', '=', false).list(resume);
            }
            for (var i = 0; i < groupAttributes.length; i++) {
                recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(groupAttributes[i]);
                recAttrFormField.fromFormField('#record-taxongroup-attributes', record.attributeValues(), attributeValueMap[groupAttributes[i].id]);
            }     
        }
    }

    //CensusMethod Attributes
    if (cmethod !== null && cmethod !== undefined) {
        var cmAttributes;
        waitfor(cmAttributes) {
            cmethod.attributes().order('weight', true).list(resume);
        }
        
        var recAttrFormField;
        for (var i = 0; i < cmAttributes.length; i++) {
            recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(cmAttributes[i]);
            recAttrFormField.fromFormField('#record-method-attributes', record.attributeValues(), attributeValueMap[cmAttributes[i].id]);
        }
        cmethod.records().add(record);
    }
    survey.records().add(record);
    
    persistence.flush();
    
    jQuery.mobile.hidePageLoadingMsg();
    
    return record;
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

exports._goToRecord = function(recordId, direction) {
	bdrs.mobile.Debug("Going to record = " + recordId + ", " + direction);
	if (recordId !== '-1') {
		bdrs.mobile.setParameter("selected-record", recordId);
		//apply transition depending on direction
//		jQuery('#record').fadeOut();
		jQuery.mobile.changePage("#record", {allowSamePageTransition: true});
//		jQuery('#record').fadeIn();
	}	
};

/**
 * Removes values from fields.
 * @param elements an array of elements from which the values need to be removed.
 */
exports._removeFieldValues = function(elements) {
	for(var i=0; i<elements.length; i++) {
		var el = elements[i];
		var type = el.type;
	    var tag = el.tagName.toLowerCase(); // normalize case
	    // it's ok to reset the value attr of text inputs,
	    // password inputs, and textareas
	    if (type == 'text' || type == 'password' || tag == 'textarea')
	      jQuery(el).val("");
	    // checkboxes and radios need to have their checked state cleared
	    // but should *not* have their 'value' changed
	    else if (type == 'checkbox' || type == 'radio')
	    	jQuery(el).prop('checked', false);
	    // select elements need to have their 'selectedIndex' property set to -1
	    // (this works for both single and multiple select elements)
	    else if (tag == 'select')
	    	jQuery(el).prop('selectedIndex', -1);
	}
};