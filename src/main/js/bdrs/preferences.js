//--------------------------------------
// Preferences
//--------------------------------------

bdrs.preferences = {};
bdrs.preferences.addPreferenceRow = function(categoryId, indexSelector, tableSelector) {
    var indexElem = jQuery(indexSelector);
    var index = parseInt(indexElem.val(),10);
    indexElem.val(index+1);
    
    var params = {
        categoryId: categoryId,
        index: index
    };
    jQuery.get(bdrs.contextPath+"/bdrs/admin/preference/ajaxAddPreferenceRow.htm", params, function(data) {
        jQuery(tableSelector).find("tbody").append(data);
        jQuery('form').ketchup();
    });
};

// Preferences -------------------------
// -------------------------------------