exports.DELETE_CONFIRM_BUTTON_SELECTOR = "#trash-delete-confirm-button";

exports.Create = function() {
    jQuery(exports.DELETE_CONFIRM_BUTTON_SELECTOR).click(function() {
        
        var page = jQuery('#trash-delete-confirm');
        page.one("pagehide", function() {
            bdrs.mobile.pages.trash.delete_records_confirmed();
        });
        page.dialog('close');
       
    });
};
    
exports.Show = function() {
};

exports.Hide = function() {
};
