
exports.type = {};
exports.type.DATE = "DA";
exports.type.STRING_WITH_VALID_VALUES = "SV";
exports.type.INTEGER_WITH_RANGE = "IR";
exports.type.IMAGE = "IM";
exports.type.DECIMAL = "DE";
exports.IMG_SRC_BASE64_PREFIX = 'data:image/jpeg;base64,';

// { attribute.id: "option value" }
exports._lastAttrSelection = {};

exports.AttributeValueFormField = function(attribute) {

    this.getAttributeInputSelector = function() {
        return '#record-attr-'+attribute.id;
    }

    this.toFormField = function(target, attributeValue) {
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
		if(bdrs.mobile.attribute.type.STRING_WITH_VALID_VALUES == attribute.typeCode()) {
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
		    
		} else if(bdrs.mobile.attribute.type.INTEGER_WITH_RANGE == attribute.typeCode()) {
		
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
            
		} else if (bdrs.mobile.attribute.type.IMAGE == attribute.typeCode()) {
		
		    if (bdrs.mobile.cameraExists()) {
		        tmplName = tmplName+"-camera";
            } else {
                tmplName = tmplName+"-file";
            }
		}
		
		bdrs.template.renderCallback(tmplName, tmplParams, target, function() {
		    
		    if(bdrs.mobile.attribute.type.DATE == attribute.typeCode()) {
		        jQuery('#record-attr-'+attribute.id).datepicker();
	        }
	        
	        if (bdrs.mobile.attribute.type.IMAGE == attribute.typeCode()) {
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
		    bdrs.mobile.restyle(target);
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
        
        if (bdrs.mobile.attribute.type.IMAGE == attribute.typeCode()) {
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
