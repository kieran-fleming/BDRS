/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
    // Server Polling
    var enablePollingInput = jQuery("#sync-settings-enable-polling");
    enablePollingInput.attr("checked", bdrs.mobile.connectivity.isPollingEnabled());
    bdrs.mobile.restyle(enablePollingInput.parent());
    enablePollingInput.change(bdrs.mobile.pages.sync_settings._pollingActionChanged);

    // Polling Period
    var periodInput = jQuery("#sync-settings-polling-period");
    periodInput.val(bdrs.mobile.connectivity.getPollingPeriod()/1000);
    periodInput.change(bdrs.mobile.pages.sync_settings._pollingPeriodChanged);
     
    // Manual Synchronization
    jQuery("#sync-settings-sync-now").click(function() {
        bdrs.mobile.syncService.synchronize();
    });
};
	
exports.Show = function() {
};
	
exports.Hide = function() {
};

exports._pollingPeriodChanged = function(event) {
    var periodInput = jQuery("#sync-settings-polling-period");
    var period = parseInt(periodInput.val(), 10);
    if(period === NaN || period === null || period === undefined) {
        period = bdrs.mobile.connectivity.getPollingPeriod()/1000;
    } else {
        if(period < 1) {
            period = bdrs.mobile.connectivity.getPollingPeriod()/1000;
        } else {
            // convert seconds to milliseconds
            bdrs.mobile.connectivity.setPollingPeriod(period * 1000);
        }
    }
    
    // Set the value in case the inputted value was not a valid number.
    periodInput.val(period);
};

exports._pollingActionChanged = function(event) {
    var isPollingEnabled = jQuery("#sync-settings-enable-polling:checked").length > 0;
    bdrs.mobile.connectivity.setPollingEnabled(isPollingEnabled);
};
