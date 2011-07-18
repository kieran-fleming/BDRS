/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}
	
exports.Show = function() {
	var Callback =  function(id) {
		var id = id;
		
		this.handler = function(event) {
			bdrs.mobile.setParameter('taxonGroup', id);
			jQuery.mobile.changePage("#species-list", "slide", false, true);
		}
		return this;
	};

	var taxonGroups;
	waitfor(taxonGroups) {
		TaxonGroup.all().order('name', true).list(resume);
	}
	for (var i = 0; i < taxonGroups.length; i++) {
		waitfor() {
			bdrs.template.renderCallback(
				'textListItem', 
				{id: taxonGroups[i].id, title : taxonGroups[i].name(), description: 'View species from the group ' + taxonGroups[i].name() }, 
				'.bdrs-page-guide .taxonList', 
				resume);
		}
		jQuery('.bdrs-page-guide .taxonList .' + taxonGroups[i].id).click(new Callback(taxonGroups[i].id).handler);
	}
	jQuery('.bdrs-page-guide .taxonList').listview("refresh");
}
	
exports.Hide = function() {
	jQuery('.bdrs-page-guide .taxonList').empty();
}