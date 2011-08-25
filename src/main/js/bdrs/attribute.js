//--------------------------------------
// Attributes
//--------------------------------------

bdrs.attribute = {};

bdrs.attribute.addAttributeCount = 1;

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
    });
};

/**
 * Enables or disables the element(s) specified by the selector.
 * @param enableOption the enabled/disabled state
 * @param optionSelector jQuery selector for elements to modify.
 */
bdrs.attribute.enableOptionInput = function(enableOption, optionSelector) {
    var elem = jQuery(optionSelector);
    if(enableOption) {
        elem.removeAttr("disabled");
    } else {
        // clear the options before disabling
        elem.val('');
        elem.attr("disabled", "disabled");
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
}

bdrs.attribute.closeHtmlEditor = function(popup) {
    popup.dialog('close');
};

