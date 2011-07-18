/**
 * Sanity check 
 */
if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {}
}

bdrs.mobile.User = undefined; // The currently logged in user.

bdrs.mobile.CurrentSurvey = undefined; // The currently chosen survey.

bdrs.mobile.args = {};

bdrs.mobile.setParameter = function(key, value) {
	bdrs.mobile.args[key] = value;
	bdrs.mobile.Debug('Setting parameter : ' + key + ' to ' + value); 
}

bdrs.mobile.getParameter = function(key) {
	return bdrs.mobile.args[key];
}

bdrs.mobile.removeParameter = function(key) {
    var val = bdrs.mobile.getParameter(key);
    delete bdrs.mobile.args[key];
    
    bdrs.mobile.Debug('Removing parameter : ' + key);
    return val;
}