/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}
	
exports.Show = function() {
	bdrs.mobile.Debug('sites show');
	var sites;
	waitfor(sites) {
		Location.all().order('name', true).list(resume);
	}
	
	var Callback =  function(siteId) {
        var siteId = siteId;
        
        this.handler = function(event) {
        	bdrs.mobile.setParameter('siteId', siteId);
			jQuery.mobile.changePage("#site", "slide", false, true);
        }
        return this;
	};

	for (var i = 0; i < sites.length; i++) {
		waitfor() {
			bdrs.template.renderCallback(
				'siteListItem', 
				{id: sites[i].id, title : sites[i].name(), description: sites[i].latitude() + ", " + sites[i].longitude() }, 
				'.bdrs-page-sites .siteList', 
				resume);
		}
		jQuery('.bdrs-page-sites .site-' + sites[i].id).click(new Callback(sites[i].id).handler);
	}
	jQuery('.bdrs-page-sites .siteList').listview("refresh");
}
	
exports.Hide = function() {
	jQuery('.bdrs-page-sites .siteList').empty();
}