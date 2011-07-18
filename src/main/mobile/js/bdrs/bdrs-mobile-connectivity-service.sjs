exports.SERVER_POLLING_ENABLED_KEY = 'server-polling-enabled';
exports.DEFAULT_SERVER_POLLING_ENABLED = true;
exports.SERVER_POLLING_PERIOD_KEY = 'server-polling-period';
exports.DEFAULT_SERVER_POLLING_PERIOD = 60000;

exports.PING_TIMEOUT = 5000;

exports._pollingEnabled = null;
exports._pollingPeriod = null;

exports._pollingTimer = null;

exports._connectivityListeners = [];

exports.init = function() {
    bdrs.mobile.connectivity.startTimer();
    bdrs.mobile.Debug("Connectivity Service Started");
};

exports.startTimer = function() {

    if(bdrs.mobile.connectivity.isPollingEnabled()) {
        bdrs.mobile.connectivity._pollingTimer = 
            setTimeout(function(){bdrs.mobile.connectivity.timeoutTriggered();}, 
                        bdrs.mobile.connectivity.getPollingPeriod());
    }

    //    jQuery.ajax("http://localhost:8080/BDRS/webservice/application/ping.htm", {dataType:'jsonp', success:function(){console.log("success");}, error: function(a,b,c){console.log("error"); console.log(c)}, timeout:5000});
};

exports.timeoutTriggered = function() {
    var url = bdrs.mobile.User.server_url() + "/webservice/application/ping.htm";
    var jqXHR
    var textStatus;
    waitfor(jqXHR, textStatus) {
        jQuery.ajax(url, {
            dataType: 'jsonp',
            timeout: bdrs.mobile.connectivity.PING_TIMEOUT, 
            complete: resume
        });        
    }
    
    bdrs.mobile.connectivity._fireConnectivityEvent({
        jqXHR: jqXHR,
        textStatus: textStatus
    });
    
    bdrs.mobile.connectivity.startTimer();
};

/**
 * Enables or disables server connectivity polling.
 * @param isEnabled true if server polling should be activated, false otherwise.
 */
exports.setPollingEnabled = function(isEnabled) { 
    bdrs.mobile.connectivity._pollingEnabled = Boolean(isEnabled);
    
    var pollingEnabledSetting;
    waitfor(pollingEnabledSetting) {
        Settings.findBy('key', 'server-polling-enabled', resume);
    };
    
    if(pollingEnabledSetting === null) {
        // No existing value in the database.
        pollingEnabledSetting = new Settings({
            key : 'server-polling-enabled', 
            value : String(bdrs.mobile.connectivity._pollingEnabled)
        });
        persistence.add(pollingEnabledSetting);
    } else {
        pollingEnabledSetting.value(String(bdrs.mobile.connectivity._pollingEnabled));
    }
    persistence.flush();

    bdrs.mobile.Debug("Polling Enabled: "+bdrs.mobile.connectivity._pollingEnabled);
    bdrs.mobile.connectivity.startTimer();    
};

/**
 * @return true if server connectivity polling is enabled, false otherwise.
 */
exports.isPollingEnabled = function() {

    if(bdrs.mobile.connectivity._pollingEnabled === null) {
        var pollingEnabledSetting;
        waitfor(pollingEnabledSetting) {
            Settings.findBy('key', 'server-polling-enabled', resume);
        };
        
        if(pollingEnabledSetting === null) {
            // No existing value in the database. Set the default.
            bdrs.mobile.connectivity._pollingEnabled = 
                bdrs.mobile.connectivity.DEFAULT_SERVER_POLLING_ENABLED;
            pollingEnabledSetting = new Settings({
                key : 'server-polling-enabled', 
                value : String(bdrs.mobile.connectivity._pollingEnabled)
            });
            persistence.add(pollingEnabledSetting);
            persistence.flush();
        } else {
            // Load value from the database
            bdrs.mobile.connectivity._pollingEnabled = pollingEnabledSetting.value() === "true";
        }
    }
    
    return bdrs.mobile.connectivity._pollingEnabled;
};

/**
 * @return the period (in seconds) between each attempt to test connectivity
 * to the server.
 */
exports.getPollingPeriod = function() {
    if(bdrs.mobile.connectivity._pollingPeriod === null) {
        var pollingPeriodSetting;
        waitfor(pollingPeriodSetting) {
            Settings.findBy('key', 'server-polling-period', resume);
        };
        
        if(pollingPeriodSetting === null) {
            // No existing value in the database. Set the default.
            bdrs.mobile.connectivity._pollingPeriod = 
                bdrs.mobile.connectivity.DEFAULT_SERVER_POLLING_PERIOD;
            pollingPeriodSetting = new Settings({
                key : 'server-polling-period', 
                value : String(bdrs.mobile.connectivity._pollingPeriod)
            });
            persistence.add(pollingPeriodSetting);
            persistence.flush();
        } else {
            // Load value from the database
            bdrs.mobile.connectivity._pollingPeriod = parseInt(pollingPeriodSetting.value(), 10);
        }
    }
    
    return bdrs.mobile.connectivity._pollingPeriod;
};

exports.setPollingPeriod = function(period) {
    var periodInt = parseInt(period, 10);
    if(periodInt === NaN || periodInt < 1) {
        // Something bad happened
        bdrs.mobile.Error("Invalid period value: "+period);
        return;
    }
    
    bdrs.mobile.connectivity._pollingPeriod = periodInt;

    var pollingPeriodSetting;
    waitfor(pollingPeriodSetting) {
        Settings.findBy('key', 'server-polling-period', resume);
    };
    
    if(pollingPeriodSetting === null) {
        // No existing value in the database.
        pollingPeriodSetting = new Settings({
            key : 'server-polling-period', 
            value : String(bdrs.mobile.connectivity._pollingPeriod)
        });
        persistence.add(pollingPeriodSetting);
    } else {
        pollingPeriodSetting.value(String(bdrs.mobile.connectivity._pollingPeriod));
    }
    
    persistence.flush();
    bdrs.mobile.Debug("Polling Period Updated: "+bdrs.mobile.connectivity._pollingPeriod);
    
    clearTimeout(bdrs.mobile.connectivity._pollingTimer);
    bdrs.mobile.connectivity.startTimer();
};

/** 
 * Adds a connectivity listener to this service.
 * @param callback the listener to be added.
 */
exports.addConnectivityListener = function(callback) {
    bdrs.mobile.connectivity._connectivityListeners.push(callback);
};

/**
 * Removes a connectivity listener from this service.
 * @param callback the listener to be removed.
 * @return true if the listener was removed successfully, false otherwise.
 */
exports.removeSyncListener = function(callback) {
    for(var i=0; i<bdrs.mobile.connectivity._connectivityListeners.length; i++) {
        var listener = bdrs.mobile.connectivity._connectivityListeners[i];
        if(callback === listener) {
            bdrs.mobile.connectivity._connectivityListeners.splice(i,1);
            return true;
        }
    }
    
    return false;
};

exports._fireConnectivityEvent = function(event) {
    var copy = bdrs.mobile.connectivity._connectivityListeners.slice();
    for(var i=0; i<copy.length; i++) {
        copy[i](event);
    }
};
