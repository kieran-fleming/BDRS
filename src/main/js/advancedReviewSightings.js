/*global jQuery: false OpenLayers: false */
if(window.bdrs === undefined) {
    bdrs = {};
}

bdrs.advancedReview = {};

/**
 * Applies click handlers to the tabs to switch between map and table views. 
 */
bdrs.advancedReview.initTabHandlers = function() {

	jQuery("#listViewTab").click(function() {
		jQuery("input[name=viewType]").val("table");
		jQuery("#facetForm").submit();
	});
	
	jQuery("#mapViewTab").click(function() {
		jQuery("input[name=viewType]").val("map");
		jQuery("#facetForm").submit();
	});
};

/**
 * Initialises the table view by retrieving records from the server via AJAX.
 *
 * @param formSelector jQuery selector of the form containing the query parameters.
 * @param tableSelector jQuery selector of the table where the list of records will be inserted.
 * @param sortOrderSelector selector for the input providing the sorting order of the records.
 * @param sortBySelector selector for the input providing the sorting property of the records.
 * @param resultsPerPageSelector selector for the input to alter the number of records displayed per page.
 */
bdrs.advancedReview.initTableView = function(formSelector, 
                                            tableSelector, sortOrderSelector, 
                                            sortBySelector, resultsPerPageSelector) {
    // AJAX load the content for the table
    var url = bdrs.contextPath + "/review/sightings/advancedReviewJSONSightings.htm?" 
    var queryParams = jQuery(formSelector).serialize();
    jQuery.getJSON(url + queryParams, {}, function(recordArray) {
        var html = [];
        for(var i=0; i<recordArray.length; i++) {
            var record = recordArray[i];
            // Start of sighting
            html.push('<div class="sighting">');
            
            // Start of first line
            html.push('<div>');
            // Record Type
            html.push('<span class="recordType">');
            if(record.censusMethod !== null && record.censusMethod !== undefined) {
                html.push(record.censusMethod.type);
                html.push(':&nbsp;');
            } else {
                html.push("Observation:&nbsp;");   
            }
            html.push("</span>");
            
            // Date
            html.push('<span class="nowrap">');
            html.push('<a href="');
            html.push(bdrs.contextPath);
            html.push('/bdrs/user/surveyRenderRedirect.htm?surveyId=')
            html.push(record.survey.id)
            html.push('&recordId=');
            html.push(record.id);
            html.push('">');
            html.push(bdrs.util.formatDate(new Date(record.when)));
            html.push('</a>');
            html.push('</span>');
            
            // Scientific Name
            if(record.species !== null && record.species !== undefined) {
                html.push('&nbsp;&mdash;&nbsp;');
                
                html.push('<span class="taxonRank">');
                html.push(titleCaps(record.species.taxonRank.toLowerCase()));
                html.push(':&nbsp;</span>');
                
                html.push('<span class="scientificName">');
                html.push(record.species.scientificName);
                html.push('</span>');
                
                html.push('&nbsp;|&nbsp;');
                
                html.push('<span class="commonName">');
                html.push(record.species.commonName);
                html.push('</span>');
            }
            // End of first line
            html.push('</div>');
            
            // Start of second line
            html.push('<div>');
            
            // Location
            html.push('<span class="location">');
            if(record.location !== null && record.location !== undefined) {
                html.push('Location:&nbsp;');
                html.push(record.location.name);
            } else {
                html.push('Coordinate:&nbsp;');
                html.push(record.latitude);
                html.push(',&nbsp;');
                html.push(record.longitude);
            }
            html.push('</span>');
            
            // End of second line
            html.push("</div>");
            
            // End of sighting
            html.push("</div>");
        }
        jQuery(tableSelector).append(html.join(''));
    });
    
    // Change Handlers for the Sort Property and Order
    var changeHandler = function(evt) {
        jQuery("#facetForm").submit();
    };
    
    jQuery(sortOrderSelector).change(changeHandler); 
    jQuery(sortBySelector).change(changeHandler);
    jQuery(resultsPerPageSelector).change(changeHandler);
};

bdrs.advancedReview.pageSelected = function(pageNumber) {
    jQuery("#pageNumber").val(pageNumber);
    jQuery("#facetForm").submit();
};

/**
 * Initialises the map view by requesting a KML from the server and populating 
 * the record count.
 *
 * @param formSelector jQuery selector of the form containing the query parameters.
 * @param mapId the id of the element where the map will be inserted.
 * @param mapOptions options to be used for map initialisation.
 */
bdrs.advancedReview.initMapView = function(formSelector, mapId, mapOptions) {

    bdrs.map.initBaseMap(mapId, mapOptions);
    bdrs.map.baseMap.events.register('addlayer', null, bdrs.map.addFeaturePopUpHandler);
    bdrs.map.baseMap.events.register('removeLayer', null, bdrs.map.removeFeaturePoupUpHandler);
    
    var queryParams = jQuery(formSelector).serialize();
    var kmlURL = bdrs.contextPath + "/review/sightings/advancedReviewKMLSightings.htm?" + queryParams;
	
	var layerOptions = {
		visible: true,
        includeClusterStrategy: true
	};
	
    var layer = bdrs.map.addKmlLayer(bdrs.map.baseMap, "Sightings", kmlURL, layerOptions);
    layer.events.register('loadend', layer, function(event) {
        var extent = event.object.getDataExtent();
        if(extent !== null) {
            bdrs.map.baseMap.zoomToExtent(event.object.getDataExtent(), false);
        }
    });
};

/**
 * Initialises the facets by attaching change handlers to the facet options.
 *
 * @param formSelector jQuery selector for the form that will be submitted when the facet option is changed.
 * @param facetSelector jQuery selector for the facet container.
 */
bdrs.advancedReview.initFacets = function(formSelector, facetSelector) {
    jQuery(facetSelector).find('input[type=checkbox]').change(function() {
        jQuery(formSelector).submit();
    });
};

bdrs.advancedReview.initRecordDownload = function(formSelector, downloadSelector) {
    jQuery(downloadSelector).click(function(event) {
        var queryParams = jQuery(formSelector).serialize();
        var downloadURL = bdrs.contextPath + "/review/sightings/advancedReviewDownload.htm?" + queryParams;
        window.document.location = downloadURL; 
    }); 
};