//--------------------------------------
// Attributes
//--------------------------------------

bdrs.attribute.addAttributeCount = 1;

// map for option tool tips
bdrs.attribute.OPTION_TOOLTIP = new Hashtable();

// Note not all attribute types have a tooltip for their options.
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.INTEGER_WITH_RANGE, "Enter two numbers, separated by a comma");
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.BARCODE, "This is designed for use on mobile devices");
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.REGEX, "Enter a Java regular expression");
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.HTML, "Enter valid HTML into this option field to have it display on the form");
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.STRING_WITH_VALID_VALUES, "Enter your choices, separated by a comma");
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.MULTI_CHECKBOX, "Enter your choices, separated by a comma");
bdrs.attribute.OPTION_TOOLTIP.put(bdrs.model.taxa.attributeType.MULTI_SELECT, "Enter your choices, separated by a comma");

// map for validation ketchup class.
bdrs.attribute.VALIDATION_CLASS = new Hashtable();
bdrs.attribute.VALIDATION_CLASS.put(bdrs.model.taxa.attributeType.INTEGER_WITH_RANGE, "validate(attrOptionIntWithRange, required)");
bdrs.attribute.VALIDATION_CLASS.put(bdrs.model.taxa.attributeType.STRING_WITH_VALID_VALUES, "validate(required)");
bdrs.attribute.VALIDATION_CLASS.put(bdrs.model.taxa.attributeType.MULTI_CHECKBOX, "validate(attrOptionCommaSeparated, required)");
bdrs.attribute.VALIDATION_CLASS.put(bdrs.model.taxa.attributeType.MULTI_SELECT, "validate(attrOptionCommaSeparated, required)");
bdrs.attribute.VALIDATION_CLASS.put(bdrs.model.taxa.attributeType.STRING_WITH_VALID_VALUES, "validate(attrOptionCommaSeparated, required)");

/**
 * Adds a row to the attribute table.
 */
bdrs.attribute.addAttributeRow = function(tableSelector, showScope, isTag) {
    var index = bdrs.attribute.addAttributeCount++;

    jQuery.get(bdrs.contextPath+'/bdrs/admin/attribute/ajaxAddAttribute.htm',
            {'index': index, 'showScope': showScope, 'isTag': isTag}, function(data) {

        var table = jQuery(tableSelector); 
        var row = jQuery(data);
        table.find("tbody").append(row);
        bdrs.dnd.attachTableDnD(tableSelector);
        bdrs.dnd.tableDnDDropHandler(table[0], row[0]); 
        jQuery('form').ketchup();
        
        bdrs.attribute.setAttributeWeights(tableSelector);
    });
};

/**
 * Sets the attribute weights from 100 upwards in increments of 100
 * 
 * @param {Object} tableSelector - the selector for the form table
 */
bdrs.attribute.setAttributeWeights = function(tableSelector) {
    // this code relies on the fact that jQuery returns nodes in document order.
    // jQuery does this as of 1.3.2, http://docs.jquery.com/Release%3AjQuery_1.3.2
    var weight = 100;
    jQuery(tableSelector).find(".sort_weight").each(function(index, element) {
        jQuery(element).val(weight);
        weight += 100;
    });
};

bdrs.attribute.rowTypeChanged = function(event) {
    var index = event.data.index;
    var bNewRow = event.data.bNewRow;
    
    var prefix = bNewRow ? 'add_' : '';
    var newTypeCode = jQuery("[name=" + prefix + "typeCode_" + index + "]").val();
    var attrType = bdrs.model.taxa.attributeType.code[newTypeCode];
    var tooltip = bdrs.attribute.OPTION_TOOLTIP.get(attrType);
    var validation = bdrs.attribute.VALIDATION_CLASS.get(attrType);

    bdrs.attribute.enableInput(
        (bdrs.model.taxa.attributeType.STRING_WITH_VALID_VALUES.code === newTypeCode) ||
        (bdrs.model.taxa.attributeType.INTEGER_WITH_RANGE.code  === newTypeCode) ||
        (bdrs.model.taxa.attributeType.BARCODE.code  === newTypeCode) ||
        (bdrs.model.taxa.attributeType.REGEX.code  === newTypeCode) ||
        (bdrs.model.taxa.attributeType.HTML.code  === newTypeCode) ||
        (bdrs.model.taxa.attributeType.MULTI_CHECKBOX.code  === newTypeCode) ||
        (bdrs.model.taxa.attributeType.MULTI_SELECT.code  === newTypeCode),
        '[name=' + prefix + 'option_'+index+']', tooltip, validation); 
        

    var requiredSelector = '[name=' + prefix  +'required_'+index+']';
    if(bdrs.model.taxa.attributeType.SINGLE_CHECKBOX.code  === newTypeCode ||
        bdrs.model.taxa.attributeType.HTML.code  === newTypeCode ||
        bdrs.model.taxa.attributeType.HTML_COMMENT.code  === newTypeCode ||
        bdrs.model.taxa.attributeType.HTML_HORIZONTAL_RULE.code  === newTypeCode) {
        jQuery(requiredSelector).attr('checked',false);
        jQuery(requiredSelector).attr('disabled','disabled');
    } else { 
        jQuery(requiredSelector).removeAttr('disabled'); 
    }
    
    var descriptionSelector = '[name=' + prefix + 'description_'+index+']';
    if(bdrs.model.taxa.attributeType.HTML_HORIZONTAL_RULE.code  === newTypeCode) { 
        jQuery(descriptionSelector).val('');
        jQuery(descriptionSelector).attr('disabled','disabled');
    } else { 
        jQuery(descriptionSelector).removeAttr('disabled');
    }
    if(bdrs.model.taxa.attributeType.HTML.code  === newTypeCode) { 
        jQuery(descriptionSelector).attr('onfocus','bdrs.attribute.showHtmlEditor(jQuery(\'#htmlEditorDialog\'), jQuery(\'#markItUp\')[0], this)');
    } else { 
        jQuery(descriptionSelector).removeAttr('onfocus');
    }
    
    // name in database
    var nameSelector = '[name=' + prefix + 'name_'+index+']';
    if (bdrs.model.taxa.attributeType.HTML_HORIZONTAL_RULE.code === newTypeCode) {
        jQuery(nameSelector).val("");
        bdrs.attribute.enableInput(false, nameSelector, "", null);
    } else {
        bdrs.attribute.enableInput(true, nameSelector, "The name used to store this attribute in the database", "validate(uniqueAndRequired(.uniqueName))");
    }
}

/**
 * Returns a function to be attached to the onchanged event of the field type
 * input.
 * 
 * @param {Object} index the row - used to give each row a unique
 * identity.
 * @param {Object} bNewRow signals whether this row is a new row or an existing row.
 * @return a function to be triggered by the field type select control onchange.
 */
bdrs.attribute.getRowTypeChangedFunc = function(index, bNewRow) {
    return bdrs.attribute.rowTypeChanged;
};

bdrs.attribute.validateClassRegex = /validate\([\w,\s]+\)/;

/**
 * Enables or disables the element(s) specified by the selector.
 * @param enableOption the enabled/disabled state
 * @param inputSelector jQuery selector for elements to modify.
 * @param tooltip text to apply to the title attribute for the input.
 * @param validationClass ketchup validation class to add to the class attribute for the input.
 */
bdrs.attribute.enableInput = function(enableOption, inputSelector, tooltip, validationClass) {
    var elem = jQuery(inputSelector);
    
    //var oldClass = elem.attr("class");
    // we always want to remove the current validation so...
    //var newClass = oldClass ? elem.attr("class").replace(bdrs.attribute.validateClassRegex, "") : null;
    var newClass = bdrs.attribute.removeValidationClass(elem.attr("class"));
    elem.attr("class", newClass);

    if(enableOption) {
        elem.removeAttr("disabled");
        elem.attr("title", tooltip);
        elem.addClass(validationClass);
    } else {
        // clear the options before disabling
        elem.val('');
        elem.attr("disabled", "disabled");
        elem.attr("title", null);
        elem.removeClass("hasKetchup");
    }
    
    // rebind ketchup
    elem.parents('form').ketchup();
};


bdrs.attribute.KETCHUP_VALIDATE_CLASS_PREFIX = "validate(";

/**
 * @param classStr the contents of the class attribute
 * @return the class string minus the validation class if it exists. null otherwise. 
 */
bdrs.attribute.removeValidationClass = function(classStr) {
    if (!classStr) {
        return null;
    }
    var startIdx = classStr.indexOf(bdrs.attribute.KETCHUP_VALIDATE_CLASS_PREFIX);
    if (startIdx > 0) {
       var currentIdx = startIdx + bdrs.attribute.KETCHUP_VALIDATE_CLASS_PREFIX.length;
       var bracketCount = 1;
       
       var currentChar;
       while (currentIdx < classStr.length - 1) {
              
           currentChar = classStr.charAt(currentIdx);
           if (currentChar === "(") {
              ++bracketCount;
           } else if (currentChar === ")") {
                 --bracketCount;
           }
           if (bracketCount === 0) {
              break;
           }
           ++currentIdx; 
       }
       
       if (bracketCount !== 0) {
           // error, brackets not matched.
           return null;    
       }
       // else brackets are closed properly...
       var validateClassString = classStr.substr(startIdx, currentIdx - startIdx + 1);
       var result = classStr.replace(validateClassString, "");
       return result;
       
    } else {
        return null;
    }
};

bdrs.attribute.htmlInput = "";

bdrs.attribute.saveAndUpdateContent = function(textEditor) {
    bdrs.attribute.htmlInput.value = textEditor.value;
};

/**
 * Shows a popup dialog with an HTML editor to allow the user to edit an HTML
 * attribute more easily.
 * @param popup the popup dialog on the page that you want to interact with
 * @param input the input that originated the dialog
 */
bdrs.attribute.showHtmlEditor = function(popup, textEditor, input) {
    textEditor.value = input.value;
    bdrs.attribute.htmlInput = input;
    popup.dialog('open');
};

bdrs.attribute.closeHtmlEditor = function(popup) {
    popup.dialog('close');
};

bdrs.attribute.createAttributeDisplayDiv = function(attributes, attributeSelector) {
    if (!attributeSelector) {
        return;
    }
    var attDiv = jQuery(attributeSelector);
    if (!attDiv) {
        return;
    }
    // remove any existing attributes
    jQuery(".attributeElement").remove();
    
    if (!attributes) {
        return;
    }
    
    var i;
    for (i = 0; i < attributes.length; i++) {
        var att = attributes[i];
        var attElem = jQuery('<div class="attributeElement" ></div>');
        var attDescElem = jQuery('<div class="attributeDescription" >' + 
                att.attribute.description + '</div>');
        var attValueElem = jQuery('<div class="attributeValue" >' + 
                att.stringValue + '</div>');
        attElem.append(attDescElem);
        attElem.append(attValueElem);
        attDiv.append(attElem);
    }
};
