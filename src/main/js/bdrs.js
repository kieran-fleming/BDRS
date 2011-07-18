/*global jQuery: false OpenLayers: false */

/**
 * @fileoverview BDRS Scripts. Depends on jQuery 1.4.2+.
 * @author Gaia Resources
 */
if(window.bdrs === undefined) {
    window.bdrs = {};
}

/**
 * Create an empty debugging console if none exists (stops crash on Firefox 
 */
if(window.console === undefined) {
	window.console = {};
}
if(window.console.log === undefined) {
	window.console.log = function(){
		//TODO if we need logging for non debug browsers (ie firefox without firebug)
	};
}

bdrs.contextPath = "";
bdrs.ident = "";
bdrs.dateFormat = 'dd M yy';

bdrs.monthNames = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];

if (bdrs.map === undefined) {
	bdrs.map = {};
}
bdrs.map.SHADOW_Z_INDEX = 10;
bdrs.map.MARKER_Z_INDEX = 11;


// Dynamic post helper
bdrs.postWith = function(to,p) {
	var myForm = document.createElement("form");
	myForm.method="post" ;
	myForm.action = to ;
	for (var k in p) {
	    var myInput = document.createElement("input");
	    myInput.setAttribute("name", k) ;
	    myInput.setAttribute("value", p[k]);
	    myForm.appendChild(myInput) ;
	}
	document.body.appendChild(myForm);
	myForm.submit() ;
	document.body.removeChild(myForm);
}

// JQGrid helper
bdrs.JqGrid = function(gridSelector, baseUrl, baseQueryString) {
	this.baseUrl = baseUrl;
	this.baseQueryString = baseQueryString;
	
	this.getSelected = function() {
        var grid = jQuery(gridSelector);
        if (grid.getGridParam("multiselect")) {
            return jQuery(gridSelector).getGridParam('selarrrow');
        } else {
            return jQuery(gridSelector).getGridParam('selrow');
        }
    };
	
	this.getRowData = function(rowId) {
		var grid = jQuery(gridSelector);
		var selected = this.getSelected();
		return grid.jqGrid('getRowData', rowId);
	};
	
	this.setQueryString = function(f) {
		this.queryString = f;
		jQuery(gridSelector).jqGrid("setGridParam", {
			 url: this.createUrl()
		});
	};
	
	this.setBaseQueryString = function(f) {
		this.baseQueryString = f;
		jQuery(gridSelector).jqGrid("setGridParam", {
             url: this.createUrl()
        });
	};
	
	this.createQueryString = function() {
		if (this.baseQueryString && !this.queryString) {
			return this.baseQueryString;
		}
		if (this.baseQueryString && this.queryString) {
			return this.baseQueryString + '&' + this.queryString;
		}
		if (!this.baseQueryString && this.queryString) {
			return this.queryString;
		}
		// no query string to return...
		return null;
	};
	
	this.createUrl = function() {
		var q = this.createQueryString();
		if (q) {
			return baseUrl + "?" + this.createQueryString();
		} else {
			return baseUrl;
		}
	};
	
	this.reload = function() {
		jQuery(gridSelector).trigger("reloadGrid");
	};
};

// --------------------------------------
// Some notes on map projections
//
// Most spherical mercator maps use an extent of the world 
// from -180 to 180 longitude, and from -85.0511 to 85.0511 
// latitude. Because the mercator projection stretches to 
// infinity as you approach the poles, a cutoff in the north-south 
// direction is required, and this particular cutoff results in a 
// perfect square of projected meters. As you can see from the 
// maxExtent parameter sent in the constructor of the map, the 
// coordinates stretch from -20037508.34 to 20037508.34 in each direction.
//
// The maxResolution of the map fits this extent into 256 pixels, 
// resulting in a maxResolution of 156543.0339. 
//
// --------------------------------------


// initBaseMap:
// initialises bdrs.map.baseMap. Will use the createCustomMap function if it
// exists, else will use createDefaultMap.
// createCustomMap can be overridden in any subproject or theme giving you
// complete control on how the open layers map is configured.
bdrs.map.baseMap = null;
bdrs.map.selectedRecord = null;
bdrs.map.initBaseMap = function(mapId, options) {
    var mapOptions = { isPublic: true, ajaxFeatureLookup: false};
    jQuery.extend(mapOptions, options);

    bdrs.map.initOpenLayers();

    if (bdrs.map.createCustomMap) {
		bdrs.map.baseMap = bdrs.map.createCustomMap(mapId, mapOptions);
	} else {
		bdrs.map.baseMap = bdrs.map.createDefaultMap(mapId, mapOptions);
	}
	if (mapOptions.ajaxFeatureLookup) {
		var highlightLayer = bdrs.map.addWMSHighlightLayer(bdrs.map.baseMap);
        bdrs.map.initAjaxFeatureLookupClickHandler(bdrs.map.baseMap, {
			wmsHighlightLayer: highlightLayer
	    });
    } else {
        bdrs.map.initPointSelectClickHandler(bdrs.map.baseMap);
    }
}; // End initBaseMap

bdrs.map.createDefaultMap = function(mapId, mapOptions) {
    /*
        var mapOptions = {
            // the initial center point of the map
            mapCenter : new OpenLayers.LonLat(...) 
            
            // the initial zoom level of the map
            // if the zoom level is < 0, zoom the map to its maximum level
            mapZoom : int
            
            // Geocode Options
            geocode : {
                // jQuery selector of the container for the geocode input.
                selector: string
            
                // zoom level to use when centering on a geocode location. default 10.
                // if the zoom level is < 0, zoom the map to its maximum level
                zoom: int
                
                // true if the map center occurs on key down, false otherwise. default true.
                useKeyHandler : boolean
            } 
        }
    */ 
    
	var options = { projection: bdrs.map.GOOGLE_PROJECTION,
                    displayProjection: bdrs.map.WGS84_PROJECTION,
                    units: 'm',
                    maxResolution: 156543.0339,
                    maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34,
                                                     20037508.34, 20037508.34)};
    var map = new OpenLayers.Map(mapId, options);

    // duck punch in persistentLayers - will not be removed when other layers
	// are cleared from the map
	map.persistentLayers = new Array();

    if (mapOptions.isPublic === true) {
		var gphy = new OpenLayers.Layer.Google('Google Physical', {
			type: G_PHYSICAL_MAP,
			sphericalMercator: true
		});
		var gmap = new OpenLayers.Layer.Google('Google Streets', // the default
		{
			numZoomLevels: 20,
			sphericalMercator: true
		});
		var ghyb = new OpenLayers.Layer.Google('Google Hybrid', {
			type: G_HYBRID_MAP,
			numZoomLevels: 20,
			sphericalMercator: true
		});
		var gsat = new OpenLayers.Layer.Google('Google Satellite', {
			type: G_SATELLITE_MAP,
			numZoomLevels: 22,
			sphericalMercator: true
		});
		map.addLayers([gphy, gmap, ghyb, gsat]);
	} else {
        // add some OSM layers...This works well.
        var mapnik = new OpenLayers.Layer.OSM();
        map.addLayer(mapnik);
	}

    map.addControl(new OpenLayers.Control.LayerSwitcher());
    map.addControl(new OpenLayers.Control.MousePosition());
    
    if(mapOptions.geocode !== undefined && mapOptions.geocode !== null) {
        bdrs.map.addGeocodeControl(mapOptions.geocode);
    }
    if (document.getElementById('OpenLayers_Control_MaximizeDiv_innerImage')) {
        document.getElementById('OpenLayers_Control_MaximizeDiv_innerImage').setAttribute("title","Change the baselayer and turn map layers on or off");
    }
    if (document.getElementById("latitude")) {
        var latText = document.getElementById("latitude");
        latText.onchange = new Function('bdrs.map.lonlatChanged()');
            
        var lonText = document.getElementById("longitude");
        lonText.onchange = new Function('bdrs.map.lonlatChanged()');
    }
	var graticule = new OpenLayers.Control.Graticule({
        displayInLayerSwitcher: true
    });
    map.addControl(graticule);
	map.persistentLayers.push(graticule.gratLayer);
	
	if(mapOptions.mapCenter === undefined || mapOptions.mapCenter === null) {
	    if (document.getElementById('location')) {
	        bdrs.map.centerProjectMap(map);
	    }
	    else {
	        map.setCenter(
	        new OpenLayers.LonLat(136.5, -28.5).transform(
	            bdrs.map.WGS84_PROJECTION,
	            bdrs.map.GOOGLE_PROJECTION), 4);
	    }
    } else {
        var mapZoom = 4;
        if(mapOptions.mapZoom !== undefined && mapOptions.mapZoom !== null) {
            mapZoom = mapOptions.mapZoom;
            mapZoom = mapZoom < 0 ? map.getNumZoomLevels()-1 : mapZoom;
        }
        var lonlat = new OpenLayers.LonLat(mapOptions.mapCenter.lon, mapOptions.mapCenter.lat);
        map.setCenter(lonlat.transform(bdrs.map.WGS84_PROJECTION, bdrs.map.GOOGLE_PROJECTION), mapZoom);
    }
    return map;
};

/**
 * Handler function that adds a KML layer to the map via an AJAX request.
 *
 * @param formIdSelector
 *            selector for the form containing the query parameters.
 * @param mapLayersSelectSelector
 *            selector for the select element that displays added layers.
 */
bdrs.map.addRecordLayerHandler = function(formIdSelector, mapLayersSelectSelector, formOverrides) {
    var form = jQuery(formIdSelector);
    var map = bdrs.map.baseMap;
    var params = bdrs.util.formToMap(formIdSelector);
    if(typeof(formOverrides) !== 'undefined') {
        for(var property in formOverrides) {
            if(formOverrides.hasOwnProperty(property)) {
                params[property] = formOverrides[property];
            }
        }
    }
    
    var layerName = params.layer_name;
    if(layerName.length === 0) {
        return false;
    }
    
    var kmlURL = form.attr("action")+"?"+jQuery.param(params);
    var recordLayer = new OpenLayers.Layer.Vector(layerName, {
        projection: map.displayProjection,
        strategies: [new OpenLayers.Strategy.Fixed(),new OpenLayers.Strategy.BdrsCluster()],
        protocol: new OpenLayers.Protocol.HTTP({
            url: kmlURL,
            //data: formData,
            format: new OpenLayers.Format.KML({
                extractStyles: true,
                extractAttributes: true
            })
        })
    });
    recordLayer.events.register('loadend', recordLayer, function(event) {
        var layerOptElem = jQuery('option[id="'+recordLayer.id+'"]');
        if(layerOptElem.length > 0) {
            layerOptElem.text(layerOptElem.text() + ' ('+recordLayer.features.length+' Records)');
        }
        var extent = event.object.getDataExtent();
        if(extent !== null) {
            bdrs.map.baseMap.zoomToExtent(event.object.getDataExtent(), false);
        }
    });
    map.addLayers([recordLayer]);

    if(mapLayersSelectSelector !== null) {
        var layerOptionElem = jQuery('<option></option>');
        layerOptionElem.attr('id', recordLayer.id);
        layerOptionElem.text(layerName);
        layerOptionElem.val(recordLayer.id);
        // Needed later if we want to download the kml
        layerOptionElem.data('kmlRequest', kmlURL);
        jQuery(mapLayersSelectSelector).append(layerOptionElem);
    }
};

bdrs.map.addKmlLayer = function(map, layerName, kmlURL, options) {
	if (options === undefined) {
		options = {};
	}
	var defaultOptions = {
		visible: true,
		bdrsLayerId: undefined,
		includeClusterStrategy: false
	};
	options = jQuery.extend(defaultOptions, options);

    var strategies = [new OpenLayers.Strategy.Fixed()]
    if(options.includeClusterStrategy === true) {
        strategies.push(new OpenLayers.Strategy.BdrsCluster());
    }

	var layer = new OpenLayers.Layer.Vector(layerName, {
        projection: map.displayProjection,
		strategies: strategies,
        protocol: new OpenLayers.Protocol.HTTP({
            url: kmlURL,
            format: new OpenLayers.Format.KML({
                extractStyles: true,
                extractAttributes: true
            })
        }),
		visibility: options.visible
    });
	// duck punch!
	layer.bdrsLayerId = options.bdrsLayerId;
	
	map.addLayers([layer]);
	return layer;
};

bdrs.map.getBdrsMapServerUrl = function() {
	return "http://" + document.location.hostname + "/cgi-bin/mapserv?map=bdrs.map&";
};

bdrs.map.addMapServerLayer = function(map, layerName, url, options) {
    return bdrs.map.addWMSLayer(map, layerName, url, options);
};

bdrs.map.addWMSHighlightLayer = function(map, layername, url, options) {
	if (options === undefined) {
        options = {};
    }
    var defaultOptions = {
        visible: false,
        wmsLayer: 'bdrs_highlight',
        opacity: 0.85,
		displayInLayerSwitcher: false,
		tileSize: new OpenLayers.Size(512,512),
        buffer: 0
    };
	options = jQuery.extend(defaultOptions, options);
	
	var layer = new OpenLayers.Layer.WMS(
	layername ? layername : "highlightLayer",
	url ? url : bdrs.map.getBdrsMapServerUrl(), {
		'layers': options.wmsLayer,
		'transparent': 'true'
	}, {
		isBaseLayer: false,
		opacity: options.opacity,
		visibility: options.visible,
		displayInLayerSwitcher: options.displayInLayerSwitcher,
		tileSize: options.tileSize,
        buffer: options.buffer
	});
    map.addLayer(layer);
	return layer;
};

bdrs.map.addWMSLayer = function(map, layerName, url, options) {	
	if (options === undefined) {
        options = {};
    }
    var defaultOptions = {
        visible: true,
        bdrsLayerId: undefined,
		wmsLayer: 'bdrs',
		opacity: 1,
		tileSize: new OpenLayers.Size(512,512),
		buffer: 0
    };
    options = jQuery.extend(defaultOptions, options);
	
	var sld = bdrs.map.generateLayerSLD(options);

	var wmsLayer = new OpenLayers.Layer.WMS(
        layerName,
        url, {
            'layers': options.wmsLayer,
            'transparent': 'true',
			'geo_map_layer_id': options.bdrsLayerId
        }, {
            isBaseLayer: false,
            visibility: options.visible,
			opacity: options.opacity,
			tileSize: options.tileSize,
			buffer: options.buffer
        } );
	wmsLayer.mergeNewParams({"sld_body":sld});
	
	// duck punch!
    wmsLayer.bdrsLayerId = options.bdrsLayerId;
    map.addLayer(wmsLayer);
    return wmsLayer;
};

// Note the user/pass combination. Not the most secure way to do this I'm sure...
bdrs.map.addSlipWMSLayer = function(displayName, layerName, map) {
	var slipLayer = new OpenLayers.Layer.WMS(
	    displayName,
		'https://aarongaia:I2LJ5P7ozN@www2.landgate.wa.gov.au/ows/wmspublic', {
            'layers': layerName,
            'transparent': 'true'
		}, {
			isBaseLayer: false,
			visibility: true
		} );
    map.addLayer(slipLayer);
	return slipLayer;
};

bdrs.map.addSlipWFSLayer = function(displayLayerName, layerName, map, styleMap) {
	// self explanatory....
	throw "Do not use. Have not set up proxy";
	
    var slipLayer = new OpenLayers.Layer.Vector(displayLayerName, {
        projection: map.displayProjection,
        strategies: [new OpenLayers.Strategy.BBOX()],   // at least bbox seems to work out of the bbox, yay
        protocol: new OpenLayers.Protocol.WFS({
            version: "1.0.0",
            srsName: "EPSG:4326", // this is the default
            url: bdrs.contextPath + "/slip/wfspublic_4326/wfs",
            featureNS: "http://www.openplans.org/slip",
            featureType: layerName // overview roads
        }),
        styleMap: styleMap,
		visibility: false
    });
    map.addLayer(slipLayer);
	slipLayer.refresh(); // trigger bbox to download data
	return slipLayer;
};



// add a select handler for multiple layers.
// 'layers' is an array of layer objects
bdrs.map.addSelectHandler = function(map, layers) {
	if (!layers || layers.length == 0) {
		return;
	}
	var select = new OpenLayers.Control.SelectFeature(layers);
	
	for (var i=0; i<layers.length; ++i) {
		var layer = layers[i];
		
		layer.events.on({
            "featureselected": function(event) {
				
				bdrs.map.clearPopups(map);
				
                var feature = event.feature;
				
				try {
					var itemArray = new Array();
					for (var i = 0; i < feature.cluster.length; ++i) {
						itemArray.push(jQuery.parseJSON(feature.cluster[i].attributes.description));
					}
					bdrs.map.createFeaturePopup(map, feature.geometry.getBounds().getCenterLonLat(), itemArray);
					
				} catch (jsonParsingError) {
					console.log(jsonParsingError);
				    // exception thrown, we will assume it is a json parsing error...
					
		            // Since KML is user-generated, do naive protection against
		            // Javascript.
		            var content = "<h1>"+ (feature.attributes.name ? feature.attributes.name : "") + "</h1>" + 
		            (feature.attributes.description ? feature.attributes.description : "");
		            if (content.search("<script") != -1) {
		                content = "Content contained Javascript! Escaped content below.<br />" + content.replace(/</g, "&lt;");
		            }
		            popup = new OpenLayers.Popup.FramedCloud("chicken", 
		                                     feature.geometry.getBounds().getCenterLonLat(),
		                                     new OpenLayers.Size(100,100),
		                                     content,
		                                     null, true, function(evt) { select.unselectAll(); });
		            feature.popup = popup;
		            map.addPopup(popup);
				}
	        },
	        "featureunselected": function(event) {
	            var feature = event.feature;
	            if(feature.popup) {
	                map.removePopup(feature.popup);
	                feature.popup.destroy();
	                delete feature.popup;
	            }
	        }
	    });
	}
	map.addControl(select);
	
	// enable map drag panning while mouse is over feature
    select.handlers.feature.stopDown = false;
	
    select.activate();   
};

// trigger selector - the selector for the dom element that triggers the expansion / shrinking. e.g. an anchor
// contentSelector - the selector for the dom element that contains your map
// maximiseLabel - the string to show when the next operation is maximise
// minimiseLabel - the string to show when the next operation is minimise
// maxClass - the class to apply when the map is maximised
// minClass - the class to apply when the map is minimised
// mapSelector - the selector of the actual map div - not the wrapper
// map - the map javascript object
bdrs.map.maximiseMap = function(triggerSelector, contentSelector, maximiseLabel, minimiseLabel, maxClass, minClass, mapSelector, map) {
    var content = jQuery(contentSelector);
    var trigger = jQuery(triggerSelector);
    if (content.hasClass("maximise")) {
		// maximise the map!
		content.unwrap();
		content.removeClass("maximise");
		jQuery(mapSelector).removeClass(maxClass).addClass(minClass);
		map.updateSize();
		trigger.text(maximiseLabel);
		trigger.css("position", "static");
		jQuery(".mapLogoMax").removeClass("mapLogoMax").addClass("mapLogoMin");
	} else {
		// minimise the map!
		var wrapper = content.wrap('<div></div>').parent();
		wrapper.css({
			position: 'fixed',
			top: 0,
			bottom: 0,
			left: 0,
			right: 0,
			backgroundColor: 'white',
			zIndex: 1500
		});
		content.addClass("maximise");
		jQuery(mapSelector).removeClass(minClass).addClass(maxClass);
		map.updateSize();
		trigger.text(minimiseLabel);
		trigger.css("position", "fixed");
		trigger.css("top", "1px");
		trigger.css("right", "3px");
		trigger.css("zIndex", "1600");
		jQuery(".mapLogoMin").removeClass("mapLogoMin").addClass("mapLogoMax");
	}
};

bdrs.map.addMapLogo = function(map, imgUrl, containerNodeSelector) {
    var img = jQuery('<img class="mapLogoMin" src="'+imgUrl +'" />');
    jQuery(containerNodeSelector).append(img);
}

/**
 * Clears all layers in the specified select element from the map.
 *
 * @param mapLayersSelectSelector
 *            selector for the select element that displays added layers.
 */
bdrs.map.clearAllLayers = function(mapLayersSelectSelector) {
    jQuery(mapLayersSelectSelector).children().each(function(index, element) {
        var jelem = jQuery(element);
        var layerId = jelem.val();
        var layer = bdrs.map.baseMap.getLayer(layerId);
        bdrs.map.baseMap.removeLayer(layer);
        jelem.remove();
    });
};

bdrs.map.clearAllVectorLayers = function(map) {
    var layers = bdrs.map.baseMap.getLayersByClass('OpenLayers.Layer.Vector');
    for(var i=0; i<layers.length; i++) {
		// persistentLayers must exist
		// persistentLayers must be an array
		// anything contained in persistent layers cannot be removed
		// if the duck punched persistentLayers property is missing then just remove the layer.
		if (map.persistentLayers && jQuery.isArray(map.persistentLayers)) {
			if (jQuery.inArray(layers[i], map.persistentLayers) == -1) {
				map.removeLayer(layers[i]);
			}
		} else {
			map.removeLayer(layers[i]);
		}
    }
};

/**
 * Removes a layer from the map.
 *
 * @param layerId
 *            the id of the layer to remove.
 */
bdrs.map.removeLayerById = function(layerId) {
    // Remove the layer
    var layer = bdrs.map.baseMap.getLayer(layerId);
    bdrs.map.baseMap.removeLayer(layer);
};

/**
 * Removes the selected layer in the specified selector from the map.
 *
 * @param mapLayersSelectSelector
 *            selector for the select element that displays added layers.
 */
bdrs.map.removeLayer = function(mapLayersSelectSelector) {
    var jelem = jQuery(mapLayersSelectSelector).find("option:selected");
    if(jelem.length === 0) {
        return;
    }
    jelem.remove();
};

/**
 * Triggers a KML download by navigating (window.document.location) to the KML
 * URL.
 *
 * @param formIdSelector
 *            selector for the form containing the query parameters.
 * @param mapLayersSelectSelector
 *            selector for the select element that displays added layers.
 */
bdrs.map.downloadKML = function(formIdSelector, mapLayersSelectSelector) {
    var jelem = jQuery(mapLayersSelectSelector).find("option:selected");
    if(jelem.length === 0) {
        // There is no map layers select option available. This is where the
        // previously used url would be saved. Return the kml
        // for the current form.
        var form = jQuery(formIdSelector);
        window.document.location = form.attr("action") + '?' + form.serialize();
    }
    else {
        window.document.location = jelem.data("kmlRequest");
    }
};

bdrs.map.addPositionLayer = function(layerName) {
    var layer = new OpenLayers.Layer.Vector(
        layerName, {
        styleMap: new OpenLayers.StyleMap({
            'strokeWidth': 2,
            'strokeOpacity': 1,
            'strokeColor': '#EE9900',

            'fillColor': '#EE9900',
            'fillOpacity': 0.6,

            'pointRadius': 4
        })
    });

    var map = bdrs.map.baseMap;
    map.addLayers([layer]);
    return layer;
};

bdrs.map.roundNumber = function(num, dec) {
	var result = Math.round(num*Math.pow(10,dec))/Math.pow(10,dec);
    return result;
};

bdrs.map.roundLatOrLon = function(latOrLong) {
	return bdrs.map.roundNumber(latOrLong, 6);
}

/**
 *
 */
bdrs.map.addSingleClickPositionLayer = function(layerName,
        latitudeInputSelector, longitudeInputSelector) {
    var layer = bdrs.map.addPositionLayer(layerName);

    var map = bdrs.map.baseMap;
    var updateLatLon = function(feature, pixel) {
        var lonLat = map.getLonLatFromViewPortPx(pixel);
        lonLat.transform(bdrs.map.GOOGLE_PROJECTION,
                         bdrs.map.WGS84_PROJECTION);
        jQuery(latitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lat)).trigger("change");
        jQuery(longitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lon)).trigger("change");
    };

    // Add a drag feature control to move features around.
    var dragFeature = new OpenLayers.Control.DragFeature(layer, {
        onDrag: updateLatLon,
        onComplete: updateLatLon
    });
    map.addControl(dragFeature);
    dragFeature.activate();

    var clickControl = new OpenLayers.Control.Click({
        handlerOptions: {
            "single": true,

            'featureLayerId': layer.id,
            'featureCount': 1,
            'latitudeInputSelector': latitudeInputSelector,
            'longitudeInputSelector': longitudeInputSelector
        }
    });
    map.addControl(clickControl);
    clickControl.activate();

    return layer;
};

bdrs.map.addMultiClickPositionLayer = function(layerName, featureAddedHandler, featureMovedHandler) {
    var layer = new OpenLayers.Layer.Vector(
        layerName, {
        styleMap: new OpenLayers.StyleMap({
            'strokeWidth': 2,
            'strokeOpacity': 1,
            'strokeColor': '#EE9900',

            'fillColor': '#EE9900',
            'fillOpacity': 0.6,

            'pointRadius': 4
        })
    });

    var map = bdrs.map.baseMap;
    map.addLayers([layer]);

    if(jQuery.isFunction(featureAddedHandler)) {
        layer.events.register('featureadded', null, function(event) {
            featureAddedHandler(event.feature);
        });
    }

    // Add a drag feature control to move features around.
    var dragFeature = new OpenLayers.Control.DragFeature(layer, {
        onDrag: featureMovedHandler,
        onComplete: featureMovedHandler
    });
    map.addControl(dragFeature);
    dragFeature.activate();

    var clickControl = new OpenLayers.Control.Click({
        handlerOptions: {
            "single": true,

            'featureLayerId': layer.id,
            'featureCount': 0,
            'latitudeInputSelector': null,
            'longitudeInputSelector': null
        }
    });
    map.addControl(clickControl);
    clickControl.activate();

    return layer;
};

bdrs.map.addFeaturePopUpHandler = function(event) {
    var control = bdrs.map.addFeatureClickPopup(event.layer);
    event.layer.selectPopUpControl = control;
};

bdrs.map.removeFeaturePopUpHandler = function(event) {
    delete event.layer.selectPopupControl;
};

bdrs.map.addFeatureClickPopup = function(layer) {
	var onPopupClose = function(evt) {
	    // 'this' is the popup.
	    selectControl.unselect(this.feature);
    };
	
	var onFeatureSelect = function(feature) {
        var selectedFeature = feature;
        var descObj = jQuery.parseJSON(feature.cluster[0].attributes.description);
		
		var descObjArray = new Array();
		for (var i=0; i<feature.cluster.length; ++i) {
			descObjArray.push(jQuery.parseJSON(feature.cluster[i].attributes.description));
		} 
		
		var popupOptions = {
			onPopupClose: onPopupClose,
			featureToBind: feature
		};
		
		bdrs.map.createFeaturePopup(bdrs.map.baseMap, feature.geometry.getBounds().getCenterLonLat(), descObjArray, popupOptions);
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

bdrs.map.createContentState = function(itemArray, popup, mapServerQueryManager) {
	for (var itemIndex=0; itemIndex<itemArray.length; ++itemIndex) {
	
		var item = itemArray[itemIndex];
				
        var tbody = jQuery("<tbody></tbody>");
		
		if (item.type == "record") {
			// record specific stuff
			var recordAttrKeys = ["census_method", "species", "common_name", "number", "notes", "habitat", "when", "behaviour"];
			
			var viewRecordRow = jQuery("<tr></tr>");
	        viewRecordRow.attr('colspan', '2');
	        var recordId = item["recordId"];
	        var surveyId = item["surveyId"];
	        var recordUrl = bdrs.contextPath + "/bdrs/user/surveyRenderRedirect.htm?surveyId=" + surveyId + "&recordId=" + recordId;
	        jQuery("<a>View&nbsp;Record</a>").attr('href', recordUrl).appendTo(viewRecordRow);
	        tbody.append(viewRecordRow);
			
			for (var i = 0; i < recordAttrKeys.length; i++) {
				var key = recordAttrKeys[i];
				var value;
				if (key === 'when') {
					value = new Date(parseInt(item[key], 10));
					value = bdrs.util.formatDate(value);
				}
				else 
					if (key === 'species' && item[key]) {
						value = jQuery("<i></i>").append(item[key]);
					}
					else {
						value = item[key];
					}
				if (value && value !== null && value.length > 0 && value !== '-1') {
					var row = jQuery("<tr></tr>");
					row.append(jQuery("<th></th>").css('whiteSpace', 'nowrap').append(titleCaps(key.replace("_", " ")) + ":"));
					row.append(jQuery("<td></td>").css('whiteSpace', 'nowrap').append(value));
					tbody.append(row);
				}
			}
		} else if (item.type = "geoMapFeature") {
			// Map feature specific stuff
			var mapFeatureTitle = jQuery("<tr></tr>");
            mapFeatureTitle.attr('colspan', '2');
            jQuery("<span>Map&nbsp;Feature<span>").appendTo(mapFeatureTitle);
            tbody.append(mapFeatureTitle);
		}

        var attrArray = item.attributes;
        for(var j=0; j<attrArray.length; j++) {
            var tuple = attrArray[j];
            for(var k in tuple) {
                if(tuple.hasOwnProperty(k)) {
                    var v = tuple[k];
					// if v is a number, change it to a string..
					if (v.toString) {
						v = v.toString();
					}
                    if(v !== null && v.length > 0 && v !== '-1') {
                        var r = jQuery("<tr></tr>");
                        r.append(jQuery("<th></th>").css('whiteSpace', 'nowrap').append(k+":"));
                        r.append(jQuery("<td></td>").css('whiteSpace', 'nowrap').append(v));
                        tbody.append(r);
                    }
                }
            }
        }
        var table = jQuery("<table></table>").width('100%').append(tbody);
        table.addClass("kmlDescriptionTable");
		var tableDiv = jQuery("<div></div>").append(table);
		tableDiv.addClass("popupPage" + itemIndex);
		// phwoar! duck punch onto the item object GO
		item.htmlContent = tableDiv;
	}
	
	var popupContent = jQuery("<div></div>").addClass("popupContent");

	for (var i=0; i<itemArray.length; ++i) {
		popupContent.append(itemArray[i].htmlContent);
	}
	
	popupContent.appendTo(popup.contentDiv);
	
	var result = {
		currentPage: 0,
		itemArray: itemArray,
		popup: popup,
		mapServerQueryManager: mapServerQueryManager
	};
	return result;
};

bdrs.map.getPopupLeftHandler = function(contentState) {
	var contentState = contentState;
	return function(){
		if (contentState.currentPage > 0) {
			contentState.currentPage -= 1;
			bdrs.map.displayPopupPage(contentState);
	    }
	};
};

bdrs.map.displayPopupPage = function(contentState) {
	jQuery(".currentPage", contentState.popup.contentDiv).text(contentState.currentPage + 1);
	// if a query manager was passed in, query the map server appropriately!
	if (contentState.mapServerQueryManager) {
		var qm = contentState.mapServerQueryManager;
		var item = contentState.itemArray[contentState.currentPage];
		qm.highlightFeature(item);
	}
	for (var i=0; i<contentState.itemArray.length; ++i) {
		jQuery(".popupPage" + i).hide();
	}
	jQuery(".popupPage"+contentState.currentPage).show();
	if (contentState.currentPage == contentState.itemArray.length - 1) {
       // hide the right arrow
       jQuery(".shiftContentRight").attr("hidden","hidden");
    } else {
        // show the right arrow
        jQuery(".shiftContentRight").removeAttr("hidden");
    }
    
    if (contentState.currentPage == 0) {
       // hide the left arrow
       jQuery(".shiftContentLeft").attr("hidden","hidden");
    } else {
        // show the left arrow
       jQuery(".shiftContentLeft").removeAttr("hidden");
    }
    
	contentState.popup.updateSize();
};

bdrs.map.getPopupRightHandler = function(contentState) {
	var contentState = contentState;
	return function() {
		if (contentState.currentPage < contentState.itemArray.length - 1) {
			contentState.currentPage += 1;
			bdrs.map.displayPopupPage(contentState);
		}
	};
};

//-----------------------------------------------
/**
 * Adds the Geocoder Control to the map
 *     Have hacked it in here...
 *	   Putting it in Openlayers.js (while making more sense) would
 *     mean we would forever have to maintain our custom OL build
 */
bdrs.map.addGeocodeControl = function(geocodeOptions) {
        // See bdrs.map.createDefaultMap for a description of geocode options.
        var defaultOptions = { useKeyHandler: true, zoom: 10 };
        var options = jQuery.extend(true, {}, defaultOptions, geocodeOptions);        

        if( options.selector === undefined || options.selector === null) {
            // Cannot do anything without a container for the geocode inputs
            return;
        }
        
        var geocodeContainer = jQuery(options.selector);
        if(geocodeContainer.length === 0) {
            // Cannot find the element so we can't add controls to it.
            return;
        }
        
        var inputGeocode = jQuery('<input type="text"></input>');
        inputGeocode.attr({
            'id': 'inputGeocode'
        });
        
        if(options.useKeyHandler !== false) {
	        inputGeocode.keydown(function(event) {
	            bdrs.map.geocode(options, jQuery("#inputGeocode").val());
	            if (event.keyCode == 13){
	                return false
	            };
	        });
        }
        
        var btnGeocode = jQuery('<input type="submit"></input>');
        btnGeocode.attr({
            'id': 'btnGeocode',
            'value': 'Find'
        });
        btnGeocode.addClass("form_action");
        btnGeocode.click(function() {
            bdrs.map.geocode(options, jQuery("#inputGeocode").val());
            return false;
        });
        
        var frmGeocode = jQuery("<form></form>");
        frmGeocode.attr({'id': 'geocodeForm'});
        frmGeocode.append(inputGeocode);
        frmGeocode.append(btnGeocode);
        geocodeContainer.append(frmGeocode);
}

//Takes a text string, sets map view to point with no return
bdrs.map.geocode = function(options, address) {
	if (address.length > 3) {
	   address = address + ', Australia';
	   var geocoder = new GClientGeocoder();
	   if (geocoder) {
	       geocoder.getLatLng(address, function(point) {
               if (!point) {
	           		//Set to Australia wide view
	           		bdrs.map.baseMap.setCenter(new OpenLayers.LonLat(130, -27).transform(
	            		bdrs.map.WGS84_PROJECTION,
	            		bdrs.map.GOOGLE_PROJECTION), 4);
	           } else {
	                var zoom = options.zoom < 0 ? bdrs.map.baseMap.getNumZoomLevels()-1 : options.zoom;
	           		// Jump to entered location and update lat/long
	           		bdrs.map.baseMap.setCenter(new OpenLayers.LonLat(point.x, point.y).transform(
	            		bdrs.map.WGS84_PROJECTION,
	            		bdrs.map.GOOGLE_PROJECTION), zoom);
	           }
	       });
	    }
	}
};

/**
 * The latLonToNameTimer is used to prevent sending too many requests to the
 * google server. The timer is restarted each time the function is invoked.
 */
bdrs.map.latLonToNameTimer = null;
/**
 * Retrieves the name of the lat lon coordinate.
 * 
 * @param latitude the latitude coordinate
 * @param longitude the longitude coordinate
 * @param callback the callback function. This function will receive a single
 * parameter which is the name of the location.
 */
bdrs.map.latLonToName = function(latitude, longitude, callback) {

    if(bdrs.map.latLonToNameTimer !== null) {
        clearTimeout(bdrs.map.latLonToNameTimer);
    }
    
    bdrs.map.latLonToNameTimer = setTimeout(function() {
	    new GClientGeocoder().getLocations(
	        new GLatLng(latitude,longitude), 
	        function(data) {
	            var name;
			    if(data !== null && data.Status.code === 200) {
			        if(data.Placemark.length > 0) {
	                    name = data.Placemark[0].address;
	                } else {
	                    name = data.name;
	                }
			    } else {
			      name = [latitude,longitude].join(', ');
			    }
			    callback(name);
	        }
	    );
    }, 500);
};

bdrs.map.centerProjectMap = function(map) {
	var arrayLocation = document.getElementById('location').options;
	
	if (arrayLocation.length > 1) {
		var locIds = [];
		for(var i=0; i<arrayLocation.length; i++) {
			locIds.push(arrayLocation[i].value);
		}
		
		jQuery.getJSON(bdrs.contextPath+"/webservice/location/getLocationsById.htm", {ids: JSON.stringify(locIds)}, function(data) {
			var wkt = new OpenLayers.Format.WKT();
			var feature = wkt.read(data.geometry);
			var bounds = feature.geometry.getBounds().transform(bdrs.map.WGS84_PROJECTION,
	                                   bdrs.map.GOOGLE_PROJECTION);
			var zoom = map.getZoomForExtent(bounds);
			map.setCenter(bounds.getCenterLonLat(), zoom);
		});
	} else {
	    map.setCenter(new OpenLayers.LonLat(136.5, -28.5).transform(
        	bdrs.map.WGS84_PROJECTION,
        	bdrs.map.GOOGLE_PROJECTION), 3);
	}
}

bdrs.map.lonlatChanged = function() {
        if (document.getElementById("latitude").value != '' && document.getElementById("longitude").value != '') {
                var layer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LAYER_NAME)[0];
                layer.removeFeatures(layer.features);
                
                var lonLat = new OpenLayers.LonLat(
                    document.getElementById('longitude').value, document.getElementById('latitude').value);
                lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
                                              bdrs.map.GOOGLE_PROJECTION);
                layer.addFeatures(new OpenLayers.Feature.Vector(
                    new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat)));
        }
}
//-----------------------------------------------

bdrs.map.initOpenLayers = function() {	
    bdrs.map.GOOGLE_PROJECTION = new OpenLayers.Projection('EPSG:900913');
    bdrs.map.WGS84_PROJECTION = new OpenLayers.Projection('EPSG:4326');
};

bdrs.map.initPointSelectClickHandler = function(map) {
    if (!map) {
        throw 'must pass in a map argument';
    }
	
	// --------------------------------------
    // Extension point for OpenLayers
    // --------------------------------------
    // Click handler for adding features (points) to the map.
    OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
        defaultHandlerOptions: {
             'single': true,
             'double': false,
             'pixelTolerance': 0,
             'stopSingle': false,
             'stopDouble': false,

             'featureLayerId': null,
             'featureCount': 0,
             'latitudeInputSelector': null,
             'longitudeInputSelector': null
         },

         initialize: function(options) {
             this.handlerOptions = OpenLayers.Util.extend(
                     {}, this.defaultHandlerOptions
             );
             OpenLayers.Control.prototype.initialize.apply(
                     this, arguments
             );
             this.handler = new OpenLayers.Handler.Click(
                     this, {
                         'click': this.onClick
                     }, this.handlerOptions
             );
         },

         onClick: function(evt) {

             // You didn't specify what layer to put the feature.
             if(this.handlerOptions.featureLayerId === null) {
                 return;
             }
             // Check for the number of features.
             var layer = map.getLayer(this.handlerOptions.featureLayerId);
             if(this.handlerOptions.featureCount > 0 &&
                     layer.features.length >= this.handlerOptions.featureCount) {
                 return;
             }

             var lonLat = map.getLonLatFromViewPortPx(evt.xy);
             var feature = new OpenLayers.Feature.Vector(
                     new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat)
             );

             lonLat.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
             // The extra blur is for ketchup
             if(this.handlerOptions.latitudeInputSelector !== null) {
                 jQuery(this.handlerOptions.latitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lat)).trigger("change").trigger("blur");
             }
             if(this.handlerOptions.longitudeInputSelector !== null) {
                 jQuery(this.handlerOptions.longitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lon)).trigger("change").trigger("blur");
             }

             if(layer !== null) {
                 layer.addFeatures(feature);
             }
         }
    });
};

bdrs.map.initAjaxFeatureLookupClickHandler = function(map, options) {
	if (!map) {
        throw 'must pass in a map argument';
    }

	if (options === undefined) {
        options = {};
    }
    var defaultOptions = {
    };
	
    options = jQuery.extend(defaultOptions, options);
	
	var mapServerQueryManager = new bdrs.map.MapServerQueryManager({
		wmsHighlightLayer: options.wmsHighlightLayer
	});
	
	var popupOptions = {
		mapServerQueryManager: mapServerQueryManager
	};

    // --------------------------------------
    // Extension point for OpenLayers
    // --------------------------------------
    // Click handler for adding features (points) to the map.
    OpenLayers.Control.Click = OpenLayers.Class(OpenLayers.Control, {
        defaultHandlerOptions: {
             'single': true,
             'double': false,
             'pixelTolerance': 0,
             'stopSingle': false,
             'stopDouble': false,
             'delay':100,
             'featureLayerId': null,
             'featureCount': 0,
             'latitudeInputSelector': null,
             'longitudeInputSelector': null
         },

         initialize: function(options) {
             this.handlerOptions = OpenLayers.Util.extend(
                     {}, this.defaultHandlerOptions
             );
             OpenLayers.Control.prototype.initialize.apply(
                     this, arguments
             );
             this.handler = new OpenLayers.Handler.Click(
                     this, {
                         'click': this.onClick
                     }, this.handlerOptions
             );
         },

         onClick: function(evt) {
			
            bdrs.map.clearPopups(map);
			
			mapServerQueryManager.unhighlightAll();
			
		 	 var googleProjectionLonLat = map.getLonLatFromViewPortPx(evt.xy);
             var lonLat = map.getLonLatFromViewPortPx(evt.xy);
             lonLat.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
			 var lat = bdrs.map.roundLatOrLon(lonLat.lat);
			 var lon = bdrs.map.roundLatOrLon(lonLat.lon);
			 
			 var mapLayerIds = new Array();
			 var visibleLayers = map.getLayersBy("visibility", true);
			 for (var i=0; i<visibleLayers.length; ++i) {
			     if (visibleLayers[i].bdrsLayerId) {
				 	mapLayerIds.push(visibleLayers[i].bdrsLayerId);
				 }
			 }
			 
			 // buffer based on current map zoom level...
			 // pixel tolerance based on the default point placemark - a circle
			 // of radius 4 pixels.
			 var bufferKm = bdrs.map.calcClickBufferKm(map, 4);

			 var ajaxParams = {
			 	latitude: lat,
                longitude: lon,
                buffer: bufferKm,
                mapLayerId: mapLayerIds
			 };
			 
            jQuery.ajax({
            url: bdrs.contextPath + '/bdrs/map/getFeatureInfo.htm',
				data: jQuery.param(ajaxParams, true),
                success: function(data, textStatus, jqXhr) {
					if (data.items && jQuery.isArray(data.items) && data.items.length > 0) {
						bdrs.map.createFeaturePopup(map, googleProjectionLonLat, data["items"], popupOptions);
					}
                },
				
				error: function(data) {
					bdrs.message.set("Error retrieving feature info");
				}
            });
         }
    });
	
	var clickControl = new OpenLayers.Control.Click();
    map.addControl(clickControl);
    clickControl.activate();
};

bdrs.map.clearPopups = function(map) {
    // Clear all popups
    while (map.popups.length > 0) {
        map.removePopup(map.popups[0]);
    }
};

bdrs.map.createFeaturePopup = function (map, googleProjectionLonLatPos, featureArray, options) {
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
    // only add the arrows if more than one total page
    jQuery('<img src="' + bdrs.contextPath + '/images/icons/left.png" />').addClass("shiftContentLeft left").appendTo(cyclerDiv);
    jQuery("<span></span>").addClass("currentPage").appendTo(cyclerDiv);
    jQuery("<span> of </span>").appendTo(cyclerDiv);
    jQuery("<span></span>").addClass("totalPages").appendTo(cyclerDiv);
    // only add the arrows if more than one total page
    jQuery('<img src="' + bdrs.contextPath + '/images/icons/right.png" />').addClass("shiftContentRight right").appendTo(cyclerDiv);

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

    // now the dom is recreated from our content string, add our handlers and content state
    var contentState = bdrs.map.createContentState(featureArray, popup, options.mapServerQueryManager);
    bdrs.map.displayPopupPage(contentState);
    jQuery(".shiftContentLeft", popup.contentDiv).click(bdrs.map.getPopupLeftHandler(contentState));
    jQuery(".shiftContentRight", popup.contentDiv).click(bdrs.map.getPopupRightHandler(contentState));
    jQuery(".totalPages", popup.contentDiv).text(contentState.itemArray.length);
	
	/*
	if (contentState.itemArray.length <= 1) {
	   // set the arrow images to hidden
	   jQuery(".shiftContentLeft").attr("hidden", "hidden");
       jQuery(".shiftContentRight").attr("hidden", "hidden");
	}
    */
	
	return popup;
};


//--------------------------------------
// Survey
//--------------------------------------

bdrs.survey = {};
bdrs.survey.location = {};





bdrs.initDatePicker = function() {
    // This blur prevents the ketchup validator from indicating the
    // field is required when it is already filled in
    var onSelectHandler = function(dateText, datepickerInstance) { jQuery(this).trigger('blur'); };
    var keyDown = function(){ return false; };
    // this function prevents the from date from being after the to date
    // and vice versa in the date picker
    var onSelectDateRangeHandler = function( selectedDate ) {
                var option = this.id == "from" ? "minDate" : "maxDate",
                    instance = $( this ).data( "datepicker" ),
                    date = $.datepicker.parseDate(
                        instance.settings.dateFormat ||
                        $.datepicker._defaults.dateFormat,
                        selectedDate, instance.settings );
                dates.not( this ).datepicker( "option", option, date );
                jQuery(this).trigger('blur');
            };
    
    jQuery(".datepicker").not(".hasDatepicker").datepicker({
        dateFormat: bdrs.dateFormat,
        onSelect: onSelectHandler
    }).keydown(keyDown);

    jQuery(".datepicker_historical").not(".hasDatepicker").datepicker({
        dateFormat: bdrs.dateFormat,
        onSelect: onSelectHandler,
        maxDate: new Date() // Cannot pick a date in the future
    }).keydown(keyDown);
    
    var dates = jQuery(".datepicker_range").not(".hasDatepicker").datepicker({
        dateFormat: bdrs.dateFormat,
        onSelect: onSelectDateRangeHandler
    });
};

bdrs.initColorPicker = function() {
	jQuery(".COLOR").ColorPicker({
	   onSubmit: function(hsb, hex, rgb, el) {
	       jQuery(el).val("#"+hex.toUpperCase());
	       jQuery(el).ColorPickerHide();
	   },
       onBeforeShow: function () {
	       jQuery(this).ColorPickerSetColor(this.value);
	   }
	}).bind('keyup', function(){
	   jQuery(this).ColorPickerSetColor(this.value);
	});
};

bdrs.init = function() {
    bdrs.initDatePicker();
    bdrs.initColorPicker();
    
    // Deferred_ketchup may be used if the form contains many inputs
    // such that using normal ketchup causes a large initial overhead
    // when loading the page.
    jQuery('[class*=deferred_ketchup]').blur(function(evt) {
        
        var elem = jQuery(evt.target);
        var klass_attr = elem.attr("class");
        var klass_split = klass_attr.split(" ");
        
        var i;
        var klass_name;
        var complete = false;
        for(i=0; i<klass_split.length && !complete;i++) {
            klass_name = klass_split[i];
            if(klass_name.indexOf("deferred_ketchup") === 0) {
                elem.attr("class", klass_attr.replace("deferred_ketchup", "validate"));
                elem.parents("form").ketchup();
                elem.trigger("blur");                
                complete = true;
            }
        }
    });    
};

/*
 * Title Caps
 *
 * Ported to JavaScript By John Resig - http://ejohn.org/ - 21 May 2008 Original
 * by John Gruber - http://daringfireball.net/ - 10 May 2008 License:
 * http://www.opensource.org/licenses/mit-license.php
 */

(function(){
    var small = "(a|an|and|as|at|but|by|en|for|if|in|of|on|or|the|to|v[.]?|via|vs[.]?)";
    var punct = "([!\"#$%&'()*+,./:;<=>?@[\\\\\\]^_`{|}~-]*)";

    this.titleCaps = function(title){
        var parts = [], split = /[:.;?!] |(?: |^)["Ò]/g, index = 0;

        while (true) {
            var m = split.exec(title);
            parts.push( title.substring(index, m ? m.index : title.length)
                .replace(/\b([A-Za-z][a-z.'Õ]*)\b/g, function(all){
                    return (/[A-Za-z]\.[A-Za-z]/).test(all) ? all : upper(all);
                })
                .replace(RegExp("\\b" + small + "\\b", "ig"), lower)
                .replace(RegExp("^" + punct + small + "\\b", "ig"), function(all, punct, word){
                    return punct + upper(word);
                })
                .replace(RegExp("\\b" + small + punct + "$", "ig"), upper));

            index = split.lastIndex;

            if ( m ) { parts.push( m[0] ); }
            else { break; }
        }

        return parts.join("").replace(/ V(s?)\. /ig, " v$1. ")
            .replace(/(['Õ])S\b/ig, "$1s")
            .replace(/\b(AT&T|Q&A)\b/ig, function(all){
                return all.toUpperCase();
            });
    };

    function lower(word){
        return word.toLowerCase();
    }

    function upper(word){
      return word.substr(0,1).toUpperCase() + word.substr(1);
    }
})();

/* jQuery.require plugin */
/**
* require is used for on demand loading of JavaScript
* 
* require r1 // 2008.02.05 // jQuery 1.2.2
* 
* // basic usage (just like .accordion) 
* $.require("comp1.js");
*
* @param  jsFiles string array or string holding the js file names to load
* @param  params object holding parameter like browserType, callback, cache
* @return The jQuery object
* @author Manish Shanker
*/

(function($){
    $.require = function(jsFiles, params) {
    
        var params = params || {}; 
        var bType = params.browserType===false?false:true;
        
        if (!bType){ 
          return $; 
        }
        
        var cBack = params.callBack || function(){}; 
        var eCache = params.cache===false?false:true;
        
        if (!$.require.loadedLib) $.require.loadedLib = {};
        
        if ( !$.scriptPath ) { 
            var path = $('script').attr('src'); 
            $.scriptPath = path.replace(/\w+\.js$/, '');
        } 
        if (typeof jsFiles === "string") { 
          jsFiles = new Array(jsFiles); 
        } 
        for (var n=0; n< jsFiles.length; n++) { 
            if (!$.require.loadedLib[jsFiles[n]])   {
                $.ajax({ 
                type: "GET", 
                url: $.scriptPath + jsFiles[n], 
                success: cBack, 
                dataType: "script", 
                cache: eCache, 
                async: false 
            });
            $.require.loadedLib[jsFiles[n]] = true; 
        }
    }   
    return $; 
    };
})(jQuery);

if (bdrs.require === undefined) {
	// we can overwrite this function in the test framework to change our path appropriately...
	bdrs.require = function(file){
		jQuery.require("/../" + file);
	};
}

/* Required files section */
bdrs.require("bdrs/map/MapServerQueryManager.js");
bdrs.require("bdrs/censusMethod.js");
bdrs.require("bdrs/menu.js");
bdrs.require("bdrs/message.js");
bdrs.require("bdrs/user.js");
bdrs.require("bdrs/util.js");
bdrs.require("bdrs/preferences.js");
bdrs.require("bdrs/contribute.js");
bdrs.require("bdrs/location.js");
bdrs.require("bdrs/attribute.js");
bdrs.require("bdrs/dnd.js");
bdrs.require("bdrs/map.js");