/*global jQuery: false OpenLayers: false */

/**
 * @fileoverview BDRS Scripts. Depends on jQuery 1.4.2+.
 * @author Gaia Resources
 */
var bdrs = {};
bdrs.contextPath = "";
bdrs.ident = "";
bdrs.dateFormat = 'dd M yy';

bdrs.monthNames = [ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"];
bdrs.map = {};
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
// Base Map
// --------------------------------------
bdrs.map.baseMap = null;
bdrs.map.selectedRecord = null;
bdrs.map.initBaseMap = function(mapId) {
    bdrs.map.initOpenLayers();

    var options = { projection: bdrs.map.GOOGLE_PROJECTION,
                    displayProjection: bdrs.map.WGS84_PROJECTION,
                    units: 'm',
                    maxResolution: 156543.0339,
                    maxExtent: new OpenLayers.Bounds(-20037508.34, -20037508.34,
                                                     20037508.34, 20037508.34) };
    var map = new OpenLayers.Map(mapId, options);

    var gphy = new OpenLayers.Layer.Google(
        'Google Physical',
        {type: G_PHYSICAL_MAP, sphericalMercator: true}
    );
    var gmap = new OpenLayers.Layer.Google(
        'Google Streets', // the default
        {numZoomLevels: 20, sphericalMercator: true}
    );
    var ghyb = new OpenLayers.Layer.Google(
        'Google Hybrid',
        {type: G_HYBRID_MAP, numZoomLevels: 20, sphericalMercator: true}
    );
    var gsat = new OpenLayers.Layer.Google(
        'Google Satellite',
        {type: G_SATELLITE_MAP, numZoomLevels: 22, sphericalMercator: true}
    );

    map.addLayers([gphy, gmap, ghyb, gsat]);

    map.addControl(new OpenLayers.Control.LayerSwitcher());
    map.addControl(new OpenLayers.Control.MousePosition());
    addGeocodeControl();
    if (document.getElementById('OpenLayers_Control_MaximizeDiv_innerImage')) {
    	document.getElementById('OpenLayers_Control_MaximizeDiv_innerImage').setAttribute("title","Change the baselayer and turn map layers on or off");
    }
    if (document.getElementById("latitude")) {
	    var latText = document.getElementById("latitude");
	    latText.onchange = new Function('lonlatChanged()');
	        
	    var lonText = document.getElementById("longitude");
	    lonText.onchange = new Function('lonlatChanged()');
    }
           
    if (document.getElementById('location')) {
    	centerProjectMap();
    }
    else {
        map.setCenter(
        // Victoria
        // new OpenLayers.LonLat(144.266667, -31.75).transform(

        // Perth
        //new OpenLayers.LonLat(115.81, -32.0).transform(
        //    bdrs.map.WGS84_PROJECTION,
        //    bdrs.map.GOOGLE_PROJECTION), 10);
        new OpenLayers.LonLat(136.5, -28.5).transform(
            bdrs.map.WGS84_PROJECTION,
            bdrs.map.GOOGLE_PROJECTION), 4);
    }
    
	
	/*
	map.setCenter(
        // Victoria
        // new OpenLayers.LonLat(144.266667, -31.75).transform(

        // Perth
        //new OpenLayers.LonLat(115.81, -32.0).transform(
        //    bdrs.map.WGS84_PROJECTION,
        //    bdrs.map.GOOGLE_PROJECTION), 10);
        new OpenLayers.LonLat(136.5, -28.5).transform(
            bdrs.map.WGS84_PROJECTION,
            bdrs.map.GOOGLE_PROJECTION), 4);
            */

    bdrs.map.baseMap = map;
}; // End initBaseMap

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
    
    //var kmlURL = form.attr('action')+"?"+form.serialize();
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

bdrs.map.clearAllVectorLayers = function() {
    var layers = bdrs.map.baseMap.getLayersByClass('OpenLayers.Layer.Vector');
    for(var i=0; i<layers.length; i++) {
        bdrs.map.baseMap.removeLayer(layers[i]);
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
        jQuery(latitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lat));
        jQuery(longitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lon));
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
		
		// the final target....
		var content = jQuery("<span></span>");
		var cyclerDiv = jQuery("<div></div>").addClass("textcenter").width('100%').appendTo(content);
		jQuery('<img src="' + bdrs.contextPath + '/images/icons/left.png" />').addClass("shiftContentLeft left").appendTo(cyclerDiv);
		jQuery("<label></label>").addClass("currentPage").appendTo(cyclerDiv);
		jQuery("<label> of </label>").appendTo(cyclerDiv);
		jQuery("<label></label>").addClass("totalPages").appendTo(cyclerDiv);
        jQuery('<img src="' + bdrs.contextPath + '/images/icons/right.png" />').addClass("shiftContentRight right").appendTo(cyclerDiv);

        var popup = new OpenLayers.Popup.FramedCloud(feature.id,
                                 feature.geometry.getBounds().getCenterLonLat(),
                                 null,
                                 content.html(),
                                 null, true, onPopupClose);

        feature.popup = popup;
		// important so we can remove the feature later via onPopupClose
		popup.feature = feature;
        bdrs.map.baseMap.addPopup(popup);

        // now the dom is recreated from our content string, add our handlers and content state
        var contentState = bdrs.map.createContentState(descObjArray, popup);
        bdrs.map.displayPopupPage(contentState);
        jQuery(".shiftContentLeft", popup.contentDiv).click(bdrs.map.getPopupLeftHandler(contentState));
        jQuery(".shiftContentRight", popup.contentDiv).click(bdrs.map.getPopupRightHandler(contentState));
        jQuery(".totalPages", popup.contentDiv).text(contentState.contentArray.length);
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

bdrs.map.createContentState = function(descObjArray, popup) {
	var contentArray = new Array();
	for (var descObjIndex=0; descObjIndex<descObjArray.length; ++descObjIndex) {
		var descObj = descObjArray[descObjIndex];
		var recordAttrKeys = ["species", "common_name", "number", "notes", "habitat", "when", "behaviour"];
        var tbody = jQuery("<tbody></tbody>");
        for(var i=0; i<recordAttrKeys.length; i++) {
            var key = recordAttrKeys[i];
            var value;
            if(key === 'when') {
                value = new Date(parseInt(descObj[key],10));
                value = bdrs.util.formatDate(value);
            } else if(key === 'species') {
                // value = "<i>"+descObj[key]+"</i>";
                value = jQuery("<i></i>").append(descObj[key]);
            } else {
                value = descObj[key];
            }

            if(value !== null && value.length > 0 && value !== '-1') {
                var row = jQuery("<tr></tr>");
                row.append(jQuery("<th></th>").css('whiteSpace', 'nowrap').append(titleCaps(key.replace("_"," "))+":"));
                row.append(jQuery("<td></td>").css('whiteSpace', 'nowrap').append(value));
                tbody.append(row);
            }
        }

        var recordAttrArray = descObj.attributes;
        for(var j=0; j<recordAttrArray.length; j++) {
            var tuple = recordAttrArray[j];
            for(var k in tuple) {
                if(tuple.hasOwnProperty(k)) {
                    var v = tuple[k];
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
		table.addClass("popupPage" + descObjIndex);
		contentArray.push(table);
	}
	
	var popupContent = jQuery("<div></div>").addClass("popupContent");
	for (var i=0; i<contentArray.length; ++i) {
		popupContent.append(contentArray[i]);
	}
	
	popupContent.appendTo(popup.contentDiv);
	
	var result = {
		currentPage: 0,
		contentArray: contentArray,
		popup: popup
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
	// this doesn't do the resizing nicely!
	for (var i=0; i<contentState.contentArray.length; ++i) {
		jQuery(".popupPage" + i).hide();
	}
	jQuery(".popupPage"+contentState.currentPage).show();
    contentState.popup.updateSize();
};

bdrs.map.getPopupRightHandler = function(contentState) {
	var contentState = contentState;
	return function() {
		if (contentState.currentPage < contentState.contentArray.length - 1) {
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
function addGeocodeControl() {
        var divTag = document.createElement("div");
        divTag.id = "geocodeDiv";
        divTag.setAttribute("align","center");
        divTag.style.margin = "0px auto";
        divTag.style.position = "relative"
        divTag.style.zIndex = "1500";
        divTag.style.bottom = "45px";
        divTag.style.width = "500px";
        divTag.style.height = "20px";       
        document.getElementById("map_wrapper").appendChild(divTag);
        
        var frmGeocode = document.createElement("form");
        frmGeocode.id = "geocodeForm";
        document.getElementById("geocodeDiv").appendChild(frmGeocode);
        
        var inputGeocode = document.createElement("input");
        inputGeocode.id = "inputGeocode";
        inputGeocode.type = "text";
        inputGeocode.style.width = "75%";
        inputGeocode.style.height = "21px";
        inputGeocode.style.backgroundColor = "#E1E1E1";
        inputGeocode.style.borderColor = "#000000";
        inputGeocode.onkeydown = function() {geocode(document.getElementById("inputGeocode").value);if (event.keyCode == 13){return false};};
        document.getElementById("geocodeForm").appendChild(inputGeocode);
        
        var btnGeocode = document.createElement("input");
        btnGeocode.type = "submit";
        btnGeocode.id = "btnGeocode";
        btnGeocode.style.width = "80px";
        btnGeocode.style.height = "23px";
        btnGeocode.style.verticalAlign = "bottom";
        btnGeocode.value = "Find";
        btnGeocode.className = "form_action";
        btnGeocode.onclick = function() {geocode(document.getElementById("inputGeocode").value);return false;};
        document.getElementById("geocodeForm").appendChild(btnGeocode);
}

//Takes a text string, sets map view to point with no return
function geocode(address) {
	if (address.length > 3) {
	   address = address + ', Australia';
	   geocoder = new GClientGeocoder();
	   if (geocoder) {
	       geocoder.getLatLng(address,function(point) {
	       		if (!point) {
	           		//Set to Australia wide view
	           		bdrs.map.baseMap.setCenter(new OpenLayers.LonLat(130, -27).transform(
	            		bdrs.map.WGS84_PROJECTION,
	            		bdrs.map.GOOGLE_PROJECTION), 4);
	           } else {
	           		//Jump to entered location and update lat/long
	           		bdrs.map.baseMap.setCenter(new OpenLayers.LonLat(point.x, point.y).transform(
	            		bdrs.map.WGS84_PROJECTION,
	            		bdrs.map.GOOGLE_PROJECTION), 10);
	            	if (document.getElementById("longitude") != null) {
	            		document.getElementById("longitude").value = point.x;
	            		document.getElementById("latitude").value = point.y;
	            	}
	           }
	       });
	    }
	}
}

function centerProjectMap() {
	var arrLocation = document.getElementById('location').options;
	var arrLen = arrLocation.length;
	bounds = new OpenLayers.Bounds();
	for (var i=0,len=arrLen;i<len;i++) {
		if (arrLocation[i].value > -1) {
			jQuery.get(bdrs.contextPath+"/webservice/location/getLocationById.htm", {id: arrLocation[i].value}, function(data) {
				var wkt = new OpenLayers.Format.WKT();
				var feature = wkt.read(data.location);
		
				var lonLat = new OpenLayers.LonLat(feature.geometry.x, feature.geometry.y);
			    lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
	                                    bdrs.map.GOOGLE_PROJECTION);
				bounds.extend(lonLat);
				
				var zoom = bdrs.map.baseMap.getZoomForExtent(bounds);
				bdrs.map.baseMap.setCenter(bounds.getCenterLonLat(),zoom);
			});
		}
	}
}

function lonlatChanged() {
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
             var map = bdrs.map.baseMap;

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
             if(this.handlerOptions.latitudeInputSelector !== null) {
                 jQuery(this.handlerOptions.latitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lat));
             }
             if(this.handlerOptions.longitudeInputSelector !== null) {
                 jQuery(this.handlerOptions.longitudeInputSelector).val(bdrs.map.roundLatOrLon(lonLat.lon));
             }

             if(layer !== null) {
                 layer.addFeatures(feature);
             }
         }
    });
};

// Display message on browser in same style as JSP approach
bdrs.message = {
	set: function(msg) {
		this.clear();
		this.append(msg);
	},
	append: function(msg) {
		this.getDom().append('<p class="message">'+msg+'</p>');
	},
	clear: function() {
		this.getDom().empty();
	},
	getDom: function() {
		return jQuery(".messages");
	}
};


// --------------------------------------
// Users
// --------------------------------------
bdrs.user = {};

/**
 * Returns the representation of this users full name plus username in the form,
 *
 * LastName, FirstName (Username) - if both the first name and last name is not
 * null and non blank otherwise, just the username.
 *
 * @param user
 *            the javascript object representation of a User provided by the
 *            UserService (webservice).
 * @return the representation of the users full name plus username. If the
 *            object does not provide a username, this function will return
 *            null.
 */
bdrs.user.getLastFirstUsername = function(user) {
    if(user.name === undefined) {
        return null;
    }

    var firstName = user.firstName === undefined ? "" : user.firstName;
    var lastName = user.lastName === undefined ? "" : user.lastName;
    var name = user.name === undefined ? "" : user.name;

    var parts = [];
    if(firstName !== null && firstName.length > 0 &&
            lastName !== null && lastName.length > 0) {

        parts.push(lastName+',');
        parts.push(firstName);
        parts.push('('+name+')');
    } else {
        parts.push(name);
    }

    return parts.join(' ');
};

bdrs.util = {};
bdrs.util.file = {};

bdrs.util.file.IMAGE_FILE_EXTENSIONS = ['jpg','png','gif','jpeg'];

/**
 * Ensures that only image files can be added by inspecting the file extension
 * of the input value.
 * @param element the file input element.
 */
bdrs.util.file.imageFileUploadChangeHandler = function(element) {
    element = jQuery(element);
    var val = element.val();
    if(val !== undefined && val !== null && val.length > 0) {
        var ext = val.substr(val.lastIndexOf('.')+1, val.length).toLowerCase();

        // Put image types in the order of likelihood of showing up
        var IMAGE_TYPES = bdrs.util.file.IMAGE_FILE_EXTENSIONS;
        var imageType;
        var isValid = false;
        for(var i=0; i<IMAGE_TYPES.length; i++) {
            imageType = IMAGE_TYPES[i];
            if(ext === imageType) {
                isValid = true;
            }
        }

        if(isValid === false) {
            element.val(null);
        }
    }
    element.trigger("blur");
};

//--------------------------------------
// Survey
//--------------------------------------

bdrs.survey = {};
bdrs.survey.location = {};

//--------------------------------------
// Table Drag and Drop
//--------------------------------------

bdrs.dnd = {};

/**
 * Drop handler that is invoked when a row in the attribute table is reordered.
 * @param {element} table the table that contains the dropped row.
 * @param {element} row the row that was dropped.
 */
bdrs.dnd.tableDnDDropHandler = function(table, row) {
    var rows = table.tBodies[0].rows;
    for(var row_index=0; row_index<rows.length; row_index++) {
        var j_row = jQuery(rows[row_index]);
        j_row.find("input.sort_weight").val(row_index * 100);
    }
    
    jQuery(row).effect("highlight", {}, "normal");
};

/**
 * Attaches drag and drop event handling to the attribute table.
 */
bdrs.dnd.attachTableDnD = function(attributeTableSelector) {
    jQuery(attributeTableSelector).tableDnD({
        onDragClass: "drag_row",
        onDrop: bdrs.dnd.tableDnDDropHandler
    });
};

//--------------------------------------
// Attributes
//--------------------------------------

bdrs.attribute = {};

bdrs.attribute.addAttributeCount = 1;

/**
 * Adds a row to the attribute table.
 */
bdrs.attribute.addAttributeRow = function(tableSelector, showScope, isTag) {
	var index = bdrs.attribute.addAttributeCount++;

    jQuery.get(bdrs.contextPath+'/bdrs/admin/attribute/ajaxAddAttribute.htm',
            {'index': index, 'showScope': showScope, 'isTag': isTag}, function(data) {

        var table = jQuery(tableSelector); 
        var row = jQuery(data);
        table.find("tbody").append(row);
        bdrs.dnd.attachTableDnD('#attribute_input_table');
        bdrs.dnd.tableDnDDropHandler(table[0], row[0]); 
        jQuery('form').ketchup();
    });
};

/**
 * Enables or disables the element(s) specified by the selector.
 * @param enableOption the enabled/disabled state
 * @param optionSelector jQuery selector for elements to modify.
 */
bdrs.attribute.enableOptionInput = function(enableOption, optionSelector) {
    var elem = jQuery(optionSelector);
    if(enableOption) {
        elem.removeAttr("disabled");
    } else {
        elem.attr("disabled", "disabled");
    }
};

//--------------------------------------
// Location
//--------------------------------------
bdrs.location = {};
bdrs.location.initLocationMapAndTable = function() {
    var layerName = 'Location Layer';
    bdrs.map.initBaseMap('base_map');
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
        var params = {'index': index};

        jQuery.get(bdrs.contextPath+'/bdrs/location/ajaxAddLocationRow.htm', params, function(data) {
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

//--------------------------------------
// Contribute
//--------------------------------------
bdrs.contribute = {};

//--------------------------------------
// Yearly Sightings
//--------------------------------------
bdrs.contribute.yearlysightings = {};

bdrs.contribute.yearlysightings.init = function() {
    var form = jQuery('form');
    form.submit(bdrs.contribute.yearlysightings.submitHandler);

    var locationSelect = jQuery("#location");
    locationSelect.change(bdrs.contribute.yearlysightings.locationSelected);

    var sightingCells = jQuery(".sightingCell");
    sightingCells.change(bdrs.contribute.yearlysightings.validateCellChange);
    sightingCells.blur(bdrs.contribute.yearlysightings.validateCellChange);
};

bdrs.contribute.yearlysightings.submitHandler = function(event) {
    var form = jQuery(event.currentTarget);
    return form.find(".errorCell").length === 0;
};

bdrs.contribute.yearlysightings.validateCellChange = function(event) {
    var inp = jQuery(event.currentTarget);
    var cell = inp.parent("td");
    
    var isValid = true;    
    if(/^\d+$/.test(inp.val()) || (inp.val().length === 0)) {
        isValid = parseInt(inp.val(),10) < 1000000;
    } else {
        isValid = false;
    }
    
    if(isValid) {
        cell.removeClass("errorCell");
        var date = new Date(parseInt(inp.attr("name").split("_")[1], 10));
        inp.attr("title", bdrs.util.formatDate(date));
    } else {
        cell.addClass("errorCell");
        inp.attr("title", "Must be a positive integer or blank");
    }
};

bdrs.contribute.yearlysightings.insertRecordAttribute = function(recAttr) {
    var inp = jQuery("[name=attribute_"+recAttr.attribute+"]");
    inp.val(recAttr.stringValue);
    
    // Repopulate files
    var fileInput = jQuery("#attribute_file_"+recAttr.attribute);
    if(fileInput.length > 0) {
        var fileUrl = bdrs.contextPath+"/files/download.htm?"+recAttr.fileURL;
        if(fileInput.hasClass("image_file")) {
            // Images
            var img = jQuery("<img/>");
            img.attr({
                width: 250,
                src: fileUrl,
                alt: "Missing Image"
            });
            
            var imgAnchor = jQuery("<a></a>");
            imgAnchor.attr("href", fileUrl);
            
            var imgContainer = jQuery("<div></div>");
            imgContainer.attr("id", "attribute_img_"+recAttr.attribute);
            
            imgContainer.append(imgAnchor);
            imgAnchor.append(img);
            
            inp.parent().before(imgContainer);
        }
        else if(fileInput.hasClass("data_file")) {
            
            // Data
            var dataAnchor = jQuery("<a></a>");
            dataAnchor.attr("href", fileUrl);
            dataAnchor.text(recAttr.stringValue);
            
            var dataContainer = jQuery("<div></div>");
            dataContainer.attr("id", "attribute_data_"+recAttr.attribute);
            
            dataContainer.append(dataAnchor);
            inp.parent().before(dataContainer);                                                                         
        }
    } // End file repopulation
};

bdrs.contribute.yearlysightings.locationSelected = function(event) {

    var selectLocation = jQuery(event.currentTarget);
    var location = jQuery("[name=locationId]");
    var ans;
    if(location.val().length > 0) {
        ans = confirm("Changing the location will replace the data below. Do you wish to proceed?");
    }
    else {
        ans = true;
    }

    if(ans) {
        location.val(selectLocation.val());

        // Clear all cells
        jQuery(".sightingCell").each(function(index, element){
            var inp = jQuery(element);
            var cell = inp.parents("td");
            inp.val('');
            cell.removeClass("errorCell");
            var date = new Date(parseInt(inp.attr("name").split("_")[1],10));
            inp.attr("title", bdrs.util.formatDate(date));
        });
        
        // Clear the survey scope attributes
        jQuery("[name^=attribute_]").val('');
        jQuery("[id^=attribute_img_], [id^=attribute_data_]").remove();

        if(selectLocation.val().length > 0) {
            var param = {
                locationId: selectLocation.val(),
                surveyId: jQuery('#surveyId').val(),
                ident: jQuery('#ident').val()
            };
            jQuery.getJSON(bdrs.contextPath+'/webservice/record/getRecordsForLocation.htm', param, function(data) {
                var rec;
                for(var i=0; i<data.length; i++) {
                    rec = data[i];
                    if(rec.number !== null) {
                        jQuery("[name=date_"+rec.when+"]").val(rec.number);
                    }
                }
                
                if(typeof(rec) !== 'undefined') {
                    // Use the last survey as the prototype to load the 
                    // Survey scoped attributes
                    for(var j=0; j<rec.attributes.length; j++) {
                        var param = {
                            recordAttributeId: rec.attributes[j],
                            ident: jQuery('#ident').val()
                        };
                        jQuery.getJSON(bdrs.contextPath+"/webservice/record/getRecordAttributeById.htm", param, bdrs.contribute.yearlysightings.insertRecordAttribute);
                    } // End for-loop request for survey scope attributes
                }
            });
        } // End location update
    } // End confirm dialog return check
    else {
        selectLocation.val(location.val());
    }
};

// End of Yearly Sightings -------------
// -------------------------------------

//--------------------------------------
// Single Site Multiple Taxa
//--------------------------------------
bdrs.contribute.singleSiteMultiTaxa = {};

bdrs.contribute.singleSiteMultiTaxa.speciesSearchSource = function(request, callback) {

    var params = {};
    params.q = request.term;
    params.surveyId = jQuery(this.element).data("surveyId");
    jQuery.getJSON(bdrs.contextPath+'/webservice/survey/speciesForSurvey.htm', params, function(data, textStatus) {
        var label;
        var result;
        var taxon;
        var resultsArray = [];
        for(var i=0; i<data.length; i++) {
            taxon = data[i];

            label = [];
            if(taxon.scientificName !== undefined && taxon.scientificName.length > 0) {
                label.push("<b><i>"+taxon.scientificName+"</b></i>");
            }
            if(taxon.commonName !== undefined && taxon.commonName.length > 0) {
                label.push(taxon.commonName);
            }

            label = label.join(' ');

            resultsArray.push({
                label: label,
                value: taxon.scientificName,
                data: taxon
            });
        }

        callback(resultsArray);
    });
};

bdrs.contribute.singleSiteMultiTaxa.addSighting = function(sightingIndexSelector, surveyIdSelector, sightingTableBody) {
    var sightingIndexElem = jQuery(sightingIndexSelector);
    var sightingIndex = parseInt(sightingIndexElem.val(), 10);
    sightingIndexElem.val(sightingIndex+1);
    
    var surveyId = jQuery(surveyIdSelector).val();
    
    var url = bdrs.contextPath+"/bdrs/user/singleSiteMultiTaxa/sightingRow.htm";
    var param = {
        sightingIndex: sightingIndex,
        surveyId: surveyId
    };
    jQuery.get(url, param, function(data) {
        jQuery(sightingTableBody).append(data);
        
        // Attach the autocomplete
        var search_elem = jQuery("[name="+sightingIndex+"_survey_species_search]");
        search_elem.data("surveyId", surveyId); 
        search_elem.autocomplete({
            source: bdrs.contribute.singleSiteMultiTaxa.speciesSearchSource,
            select: function(event, ui) {
                var taxon = ui.item.data;
                jQuery("[name="+sightingIndex+"_species]").val(taxon.id);
            },
            html: true,
            minLength: 2,
            delay: 300
        });
        
        // Attach the datepickers
        bdrs.initDatePicker();
        search_elem.parents("tr").ketchup();
    });
};

// Single Site Multiple Taxa -----------
// -------------------------------------

//--------------------------------------
// Preferences
//--------------------------------------

bdrs.preferences = {};
bdrs.preferences.addPreferenceRow = function(categoryId, indexSelector, tableSelector) {
    var indexElem = jQuery(indexSelector);
    var index = parseInt(indexElem.val(),10);
    indexElem.val(index+1);
    
    var params = {
        categoryId: categoryId,
        index: index
    };
    jQuery.get(bdrs.contextPath+"/bdrs/admin/preference/ajaxAddPreferenceRow.htm", params, function(data) {
        jQuery(tableSelector).find("tbody").append(data);
        jQuery('form').ketchup();
    });
};

// Preferences -------------------------
// -------------------------------------

/**
 * Displays a confirmation dialog with the specified message. If the user
 * confirms the action, the specified form is submitted.
 *
 * @param message the message to be displayed in the confirmation dialog.
 * @param formSelector the jQuery selector to the form to be submitted if the 
 * user confirms the action.
 */
bdrs.util.confirmSubmit = function(message, formSelector) {
    var confirmation = confirm(message);
    if (confirmation) {
        jQuery(formSelector).submit();
    } 
};

bdrs.util.confirmExec = function(message, callback) {
    var confirmation = confirm(message);
    if(confirmation) {
        callback();
    }
};

/**
 * Formats a javascript Date object as a string dd MMM yyyy.
 *
 * @param a javascript Date object
 * @param a string representation of the Date object.
 */
bdrs.util.formatDate = function(date) {
    if(date === undefined || date === null) {
        return '';
    } else {
        return bdrs.util.zerofill(date.getDate(), 2) + " " + bdrs.monthNames[date.getMonth()] + " " +
            date.getFullYear();
    }
};

bdrs.util.zerofill = function(number, width) {
    width -= number.toString().length;
    if (width>0){
        var pattern = /\./;
        // Done this way because jslint does not like new Array();
        var array = [];
        array.length = width + (pattern.test( number ) ? 2 : 1);
        return array.join( '0' ) + number;
    }
    return number;
};

bdrs.util.formToMap = function(formSelector) {
    var data = {};
    var form = jQuery(formSelector);
    var inputs = form.find("select, input");

    var name;
    var inp;
    for(var i=0; i<inputs.length; i++) {
        inp = jQuery(inputs[i]);
        name = inp.attr("name");
        if(name !== undefined && name !== null && name.length > 0) {
            if(data.name === undefined) {
                data[name] = inp.val();
            } else if(!jQuery.isArray(data.name)){
                data[name] = [data[name], inp.val()];
            } else {
                data[name].push(inp.val());
            }
        }
    }
    return data;
};

/**
 * Event handler attached to key press events, this function prevents the
 * user from submitting the form by pressing the return key.
 */
bdrs.util.preventReturnKeySubmit = function(event) {
    var key;
    if(window.event) {
        //IE
        key = window.event.keyCode; 
    } else {
        //firefox
        key = e.which; 
    }      

     return (key != 13);
}

/**
 * Maximise the amount of screen real-estate provided to an element by wrapping
 * it in a div and making that div fill the entire screen.
 * 
 * @param triggerSelector the element that triggers the maximise/minimise state.
 * @param contentSelector the content that shall be displayed maximised or minimised.
 * @param maximiseLabel the label to indicate a maximise action.
 * @param minimiseLabel the label to indicate a minimise action.
 */
bdrs.util.maximise = function(triggerSelector, contentSelector, maximiseLabel, minimiseLabel) {
    var content = jQuery(contentSelector);
    var trigger = jQuery(triggerSelector);
    if(content.hasClass("maximise")) {
        content.unwrap();
        content.removeClass("maximise");
        trigger.text(maximiseLabel);
    } else {
        var wrapper = content.wrap('<div></div>').parent();
        wrapper.css({
            position: 'fixed',
            top: 0,
            bottom: 0,
            left: 0,
            right: 0,
            padding: "2em 2em 2em 2em",
            backgroundColor: 'white',
            zIndex: 1500
        });
        content.addClass("maximise");
        trigger.text(minimiseLabel);
    }
};

bdrs.initDatePicker = function() {
    // This blur prevents the ketchup validator from indicating the
    // field is required when it is already filled in
    var onSelectHandler = function(dateText, datepickerInstance) { jQuery(this).trigger('blur'); };
    var keyDown = function(){ return false; };
    
    jQuery(".datepicker").not(".hasDatepicker").datepicker({
        dateFormat: bdrs.dateFormat,
        onSelect: onSelectHandler
    }).keydown(keyDown);

    jQuery(".datepicker_historical").not(".hasDatepicker").datepicker({
        dateFormat: bdrs.dateFormat,
        onSelect: onSelectHandler,
        maxDate: new Date() // Cannot pick a date in the future
    }).keydown(keyDown);
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
