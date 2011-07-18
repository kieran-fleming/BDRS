/**
 * Event handlers for the logging page. 
 */
exports.Create =  function() {
    
    jQuery("#logLevel").change(function(event){ 
    
        var log_level = parseInt(jQuery("#logLevel").val(), 10);
        var log_level_name;
        switch(log_level) {
            case 0:
                log_level_name = "Off";
                break;
            case 1:
                log_level_name = "Error";
                break;
            case 2:
                log_level_name = "Warn";
                break;
            case 3:
                log_level_name = "Info";
                break;
            case 4:
                log_level_name = "Debug";
                break;
            default:
                log_level_name = "Unknown";
        }
         
        switch(bdrs.mobile.log_level) {
            case 0:
                // can't log. logging off.
                break;
            case 1:
                bdrs.mobile.Error("Logging level changed to "+log_level_name);
                break;
            case 2:
                bdrs.mobile.Warn("Logging level changed to "+log_level_name);
                break;
            case 3:
                bdrs.mobile.Info("Logging level changed to "+log_level_name);
                break;
            case 4:
                bdrs.mobile.Debug("Logging level changed to "+log_level_name);
                break;
            default:
                bdrs.mobile.Error("Logging level changed to "+log_level_name);
                break;
        }
        
        bdrs.mobile.log_level = log_level;
    });

    jQuery("#remoteLogging").change(function(event){ 
        bdrs.mobile.remote = jQuery("#remoteLogging:checked").length > 0;
        if(bdrs.mobile.remote) {
            bdrs.mobile.Debug("Remote Logging Enabled");
        } else {
            bdrs.mobile.Debug("Remote Logging Disabled");
        }
    });
}
	
exports.Show = function() {
    var logLevel = jQuery("#logLevel");
    logLevel[0].selectedIndex = bdrs.mobile.log_level;
    logLevel.selectmenu("refresh");
    
    var remoteLogging = jQuery("#remoteLogging");
    if(bdrs.mobile.remote) { 
        remoteLogging.attr("checked","checked");
    } else {
        remoteLogging.removeAttr("checked");
    } 
    remoteLogging.checkboxradio("refresh");
}
	
exports.Hide = function() {
}