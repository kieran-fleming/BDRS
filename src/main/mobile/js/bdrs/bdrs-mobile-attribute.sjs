
exports.type = {};
exports.type.INTEGER = "IN";
exports.type.DATE = "DA";
exports.type.TIME = "TM";
exports.type.SINGLE_CHECKBOX = "SC";
exports.type.MULTI_CHECKBOX = "MC";
exports.type.MULTI_SELECT = "MS";
exports.type.STRING = "ST";
exports.type.STRING_WITH_VALID_VALUES = "SV";
exports.type.TEXT = "TA";
exports.type.STRING_AUTOCOMPLETE = "SA";
exports.type.STRING_AUTOCOMPLETE_WITH_DATASOURCE = "SD";
exports.type.LATITUDE_LONGITUDE = "LL";
exports.type.DECIMAL_WITH_RANGE = "DR";
exports.type.INTEGER_WITH_RANGE = "IR";
exports.type.IMAGE = "IM";
exports.type.DECIMAL = "DE";
exports.type.BARCODE = "BC";
exports.type.REGEX = "RE";
exports.type.HTML = "HL";
exports.type.HTML_COMMENT = "CM";
exports.type.HTML_HORIZONTAL_RULE = "HR";

exports.IMG_SRC_BASE64_PREFIX = 'data:image/jpeg;base64,';

exports.behaviour = {};
exports._lastAttrSelection = {};

/**
 * Attaches autocomplete and the datasource to the element.
 * @param element to which the behaviour needs to be attached.
 */
exports.behaviour[exports.type.STRING_AUTOCOMPLETE_WITH_DATASOURCE] = function(element) {
	//TODO: Make abstract so other datasources can be used
    jQuery(element).autocomplete({
        source: function(request, response) {
    		//get species for current survey
    		bdrs.mobile.survey.getDefault().species().filter('scientificName','like','%' + request.term + '%').or(new persistence.PropertyFilter('commonName','like','%' + request.term + '%')).limit(5).list(null,function(speciesList){
    			var names = [];
    			speciesList.forEach(function(aSpecies){
    				//add the names of the found species to the response
                    names.push({ label : aSpecies.commonName() + ' - <i>' + aSpecies.scientificName()+ '</i>', value : aSpecies.scientificName()});
    			});
    			response(names);
    		});
        },
        change: function(event, ui) {
            jQuery.mobile.pageLoading(false);
            
            var scientificName = jQuery(element).val();
            var taxon;
            waitfor(taxon) {
                Species.all().filter('scientificName', '=', scientificName).one(resume);
            }

            bdrs.mobile.pages.record._insertTaxonGroupAttributes(taxon, {});
            
            jQuery.mobile.pageLoading(true);
        },
        open: function(event, ui){
        	jQuery('.ui-autocomplete').css('z-index',100);
        },
        html: true
    });
};

/**
 * Attaches quick pick numbers to the  integer field.
 * @param element to which the behaviour needs to be attached.
 */
exports.behaviour[exports.type.INTEGER] = function(element) {
	var id = jQuery(element).attr('id');
	var countId = "#count-" + id;
	jQuery(countId + " :button").each(function(index, btnElement){
		jQuery(btnElement).click(function(){
			var currentValue = jQuery(element).val();
			if (currentValue === "") {
				currentValue = 0;
			}
			currentValue = parseInt(currentValue);
			var addValue = parseInt(jQuery(btnElement).attr('addval'));
			if (addValue === 0){
				jQuery(element).val(addValue);
			} else {
				jQuery(element).val(currentValue + addValue);
			}
		});
	});
}

/**
 * Attaches quick pick numbers to the integer with range field.
 * @param element to which the behaviour needs to be attached.
 */
exports.behaviour[exports.type.INTEGER_WITH_RANGE] = function(element) {
	exports.behaviour[exports.type.INTEGER](element);
}

/**
 * Attaches clickhandler to gps button which when clicked on populates the lat and lon fields
 * @param element to which the behaviour needs to be attached.
 * @param options the selectors of the lat,lon and accuracy input fields.
 */
exports.behaviour[exports.type.LATITUDE_LONGITUDE] = function(element, options){
    jQuery('#record-gps').click(function (event) {
        var position;
        waitfor(position) {
            bdrs.mobile.geolocation.getCurrentPosition(resume);
        }
        if (position !== undefined) {
            jQuery(options.latSelector).val(bdrs.mobile.roundnumber(position.coords.latitude, 5));
            jQuery(options.lonSelector).val(bdrs.mobile.roundnumber(position.coords.longitude, 5));
            if (options.accuracySelector !== undefined) {
            	jQuery(options.accuracySelector).val(bdrs.mobile.roundnumber(position.coords.accuracy, 5));
            }   
        } else {
            bdrs.mobile.Debug('Could not get GPS loc');
        }
    });
}

/**
 * Attaches date picker to field.
 * @param element to which the behaviour needs to be attached.
 */
exports.behaviour[exports.type.DATE] = function(element) {
	jQuery(element).datepicker({maxDate: new Date()});
};

/**
 * Attaches time picker to field.
 * @param element to which the behaviour needs to be attached.
 */
exports.behaviour[exports.type.TIME] = function(element) {
	jQuery(element).timepicker( {showNowButton: true});
};

/**
 * Attaches behaviour to string with values input.
 * @param element to which the behaviour needs to be attached.
 * @param options the selectors of the lat and lon input fields.
 */
exports.behaviour[exports.type.STRING_WITH_VALID_VALUES] = function(element, options) {
	// On selecting a location from the list the lat and long fields will be populated with the location values if tehy exist.
	if (options.attributeName === 'Location') {
		var survey = bdrs.mobile.survey.getDefault();
		var pointAttributes;
		waitfor(pointAttributes) {
			survey.attributes().filter('name','=','Point').list(resume);
		}
		if(pointAttributes.length > 0){
			jQuery(element).change(function(){
				var serveridLatLon = jQuery(element + ":selected").val();
				var serveridLatLonArray = serveridLatLon.split(":");
				jQuery("#record-attr-" + pointAttributes[0].id + "-lat").val(bdrs.mobile.roundnumber(serveridLatLonArray[1], 5));
		        jQuery("#record-attr-" + pointAttributes[0].id + "-lon").val(bdrs.mobile.roundnumber(serveridLatLonArray[2], 5));
			});
		}
	}
};

/**
 * @param attribute
 */
exports.AttributeValueFormField = function(attribute) {

	/**
	 * @return selector of the input
	 */
    this.getAttributeInputSelector = function() {
        if(bdrs.mobile.attribute.type.MULTI_CHECKBOX === attribute.typeCode() || 
            bdrs.mobile.attribute.type.MULTI_SELECT === attribute.typeCode()) {
           return 'input[name^=record-attr-'+attribute.id+'-]';
        } else {
            return '#record-attr-'+attribute.id;
        }
    }

	/**
	 * Transforms attribute to a formfield
	 * @param target selector from the element that will receive the formfield.
	 * @param attributeValue an optional value for the formField
	 * @param record which could contain a value for the formField
	 * @param callback
	 */
    this.toFormField = function(target, attributeValue, record, callback) {
        var attributeValueValue;
        if(attributeValue === undefined || attributeValue === null) {
            if(bdrs.mobile.attribute._lastAttrSelection[attribute.id] !== undefined) {
                attributeValueValue = bdrs.mobile.attribute._lastAttrSelection[attribute.id];
            } else {
                attributeValueValue = '';
            }
        } else {
            attributeValueValue = attributeValue.value();
        }
        
        // Setup template parameters
		var tmplName = 'attributeValue'+attribute.typeCode();
		var tmplParams = [{
            id: attribute.id,
		    required: attribute.required(),
		    description: attribute.description(),
		    value: attributeValueValue,
		    behaviourType: attribute.typeCode()
		}];
		var options = {};
		
		// Special handling for select boxes
		if (bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES === attribute.typeCode()) {
			
		    var optionTmplName = tmplName+"-option";
		    var optionTmplParams = [];
		    
		    var attrOpts;
            waitfor(attrOpts) {
                attribute.options().list(resume);
            }
            
            if (attribute.name() === 'Location') {
            	// Get user-locations
            	var locations;
                var userLocations;
                waitfor(userLocations) {
                	bdrs.mobile.User.locations().order('weight',true).list(resume);
                }
                //get survey-locations
                var surveyLocations;
                waitfor(surveyLocations) {
                	var survey = bdrs.mobile.survey.getDefault();
                    survey.locations().order('weight',true).list(resume);
                }
                locations = userLocations.concat(surveyLocations);
                // Create options
                var opt;
                var selectedOption;
    		    for(var i=0; i<locations.length; i++) {
    		        opt = locations[i];
    		        selectedOption = false;
    		        var locationRecord;
    		        waitfor(locationRecord) {
    		        	opt.records().filter('id','=',record.id).list(resume);
    		        }
    		        if (locationRecord.length > 0) {
    		        	selectedOption = true;
    		        }
    		        var serveridLatLon = opt.server_id() + ":" + opt.latitude() + ":" + opt.longitude();
    		        optionTmplParams.push({
    		            value: serveridLatLon,
    		            text: opt.name(),
    		            selected: selectedOption
    		        });
    		    }
            } else {
    		    var opt;
    		    for(var i=0; i<attrOpts.length; i++) {
    		        opt = attrOpts[i];
    		        optionTmplParams.push({
    		            value: opt.value(),
    		            text: opt.value(),
    		            selected: attributeValueValue == opt.value()
    		        });
    		    }
            }
            
            var optionElements;		    
		    waitfor (optionElements) {
		        bdrs.template.renderOnlyCallback(optionTmplName, optionTmplParams, resume); 
		    }
		    
		    var tmpSelect = jQuery("<select></select>");
		    optionElements.appendTo(tmpSelect);
		    tmplParams[0].options = tmpSelect.html();
		    
		} else if (bdrs.mobile.attribute.type.INTEGER_WITH_RANGE === attribute.typeCode()) {
			if (attribute.name() === "Number") {
				if(record.number() !== null ) {
					tmplParams.value = record.number();
				}
			}
		    //Special handling for input boxes with integer range requirement	
			var optionTmplParams = [];
			
			var attrOpts;
            waitfor(attrOpts) {
                attribute.options().list(resume);
            }
            
		    var opt;
		    var optString= "";
		    for(var i=0; i<attrOpts.length; i++) {
		        opt = attrOpts[i];
		        optString += opt.value() + " ";
		    }
		    
		    tmplParams[0].minmax = optString;
            
		} else if (bdrs.mobile.attribute.type.IMAGE === attribute.typeCode()) {
		
		    if (bdrs.mobile.cameraExists()) {
		        tmplName = tmplName+"-camera";
            } else {
                tmplName = tmplName+"-file";
            }
		    
		} else if (bdrs.mobile.attribute.type.BARCODE === attribute.typeCode() 
				|| bdrs.mobile.attribute.type.REGEX === attribute.typeCode()
				) {
			
			var attrOpts;
            waitfor(attrOpts) {
                attribute.options().list(resume);
            }
            
            var opt;
		    var optString= "";
            for(var i=0; i<attrOpts.length; i++) {
		        opt = attrOpts[i];
		        optString += opt.value() + ",";
		    }
            optString = optString.slice(0,-1);
            // prepend the '^' and append the '$' to make the js validation match java validation
			tmplParams[0].regExp = "^" + optString + "$";
			
			if (bdrs.phonegap.isPhoneGap()) {
		        tmplName = tmplName+"-btn";
            }
            
		} else if (bdrs.mobile.attribute.type.SINGLE_CHECKBOX === attribute.typeCode()) {
		  tmplParams[0].value = bdrs.mobile.parseBoolean(tmplParams[0].value);
		} else if (bdrs.mobile.attribute.type.MULTI_CHECKBOX === attribute.typeCode() || 
		            bdrs.mobile.attribute.type.MULTI_SELECT === attribute.typeCode()) {
		    var optionTmplName = tmplName+"-option";
		    var optionTmplParams = [];
		    
		    var attrOpts;
            waitfor(attrOpts) {
                attribute.options().list(resume);
            }
		    
		    var opt;
		    var attributeValueValueArray = bdrs.mobile.csv.fromCSVString(attributeValueValue);
		    for(var i=0; i<attrOpts.length; i++) {
		        opt = attrOpts[i];
		        optionTmplParams.push({
		            id: tmplParams[0].id,
                    index: i,
                    optname: opt.value(),
                    checked: jQuery.inArray(opt.value(), attributeValueValueArray) > -1,
                    required: tmplParams[0].required,
		        });
		    }

            var optionElements;		    
		    waitfor (optionElements) {
		        bdrs.template.renderOnlyCallback(optionTmplName, optionTmplParams, resume); 
		    }
		    
		    var tmpSelect = jQuery("<select></select>");
		    optionElements.appendTo(tmpSelect);
		    tmplParams[0].options = tmpSelect.html();
		} else if (bdrs.mobile.attribute.type.STRING_AUTOCOMPLETE_WITH_DATASOURCE === attribute.typeCode()) {
			if (attribute.name() === "Species") {
				if(record.species() !== null ) {
					tmplParams.value = record.species().scientificName();
				}
			}
		} else if (bdrs.mobile.attribute.type.TEXT === attribute.typeCode()) {
			if (attribute.name() === "Notes") {
				if(record.notes() !== null ) {
					tmplParams.value = record.notes();
				}
			}
		}

		if(jQuery.isFunction( bdrs.mobile.attribute.behaviour[attribute.typeCode()])){
			bdrs.template.renderCallback(tmplName, tmplParams, target, function(){
				options.attributeName = attribute.name();
				var element = jQuery('#record-attr-'+ attribute.id);
				if(attribute.name() === 'Point') {
					options.latSelector = "#record-attr-" + attribute.id + "-lat";
					options.lonSelector = "#record-attr-" + attribute.id + "-lon";
					//TODO: This can be done cleaner
					var survey = bdrs.mobile.survey.getDefault();
					var accuracyAttribute;
					waitfor(accuracyAttribute) {
						survey.attributes().filter('name','=','AccuracyInMeters').list(resume);
					}
					if (accuracyAttribute.length > 0) {
						options.accuracySelector = "#record-attr-" + accuracyAttribute[0].id;
					}
				}
				bdrs.mobile.attribute.behaviour[attribute.typeCode()](element, options);
				bdrs.mobile.restyle(target);
				 if(jQuery.isFunction(callback)) {
				    	callback();
				    }
			});
		}else{
			bdrs.template.renderCallback(tmplName, tmplParams, target, function(){
				bdrs.mobile.restyle(target);
			});
		}
    };

    this.fromFormField = function(target, queryCollection, recAttr) {
        var inputElem = jQuery(this.getAttributeInputSelector());
        if(inputElem.length === 0) {
            return;
        }
        if(recAttr === undefined || recAttr === null) {
            recAttr = new AttributeValue();
            queryCollection.add(recAttr);
            attribute.attributeValues().add(recAttr);
        }

        if (bdrs.mobile.attribute.type.IMAGE === attribute.typeCode()) {
            if (bdrs.mobile.cameraExists()) {
                var imgArray = jQuery("img", inputElem);
                if (imgArray.length === 1) {
                    // the documentation for .attr() says it returns the attribute value for the 
                    // first element in the matched set. Actually when I tried to use .attr()
                    // on the 0 index element of imgArray I got an exception saying that attr
                    // didn't exist ???
                    var data = imgArray.attr("src").substr(bdrs.mobile.attribute.IMG_SRC_BASE64_PREFIX.length);
                    recAttr.value(data);
                }
            } else {
                recAttr.value(new String(inputElem.val()));
            }
        } else if(bdrs.mobile.attribute.type.MULTI_CHECKBOX === attribute.typeCode() || 
                    bdrs.mobile.attribute.type.MULTI_SELECT === attribute.typeCode()) {
            var elems = inputElem.parent().find(":checked");
            var optArray = [];
            for(var i=0; i<elems.length; i++) {
                optArray.push(jQuery(elems[i]).val())
            }
            recAttr.value(bdrs.mobile.csv.toCSVString(optArray));
        }
        // Multi select attributes have been disabled (and replaced with multi checkboxes)
        // because the current implementation in jquery mobile is buggy.
        // 1) The dialog does not close consistently in 1.0b1
        // 2) The selection of the first item does not update the display 1.0a4 and 1.0b1
        /* else if(bdrs.mobile.attribute.type.MULTI_SELECT == attribute.typeCode()) {
            recAttr.value(bdrs.mobile.csv.toCSVString(inputElem.val()));
        } */
        else if(bdrs.mobile.attribute.type.SINGLE_CHECKBOX === attribute.typeCode()) {
            var value = inputElem.parent().find(":checked").length === 1 ? "true" : "false";
            recAttr.value(value);
        } else {
            recAttr.value(new String(inputElem.val()));
            
            if(bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES === attribute.typeCode()) {
                bdrs.mobile.attribute._lastAttrSelection[attribute.id] = inputElem.val();
            }
        }
    };

    return this;
};

/** 
 * the click handler for starting up the camera 
 *  id: the record attribute ID to take the picture for...
 */
exports.takePicture = function(id) {
    var picResult = bdrs.mobile.camera.getPicture();
    if (picResult.success) {
        bdrs.mobile.attribute.setPicture(id, bdrs.mobile.attribute.createPicImg(picResult.data));
    } else {
        bdrs.mobile.Info(picResult.message);
    }
};

// id - id of the record attribute of type IMAGE
// img - dom node of the img
exports.setPicture = function(id, img) {
    var selector = '#record-attr-' + id;
    jQuery(selector).empty();
    jQuery(selector).append(img);
};

// TODO: move this html templatey stuff into bdrs-mobile-template.js
exports.createPicImg = function(picdata) {
    return jQuery('<img src=' + bdrs.mobile.attribute.IMG_SRC_BASE64_PREFIX + picdata + ' width="200" />');
};

/**
 * Removes the attributes and their options from the database.
 * @param attributes An array of entity objects.
 */
exports.removeAttributes = function(attributes) {
	var attribute;
	var options;
	for (var i=0; i<attributes.length; i++) {
		attribute = attributes[i];
		waitfor (options) {
			attribute.options().list(resume);
		}
		for (var j=0; j<options.length; j++) {
			persistence.remove(options[j]);
		}
		persistence.remove(attribute);
	}
}
