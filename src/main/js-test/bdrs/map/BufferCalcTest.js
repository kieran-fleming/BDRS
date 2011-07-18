describe("test click buffer calculation", function() {
    // hooray for easy mocking in js
    var MockMap = function(scale) {
        this.scale = scale;
        this.getScale = function() {
            return this.scale;
        };
    };
	
	it("should calc the buffer as expected", function() {
		var map = new MockMap(5000000);
		var pixelTolerance = 4;
		
		var expectedValue = pixelTolerance*(1/1000)*OpenLayers.METERS_PER_INCH*map.getScale()/OpenLayers.DOTS_PER_INCH;
		//OpenLayers.DOTS_PER_INCH
		expect(bdrs.map.calcClickBufferKm(map, 4)).toEqualFloat(expectedValue, 0.05);
    });
	
	it("should use the default value of 4", function() {
		var map = new MockMap(5000000);
        var pixelTolerance = 4;
        
        var expectedValue = pixelTolerance*(1/1000)*OpenLayers.METERS_PER_INCH*map.getScale()/OpenLayers.DOTS_PER_INCH;
        //OpenLayers.DOTS_PER_INCH
        expect(bdrs.map.calcClickBufferKm(map)).toEqualFloat(expectedValue, 0.05);
	});
});