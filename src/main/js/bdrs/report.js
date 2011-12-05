if(bdrs === undefined) {
    window.bdrs = {};
}

if(bdrs.report === undefined) {
    bdrs.report = {};
}

bdrs.report.listing = {};

bdrs.report.listing.ADD_REPORT_BUTTON_SELECTOR = '#add_report_button';
bdrs.report.listing.ADD_REPORT_FILE_SELECTOR = '#add_report_file';

bdrs.report.listing.DELETE_REPORT_ACTION_SELECTOR = ".delete_report";

bdrs.report.listing.init = function() {
    
    // Submits the add report form when the file selection changes.
    jQuery(bdrs.report.listing.ADD_REPORT_FILE_SELECTOR).change(function(event) {
        var file_elem = jQuery(event.currentTarget);
        file_elem.parents('form').trigger('submit');
    });
    // Displays a file selection dialog when the button is clicked.
    jQuery(bdrs.report.listing.ADD_REPORT_BUTTON_SELECTOR).click(function() {
        jQuery(bdrs.report.listing.ADD_REPORT_FILE_SELECTOR).trigger('click');
    });
    
    // Performs a form submission when the admin clicks the delete button
    jQuery(bdrs.report.listing.DELETE_REPORT_ACTION_SELECTOR).click(function(event) {
        var confirm_delete = confirm("Are you sure you wish to delete this report?");
        if(confirm_delete) {
            jQuery(event.currentTarget).parents("form").submit();
        }
    });          
};