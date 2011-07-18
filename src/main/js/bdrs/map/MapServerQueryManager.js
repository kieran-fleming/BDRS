// MapServerQueryManager object
// maybe put some styling information in the ctor

bdrs.map.MapServerQueryManager = function(options) {
	if (!options) {
		options = {};
	}
	
	this.highlightLayer = options.wmsHighlightLayer;
	
	this.highlightFeature = function(item) {
		
		var recordId = 0;
		var gmfId = 0;
		
		if (item.type == "record") {
			recordId = item.id;
        } else if (item.type = "geoMapFeature") {
			gmfId = item.id;
        }
		
		var sld = bdrs.map.generateHeaderSLD();
		
		sld += bdrs.map.generatePolygonSLD({
			layerName: "geoMapFeature_polygon_highlight",
            userStyleName: "geoMapFeature_polygon_highlight",
            fillColor: "#FF0000",
            fillOpacity: "0.4",
            strokeColor: "#FF0000",
            strokeOpacity: "0.4",
            strokeWidth: "2"
		});
		
		sld += bdrs.map.generatePointSLD({
			layerName: "record_point_highlight",
            userStyleName: "record_point_highlight",
            fillColor: "#FF0000",
            strokeColor: "#000000",
            strokeWidth: "2",
            size: "10"
		});
		
		sld += bdrs.map.generatePointSLD({
            layerName: "geoMapFeature_point_highlight",
            userStyleName: "geoMapFeature_point_highlight",
            fillColor: "#FF0000",
            strokeColor: "#000000",
            strokeWidth: "2",
            size: "10"
        });
		
		sld += bdrs.map.generateFooterSLD();
		
		if (this.highlightLayer) {
            this.highlightLayer.mergeNewParams({
				"RECORD_ID": recordId,
				"GEO_MAP_FEATURE_ID": gmfId,
				"sld_body": sld
		    });
            this.highlightLayer.setVisibility(true);
		} 
	};
	
	this.unhighlightAll = function() {
		this.highlightLayer.setVisibility(false);
		this.highlightLayer.mergeNewParams({
			"RECORD_ID": 0,
            "GEO_MAP_FEATURE_ID": 0
	    });
	};
	
};
