/**
 * Recursiveley deletes censusmethods
 * @param	cm	Parent censusmethod.
 */
exports.recurse_delete_censusmethod = function(cm) {
    var children;
    waitfor(children) {
    	cm.children().list(resume);
    }    
	for (var i=0; i<children.length; i++) {
    	exports.recurse_delete_censusmethod(children[i]);
    }
	var attributes;
	waitfor (attributes) {
		cm.attributes().list(resume);
	}
	bdrs.mobile.attribute.removeAttributes(attributes);
	persistence.remove(cm);
};