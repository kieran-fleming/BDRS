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

        if(selectLocation.val().length > 0) {
            var param = {
                locationId: selectLocation.val(),
                surveyId: jQuery('#surveyId').val(),
                ident: jQuery('#ident').val()
            };
            jQuery.getJSON(bdrs.contextPath+'/webservice/record/getRecordsForLocation.htm', param, function(data) {
                var rec;
                for(var i=0; i<data.length; i++) {
                    rec = data[i];
                    if(rec.number !== null) {
                        jQuery("[name=date_"+rec.when+"]").val(rec.number);
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
    } // End confirm dialog return check
    else {
        selectLocation.val(location.val());
    }
};

// End of Yearly Sightings -------------
// -------------------------------------

//--------------------------------------
// Single Site Multiple Taxa
//--------------------------------------
bdrs.contribute.singleSiteMultiTaxa = {};

bdrs.contribute.singleSiteMultiTaxa.speciesSearchSource = function(request, callback) {

    var params = {};
    params.q = request.term;
    params.surveyId = jQuery(this.element).data("surveyId");
    jQuery.getJSON(bdrs.contextPath+'/webservice/survey/speciesForSurvey.htm', params, function(data, textStatus) {
        var label;
        var result;
        var taxon;
        var resultsArray = [];
        for(var i=0; i<data.length; i++) {
            taxon = data[i];

            label = [];
            if(taxon.scientificName !== undefined && taxon.scientificName.length > 0) {
                label.push("<b><i>"+taxon.scientificName+"</b></i>");
            }
            if(taxon.commonName !== undefined && taxon.commonName.length > 0) {
                label.push(taxon.commonName);
            }

            label = label.join(' ');

            resultsArray.push({
                label: label,
                value: taxon.scientificName,
                data: taxon
            });
        }

        callback(resultsArray);
    });
};

bdrs.contribute.singleSiteMultiTaxa.addSighting = function(sightingIndexSelector, surveyIdSelector, sightingTableBody) {
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
        
        // Attach the autocomplete
        var search_elem = jQuery("[name="+sightingIndex+"_survey_species_search]");
        search_elem.data("surveyId", surveyId); 
        search_elem.autocomplete({
            source: bdrs.contribute.singleSiteMultiTaxa.speciesSearchSource,
            select: function(event, ui) {
                var taxon = ui.item.data;
                jQuery("[name="+sightingIndex+"_species]").val(taxon.id);
            },
            html: true,
            minLength: 2,
            delay: 300
        });
        
        // Attach the datepickers
        bdrs.initDatePicker();
        search_elem.parents("tr").ketchup();
    });
};

// Single Site Multiple Taxa -----------
// -------------------------------------
