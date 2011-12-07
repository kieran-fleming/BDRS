//--------------------------------------
// Location
//--------------------------------------
bdrs.location = {};

// CONSTANTS

// key used by $.data to store the feature associated with a location
// table row
bdrs.location.FEATURE_KEY = "olFeature";

// key used by $.data to store the update mediator. Also the key used to
// duckpunch the mediator into open layer feature objects 
bdrs.location.UPDATE_MEDIATOR_KEY = "updateMediator";

// key used to find the select control for the kml layer.
bdrs.location.SELECT_CONTROL_KEY = "selectControl";

// class applied when a row is focused
bdrs.location.ROW_HIGHLIGHT_CLASS = "bdrsHighlight";

bdrs.location.initLocationMapAndTable = function(locationArray, tbodySelector) {
    var layerName = 'Location Layer';
    bdrs.map.initBaseMap('base_map', { geocode: { selector: '#geocode' }});
	
	bdrs.location.wktFormatter = new OpenLayers.Format.WKT(bdrs.map.wkt_options);
	
	var addFeatureHandler = function(feature) {

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
        var params = {
            'index': index
        };
		
		var lonLat = feature.geometry.bounds.getCenterLonLat().clone();
        lonLat.transform(bdrs.map.GOOGLE_PROJECTION,
             bdrs.map.WGS84_PROJECTION);
        
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
                complete = true;
            }
            count += 1;
        }
		
		var locationJson = {
			name: name,
			latitude: bdrs.map.roundLatOrLon(lonLat.lat),
			longitude: bdrs.map.roundLatOrLon(lonLat.lon),
			wkt: bdrs.location.wktFormatter.write(feature)
		};

        bdrs.location.addLocationRow(tbodySelector, locationJson, feature, true);
	};
	
	var featureMoveCompleteHandler = function(feature, pixel) {	
	   var updateMediator = feature[bdrs.location.UPDATE_MEDIATOR_KEY];
	   if (!updateMediator) {
	       throw 'illegal state, updateMediator cannot be null';
	   }
	   // trigger update
	   updateMediator.updateFeature();
    };
	
	var selectControl = bdrs.location.createSelectControl();

    var polygonDrawLayerOptions = {
		featureAddedHandler: addFeatureHandler,
		featureMoveCompleteHandler: featureMoveCompleteHandler,
		dragStartHandler: function(feature, pixel) {
			bdrs.location.selectFeature(feature);
		},
		toolActivatedHandler: function(toolId) {
			// unselect all selected features...
			selectControl.unselectAll();
		}
	};

    var layer = bdrs.map.addPolygonDrawLayer(bdrs.map.baseMap, layerName, polygonDrawLayerOptions);
    
    // Create features for existing locations and bind the necessary listeners
	for (var i=0; i<locationArray.length; ++i) {
		var locationJson = locationArray[i];
		var feature = bdrs.location.addExistingFeatureFromWkt(layer, locationJson.wkt, locationJson.id);
		if (feature) {
            bdrs.location.addLocationRow(tbodySelector, locationJson, feature, false);	
		}
	}

    bdrs.map.centerMapToLayerExtent(bdrs.map.baseMap, layer);
    bdrs.location.addSelectControl(layer, selectControl);
};

// currently no longer used. leaving code here incase of future popup implementations.
bdrs.location.createPopup = function(feature) {
	var onPopupClose = function(evt) {
		bdrs.location.removePopup(feature);
    };
	
	var popupOptions = {
        onPopupClose: onPopupClose,
        featureToBind: feature
    };
	
	var center = feature.geometry.getBounds().getCenterLonLat().clone();
    bdrs.location.createFeaturePopup(bdrs.map.baseMap, feature.geometry.getBounds().getCenterLonLat().clone(), feature, popupOptions);
};

bdrs.location.createSelectControl = function() {
    
    var onFeatureSelect = function(feature) {
        
        var updateMediator = feature[bdrs.location.UPDATE_MEDIATOR_KEY];
        if (updateMediator) {
            updateMediator.featureSelected();
        } else {
            throw 'Illegal state, updateMediator should be defined';
        }
    };
    var onFeatureUnselect = function(feature) {
        var updateMediator = feature[bdrs.location.UPDATE_MEDIATOR_KEY];
        if (updateMediator) {
            updateMediator.featureUnselected();
        } else {
            throw 'Illegal state, updateMediator should be defined';
        }
    };

    // create the select control, initially attached to no layers.
    var selectControl = new OpenLayers.Control.SelectFeature([], {
        onSelect: onFeatureSelect,
        onUnselect: onFeatureUnselect
    });
	
	return selectControl;
};

// returns true if a popup has been removed, false otherwise (i.e. there was no popup to remove)
bdrs.location.removePopup = function(feature) {
	if (feature.popup) {
        bdrs.map.baseMap.removePopup(feature.popup);
        feature.popup.destroy();
        feature.popup = null;
		return true;
    }
	return false;
};

bdrs.location.addSelectControl = function(layer, selectControl) {
    
	selectControl.setLayer(layer);
	
    // duck punch selectControl for easy access on layer object.
    // It's available in the control array for the layer but it's a pain
    // to find it.
    layer[bdrs.location.SELECT_CONTROL_KEY] = selectControl;

    bdrs.map.baseMap.addControl(selectControl);
    selectControl.activate();

    bdrs.location.selectControl = selectControl;

    return selectControl;
};

// for creating the names of new locations
bdrs.location.index = 0;

bdrs.location.addExistingFeatureFromWkt = function(layer, wktString, pk) {
	
    var geoFeature = bdrs.location.wktFormatter.read(wktString);    
    // We are importantly setting the primary key attribute to the
    // bdrs object. This marks this object as a persisted location and
    // will be treated as such by the rest of the script.
	if (geoFeature) {
		geoFeature.bdrs = {};
	    geoFeature.bdrs.pk = pk;
	    layer.addFeatures(geoFeature);
	    return geoFeature;
	}
	return null;
};

// tableBodySelector - the selector for the table body which the row will be appended to
// locationJsonObj - the json object that represents a location, see required fields below
// bNewRow - boolean whether the row is for a new location or one loaded from the data base
// olFeature - the open layers feature that will be associated with the row  
bdrs.location.addLocationRow = function(tableBodySelector, locationJsonObj, olFeature, bNewRow) {

    // required fields for locationJsonObj:
	// name
	// id (if existing)
	// index (if new row)
	// latitude
	// longitude
	// wkt
	
	if (!locationJsonObj) {
		throw 'locationJsonObj canot be null';
	}

	if (bNewRow) {
		locationJsonObj.newLocation = true;
        locationJsonObj.index = bdrs.location.index++;  // post increment!
    }
	
	locationJsonObj.bdrsContextPath = bdrs.contextPath;
	
	// Asynchronously load our template content.
	// can optimize by loading and precompiling the template once
    $.get(bdrs.contextPath + '/tmpl/locationRow.tmpl', function(template) {
        // Use that stringified template with $.tmpl() and 
        // inject the rendered result into the body.
        var row = $.tmpl(template, locationJsonObj);
		row.appendTo($(tableBodySelector));
		
		
		row.data(bdrs.location.FEATURE_KEY, olFeature);
		
		var nameInput = row.find(".location_name");
		var latInput = row.find(".loc_latitude");
		var lonInput = row.find(".loc_longitude");
		var wktInput = row.find(".loc_wkt");
		var delAnchor = row.find(".loc_delete");
		
		// event bindings....
		
		row.focus(bdrs.location.onRowFocus);
        row.blur(bdrs.location.onRowBlur);
		
		nameInput.keypress(bdrs.location.inputKeyPressed);
		latInput.keypress(bdrs.location.inputKeyPressed);
		lonInput.keypress(bdrs.location.inputKeyPressed);
		wktInput.keypress(bdrs.location.inputKeyPressed);
		
		nameInput.focus(bdrs.location.inputFocus);
		latInput.focus(bdrs.location.inputFocus);
		lonInput.focus(bdrs.location.inputFocus);
		wktInput.focus(bdrs.location.inputFocus);
		
		nameInput.blur(bdrs.location.inputBlur);
        latInput.blur(bdrs.location.inputBlur);
        lonInput.blur(bdrs.location.inputBlur);
        wktInput.blur(bdrs.location.inputBlur);

		nameInput.change(bdrs.location.onNameChanged);
		latInput.change(bdrs.location.onLatChanged);
		lonInput.change(bdrs.location.onLonChanged);
		wktInput.change(bdrs.location.onWktChanged);
		
		delAnchor.click(function() {
			var myRow = jQuery(this).parents('tr'); 
			myRow.hide().find('select, input, textarea').attr('disabled', 'disabled');
			var feature = myRow.data(bdrs.location.FEATURE_KEY);
			// unselect feature if it is selected. this removes the popups and highlighting.
			bdrs.location.unselectFeature(feature);
			feature.layer.destroyFeatures(feature);
		});
		
		// duck punching
		var updateMediator = new bdrs.location.LocationUpdateMediator(olFeature, row);
		
		olFeature[bdrs.location.UPDATE_MEDIATOR_KEY] = updateMediator;
		row.data(bdrs.location.UPDATE_MEDIATOR_KEY, updateMediator);
		
		if (bNewRow) {
			// select the new row
			bdrs.location.selectFeature(olFeature);
		}
		
		// init ketchup for the row
		row.ketchup();
	});
};

bdrs.location.inputKeyPressed = function(e) {
	
	if(e.keyCode == 13) {
		// triggers the change event and
		// blocks 'Enter' from bubbling up and submitting the form
        $(this).change();
        return false; // returning false will prevent the event from bubbling up.
    } else {
        return true;
    }
};

bdrs.location.inputFocus = function() {
	var row = jQuery(this).parents('tr');
	row.focus();
};

bdrs.location.inputBlur = function() {
	var row = jQuery(this).parents('tr');
    row.blur();
}

bdrs.location.onRowFocus = function(){
	var row = jQuery(this);
	var updateMediator = row.data(bdrs.location.UPDATE_MEDIATOR_KEY);
	if (updateMediator) {
		updateMediator.rowSelected();
	} else {
	   throw 'Illegal state, update mediator cannot be null';	
	}
};

bdrs.location.onRowBlur = function() {
	var row = jQuery(this);
    var updateMediator = row.data(bdrs.location.UPDATE_MEDIATOR_KEY);
	if (updateMediator) {
       updateMediator.rowUnselected();
    } else {
       throw 'Illegal state, update mediator cannot be null';   
    }
};

bdrs.location.onNameChanged = function() {
	var updateMediator = bdrs.location.getMediatorForInput(this);
	updateMediator.updateName();
};

bdrs.location.onLatChanged = function() {	
	var updateMediator = bdrs.location.getMediatorForInput(this);
	updateMediator.updateLatitude();
};

bdrs.location.onLonChanged = function() {
	var updateMediator = bdrs.location.getMediatorForInput(this);
	updateMediator.updateLongitude();
};

bdrs.location.onWktChanged = function() {
	var updateMediator = bdrs.location.getMediatorForInput(this);
	updateMediator.updateWkt();
};

bdrs.location.getMediatorForInput = function(input) {
	var row = jQuery(input).parents('tr'); 
    return row.data(bdrs.location.UPDATE_MEDIATOR_KEY);
};



bdrs.location.createFeaturePopup = function (map, googleProjectionLonLatPos, feature, options) {
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
    jQuery("<span> Name: </span>").appendTo(cyclerDiv);
    var name = "";
    if(feature.bdrs.index === undefined) {
    	name = jQuery("[name=name_"+feature.bdrs.pk+"]").val();
    } else {
    	name = jQuery("[name=add_name_"+feature.bdrs.index+"]").val();        
    }
    jQuery("<span>"+name+"</span>").appendTo(cyclerDiv);
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

    popup.show();
    return popup;
};

bdrs.location.locationRowKeyPressed = function(e) {
    if(e.keyCode == 13) {
        return false; // returning false will prevent the event from bubbling up.
    } else {
        return true;
    }
};

// mediator object
// feature : instance of open layers feature object
// tableRow : jQuery wrapped tr node
// selectionManager : instance of bdrs.location.selectionManager
bdrs.location.LocationUpdateMediator = function(feature, tableRow) {
	
	if (!feature) {
		throw 'feature must be defined';
	}
	if (!tableRow) {
		throw 'tableRow must be defined';
	}
	
	// assigning the global to a member incase we need to decouple in the future
	this.selectionManager = bdrs.location.selectionManager;
	
	this.selected = false;
	
	this.feature = feature;
	this.tableRow = tableRow;
	
	this.latInput = this.tableRow.find(".loc_latitude");
    this.lonInput = this.tableRow.find(".loc_longitude");
    this.wktInput = this.tableRow.find(".loc_wkt");
	
	// state machine for managing location object selection
	this.fsm = StateMachine.create({
		initial: 'notSelected',
		events: [
		  {name: 'selectFeature', from: 'notSelected', to: 'selected'},
		  {name: 'selectFeature', from: 'selectingFeature', to: 'selected'},
		  {name: 'selectFeature', from: 'selected', to: 'selected'},
		  {name: 'rowFocus', from: 'notSelected', to: 'selectingFeature'},
		  {name: 'rowFocus', from: 'selected', to: 'selected'},
		  {name: 'deselectFeature', from: 'selected', to: 'notSelected'},
		  {name: 'deselectFeature', from: 'notSelected', to: 'notSelected'},
		  {name: 'deselectFeature', from: 'deselectingFeature', to: 'notSelected'},
		  {name: 'deselectFeature', from: 'deselectingFeature', to: 'notSelected'},
		  {name: 'selectFeature', from: 'deselectingFeature', to: 'selected'},
		  {name: 'rowBlur', from: 'selected', to: 'deselectingFeature'},
		  {name: 'rowBlur', from: 'notSelected', to: 'notSelected'}
		],
		callbacks: {
			onenterselected: function(event, from, to) {
				// highlight the row
				this.location.tableRow.addClass(bdrs.location.ROW_HIGHLIGHT_CLASS);
				this.location.selectionManager.selectLocation(this.location);
			},
			onleaveselected: function(event, from, to) {
				// dehighlight the row
				this.location.tableRow.removeClass(bdrs.location.ROW_HIGHLIGHT_CLASS);
			},
			onenterselectingFeature: function(event, from, to) {
                bdrs.location.selectFeature(this.location.feature);
			},
			onenterdeselectingFeature: function(event, from, to) {
				bdrs.location.unselectFeature(this.location.feature);
			}
		}
	});
	// duck punch in location mediator object
	this.fsm.location = this;
	
	if (!this.latInput) {
		throw 'latInput must be defined';
	}
	if (!this.lonInput) {
		throw 'lonInput must be defined';
	}
	if (!this.wktInput) {
		throw 'wktInput must be defined';
	}
	
	this.updateName = function() {
		var feature = this.feature;
		bdrs.location.unselectFeature(feature);
		bdrs.location.selectFeature(feature);
	};
	
	this.updateLatitude = function() {
		this._updateLonLat();
	};
	
	this.updateLongitude = function() {
        this._updateLonLat();
	};
	
	this.updateWkt = function() {
		
		var wktString = this.wktInput.val();
		
		var newgeom = OpenLayers.Geometry.fromWKT(wktString).transform(bdrs.map.WGS84_PROJECTION, bdrs.map.GOOGLE_PROJECTION);

        this._refreshMapFeature(newgeom);
		
		var centroid = this.feature.geometry.getCentroid();
        centroid.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
		// update lat lon inputs
        this.latInput.val(bdrs.map.roundLatOrLon(centroid.y));
        this.lonInput.val(bdrs.map.roundLatOrLon(centroid.x));
		
		this._setFieldsEnabled();
	};
	
	this.updateFeature = function() {
		var feature = this.feature;
        var centroid = feature.geometry.getCentroid();
        centroid.transform(bdrs.map.GOOGLE_PROJECTION, bdrs.map.WGS84_PROJECTION);
        this.latInput.val(bdrs.map.roundLatOrLon(centroid.y));
        this.lonInput.val(bdrs.map.roundLatOrLon(centroid.x));
        this.wktInput.val(bdrs.location.wktFormatter.write(feature));
	};
	
	// used by open layers handlers to signal that a feature has been selected
	this.featureSelected = function() {
		this.fsm.selectFeature();
	};
	
	this.featureUnselected = function() {
        this.fsm.deselectFeature();
	}
	
	// used by the table row to signal that the row has been selected
	this.rowSelected = function() {
		this.fsm.rowFocus();
	};
	
	this.rowUnselected = function() {
		this.fsm.rowBlur();
	};
	
	this._refreshMapFeature = function(newgeom) {
		// the map feature must be selected for you to be editing it so deselect and select
		// the feature again when refreshing it
		var feature = this.feature;
		bdrs.location.unselectFeature(feature);
		var layer = feature.layer;
        layer.removeFeatures(feature);
        feature.geometry = newgeom;
        layer.addFeatures(feature);
		bdrs.location.selectFeature(feature);
	};
	
	this._updateLonLat = function() {
		var lat = this.latInput.val();
        var lon = this.lonInput.val();
        var newgeom = new OpenLayers.Geometry.Point(lon, lat).transform(bdrs.map.WGS84_PROJECTION, bdrs.map.GOOGLE_PROJECTION);
        this._refreshMapFeature(newgeom);
        // this is kind of cyclic and messed up but hey, that's how i roll.
        // short of keeping another copy of the geometry object in the mediator and implementing
		// all the collegues directly.
        // I'm just reusing the geometry information in the feature. thus it is important
        // the order in which we update things.
        this.wktInput.val(bdrs.location.wktFormatter.write(this.feature));
	};
	
	this._removeFocus = function() {
		this.tableRow.find("input:focus").blur();
	};
	
	this._setFieldsEnabled = function() {
		var wktString = this.wktInput.val();
        if (wktString.indexOf("POINT") == 0) {
            this.latInput.attr('disabled', null);
            this.lonInput.attr('disabled', null);
        } else {
            this.latInput.attr('disabled', 'disabled');
            this.lonInput.attr('disabled', 'disabled');
        }
	};
	
	// initialise whether the lat/lon fields are enabled
	this._setFieldsEnabled();
};

// get the selection control for the layer. we always expect
// a select control to be duck punched in
bdrs.location.getSelectControl = function(layer) {

    var selectControl = layer[bdrs.location.SELECT_CONTROL_KEY];
    if (!selectControl) {
        throw 'Illegal state, select control should be defined';
    }
    return selectControl;
};

// select a feature. makes use of bidirectional relationship between
// layer and feature
bdrs.location.selectFeature = function(feature) {
    var layer = feature.layer;
    var selectControl = bdrs.location.getSelectControl(layer);
    // if the feature is NOT selected, select it.
    if ($.inArray(feature, layer.selectedFeatures) == -1) {
        selectControl.select(feature);  
    }
};

// deselect a feature. makes use of bidirectional relationship between
// layer and feature
bdrs.location.unselectFeature = function(feature) {
    var layer = feature.layer;
    var selectControl = bdrs.location.getSelectControl(layer);
    // if the feature is selected, deselect it.
    if ($.inArray(feature, layer.selectedFeatures) > -1) {
        selectControl.unselect(feature);
    }
};

// remembers what item is currently selected and deselects it if appropriate
bdrs.location.SelectionManager = function() {
	// bdrs.location.LocationUpdateMediator
	this.currentSelection = null;
	
	this.selectLocation = function(location) {

		if (!this.currentSelection) {
			// no previous selection
			this.currentSelection = location;
		} else if (this.currentSelection !== location) {
            // deselect the currently selected item...
            var feature = this.currentSelection.feature;
            // will trigger state machine inside the LocationUpdateMediator to dehighlight the row
			// if the feature is already unselected nothing will happen
            bdrs.location.unselectFeature(feature);
            // set the new location
            this.currentSelection = location;   
		}
	};
};

// instantiate global selection manager
bdrs.location.selectionManager = new bdrs.location.SelectionManager();

// note: this is a duplicate of the row template in locationListing.jsp
bdrs.location.LOCATION_LISTING_ROW_TMPL = '\
<tr>\
    <td class="name">\
        <input type="hidden" name="location" value="${ id }"/>\
        <a href="${contextPath}/bdrs/admin/survey/editLocation.htm?surveyId=${ surveyId }&locationId=${ id }">${ name }</a>\
    </td>\
    <td class="textcenter">\
        <a id="delete_${location.id}" href="javascript: void(0);" onclick="jQuery(this).parents(\'tr\').hide().find(\'select, input, textarea\').attr(\'disabled\', \'disabled\'); return false;">\
            <img src="${contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>\
        </a>\
    </td>\
</tr>';


/**
 * initialises the locations selecting dialog and the grid inside of it.
 * The locations displayed will be the survey locations for all of the
 * surveys that the accessing user has access to. The survey locations
 * for the current survey (i.e. the one you are looking at right now)
 * will not be seen.
 * 
 * @param {Object} gridSelector - selector for the grid table element
 * @param {Object} pagerSelector - selector for the pager div element
 * @param {Object} dialogSelector - selector for the dialog div element
 * @param {Object} openDialogButtonSelector - selector for the button that opens the dialog
 * @param {Object} tableBodySelector - selector for the table body to append locations onto
 * @param {Object} surveyId - the survey ID that we are viewing in this page.
 */
bdrs.location.createGetSurveyLocationsForUserDialogGrid = function(gridSelector, 
																	pagerSelector, 
																	dialogSelector, 
																	openDialogButtonSelector,
																	tableBodySelector,
																	surveyId) {
																		
    var compiledRowTemplate = jQuery.template(bdrs.location.LOCATION_LISTING_ROW_TMPL);																	
																		
	jQuery(gridSelector).jqGrid({
            url: bdrs.contextPath + '/bdrs/location/getSurveyLocationsForUser.htm?surveyId=' + surveyId,
            datatype: "json",
            mtype: "GET",
            colNames:['Name','Description'],
            colModel:[
                {name:'name',index:'name'},
                {name:'description', index:'description'}
            ],
            autowidth: true,
            jsonReader : { repeatitems: false },
            rowNum:10,
            rowList:[10,20,30],
            pager: pagerSelector,
            sortname: 'name',
            viewrecords: true,
            sortorder: "asc",
            multiselect: true,
            caption:"Locations Listing"
    });
    
    jQuery(pagerSelector).jqGrid('navGrid',pagerSelector,{edit:false,add:false,del:false});
	
	
	var tbodyNode = jQuery(tableBodySelector);
	
	jQuery(dialogSelector).dialog({
        width: 'auto',   // doesn't work properly in IE7 - needs an explicit width
        modal: true,
        autoOpen: false,
        zIndex: bdrs.MODAL_DIALOG_Z_INDEX,
		resizable: false,
        buttons: {
            "Ok": function() {
				
				var grid = jQuery(gridSelector);
				var selected = grid.getGridParam('selarrrow');
				
				var rowId;
				for (var selIdx=0; selIdx < selected.length; ++selIdx) {
					rowId = selected[selIdx];
					var rowData = grid.jqGrid('getRowData', rowId);
					var data = {
						"id": rowId,
						"name": rowData.name,
						"surveyId": surveyId,
						"contextPath": bdrs.contextPath
					};
					var processedRow = jQuery.tmpl(compiledRowTemplate, data);
                    tbodyNode.append(processedRow);
				}                
                $( this ).dialog( "close" );
            },
            Cancel: function() {
                $( this ).dialog( "close" );
            }
        },
        title: "Add Existing Location"
    });
        
    jQuery(openDialogButtonSelector).click(function() {
        jQuery(dialogSelector).dialog( "open" );
    });
}
