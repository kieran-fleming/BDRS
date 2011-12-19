if (bdrs.map === undefined) {
	bdrs.map = {};
}

/*
 * If we want a point with a _radius_ of 4 pixels tolerance, we would use 4 for pixel tolerance.
 */
bdrs.map.calcClickBufferKm = function(map, pixelTolerance) {
	if (!pixelTolerance) {
		pixelTolerance = 4;
	}
	var scale = map.getScale();
    // current we have a 1 pixel tolerance! We can increase this if required...
	var buffer = pixelTolerance*(scale/(OpenLayers.DOTS_PER_INCH))*OpenLayers.METERS_PER_INCH/1000;
	return buffer;
};



bdrs.map.generateHeaderSLD = function() {
    var sld = '<?xml version="1.0" encoding="utf-8"?>';
    sld+= '<sld:StyledLayerDescriptor version="1.0.0">';
	return sld;
};

bdrs.map.generateFooterSLD = function() {
	var sld = '</sld:StyledLayerDescriptor>';
	return sld;
};

bdrs.map.generatePolygonSLD = function(options) {
    if (!options) {
        options = {};
    }
    var defaultOptions = {
        layerName: "DEFAULTNAME",
		userStyleName: "DEFAULTSYLENAME",
        fillColor: "FF0000",
        fillOpacity: "1",
        strokeColor: "FF0000",
		strokeOpacity: "1",
		strokeWidth: "1"
    };
    options = jQuery.extend(defaultOptions, options);
	
	var sld = '<sld:NamedLayer><sld:Name>'+options.layerName+'</sld:Name><sld:UserStyle><sld:Name>'+options.userStyleName+'</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
    sld+= '<sld:PolygonSymbolizer><sld:Fill><sld:CssParameter name="fill">'+options.fillColor+'</sld:CssParameter><sld:CssParameter name="fill-opacity">'+options.fillOpacity+'</sld:CssParameter></sld:Fill>';
    sld+= '<sld:Stroke><sld:CssParameter name="stroke">'+options.strokeColor+'</sld:CssParameter><sld:CssParameter name="stroke-opacity">'+options.strokeOpacity+'</sld:CssParameter><sld:CssParameter name="stroke-width">'+options.strokeWidth+'</sld:CssParameter></sld:Stroke></sld:PolygonSymbolizer>';
    sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>';
	return sld;
};

bdrs.map.generateLineSLD = function(options) {

	if (!options) {
        options = {};
    }
    var defaultOptions = {
        layerName: "DEFAULTNAME",
        userStyleName: "DEFAULTSYLENAME",
        fillColor: "FF0000",
        fillOpacity: "1",
        strokeColor: "FF0000",
        strokeOpacity: "1",
        strokeWidth: "2"
    };
	options = jQuery.extend(defaultOptions, options);
	
    var sld = '<sld:NamedLayer><sld:Name>'+options.layerName+'</sld:Name><sld:UserStyle><sld:Name>'+options.userStyleName+'</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
    sld+= '<sld:LineSymbolizer>';
    sld+= '<sld:Stroke><sld:CssParameter name="stroke">'+options.strokeColor+'</sld:CssParameter><sld:CssParameter name="stroke-opacity">'+options.strokeOpacity+'</sld:CssParameter><sld:CssParameter name="stroke-width">'+options.strokeWidth+'</sld:CssParameter></sld:Stroke></sld:LineSymbolizer>';
    sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>';
	
    return sld; 
};

bdrs.map.generatePointSLD = function(options) {  
    if (!options) {
        options = {};
    }
    var defaultOptions = {
        layerName: "DEFAULTNAME",
        userStyleName: "DEFAULTSTYLENAME",
        fillColor: "#EE0000",
        strokeColor: "#EF0000",
        strokeWidth: "1",
        size: "5"
    };
    options = jQuery.extend(defaultOptions, options);

    var sld = '<sld:NamedLayer><sld:Name>'+options.layerName+'</sld:Name><sld:UserStyle><sld:Name>'+options.userStyleName+'</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
    sld+= '<sld:PointSymbolizer><sld:Graphic><sld:Mark><sld:WellKnownName>circle</sld:WellKnownName><sld:Fill><sld:CssParameter name="fill">'+options.fillColor+'</sld:CssParameter></sld:Fill><sld:Stroke><sld:CssParameter name="stroke">'+options.strokeColor+'</sld:CssParameter><sld:CssParameter name="stroke-width">'+options.strokeWidth+'</sld:CssParameter></sld:Stroke></sld:Mark><sld:Size>'+options.size+'</sld:Size></sld:Graphic></sld:PointSymbolizer>';
    sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>';
        
    return sld;  
};

bdrs.map.generateLineSLD = function(options) {
	if (!options) {
        options = {};
    }
    var defaultOptions = {
        layerName: "DEFAULTNAME",
        userStyleName: "DEFAULTSTYLENAME",
        strokeColor: "#EF0000",
        strokeWidth: "1"
    };
    options = jQuery.extend(defaultOptions, options);

    var sld = '<sld:NamedLayer><sld:Name>'+options.layerName+'</sld:Name><sld:UserStyle><sld:Name>'+options.userStyleName+'</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
    sld+= '<sld:LineSymbolizer><sld:Stroke><sld:CssParameter name="stroke">'+options.strokeColor+'</sld:CssParameter><sld:CssParameter name="stroke-width">'+options.strokeWidth+'</sld:CssParameter></sld:Stroke></sld:LineSymbolizer>';
    sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>';
        
    return sld;  
};

/*
 * For generating SLD for non highlight layers
 */
bdrs.map.generateLayerSLD = function(options) {
	if (!options) {
        options = {};
    }
	
    var defaultOptions = {
        layerName: "",
        userStyleName: "CAN_BE_ANYTHING",
        fillColor: "#EE0000",
        strokeColor: "#EF0000",
        strokeWidth: "1",
        size: "5"
    };
	options = jQuery.extend(defaultOptions, options);

    // the layer names used here correspond to the 'low level' layers in the 
	// map file. DONT MESS WITH THESE, THE MAPSERVER WON'T STYLE YOUR LAYERS PROPERLY!
    var sld = bdrs.map.generateHeaderSLD();
	
	// geo map features....
	options.layerName = "geoMapFeature_polygon";
	options.userStyleName = "geoMapFeature_polygon";
    sld += bdrs.map.generatePolygonSLD(options);
	
	options.layerName = "geoMapFeature_point";
	options.userStyleName = "geoMapFeature_point";
    sld += bdrs.map.generatePointSLD(options);
	
	options.layerName = "geoMapFeature_line";
	options.userStyleName = "geoMapFeature_line";
	sld += bdrs.map.generateLineSLD(options);
	
	// records...
	 options.layerName = "record_polygon";
    options.userStyleName = "record_polygon";
    sld += bdrs.map.generatePolygonSLD(options);
    
    options.layerName = "record_point";
    options.userStyleName = "record_point";
    sld += bdrs.map.generatePointSLD(options);
	
	options.layerName = "record_line";
    options.userStyleName = "record_line";
    sld += bdrs.map.generateLineSLD(options);
	
    sld += bdrs.map.generateFooterSLD();
	return sld;
};

/**
 * Initialises event handling for wkt validation. Uses a webservice to calculate whether
 * the geometry is valid or not
 * 
 * @param {Object} wktSelector selector for the WKT input field
 * @param {Object} wktMessageSelector selector for the field to display the resulting message
 * @param {Object} validHandler handler that runs when the geometry is valid
 * @param {Object} notValidHandler handler that runs when the geometry is invalid
 */
bdrs.map.initWktOnChangeValidation = function(wktSelector, wktMessageSelector, validHandler, notValidHandler) {
	jQuery(wktSelector).bind("change", function(value) {
        bdrs.map.validateWktInput(wktSelector, wktMessageSelector, validHandler, notValidHandler);
	});
};

/**
 * Validates an input with a wkt string in it for a valid geometry.
 * 
 * Use when you don't want to bind to a javascript event.
 * 
 * @param {Object} wktSelector selector for the WKT input field
 * @param {Object} wktMessageSelector selector for the field to display the resulting message
 * @param {Object} validHandler handler that runs when the geometry is valid
 * @param {Object} notValidHandler handler that runs when the geometry is invalid
 */
bdrs.map.validateWktInput = function(wktSelector, wktMessageSelector, validHandler, notValidHandler) {
	var requestParams = {
        "wkt": jQuery(wktSelector).val()
    };
        
    jQuery.ajax({
        url:  bdrs.contextPath + "/webservice/location/isValidWkt.htm",
        data: jQuery.param(requestParams, true),
        success: function(data, textStatus, jqXhr) {
            if (data.isValid) {
                if (validHandler) {
                    validHandler(wktSelector, wktMessageSelector, data);
                }
            } else {
                if (notValidHandler) {
                    notValidHandler(wktSelector, wktMessageSelector, data);
                }
            }
            jQuery(wktMessageSelector).text(data.message);
        },
        error: bdrs.message.getAjaxErrorFunc("Could not validate WKT string")
    });
};
