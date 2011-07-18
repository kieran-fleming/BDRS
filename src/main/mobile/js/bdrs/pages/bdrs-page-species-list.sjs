/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
	
}
	
exports.Show = function() {
	taxonGroupId = bdrs.mobile.getParameter('taxonGroup');
	var taxonGroup;
	waitfor(taxonGroup) {
		TaxonGroup.findBy('id', taxonGroupId, resume);
	}
	var species;
	if (taxonGroup !== null) {
		waitfor(species) {
			Species.all().filter('taxonGroup', '=', taxonGroup).order('commonName', true).list(resume);
		}
	} else {
		waitfor(species) 
		{
			Species.all().order('commonName', true).list(resume);
		}
	}

	var Callback =  function(id) {
		var id = id;
		
		this.handler = function(event) {
			bdrs.mobile.setParameter('species', id);
			jQuery.mobile.changePage("#species", "slide", false, true);
		}
		return this;
	};

	// Because of rendering performance issues, 
	// don't render more than a couple of hundred species.	
	for (var i = 0; (i < species.length && i < 200); i++) {
		waitfor() {
			bdrs.template.renderCallback(
				'textListItem', 
				{ id: species[i].id, title : species[i].commonName(), description: species[i].scientificName() }, 
				'.bdrs-page-species-list .taxonList', 
				resume);
		}
		jQuery('.bdrs-page-species-list .taxonList .' + species[i].id).click(new Callback(species[i].id).handler);
	}
	jQuery('.bdrs-page-species-list .taxonList').listview("refresh");
}
	
exports.Hide = function() {
	jQuery('.bdrs-page-species-list .taxonList').empty();
}
