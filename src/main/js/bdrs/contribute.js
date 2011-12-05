//--------------------------------------
// Contribute
//--------------------------------------
bdrs.contribute = {};

//--------------------------------------
// Yearly Sightings
//--------------------------------------
bdrs.contribute.yearlysightings = {};

bdrs.contribute.yearlysightings.init = function() {
    var form = jQuery('form');
    form.submit(bdrs.contribute.yearlysightings.submitHandler);

    var locationSelect = jQuery("#location");
    locationSelect.change(bdrs.contribute.yearlysightings.locationSelected);

    var sightingCells = jQuery(".sightingCell");
    sightingCells.change(bdrs.contribute.yearlysightings.validateCellChange);
    sightingCells.blur(bdrs.contribute.yearlysightings.validateCellChange);
};

bdrs.contribute.yearlysightings.submitHandler = function(event) {
    var form = jQuery(event.currentTarget);
    return form.find(".errorCell").length === 0;
};

bdrs.contribute.yearlysightings.validateCellChange = function(event) {
    var inp = jQuery(event.currentTarget);
    var cell = inp.parent("td");
    
    var isValid = true;    
    if(/^\d+$/.test(inp.val()) || (inp.val().length === 0)) {
        isValid = parseInt(inp.val(),10) < 1000000;
    } else {
        isValid = false;
    }
    
    if(isValid) {
        cell.removeClass("errorCell");
        var date = new Date(parseInt(inp.attr("name").split("_")[1], 10));
        inp.attr("title", bdrs.util.formatDate(date));
    } else {
        cell.addClass("errorCell");
        inp.attr("title", "Must be a positive integer or blank");
    }
};

/**
 * Event handler for inserting a record attribute
 * @param {Object} recAttr - record attribute to insert
 */
bdrs.contribute.yearlysightings.insertRecordAttribute = function(recAttr) {
    var inp = jQuery("[name=attribute_"+recAttr.attribute+"]");
    inp.val(recAttr.stringValue);
    
    // Repopulate files
    var fileInput = jQuery("#attribute_file_"+recAttr.attribute);
    if(fileInput.length > 0) {
        var fileUrl = bdrs.contextPath+"/files/download.htm?"+recAttr.fileURL;
        if(fileInput.hasClass("image_file")) {
            // Images
            var img = jQuery("<img/>");
            img.attr({
                width: 250,
                src: fileUrl,
                alt: "Missing Image"
            });
            
            var imgAnchor = jQuery("<a></a>");
            imgAnchor.attr("href", fileUrl);
            
            var imgContainer = jQuery("<div></div>");
            imgContainer.attr("id", "attribute_img_"+recAttr.attribute);
            
            imgContainer.append(imgAnchor);
            imgAnchor.append(img);
            
            inp.parent().before(imgContainer);
        }
        else if(fileInput.hasClass("data_file")) {
            
            // Data
            var dataAnchor = jQuery("<a></a>");
            dataAnchor.attr("href", fileUrl);
            dataAnchor.text(recAttr.stringValue);
            
            var dataContainer = jQuery("<div></div>");
            dataContainer.attr("id", "attribute_data_"+recAttr.attribute);
            
            dataContainer.append(dataAnchor);
            inp.parent().before(dataContainer);                                                                         
        }
    } // End file repopulation
};

/**
 * Event handler
 * @param {Object} event - event to be handled
 */
bdrs.contribute.yearlysightings.locationSelected = function(event) {

    var selectLocation = jQuery(event.currentTarget);
    var location = jQuery("[name=locationId]");

    var ans;
    if(location.val().length > 0) {
        ans = confirm("Changing the location will replace the data below. Do you wish to proceed?");
    }
    else {
        ans = true;
    }

    if(ans) {
        location.val(selectLocation.val());

        var locationId = selectLocation.val();
        var surveyId = jQuery('#surveyId').val();
        var ident = jQuery('#ident').val();

        bdrs.contribute.yearlysightings.loadCellData(locationId, surveyId, ident, true);
    } // End confirm dialog return check
    else {
        selectLocation.val(location.val());
    }
};

/**
 * Loads the cells in the yearly sightings form. Each cell represents a different
 * day in the year
 * 
 * @param {Object} locationId - the location id
 * @param {Object} surveyId - the survey id
 * @param {Object} ident - the ident of the user
 * @param {Object} editable - whether the form is editable or not
 */
bdrs.contribute.yearlysightings.loadCellData = function(locationId, surveyId, ident, editable) {
	// Clear all cells
    jQuery(".sightingCell").each(function(index, element){
        var inp = jQuery(element);
        var cell = inp.parents("td");
        inp.val('');
        cell.removeClass("errorCell");
        var date = new Date(parseInt(inp.attr("name").split("_")[1],10));
        inp.attr("title", bdrs.util.formatDate(date));
    });
    
    // Clear the survey scope attributes
    jQuery("[name^=attribute_]").val('');
    jQuery("[id^=attribute_img_], [id^=attribute_data_]").remove();

    if(locationId) {
        var param = {
            locationId: locationId,
            surveyId: surveyId,
            ident: ident
        };
        jQuery.getJSON(bdrs.contextPath+'/webservice/record/getRecordsForLocation.htm', param, function(data) {
            var rec;
            for(var i=0; i<data.length; i++) {
                rec = data[i];
                if(rec.number !== null) {
					if (editable) {
					   jQuery("[name=date_"+rec.when+"]").val(rec.number);	
					} else {
					   jQuery("#date_"+rec.when).text(rec.number);
					}
                }
            }
            
            if(typeof(rec) !== 'undefined') {
                // Use the last survey as the prototype to load the 
                // Survey scoped attributes
                for(var j=0; j<rec.attributes.length; j++) {
                    var param = {
                        recordAttributeId: rec.attributes[j],
                        ident: jQuery('#ident').val()
                    };
                    jQuery.getJSON(bdrs.contextPath+"/webservice/record/getRecordAttributeById.htm", param, bdrs.contribute.yearlysightings.insertRecordAttribute);
                } // End for-loop request for survey scope attributes
            }
        });
    } // End location update
}

// End of Yearly Sightings -------------
// -------------------------------------

//--------------------------------------
// Single Site Multiple Taxa
//--------------------------------------
bdrs.contribute.singleSiteMultiTaxa = {};

/**
 * 
 * @param {Object} sightingIndexSelector - on the form page there is a hidden input, aka the sighting index, that shows
 * how many rows need to be saved. This is the jquery selector for the sighting index element.
 * @param {Object} surveyIdSelector - on the form there is a hidden input for the survey id, this is the selector for
 * the survey id element.
 * @param {Object} speciesRequired - a boolean, sets validation on the species field.
 * @param {Object} numberRequired - a boolean, sets validation on the number field.
 */
bdrs.contribute.singleSiteMultiTaxa.init = function(sightingIndexSelector, surveyIdSelector, speciesRequired, numberRequired) {
	var sightingIndexElem = jQuery(sightingIndexSelector);
    var sightingIndex = parseInt(sightingIndexElem.val(), 10);
    // note we don't increment the sighting index during init
    var surveyId = jQuery(surveyIdSelector).val();
	
	for (var i=0; i< sightingIndex; ++i) {
		bdrs.contribute.singleSiteMultiTaxa.attachRowControls(i, surveyId, speciesRequired, numberRequired);
	}
};


/**
 * @param {Object} sightingIndexSelector - on the form page there is a hidden input, aka the sighting index, that shows
 * how many rows need to be saved. This is the jquery selector for the sighting index element.
 * @param {Object} surveyIdSelector - on the form there is a hidden input for the survey id, this is the selector for
 * the survey id element.
 * @param {Object} sightingTableBodyy - jQuery selector for the sighting table body element
 * @param {Object} speciesRequired - a boolean, sets validation on the species field.
 * @param {Object} numberRequired - a boolean, sets validation on the number field.
 */
bdrs.contribute.singleSiteMultiTaxa.addSighting = function(sightingIndexSelector, surveyIdSelector, sightingTableBody, speciesRequired, numberRequired, showScientificName) {
    var sightingIndexElem = jQuery(sightingIndexSelector);
    var sightingIndex = parseInt(sightingIndexElem.val(), 10);
    sightingIndexElem.val(sightingIndex+1);
    
    var surveyId = jQuery(surveyIdSelector).val();
    
    var url = bdrs.contextPath+"/bdrs/user/singleSiteMultiTaxa/sightingRow.htm";
    var param = {
        sightingIndex: sightingIndex,
        surveyId: surveyId
    };
    jQuery.get(url, param, function(data) {
        jQuery(sightingTableBody).append(data);
        
		bdrs.contribute.singleSiteMultiTaxa.attachRowControls(sightingIndex, surveyId, speciesRequired, numberRequired, showScientificName);
    });
};

/**
 * Helper function for attaching row controls
 * 
 * @param {Object} sightingIndex - the sighting index of the row to attach the controls on
 * @param {Object} surveyId - the survey id
 * @param {Object} speciesRequired - is the species field mandatory ?
 * @param {Object} numberRequired - is the number field mandatory ?
 * @param {Object} showScientificName - when true will show the scientific name, else will use the common name
 */
bdrs.contribute.singleSiteMultiTaxa.attachRowControls = function(sightingIndex, surveyId, speciesRequired, numberRequired, showScientificName) {

	// Attach the autocomplete
    var search_elem = jQuery("[name="+sightingIndex+"_survey_species_search]");
    search_elem.data("surveyId", surveyId); 
    search_elem.autocomplete({
        source: bdrs.contribute.getAutocompleteSourceFcn(showScientificName),
        select: function(event, ui) {
            var taxon = ui.item.data;
            jQuery("[name="+sightingIndex+"_species]").val(taxon.id).blur();
        },
        html: true,
        minLength: 2,
        delay: 300
    });
    
    // Attach the datepickers
    bdrs.initDatePicker();
    // attach the validation
    var species_elem = jQuery("[name="+sightingIndex+"_species]");
    if (speciesRequired) {
        species_elem.addClass("validate(required)");
    }
    
    var count_elem = jQuery("[name="+sightingIndex+"_number]");
    if (numberRequired) {
        count_elem.addClass("validate(positiveIntegerLessThanOneMillion)");
    } else {
        count_elem.addClass("validate(positiveIntegerLessThanOneMillionOrBlank)");
    }
    
    search_elem.parents("tr").ketchup();
}

//--------------------------------------
// End Single Site Multiple Taxa -------
// -------------------------------------
//--------------------------------------
// Single Site All Taxa
//--------------------------------------
bdrs.contribute.singleSiteAllTaxa = {};

/**
 * Add a row to the sightings table
 * 
 * @param {Object} sightingIndexSelector - for the input which has the number of existing rows in the sightings table
 * @param {Object} surveyIdSelector - for the input that holds the survey id
 * @param {Object} sightingTableBody - the table body for the sightings table
 */
bdrs.contribute.singleSiteAllTaxa.addSighting = function(sightingIndexSelector, surveyIdSelector, sightingTableBody) {
	var sightingIndexElem = jQuery(sightingIndexSelector);
  var sightingIndex = parseInt(sightingIndexElem.val(), 10);
  sightingIndexElem.val(sightingIndex+1);
  
  var surveyId = jQuery(surveyIdSelector).val();
  
  var url = bdrs.contextPath+"/bdrs/user/singleSiteAllTaxa/sightingTableAllTaxa.htm";
  var param = {
      sightingIndex: sightingIndex,
      surveyId: surveyId
  };
 
  jQuery.ajax(url, {
	  data: param, 
	  success: function(data) {
	      jQuery(sightingTableBody).append(data);
	      
	      // Attach the datepickers
	      bdrs.initDatePicker();
  	  },
  	  async: false
  });
  
  jQuery(sightingTableBody).ketchup();
  
  // update the sightingIndex field to match the attribute count
  sightingIndexElem.val(jQuery(sightingTableBody).find('tr').length);
};


/**
 * Will initialise the auto complete events, see parameters below
 *
 * required parameters:
 * surveySpeciesSearchSelector - selector for the text input used to search for the species, autocomplete will be applied to this input
 * speciesIdSelector - the selector for the species ID field. This field is normally hidden
 * taxonAttrRowSelector - the selector for the taxon attr rows (i.e. will probably be a wildcard)
 * surveyId - the survey id
 * recordId - the record id
 * editable - whether the field is editable or not
 * attributeTbodySelector - selector for the table where we will append our taxon group attributes
 * showScientificName - boolean. if true, uses the scientific name for auto complete, else uses the common name
 * 
 * Species auto complete
 * 
 * @param {Object} args - mandatory arguments to do initialisation of species autocomplete. see notes above...
 */
bdrs.contribute.initSpeciesAutocomplete = function(args) {
	
	var surveySpeciesSearchSelector = args.surveySpeciesSearchSelector;
	var speciesIdSelector = args.speciesIdSelector;
	var taxonAttrRowSelector = args.taxonAttrRowSelector;
	var surveyId = args.surveyId;
	var recordId = args.recordId;
	var editable = args.editable;
	var attributeTbodySelector = args.attributeTbodySelector;
	var showScientificName = args.showScientificName;
	
	jQuery(surveySpeciesSearchSelector).autocomplete({
		
		source: bdrs.contribute.getAutocompleteSourceFcn(showScientificName),
        select: function(event, ui) {
            var taxon = ui.item.data;
            jQuery(speciesIdSelector).val(taxon.id).trigger("blur");
            
            // Load Taxon Group Attributes
            // Clear the group attribute rows
            jQuery(taxonAttrRowSelector).parents("tr").remove();
            
            // Build GET request parameters
            var params = {};
            params.surveyId = surveyId;
            params.taxonId = taxon.id;
			params.recordId = recordId;
            params.editForm = editable;

            // Issue Request
            jQuery.get(bdrs.contextPath+"/bdrs/user/ajaxTrackerTaxonAttributeTable.htm", params, function(data) {
                jQuery(attributeTbodySelector).append(data);
            });
        },
        change: function(event, ui) {
            if(jQuery(event.target).val().length === 0) {
                jQuery(speciesIdSelector).val("").trigger("blur");
            
                // Clear the group attribute rows
                jQuery(taxonAttrRowSelector).parents("tr").remove();
            }
        },
        minLength: 2,
        delay: 300,
        html: true
    });
}

/**
 * Get the function to use as the parameter for the jQuery autocomplete 'source'
 * parameter
 * 
 * @param {Object} showScientificName - boolean, when true will use the scientific name
 * for the autocomplete value. Else will use the common name
 */
bdrs.contribute.getAutocompleteSourceFcn = function(showScientificName) {
	var fcn = function(request, callback) {
		var params = {};
		params.q = request.term;
		params.surveyId = jQuery(this.element).data("surveyId");
		jQuery.getJSON(bdrs.contextPath + '/webservice/survey/speciesForSurvey.htm', params, function(data, textStatus){
			var label;
			var result;
			var taxon;
			var resultsArray = [];
			for (var i = 0; i < data.length; i++) {
				taxon = data[i];
				
				label = [];
				if (taxon.scientificName !== undefined && taxon.scientificName.length > 0) {
					label.push("<b><i>" + taxon.scientificName + "</b></i>");
				}
				if (taxon.commonName !== undefined && taxon.commonName.length > 0) {
					label.push(taxon.commonName);
				}
				
				label = label.join(' ');
				
				resultsArray.push({
					label: label,
					value: showScientificName ? taxon.scientificName : taxon.commonName,
					data: taxon
				});
			}
			
			callback(resultsArray);
		});
	}
	return fcn;
}
