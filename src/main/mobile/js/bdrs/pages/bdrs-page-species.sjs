/**
 * Event handlers for the record page. 
 */
exports.Create =  function() {
}
	
exports.Show = function() {
	
	var speciesId = bdrs.mobile.getParameter('species');
	
	var species;
	waitfor(species) {
		Species.findBy('id', speciesId, resume);
	}
	
	waitfor() {
		bdrs.template.renderCallback(
			'textListItem',
			{ id: species.id, title : 'Common Name', description: species.commonName() },
			'.bdrs-page-species .taxonList',
			resume);
	}
	waitfor() {
		bdrs.template.renderCallback(
			'textListItem',
			{ id: species.id, title : 'Scientific Name', description: species.scientificName() },
			'.bdrs-page-species .taxonList',
			resume);
	}
	
	var profileItems;
	waitfor(profileItems) {
		SpeciesProfile.all().filter('species', '=', species).order('weight', true).list(resume);
	}
	
	for (var i = 0; i < profileItems.length; i++) {
		
		if (profileItems[i].type() == "text"){

			waitfor() {
				bdrs.template.renderCallback(
						'textListItem',
						{ id: profileItems[i].id, title : profileItems[i].description(), description: profileItems[i].content() },
						'.bdrs-page-species .taxonList',
						resume);
			}
			
		}
		else if (bdrs.phonegap.isPhoneGap() && profileItems[i].type() == "audio"){
			
			waitfor() {
				bdrs.template.renderCallback(
						'audioListItem',
						{ id: profileItems[i].id, title : "Call" },
						'.bdrs-page-species .taxonList',
						resume);
			}
			
		}
		else if (bdrs.phonegap.isPhoneGap() && (profileItems[i].type() == "profile_img" || profileItems[i].type() == "map_40x40" || profileItems[i].type() == "silhouette")){
			
			//websql
			var profileImage;
			waitfor(profileImage){
				Image.findBy('path', profileItems[i].content(), resume);
			}
			waitfor(){
				bdrs.template.renderCallback(
						'profileImageListItem',
						{ id: profileItems[i].id, imageData : profileImage.data(), type: profileImage.type() },
						'.bdrs-page-species .taxonList',resume);
			}

			//localstorage
/*			var jsonImage = JSON.parse(localStorage[profileItems[i].content()]);
			waitfor() {
				bdrs.template.renderCallback(
						'profileImageListItem',
						{ id: profileItems[i].id, imageData : jsonImage.data, type: jsonImage.type },
						'.bdrs-page-species .taxonList',resume);
			}*/
			
		}
			
	}
	
	jQuery('.bdrs-page-species .taxonList').listview("refresh");
}
	
exports.Hide = function() {
	jQuery('.bdrs-page-species .taxonList').empty();
}