if(bdrs === undefined) {
    window.bdrs = {};
}

if(bdrs.review === undefined) {
    bdrs.review = {};
}

bdrs.review.downloadSightingsWidget = {};

bdrs.review.downloadSightingsWidget.FILE_DOWNLOAD_BUTTON_SELECTOR = "#download_button";
bdrs.review.downloadSightingsWidget.DOWNLOAD_FILE_FORMAT_SELECTOR = "input[name=download_format]";
bdrs.review.downloadSightingsWidget.KML_FILE_FORMAT = "kml";
bdrs.review.downloadSightingsWidget.SHP_FILE_FORMAT = "shp";
bdrs.review.downloadSightingsWidget.XLS_FILE_FORMAT = "xls";

bdrs.review.downloadSightingsWidget.init = function(searchCriteriaFormSelector, downloadUrl, fileFormatSelectionChangeCallback) {
	// Disable file downloading if no format is selected, and update the permalink
    jQuery(bdrs.review.downloadSightingsWidget.DOWNLOAD_FILE_FORMAT_SELECTOR).change(function(event) {
		if (fileFormatSelectionChangeCallback) {
			fileFormatSelectionChangeCallback();
		}
        var checked = jQuery(bdrs.review.downloadSightingsWidget.DOWNLOAD_FILE_FORMAT_SELECTOR).filter(":checked");
        jQuery(bdrs.review.downloadSightingsWidget.FILE_DOWNLOAD_BUTTON_SELECTOR).prop("disabled", checked.length === 0); 
    }).trigger("change");
        
    // Click handler for the file download tab
    jQuery(bdrs.review.downloadSightingsWidget.FILE_DOWNLOAD_BUTTON_SELECTOR).click(function(event) {
        var url = [
            bdrs.contextPath,
            downloadUrl+'?',
            jQuery(searchCriteriaFormSelector).serialize()
        ].join('');
        
        document.location = url;
    });
};
