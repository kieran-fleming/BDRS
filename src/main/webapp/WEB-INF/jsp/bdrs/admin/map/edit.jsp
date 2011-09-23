<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ geoMap.id == null }">
        <h1>Add Map</h1>
		<p>
			You can create a Map, and then assign Map Layers that you have already 
			<a href="${pageContext.request.contextPath}/bdrs/admin/mapLayer/listing.htm">created</a> or 
			<a href="${pageContext.request.contextPath}/bdrs/admin/mapLayer/listing.htm">edited</a> using this interface.  
			If you need to Edit an existing Map, use the 
			<a href="${pageContext.request.contextPath}/bdrs/admin/map/listing.htm">Edit Map interface</a> instead.
		</p>
    </c:when>
    <c:otherwise>
        <h1>Edit Map</h1>
		<p>
			Here you can edit an existing Map.  You will firstly have needed to use the 
			<a href="${pageContext.request.contextPath}/bdrs/admin/map/edit.htm">Add Map</a> 
			interface, and added Map Layers to it as well.
		</p>
    </c:otherwise>
</c:choose>

<form method="POST" action="${pageContext.request.contextPath}/bdrs/admin/map/edit.htm">
    <input type="hidden" name="geoMapPk" value="${geoMap.id}" />
    <table>
        <tr>
            <td><label for="name">Map Name:</label></td>
            <td><input id="name" title="This is what your users will recognise your map by" class="validate(required, maxlength(255))" type="text" style="width:40em" name="name" value="<c:out value="${geoMap.name}" />" size="40"  autocomplete="off"></td>
			
        </tr>
        <tr>
            <td><label for="description">Map Description:</label></td>
            <td><input id="description" title="This can be a more detailed text that will be displayed as a blurb on your map" class="validate(required, maxlength(1023))" type="text" style="width:40em" name="description" value="<c:out value="${geoMap.description}" />" size="40"  autocomplete="off"></td>
        </tr>
        <tr>
            <td><label for="anonAccess">Anonymous Access:</label></td>
            <td><input id="anonAccess" title="If this is checked, users will be able to look at your map without being logged in" type="checkbox" value="on" name="anonymousAccess" <c:if test="${geoMap.anonymousAccess}">checked="checked"</c:if> /></td>
        </tr>
        <tr>
            <td><label for="publish">Publish:</label></td>
            <td><input id="publish" title="If this is checked the map will be accessible to users, both anonymous and logged in" type="checkbox" value="on" name="publish" <c:if test="${geoMap.publish}">checked="checked"</c:if> /></td>
        </tr>
        <tr>
            <td><label for="hidePrivateDetails">Hide Private Details:</label></td>
            <td><input id="hidePrivateDetails" title="Unused at this time" type="checkbox" value="on" name="hidePrivateDetails" <c:if test="${geoMap.hidePrivateDetails}">checked="checked"</c:if> /></td>
        </tr>
        <tr>
            <td><label for="listPosition">List Position:</label></td>
            <td><input id="listPosition" title="What order your maps will appear in under the 'Review' menu item" class="validate(required, number)" type="" name="weight" value="${geoMap.weight}" /></td>
        </tr>
    </table>

    <div id="mapLayerContainer">
        <div class="textright buttonpanel">
            <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#mapLayerContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
            <input id="addMapLayerBtn" class="form_action" type="button" value="Add Map Layer" />
        </div>
        <table id="mapLayer_input_table" class="datatable attribute_input_table">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th title="The name of the map layer">Map Layer Name</th>
                    <th title="The description of the map layer">Map Layer Description</th>
					<th title="The upper zoom level that the layer will appear on. Higher numbers = more zoomed in.">Zoom In Limit</th>
					<th title="The lower zoom level that the layer will appear on. Lower numbers = more zoomed out.">Zoom Out Limit</th>
					<th title="Whether the map layer will be visible upon map loading">Visible</th>
                    <th title="Remove the map layer from this map">Delete</th>
                </tr>
            </thead>
            <tbody id="mapLayersTbody">

            </tbody>
        </table>
    </div>
    

    <div class="buttonpanel textright">
        <input type="submit" class="form_action" type="button" value="Save" />
     </div>
</form>

<!--  DIALOGS GO HERE  ! -->
<div id="addMapLayerDialog" title="Add Map Layer">
    <tiles:insertDefinition name="geoMapLayerGrid">
           <tiles:putAttribute name="widgetId" value="addMapLayerGrid"/>
           <tiles:putAttribute name="multiselect" value="false"/>
           <tiles:putAttribute name="scrollbars" value="true" />
    </tiles:insertDefinition>
</div>


<script id="rowTemplate" type="text/x-jquery-tmpl">
	<tr>
        <td class="drag_handle">
            <input type="hidden" value="0" class="sort_weight" />         
        </td>
        <td>
            <label>${'${'}name}</label>
            <input type="hidden" value="${'${'}id}" name="mapLayerPk" />                                 
        </td>
        <td>
            <label>${'${'}description}</label>                                                           
        </td>
        <td class="textcenter">
            <select style="width:7em" name="upperZoomLimit" onchange="upperZoomLimitChanged(jQuery(this));">
                <option value="">No Limit</option>
                <option value="0" {{if (lowerZoomLimit == 0)}} selected="selected" {{/if}}>0</option>
                <option value="1" {{if (upperZoomLimit == 1)}} selected="selected" {{/if}}>1</option>
                <option value="2" {{if (upperZoomLimit == 2)}} selected="selected" {{/if}}>2</option>
                <option value="3" {{if (upperZoomLimit == 3)}} selected="selected" {{/if}}>3</option>
                <option value="4" {{if (upperZoomLimit == 4)}} selected="selected" {{/if}}>4</option>
                <option value="5" {{if (upperZoomLimit == 5)}} selected="selected" {{/if}}>5</option>
                <option value="6" {{if (upperZoomLimit == 6)}} selected="selected" {{/if}}>6</option>
                <option value="7" {{if (upperZoomLimit == 7)}} selected="selected" {{/if}}>7</option>
                <option value="8" {{if (upperZoomLimit == 8)}} selected="selected" {{/if}}>8</option>
                <option value="9" {{if (upperZoomLimit == 9)}} selected="selected" {{/if}}>9</option>
                <option value="10" {{if (upperZoomLimit == 10)}} selected="selected" {{/if}}>10</option>
                <option value="11" {{if (upperZoomLimit == 11)}} selected="selected" {{/if}}>11</option>
                <option value="12" {{if (upperZoomLimit == 12)}} selected="selected" {{/if}}>12</option>
                <option value="13" {{if (upperZoomLimit == 13)}} selected="selected" {{/if}}>13</option>
                <option value="14" {{if (upperZoomLimit == 14)}} selected="selected" {{/if}}>14</option>
                <option value="15" {{if (upperZoomLimit == 15)}} selected="selected" {{/if}}>15</option>
            </select>
        </td>
        <td class="textcenter">
            <select style="width:7em" name="lowerZoomLimit" onchange="lowerZoomLimitChanged(jQuery(this));">
            	<option value="">No Limit</option>
                <option value="0" {{if (lowerZoomLimit == 0)}} selected="selected" {{/if}}>0</option>
                <option value="1" {{if (lowerZoomLimit == 1)}} selected="selected" {{/if}}>1</option>
                <option value="2" {{if (lowerZoomLimit == 2)}} selected="selected" {{/if}}>2</option>
                <option value="3" {{if (lowerZoomLimit == 3)}} selected="selected" {{/if}}>3</option>
                <option value="4" {{if (lowerZoomLimit == 4)}} selected="selected" {{/if}}>4</option>
                <option value="5" {{if (lowerZoomLimit == 5)}} selected="selected" {{/if}}>5</option>
                <option value="6" {{if (lowerZoomLimit == 6)}} selected="selected" {{/if}}>6</option>
                <option value="7" {{if (lowerZoomLimit == 7)}} selected="selected" {{/if}}>7</option>
                <option value="8" {{if (lowerZoomLimit == 8)}} selected="selected" {{/if}}>8</option>
                <option value="9" {{if (lowerZoomLimit == 9)}} selected="selected" {{/if}}>9</option>
                <option value="10" {{if (lowerZoomLimit == 10)}} selected="selected" {{/if}}>10</option>
                <option value="11" {{if (lowerZoomLimit == 11)}} selected="selected" {{/if}}>11</option>
                <option value="12" {{if (lowerZoomLimit == 12)}} selected="selected" {{/if}}>12</option>
                <option value="13" {{if (lowerZoomLimit == 13)}} selected="selected" {{/if}}>13</option>
                <option value="14" {{if (lowerZoomLimit == 14)}} selected="selected" {{/if}}>14</option>
                <option value="15" {{if (lowerZoomLimit == 15)}} selected="selected" {{/if}}>15</option>
            </select>
        </td>
        <td class="textcenter">
            {{if (visible == true)}}
               <input type="hidden" name="mapLayerVisible" value="false" disabled="disabled" />
               <input type="checkbox" name="mapLayerVisible" value="true" checked="checked" onchange="mapLayerVisibleChanged(jQuery(this));" />
            {{else}}
               <input type="hidden" name="mapLayerVisible" value="false" />
               <input type="checkbox" name="mapLayerVisible" value="true"  onchange="mapLayerVisibleChanged(jQuery(this));" />
            {{/if}}
        </td>                                                                                   
        <td class="textcenter">                                                                     
            <a href="javascript: void(0);" onclick="jQuery(this).parents('tr').hide().find('select, input, textarea').attr('disabled', 'disabled').removeClass(); return false;">   
            <img src="${pageContext.request.contextPath}/images/icons/delete.png" alt="Delete" class="vertmiddle"/>                                                                         
            </a>                                                                                    
        </td>                                                                                       
    </tr>    
</script>

<script type="text/javascript">
    $(function() {
        bdrs.dnd.attachTableDnD('#mapLayer_input_table');
        $( "#addMapLayerDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
            zIndex: bdrs.MODAL_DIALOG_Z_INDEX,
            buttons: {
                "Ok": function() {
                    var rowId = addMapLayerGrid_GridHelper.getSelected();                    
                    // single select so...
                    if (rowId) {
	                    var rowData = addMapLayerGrid_GridHelper.getRowData(rowId);
	                    
						// setup default values here...
	                    var data = {
	                        "id": rowId,
	                        "name": rowData.name,
	                        "description": rowData.description,
							"visible": true,
							"upperZoomLimit": null,
							"lowerZoomLimit": null
	                    };
	                    addTableRow(data);
	                }
	                
                    $( this ).dialog( "close" );
                },
                Cancel: function() {
                    $( this ).dialog( "close" );
                }
            }
        });
        
        $( "#addMapLayerBtn" )
            .click(function() {
                addMapLayerGrid_GridHelper.reload();
                $( "#addMapLayerDialog" ).dialog( "open" );
        });
        
        <c:forEach var="assignedLayer" items="${assignedLayers}">
        {
            var data = {
                "id": <c:out value="${assignedLayer.layer.id}" />,
                "name": '<c:out value="${assignedLayer.layer.name}" />',
                "description": '<c:out value="${assignedLayer.layer.description}"/>',
                "visible": <c:out value="${assignedLayer.visible}" />,
				"upperZoomLimit": <c:out value="${assignedLayer.upperZoomLimit != null ? assignedLayer.upperZoomLimit : 'null'}" />,
				"lowerZoomLimit": <c:out value="${assignedLayer.lowerZoomLimit != null ? assignedLayer.lowerZoomLimit : 'null'}" />
            };
            addTableRow(data);
        }
        </c:forEach>
		
		// trigger change events so the enabling/disabling occurs.
		jQuery('select').change();
    });
	
    // select: the select node
    // siblingSelector: the selector for sibling select node for the table row
    // bEnableWhenHigher: effects the check for whether the value of the
    // current select node should be higher or lower than the sibling select node.
	var enableDisableOptions = function (select, siblingSelector, bEnableWhenHigher) {
		var selector = siblingSelector;
		var value = select.val();
        var siblingValue = select.parent().siblings().children(selector).val();     
        var enableOptions = select.parent().siblings().children(selector).children().filter(function() {
            if (value === "") {
                return true;
            }
            var intValue = parseInt(value);
            var intSiblingValue = parseInt(jQuery(this).val());
			
			return bEnableWhenHigher ? intSiblingValue <= intValue : intValue <= intSiblingValue; 
        });
		enableOptions.removeAttr('disabled');
		
		var disableOptions = select.parent().siblings().children(selector).children().filter(function() {
            if (value === "") {
                return false;
            }
            var intValue = parseInt(value);
            var intSiblingValue = parseInt(jQuery(this).val());
			
			return bEnableWhenHigher ? intSiblingValue > intValue : intValue > intSiblingValue
        });
        disableOptions.attr('disabled','disabled');
		
		if (value !== "" && siblingValue !== "") {
            if (bEnableWhenHigher ? (parseInt(siblingValue) > parseInt(value)) : (parseInt(value) > parseInt(siblingValue))) {
                select.parent().siblings().children(selector).val(value);
            }
        }
	};
	
	var upperZoomLimitChanged = function(select) {
		var selector = 'select[name="lowerZoomLimit"]';
		enableDisableOptions(select, selector, true);
	};
	
	var lowerZoomLimitChanged = function(select) {
		var selector = 'select[name="upperZoomLimit"]';
        enableDisableOptions(select, selector, false);
	};
	
	var mapLayerVisibleChanged = function(checkbox) {
        if (checkbox.attr('checked')) {
            checkbox.siblings('input:hidden').attr('disabled','disabled');
        } else {
            checkbox.siblings('input:hidden').removeAttr('disabled');
        }
    };
    
    var addTableRow = function(geoMapLayerJsonObj) {
        var pkNodes = $("input[name=mapLayerPk]");
        for (; pkNodes.length > 0; pkNodes.splice(0,1)) {
            if (pkNodes.val() == geoMapLayerJsonObj.id) {
                bdrs.message.set("Map layer has already been added.");
                return;
            }
        }
        var row = $('#rowTemplate').tmpl(geoMapLayerJsonObj);
        bdrs.dnd.addDnDRow("#mapLayer_input_table", row);
    };
    
</script>
