//--------------------------------------
// Location
//--------------------------------------
bdrs.location = {};
bdrs.location.initLocationMapAndTable = function(locationRowURL) {
    var layerName = 'Location Layer';
    bdrs.map.initBaseMap('base_map', { geocode: { selector: '#geocode' }});
	
	var addFeatureHandler = function(feature) {

        if(feature.bdrs !== undefined) {
            // If it has already been defined, then it has been created and
            // added directly and NOT via a mouse click. This is the use
            // case when editing a feature. We do not want to add more
            // input boxes.
            return;
        }

        // Add Feature Handler
        var index = jQuery("[name=add_location]").length;
        feature.bdrs = {};
        feature.bdrs.index = index;
        var params = {
            'index': index
        };

        jQuery.get(bdrs.contextPath + locationRowURL, params, function(data) {
            var table = jQuery("#locationList");
            var tbody = table.find("tbody");

            var row = jQuery(data);

            // Bind the delete link to remove the feature also
            row.find("#delete_"+index).click(function() {
                layer.destroyFeatures([feature]);
            });

            var lonLat = feature.geometry.bounds.getCenterLonLat();
            lonLat.transform(bdrs.map.GOOGLE_PROJECTION,
                 bdrs.map.WGS84_PROJECTION);

            row.find("[name=add_latitude_"+index+']').val(bdrs.map.roundLatOrLon(lonLat.lat));
            row.find("[name=add_longitude_"+index+']').val(bdrs.map.roundLatOrLon(lonLat.lon));
            // for adding a line or polygon
            var wkt = new OpenLayers.Format.WKT(bdrs.map.wkt_options);
            row.find("[name=add_location_WKT_"+index+"]").val(wkt.write(feature));
            
            // Make up a name
            var locationNames = jQuery(".location_name");
            var nameMap = {};
            var current;
            for(var i=0; i<locationNames.length; i++) {
                current = jQuery(locationNames[i]);
                nameMap[current.val()] = current;
            }
            var count = 1;
            var complete = false;
            var name;
            while(!complete) {
                name = "Location "+count;
                if(nameMap[name] === undefined) {
                    row.find("[name=add_name_"+index+"]").val(name);
                    complete = true;
                }
                count += 1;
            }

            tbody.append(row);
        });
	};
	
	var featureMoveCompleteHandler = function(feature, pixel) {	
        // Move Feature Handler
        var lonInput;
        var latInput;
        var wktInput;
        
        if(feature.bdrs.index === undefined) {
            // Moving a persisted point.
            var pk = feature.bdrs.pk;
            latInput = jQuery("[name=latitude_"+pk+']');
            lonInput = jQuery("[name=longitude_"+pk+']');
            wktInput = jQuery("[name=location_WKT_"+pk+"]");
            
        } else {
            // Moving an added point.
            var index = feature.bdrs.index;
            latInput = jQuery("[name=add_latitude_"+index+']');
            lonInput = jQuery("[name=add_longitude_"+index+']');
            wktInput = jQuery("[name=add_location_WKT_"+index+"]");
        }
		
		var centroid = feature.geometry.getCentroid();
        centroid.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
		latInput.val(bdrs.map.roundLatOrLon(centroid.y));
        lonInput.val(bdrs.map.roundLatOrLon(centroid.x));

        var wkt = new OpenLayers.Format.WKT(bdrs.map.wkt_options);
        wktInput.val(wkt.write(feature));
    };

    var polygonDrawLayerOptions = {
		featureAddedHandler: addFeatureHandler,
		featureMoveCompleteHandler: featureMoveCompleteHandler
	};

    var layer = bdrs.map.addPolygonDrawLayer(bdrs.map.baseMap, layerName, polygonDrawLayerOptions);
    
    // Create features for existing locations and bind the necessary listeners
    var locationElems = jQuery("[name=location]");
    var deleteHandler = function(event) {
        var layer = bdrs.map.baseMap.getLayer(event.data.layerId);
        var feature = layer.getFeatureById(event.data.featureId);
        layer.destroyFeatures([feature]);
    };

    // handler to highlight the location on the map when the location in the table
    // gets focus
    var focusHandler = function(event) {
        var layer = bdrs.map.baseMap.getLayer(event.data.layerId);
        var feature = layer.getFeatureById(event.data.featureId);
        // highlight the feature
    }
    
    var wkt = new OpenLayers.Format.WKT(bdrs.map.wkt_options);
    for(var i=0; i<locationElems.length; i++) {
        var pk = jQuery(locationElems[i]).val();
        // add the WKT geometry feature
        var wktLoc = $("[name=location_WKT_"+pk+"]");
        var geoFeature = wkt.read(wktLoc.val());
        
        // Bind the delete link to remove the feature
        jQuery("#delete_"+pk).bind("click",
            {'featureId': geoFeature.id, 'layerId': layer.id}, deleteHandler);

        // bind the selectionHandler to focus on the corresponding location
        // in the table when the feature is selected
        //$("[name=location_"+pk+"]")
        
        // bind the focus for the table element to select the map feature
        jQuery("[name=name_"+pk+']').bind("focus",
                {'featureId': geoFeature.id, 'layerId': layer.id}, focusHandler);
        
        // We are importantly setting the primary key attribute to the
        // bdrs object. This marks this object as a persisted location and
        // will be treated as such by the rest of the script.
        geoFeature.bdrs = {};
        geoFeature.bdrs.pk = pk;
        layer.addFeatures(geoFeature);
    }
    bdrs.map.centerMapToLayerExtent(bdrs.map.baseMap, layer);
    
    bdrs.location.addFeatureClickPopup(layer);
};

bdrs.location.addFeatureClickPopup = function(layer) {
    var onPopupClose = function(evt) {
        // 'this' is the popup.
        selectControl.unselect(this.feature);
    };
    
    var onFeatureSelect = function(feature) {
        var popupOptions = {
            onPopupClose: onPopupClose,
            featureToBind: feature
        };
        
        bdrs.location.createFeaturePopup(bdrs.map.baseMap, feature.geometry.getBounds().getCenterLonLat().clone(), feature, popupOptions);
    };
    var onFeatureUnselect = function(feature) {
        bdrs.map.baseMap.removePopup(feature.popup);
        feature.popup.destroy();
        feature.popup = null;
    };

    var selectControl = new OpenLayers.Control.SelectFeature(layer, {
        onSelect: onFeatureSelect,
        onUnselect: onFeatureUnselect
    });

    bdrs.map.baseMap.addControl(selectControl);
    selectControl.activate();

    return selectControl;
};

bdrs.location.createFeaturePopup = function (map, googleProjectionLonLatPos, feature, options) {
    if (!options) {
        options = {};
    }
    defaultOptions = {
        popupName: "popcorn",
        onPopupClose: null,
        mapServerQueryManager: null,
        featureToBind: null
    };
    options = jQuery.extend(defaultOptions, options);
    
    // the final target....
    var content = jQuery("<span></span>");
    var cyclerDiv = jQuery("<div></div>").addClass("textcenter").width('100%').appendTo(content);
    jQuery("<span> Name: </span>").appendTo(cyclerDiv);
    var name = "";
    if(feature.bdrs.index === undefined) {
    	name = jQuery("[name=name_"+feature.bdrs.pk+"]").val();
    } else {
    	name = jQuery("[name=add_name_"+feature.bdrs.index+"]").val();
    	googleProjectionLonLatPos.transform(bdrs.map.WGS84_PROJECTION,
                bdrs.map.GOOGLE_PROJECTION);
        
    }
    jQuery("<span>"+name+"</span>").appendTo(cyclerDiv);
    var popup = new OpenLayers.Popup.FramedCloud(options.popupName,
                             googleProjectionLonLatPos,
                             null,
                             content.html(),
                             null, true, options.onPopupClose);
    
    // stops scroll bars appearing in the popup. 
    // the popup will resize to the content         
    jQuery(popup.contentDiv).css("overflow", "hidden");
                 
    if (options.featureToBind) {
        var feature = options.featureToBind;
        feature.popup = popup;
        // So we can deselect the feature later via onPopupClose
        popup.feature = feature;
    }

    map.addPopup(popup);

    popup.show();
    
    return popup;
};