describe("testing map server query manager highlighting", function() {
	
	var MockLayer = function() {
		this.mergeNewParams = function(arg) {
		};
		this.setVisibility = function(arg) {
		};
	};
	
    it("should attempt to highlight layers", function() {
		var highlightLayer = new MockLayer();
		spyOn(highlightLayer, 'mergeNewParams').andReturn();

		var qm = new bdrs.map.MapServerQueryManager({
            wmsHighlightLayer: highlightLayer
        });
		
		var item = {
			type: "record",
			id: 12
		};
		qm.highlightFeature(item);
		
        expect(highlightLayer.mergeNewParams).toHaveBeenCalled();
		var params = highlightLayer.mergeNewParams.mostRecentCall.args[0];
		expect(params.RECORD_ID).toEqual(12);
		expect(params.GEO_MAP_FEATURE_ID).toEqual(0);
    });
	
	it("should unhighlight stuff", function() {
		var highlightLayer = new MockLayer();
        spyOn(highlightLayer, 'mergeNewParams').andReturn();
		spyOn(highlightLayer, 'setVisibility').andReturn();

        var qm = new bdrs.map.MapServerQueryManager({
            wmsHighlightLayer: highlightLayer
        });
        
        qm.unhighlightAll();
        
        expect(highlightLayer.setVisibility).toHaveBeenCalled();
        var visible = highlightLayer.setVisibility.mostRecentCall.args[0];
        expect(visible).toEqual(false);
		
		expect(highlightLayer.mergeNewParams).toHaveBeenCalled();
        var params = highlightLayer.mergeNewParams.mostRecentCall.args[0];
        expect(params.RECORD_ID).toEqual(0);
        expect(params.GEO_MAP_FEATURE_ID).toEqual(0);
	});
});