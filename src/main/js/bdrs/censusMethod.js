bdrs.censusMethod = {};
// id is the id of the census method to add...
bdrs.censusMethod.addCensusMethodRow = function(tableSelector, id) {
    jQuery.get(bdrs.contextPath+'/bdrs/admin/censusMethod/ajaxAddSubCensusMethod.htm',
            {'id': id}, function(data) {
        var table = jQuery(tableSelector); 
        var row = jQuery(data);
        table.find("tbody").append(row);
        bdrs.dnd.attachTableDnD(tableSelector);
        bdrs.dnd.tableDnDDropHandler(table[0], row[0]); 
    });
};
