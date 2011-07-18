exports.NONTAXONOMIC = 'NONTAXONOMIC';
exports.TAXONOMIC = 'TAXONOMIC';
exports.OPTIONALLYTAXONOMIC = 'OPTIONALLYTAXONOMIC';

/**
 * Invoked when the page is created.
 */
exports.Create =  function() {
	jQuery('#record-save').click(function (event) {
	    if(bdrs.mobile.validation.isValidForm('#record-form')) {
	        bdrs.mobile.pages.record._record();
            jQuery.mobile.changePage("#review", "slide", false, true);
	    }
	});
	jQuery('#record-save-continue').click(function (event) {
	    if(bdrs.mobile.validation.isValidForm('#record-form')) {
	        // Save the record.
		    var record = bdrs.mobile.pages.record._record();
		    bdrs.mobile.removeParameter("selected-record");
			jQuery('.recent-taxa-widget').remove();
			
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
			
		    exports.Show();
	    }
	});
	jQuery('#record-gps').click(function (event) {
		var position;
		waitfor(position) {
			bdrs.mobile.geolocation.getCurrentPosition(resume);
		}
		if (position !== undefined) {
			jQuery('#record-latitude').val(bdrs.mobile.roundnumber(position.coords.latitude, 5));
        	jQuery('#record-longitude').val(bdrs.mobile.roundnumber(position.coords.longitude, 5));
			jQuery('#record-accuracy').val(bdrs.mobile.roundnumber(position.coords.accuracy, 5));
		} else {
			bdrs.mobile.Debug('Could not get GPS loc');
		}
	});
	

	jQuery('#record-when').datepicker();
	jQuery('#record-time').timepicker();
}

/**
 * Invoked when the page is displayed.
 */
exports.Show = function() {
	var setting;
	waitfor(setting) {
		Settings.findBy('key', 'current-survey-id' , resume);
	}
	
	// If editing load the record from the database, otherwise create a blank 
	// record.
	var record;
	var parent = null;
	// Map of record attributes where { attributeID: attributeValueObj }
	var attributeValueMap = {}
	var selectedRecordId = bdrs.mobile.getParameter('selected-record');
	var isNewRecord = false;
	if (selectedRecordId === undefined) {
	    record = new Record();
	    isNewRecord = true;
	    if (bdrs.mobile.getParameter('record-parent') !== undefined) {
            waitfor(parent) {
                Record.load(bdrs.mobile.getParameter('record-parent'), resume);
            }
        }
	} else {
        waitfor(record) {
            Record.all().filter('id', '=', selectedRecordId).prefetch('censusMethod').prefetch('parent').one(resume);
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
    var isCensusMethodRecord = ((bdrs.mobile.getParameter('censusMethodId') !== undefined) || (record.censusMethod() !== null))
    
    bdrs.mobile.Debug('isCensusMethodRecord : ' + isCensusMethodRecord);
	jQuery('.bdrs-page-record .speciesEntryField').remove();
	jQuery('.bdrs-page-record .numberSlider').remove();
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
		
		isTaxonomic = cmethod.taxonomic() !== bdrs.mobile.pages.record.NONTAXONOMIC;
		isOptionallyTaxonomic = cmethod.taxonomic() === bdrs.mobile.pages.record.OPTIONALLYTAXONOMIC;
		
	} else {
	    // If there are no census method records, hide the collapsible.
	    var methodAttrsWrapperElem = jQuery('#record-method-attributes');
        var methodAttrsCollapsible = methodAttrsWrapperElem.parents('[data-role=collapsible]');
        methodAttrsCollapsible.hide();
        methodAttrsWrapperElem.empty();
        
        // No census method means that the record will be optionally taxonomic.
		isTaxonomic = true;
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
                parent.children().count(resume);
            }
        } else {
            waitfor(siblingCount) {
                parent.children().filter('censusMethod','=', cmethod.id).count(resume);                
            }
        }
        
        // If we are adding a new record, we need to increment by one (the index of this record).
        siblingCount += 1;
        siblingCountElem.text(siblingCount);
    }
    
    if(isTaxonomic) {
        // Is Taxonomy Required
        var templateParams = {
            required : !isOptionallyTaxonomic
        };
	    // add the number slider.
	    bdrs.template.renderOnlyCallback('recordNumberSlider', templateParams, function(element) {
		    jQuery('#record-form').prepend(element);
		    jQuery('#record-number').textinput();
		    jQuery('#record-number').slider();
		    jQuery('.bdrs-page-record .numberSlider').fieldcontain();
	    });
	
	    // add the species box.
	    bdrs.template.renderOnlyCallback('recordSpeciesInput', templateParams, function(element) {
		    jQuery('#record-form').prepend(element);
		    bdrs.mobile.restyle(element);
		    new bdrs.mobile.widget.RecentTaxaWidget(2).after(element);
		
		    var scientificName = record.species() === null ? '' : record.species().scientificName();
		    jQuery('#record-species').val(scientificName).autocomplete({
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
			        jQuery.mobile.pageLoading(false);
			        
			        var scientificName = jQuery('#record-species').val();
			        var taxon;
                    waitfor(taxon) {
                        Species.all().filter('scientificName', '=', scientificName).one(resume);
                    }
   
                    bdrs.mobile.pages.record._insertTaxonGroupAttributes(taxon, {});
	                
                    jQuery.mobile.pageLoading(true);
                },
			    html: true
		    });
	    });
    }

    //////////////////////////
    // Sub Census Methods
    //////////////////////////
    bdrs.mobile.pages.record._insertSubCensusMethods(record, cmethod);

    ////////////////////////////////////
    // Survey & Taxon Group Attributes
    ////////////////////////////////////
    if (setting === null) {
	    // trouble in paradise.
	    bdrs.mobile.Error('Current Survey ID not known');
	    return;
    } else {
	    var survey;
	    waitfor(survey) {
		    Survey.findBy('server_id', setting.value(), resume);
	    }
	    if (survey !== null) {
	        jQuery.mobile.pageLoading(false);
		
            bdrs.mobile.pages.record._insertSurveyAttributes(record, survey, attributeValueMap);
		    bdrs.mobile.pages.record._insertTaxonGroupAttributes(record.species(), attributeValueMap);
		
		    jQuery.mobile.pageLoading(true);
	    } else {
		    bdrs.mobile.Error('Currently set survey is not in the database');
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
        jQuery('#record-latitude').val(latCoord);
        jQuery('#record-longitude').val(lonCoord);
	    jQuery('#record-accuracy').val(accuracy);
	
	    var when = record.when() === null ? bdrs.mobile.getCurrentDate() : bdrs.mobile.formatDate(record.when());
	    jQuery('#record-when').val(when);

        var time = record.time().length === 0 ? bdrs.mobile.getCurrentTime() : record.time(); 
	    jQuery('#record-time').val(time);
	
	    jQuery('#record-notes').val(record.notes());
        jQuery('#record-number').val(record.number());
    }
}
	
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
    
    jQuery('.recent-taxa-widget').remove();
    jQuery("#record-type").text('');
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
}

/**
 * Insert survey attributes into the record form.
 * 
 * @param record The object that backs the current form.
 * @param survey The survey containing the attributes to be added to the form.
 * @param attributeValueMap a map of record attributes keyed against the 
 * attribute ID. That is, { attributeID: attributeValueObj }.
 */
exports._insertSurveyAttributes = function(record, survey, attributeValueMap) {
    bdrs.mobile.Info('Survey Name : ' + survey.name() );
    var attributes;
    waitfor(attributes) {
        survey.attributes().order('weight', true).list(resume);
    }
    
    bdrs.mobile.Debug('Survey Attributes');
    
    // Clear old attributes and hide the collapsible.
    var surveyAttrsWrapperElem = jQuery('#record-survey-attributes');
    var surveyAttrsCollapsible = surveyAttrsWrapperElem.parents('[data-role=collapsible]');
    if(attributes.length === 0) {
        surveyAttrsCollapsible.hide();
    }
    surveyAttrsWrapperElem.empty();

    // Insert record attributes
    var recAttrFormField;
    for (var i = 0; i < attributes.length; i++) {
        recAttrFormField = new bdrs.mobile.attribute.AttributeValueFormField(attributes[i]);
        recAttrFormField.toFormField('#record-survey-attributes', attributeValueMap[attributes[i].id]);
    }
    
    // Show the collapsible.
    if(attributes.length > 0) {
        surveyAttrsCollapsible.show().trigger('expand');
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
                record.children().prefetch('species').filter('censusMethod', '=', subMethods[i]).list(resume);
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
                        childRecords[i].children().count(resume);
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
                    listViewItem.find("a").jqmData("recordId", childRecords[i].id);
                    listView.find("#"+listViewItemTmplParams.linkId).click(function(event) {
                        var record;
                        waitfor(record) {
                            Record.load(jQuery(event.currentTarget).jqmData("recordId"), resume);
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
                            jQuery.mobile.changePage("#point-intersect");
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
                    var isPointIntersect  = bdrs.mobile.pages.point_intersect.isPointIntersect(censusMethod, record, new Record());
                    if(isPointIntersect) {
                        jQuery.mobile.changePage("#point-intersect");
                    } else {
                        exports.Show();
                    }
		        }
            });
        }
	    
	    bdrs.mobile.restyle(subCensusCollapsibleSet);
	    subCensusCollapsibleSet.show();
    }
};

/**
 * Insert taxon group attributes to the form.
 * 
 * @param record The object that backs the current form.
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
            taxonGroupAttrsCollapsible.show().trigger('expand');
        }
    }
};

/**
 * Private function, does the actual recording.
 */
exports._record = function() {
    // Need to blur to force the species autocomplete to insert any 
    // required dom elements. 
    jQuery(":focus").blur();
    
    jQuery.mobile.pageLoading(false);
	bdrs.mobile.Debug ('Record called');
	
	bdrs.mobile.Debug(jQuery('#record-latitude').val());
	bdrs.mobile.Debug(jQuery('#record-longitude').val());
	bdrs.mobile.Debug(jQuery('#record-accuracy').val());
	bdrs.mobile.Debug(jQuery('#record-when').val());
	bdrs.mobile.Debug(jQuery('#record-time').val());
	bdrs.mobile.Debug(jQuery('#record-species').val());
	
	var scientificName = jQuery('#record-species').val();
	var species;
	waitfor(species) {
		Species.all().filter('scientificName', '=', scientificName).prefetch('taxonGroup').one(resume);
	}
	bdrs.mobile.Debug('Scientific Name for record: ' + scientificName);
	if (species != undefined) {
		bdrs.mobile.Debug(species.commonName());
		bdrs.mobile.Debug(species.scientificName());
	} else if (scientificName != '') {
		// Create a field species.
		var sp = new Species({
			scientificNameAndAuthor: "Field Species",
	    	scientificName: "Field Species",
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
		species = sp;
		waitfor() {
			persistence.flush(resume);
		}
		bdrs.mobile.Debug('Created a new species: ' + species.scientificName());
		bdrs.mobile.Debug(species.scientificName());
	}
	
    var setting;
    waitfor(setting) {
        Settings.findBy('key', 'current-survey-id', resume);
    }
    var survey;
    waitfor(survey) {
        Survey.findBy('server_id', setting.value(), resume);
    }
	
	var latitude = jQuery('#record-latitude').val();
    var longitude = jQuery('#record-longitude').val();
	var accuracy = jQuery('#record-accuracy').val();
    var when = bdrs.mobile.parseDate(jQuery('#record-when').val());
    var time = jQuery('#record-time').val();    
    var notes = jQuery('#record-notes').val();
    var number = jQuery('#record-number').val();
	
	var record;
    var attributeValueMap = {};
    bdrs.mobile.Debug('Pulled attributes from record form');
    var selectedRecordId = bdrs.mobile.getParameter('selected-record');
	if (selectedRecordId === undefined) {
        record = new Record();
        persistence.add(record);
        survey.records().add(record);
        if (species != undefined) {
            species.records().add(record);
			// Update Species Count index.
			bdrs.mobile.Debug('Updating species count');
        	var counter;
        	waitfor(counter) {
				SpeciesCount.all().filter('scientificName', '=', species.scientificName()).one(resume);
        	}
        	if (counter == null) {
				bdrs.mobile.Debug('Count not found, creating new one.');
				counter = new SpeciesCount({scientificName: species.scientificName(), 
					count: 1 });
				persistence.add(counter);
				counter.species(species);
        	} else {
        		bdrs.mobile.Debug('Count found, incrementing');
        		counter.count(counter.count() + 1);
        	}
        }
    } else {
        waitfor(record) {
            Record.all().filter('id', '=', selectedRecordId).one(resume);
        }
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
    
    record.modifiedAt(new Date());
    record.latitude(latitude);
    record.longitude(longitude);
	record.accuracy(accuracy);
    record.when(when);
    record.time(time);    
    record.notes(notes);
    record.number(number);
    
    if (bdrs.mobile.getParameter('record-parent') !== undefined) {
        var parent;
        waitfor(parent) {
            Record.load(bdrs.mobile.getParameter('record-parent'), resume);
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
	
	var attributes;
    waitfor(attributes) {
        survey.attributes().list(resume);
    }
    
    // Taxon Group Attributes
    if(species !== null) {
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
    
    if (bdrs.mobile.getParameter('censusMethodId') !== undefined) {
        // we are doing a census type record, and don't need the species box.
        var cmethod;
        waitfor(cmethod) {
            CensusMethod.load(bdrs.mobile.getParameter('censusMethodId'), resume);
        }
        
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
	
	persistence.flush();
	
	jQuery.mobile.pageLoading(true);
	
	return record;
}

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

