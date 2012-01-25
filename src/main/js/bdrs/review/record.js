if (bdrs === undefined) {
	bdrs = {};
}

if (bdrs.review === undefined) {
	bdrs.review = {};
}

bdrs.review.record = {};

bdrs.review.record.RECORD_INFO_DIALOG = '\
<div class="record_dialog_content" title="Record Info">\
    <div class="left"><a href="${ contextPath }/bdrs/user/surveyRenderRedirect.htm?surveyId=${ survey }&recordId=${ id }">Edit Record</a></div>\
    <div class="right"><a href="${ contextPath }/bdrs/user/contactRecordOwner.htm?recordId=${ id }">Contact Owner</a></div>\
    <div class="sep clear"></div>\
    <div><table class="form_table"><tbody></tbody></table></div>\
</div>\
';

bdrs.review.record.RECORD_INFO_DIALOG_CONTENT_ROW = '<tr><th>${ header }</th><td class="record_dialog_content_cell">${ _stringValue }{{html _htmlValue}}</td></tr>';
bdrs.review.record.DOWNLOAD_LINK = '<a href="${ url }">${ text }</a>';

/**
 * Opens a dialog containing the record information
 * 
 * @param {Object} record - complete record object
 */
bdrs.review.record.displayRecordInfo = function(record) {

    record.contextPath = bdrs.contextPath;
    
    var dialog_elem = jQuery.tmpl(bdrs.review.record.RECORD_INFO_DIALOG, record);
    var body = dialog_elem.find("tbody");
    var compiled_content_row = jQuery.template(bdrs.review.record.RECORD_INFO_DIALOG_CONTENT_ROW);
    var tmpl_params;
    
    // Create rows for core fields
    
    // ID
    body.append(jQuery.tmpl(compiled_content_row, {'header': 'Record ID', '_stringValue': record.id}));
    
    // When
    if(record.when !== null && record.when !== undefined) {
        tmpl_params = {'header': 'Date', '_stringValue': bdrs.util.formatDate(new Date(record.when))};
        body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
    }
    
    // Species
    if(record.species !== null && record.species !== undefined) {
        var species = record.species;
        if(species.commonName !== null && species.commonName !== undefined) {
            tmpl_params = {'header': 'Common Name', '_stringValue': species.commonName};
            body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
        }
        if(species.scientificName !== null && species.scientificName !== undefined) {
            var val = jQuery('<span></span>');
            val.addClass("scientificName");
            val.text(species.scientificName);
            tmpl_params = {'header': 'Scientific Name', '_htmlValue': jQuery("<span/>").append(val).html()};
            body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
        }
    }

    // Latitude
    if(record.latitude !== null && record.latitude !== undefined) {
        tmpl_params = {'header': 'Latitude', '_stringValue': record.latitude};
        body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
    }
    
    // Longitude
    if(record.longitude !== null && record.longitude !== undefined) {
        tmpl_params = {'header': 'Longitude', '_stringValue': record.longitude};
        body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
    }
    
    // Number
    if(record.number !== null && record.number !== undefined) {
        tmpl_params = {'header': 'Number', '_stringValue': record.number};
        body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
    }
    
    // Notes
    if(record.notes !== null && record.notes !== undefined) {
        tmpl_params = {'header': 'Notes', '_stringValue': record.notes};
        body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
    }

    // Create rows for each attribute
    var attr_val;
    var attr;
    var attr_type;
    
    for(var i=0; i<record.attributes.length; i++) {
        attr_val = record.attributes[i];
        tmpl_params = {
            'header': attr_val.attribute.description
        };
        // Not displaying empty attribute values. Is that desired?
        if(attr_val.stringValue.trim().length > 0) {
            attr = attr_val.attribute;
            attr_type = bdrs.model.taxa.attributeType.code[attr.typeCode];
            
            // If this is a file attribute, create a link.
            if(attr_type.isFileType()) {
                // The span is here because .html will drop the root elem.
                tmpl_params._htmlValue = jQuery("<span/>").append(jQuery.tmpl(bdrs.review.record.DOWNLOAD_LINK, {
                    'text': attr_val.stringValue.trim(), 
                    'url': [bdrs.contextPath, bdrs.url.FILE_DOWNLOAD, '?', attr_val.fileURL].join('')
    
                })).html();
            } else {
                tmpl_params._stringValue = attr_val.stringValue.trim();
            }
            
            // Append the rendered content         
            body.append(jQuery.tmpl(compiled_content_row, tmpl_params));
        }
    }
    
    // Resize and center the dialog inside the body.
    // By default, jquery-ui does not provide scroll bars and will
    // create a very large dialog to fit the content. This code caps the
    // dialog size and re-centers the dialog. 
    var maxHeight = 0.85 * jQuery(document).height();
    var d = dialog_elem.dialog({
        "resize": "auto",
        "maxHeight": maxHeight,
        "zIndex": 1040
    });

    var height = (d.height() + (d.dialog("widget").height() - d.height()));
    height = Math.min(maxHeight, height);
    
    var width = Math.max(body.width(), d.width());
    // don't forget the dialog's content padding. (x2 for the left and right)
    width += 2 * (d.outerWidth() - d.width());
    width = Math.min(width, jQuery(".wrapper").width());
    
    // Apply new width & height. Re-centers the resized dialog.
    d.dialog("option", {"height": height, "width": width});
    d.dialog("option", {"position": "center"}); 
};
