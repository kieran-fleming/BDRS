bdrs.bulkdata = {};

/**
 * Initialise the bulk data page.
 */
bdrs.bulkdata.init = function() {
	// alot of constants in this function
	
	// Download Template Expand/Collapse
    jQuery("#downloadTemplateToggle").click(function() {
        jQuery("#downloadTemplateWrapper").slideToggle(function() {
            var canSee = jQuery("#downloadTemplateWrapper").css('display') === 'none';
            jQuery("#downloadTemplateToggle").text(canSee ? "Expand" : "Collapse");
        });
    });

    // Upload Spreadsheet Expand/Collapse
    jQuery("#uploadSpreadsheetToggle").click(function() {
        jQuery("#uploadSpreadsheetWrapper").slideToggle(function() {
            var canSee = jQuery("#uploadSpreadsheetWrapper").css('display') === 'none';
            jQuery("#uploadSpreadsheetToggle").text(canSee ? "Expand" : "Collapse");
        });
    });
    
    jQuery("input[name=bulkDataUploadType]").change(function(){
        var value = jQuery("input[@name='option_layout']:checked").val();
        if (value == "spreadsheet") {
            jQuery("#spreadsheetSection").show();
            jQuery("#shapefileSection").hide();
        } else if (value == "shapefile") {
            jQuery("#spreadsheetSection").hide();
            jQuery("#shapefileSection").show();
        } else {
            throw 'unexpected value on change event: ' + value;
        }
    });
    
    var createCensusMethodOptionItems = function(data) {
        var selectNode = jQuery("#censusMethodShapefileTemplateSelect");
        selectNode.empty();
        for(var j=0; j<data.length; ++j) {
            var censusMethod = data[j];
            var cmOption = jQuery("<option>" + censusMethod.name + "</option>");
            cmOption.attr("value", censusMethod.id);
            selectNode.append(cmOption);
        }
    }
    
    jQuery("#surveyShapefileTemplateSelect").change(function() {
        var surveyId = jQuery("#surveyShapefileTemplateSelect").val();
        // populate census method items...
        jQuery.getJSON(bdrs.contextPath + "/bdrs/user/censusMethod/getSurveyCensusMethods.htm", {surveyId:surveyId}, createCensusMethodOptionItems);
    });
    
    // trigger change events to initialise UI
    jQuery("#bulkDataUploadType").change();
    jQuery("#surveyShapefileTemplateSelect").change();
    
    jQuery("#ssCsvChecklist").click(bdrs.bulkdata.getChecklistFunc("csv", '#surveyTemplateSelect'));
    jQuery("#ssZipChecklist").click(bdrs.bulkdata.getChecklistFunc("zip", '#surveyTemplateSelect'));
    jQuery("#shpCsvChecklist").click(bdrs.bulkdata.getChecklistFunc("csv", '#surveyShapefileTemplateSelect'));
    jQuery("#shpZipChecklist").click(bdrs.bulkdata.getChecklistFunc("zip", '#surveyShapefileTemplateSelect'));

    jQuery("#downloadTemplateForm").submit(bdrs.bulkdata.getSubmitFunc("#surveyTemplateSelect"));
    jQuery("#downloadShapefileTemplateForm").submit(bdrs.bulkdata.getSubmitFunc("#surveyShapefileTemplateSelect"));
	jQuery("#uploadShapefileForm").submit(bdrs.bulkdata.getShapefileUploadSubmitFunc("#shapefileInput"));
};

/**
 * func factory for downloading species checklist
 * 
 * @param {Object} format the format of the checklist. csv or zip
 * @param {Object} surveySelector the selector that returns the survey input
 */
bdrs.bulkdata.getChecklistFunc = function(format, surveySelector) {
    return bdrs.bulkdata.getWrapperSubmitFunc(function() {
		var val = jQuery(surveySelector).val();
        if (!val) {
            alert("Please select a project to download the checklist.");
            return false;
        }
        window.document.location=bdrs.contextPath+'/webservice/survey/checklist.htm?format='+format+'&surveyId='+val; 
    }, true);
};

/**
 * func factory for downloading templates
 * 
 * @param {Object} surveySelector the selector that returns the survey input
 */
bdrs.bulkdata.getSubmitFunc = function(surveySelector) {
    return bdrs.bulkdata.getWrapperSubmitFunc(function() {
        if (!jQuery(surveySelector).val()) {
            alert("Please select a project to download the template.");
            // dont submit the form
            return false;
        }
        // we are ok, submit the form
        return true;
    }, true);
};

/**
 * func factory for uploading spreadsheet templates
 * 
 * @param {Object} surveySelector selector that returns the survey input
 * @param {Object} fileSelector selector that returns the file input
 */
bdrs.bulkdata.getSpreadsheetUploadSubmitFunc = function(surveySelector, fileSelector) {
	return bdrs.bulkdata.getWrapperSubmitFunc(function() {
		if (!jQuery(surveySelector).val()) {
            alert("Please select a project to upload a template.");
            // dont submit the form
            return false;
        }
        if (!jQuery(fileSelector).val()) {
            alert("Please select a file to upload.");
            return false;
        }
        // we are ok, submit the form
        return true;
	}, false);
};

/**
 * func factory for uploading shapefile templates
 * 
 * @param {Object} fileSelector selector that returns the file input
 */
bdrs.bulkdata.getShapefileUploadSubmitFunc = function(fileSelector) {
	return bdrs.bulkdata.getWrapperSubmitFunc(function() {
		if (!jQuery(fileSelector).val()) {
            alert("Please select a file to upload.");
            return false;
        }
        // we are ok, submit the form
        return true;
	}, false);
};

/**
 * Submit wrapper func that always re-enables the submit buttons.
 * 
 * use enableInputOnSubmit = true for GET forms.
 * use enableInputOnSubmit = false for POST forms.
 * 
 * @param {Object} handlerFunc - function to handle the submit event
 * @param {boolean} alwaysEnableInputOnSubmit - if true, always enables the submit input
 * on successful form submission
 */
bdrs.bulkdata.getWrapperSubmitFunc = function(handlerFunc, enableInputOnSubmit) {
	return function() {
		var handlerResult = handlerFunc();
		if (enableInputOnSubmit) {
			bdrs.unbindDisableHandler(this);
		} else {
			// only enable the input if the form wasn't submitted
			if (!handlerResult) {
				bdrs.unbindDisableHandler(this);
			}
		}
		return handlerResult;
	};
};

