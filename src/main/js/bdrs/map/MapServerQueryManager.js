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
            fillColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            fillOpacity: "0.2",
            strokeColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            strokeOpacity: "0.2",
            strokeWidth: "2"
		});
		
		sld += bdrs.map.generatePointSLD({
			layerName: "record_point_highlight",
            userStyleName: "record_point_highlight",
            fillColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            strokeColor: "#000000",
            strokeWidth: "2",
            size: "10"
		});
		
		sld += bdrs.map.generateLineSLD({
			layerName: "record_line_highlight",
			userStyleName: "record_line_highlight",
            strokeColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            strokeWidth: "2"
		});
		
		sld += bdrs.map.generatePolygonSLD({
			layerName: "record_polygon_highlight",
            userStyleName: "record_polygon_highlight",
            fillColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            fillOpacity: "0.2",
            strokeColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            strokeOpacity: "0.2",
            strokeWidth: "2"
		});
		
		sld += bdrs.map.generatePointSLD({
            layerName: "geoMapFeature_point_highlight",
            userStyleName: "geoMapFeature_point_highlight",
            fillColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            strokeColor: "#000000",
            strokeWidth: "2",
            size: "10"
        });
		
		sld += bdrs.map.generateLineSLD({
            layeName: "geoMapFeature_line_highlight",
            userStyleName: "geoMapFeature_line_highlight",
            strokeColor: bdrs.map.DEFAULT_HIGHLIGHT_COLOR,
            strokeWidth: "2"
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
