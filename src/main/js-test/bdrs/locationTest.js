describe("testing location fsm", function() {
	
	// mock row for testing highlighting
	var createMockRow = function() {
		var row = jQuery('<tr><td class="loc_latitude"></td><td class="loc_longitude"></td><td class="loc_wkt"></td></tr>')
		return row;
	};
	
	// simplified select control.
	var MockSelectControl = function(layer) {
		this.selectArg = null;
		this.unselectArg = null;
		this.layer = layer;
		
		this.select = function(feature) {
			this.selectArg = feature;
			this.layer.selectedFeatures.push(feature);
			
		}
		this.unselect = function(feature) {
			this.unselectArg = feature;
			this.layer.selectedFeatures.splice(0, this.layer.selectedFeatures.length);
		}
	};
	
	// simplified layer
	var MockLayer = function() {
		this.selectedFeatures = new Array();
		this[bdrs.location.SELECT_CONTROL_KEY] = new MockSelectControl(this);
	}
	
	// simplified feature
	var MockFeature = function(layer) {
		if (!layer) {
			throw 'MockFeature needs a MockLayer';
		}
        this.layer = layer;
    };
	
    it("should work for feature selection/deselection", function() {
		var layer = new MockLayer();
		var feature = new MockFeature(layer);
		var row = createMockRow();
		var location = new bdrs.location.LocationUpdateMediator(feature, row);
		expect(location.fsm.current).toEqual("notSelected");
		
		location.fsm.selectFeature();
		
		expect(location.fsm.current).toEqual("selected");
		
		expect(row.hasClass(bdrs.location.ROW_HIGHLIGHT_CLASS)).toBeTruthy();
		
		location.fsm.deselectFeature();
		
		expect(location.fsm.current).toEqual("notSelected");
		
		expect(row.hasClass(bdrs.location.ROW_HIGHLIGHT_CLASS)).toBeFalsy();
    });
	
	it("should work for row selection/deselection", function() {
		var layer = new MockLayer();
		var feature = new MockFeature(layer);
        var row = createMockRow();
        var location = new bdrs.location.LocationUpdateMediator(feature, row);
        
		expect(location.fsm.current).toEqual("notSelected");
        
		var selectControl = bdrs.location.getSelectControl(layer);
		
		// at this point we expect the selectArg to be null
		expect(selectControl.selectArg).toBeNull();
		
        location.fsm.rowFocus();
		
		expect(location.fsm.current).toEqual("selectingFeature");
		
		expect(selectControl.selectArg).toEqual(feature);
		
		// trigger the selectFeature event to signal the feature has successfully been selected
		// on the map
		location.fsm.selectFeature();
		
		expect(location.fsm.current).toEqual("selected");
        
        expect(row.hasClass(bdrs.location.ROW_HIGHLIGHT_CLASS)).toBeTruthy();
		
		expect(selectControl.unselectArg).toBeNull();
		
		location.fsm.rowBlur();
		
		expect(location.fsm.current).toEqual("deselectingFeature");
		
		expect(selectControl.unselectArg).toEqual(feature);
		
		location.fsm.deselectFeature();
		
		expect(location.fsm.current).toEqual("notSelected");
		
		expect(row.hasClass(bdrs.location.ROW_HIGHLIGHT_CLASS)).toBeFalsy();
	});
});