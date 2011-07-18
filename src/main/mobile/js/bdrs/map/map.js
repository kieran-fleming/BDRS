map = 0;
		
//--- Button events		
function ZoomIn() {
	map.zoomIn();
}
function ZoomOut() {
	map.zoomOut();
}
				
function mapLocation() {
	navigator.geolocation.getCurrentPosition(function(position) {       
		var lonLat = new OpenLayers.LonLat(position.coords.longitude,position.coords.latitude)
			.transform(new OpenLayers.Projection("EPSG:4326"), //transform from WGS 1984
				map.getProjectionObject() //to Spherical Mercator Projection
				);
			map.setCenter(lonLat, map.serverMaxZoom // Zoom level
		);
	});
}
		    	
function startBox() {
	startDrag();
}
	
function setup(type){
	map = setupMap(type);
	return true;
}
//---	

function setupMap(type) {
	if (map) {
		map.destroy()
	}
	
	//Set up the basic map
	map = new OpenLayers.Map('map', {
              controls: [
                 new OpenLayers.Control.Navigation(),
             ],
        		projection: new OpenLayers.Projection("EPSG:900913"),
				units: "m",
				displayProjection: new OpenLayers.Projection("EPSG:4326"),
				maxResolution: 156543.0339,
				maxExtent: new OpenLayers.Bounds(-20037508, -20037508, 20037508, 20037508.34),
				fractionalZoom: true,
				numZoomLevels: 25
    });
                
	var osmTiles = new OpenLayers.Layer.Tile("Open Street Map",
											 TileSource(), //Checks which tile source should be used
											 {serverMinZoom: 1, serverMaxZoom: 10});
	map.addLayer(osmTiles);
	
	OpenLayers.Util.onImageLoadError = function(){this.src='http://192.168.2.208/TMS/blanktile.png';}
	OpenLayers.Tile.Image.useBlankTile=false;
	
	var center = new OpenLayers.LonLat(116.25005735342, -32.227542340702);
	map.setCenter(center.transform(map.displayProjection, map.projection), 8);
	
	if (OpenLayers.Control.MultitouchNavigation) {
		var touchControl = new OpenLayers.Control.MultitouchNavigation();
		map.addControl(touchControl);
	}

	//Events for if manifest storage is being used
	var appCache = window.applicationCache;
	function handleCacheEvent1(e) {
  		alert('cached');
	}	
	function handleCacheEvent2(e) {
  		alert('checking');
	}		
	function handleCacheEvent3(e) {
  		alert('downloading');
	}		
	function handleCacheEvent4(e) {
  		alert('error');
	}		
	function handleCacheEvent5(e) {
  		alert('noupdate');
	}		
	function handleCacheEvent6(e) {
  		alert('obsolete');
	}	
	function handleCacheEvent7(e) {
  		alert('updateready');
	}	
	
	function handleCacheError(e) {
  		alert('Error: Cache failed to update!');
	};

	// Fired after the first cache of the manifest.
	appCache.addEventListener('cached', handleCacheEvent1, false);

	// Checking for an update. Always the first event fired in the sequence.
	appCache.addEventListener('checking', handleCacheEvent2, false);

	// An update was found. The browser is fetching resources.
	appCache.addEventListener('downloading', handleCacheEvent3, false);

	// The manifest returns 404 or 410, the download failed,
	// or the manifest changed while the download was in progress.
	appCache.addEventListener('error', handleCacheError, false);

	// Fired after the first download of the manifest.
	appCache.addEventListener('noupdate', handleCacheEvent5, false);

	// Fired if the manifest file returns a 404 or 410.
	// This results in the application cache being deleted.
	appCache.addEventListener('obsolete', handleCacheEvent6, false);

	// Fired when the manifest resources have been newly redownloaded.
	appCache.addEventListener('updateready', handleCacheEvent7, false);
	
	
	// Adds in drag box functionality for desktop browser
	vectors = new OpenLayers.Layer.Vector("Vector Layer", {
    	displayInLayerSwitcher: false
    });
    map.addLayer(vectors);
    
	box = new OpenLayers.Control.DrawFeature(vectors, OpenLayers.Handler.RegularPolygon, { 
    	handlerOptions: {
    		sides: 4,
    		snapAngle: 90,
        	irregular: true,
        	persist: true
      	}
   	});
    box.handler.callbacks.done = endDrag;
    map.addControl(box);
    
    polygonLayer = new OpenLayers.Layer.Vector(name, {styleMap: new
	OpenLayers.StyleMap({fillOpacity: 0.6, fillColor: '#88FF88'})});
	map.addLayer(polygonLayer);
    
    //Map build complete
    map.setCenter(center.transform(map.displayProjection, map.projection), 8);
    
    //Set tool button function
    if (type == "lonlat") {
    	document.getElementById('selectTool').onclick = function(){lonlat();}
    }
    
    //addGeocodeControl();
    
    return map;
}

function lonlat() {
	setTimeout("map.events.register('click', map, handleMapClick)",250);
}

function handleMapClick(e) {
   var lonlat = map.getLonLatFromViewPortPx(e.xy);
   lonlat.transform( map.projection,map.displayProjection);

   document.getElementById('record-longitude').value = lonlat.lon;
   document.getElementById('record-latitude').value = lonlat.lat;

   map.events.unregister('click', map, handleMapClick);
   map.destroy()
   window.history.back()
} 

//Function to set which tile source should be used
function TileSource() {
	if (supportsHTML5) {
			img = document.createElement("img");
			return 'local';
	} else {
		alert('Sorry this device does not support local storage');
		return "http://192.168.2.208/TMS";		
	}
}

//Function to check if the device supports local storage
function supportsHTML5() {
  try {
    return 'localStorage' in window && window['localStorage'] !== null;
  } catch (e) {
    return false;
  }
}

//Functions for tile selection for offline use
function startDrag() {
	clearBox();
	box.activate();
}
  			
function endDrag(bbox) {
	var bounds = bbox.getBounds();
	map.events.unregister("moveend", map, mapMoved);
	setBounds(bounds);
	drawBox(bounds);
	box.deactivate();
}
			
function mapMoved() {
	setBounds(map.getExtent());
	// validateControls();
}

function drawBox(bounds) {
	var feature = new OpenLayers.Feature.Vector(bounds.toGeometry());
	vectors.addFeatures(feature);
}
  			
function setBounds(bounds) {
	var epsg4326 = new OpenLayers.Projection("EPSG:4326");
	var decimals = Math.pow(10, Math.floor(map.getZoom() / 3));

	bounds = bounds.clone().transform(map.getProjectionObject(), epsg4326);
			
	map.events.unregister("moveend", map, mapMoved);
	
	//parent.lat(bounds.top);
	//parent.long(bounds.left);
				
	if (supportsHTML5) {
		PopulateLocal(bounds.bottom - 0,bounds.left - 0,bounds.top + 0,bounds.right + 0,1,10)
	} else {
		alert('No local storage support');
	}
}
  			
function clearBox() {
	vectors.destroyFeatures();
}
 			 			
function addBoxToMap(boxbounds) {
	if (!vectors) {
		// Be aware that IE requires Vector layers be initialised on page load, and not under deferred script conditions
		vectors = new OpenLayers.Layer.Vector("Boxes", {
			displayInLayerSwitcher: false
		});
		map.addLayer(vectors);
	}
	var geometry = boxbounds.toGeometry().transform(epsg4326, map.getProjectionObject());
	var box = new OpenLayers.Feature.Vector(geometry, {}, {});

	vectors.addFeatures(box);

	return box;
}
			
function removeBoxFromMap(box){
	vectors.removeFeature(box);
}