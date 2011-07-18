/**7
 * Sanity Checks
 */
if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {};
}

bdrs.mobile.log_level = 4;
bdrs.mobile.remote = true;

bdrs.mobile.getLogURL = function() {
    if(bdrs.mobile.User === null || bdrs.mobile.User === undefined) {
        return null;
    } else {
        return bdrs.mobile.User.server_url() + "/webservice/user/log.htm";
    }
}

bdrs.mobile.remoteLog = function(level, msg) {
    if(!bdrs.mobile.remote) {
        return;
    }
    
    var ident;
    if(bdrs.mobile.User !== null && bdrs.mobile.User !== undefined) {
         ident = bdrs.mobile.User.ident()
    } else {
        ident = null;
    }
    
    var logURL = bdrs.mobile.getLogURL();
    if(ident !== null && logURL !== null) {
        jQuery.jsonp({
            url: logURL,
            data: { message : msg, 
                    level : level, 
                    ident : ident },
            type: 'GET',
            callbackParameter: 'callback'
        });
    }
}

bdrs.mobile.Debug = function(msg) {
	if (bdrs.mobile.log_level > 3) {
		console.log('DEBUG : ' + msg);
		bdrs.mobile.remoteLog('DEBUG', msg);
	}
}

bdrs.mobile.Dump = function(obj, name) {
	bdrs.mobile.Debug("Dumping " + name);
	for (property in obj) {
		bdrs.mobile.Debug(property + " : " + obj[property]);
	}
}

bdrs.mobile.Info = function(msg) {
	if (bdrs.mobile.log_level > 2) {
		console.log('INFO  : ' + msg);
		bdrs.mobile.remoteLog('INFO', msg);
	}
}

bdrs.mobile.Warn = function(msg) {
	if (bdrs.mobile.log_level > 1) {
		console.log('WARN  : ' + msg);
		bdrs.mobile.remoteLog('WARN', msg);
	}
}

bdrs.mobile.Error = function(msg) {
	if (bdrs.mobile.log_level > 0) {
		console.log('ERROR : ' + msg);
		bdrs.mobile.remoteLog('ERROR', msg);
	}
}