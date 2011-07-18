<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ geoMap.id == null }">
        <h1>Add Map</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Map</h1>
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
    
    
    <h3>Map Layers</h3>
	
	<p>Assign map layers to this map that you have created earlier 
    using the <a href="${pageContext.request.contextPath}/bdrs/admin/mapLayer/listing.htm">map layer interface</a></p>

    <div id="mapLayerContainer">
        <div class="textright buttonpanel">
            <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#mapLayerContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
            <input id="addMapLayerBtn" class="form_action" type="button" value="Add Map Layer" />
        </div>
        <table id="mapLayer_input_table" class="datatable attribute_input_table">
            <thead>
                <tr>
                    <th>&nbsp;</th>
                    <th>Map Layer Name</th>
                    <th>Map Layer Description</th>
					<th title="Whether the map layer will be visible upon map loading">Visible</th>
                    <th>Delete</th>
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

<div style="display:none">
    <table>
       <tbody id="rowTemplate">
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
        </tbody> 
    </table>
</div>


<script type="text/javascript">
    $(function() {
        bdrs.dnd.attachTableDnD('#mapLayer_input_table');
        $( "#addMapLayerDialog" ).dialog({
            width: 'auto',
            modal: true,
            autoOpen: false,
            buttons: {
                "Ok": function() {
                    var rowId = addMapLayerGrid_GridHelper.getSelected();                    
                    // single select so...
                    if (rowId) {
	                    var rowData = addMapLayerGrid_GridHelper.getRowData(rowId);
	                    
	                    var data = {
	                        "id": rowId,
	                        "name": rowData.name,
	                        "description": rowData.description,
							"visible": true
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
                "visible": <c:out value="${assignedLayer.visible}" />
            };
            addTableRow(data);
        }
        </c:forEach>
    });
	
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
