/**
 * Event handlers for the Settings page. 
 */
exports.Create = function() {
	// Enable the reset database button
	jQuery('.bdrs-page-settings .reset_database').click(function() {
    	persistence.reset( function () {
	   		persistence.schemaSync( function () {
	   			jQuery.mobile.changePage("#login", "none", false, true);
	   			alert("Database Cleared");
	   			window.location="index.html";
	   		});
	   	});
  	});
  	
  	// Enable the reset template button
	jQuery('.bdrs-page-settings .reset_templates').click(function() {
    	Template.all().each(function (template) {
    		persistence.remove(template);
    	});
    	persistence.flush();
  	});
  	
  	// Enable to add census method data button
  	jQuery('.bdrs-page-settings .add_census_data').click(function() {
    	bdrs.mobile.addExampleCensusMethods(); // from bdrs-mobile-persistence.js
  	});
  	
  	// Enable to add census method data button
  	jQuery('.bdrs-page-settings .clear_census_data').click(function() {
  		var censuses;
  		waitfor(censuses) {
  			CensusMethod.all().list(resume);
  		}
  		for (var i = 0; i < censuses.length; i++) {
  			var attributes;
  			waitfor (attributes) {
  				censuses[i].attributes().list(resume);
  			}
  			for (var j = 0; j < attributes.length; j++) {
  				var options;
  				waitfor (options) {
  					attributes[j].options().list(resume);
  				}
  				for (var k = 0; k < options.length; k++) {
  					bdrs.mobile.Debug('Option : ' + options[k].value());
  					persistence.remove(options[k]);
  				}
  				bdrs.mobile.Debug('Attribute : ' + attributes[j].name());
  				persistence.remove(attributes[j]);
  			}
  			var records;
  			waitfor (records) {
  				censuses[i].records().list(resume);
  			}
  			for (var j = 0; j < records.length; j++) {
  				var attributes;
  				waitfor (attributes) {
  					records[j].attributeValues().prefetch('attribute').list(resume);
  				}
  				for (var k = 0; k < attributes.length; k++) {
  					bdrs.mobile.Debug('Record Attribute : ' + attributes[k].value());
  					persistence.remove(attributes[k]);
  				}
  				bdrs.mobile.Debug('Record : ' + records[j].id);
  				persistence.remove(records[j]);
  			}
  			bdrs.mobile.Debug('Census : ' + censuses[i].name());
  			persistence.remove(censuses[i]);
  		}
  	});
  	
  	// Enable the reload button
  	jQuery('.bdrs-page-settings .reload').click(function() {
    	window.location="index.html";
  	});
  	
  	jQuery(".bdrs-page-settings .dump_db").click(function() {
  	    jQuery.mobile.pageLoading(false);
        bdrs.mobile.fileMgr.dumpToJson();
        jQuery.mobile.pageLoading(true);
  	});
  	
  	
  	// Enable the reload button
  	jQuery('.bdrs-page-settings .settings_test').click(function() {
    	window.location="test.html";
  	});
  	
}
	
exports.Show = function() {
}

exports.Hide = function() {
}
