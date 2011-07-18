if (!window.bdrs) {
	bdrs = {};
}
if (!bdrs.mobile) {
	bdrs.mobile = {};
}
bdrs.mobile.cookie = {};

bdrs.mobile.cookie.create = function(name, value, days){
	if (days) {
		var date = new Date();
		date.setTime(date.getTime()+(days*24*60*60*1000));
		var expires = "; expires="+date.toGMTString();
	}
	else var expires = "";
	document.cookie = name+"="+value+expires+"; path=/";
}

bdrs.mobile.cookie.read = function(name) {
	var nameEQ = name + "=";
	var ca = document.cookie.split(';');
	for(var i=0;i < ca.length;i++) {
		var c = ca[i];
		while (c.charAt(0)==' ') c = c.substring(1,c.length);
		if (c.indexOf(nameEQ) == 0) return c.substring(nameEQ.length,c.length);
	}
	return null;
}

bdrs.mobile.cookie.erase = function(name) {
	createCookie(name,"",-1);
}