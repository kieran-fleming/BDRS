//--------------------------------------
// Location
//--------------------------------------
bdrs.location = {};
bdrs.location.initLocationMapAndTable = function(locationRowURL) {
    var layerName = 'Location Layer';
    bdrs.map.initBaseMap('base_map', { geocode: { selector: '#geocode' }});
    var layer = bdrs.map.addMultiClickPositionLayer(layerName, function(feature) {

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
            'index': index,
        };

        jQuery.get(bdrs.contextPath + locationRowURL, params, function(data) {
            var table = jQuery("#locationTable");
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
    }, function(feature, pixel) {
        // Move Feature Handler
        var lonInput;
        var latInput;

        if(feature.bdrs.index === undefined) {
            // Moving a persisted point.
            var pk = feature.bdrs.pk;
            latInput = jQuery("[name=latitude_"+pk+']');
            lonInput = jQuery("[name=longitude_"+pk+']');
        } else {
            // Moving an added point.
            var index = feature.bdrs.index;
            latInput = jQuery("[name=add_latitude_"+index+']');
            lonInput = jQuery("[name=add_longitude_"+index+']');
        }

        var lonLat = bdrs.map.baseMap.getLonLatFromViewPortPx(pixel);
        lonLat.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
        latInput.val(bdrs.map.roundLatOrLon(lonLat.lat));
        lonInput.val(bdrs.map.roundLatOrLon(lonLat.lon));
    });

    // Create features for existing locations and bind the necessary listeners
    var locationElems = jQuery("[name=location]");
    var deleteHandler = function(event) {
        var layer = bdrs.map.baseMap.getLayer(event.data.layerId);
        var feature = layer.getFeatureById(event.data.featureId);
        layer.destroyFeatures([feature]);
    };
    for(var i=0; i<locationElems.length; i++) {
        var pk = jQuery(locationElems[i]).val();
        var lat = jQuery("[name=latitude_"+pk+"]");
        var lon = jQuery("[name=longitude_"+pk+"]");

        var lonLat = new OpenLayers.LonLat(parseFloat(lon.val()), parseFloat(lat.val()));
        lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION, bdrs.map.GOOGLE_PROJECTION);
        var feature = new OpenLayers.Feature.Vector(
            new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat));

        // Bind the delete link to remove the feature
        jQuery("#delete_"+pk).bind("click",
            {'featureId': feature.id, 'layerId': layer.id}, deleteHandler);

        // We are importantly setting the primary key attribute to the
        // bdrs object. This marks this object as a persisted location and
        // will be treated as such by the rest of the script.
        feature.bdrs = {};
        feature.bdrs.pk = pk;
        layer.addFeatures(feature);
    }
    if(layer.getDataExtent() !== null) {
        bdrs.map.baseMap.zoomToExtent(layer.getDataExtent(), false);
    }
};