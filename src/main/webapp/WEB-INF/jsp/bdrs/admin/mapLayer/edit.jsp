<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>
<%@ taglib uri="/WEB-INF/cw.tld" prefix="cw" %>

<c:choose>
    <c:when test="${ geoMapLayer.id == null }">
        <h1>Add Map Layer</h1>
    </c:when>
    <c:otherwise>
        <h1>Edit Map Layer</h1>
    </c:otherwise>
</c:choose>


<form id="mapLayerForm" action="${pageContext.request.contextPath}/bdrs/admin/mapLayer/edit.htm" method="POST">
    <c:if test="${ geoMapLayer.id != null }">
    <input type="hidden" name="geoMapLayerPk" value="${geoMapLayer.id}" />
    </c:if>
    <c:if test="${ geoMapLayer.id == null }">
    <input type="hidden" name="geoMapLayerPk" value="0" />
    </c:if>
    <table>
        <tr>
            <td>Map Layer Name:</td>
            <td><input class="validate(required,maxlength(255))" type="text" style="width:40em" name="name" title="The name of the map layer" value="<c:out value="${geoMapLayer.name}" />" size="40"  autocomplete="off"></td>
        </tr>
        <tr>
            <td>Map Layer Description:</td>
            <td><input class="validate(required,maxlength(1023))" type="text" style="width:40em" name="desc" title="The description of the map, up to 1023 characters long"	value="<c:out value="${geoMapLayer.description}" />" size="40"  autocomplete="off"></td>
        </tr>
        <tr>
            <td>Publish:</td>
            <td><input type="checkbox" value="on" name="publish" title="has no effect" <c:if test="${geoMapLayer.publish}">checked="checked"</c:if> /></td>
        </tr>
        <tr>
            <td>Hide Private Details:</td>
            <td><input type="checkbox" value="on" name="hidePrivateDetails"	title="has no effect" <c:if test="${geoMapLayer.hidePrivateDetails}">checked="checked"</c:if> /></td>
        </tr>
        <tr>
            <td>Source data for layer</td>
            <td>
                <!-- survey is the default selection -->
                <input title="Generate a KML layer from a survey" id="radio_survey_kml" type="radio" class="dataSourceSelector" name="layerSrc" value="SURVEY_KML" <c:if test="${ geoMapLayer.layerSource == \"SURVEY_KML\" }">checked="checked"</c:if> <c:if test="${ geoMapLayer.id == null }">checked="checked"</c:if> /><label for"radio_survey_kml">Survey via KML</label><br />
                <input title="Generate a WMS layer from a survey" id="radio_survey_mapserver" type="radio" class="dataSourceSelector" name="layerSrc" value="SURVEY_MAPSERVER" <c:if test="${ geoMapLayer.layerSource == \"SURVEY_MAPSERVER\" }">checked="checked"</c:if> <c:if test="${ geoMapLayer.id == null }">checked="checked"</c:if> /><label for="radio_survey_mapserver">Survey via MapServer (requires Mapserver installed)</label><br />
                <input title="Generate a WMS layer from an uploaded shapefile" id="radio_shapefile" type="radio" class="dataSourceSelector" name="layerSrc" value="SHAPEFILE" <c:if test="${ geoMapLayer.layerSource == \"SHAPEFILE\" }">checked="checked"</c:if> /><label for="radio_shapefile">Shapefile (requires Mapserver installed)</label><br />
                <input title="Load a KML file from the managed file system" id="radio_kml" type="radio" class="dataSourceSelector" name="layerSrc" value="KML" <c:if test="${ geoMapLayer.layerSource == \"KML\" }">checked="checked"</c:if> /><label for="radio_kml">KML File</label>
            </td>
        </tr>
        <tr>
            <td>File Identifier (UUID):</td>
            <td><input title="A managed file UUID. Depending on the layer source it may be a KML file or a shapefile." id="fileId" type="text" style="width:40em" name="mfuuid" value="<c:out value="${geoMapLayer.managedFileUUID}" />" size="40" autocomplete="off" onchange="setWriteFileToDatabase();" />
			     <div>Upload and browse your files in the <a href="${pageContext.request.contextPath}/bdrs/user/managedfile/listing.htm" target="_blank">managed file interface. (Opens in new window)</a></div>
			     <div id="shapefile_instructions">The uploaded file must be a zipped up shapefile containing a minimum of the .shp, .dbf, .shx and .prj files.</div>
			</td>
        </tr>
		<tr>
			<td>Write file to Database:</td>
			<td><input title="If the layer source is a shape file, checking this box will cause a database overwrite of stored map features" id="writeToDatabase" type="checkbox" name="shpToDatabase" /></td>
        <tr>
            <td>Survey:</td>
            <td>
                <select title="Select the survey data to create map layer with" id="surveyId" name="surveyPk">
                <c:forEach items="${surveyList}" var="survey">
                    <option value="${survey.id}" 
                    <c:if test="${survey.id == geoMapLayer.survey.id}">selected="selected"</c:if>
                    ><c:out value="${survey.name}"/></option>
                </c:forEach>
                </select>
            </td>
        </tr>
		<tr>
			<td><label for"strokeColor">Stroke Colour:</label></td>
			<td><div><input class="validate(required,color)" title="Stroke colour parameter for map styling" id="strokeColor" type="text" name="strokeColor" value="<c:out value="${geoMapLayer.strokeColor}" />" /></div></td>
		</tr>
		<tr>
			<td><label for"fillColor">Fill Colour:</label></td>
            <td><input class="validate(required,color)" title="Fill color parameter for map styling" id="fillColor" type="text" name="fillColor" value="<c:out value="${geoMapLayer.fillColor}" />" /></td>
		</tr>
		<tr>
            <td><label for"symbolSize">Symbol Size (only effects point data):</label></td>
            <td><input class="validate(required)" title="Symbol size parameter for map styling" id="symbolSize" type="text" name="symbolSize" value="<c:out value="${geoMapLayer.symbolSize}" />" /></td>
        </tr>
        <tr>
            <td><label for"strokeWidth">Stroke Width:</label></td>
            <td><input class="validate(required)" title="Stroke width parameter for map styling" id="strokeWidth" type="text" name="strokeWidth" value="<c:out value="${geoMapLayer.strokeWidth}" />" /></td>
        </tr>
    </table>

    <div class="buttonpanel textright">
        <input onclick="onSubmit();" class="form_action" type="button" value="Save" />
     </div>
</form>

<script type="text/javascript">

    jQuery(function() {

		bdrs.util.createColorPicker('#strokeColor');
		bdrs.util.createColorPicker('#fillColor');
		
        $('.dataSourceSelector').change(function() {
            var selected = $('input[name=layerSrc]:checked', '#mapLayerForm').val();
            setDataSource(selected);
        });
        
        // trigger the change event to do initialisation...
        $('.dataSourceSelector').change();
    });
	
	var setWriteFileToDatabase = function() {
		$('#writeToDatabase').attr("checked", "checked");
	};
	
	var onSubmit = function() {
		  if ($('#radio_shapefile').attr('checked') && !$('#writeToDatabase').attr('checked')) {
                bdrs.util.confirmExec("You have selected shapefile and a file but have not chosen to write the file to the database. Write the file to the database?\n\nNote: All other settings will still be saved!", setWriteFileToDatabase);
		  }
		  if ($('#radio_shapefile').attr('checked') && $('#writeToDatabase').attr('checked')) {
                var uuid = $('#fileId').val();
				$.ajax("${pageContext.request.contextPath}/bdrs/map/checkShapefile.htm", {
				    data: {
						mfuuid: uuid
					},
					success: function(json) {
					   if (json.status == "error") {
					   	   var msg = json.message.join("\n\n");
					       bdrs.message.set("Error with file: " + msg);
					   } else if (json.status == "warn") {
					   	  var msg = json.message.join("\n\n");
						
						  if (bdrs.util.confirm(msg + "\n\nDo you wish to continue?")) {
						  	$('#mapLayerForm').submit();
							$.blockUI({ message: '<h1>Writing shapefile to database...</h1>' });
						  } else {
						  	$.unblockUI();
						  }
					   } else if (json.status == "ok") {
					   	  $('#mapLayerForm').submit();
						  $.blockUI({ message: '<h1>Writing shapefile to database...</h1>' });
					   } else {
					   	  $.unblockUI();
					   }
					},
					error: function() {
						bdrs.message.set("Error when trying to run the file check webservice");
						$.unblockUI();
					},
					beforeSend: function() {
						$.blockUI({ message: '<h1>Just a moment, checking shapefile...</h1>' });
					}
				});
          } else {
		      $('#mapLayerForm').submit();
		  }
	};
    
    var setDataSource = function(src) {
        if (src == 'KML') {
            $('#fileId').attr('disabled', null);
            $('#fileId').addClass("validate(required,maxlength(255),uuid)");
            $('#surveyId').attr('disabled', 'disabled');
			$('#writeToDatabase').attr('disabled', 'disabled');
			$('#shapefile_instructions').hide();
        }
		if (src == 'SHAPEFILE') {
			$('#fileId').attr('disabled', null);
            $('#fileId').addClass("validate(required,maxlength(255),uuid)");
            $('#surveyId').attr('disabled', 'disabled');
			$('#writeToDatabase').attr('disabled', null);
			$('#shapefile_instructions').show();
		}
        if (src == 'SURVEY_KML' || src == 'SURVEY_MAPSERVER') {
            $('#fileId').attr('disabled', 'disabled');
            $('#fileId').removeClass("validate(required,maxlength(255),uuid)");
            $('#surveyId').attr('disabled', null);
			$('#writeToDatabase').attr('disabled', 'disabled');
			$('#shapefile_instructions').hide();
        }
        // do rebinding...
        jQuery('form').ketchup();
    };
</script>