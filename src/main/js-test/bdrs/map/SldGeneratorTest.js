describe("test SLD generator", function() {
    
	it("generates SLD for a polygon layer", function() {
		var sld = '<sld:NamedLayer><sld:Name>geoMapFeature_polygon_highlight</sld:Name><sld:UserStyle><sld:Name>geoMapFeature_polygon_highlight</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
        sld+= '<sld:PolygonSymbolizer><sld:Fill><sld:CssParameter name="fill">#FF0000</sld:CssParameter><sld:CssParameter name="fill-opacity">0.4</sld:CssParameter></sld:Fill>';
        sld+= '<sld:Stroke><sld:CssParameter name="stroke">#FF0000</sld:CssParameter><sld:CssParameter name="stroke-opacity">0.4</sld:CssParameter><sld:CssParameter name="stroke-width">2</sld:CssParameter></sld:Stroke></sld:PolygonSymbolizer>';
        sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>';
		
		var options = {
			layerName: "geoMapFeature_polygon_highlight",
            userStyleName: "geoMapFeature_polygon_highlight",
            fillColor: "#FF0000",
            fillOpacity: "0.4",
            strokeColor: "#FF0000",
            strokeOpacity: "0.4",
            strokeWidth: "2"
		};
		
		var generatedSld = bdrs.map.generatePolygonSLD(options);
        expect(generatedSld).toEqual(sld);
    });
	
	// for now will always produce a circle
	it ("generates SLD for a point layer", function() {
		var sld = '<sld:NamedLayer><sld:Name>record_point_highlight</sld:Name><sld:UserStyle><sld:Name>record_point_highlight</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
        sld+= '<sld:PointSymbolizer><sld:Graphic><sld:Mark><sld:WellKnownName>circle</sld:WellKnownName><sld:Fill><sld:CssParameter name="fill">#FF0000</sld:CssParameter></sld:Fill><sld:Stroke><sld:CssParameter name="stroke">#000000</sld:CssParameter><sld:CssParameter name="stroke-width">2</sld:CssParameter></sld:Stroke></sld:Mark><sld:Size>10</sld:Size></sld:Graphic></sld:PointSymbolizer>';
        sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>'
		
		var options = {
			layerName: "record_point_highlight",
            userStyleName: "record_point_highlight",
            fillColor: "#FF0000",
            strokeColor: "#000000",
            strokeWidth: "2",
			size: "10"
		};
		
		var generatedSld = bdrs.map.generatePointSLD(options);
		expect(generatedSld).toEqual(sld);
	});
	
	it ("generates SLD for a line layer", function() {
		var sld = '<sld:NamedLayer><sld:Name>my_layer</sld:Name><sld:UserStyle><sld:Name>my_layer_name</sld:Name><sld:FeatureTypeStyle><sld:Rule>';
        sld+= '<sld:LineSymbolizer><sld:Stroke><sld:CssParameter name="stroke">#AA0000</sld:CssParameter><sld:CssParameter name="stroke-width">5</sld:CssParameter></sld:Stroke></sld:LineSymbolizer>';
        sld+= '</sld:Rule></sld:FeatureTypeStyle></sld:UserStyle></sld:NamedLayer>'
		
		var options = {
            layerName: "my_layer",
            userStyleName: "my_layer_name",
            strokeColor: "#AA0000",
            strokeWidth: "5"
        };
        
        var generatedSld = bdrs.map.generateLineSLD(options);
        expect(generatedSld).toEqual(sld);
	});
});



