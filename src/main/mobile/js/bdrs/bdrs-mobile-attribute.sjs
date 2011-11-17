
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
exports.type.INTEGER_WITH_RANGE = "IR";
exports.type.IMAGE = "IM";
exports.type.DECIMAL = "DE";
exports.type.BARCODE = "BC";
exports.type.REGEX = "RE";
exports.type.HTML = "HL";
exports.type.HTML_COMMENT = "CM";
exports.type.HTML_HORIZONTAL_RULE = "HR";

exports.IMG_SRC_BASE64_PREFIX = 'data:image/jpeg;base64,';

// { attribute.id: "option value" }
exports._lastAttrSelection = {};

/**
 * @param attribute
 */
exports.AttributeValueFormField = function(attribute) {

    this.getAttributeInputSelector = function() {
        if(bdrs.mobile.attribute.type.MULTI_CHECKBOX === attribute.typeCode() || 
            bdrs.mobile.attribute.type.MULTI_SELECT === attribute.typeCode()) {
           return 'input[name^=record-attr-'+attribute.id+'-]';
        } else {
            return '#record-attr-'+attribute.id;
        }
    }

    this.toFormField = function(target, attributeValue, callback) {
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
        
        
		var tmplName = 'attributeValue'+attribute.typeCode();
		var tmplParams = [{
            id: attribute.id,
		    required: attribute.required(),
		    description: attribute.description(),
		    value: attributeValueValue
		}];
		
		// Special handling for select boxes
		if (bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES === attribute.typeCode()) {
		    var optionTmplName = tmplName+"-option";
		    var optionTmplParams = [];
		    
		    var attrOpts;
            waitfor(attrOpts) {
                attribute.options().list(resume);
            }
		    
		    var opt;
		    for(var i=0; i<attrOpts.length; i++) {
		        opt = attrOpts[i];
		        optionTmplParams.push({
		            value: opt.value(),
		            text: opt.value(),
		            selected: attributeValueValue == opt.value()
		        });
		    }

            var optionElements;		    
		    waitfor (optionElements) {
		        bdrs.template.renderOnlyCallback(optionTmplName, optionTmplParams, resume); 
		    }
		    
		    var tmpSelect = jQuery("<select></select>");
		    optionElements.appendTo(tmpSelect);
		    tmplParams[0].options = tmpSelect.html();
		    
		} else if (bdrs.mobile.attribute.type.INTEGER_WITH_RANGE === attribute.typeCode()) {
		
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
		} 
		// Multi select attributes have been disabled (and replaced with multi checkboxes)
        // because the current implementation in jquery mobile is buggy.
        // 1) The dialog does not close consistently in 1.0b1
        // 2) The selection of the first item does not update the display 1.0a4 and 1.0b1
		/*else if (bdrs.mobile.attribute.type.MULTI_SELECT === attribute.typeCode()) {
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
		            value: opt.value(),
		            text: opt.value(),
		            selected: jQuery.inArray(opt.value(), attributeValueValueArray) > -1,
		        });
		    }

            var optionElements;		    
		    waitfor (optionElements) {
		        bdrs.template.renderOnlyCallback(optionTmplName, optionTmplParams, resume); 
		    }
		    
		    var tmpSelect = jQuery("<select></select>");
		    optionElements.appendTo(tmpSelect);
		    tmplParams[0].options = tmpSelect.html();
		    
		}*/
		
		bdrs.template.renderCallback(tmplName, tmplParams, target, function() {
		    
		    if(bdrs.mobile.attribute.type.DATE === attribute.typeCode()) {
		        jQuery('#record-attr-'+attribute.id).datepicker();
	        } else if(bdrs.mobile.attribute.type.TIME === attribute.typeCode()) {
		        jQuery('#record-attr-'+attribute.id).timepicker();
		    } else if (bdrs.mobile.attribute.type.IMAGE === attribute.typeCode()) {
                if (bdrs.mobile.cameraExists()) {
                    if (attributeValueValue) {
                      // if there is picture data create a tag that will be used
                      // as the record attribute 'value'. We extract the actual
                      // data from this value from the img src attribute in the 
                      // fromFormField function
                      bdrs.mobile.attribute.setPicture(attribute.id, bdrs.mobile.attribute.createPicImg(attributeValueValue));
                    }                    
                } else {
                    // do nothing...for now
                }
            }
	        
	        //add clickHandler to scan button
	        if (bdrs.mobile.attribute.type.BARCODE === attribute.typeCode()) {
	        	jQuery('#record-btn-'+attribute.id).click(function(){
	        		var scanId = jQuery(this).attr('id');
	        		bdrs.phonegap.barcode.scan(scanId);
	        	});
	        }
	        
		    bdrs.mobile.restyle(target);

		    // Trigger the callback if provided.
		    if(jQuery.isFunction(callback)) {
		    	callback();
		    }
		});        
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
