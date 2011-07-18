<%@ taglib uri="http://java.sun.com/jsp/jstl/core" prefix="c" %>
<%@ taglib uri="http://java.sun.com/jsp/jstl/fmt" prefix="fmt" %>
<%@ taglib uri="http://www.springframework.org/security/tags" prefix="sec" %>
<%@ taglib uri="http://tiles.apache.org/tags-tiles" prefix="tiles" %>

<jsp:useBean id="record" scope="request" type="au.com.gaiaresources.bdrs.model.record.Record" />
<jsp:useBean id="survey" scope="request" type="au.com.gaiaresources.bdrs.model.survey.Survey" />

<%@page import="au.com.gaiaresources.bdrs.model.taxa.Attribute"%>
<%@page import="au.com.gaiaresources.bdrs.model.taxa.AttributeScope"%>

<h1><c:out value="${survey.name}"/></h1>

<span id="script_content" class="hidden">
	<p>
	    Click on the map to enter the location of the sighting.
	</p>
	
	<div id="record_base_map_hover_tip">&nbsp;</div>
	<div class="map_wrapper" id="map_wrapper">
	    <div id="base_map" class="defaultmap tracker_map"></div>
	    <div id="geocode" class="geocode"></div>
	</div>
	
	<c:if test="${ not preview }">
	    <form method="POST" action="${pageContext.request.contextPath}/bdrs/user/singleSiteMultiTaxa.htm" enctype="multipart/form-data">
	</c:if>
	<input type="hidden" name="surveyId" value="${survey.id}"/>
	
	<table class="form_table">
	    <tbody>
	        <c:forEach items="${formFieldList}" var="formField">
				<tiles:insertDefinition name="formFieldRenderer">
				    <tiles:putAttribute name="formField" value="${formField}"/>
				    <tiles:putAttribute name="locations" value="${locations}"/>
				</tiles:insertDefinition>
	        </c:forEach>
	    </tbody>
	</table>
	
	
	<div id="sightingsContainer">
		<div id="add_sighting_panel" class="textright">
	        <a id="maximiseLink" class="text-left" href="javascript:bdrs.util.maximise('#maximiseLink', '#sightingsContainer', 'Enlarge Table', 'Shrink Table')">Enlarge Table</a>
		    <input type="hidden" id="sighting_index" name="sightingIndex" value="0"/>  
		    <input class="form_action" type="button" value="Add Sighting" onclick="bdrs.contribute.singleSiteMultiTaxa.addSighting('#sighting_index', '[name=surveyId]', '#sightingTable tbody');"/>
		</div>
		
		<table id="sightingTable" class="datatable">
		    <thead>
		       <tr>
		           <c:forEach items="${ sightingRowFormFieldList }" var="sightingRowFormField">
		               <th>
			               <jsp:useBean id="sightingRowFormField" type="au.com.gaiaresources.bdrs.controller.attribute.formfield.AbstractRecordFormField" />
			               <c:choose>
		                       <c:when test="<%= sightingRowFormField.isPropertyFormField() %>">
		                           <c:choose>
			                            <c:when test="${ 'species' == sightingRowFormField.propertyName }">
			                                Species                                        
			                            </c:when>
			                            <c:when test="${ 'number' == sightingRowFormField.propertyName }">
			                                Number                            
			                            </c:when>
		                            </c:choose>
		                       </c:when>
		                       <c:when test="<%= sightingRowFormField.isAttributeFormField() %>">
		                           <c:out value="${ sightingRowFormField.attribute.description }"/>
		                       </c:when> 
		                   </c:choose>
	                   </th>
		           </c:forEach>
		       </tr>
		    </thead>
		    <tbody>
		    </tbody>
		</table>
	</div>
	
	<c:choose>
	    <c:when test="${ preview }">
	        <div class="textright">
	            <input class="form_action" type="button" value="Go Back" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editAttributes.htm?surveyId=${survey.id}'"/>
	            <input class="form_action" type="button" value="Continue" onclick="window.document.location='${pageContext.request.contextPath}/bdrs/admin/survey/editLocations.htm?surveyId=${survey.id}'"/>
	        </div>
	    </c:when>
	    <c:otherwise>
	            <div class="textright">
	                <input class="form_action" type="submit" name="submitAndAddAnother" value="Submit and Add Another"/>
	                <input class="form_action" type="submit" name="submit" value="Submit Sightings"/>
	            </div>
	        </form>
	    </c:otherwise>
	</c:choose>
</span>

<noscript>
    <tiles:insertDefinition name="noscriptMessage"></tiles:insertDefinition>
</noscript>

<script type="text/javascript">
    jQuery(function() {
        jQuery("#script_content").removeClass("hidden");
    });
</script>

<script type="text/javascript">
    bdrs.survey.location.LAYER_NAME = 'Position Layer';

    bdrs.survey.location.updateLocation = function(pk) {
        if(pk > 0) {
            jQuery.get("${pageContext.request.contextPath}/webservice/location/getLocationById.htm", {id: pk}, function(data) {
                var wkt = new OpenLayers.Format.WKT();
                var feature = wkt.read(data.location);

                var lat = jQuery('input[name=latitude]').val(feature.geometry.y).blur();
                var lon = jQuery('input[name=longitude]').val(feature.geometry.x).blur();

                var layer = bdrs.map.baseMap.getLayersByName(bdrs.survey.location.LAYER_NAME)[0];
                layer.removeFeatures(layer.features);

                var lonLat = new OpenLayers.LonLat(
                    feature.geometry.y, feature.geometry.x);
                lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
                                          bdrs.map.GOOGLE_PROJECTION);
                layer.addFeatures(new OpenLayers.Feature.Vector(
                    new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat)));
            });
        }
        else {
            jQuery('input[name=latitude]').val("").blur();
            jQuery('input[name=longitude]').val("").blur();
        }
    }

    jQuery(function() {
        var layerName = bdrs.survey.location.LAYER_NAME;
        bdrs.map.initBaseMap('base_map', { geocode: { selector: '#geocode' }});
        
        <c:choose>
            <c:when test="<%= survey.isPredefinedLocationsOnly() %>">
                var layer = bdrs.map.addPositionLayer(layerName);                          
            </c:when>
            <c:otherwise>
                var layer = bdrs.map.addSingleClickPositionLayer(layerName, 'input[name=latitude]', 'input[name=longitude]');
            </c:otherwise>
        </c:choose>

        var lat = jQuery('input[name=latitude]');
        var lon = jQuery('input[name=longitude]');
        if(lat.val().length > 0 && lon.val().length > 0) {
            var lonLat = new OpenLayers.LonLat(
                    parseFloat(lon.val()), parseFloat(lat.val()));
            lonLat = lonLat.transform(bdrs.map.WGS84_PROJECTION,
                                      bdrs.map.GOOGLE_PROJECTION);
            var feature = new OpenLayers.Feature.Vector(
                new OpenLayers.Geometry.Point(lonLat.lon, lonLat.lat));
            layer.addFeatures(feature);
            bdrs.map.baseMap.setCenter(lonLat);
        }
    });
</script>