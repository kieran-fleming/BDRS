//--------------------------------------
// Table Drag and Drop
//--------------------------------------

bdrs.dnd = {};

/**
 * Drop handler that is invoked when a row in the attribute table is reordered.
 * @param {element} table the table that contains the dropped row.
 * @param {element} row the row that was dropped.
 */
bdrs.dnd.tableDnDDropHandler = function(table, row) {
    var rows = table.tBodies[0].rows;
    for(var row_index=0; row_index<rows.length; row_index++) {
        var j_row = jQuery(rows[row_index]);
        j_row.find("input.sort_weight").val(row_index * 100);
    }
    
    jQuery(row).effect("highlight", {}, "normal");
};

/**
 * Attaches drag and drop event handling to the attribute table.
 */
bdrs.dnd.attachTableDnD = function(attributeTableSelector) {
    jQuery(attributeTableSelector).tableDnD({
        onDragClass: "drag_row",
        onDrop: bdrs.dnd.tableDnDDropHandler
    });
};

// row is a jquery dom node (or collection of nodes).
bdrs.dnd.addDnDRow = function(tableSelector, row) {
    var table = jQuery(tableSelector); 
    table.find("tbody").append(row);
    bdrs.dnd.attachTableDnD(tableSelector);
    bdrs.dnd.tableDnDDropHandler(table[0], row[0]); 
};